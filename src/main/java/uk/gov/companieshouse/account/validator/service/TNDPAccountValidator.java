package uk.gov.companieshouse.account.validator.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.account.validator.model.File;
import uk.gov.companieshouse.account.validator.model.validation.ValidationResult;

/**
 * A method of validating accounts via the FelixValidator
 */
@Component
public class TNDPAccountValidator implements AccountValidationStrategy {
    /**
     * Validate submits the file to the felix validator for validation
     *
     * @param file An IXBRL file or a ZIP file containing the accounts
     * @return the result of the validation
     */
    @Override
    public ValidationResult validate(File file) {
        throw new RuntimeException("FelixAccountValidator.validate is not yet implemented");
    }
}
