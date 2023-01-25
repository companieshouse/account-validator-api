package uk.gov.companieshouse.account.validator.service.file.transfer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.RegexValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.account.validator.model.File;
import uk.gov.companieshouse.account.validator.service.retry.RetryException;
import uk.gov.companieshouse.account.validator.service.retry.RetryStrategy;
import uk.gov.companieshouse.logging.Logger;

import java.util.Map;
import java.util.Optional;

/**
 * An implementation of the FileTransferStrategy utilising the FileTransferService to transfer files
 */
@Component
public class FileTransferService implements FileTransferStrategy {

    private final String fileTransferApiUrl;
    private final String fileTransferApiKey;
    private final RestTemplate restTemplate;
    private final Logger logger;
    private final RetryStrategy retryStrategy;
    private final UrlValidator urlValidator = new UrlValidator(
            new RegexValidator(".*"), // Bypasses authority validation check. needed for .local tld
            0L);

    @Autowired
    public FileTransferService(
            @Value("${file.transfer.api.url}") String fileTransferApiUrl,
            @Value("${file.transfer.api.key}") String fileTransferApiKey,
            RestTemplate restTemplate, Logger logger,
            @Qualifier("fileTransferRetryStrategy") RetryStrategy retryStrategy) {

        this.fileTransferApiUrl = FileTransferService.normaliseUrl(fileTransferApiUrl);
        this.fileTransferApiKey = fileTransferApiKey;
        this.restTemplate = restTemplate;
        this.logger = logger;
        this.retryStrategy = retryStrategy;

        validateParams();
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
     * Ensures the API key and URL are present.
     * It also checks that the URL is in a valid URL format.
     * If not it throws an exception with a helpful message.
     */
    private void validateParams() {
        if (StringUtils.isBlank(fileTransferApiKey)) {
            throw new IllegalArgumentException("File transfer API key must not be blank.");
        }

        if (StringUtils.isBlank(fileTransferApiUrl)) {
            throw new IllegalArgumentException("File transfer API key must not be blank.");
        } else if (!urlValidator.isValid(fileTransferApiUrl)) {
            throw new IllegalArgumentException(String.format(
                    "File transfer API url must be a valid URL. \"%s\" is not a valid URL.",
                    fileTransferApiUrl));
        }
    }

    /**
     * Downloads a file from S3 using the file transfer api
     *
     * @param id the id of the file to get
     * @return The File if it exists, Empty otherwise
     */
    @Override
    public Optional<File> get(String id) {
        Optional<FileDetails> details = retryStrategy.attempt(() -> {
            var maybeFileDetails = getFileDetails(id);
            var stillAwaitingScan = maybeFileDetails
                    .map(fileDetails -> fileDetails.avStatus().equals(AvStatus.NOT_SCANNED))
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

        var downloadUrl = fileTransferApiUrl + details.get().links().download();
        ResponseEntity<byte[]> fileBytesResponse = get(downloadUrl, byte[].class);
        var file = new File(id, details.get().name(), fileBytesResponse.getBody());
        return Optional.of(file);
    }

    private <T> ResponseEntity<T> doRequest(String urlTemplate, HttpMethod method, Class<T> clazz, Object... urlParams) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", fileTransferApiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
                urlTemplate, method, entity, clazz, urlParams);
    }

    private <T> ResponseEntity<T> get(String urlTemplate, Class<T> clazz, Object... urlParams) {
        return doRequest(urlTemplate, HttpMethod.GET, clazz, urlParams);
    }

    private void delete(String urlTemplate, Object... urlParams) {
        doRequest(urlTemplate, HttpMethod.DELETE, Void.class, urlParams);
    }

    private Optional<FileDetails> getFileDetails(final String id) {
        // For getting file details the file id is the relative path
        var getFileDetailsUrlTemplate = fileTransferApiUrl + "/files/{id}";
        ResponseEntity<FileDetails> fileDetailsResponse = get(
                getFileDetailsUrlTemplate, FileDetails.class, id);


        return switch (fileDetailsResponse.getStatusCode()) {
            case NOT_FOUND -> Optional.empty();
            case OK -> Optional.ofNullable(fileDetailsResponse.getBody());
            default -> {
                var message = "Unexpected response status from file transfer api when getting file details.";
                logger.error(message, Map.of(
                        "expected", "200 or 404",
                        "status", fileDetailsResponse.getStatusCode(),
                        "url", getFileDetailsUrlTemplate
                ));
                throw new RuntimeException(message);
            }
        };
    }


    /**
     * Deletes the file from S3 using the file transfer api
     *
     * @param id the id of the file to delete
     */
    @Override
    public void delete(String id) {
        var fileDeleteUrlTemplate = fileTransferApiUrl + "/{id}";
        delete(fileDeleteUrlTemplate, id);
    }
}
