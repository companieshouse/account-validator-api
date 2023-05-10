package uk.gov.companieshouse.account.validator.service;

import uk.gov.companieshouse.account.validator.exceptionhandler.XBRLValidationException;
import uk.gov.companieshouse.account.validator.model.File;
import uk.gov.companieshouse.account.validator.model.felix.ixbrl.Results;

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
    Results validate(File file) throws XBRLValidationException;
}
