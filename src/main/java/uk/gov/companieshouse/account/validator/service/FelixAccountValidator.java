package uk.gov.companieshouse.account.validator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.account.validator.exceptionhandler.XBRLValidationException;
import uk.gov.companieshouse.account.validator.model.content.AccountsDetails;
import uk.gov.companieshouse.account.validator.model.felix.ixbrl.Results;
import uk.gov.companieshouse.account.validator.model.validation.RequestStatus;
import uk.gov.companieshouse.account.validator.repository.RequestStatusRepository;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.felixvalidator.PrivateFelixValidatorResourceHandler;
import uk.gov.companieshouse.api.model.felixvalidator.AsyncValidationRequestApi;
import uk.gov.companieshouse.api.model.felixvalidator.PackageTypeApi;
import uk.gov.companieshouse.api.model.filetransfer.FileDetailsApi;
import uk.gov.companieshouse.logging.Logger;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A method of validating accounts via the FelixValidator
 */
public class FelixAccountValidator implements AccountValidationStrategy {

    private static final String IXBRL_VALIDATOR_BASE64_URI = "IXBRL_VALIDATOR_BASE64_URI";
    private final Logger logger;
    private final RequestStatusRepository statusRepository;
    private final RestTemplate restTemplate;
    private final PrivateFelixValidatorResourceHandler felixClient;

    @Value("${internal.api.base.path}")
    private String internalApiUrl;

    @Value("${felix.validator.url}")
    private String felixValidatorUrl;

    @Autowired
    public FelixAccountValidator(Logger logger,
            RequestStatusRepository statusRepository,
            RestTemplate restTemplate,
            PrivateFelixValidatorResourceHandler felixClient) {
        this.logger = logger;
        this.statusRepository = statusRepository;
        this.restTemplate = restTemplate;
        this.felixClient = felixClient;
    }

    /**
     * Validate submits the file to the felix validator for validation
     *
     * @param file        An IXBRL file or a ZIP file containing the accounts
     * @param fileContent Object contains information required for account/package
     *                    related requirement
     * @return the result of the validation
     */
    @Override
    public void startValidation(FileDetailsApi file, AccountsDetails fileContent) throws XBRLValidationException {
        String fileId = file.getId();
        String callbackUrl = getCallbackUrl(file.getId());
        AsyncValidationRequestApi asyncValidationRequestApi = new AsyncValidationRequestApi(fileId, callbackUrl,
                fileContent.getPackageType());
                
        try {
            logRequestApiInfo(asyncValidationRequestApi);
            felixClient.validateAsync(asyncValidationRequestApi).execute();
        } catch (URIValidationException e) {
            throw new RuntimeException(e);
        } catch (ApiErrorResponseException e) {
            throw new XBRLValidationException("Failed to validate the file. ApiErrorResponse", e);
        }
    }

    private void logRequestApiInfo(AsyncValidationRequestApi requestApi) {
        Map<String, Object> info = new HashMap<>(Map.of("fileId", requestApi.getFileId()));
        if (requestApi.getPackageType() != null) {
            info.put("packageType", requestApi.getPackageType());
        }
        logger.info("async felix validation request started", info);
    }

    @Override
    public void saveResults(String fileId, Results results) {
        Map<String, Object> logInfo = new HashMap<>();
        logInfo.put("results", results.toString());

        logger.info("Saving status for file id " + fileId, logInfo);

        String fileName = getStatus(fileId)
                .map(RequestStatus::getFileName)
                .orElse("");

        var requestStatus = RequestStatus.fromResults(fileId, results, fileName);
        logInfo.put("result", requestStatus);
        statusRepository.save(requestStatus);
        logger.debugContext(fileId, "Result saved to db", logInfo);
    }

    @Override
    public Optional<RequestStatus> getStatus(String fileId) {
        return statusRepository.findById(fileId);
    }

    private String getCallbackUrl(String fileId) {
        return internalApiUrl + Paths.get("/account-validator/validate", fileId).toString();
    }
}