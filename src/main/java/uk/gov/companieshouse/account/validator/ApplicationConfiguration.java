package uk.gov.companieshouse.account.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.charset.validation.CharSetValidation;
import uk.gov.companieshouse.charset.validation.impl.CharSetValidationImpl;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import static uk.gov.companieshouse.account.validator.AccountValidatorApplication.APPLICATION_NAME_SPACE;

@Configuration
@PropertySource("classpath:ValidationMessages.properties")
public class ApplicationConfiguration {

    @Value("${uk.gov.ch.felixvalidator.platformMaxDecodedSizeMB}")
    private int _platformMaxDecodedSizeMB;

    @Bean
    public Logger logger() {
        return LoggerFactory.getLogger(APPLICATION_NAME_SPACE);
    }

    @Bean
    public CharSetValidation getCharSetValidation() {
        return new CharSetValidationImpl();
    }

    @Bean
    public EnvironmentReader getEnvironmentReader() {
        return new EnvironmentReaderImpl();
    }

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @Bean public ObjectMapper objectMapper() {
        return new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);}
    public int getPlatformMaxDecodedSizeMB() {
        return _platformMaxDecodedSizeMB;
    }

}

