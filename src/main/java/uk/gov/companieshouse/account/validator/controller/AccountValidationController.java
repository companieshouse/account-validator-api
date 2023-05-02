package uk.gov.companieshouse.account.validator.controller;

import static org.springframework.http.MediaType.APPLICATION_PDF;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.account.validator.exceptionhandler.DeleteCompleteSubException;
import uk.gov.companieshouse.account.validator.exceptionhandler.MissingEnvironmentVariableException;
import uk.gov.companieshouse.account.validator.exceptionhandler.ResponseException;
import uk.gov.companieshouse.account.validator.exceptionhandler.ValidationException;
import uk.gov.companieshouse.account.validator.model.validation.RequestStatus;
import uk.gov.companieshouse.account.validator.model.validation.ValidationRequest;
import uk.gov.companieshouse.account.validator.model.validation.ValidationResponse;
import uk.gov.companieshouse.account.validator.repository.RequestStatusRepository;
import uk.gov.companieshouse.account.validator.service.AccountValidationStrategy;
import uk.gov.companieshouse.account.validator.service.file.transfer.FileTransferStrategy;
import uk.gov.companieshouse.account.validator.service.maintenance.AccountMaintenanceService;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.logging.Logger;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

@Controller
@RequestMapping("/account-validator/validate")
public class AccountValidationController {

    private static final String IXBRL_TO_PDF_URI_KEY = "IXBRL_TO_PDF_URI";

    private final AccountValidationStrategy accountValidationStrategy;
    private final FileTransferStrategy fileTransferStrategy;
    private final Logger logger;
    private final Executor executor;
    private final RequestStatusRepository statusRepository;
    private final RestTemplate restTemplate;
    private final EnvironmentReader environmentReader;
    private final AccountMaintenanceService accountMaintenanceService;


    @Autowired
    public AccountValidationController(AccountValidationStrategy accountValidationStrategy,
                                       FileTransferStrategy fileTransferStrategy,
                                       Logger logger,
                                       Executor executor,
                                       RequestStatusRepository statusRepository,
                                       RestTemplate restTemplate, EnvironmentReader environmentReader,
                                       AccountMaintenanceService accountMaintenanceService) {
        this.accountValidationStrategy = accountValidationStrategy;
        this.fileTransferStrategy = fileTransferStrategy;
        this.logger = logger;
        this.executor = executor;
        this.statusRepository = statusRepository;
        this.restTemplate = restTemplate;
        this.environmentReader = environmentReader;
        this.accountMaintenanceService = accountMaintenanceService;
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
        Map<String, Object> loginfo = new HashMap<>();
        loginfo.put("fileId", fileId);
        logger.debugContext(fileId, "Getting file details", loginfo);

        var optionalFileDetails = fileTransferStrategy.getDetails(fileId);
        if (optionalFileDetails.isEmpty()) {
            return ValidationResponse.fileNotFound();
        }

        RequestStatus pendingStatus = RequestStatus.pending(fileId, optionalFileDetails.get().getName());
        statusRepository.save(pendingStatus);

        // Asynchronously start validation
        executor.execute(validateFile(fileId));

        logger.debugContext(fileId, "Returning pending status", loginfo);
        return ValidationResponse.success(pendingStatus);
    }

    private Runnable validateFile(String fileId) {
        return () -> {
            Map<String, Object> loginfo = new HashMap<>();
            loginfo.put("fileId", fileId);

            var optionalFile = fileTransferStrategy.get(fileId);
            if (optionalFile.isEmpty()) {
                throw new RuntimeException(String.format("No file with id [%s] found", fileId));
            }

            var file = optionalFile.get();
            var result = accountValidationStrategy.validate(file);
            var requestStatus = RequestStatus.complete(fileId, file.getName(), result);
            loginfo.put("result", requestStatus);
            logger.debugContext(fileId, "Saving result to db", loginfo);
            statusRepository.save(requestStatus);
            logger.debugContext(fileId, "Result saved to db", loginfo);
        };
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
     * Handles the request to transform a iXBRL file stored in S3 to PDF
     * NOTE: getFilename() is overridden as required when posting byte[]
     *
     * @param fileId of remote file
     * @return S3 file as PDF
     */
    @GetMapping(path = "/render/{fileId}")
    public ResponseEntity<?> render(@PathVariable String fileId) {
        var file = fileTransferStrategy.get(fileId);
        if (file.isEmpty()) {
            return ValidationResponse.fileNotFound();
        }

        String iXbrlToPdfUri = getIxbrlToPDFEnvVal();
        if (StringUtils.isBlank(iXbrlToPdfUri)) {
            throw new MissingEnvironmentVariableException(String.format("Missing '%s' environment variable", IXBRL_TO_PDF_URI_KEY));
        }

        ByteArrayResource contentsAsResource = new ByteArrayResource(file.get().getData()) {
            @Override
            public String getFilename() {
                return "anything";
            }
        };

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>() {{
            add("instance", contentsAsResource);
        }};

        byte[] bytes = restTemplate.postForObject(iXbrlToPdfUri, map, byte[].class);

        return ResponseEntity.ok().contentType(APPLICATION_PDF).body(bytes);
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
    ResponseEntity<?> responseException(ResponseException e) {
        logger.error("Unhandled response exception", e);
        return ResponseEntity.badRequest().body("Api Response failed. " + e.getMessage());
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
     * Obtain the URL of the Ixbrl to PDF generation service from the environment
     *
     * @return String
     */
    protected String getIxbrlToPDFEnvVal() {
        return environmentReader.getMandatoryString(IXBRL_TO_PDF_URI_KEY);
    }

    /**
     * Handles delete request based on the file status complete.
     * Delete files from S3 bucket & mongodb
     */
    @DeleteMapping("/cleanup-submissions")
    ResponseEntity<Void> delete() {
        accountMaintenanceService.deleteCompleteSubmissions();
        return ResponseEntity.noContent().build();
    }

    /**
     * Handles the exception thrown when there's a validation problem
     *
     * @return 500 internal server error response
     */
    @ExceptionHandler({DeleteCompleteSubException.class})
    ResponseEntity<?> deleteCompleteSubException() {
        return ResponseEntity.internalServerError().body("Delete complete submission activity failed");
    }
}
