package uk.gov.companieshouse.account.validator.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.account.validator.service.AccountValidationStrategy;
import uk.gov.companieshouse.account.validator.service.FelixAccountValidator;
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

    /**
     * Creates the logger used by the application.
     *
     * @return the logger
     */
    @Bean
    public Logger logger() {
        return LoggerFactory.getLogger(applicationNameSpace);
    }

    /**
     * Creates the rest template used for rest api calls
     *
     * @return the rest template
     */
    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    /**
     * Creates the executor used for running asynchronous tasks
     *
     * @return the executor
     */
    @Bean
    public Executor getExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    /**
     * Creates the retry strategy for file transfer polling
     *
     * @param baseDelay      the initial delay in seconds
     * @param delayIncrement the amount o increase the delay each time
     * @param timeout        the maximum time allowed spending retrying
     * @param maxDelay       the maximum delay increment
     * @return the retry strategy
     */
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

    /**
     * Creates the account validator bean. This can be used to specify the specific strategy required
     *
     * @return The account validator strategy to use
     */
    @Bean
    public AccountValidationStrategy accountValidationStrategy() {
        return new FelixAccountValidator();
    }
}

