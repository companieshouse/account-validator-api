package uk.gov.companieshouse.account.validator.service;

import uk.gov.companieshouse.account.validator.model.File;
import uk.gov.companieshouse.account.validator.model.validation.ValidationData;
import uk.gov.companieshouse.account.validator.model.validation.ValidationResult;
import uk.gov.companieshouse.account.validator.model.validation.ValidationStatus;

import java.util.Collections;
import java.util.Set;

/**
 * DummyValidator is a stand-in for the business logic required to validate accounts.
 * It is used to show functionality of the rest of the system until the FelixValidator integration
 * is complete.
 *
 * <p>
 * It fails any file named "example.xbrl" and OK's any other.
 */
public record DummyValidator() implements AccountValidationStrategy {

    private static ValidationResult dummyResult(ValidationStatus status) {
        Set<String> errorMessages = status.equals(ValidationStatus.OK)
                ? Collections.emptySet()
                : Set.of("Error message");

        var validationData = new ValidationData(
                "balanceSheetDate",
                "accountsType",
                "companiesHouseRegisteredNumber"
        );

        return new ValidationResult(errorMessages, validationData, status);
    }


    @Override
    public ValidationResult validate(File file) {
        if (file.name().equalsIgnoreCase("example.xbrl")) {
            return DummyValidator.dummyResult(ValidationStatus.FAILED);
        }

        return DummyValidator.dummyResult(ValidationStatus.OK);
    }
}
