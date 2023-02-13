package uk.gov.companieshouse.account.validator.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.account.validator.exceptionhandler.MissingEnvironmentVariableException;
import uk.gov.companieshouse.account.validator.model.File;
import uk.gov.companieshouse.account.validator.model.felix.ixbrl.Results;
import uk.gov.companieshouse.account.validator.model.validation.ValidationData;
import uk.gov.companieshouse.account.validator.model.validation.ValidationResult;
import uk.gov.companieshouse.account.validator.model.validation.ValidationStatus;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.net.URI;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A method of validating accounts via the FelixValidator
 */
public class FelixAccountValidator implements AccountValidationStrategy {

    private static final String IXBRL_VALIDATOR_URI = "IXBRL_VALIDATOR_URI";

    private static final Logger LOG = LoggerFactory.getLogger("account.validator.api");

    //todo move to env var
    String FELIX_ENDPOINT = "http://tnep.aws.chdev.org:8080/validateBase64";

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Validate submits the file to the felix validator for validation
     *
     * @param file An IXBRL file or a ZIP file containing the accounts
     * @return the result of the validation
     */
    @Override
    public ValidationResult validate(File file) {

        String s3Key = file.getName();
        String url = getIxbrlValidatorUri();

        try {
            byte[] fileContent = Base64.getEncoder().encodeToString(file.getData()).getBytes();

            //Connect to TNEP validator via http POST using multipart file upload
            LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
            map.add("file", new FileMessageResource(fileContent, s3Key));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);

            LOG.debug(String.format("Calling Felix Ixbrl Validation with file downloaded from S3 with key '%s'", s3Key));
            Results results = restTemplate.postForObject(new URI(url), requestEntity, Results.class);
            LOG.debug("Call to Felix Ixbrl Validation was successfully made");

            //todo combine Results & ValidationResult
            Set errorMessage = new HashSet<>();
            if (results.getErrors() != null) {
                errorMessage.add(results.getErrors().getErrorMessage());
            }
            ValidationData validationData = new ValidationData(results.getData().getBalanceSheetDate(), results.getData().getAccountsType(), results.getData().getCompaniesHouseRegisteredNumber());
            ValidationStatus validationStatus = ("OK".equalsIgnoreCase(results.getValidationStatus()) ? ValidationStatus.OK : ValidationStatus.FAILED);
            ValidationResult validationResult = new ValidationResult(errorMessage, validationData, validationStatus);

            if (results != null && "OK".equalsIgnoreCase(results.getValidationStatus())) {
                Map<String, Object> logMap = new HashMap<>();
                logMap.put("location", s3Key);
                logMap.put("results", results);
                LOG.debug("Ixbrl validation succeeded", logMap);

                return validationResult;
            } else {
                Map<String, Object> logMap = new HashMap<>();
                logMap.put("location", s3Key);
                logMap.put("results", results);
                LOG.error("Ixbrl validation failed", logMap);

                return validationResult;
            }

        } catch (Exception e) {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("error", "Unable to validate ixbrl");
            logMap.put("location", s3Key);
            LOG.error(e, logMap);

            return null;
        }
    }

    /**
     * Obtain the URL of the TNEP validator
     *
     * @return String
     */
    protected String getIxbrlValidatorUri() {

        String ixbrlValidatorUri = getIxbrlValidatorUriEnvVal();
        if (StringUtils.isBlank(ixbrlValidatorUri)) {
            throw new MissingEnvironmentVariableException("Missing IXBRL_VALIDATOR_URI environment variable");
        }

        return ixbrlValidatorUri;
    }

    protected String getIxbrlValidatorUriEnvVal() {
        return System.getenv(IXBRL_VALIDATOR_URI);
    }

    private class FileMessageResource extends ByteArrayResource {

        /**
         * The filename to be associated with the {@link MimeMessage} in the form data.
         */
        private final String filename;

        /**
         * Constructs a new {@link FileMessageResource}.
         *
         * @param byteArray A byte array containing data from a {@link MimeMessage}.
         * @param filename  The filename to be associated with the {@link MimeMessage} in
         *                  the form data.
         */
        public FileMessageResource(final byte[] byteArray, final String filename) {
            super(byteArray);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}
