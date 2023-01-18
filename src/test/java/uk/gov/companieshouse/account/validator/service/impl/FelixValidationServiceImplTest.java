package uk.gov.companieshouse.account.validator.service.impl;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.account.validator.validation.ixbrl.Results;
import uk.gov.companieshouse.environment.EnvironmentReader;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
class FelixValidationServiceImplTest {
    private static final String ENV_VARIABLE_FELIX_VALIDATOR_URI = "FELIX_VALIDATOR_URI";
    private static final String ENV_VARIABLE_FELIX_VALIDATOR_URI_VALUE = "http://felix.url/validate";
    private static final String IXBRL_LOCATION = "s3://test-bucket/accounts/ixbrl-generated-name.html";
    private static final String IXBRL = getIxbrl();
    private static final String VALIDATION_STATUS_UNIT_TEST_FAILURE = "unit test failure";
    private static final String VALIDATION_STATUS_OK = "OK";

    @Mock
    private RestTemplate restTemplateMock;
    @Mock
    private EnvironmentReader environmentReaderMock;
    private FelixValidationServiceImpl felixValidationService;

    @BeforeEach
    void init(TestInfo info) {
        if (info.getDisplayName().contains("Felix validation")) {
            mockEnvironmentReaderGetMandatoryString(ENV_VARIABLE_FELIX_VALIDATOR_URI_VALUE);
        }else{
            mockEnvironmentReaderGetMandatoryString(null);
        }

        felixValidationService = new FelixValidationServiceImpl(
                restTemplateMock,
                environmentReaderMock);
    }
    @Test
    @DisplayName("Felix validation call is successful. Happy path")
    void validationSuccess() {
        mockEnvironmentReaderGetMandatoryString(ENV_VARIABLE_FELIX_VALIDATOR_URI_VALUE);

        felixValidationService = new FelixValidationServiceImpl(
                restTemplateMock,
                environmentReaderMock);

        Results results = new Results();
        results.setValidationStatus(VALIDATION_STATUS_OK);

        when(restTemplateMock.postForObject(any(URI.class), any(HttpEntity.class), eq(Results.class)))
            .thenReturn(results);

        assertTrue(validateIxbrl());
    }

    @Test
    @DisplayName("Felix validation fails due to unit test failure")
    void validationFailure() {

        Results results = new Results();
        results.setValidationStatus(VALIDATION_STATUS_UNIT_TEST_FAILURE);

        when(restTemplateMock.postForObject(any(URI.class), any(HttpEntity.class), eq(Results.class)))
            .thenReturn(results);

        assertFalse(validateIxbrl());
    }

    @Test
    @DisplayName("Felix validation fails due to missing response")
    void validationMissingResponse() {

        when(restTemplateMock.postForObject(any(URI.class), any(HttpEntity.class), eq(Results.class)))
            .thenReturn(null);

        assertFalse(validateIxbrl());
    }

    @Test
    @DisplayName("Felix validation fails due to invalid response")
    void invalidResponse() {

        when(restTemplateMock.postForObject(any(URI.class), any(HttpEntity.class), eq(Results.class)))
            .thenThrow(new RestClientException(VALIDATION_STATUS_UNIT_TEST_FAILURE));

        assertFalse(validateIxbrl());
    }

    @Test
    @DisplayName("Missing environment variable")
    void missingEnvVariable() {
        assertFalse(validateIxbrl());
    }

    private void mockEnvironmentReaderGetMandatoryString(String returnedMandatoryValue) {

        when(environmentReaderMock.getMandatoryString(ENV_VARIABLE_FELIX_VALIDATOR_URI))
                .thenReturn(returnedMandatoryValue);

    }

    private boolean validateIxbrl() {
        return felixValidationService.validate(IXBRL, IXBRL_LOCATION);
    }

    private static String getIxbrl() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<html xmlns:ixt2=\"http://www.xbrl.org/inlineXBRL/transformation/2011-07-31\">\n"
            + "  <head>\n"
            + "    <meta content=\"application/xhtml+xml; charset=UTF-8\" http-equiv=\"content-type\" />\n"
            + "    <title>\n"
            + "            TEST COMPANY\n"
            + "        </title>\n"
            + "  <body xml:lang=\"en\">\n"
            + "    <div class=\"accounts-body \">\n"
            + "      <div id=\"your-account-type\" class=\"wholedoc\">\n"
            + "      </div>\n"
            + "    </div>\n"
            + "   </body>\n"
            + "</html>\n";
    }
}
