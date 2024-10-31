package uk.gov.companieshouse.account.validator.configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import uk.gov.companieshouse.account.validator.repository.RequestStatusRepository;
import uk.gov.companieshouse.account.validator.service.factory.request.status.RequestStatusFactory;
import uk.gov.companieshouse.api.handler.felixvalidator.PrivateFelixValidatorResourceHandler;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {"file.transfer.retry.base.delay.seconds=99"})
class ApplicationConfigurationTest {

    @Mock
    Logger logger;

    @Mock
    RequestStatusRepository statusRepository;

    @Mock
    RestTemplate restTemplate;

    @Mock
    PrivateFelixValidatorResourceHandler felixClient;

    @Mock
    RequestStatusFactory statusFactory;

    private ApplicationConfiguration undertest;

    @BeforeEach
    public void setUp() {
        undertest = new ApplicationConfiguration();
    }

    @Test
    @DisplayName("Test Logging Bean creates correct type")
    void testLoggerCreation() {
        assertNotNull(undertest.logger());
    }

    @Test
    @DisplayName("Test Executor Bean creates correct type")
    void testExecutorCreation() {
        assertNotNull(undertest.executor());
    }

    @Test
    @DisplayName("Test RestTemplate Bean creates correct type")
    void testRestTemplateCreation() {
        assertNotNull(undertest.restTemplate());
    }

    @Test
    @DisplayName("Test file transfer retry strategy Bean creates correct type")
    void testFileTransferRetryStrategyCreation() {
        assertNotNull(undertest.fileTransferRetryStrategy(1L, 2L, 3L, 4L));
    }

    @Test
    @DisplayName("Test AccountValidationStrategy Bean creates correct type")
    void testAccountValidationStrategyCreation() {
        assertNotNull(undertest.accountValidationStrategy(logger, statusRepository, restTemplate, felixClient, statusFactory));
    }

    @Test
    @DisplayName("Test Environment Reader Bean creates correct type")
    void testEnvironmentReaderCreation() {
        assertNotNull(undertest.environmentReader());
    }
}