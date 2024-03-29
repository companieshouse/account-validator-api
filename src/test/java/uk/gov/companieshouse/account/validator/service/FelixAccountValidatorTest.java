package uk.gov.companieshouse.account.validator.service;

class FelixAccountValidatorTest {

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
