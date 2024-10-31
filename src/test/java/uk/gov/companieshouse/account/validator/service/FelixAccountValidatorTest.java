package uk.gov.companieshouse.account.validator.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import uk.gov.companieshouse.account.validator.exceptionhandler.XBRLValidationException;
import uk.gov.companieshouse.account.validator.model.content.AccountsDetails;
import uk.gov.companieshouse.account.validator.repository.RequestStatusRepository;
import uk.gov.companieshouse.account.validator.service.factory.request.status.RequestStatusFactory;
import uk.gov.companieshouse.api.handler.felixvalidator.PrivateFelixValidatorResourceHandler;
import uk.gov.companieshouse.api.handler.felixvalidator.request.PrivateModelFelixValidatorValidateAsync;
import uk.gov.companieshouse.api.model.felixvalidator.AsyncValidationRequestApi;
import uk.gov.companieshouse.api.model.felixvalidator.PackageTypeApi;
import uk.gov.companieshouse.api.model.filetransfer.FileDetailsApi;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class FelixAccountValidatorTest {

    @Mock
    private PrivateFelixValidatorResourceHandler felixClient;

    @Mock
    private Logger logger;

    @Mock
    private RequestStatusRepository statusRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private FileDetailsApi file;

    @Mock
    private AccountsDetails fileContent;

    @Captor
    private ArgumentCaptor<AsyncValidationRequestApi> asyncValidationRequestApiCaptor;

    @Mock
    private PrivateModelFelixValidatorValidateAsync validateAsync;
    
    @Mock
    private RequestStatusFactory requestStatusFactory;


    private FelixAccountValidator felixAccountValidator;

    @BeforeEach
    void beforeEach() {
        felixAccountValidator = new FelixAccountValidator(logger, statusRepository, restTemplate, felixClient, requestStatusFactory);
    }

    @Test
    @DisplayName("FelixAccountValidator with packageType")
    void testWithPackageType() throws XBRLValidationException {
        when(file.getId()).thenReturn("null");
        when(fileContent.getPackageType()).thenReturn(PackageTypeApi.UKSEF);
        when(felixClient.validateAsync(asyncValidationRequestApiCaptor.capture())).thenReturn(validateAsync);
        felixAccountValidator.startValidation(file, fileContent);

        verify(felixClient, times(1)).validateAsync(asyncValidationRequestApiCaptor.capture());
        assertEquals("null", asyncValidationRequestApiCaptor.getValue().getFileId());
        assertEquals(PackageTypeApi.UKSEF, asyncValidationRequestApiCaptor.getValue().getPackageType());
    }

    @Test
    @DisplayName("FelixAccountValidator without packageType")
    void testWithoutPackageType() throws XBRLValidationException {
        when(file.getId()).thenReturn("null");
        when(fileContent.getPackageType()).thenReturn(null);
        when(felixClient.validateAsync(asyncValidationRequestApiCaptor.capture())).thenReturn(validateAsync);
        felixAccountValidator.startValidation(file, fileContent);

        verify(felixClient, times(1)).validateAsync(asyncValidationRequestApiCaptor.capture());
        assertEquals("null", asyncValidationRequestApiCaptor.getValue().getFileId());
        assertEquals(null, asyncValidationRequestApiCaptor.getValue().getPackageType());
    }

//    private FelixAccountValidator underTest;
//
//    private MockRestServiceServer mockServer;
//
//    private static final String TNEP_BASE64_URL = "http://dummytnep.companieshouse.gov.uk/validateBase64";
//
//    private static final String VALIDATION_SUCCESS_RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
//            + "<results validationStatus=\"OK\">"
//            + "<data>"
//            + "<BalanceSheetDate>2016-12-31</BalanceSheetDate>"
//            + "<AccountsType>08</AccountsType>"
//            + "<CompaniesHouseRegisteredNumber>00006400</CompaniesHouseRegisteredNumber>"
//            + "</data>"
//            + "</results>";
//
//    private static final String VALIDATION_FAILURE_RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
//            + "<results validationStatus=\"FAILED\">"
//            + "<errors>"
//            + "<ErrorMessage>AccountsTypeFullOrAbbreviated must be provided for the current accounting period.</ErrorMessage>"
//            + "</errors>"
//            + "<data>"
//            + "<BalanceSheetDate>2016-12-31</BalanceSheetDate>"
//            + "<AccountsType>08</AccountsType>"
//            + "<CompaniesHouseRegisteredNumber>00006400</CompaniesHouseRegisteredNumber>"
//            + "</data>"
//            + "</results>";
//
//    @BeforeEach
//    void setUp() {
//        underTest = new FelixAccountValidator(logger, statusRepository, restTemplate) {
//            @Override
//            protected String getIxbrlValidatorBase64Uri() {
//                return TNEP_BASE64_URL;
//            }
//        };
//        RestTemplate restTemplate = new RestTemplate();
//        underTest.setRestTemplate(restTemplate);
//
//        mockServer = MockRestServiceServer.createServer(restTemplate);
//    }
//
//    @Test
//    void validationSuccess() throws XBRLValidationException {
//        File f = new File("anything", "anything", "anything".getBytes());
//
//        mockServer.expect(requestTo(TNEP_BASE64_URL))
//                .andExpect(method(HttpMethod.POST))
//                .andRespond(withSuccess(VALIDATION_SUCCESS_RESPONSE, MediaType.APPLICATION_XML));
//
//        Results actual = underTest.validate(f);
//
//        assertNotNull(actual);
//        assertEquals("OK", actual.getValidationStatus());
//
//        mockServer.verify();
//    }
//
//    @Test
//    void validationFailure() throws XBRLValidationException {
//        File f = new File("anything", "anything", "anything".getBytes());
//
//        mockServer.expect(requestTo(TNEP_BASE64_URL))
//                .andExpect(method(HttpMethod.POST))
//                .andRespond(withSuccess(VALIDATION_FAILURE_RESPONSE, MediaType.APPLICATION_XML));
//
//        Results actual = underTest.validate(f);
//
//        assertNotNull(actual);
//        assertEquals("FAILED", actual.getValidationStatus());
//
//        mockServer.verify();
//    }
//
//    @Test
//    void invalidResponse() throws XBRLValidationException {
//        File f = new File("anything", "anything", "anything".getBytes());
//
//        mockServer.expect(requestTo(TNEP_BASE64_URL))
//                .andExpect(method(HttpMethod.POST))
//                .andRespond(withSuccess("", MediaType.APPLICATION_XML));
//
//        Results actual = underTest.validate(f);
//
//        assertNull(actual);
//
//        mockServer.verify();
//    }
//
//    @Test
//    void checkMissingEnvVar() throws XBRLValidationException {
//        File f = new File("anything", "anything", "anything".getBytes());
//
//        underTest = new FelixAccountValidator(logger, statusRepository, restTemplate) {
//            @Override
//            protected String getIxbrlValidatorBase64Uri() {
//                return null;
//            }
//        };
//
//        Results actual = underTest.validate(f);
//
//        assertNull(actual);
//    }
}
