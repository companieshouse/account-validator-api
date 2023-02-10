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
public final class DummyValidator implements AccountValidationStrategy {
    @Override
    public boolean equals(Object obj) {
        return obj == this || obj != null && obj.getClass() == this.getClass();
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public String toString() {
        return "DummyValidator[]";
    }

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
        if (file.getName().equalsIgnoreCase("example.xbrl")) {
            return DummyValidator.dummyResult(ValidationStatus.FAILED);
        }

        return DummyValidator.dummyResult(ValidationStatus.OK);
    }
}
