package uk.gov.companieshouse.account.validator.service;


/**
 * An interface exposing the required methods to validate company accounts
 */
public interface AccountValidationStrategy {
    /**
     * Validates a file.
     *
     * @param file An IXBRL file or a ZIP file containing the accounts
     * @return The result of the validation
     */
}
