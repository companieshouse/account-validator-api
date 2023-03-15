package uk.gov.companieshouse.account.validator.service.file.transfer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.account.validator.exceptionhandler.ResponseException;
import uk.gov.companieshouse.account.validator.exceptionhandler.ValidationException;
import uk.gov.companieshouse.account.validator.model.File;
import uk.gov.companieshouse.account.validator.service.retry.RetryException;
import uk.gov.companieshouse.account.validator.service.retry.RetryStrategy;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
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

import java.util.Optional;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileTransferServiceTest {

    public static final String TEST_FILE_ID = "fileID";
    public static final String TEST_FILE_NAME_ZIP = "file_name.zip";

    @Mock
    private Logger logger;

    @Mock
    private RetryStrategy retryStrategy;

    @Mock
    private InternalApiClient mockClient;

    @Mock
    private PrivateFileTransferResourceHandler mockHandler;

    @InjectMocks
    private FileTransferService fileTransferService;

    @Test
    @DisplayName("Get a file using the file transfer API")
    void getHappyPath() throws ApiErrorResponseException, URIValidationException {
        // given
        var data = "Hello World!".getBytes();
        FileDetailsApi fileDetailsApi = new FileDetailsApi(TEST_FILE_ID, "avTimestamp", AvStatusApi.CLEAN, "contentType", 100, TEST_FILE_NAME_ZIP, "createdOn", null);
        FileApi fileApi = new FileApi(TEST_FILE_NAME_ZIP, data, "mimeType", 100, "extension");
        ApiResponse<FileDetailsApi> detailsResponse = new ApiResponse<>(200, null, fileDetailsApi);
        ApiResponse<FileApi> downloadResponse = new ApiResponse<>(200, null, fileApi);

        setupRetryStrategy();

        try (MockedStatic<ApiSdkManager> mockManager = mockStatic(ApiSdkManager.class)) {
            // Create mocks
            PrivateModelFileTransferGetDetails mockDetails = mock(PrivateModelFileTransferGetDetails.class);
            PrivateModelFileTransferDownload mockDownload = mock(PrivateModelFileTransferDownload.class);

            // Mock scope
            mockManager.when(ApiSdkManager::getInternalSDK).thenReturn(mockClient);
            when(mockClient.privateFileTransferResourceHandler()).thenReturn(mockHandler).thenReturn(mockHandler);
            when(mockHandler.details(anyString())).thenReturn(mockDetails);
            when(mockDetails.execute()).thenReturn(detailsResponse);
            when(mockHandler.download(anyString())).thenReturn(mockDownload);
            when(mockDownload.execute()).thenReturn(downloadResponse);

            // when
            Optional<File> maybeFile = fileTransferService.get(TEST_FILE_ID);

            // then
            assertTrue(maybeFile.isPresent());
            assertThat(maybeFile.get().getName(), is(equalTo(TEST_FILE_NAME_ZIP)));
            assertThat(maybeFile.get().getId(), is(equalTo(TEST_FILE_ID)));
            assertThat(maybeFile.get().getData(), is(equalTo("Hello World!".getBytes())));
        }
    }

    @Test
    @DisplayName("Attempt to get a file that isn't available")
    void getFileNotFound() throws ApiErrorResponseException, URIValidationException {
        // given
        ApiResponse<FileDetailsApi> detailsResponse = new ApiResponse<>(404, null, null);

        setupRetryStrategy();

        try (MockedStatic<ApiSdkManager> mockManager = mockStatic(ApiSdkManager.class)) {
            // Create mocks
            PrivateModelFileTransferGetDetails mockDetails = mock(PrivateModelFileTransferGetDetails.class);

            // Mock scope
            mockManager.when(ApiSdkManager::getInternalSDK).thenReturn(mockClient);
            when(mockClient.privateFileTransferResourceHandler()).thenReturn(mockHandler).thenReturn(mockHandler);
            when(mockHandler.details(anyString())).thenReturn(mockDetails);
            when(mockDetails.execute()).thenReturn(detailsResponse);

            // when
            Optional<File> maybeFile = fileTransferService.get(TEST_FILE_ID);

            // then
            assertTrue(maybeFile.isEmpty());
        }
    }

    @Test
    @DisplayName("Attempt to get a file that isn't scanned")
    void ThrowRetryExceptionWhenFileNotScanned() throws ApiErrorResponseException, URIValidationException {
        // given
        FileDetailsApi fileDetailsApi = new FileDetailsApi(TEST_FILE_ID, "avTimestamp", AvStatusApi.NOT_SCANNED, "contentType", 100, TEST_FILE_NAME_ZIP, "createdOn", null);
        ApiResponse<FileDetailsApi> detailsResponse = new ApiResponse<>(200, null, fileDetailsApi);

        setupRetryStrategy();

        try (MockedStatic<ApiSdkManager> mockManager = mockStatic(ApiSdkManager.class)) {
            // Create mocks
            PrivateModelFileTransferGetDetails mockDetails = mock(PrivateModelFileTransferGetDetails.class);

            // Mock scope
            mockManager.when(ApiSdkManager::getInternalSDK).thenReturn(mockClient);
            when(mockClient.privateFileTransferResourceHandler()).thenReturn(mockHandler).thenReturn(mockHandler);
            when(mockHandler.details(anyString())).thenReturn(mockDetails);
            when(mockDetails.execute()).thenReturn(detailsResponse);

            // when

            // then
            assertThrows(RetryException.class, () -> fileTransferService.get(TEST_FILE_ID));
        }
    }

    @Test
    @DisplayName("Convert ApiErrorResponseException to ResponseException in get file details during get file")
    void ThrowResponseExceptionOnApiErrorResponseExceptionInGetFileDetails() throws ApiErrorResponseException, URIValidationException {
        // given

        setupRetryStrategy();

        try (MockedStatic<ApiSdkManager> mockManager = mockStatic(ApiSdkManager.class)) {
            // Create mocks
            PrivateModelFileTransferGetDetails mockDetails = mock(PrivateModelFileTransferGetDetails.class);

            // Mock scope
            mockManager.when(ApiSdkManager::getInternalSDK).thenReturn(mockClient);
            when(mockClient.privateFileTransferResourceHandler()).thenReturn(mockHandler).thenReturn(mockHandler);
            when(mockHandler.details(anyString())).thenReturn(mockDetails);
            when(mockDetails.execute()).thenThrow(mock(ApiErrorResponseException.class));

            // when

            // then
            assertThrows(ResponseException.class, () -> fileTransferService.get(TEST_FILE_ID));
        }
    }

    @Test
    @DisplayName("Convert URIValidationException to ValidationException in get file details during get file")
    void ThrowValidationExceptionOnURIValidationExceptionInGetFileDetails() throws ApiErrorResponseException, URIValidationException {
        // given

        setupRetryStrategy();

        try (MockedStatic<ApiSdkManager> mockManager = mockStatic(ApiSdkManager.class)) {
            // Create mocks
            PrivateModelFileTransferGetDetails mockDetails = mock(PrivateModelFileTransferGetDetails.class);

            // Mock scope
            mockManager.when(ApiSdkManager::getInternalSDK).thenReturn(mockClient);
            when(mockClient.privateFileTransferResourceHandler()).thenReturn(mockHandler).thenReturn(mockHandler);
            when(mockHandler.details(anyString())).thenReturn(mockDetails);
            when(mockDetails.execute()).thenThrow(mock(URIValidationException.class));

            // when

            // then
            assertThrows(ValidationException.class, () -> fileTransferService.get(TEST_FILE_ID));
        }
    }

    @Test
    @DisplayName("Unexpected response status when get file")
    void testUnexpectedResponseStatus() throws ApiErrorResponseException, URIValidationException {
        // given
        ApiResponse<FileDetailsApi> detailsResponse = new ApiResponse<>(500, null, null);

        setupRetryStrategy();

        try (MockedStatic<ApiSdkManager> mockManager = mockStatic(ApiSdkManager.class)) {
            // Create mocks
            PrivateModelFileTransferGetDetails mockDetails = mock(PrivateModelFileTransferGetDetails.class);

            // Mock scope
            mockManager.when(ApiSdkManager::getInternalSDK).thenReturn(mockClient);
            when(mockClient.privateFileTransferResourceHandler()).thenReturn(mockHandler).thenReturn(mockHandler);
            when(mockHandler.details(anyString())).thenReturn(mockDetails);
            when(mockDetails.execute()).thenReturn(detailsResponse);

            // when

            // then
            RuntimeException actual = assertThrows(RuntimeException.class, () -> fileTransferService.get(TEST_FILE_ID));

            assertEquals("Unexpected response status from file transfer api when getting file details.", actual.getMessage());
        }
    }

    @Test
    @DisplayName("Convert ApiErrorResponseException to ResponseException during get file")
    void ThrowResponseExceptionOnApiErrorResponseExceptionInGetFile() throws ApiErrorResponseException, URIValidationException {
        // given
        FileDetailsApi fileDetailsApi = new FileDetailsApi(TEST_FILE_ID, "avTimestamp", AvStatusApi.CLEAN, "contentType", 100, TEST_FILE_NAME_ZIP, "createdOn", null);
        ApiResponse<FileDetailsApi> detailsResponse = new ApiResponse<>(200, null, fileDetailsApi);

        setupRetryStrategy();

        try (MockedStatic<ApiSdkManager> mockManager = mockStatic(ApiSdkManager.class)) {
            // Create mocks
            PrivateModelFileTransferGetDetails mockDetails = mock(PrivateModelFileTransferGetDetails.class);
            PrivateModelFileTransferDownload mockDownload = mock(PrivateModelFileTransferDownload.class);

            // Mock scope
            mockManager.when(ApiSdkManager::getInternalSDK).thenReturn(mockClient);
            when(mockClient.privateFileTransferResourceHandler()).thenReturn(mockHandler).thenReturn(mockHandler);
            when(mockHandler.details(anyString())).thenReturn(mockDetails);
            when(mockDetails.execute()).thenReturn(detailsResponse);
            when(mockHandler.download(anyString())).thenReturn(mockDownload);
            when(mockDownload.execute()).thenThrow(mock(ApiErrorResponseException.class));

            // when

            // then
            assertThrows(ResponseException.class, () -> fileTransferService.get(TEST_FILE_ID));
        }
    }

    @Test
    @DisplayName("Convert URIValidationException to ValidationException during get file")
    void ThrowValidationExceptionOnURIValidationExceptionInGetFile() throws ApiErrorResponseException, URIValidationException {
        // given
        FileDetailsApi fileDetailsApi = new FileDetailsApi(TEST_FILE_ID, "avTimestamp", AvStatusApi.CLEAN, "contentType", 100, TEST_FILE_NAME_ZIP, "createdOn", null);
        ApiResponse<FileDetailsApi> detailsResponse = new ApiResponse<>(200, null, fileDetailsApi);

        setupRetryStrategy();

        try (MockedStatic<ApiSdkManager> mockManager = mockStatic(ApiSdkManager.class)) {
            // Create mocks
            PrivateModelFileTransferGetDetails mockDetails = mock(PrivateModelFileTransferGetDetails.class);
            PrivateModelFileTransferDownload mockDownload = mock(PrivateModelFileTransferDownload.class);

            // Mock scope
            mockManager.when(ApiSdkManager::getInternalSDK).thenReturn(mockClient);
            when(mockClient.privateFileTransferResourceHandler()).thenReturn(mockHandler).thenReturn(mockHandler);
            when(mockHandler.details(anyString())).thenReturn(mockDetails);
            when(mockDetails.execute()).thenReturn(detailsResponse);
            when(mockHandler.download(anyString())).thenReturn(mockDownload);
            when(mockDownload.execute()).thenThrow(mock(URIValidationException.class));

            // when

            // then
            assertThrows(ValidationException.class, () -> fileTransferService.get(TEST_FILE_ID));
        }
    }

    @Test
    @DisplayName("Deletes a file using the file transfer service")
    void deleteFile() {
        // given
        var id = "fileId";

        try (MockedStatic<ApiSdkManager> mockManager = mockStatic(ApiSdkManager.class)) {
            // Create mocks
            PrivateModelFileTransferDelete mockDelete = mock(PrivateModelFileTransferDelete.class);

            // Mock scope
            mockManager.when(ApiSdkManager::getInternalSDK).thenReturn(mockClient);
            when(mockClient.privateFileTransferResourceHandler()).thenReturn(mockHandler);
            when(mockHandler.delete(anyString())).thenReturn(mockDelete);

            // when
            fileTransferService.delete(id);

            // then
            verify(mockHandler).delete(anyString());
        }
    }

    @Test
    @DisplayName("Convert ApiErrorResponseException to ResponseException during delete file")
    void ThrowResponseExceptionOnApiErrorResponseExceptionInDeleteFile() throws ApiErrorResponseException, URIValidationException {
        // given

        try (MockedStatic<ApiSdkManager> mockManager = mockStatic(ApiSdkManager.class)) {
            // Create mocks
            PrivateModelFileTransferDelete mockDelete = mock(PrivateModelFileTransferDelete.class);

            // Mock scope
            mockManager.when(ApiSdkManager::getInternalSDK).thenReturn(mockClient);
            when(mockClient.privateFileTransferResourceHandler()).thenReturn(mockHandler).thenReturn(mockHandler);
            when(mockHandler.delete(anyString())).thenReturn(mockDelete);
            when(mockDelete.execute()).thenThrow(mock(ApiErrorResponseException.class));

            // when

            // then
            assertThrows(ResponseException.class, () -> fileTransferService.delete(TEST_FILE_ID));
        }
    }

    @Test
    @DisplayName("Convert URIValidationException to ValidationException during delete file")
    void ThrowValidationExceptionOnURIValidationExceptionInDeleteFile() throws ApiErrorResponseException, URIValidationException {
        // given

        try (MockedStatic<ApiSdkManager> mockManager = mockStatic(ApiSdkManager.class)) {
            // Create mocks
            PrivateModelFileTransferDelete mockDelete = mock(PrivateModelFileTransferDelete.class);

            // Mock scope
            mockManager.when(ApiSdkManager::getInternalSDK).thenReturn(mockClient);
            when(mockClient.privateFileTransferResourceHandler()).thenReturn(mockHandler).thenReturn(mockHandler);
            when(mockDelete.execute()).thenThrow(mock(URIValidationException.class));
            when(mockHandler.delete(anyString())).thenReturn(mockDelete);

            // when

            // then
            assertThrows(ValidationException.class, () -> fileTransferService.delete(TEST_FILE_ID));
        }
    }

    private void setupRetryStrategy() {
        setupRetryStrategy(null);
    }

    private void setupRetryStrategy(Runnable onRetry) {
        when(retryStrategy.attempt(any())).thenAnswer(a -> {
            for (int i = 0; i < 10; i++) {
                try {
                    return a.getArgument(0, Supplier.class).get();
                } catch (RetryException e) {
                    if (onRetry == null) {
                        throw e;
                    }

                    onRetry.run();
                }
            }

            return null;
        });
    }
}