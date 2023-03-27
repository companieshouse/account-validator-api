package uk.gov.companieshouse.account.validator.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {"file.transfer.retry.base.delay.seconds=99"})
class ApplicationConfigurationTest {

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
        assertNotNull(undertest.accountValidationStrategy());
    }

    @Test
    @DisplayName("Test Environment Reader Bean creates correct type")
    void testEnvironmentReaderCreation() {
        assertNotNull(undertest.environmentReader());
    }
}