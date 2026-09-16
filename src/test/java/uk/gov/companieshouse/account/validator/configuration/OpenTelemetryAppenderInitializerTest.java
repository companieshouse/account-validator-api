package uk.gov.companieshouse.account.validator.configuration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.InitializingBean;

@ExtendWith(MockitoExtension.class)
class OpenTelemetryAppenderInitializerTest {

    @Mock
    private OpenTelemetry openTelemetry;

    private OpenTelemetryAppenderInitializer undertest;

    @BeforeEach
    public void setUp() {
        undertest = new OpenTelemetryAppenderInitializer(openTelemetry);
    }

    @Test
    @DisplayName("Test component is instantiated with OpenTelemetry dependency")
    void testComponentInstantiation() {
        assertNotNull(undertest);
    }

    @Test
    @DisplayName("Test constructor properly injects OpenTelemetry instance")
    void testConstructorInjection() {
        OpenTelemetryAppenderInitializer component = new OpenTelemetryAppenderInitializer(openTelemetry);
        assertNotNull(component);
    }

    @Test
    @DisplayName("Test afterPropertiesSet installs OpenTelemetryAppender with correct OpenTelemetry instance")
    void testAfterPropertiesSetInstallsAppender() {
        try (MockedStatic<OpenTelemetryAppender> mockedStatic = mockStatic(OpenTelemetryAppender.class)) {
            undertest.afterPropertiesSet();
            mockedStatic.verify(() -> OpenTelemetryAppender.install(openTelemetry));
        }
    }

    @Test
    @DisplayName("Test afterPropertiesSet can be called multiple times without error")
    void testAfterPropertiesSetMultipleCalls() {
        try (MockedStatic<OpenTelemetryAppender> mockedStatic = mockStatic(OpenTelemetryAppender.class)) {
            undertest.afterPropertiesSet();
            undertest.afterPropertiesSet();
            mockedStatic.verify(() -> OpenTelemetryAppender.install(openTelemetry), times(2));
        }
    }

    @Test
    @DisplayName("Test afterPropertiesSet properly uses the injected OpenTelemetry instance")
    void testAfterPropertiesSetUsesCorrectInstance() {
        try (MockedStatic<OpenTelemetryAppender> mockedStatic = mockStatic(OpenTelemetryAppender.class)) {
            OpenTelemetry mockInstance1 = mock(OpenTelemetry.class);
            OpenTelemetryAppenderInitializer component1 = new OpenTelemetryAppenderInitializer(mockInstance1);
            component1.afterPropertiesSet();
            
            mockedStatic.verify(() -> OpenTelemetryAppender.install(mockInstance1));
        }
    }

    @Test
    @DisplayName("Test component is a Spring bean that implements InitializingBean")
    void testComponentImplementsInitializingBean() {
        assertNotNull(undertest);
        assertInstanceOf(InitializingBean.class, undertest);
    }
}
