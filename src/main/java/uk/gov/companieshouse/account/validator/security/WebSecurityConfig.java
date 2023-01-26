package uk.gov.companieshouse.account.validator.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import uk.gov.companieshouse.logging.Logger;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserAuthenticationInterceptor userAuthenticationInterceptor;
    private final Logger logger;

    @Autowired
    public WebSecurityConfig(UserAuthenticationInterceptor userAuthenticationInterceptor, Logger logger) {
        this.userAuthenticationInterceptor = userAuthenticationInterceptor;
        this.logger = logger;
    }

    /**
     * Configure Http Security.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic().disable()
                .csrf().disable()
                .formLogin().disable()
                .logout().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .anyRequest().permitAll();
    }

    /**
     * Configure Web Security.
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        // Excluding healthcheck endpoint from security filter
        web.ignoring().antMatchers("/actuator/**");
    }
}
