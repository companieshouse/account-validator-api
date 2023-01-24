package uk.gov.companieshouse.account.validator.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.RequestLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class manages the logging of the start request and end request.
 */
@Component
public class LoggingInterceptor extends HandlerInterceptorAdapter implements RequestLogger {
    private final Logger logger;

    /**
     * Sets the logger used to log out request information.
     *
     * @param logger the configured logger
     */
    @Autowired
    public LoggingInterceptor(Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        logStartRequestProcessing(request, logger);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
        logEndRequestProcessing(request, response, logger);
    }

}