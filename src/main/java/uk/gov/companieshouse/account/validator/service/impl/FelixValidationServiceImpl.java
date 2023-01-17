package uk.gov.companieshouse.account.validator.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.account.validator.service.FelixValidationService;
import uk.gov.companieshouse.account.validator.validation.ixbrl.Results;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static uk.gov.companieshouse.account.validator.AccountValidatorApplication.APPLICATION_NAME_SPACE;

@Service
public class FelixValidationServiceImpl implements FelixValidationService {

    private static final String FELIX_VALIDATOR_URI = "FELIX_VALIDATOR_URI";

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private RestTemplate restTemplate;
    private EnvironmentReader environmentReader;

    private String felixUri;

    @Autowired
    public FelixValidationServiceImpl(RestTemplate restTemplate,
                                      EnvironmentReader environmentReader) {

        this.restTemplate = restTemplate;
        this.environmentReader = environmentReader;
        this.felixUri = getFelixValidatorUri();
    }

    /**
     * Validate the ixbrl
     *
     * @return boolean
     */
    @Override
    public boolean validate(String ixbrl, String location) {

        boolean isIxbrlValid = false;

        LOGGER.info("FelixValidationServiceImpl: Ixbrl validation has started");
        try {
            Results results = validatIxbrlAgainstFelix(ixbrl, location);

            if (hasPassedFelixValidation(results)) {
                addToLog(false, null, location,
                        "Ixbrl is valid. It has passed the FELIX validation");

                isIxbrlValid = true;

            } else {
                addToLog(true, null, location,
                        "Ixbrl is invalid. It has failed the FELIX validation");
            }

        } catch (Exception e) {
            addToLog(true, e, location,
                    "Exception has been thrown when calling FELIX validator. Unable to validate Ixbrl");
        }

        LOGGER.info("FelixValidationServiceImpl: Ixbrl validation has finished");

        return isIxbrlValid;
    }

    /**
     * Call FELIX validator service, via http POST using multipart file upload, to check if ixbrl is
     * valid.
     *
     * @param ixbrl - ixbrl content to be validated.
     * @param location - ixbrl location, public location.
     * @return {@link Results} with the information from calling the Felix service.
     * @throws URISyntaxException
     */
    private Results validatIxbrlAgainstFelix(String ixbrl, String location)
            throws URISyntaxException {

        LinkedMultiValueMap<String, Object> map = createFileMessageResource(ixbrl, location);
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = setHttpHeaders(map);

        return postForValidation(requestEntity);
    }

    private boolean hasPassedFelixValidation(Results results) {
        return results != null && "OK".equalsIgnoreCase(results.getValidationStatus());
    }

    /**
     * Connect to the FELIX validator via http POST using multipart file upload
     *
     * @return RestTemplate
     */
    private Results postForValidation(HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity)
            throws URISyntaxException {

        return restTemplate
                .postForObject(new URI(this.felixUri), requestEntity, Results.class);
    }

    private void addToLog(boolean hasValidationFailed, Exception e,
                          String location, String message) {

        Map<String, Object> logMap = new HashMap<>();
        logMap.put("message", message);
        logMap.put("location", location);

        if (hasValidationFailed) {
            LOGGER.error("FelixValidationServiceImpl: validation has failed", e, logMap);
        } else {
            LOGGER.debug("FelixValidationServiceImpl: validation has passed", logMap);
        }
    }

    /**
     * Add http Header attributes for validation POST
     *
     * @Return HttpEntity<>(LinkedMultiValueMap<String, Object> , HttpHeaders);
     */
    private HttpEntity<LinkedMultiValueMap<String, Object>> setHttpHeaders(
            LinkedMultiValueMap<String, Object> map) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        return new HttpEntity<>(map,
                headers);
    }

    private LinkedMultiValueMap<String, Object> createFileMessageResource(String ixbrl,
                                                                          String location) {
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", new FileMessageResource(ixbrl.getBytes(), location));

        return map;
    }

    /**
     * Obtain the URL of the FELIX validator from the environment
     *
     * @return String
     */
    protected String getFelixValidatorUri() {

        return environmentReader.getMandatoryString(FELIX_VALIDATOR_URI);
    }

    private static class FileMessageResource extends ByteArrayResource {

        /**
         * The filename to be associated with the {@link MimeMessage} in the form data.
         */
        private final String filename;

        /**
         * Constructs a new {@link FileMessageResource}.
         *
         * @param byteArray A byte array containing data from a {@link MimeMessage}.
         * @param filename The filename to be associated with the {@link MimeMessage} in the form
         * data.
         */
        public FileMessageResource(final byte[] byteArray, final String filename) {
            super(byteArray);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof FileMessageResource)) {
                return false;
            }

            if (!super.equals(o)) {
                return false;
            }

            FileMessageResource that = (FileMessageResource) o;
            return Objects.equals(filename, that.filename);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), filename);
        }
    }
}
