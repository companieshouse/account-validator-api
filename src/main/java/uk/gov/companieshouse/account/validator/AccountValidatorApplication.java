package uk.gov.companieshouse.account.validator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AccountValidatorApplication {

    public static final String APPLICATION_NAME_SPACE = "account-validator-api";

    public static void main(String[] args) {
        SpringApplication.run(AccountValidatorApplication.class, args);
    }
}
