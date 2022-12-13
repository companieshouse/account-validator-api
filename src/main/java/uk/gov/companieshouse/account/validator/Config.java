package uk.gov.companieshouse.account.validator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import static uk.gov.companieshouse.account.validator.AccountValidatorApplication.NAMESPACE;


@Configuration
public class Config {

    @Bean
    public Logger logger() {
        return LoggerFactory.getLogger(NAMESPACE);
    }
}

