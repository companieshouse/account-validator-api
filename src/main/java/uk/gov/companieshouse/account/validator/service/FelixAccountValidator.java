package uk.gov.companieshouse.account.validator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.account.validator.exceptionhandler.XBRLValidationException;
import uk.gov.companieshouse.account.validator.model.felix.ixbrl.Results;
import uk.gov.companieshouse.account.validator.model.validation.RequestStatus;
import uk.gov.companieshouse.account.validator.repository.RequestStatusRepository;
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

    @Value("${internal.api.base.path}")
    private String internalApiUrl;

    @Value("${felix.validator.url}")
    private String felixValidatorUrl;

    @Autowired
    public FelixAccountValidator(Logger logger, RequestStatusRepository statusRepository, RestTemplate restTemplate) {
        this.logger = logger;
        this.statusRepository = statusRepository;
        this.restTemplate = restTemplate;
    }

    /**
     * Validate submits the file to the felix validator for validation
     *
     * @param file An IXBRL file or a ZIP file containing the accounts
     * @return the result of the validation
     */
    @Override
    public void startValidation(FileDetailsApi file) throws XBRLValidationException {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("fileId", file.getId());
        body.add("callbackUrl", getCallbackUrl(file.getId()));
        body.add("isBase64", "false");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        String url = felixValidatorUrl + "/validate-async";
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new XBRLValidationException("Failed to validate the file. Response: " + response);
        }
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