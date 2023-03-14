package uk.gov.companieshouse.account.validator.service.file.transfer;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import uk.gov.companieshouse.account.validator.model.File;
import uk.gov.companieshouse.account.validator.service.retry.RetryException;
import uk.gov.companieshouse.account.validator.service.retry.RetryStrategy;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.filetransfer.PrivateFileTransferResourceHandler;
import uk.gov.companieshouse.api.handler.filetransfer.request.PrivateModelFileTransferDownload;
import uk.gov.companieshouse.api.handler.filetransfer.request.PrivateModelFileTransferGetDetails;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.filetransfer.AvStatusApi;
import uk.gov.companieshouse.api.model.filetransfer.FileApi;
import uk.gov.companieshouse.api.model.filetransfer.FileDetailsApi;
import uk.gov.companieshouse.api.model.filetransfer.FileLinksApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileTransferServiceTest {
    private static final String fileTransferUrl = "http://file.transfer.chs.local";
    private static final String fileTransferApiKey = "file-transfer-api-key";

    @Mock
    Logger logger;

    @Mock
    RetryStrategy retryStrategy;

    HttpEntity<String> httpEntity;

    FileTransferService fileTransferService;

    public static Stream<Arguments> constructorParameters() {
        var validApiKey = Named.of("Valid API Key", fileTransferApiKey);
        var validUrl = Named.of("Valid URL", fileTransferUrl);
        var emptyApiKey = Named.of("Empty API key", "");
        var emptyUrl = Named.of("Empty URL", "");
        var inValidUrl = Named.of("Invalid URL", "hgfjsgdf");
        var isValid = Named.of("Is Valid", false);
        var isNotValid = Named.of("Is not valid", true);

        return Stream.of(
                Arguments.of(validUrl, validApiKey, isValid),
                Arguments.of(inValidUrl, validApiKey, isNotValid),
                Arguments.of(emptyUrl, validApiKey, isNotValid),
                Arguments.of(validUrl, emptyApiKey, isNotValid)
        );
    }

    @BeforeEach
    void setUp() {
        fileTransferService = new FileTransferService(logger, retryStrategy);

        var headers = new HttpHeaders();
        headers.set(FileTransferService.API_KEY_HEADER, fileTransferApiKey);
        httpEntity = new HttpEntity<>(headers);
    }

    //    @ParameterizedTest
//    @MethodSource("constructorParameters")
//    @DisplayName("Test the constructor parameter validation")
//    void parameterValidation(
//            String fileTransferUrl, String fileTransferApiKey, boolean shouldError) {
//
//        Executable createService = () -> new FileTransferService(null, null);
//
//        if (shouldError) {
//            assertThrows(RuntimeException.class, createService);
//        } else {
//            assertDoesNotThrow(createService);
//        }
//    }
//
    @Test
    @DisplayName("Get a file using the file transfer API")
    void getHappyPath() throws ApiErrorResponseException, URIValidationException {
        // given
        var fileId = "fileID";
        var fileName = "file_name.zip";
        var data = "Hello World!".getBytes();

        setupRetryStrategy();

        try (MockedStatic<ApiSdkManager> mockManager = mockStatic(ApiSdkManager.class)) {
            InternalApiClient mockClient = mock(InternalApiClient.class);
            PrivateFileTransferResourceHandler mockHandler = mock(PrivateFileTransferResourceHandler.class);
            PrivateModelFileTransferGetDetails mockDetails = mock(PrivateModelFileTransferGetDetails.class);
            PrivateModelFileTransferDownload mockDownload = mock(PrivateModelFileTransferDownload.class);

            FileDetailsApi fileDetailsApi = new FileDetailsApi(fileId, "avTimestamp", AvStatusApi.CLEAN, "contentType", 100, fileName, "createdOn", null);
            FileApi fileApi = new FileApi(fileName, data, "mimeType", 100, "extension");

            ApiResponse<FileDetailsApi> detailsResponse = new ApiResponse<>(200, null, fileDetailsApi);
            ApiResponse<FileApi> downloadResponse = new ApiResponse<>(200, null, fileApi);

            // Mock scope
            mockManager.when(ApiSdkManager::getInternalSDK).thenReturn(mockClient);
            when(mockClient.privateFileTransferResourceHandler()).thenReturn(mockHandler).thenReturn(mockHandler);

            when(mockHandler.details(anyString())).thenReturn(mockDetails);
            when(mockDetails.execute()).thenReturn(detailsResponse);

            when(mockHandler.download(anyString())).thenReturn(mockDownload);
            when(mockDownload.execute()).thenReturn(downloadResponse);

            //when
            Optional<File> maybeFile = fileTransferService.get(fileId);

            //then
            assertTrue(maybeFile.isPresent());
            assertThat(maybeFile.get().getName(), is(equalTo(fileName)));
            assertThat(maybeFile.get().getId(), is(equalTo(fileId)));
            assertThat(maybeFile.get().getData(), is(equalTo("Hello World!".getBytes())));
        }
    }

    @Test
    @DisplayName("Attempt to get a file that isn't available")
    void getFileNotFound() throws ApiErrorResponseException, URIValidationException {
        // given
        var fileId = "fileID";
        var fileName = "file_name.zip";
        var data = "Hello World!".getBytes();

        setupRetryStrategy();

        try (MockedStatic<ApiSdkManager> mockManager = mockStatic(ApiSdkManager.class)) {
            InternalApiClient mockClient = mock(InternalApiClient.class);
            PrivateFileTransferResourceHandler mockHandler = mock(PrivateFileTransferResourceHandler.class);
            PrivateModelFileTransferGetDetails mockDetails = mock(PrivateModelFileTransferGetDetails.class);

            ApiResponse<FileDetailsApi> detailsResponse = new ApiResponse<>(404, null, null);

            // Mock scope
            mockManager.when(ApiSdkManager::getInternalSDK).thenReturn(mockClient);
            when(mockClient.privateFileTransferResourceHandler()).thenReturn(mockHandler).thenReturn(mockHandler);

            when(mockHandler.details(anyString())).thenReturn(mockDetails);
            when(mockDetails.execute()).thenReturn(detailsResponse);

            //when
            Optional<File> maybeFile = fileTransferService.get(fileId);

            //then
            assertTrue(maybeFile.isEmpty());
        }


//        // Given
//        var id = "fileId";
//        setupFileDownloadNotFound(id);
//        setupRetryStrategy();
//
//        // When
//        Optional<File> maybeFile = fileTransferService.get(id);
//
//        // Then
//        assertTrue(maybeFile.isEmpty());
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

//    @Test
//    void testDownloadFileNotScanned() {
//        // Given
//        var fileId = "fileID";
//        var fileName = "file_name.zip";
//        var data = "Hello World!".getBytes();
//        setupFileDetails(fileId, fileName, AvStatusApi.NOT_SCANNED, data);
//        setupRetryStrategy(() -> setupFileDownload(fileId, fileName, data));
//
//        // When
//        Optional<File> maybeFile = fileTransferService.get(fileId);
//
//        // Then
//
//        assertTrue(maybeFile.isPresent());
//        verify(retryStrategy).attempt(any());
//    }
//
//    @Test
//    @DisplayName("Unexpected response status")
//    void testUnexpectedResponseStatus() {
//        // Given
//        var fileId = "fileId";
//        var details = createDetails(fileId, "name", AvStatusApi.CLEAN, new byte[]{});
//
//        when(restTemplate.exchange(
//                fileTransferUrl + details.getLinks().getSelf(),
//                HttpMethod.GET,
//                httpEntity,
//                FileDetailsApi.class, fileId))
//                .thenReturn(ResponseEntity.internalServerError().build());
//
//        setupRetryStrategy();
//
//        // When
//
//        assertThrows(RuntimeException.class, () -> fileTransferService.get(fileId));
//
//        // Then
//
//        verify(logger).error(any(String.class), anyMap());
//    }
//
//    private String setupFileDetails(String id, String name, AvStatusApi avStatus, byte[] data) {
//        var details = createDetails(id, name, avStatus, data);
//
//        when(restTemplate.exchange(fileTransferUrl + details.getLinks().getSelf(), HttpMethod.GET, httpEntity, FileDetailsApi.class, id))
//                .thenReturn(ResponseEntity.ok(details));
//
//        return details.getLinks().getDownload();
//    }

    @NotNull
    private FileDetailsApi createDetails(String id, String name, AvStatusApi avStatus, byte[] data) {
        var getFileDetailsUrl = "/files/{id}";
        var fileDownloadUrl = getFileDetailsUrl + "/download";

        return new FileDetailsApi(id,
                "String avTimestamp",
                avStatus,
                "String contentType",
                data.length,
                name,
                "String createdOn",
                new FileLinksApi(fileDownloadUrl, getFileDetailsUrl));
    }

//    private void setupFileDownload(String id, String name, byte[] data) {
//        var fileDownloadUrl = setupFileDetails(id, name, AvStatusApi.CLEAN, data);
//
//        when(restTemplate.exchange(fileTransferUrl + fileDownloadUrl, HttpMethod.GET, httpEntity, byte[].class))
//                .thenReturn(ResponseEntity.ok(data));
//    }
//
//    void setupFileDownloadNotFound(String id) {
//        var getFileDetailsUrl = fileTransferUrl + "/files/{id}";
//
//        when(restTemplate.exchange(getFileDetailsUrl, HttpMethod.GET, httpEntity, FileDetailsApi.class, id))
//                .thenReturn(ResponseEntity.notFound().build());
//    }
//
//    @Test
//    @DisplayName("Deletes a file using the fil transfer service")
//    void delete() {
//        // Given
//        var id = "fileId";
//        var fileDeleteUrlTemplate = fileTransferUrl + "/{id}";
//
//        // When
//        fileTransferService.delete(id);
//
//        verify(restTemplate).exchange(
//                fileDeleteUrlTemplate,
//                HttpMethod.DELETE,
//                httpEntity,
//                Void.class,
//                id);
//
//    }
}