package uk.gov.companieshouse.account.validator.controller;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.account.validator.message.ResponseMessage;
import uk.gov.companieshouse.account.validator.model.AccountValidated;
import uk.gov.companieshouse.account.validator.model.FileDetails;

import uk.gov.companieshouse.account.validator.model.ValidationResponse;
import uk.gov.companieshouse.account.validator.service.AccountValidatedService;

import javax.validation.Valid;
import java.util.UUID;


@Controller
public class FilesController {

    @Autowired
    AccountValidatedService accountValidatedService;

    @PostMapping("/validate")
    public ResponseEntity<ResponseMessage> validate(@Valid @RequestBody FileDetails fileDetails) {
        String message = "";
        try {

            // TODO
            // IF ZIP FILE calls tnedp valildation No ixbrl_image_renderer
            // IF NO ZIP FILE calls ixbrl/felix valildation call to ixbrl_image_renderer

            message = "Uploaded the file successfully";
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Could not upload the file: " + fileDetails.getFile_name()+ ". Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
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

            message = "Uploaded the file successfully";
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Could not upload the file: " + fileDetails.getFile_name()+ ". Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

}
