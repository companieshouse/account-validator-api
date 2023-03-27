package uk.gov.companieshouse.account.validator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.account.validator.exceptionhandler.ResponseException;
import uk.gov.companieshouse.account.validator.exceptionhandler.ValidationException;
import uk.gov.companieshouse.account.validator.model.File;
import uk.gov.companieshouse.account.validator.model.validation.RequestStatus;
import uk.gov.companieshouse.account.validator.model.validation.ValidationRequest;
import uk.gov.companieshouse.account.validator.model.validation.ValidationResponse;
import uk.gov.companieshouse.account.validator.repository.RequestStatusRepository;
import uk.gov.companieshouse.account.validator.service.AccountValidationStrategy;
import uk.gov.companieshouse.account.validator.service.file.transfer.FileTransferStrategy;
import uk.gov.companieshouse.logging.Logger;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

@Controller
@RequestMapping("/validate")
public class AccountValidationController {

    private final AccountValidationStrategy accountValidationStrategy;
    private final FileTransferStrategy fileTransferStrategy;
    private final Logger logger;
    private final Executor executor;
    private final RequestStatusRepository statusRepository;

    @Autowired
    public AccountValidationController(AccountValidationStrategy accountValidationStrategy,
                                       FileTransferStrategy fileTransferStrategy,
                                       Logger logger,
                                       Executor executor,
                                       RequestStatusRepository statusRepository) {
        this.accountValidationStrategy = accountValidationStrategy;
        this.fileTransferStrategy = fileTransferStrategy;
        this.logger = logger;
        this.executor = executor;
        this.statusRepository = statusRepository;
    }

    /**
     * Handles the request to validate an accounts file.
     * Starts a background process that will validate the file and save the result in the repository
     * when complete.
     *
     * @param validationRequest the request data
     * @return 404 if no file with that id is found, 200 and PENDING status otherwise
     */
    @PostMapping
    public ResponseEntity<?> submitForValidation(
            @Valid @RequestBody ValidationRequest validationRequest) {

        var fileId = validationRequest.getId();
        var optionalFile = fileTransferStrategy.get(fileId);
        if (optionalFile.isEmpty()) {
            return ValidationResponse.fileNotFound();
        }

        var file = optionalFile.get();

        RequestStatus pendingStatus = RequestStatus.pending(fileId, file.getName());
        statusRepository.save(pendingStatus);

        executor.execute(() -> {
            var result = accountValidationStrategy.validate(file);
            var requestStatus = RequestStatus.complete(fileId, file.getName(), result);
            statusRepository.save(requestStatus);
        });

        return ValidationResponse.success(pendingStatus);
    }

    /**
     * Checks the status of a validation request by retrieving the status from the repository
     * Can be pending or complete, and a complete request will include the validation result.
     *
     * @param fileId the id of the file for which validation was requested
     * @return 404 if there is no request for a file with that id, 200 and the status otherwise
     */
    @GetMapping("/check/{fileId}")
    ResponseEntity<?> getStatus(@PathVariable final String fileId) {
        var requestStatus = statusRepository.findById(fileId);
        if (requestStatus.isEmpty()) {
            return ValidationResponse.requestNotFound();
        }

        return ValidationResponse.success(requestStatus.get());
    }

    /**
     * Handles the exception thrown when the request body is empty
     *
     * @return 400 bad request response
     */
    @ExceptionHandler({HttpMessageNotReadableException.class})
    ResponseEntity<?> noBodyException() {
        return ResponseEntity.badRequest().body("Request required a body");
    }

    /**
     * Handles the exception thrown when there's a response problem
     *
     * @return 400 bad request response
     */
    @ExceptionHandler({ResponseException.class})
    ResponseEntity<?> responseException() {
        return ResponseEntity.badRequest().body("Api Response failed");
    }

    /**
     * Handles the exception thrown when there's a validation problem
     *
     * @return 400 bad request response
     */
    @ExceptionHandler({ValidationException.class})
    ResponseEntity<?> validationException() {
        return ResponseEntity.badRequest().body("Validation failed");
    }

    /**
     * Handles all un-caught exceptions
     *
     * @param ex the exception
     * @return 500 internal server error response
     */
    @ExceptionHandler
    ResponseEntity<?> exceptionHandler(Exception ex) {
        logger.error("Unhandled exception", ex);

        return ResponseEntity.internalServerError().build();
    }

    /**
     * handles delete request based on the file status complete.
     *
     */
    @DeleteMapping("/cleanup-submissions")
    ResponseEntity<List<RequestStatus>> delete() {
        List<RequestStatus> requestStatus = statusRepository.findByStatus("complete");
        for (RequestStatus rs:requestStatus) {
            fileTransferStrategy.delete(rs.getFileId());
            Optional<File> file = fileTransferStrategy.get(rs.getFileId());
            if(file.isEmpty()){
                statusRepository.deleteById(rs.getFileId());
            }else{
                logger.error("File has not been deleted from S3 bucket:" + rs.getFileId());
            }
        };
        return ResponseEntity.ok(requestStatus);
    }
}
