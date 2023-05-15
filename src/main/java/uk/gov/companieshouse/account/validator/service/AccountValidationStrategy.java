package uk.gov.companieshouse.account.validator.service;

import uk.gov.companieshouse.account.validator.exceptionhandler.XBRLValidationException;
import uk.gov.companieshouse.account.validator.model.felix.ixbrl.Results;
import uk.gov.companieshouse.account.validator.model.validation.RequestStatus;
import uk.gov.companieshouse.api.model.filetransfer.FileDetailsApi;

import java.util.Optional;

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
    void startValidation(FileDetailsApi file) throws XBRLValidationException;

    void saveResults(String fileId, Results results);

    Optional<RequestStatus> getStatus(String fileId);
}
