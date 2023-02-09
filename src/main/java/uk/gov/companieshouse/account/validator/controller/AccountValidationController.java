package uk.gov.companieshouse.account.validator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.companieshouse.account.validator.model.validation.RequestStatus;
import uk.gov.companieshouse.account.validator.model.validation.ValidationRequest;
import uk.gov.companieshouse.account.validator.model.validation.ValidationResponse;
import uk.gov.companieshouse.account.validator.repository.RequestStatusRepository;
import uk.gov.companieshouse.account.validator.service.AccountValidationStrategy;
import uk.gov.companieshouse.account.validator.service.file.transfer.FileTransferStrategy;
import uk.gov.companieshouse.logging.Logger;

import javax.validation.Valid;
import java.util.concurrent.Executor;

@Controller
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
    @PostMapping("/validate")
    public ResponseEntity<?> submitForValidation(
            @Valid @RequestBody ValidationRequest validationRequest) {

        var fileId = validationRequest.getId();
        var file = fileTransferStrategy.get(fileId);
        if (file.isEmpty()) {
            return ValidationResponse.fileNotFound();
        }

        executor.execute(() -> {
            var result = accountValidationStrategy.validate(file.get());
            var requestStatus = RequestStatus.complete(fileId, result);
            statusRepository.save(requestStatus);
        });

        return ValidationResponse.success(RequestStatus.pending(fileId));
    }

    /**
     * Checks the status of a validation request by retrieving the status from the repository
     * Can be pending or complete, and a complete request will include the validation result.
     *
     * @param fileId the id of the file for which validation was requested
     * @return 404 if there is no request for a file with that id, 200 and the status otherwise
     */
    @GetMapping("/validate/check/{fileId}")
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
}
