package uk.gov.companieshouse.account.validator.service.file.transfer;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.account.validator.model.File;
import uk.gov.companieshouse.account.validator.service.retry.RetryException;
import uk.gov.companieshouse.account.validator.service.retry.RetryStrategy;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.handler.filetransfer.PrivateFileTransferResourceHandler;
import uk.gov.companieshouse.api.handler.filetransfer.request.PrivateModelFileTransferDelete;
import uk.gov.companieshouse.api.handler.filetransfer.request.PrivateModelFileTransferDownload;
import uk.gov.companieshouse.api.handler.filetransfer.request.PrivateModelFileTransferGetDetails;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.filetransfer.AvStatusApi;
import uk.gov.companieshouse.api.model.filetransfer.FileApi;
import uk.gov.companieshouse.api.model.filetransfer.FileDetailsApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

import java.util.Map;
import java.util.Optional;

/**
 * An implementation of the FileTransferStrategy utilising the FileTransferService to transfer files
 */
@Component
public class FileTransferService implements FileTransferStrategy {

    public static final String API_KEY_HEADER = "x-api-key";
    private final Logger logger;
    private final RetryStrategy retryStrategy;

    @Autowired
    public FileTransferService(
            Logger logger,
            @Qualifier("fileTransferRetryStrategy") RetryStrategy retryStrategy) {

        this.logger = logger;
        this.retryStrategy = retryStrategy;
    }

    /**
     * Normalise URL converts a url into the form expected by the code.
     * Path concatenation will assume the url does not have a trailing slash.
     * By normalising the url, that assumption is always true.
     *
     * @param url A url as a string
     * @return A url in a normalised form
     */
    private static String normaliseUrl(String url) {
        return StringUtils.stripEnd(url, "/");
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
            var maybeFileDetails = getFileDetails(id);
            var stillAwaitingScan = maybeFileDetails
                    .map(fileDetailsApi -> fileDetailsApi.getAvStatusApi().equals(AvStatusApi.NOT_SCANNED))
                    .orElse(false);

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

    private Optional<FileDetailsApi> getFileDetails(final String id) {
        ApiResponse<FileDetailsApi> response = getFileDetailsApiResponse(id);

        HttpStatus status = HttpStatus.resolve(response.getStatusCode());
        switch (status) {
            case NOT_FOUND:
                return Optional.empty();
            case OK:
                return Optional.ofNullable(response.getData());
            default:
                var message = "Unexpected response status from file transfer api when getting file details.";
                logger.error(message, Map.of(
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
        InternalApiClient client = ApiSdkManager.getInternalSDK();
        PrivateFileTransferResourceHandler resourceHandler = client.privateFileTransferResourceHandler();
        PrivateModelFileTransferDelete delete = resourceHandler.delete(id);
//todo add exceptions to signature and capture in controller
        //todo fix version and unit tests in 3 sdks, raise PRs and merge
        //todo unit testa for FTS & AVA
        //todo tidy up, code analyis etc... don't merge 2 PRs until Harry has approved
        try {
            delete.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ApiResponse<FileApi> getFileApiResponse(String id) {
        InternalApiClient client = ApiSdkManager.getInternalSDK();
        PrivateFileTransferResourceHandler resourceHandler = client.privateFileTransferResourceHandler();
        PrivateModelFileTransferDownload download = resourceHandler.download(id);

        try {
            return download.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ApiResponse<FileDetailsApi> getFileDetailsApiResponse(String id) {
        InternalApiClient client = ApiSdkManager.getInternalSDK();
        PrivateFileTransferResourceHandler resourceHandler = client.privateFileTransferResourceHandler();
        PrivateModelFileTransferGetDetails details = resourceHandler.details(id);

        try {
            return details.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
