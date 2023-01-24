package uk.gov.companieshouse.account.validator.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.account.validator.security.LoggingInterceptor;

@Component
public class WebMvcConfig implements WebMvcConfigurer {
    private final LoggingInterceptor loggingInterceptor;

    @Autowired
    public WebMvcConfig(LoggingInterceptor loggingInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor);
    }
}
