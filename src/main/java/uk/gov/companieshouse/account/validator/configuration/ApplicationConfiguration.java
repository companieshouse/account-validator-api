package uk.gov.companieshouse.account.validator.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import uk.gov.companieshouse.account.validator.repository.RequestStatusRepository;
import uk.gov.companieshouse.account.validator.service.AccountValidationStrategy;
import uk.gov.companieshouse.account.validator.service.FelixAccountValidator;
import uk.gov.companieshouse.account.validator.service.factory.request.status.RequestStatusFactory;
import uk.gov.companieshouse.account.validator.service.retry.IncrementalBackoff;
import uk.gov.companieshouse.account.validator.service.retry.RetryStrategy;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.handler.felixvalidator.PrivateFelixValidatorResourceHandler;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class ApplicationConfiguration {
    @Value("${application.namespace}")
    private String applicationNameSpace;

    @Value("${felix.validator.url}")
    private String felixValidatorUrl;

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
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Creates the executor used for running asynchronous tasks
     *
     * @return the executor
     */
    @Bean
    public Executor executor() {
        return Executors.newWorkStealingPool();
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
     * Creates the account validator bean. This can be used to specify the specific
     * strategy required
     *
     * @return The account validator strategy to use
     */
    @Bean
    public AccountValidationStrategy accountValidationStrategy(Logger logger,
            RequestStatusRepository statusRepository,
            RestTemplate restTemplate,
            PrivateFelixValidatorResourceHandler felixClient,
            RequestStatusFactory statusFactory) {
        return new FelixAccountValidator(logger, statusRepository, restTemplate, felixClient, statusFactory);
    }

    /**
     * Creates the environment reader bean.
     *
     * @return The environment reader
     */
    @Bean
    public EnvironmentReader environmentReader() {
        return new EnvironmentReaderImpl();
    }

    @Bean
    public InternalApiClient internalApiClient(
            @Value("${api.base.path}") String apiBasePath,
            @Value("${internal.api.base.path}") String internalApiBasePath,
            @Value("${payments.api.base.path}") String paymentsApiBasePath,
            @Value("${internal.api.key}") String internalApiKey) {
        ApiKeyHttpClient httpClient = new ApiKeyHttpClient(internalApiKey);
        InternalApiClient internalApiClient = new InternalApiClient(httpClient);

        internalApiClient.setBasePath(internalApiBasePath);
        internalApiClient.setBasePaymentsPath(paymentsApiBasePath);
        internalApiClient.setInternalBasePath(internalApiBasePath);

        return internalApiClient;
    }

    @Bean
    PrivateFelixValidatorResourceHandler felixClient(InternalApiClient internalApiClient) {
        return internalApiClient.privateFelixValidatorResourceHandler(felixValidatorUrl);
    }
}
