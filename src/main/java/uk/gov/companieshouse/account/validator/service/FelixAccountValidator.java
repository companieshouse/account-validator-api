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
import uk.gov.companieshouse.account.validator.exceptionhandler.XBRLValidationException;
import uk.gov.companieshouse.account.validator.model.File;
import uk.gov.companieshouse.account.validator.model.felix.ixbrl.Results;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.net.URI;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * A method of validating accounts via the FelixValidator
 */
public class FelixAccountValidator implements AccountValidationStrategy {

    private static final String IXBRL_VALIDATOR_BASE64_URI = "IXBRL_VALIDATOR_BASE64_URI";

    private static final Logger LOG = LoggerFactory.getLogger("account.validator.api");

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Validate submits the file to the felix validator for validation
     *
     * @param file An IXBRL file or a ZIP file containing the accounts
     * @return the result of the validation
     */
    @Override
    public Results validate(File file) throws XBRLValidationException {
        String s3Key = file.getName();
        String url = getIxbrlValidatorBase64Uri();
        Map<String, Object> logMap = new HashMap<>();

        logMap.put("location", s3Key);

        try {
            byte[] fileContent = Base64.getEncoder().encodeToString(file.getData()).getBytes();

            //Connect to TNEP validator via http POST using multipart file upload
            LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
            map.add("file", new FileMessageResource(fileContent, s3Key));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);

            LOG.debugContext(file.getId(), String.format("Calling Felix Ixbrl Validation with file downloaded from S3 with key '%s'", s3Key), logMap);
            Results results = restTemplate.postForObject(new URI(url), requestEntity, Results.class);
            LOG.debugContext(file.getId(), "Call to Felix Ixbrl Validation was successfully made", null);

            String logMessage = "Ixbrl validation " + ((results != null && "OK".equalsIgnoreCase(results.getValidationStatus())) ? "succeeded" : "failed");
            logMap.put("results", results);
            LOG.debugContext(file.getId(), logMessage, logMap);

            return results;
        } catch (Exception e) {
            logMap.put("error", "Unable to validate ixbrl");
            LOG.errorContext(file.getId(), e, logMap);

            return null;
        }
    }

    /**
     * Obtain the URL of the TNEP validator
     *
     * @return String
     */
    protected String getIxbrlValidatorBase64Uri() {

        String ixbrlValidatorUri = getIxbrlValidatorUriEnvVal();
        if (StringUtils.isBlank(ixbrlValidatorUri)) {
            throw new MissingEnvironmentVariableException("Missing  environment variable");
        }

        return ixbrlValidatorUri;
    }

    /**
     * Obtain the URL of the TNEP validator from the environment
     *
     * @return String
     */
    protected String getIxbrlValidatorUriEnvVal() {
        return System.getenv(IXBRL_VALIDATOR_BASE64_URI);
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private class FileMessageResource extends ByteArrayResource {
        /**
         * The filename to be associated with the  MimeMessage in the form data.
         */
        private final String filename;

        /**
         * Constructs a new {@link FileMessageResource}.
         *
         * @param byteArray A byte array containing data from a MimeMessage.
         * @param filename  The filename to be associated with the MimeMessage in
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