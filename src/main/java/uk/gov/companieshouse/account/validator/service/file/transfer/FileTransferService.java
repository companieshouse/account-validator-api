package uk.gov.companieshouse.account.validator.service.file.transfer;

import uk.gov.companieshouse.account.validator.model.File;

import java.util.Optional;

/**
 * An implementation of the FileTransferStrategy utilising the FileTransferService to transfer files
 */
public class FileTransferService implements FileTransferStrategy {
    /**
     * Downloads a file from S3
     *
     * @param id the id of the file to get
     * @return The File if it exists, Empty otherwise
     */
    @Override
    public Optional<File> get(String id) {
        throw new RuntimeException("FileTransferServiceStrategy.download is not yet implemented");
    }

    /**
     * Deletes the file from S3
     *
     * @param id the id of the file to delete
     */
    @Override
    public void delete(String id) {
        throw new RuntimeException("FileTransferServiceStrategy.delete is not yet implemented");
    }
}
