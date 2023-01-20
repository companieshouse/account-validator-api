package uk.gov.companieshouse.account.validator.service.file.transfer;

import uk.gov.companieshouse.account.validator.model.File;

import java.util.Optional;

/**
 * An interface exposing the file transfer needs of the application.
 * The implementation can be swapped out to change the method of transferring files.
 */
public interface FileTransferStrategy {
    /**
     * Downloads a file from the remote repository
     *
     * @param id the id of the file to get
     * @return Empty, if there is no such file, otherwise the File wrapped in an optional
     */
    Optional<File> get(String id);

    /*
     * Deletes the file with the given id
     */
    void delete(String id);
}
