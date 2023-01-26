package uk.gov.companieshouse.account.validator.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.account.validator.service.AccountValidationStrategy;
import uk.gov.companieshouse.account.validator.service.DummyValidator;
import uk.gov.companieshouse.account.validator.service.retry.IncrementalBackoff;
import uk.gov.companieshouse.account.validator.service.retry.RetryStrategy;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.time.Duration;
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
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Executor getExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    @Bean
    public RetryStrategy fileTransferRetryStrategy(
            @Value("${file.transfer.retry.base.delay.seconds}") long baseDelay,
            @Value("${file.transfer.retry.delay.increment.seconds}") long delayIncrement,
            @Value("${file.transfer.retry.timeout.seconds}") long timeout,
            @Value("${file.transfer.retry.max.delay.seconds}") long maxDelay) {
        return new IncrementalBackoff(
                Duration.ofSeconds(baseDelay),
                Duration.ofSeconds(delayIncrement),
                Duration.ofSeconds(timeout),
                Duration.ofSeconds(maxDelay));
    }

    @Bean
    public AccountValidationStrategy accountValidationStrategy() {
        return new DummyValidator();
    }
}

