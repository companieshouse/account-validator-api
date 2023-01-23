package uk.gov.companieshouse.account.validator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.companieshouse.account.validator.model.validation.CompleteStatus;
import uk.gov.companieshouse.account.validator.model.validation.FileNotFoundResponse;
import uk.gov.companieshouse.account.validator.model.validation.PendingStatus;
import uk.gov.companieshouse.account.validator.model.validation.SuccessResponse;
import uk.gov.companieshouse.account.validator.model.validation.ValidationRequest;
import uk.gov.companieshouse.account.validator.model.validation.ValidationResponse;
import uk.gov.companieshouse.account.validator.service.AccountValidationStrategy;
import uk.gov.companieshouse.account.validator.service.file.transfer.FileTransferStrategy;
import uk.gov.companieshouse.logging.Logger;

import javax.validation.Valid;
import java.util.concurrent.Executor;

@Controller
public class AccountValidationController {

    private final AccountValidationStrategy accountValidationStrategy;
    private final FileTransferStrategy fileTransferStrategy;
    private final Logger logger;
    private final Executor executor;

    @Autowired
    public AccountValidationController(AccountValidationStrategy accountValidationStrategy,
                                       FileTransferStrategy fileTransferStrategy,
                                       Logger logger,
                                       Executor executor) {
        this.accountValidationStrategy = accountValidationStrategy;
        this.fileTransferStrategy = fileTransferStrategy;
        this.logger = logger;
        this.executor = executor;
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidationResponse> validate(
            @Valid @RequestBody ValidationRequest validationRequest) {

        var fileId = validationRequest.id();
        var file = fileTransferStrategy.get(fileId);
        if (file.isEmpty()) {
            return new ResponseEntity<>(new FileNotFoundResponse(fileId), HttpStatus.NOT_FOUND);
        }

        executor.execute(() -> {
            var result = accountValidationStrategy.validate(file.get());
            var requestStatus = new CompleteStatus(result);
            // TODO: save in repository
        });

        return new ResponseEntity<>(new SuccessResponse(new PendingStatus()), HttpStatus.CREATED);
    }

}
