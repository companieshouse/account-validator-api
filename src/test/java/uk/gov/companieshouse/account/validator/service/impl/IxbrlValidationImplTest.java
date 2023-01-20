package uk.gov.companieshouse.account.validator.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;


public class IxbrlValidationImplTest {


    private IxbrlValidationImpl ixbrlValidationImpl;
    
    private MockRestServiceServer mockServer;
    
    private static final String TNEP_URL = "http://dummyfelix.companieshouse.gov.uk/validate";
    
    private static final String VALIDATION_FAILURE_RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<results validationStatus=\"FAILED\">"
                   + "<errors>"
                       + "<ErrorMessage>AccountsTypeFullOrAbbreviated must be provided for the current accounting period.</ErrorMessage>"
                   + "</errors>"
                   + "<data>"
                      + "<BalanceSheetDate>2016-12-31</BalanceSheetDate>"
                      + "<AccountsType>08</AccountsType>"
                      + "<CompaniesHouseRegisteredNumber>00006400</CompaniesHouseRegisteredNumber>"
                   + "</data>"
             + "</results>";
    
    private static final String VALIDATION_SUCCESS_RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<results validationStatus=\"OK\">"
                   + "<data>"
                      + "<BalanceSheetDate>2016-12-31</BalanceSheetDate>"
                      + "<AccountsType>08</AccountsType>"
                      + "<CompaniesHouseRegisteredNumber>00006400</CompaniesHouseRegisteredNumber>"
                   + "</data>"
             + "</results>";
    
    @BeforeEach
    public void setUp() {
        ixbrlValidationImpl = new IxbrlValidationImpl() {
            @Override
            protected String getIxbrlValidatorUri() {
                return TNEP_URL;
            }
        };
        
        RestTemplate restTemplate = new RestTemplate();
        ixbrlValidationImpl.setRestTemplate(restTemplate);
        
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }
 
    @Test
    void validationSucess() {

        mockServer.expect(requestTo(TNEP_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(VALIDATION_SUCCESS_RESPONSE, MediaType.APPLICATION_XML));

        boolean result = ixbrlValidationImpl.validate("test", "s3://notabucket/blah");

        assertTrue(result);

        mockServer.verify();
    }
    
    @Test
    void validationFailure() {

        mockServer.expect(requestTo(TNEP_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(VALIDATION_FAILURE_RESPONSE, MediaType.APPLICATION_XML));
        
        boolean result = ixbrlValidationImpl.validate("test", "s3://notabucket/blah");
        
        assertFalse(result);

        mockServer.verify();
    }
    
    @Test
    void invalidResponse() {

        mockServer.expect(requestTo(TNEP_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess("", MediaType.APPLICATION_XML));
        
        boolean result = ixbrlValidationImpl.validate("test", "s3://notabucket/blah");
        
        assertFalse(result);

        mockServer.verify();
    }
    
    @Test
    void checkMissingEnvVar() {

        ixbrlValidationImpl = new IxbrlValidationImpl() {
            @Override
            protected String getIxbrlValidatorUriEnvVal() {
                return null;
            }
        };
        
        boolean result = ixbrlValidationImpl.validate("test", "s3://notabucket/blah");
        
        assertFalse(result);
    }
}
