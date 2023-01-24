package uk.gov.companieshouse.account.validator.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.account.validator.service.AccountValidationStrategy;
import uk.gov.companieshouse.account.validator.service.TNDPAccountValidator;
import uk.gov.companieshouse.account.validator.service.file.transfer.FileTransferService;
import uk.gov.companieshouse.account.validator.service.file.transfer.FileTransferStrategy;
import uk.gov.companieshouse.charset.validation.CharSetValidation;
import uk.gov.companieshouse.charset.validation.impl.CharSetValidationImpl;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class ApplicationConfiguration {
    @Value("${application.namespace}")
    private String applicationNameSpace;

    @Bean
    public Logger logger() {
        return LoggerFactory.getLogger(applicationNameSpace);
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

    @Bean
    public Executor getExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    @Bean
    public AccountValidationStrategy getAccountValidationStrategy() {
        return new TNDPAccountValidator();
    }

    @Bean
    public FileTransferStrategy getFileTransferStrategy() {
        return new FileTransferService();
    }
}

