package uk.gov.companieshouse.account.validator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.account.validator.message.ResponseMessage;
import uk.gov.companieshouse.account.validator.model.AccountValidated;
import uk.gov.companieshouse.account.validator.model.FileDetails;
import uk.gov.companieshouse.account.validator.model.ValidationResponse;
import uk.gov.companieshouse.account.validator.service.AccountValidatedService;
import uk.gov.companieshouse.account.validator.service.impl.AccountValidatorImpl;
import uk.gov.companieshouse.account.validator.validation.ixbrl.Results;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import javax.validation.Valid;
import java.util.UUID;

@Controller
public class FilesController {

    private static final Logger LOGGER = LoggerFactory.getLogger("accounts-validator-api");


    private final AccountValidatorImpl accountValidatorImpl;


    private final AccountValidatedService accountValidatedService;

    @Autowired
    public FilesController(AccountValidatorImpl accountValidatorImpl, AccountValidatedService accountValidatedService) {
        this.accountValidatorImpl = accountValidatorImpl;
        this.accountValidatedService = accountValidatedService;
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validate(@Valid @RequestBody FileDetails fileDetails) {
        String message = "";
        try {
            Results result = accountValidatorImpl.downloadIxbrlFromLocation(fileDetails);
            return new ResponseEntity<Results>(result, HttpStatus.OK);
        } catch (Exception e) {
            message = "Could not validate the file: " + fileDetails.getFile_name() + ". Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(message));
        }
    }


    @PostMapping("/mock_validate")
    public ResponseEntity<ResponseMessage> mockValidate(@Valid @RequestBody FileDetails fileDetails) {
        String message = "";
        try {

            String uuid = UUID.randomUUID().toString();
            AccountValidated accountValidated = new AccountValidated();
            accountValidated.setId(uuid);
            accountValidated.setCustomerId("1234567890");
            accountValidated.setFilename("account.zip");
            accountValidated.setS3Key("697e40ba-cc6a-4b40-967c-6b4cdde8af23");
            accountValidated.setStatus("Complete");
            ValidationResponse validationResponse = new ValidationResponse();
            validationResponse.setId(uuid);
            validationResponse.setValid(true);
            validationResponse.setTndpResponse("OK");

            accountValidated.setData(validationResponse);
            accountValidated.setShowImage(false);

            accountValidatedService.createAccount(accountValidated);

            message = "Validation saved successfully";
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Could not save the validation: " + fileDetails.getFile_name() + ". Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    @PostMapping("/direct_file_validate")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        String message = "";
        try {
            String inputDoc = new String(file.getBytes());
            Results result = accountValidatorImpl.validateFileDirect(inputDoc, file.getOriginalFilename());
            return new ResponseEntity<Results>(result, HttpStatus.OK);
        } catch (Exception e) {
            message = "Could not validate the file: " + file.getName() + ". Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        }
    }

}
