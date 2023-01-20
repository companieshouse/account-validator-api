package uk.gov.companieshouse.account.validator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.companieshouse.account.validator.message.ResponseMessage;
import uk.gov.companieshouse.account.validator.model.FileDetails;
import uk.gov.companieshouse.account.validator.service.AccountValidatedService;
import uk.gov.companieshouse.account.validator.service.impl.AccountValidatorImpl;
import uk.gov.companieshouse.logging.Logger;

import javax.validation.Valid;

@Controller
public class AccountValidationController {
    private AccountValidatorImpl accountValidatorImpl;
    private AccountValidatedService accountValidatedService;
    private Logger logger;

    @Autowired
    public AccountValidationController(
                AccountValidatorImpl accountValidatorImpl,
                AccountValidatedService accountValidatedService,
                Logger logger) {
        this.accountValidatorImpl = accountValidatorImpl;
        this.accountValidatedService = accountValidatedService;
        this.logger = logger;
    }

    @PostMapping("/validate")
    public ResponseEntity<ResponseMessage> validate(@Valid @RequestBody FileDetails fileDetails) {
        String message = "";
        try {
            boolean result = accountValidatorImpl.downloadIxbrlFromLocation(fileDetails);
            if (result) {
                message = "File validated successfully";
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
            } else {
                message = "File validation failed";
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
            }
        } catch (Exception e) {
            message = "Could not validate the file: " + fileDetails.getFile_name() + ". Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }
}
