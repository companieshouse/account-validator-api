package uk.gov.companieshouse.account.validator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.companieshouse.account.validator.message.ResponseMessage;
import uk.gov.companieshouse.account.validator.model.AccountValidated;
import uk.gov.companieshouse.account.validator.model.FileDetails;
import uk.gov.companieshouse.account.validator.model.ValidationResponse;
import uk.gov.companieshouse.account.validator.service.AccountValidatedService;
import uk.gov.companieshouse.account.validator.service.impl.AccountValidatorImpl;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import javax.validation.Valid;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.util.UUID;
import java.util.zip.ZipInputStream;


@Controller
public class FilesController {

    private static final Logger LOGGER = LoggerFactory.getLogger("accounts-validator-api");

    @Autowired
    private AccountValidatorImpl accountValidatorImpl;

    @Autowired
    AccountValidatedService accountValidatedService;

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

    /**
     * Verifies (ZIP) file or Plain HMTL
     *
     * @param source - the file.
     * @return then returns false ou true.
     */
    public boolean isZipFile(StreamSource source) throws IOException {
        source.getInputStream().mark(Integer.MAX_VALUE);
        ZipInputStream zipInputStream = new ZipInputStream(source.getInputStream());
        boolean isZipFile = zipInputStream.getNextEntry() != null;
        source.getInputStream().reset();

        return isZipFile;
    }
}
