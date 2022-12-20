package uk.gov.companieshouse.account.validator;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.gov.companieshouse.account.validator.service.FilesStorageService;

import javax.annotation.Resource;

@SpringBootApplication
public class AccountValidatorApplication implements CommandLineRunner {

    @Resource
    FilesStorageService storageService;
    public static final String NAMESPACE = "account-validator-api";

    public static void main(String[] args) {
        SpringApplication.run(AccountValidatorApplication.class, args);
    }

    @Override
    public void run(String... arg) throws Exception {
//    storageService.deleteAll();
        storageService.init();
    }
}
