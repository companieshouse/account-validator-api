package uk.gov.companieshouse.account.validator.service.file.transfer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.account.validator.exceptionhandler.ResponseException;
import uk.gov.companieshouse.account.validator.exceptionhandler.UriValidationException;
import uk.gov.companieshouse.account.validator.model.File;
import uk.gov.companieshouse.account.validator.service.retry.RetryException;
import uk.gov.companieshouse.account.validator.service.retry.RetryStrategy;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.filetransfer.AvStatus;
import uk.gov.companieshouse.api.filetransfer.FileApi;
import uk.gov.companieshouse.api.filetransfer.FileDetailsApi;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.filetransfer.InternalFileTransferClient;
import uk.gov.companieshouse.api.handler.filetransfer.request.PrivateFileTransferDelete;
import uk.gov.companieshouse.api.handler.filetransfer.request.PrivateFileTransferDownload;
import uk.gov.companieshouse.api.handler.filetransfer.request.PrivateFileTransferGetDetails;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.handler.filetransfer.FileTransferHttpClient;
import uk.gov.companieshouse.logging.Logger;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * An implementation of the FileTransferStrategy utilising the FileTransferService to transfer files
 */
@Component
public class FileTransferService implements FileTransferStrategy {

    private final Logger logger;
    private final RetryStrategy retryStrategy;

    private final Supplier<InternalFileTransferClient> apiClientSupplier;

    @Autowired
    public FileTransferService(
            Logger logger,
            @Qualifier("fileTransferRetryStrategy") RetryStrategy retryStrategy,
            Supplier<InternalFileTransferClient> apiClientSupplier) {

        this.logger = logger;
        this.retryStrategy = retryStrategy;
        this.apiClientSupplier = apiClientSupplier;
    }

    /**
     * Downloads a file from S3 using the file transfer api
     *
     * @param id the id of the file to get
     * @return The File if it exists, Empty otherwise
     */
    @Override
    public Optional<File> get(String id) {


        Optional<FileDetailsApi> details = retryStrategy.attempt(() -> {
            Optional<FileDetailsApi> maybeFileDetails;
            maybeFileDetails = getFileDetails(id);
            var stillAwaitingScan = maybeFileDetails
                    .map(fileDetailsApi -> fileDetailsApi.getAvStatus().equals(AvStatus.NOT_SCANNED))
                    .orElse(false);

            logger.debugContext(id, "File still awaiting scan. Retrying.", null);

            if (stillAwaitingScan) {
                // AvScan has still not been completed. Attempt to retry
                throw new RetryException();
            }

            return maybeFileDetails;
        });

        // No file with id
        if (details.isEmpty()) {
            return Optional.empty();
        }

        ApiResponse<FileApi> response = getFileApiResponse(id);

        var file = new File(id, details.get().getName(), response.getData().getBody());
        return Optional.of(file);
    }

    @Override
    public Optional<FileDetailsApi> getDetails(String id) {
        return getFileDetails(id);
    }

    private Optional<FileDetailsApi> getFileDetails(final String id) {
        ApiResponse<FileDetailsApi> response = getFileDetailsApiResponse(id);

        HttpStatus status = HttpStatus.resolve(response.getStatusCode());
        switch (Objects.requireNonNull(status)) {
            case NOT_FOUND:
                return Optional.empty();
            case OK:
                return Optional.ofNullable(response.getData());
            default:
                var message = "Unexpected response status from file transfer api when getting file details.";
                logger.errorContext(id, message, null, Map.of(
                        "expected", "200 or 404",
                        "status", response.getStatusCode()
                ));
                throw new RuntimeException(message);
        }
    }

    /**
     * Deletes the file from S3 using the file transfer api
     *
     * @param id the id of the file to delete
     */
    @Override
    public void delete(String id) {
        PrivateFileTransferDelete delete = getInternalApiClient()
                .privateFileTransferHandler()
                .delete(id);

        try {
            delete.execute();
        } catch (ApiErrorResponseException | URIValidationException e) {
            throw handleCheckedExceptions(e);
        }
    }

    private ApiResponse<FileApi> getFileApiResponse(String id) {
        PrivateFileTransferDownload download = getInternalApiClient()
                .privateFileTransferHandler()
                .download(id);

        try {
            return download.execute();
        } catch (ApiErrorResponseException | URIValidationException e) {
            throw handleCheckedExceptions(e);
        }
    }

    private ApiResponse<FileDetailsApi> getFileDetailsApiResponse(String id) {
        PrivateFileTransferGetDetails details = getInternalApiClient()
                .privateFileTransferHandler()
                .details(id);

        try {
            return details.execute();
        } catch (ApiErrorResponseException | URIValidationException e) {
            throw handleCheckedExceptions(e);
        }
    }

    private InternalFileTransferClient getInternalApiClient() {
        return apiClientSupplier.get();
    }

    /**
     * Method to wrap checked exceptions (currently only 2) within a RuntimeException. This is done to -
     * 1. Prevent modification of upstream method signatures
     * 2. Be Lambda friendly (Lambda's can't throw checked exceptions)
     *
     * @param e checked exception to process
     * @return wrapped checked exception
     */
    private RuntimeException handleCheckedExceptions(Exception e) {
        return (e instanceof ApiErrorResponseException ? new ResponseException(e) : new UriValidationException(e));
    }
}
