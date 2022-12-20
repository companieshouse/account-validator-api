package uk.gov.companieshouse.account.validator;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.companieshouse.logging.Logger;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthenticationFilter extends OncePerRequestFilter {

    private final Logger logger;

    public AuthenticationFilter(Logger logger) {
        this.logger = logger;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String xxxId = request.getHeader("XXX-Identity");

        if (StringUtils.isBlank(xxxId)) {
            logger.error("Unauthenticated request received without XXX identity");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String xxxIdType = request.getHeader("XXX-Identity-Type");

        if (StringUtils.isBlank(xxxIdType) ||
                !(xxxIdType.equalsIgnoreCase("key") || xxxIdType.equalsIgnoreCase("oauth2"))) {
            logger.error("Unauthenticated request received without XXX identity type");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
