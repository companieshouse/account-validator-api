package uk.gov.companieshouse.account.validator.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.account.validator.model.validation.RequestStatus.STATE_PENDING;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.account.validator.exceptionhandler.MissingEnvironmentVariableException;
import uk.gov.companieshouse.account.validator.exceptionhandler.ResponseException;
import uk.gov.companieshouse.account.validator.exceptionhandler.XBRLValidationException;
import uk.gov.companieshouse.account.validator.model.File;
import uk.gov.companieshouse.account.validator.model.content.AccountsDetails;
import uk.gov.companieshouse.account.validator.model.validation.RequestStatus;
import uk.gov.companieshouse.account.validator.model.validation.ValidationRequest;
import uk.gov.companieshouse.account.validator.repository.RequestStatusRepository;
import uk.gov.companieshouse.account.validator.service.AccountValidationStrategy;
import uk.gov.companieshouse.account.validator.service.file.transfer.FileTransferStrategy;
import uk.gov.companieshouse.account.validator.service.maintenance.AccountMaintenanceService;
import uk.gov.companieshouse.api.model.felixvalidator.PackageTypeApi;
import uk.gov.companieshouse.api.model.filetransfer.FileDetailsApi;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.logging.Logger;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AccountValidationControllerTest {

    AccountsDetails accountsDetails;

    AccountsDetails accountsDetailsWithoutPackage;

    @Mock
    AccountValidationStrategy accountValidationStrategy;

    @Mock
    FileTransferStrategy fileTransferStrategy;

    @Mock
    Logger logger;

    @Mock
    RequestStatusRepository repository;

    @Mock
    RestTemplate restTemplate;

    @Mock
    RequestStatus requestStatus;

    @Mock
    ValidationRequest validationRequest;

    @Mock
    EnvironmentReader environmentReader;

    @Mock
    File file;

    AccountValidationController controller;

    @Mock
    AccountMaintenanceService accountMaintenanceService;

    @Captor
    ArgumentCaptor<RequestStatus> requestStatusCaptor;

    @Captor
    ArgumentCaptor<FileDetailsApi> detailsApiArgumentCaptor;

    @BeforeEach
    void setUp() {
        controller = new AccountValidationController(
                accountValidationStrategy,
                fileTransferStrategy,
                logger,
                repository,
                restTemplate,
                environmentReader,
                accountMaintenanceService);
        accountsDetails = new AccountsDetails(PackageTypeApi.UKSEF);
        accountsDetailsWithoutPackage = new AccountsDetails();
        
    }

    @Test
    @DisplayName("Submit file for validation")
    void submitForValidation() throws XBRLValidationException {
        // Given
        String fileId = "fileId";
        setupFile(fileId, file);

        // When
        when(validationRequest.getPackageType()).thenReturn(PackageTypeApi.UKSEF);
        var resp = controller.submitForValidation(validationRequest);

        // Then

        // pending status was returned
        assertThat(resp.getBody(), instanceOf(RequestStatus.class));
        RequestStatus body = (RequestStatus) resp.getBody();
        assertNotNull(body);
        assertEquals(body.getStatus(), STATE_PENDING);

        // Pending status was saved to the database
        verify(repository).save(requestStatusCaptor.capture());
        RequestStatus requestStatus = requestStatusCaptor.getValue();
        assertEquals(requestStatus.getStatus(), STATE_PENDING);

        // Validation was started
        verify(accountValidationStrategy).startValidation(detailsApiArgumentCaptor.capture(), eq(accountsDetails));
        assertEquals(detailsApiArgumentCaptor.getValue().getId(), fileId);

    }

    @Test
    @DisplayName("Submit file for validation without package type")
    void submitForValidationWithoutPackageType() throws XBRLValidationException {
        // Given
        String fileId = "fileId";
        setupFile(fileId, file);

        // When
        when(validationRequest.getPackageType()).thenReturn(null);
        var resp = controller.submitForValidation(validationRequest);

        // Then

        // pending status was returned
        assertThat(resp.getBody(), instanceOf(RequestStatus.class));
        RequestStatus body = (RequestStatus) resp.getBody();
        assertNotNull(body);
        assertEquals(body.getStatus(), STATE_PENDING);

        // Pending status was saved to the database
        verify(repository).save(requestStatusCaptor.capture());
        RequestStatus requestStatus = requestStatusCaptor.getValue();
        assertEquals(requestStatus.getStatus(), STATE_PENDING);

        // Validation was started
        verify(accountValidationStrategy).startValidation(detailsApiArgumentCaptor.capture(), eq(accountsDetailsWithoutPackage));
        assertEquals(detailsApiArgumentCaptor.getValue().getId(), fileId);

    }


    @Test
    @DisplayName("Returns 404 when the request file is not available")
    void submitForValidationFileNotFound() throws XBRLValidationException {
        // Given
        setupFile("fileId");

        // When
        var resp = controller.submitForValidation(validationRequest);

        // Then
        assertThat(resp.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    private void setupFile(String id, File file) {
        when(validationRequest.getId()).thenReturn(id);

        var maybeFile = Optional.ofNullable(file);
        var maybeFileDetails = maybeFile.map(f -> new FileDetailsApi());
        when(fileTransferStrategy.getDetails(id)).thenReturn(maybeFileDetails);
        if (maybeFile.isPresent()) {
            ReflectionTestUtils.setField(maybeFileDetails.get(), "id", id);
        }
    }

    private void setupFile(String id) {
        setupFile(id, null);
    }

    @Test
    @DisplayName("When a file that is requested is not present 404 is returned")
    void getStatusNotFound() {
        // Given
        var fileId = "FileID";
        when(repository.findById(fileId)).thenReturn(Optional.empty());

        // When
        var resp = controller.getStatus(fileId);

        // Then
        assertThat(resp.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("When a file is present, 200 is returned along with the request status")
    void getStatus() {
        // Given
        var fileId = "FileID";
        when(repository.findById(fileId)).thenReturn(Optional.of(requestStatus));

        // When
        var resp = controller.getStatus(fileId);

        // Then
        assertThat(resp.getStatusCode(), is(HttpStatus.OK));
        assertThat(resp.getBody(), instanceOf(RequestStatus.class));
    }

    @Test
    @DisplayName("Render xhtml as pdf")
    void render() {
        byte[] expectedBytes = "hello".getBytes();

        // Given
        when(fileTransferStrategy.get(anyString())).thenReturn(Optional.of(new File(null, null, new byte[]{})));
        when(environmentReader.getMandatoryString(anyString())).thenReturn("anything");
        when(restTemplate.postForObject(anyString(), anyMap(), any())).thenReturn(expectedBytes);

        // When
        var actual = controller.render("fileId");

        // Then
        assertThat(actual.getStatusCode(), is(HttpStatus.OK));
        assertThat(actual.getHeaders().getContentType(), is(equalTo(MediaType.APPLICATION_PDF)));
        assertThat(actual.getBody(), is(equalTo(expectedBytes)));
        verify(fileTransferStrategy).get(anyString());
        verify(restTemplate).postForObject(anyString(), anyMap(), any());
    }

    @Test
    @DisplayName("Returns 404 when the request file is not available on render")
    void renderFileNotFound() {
        // Given
        when(fileTransferStrategy.get(anyString())).thenReturn(Optional.empty());

        // When
        var actual = controller.render("fileId");

        // Then
        assertThat(actual.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("Throws MissingEnvironmentVariableException when missing Ixbrl to pdf uri env var on render")
    void renderMissingEnvironmentVariableException() {
        // Given

        // When
        when(fileTransferStrategy.get(anyString())).thenReturn(Optional.of(new File(null, null, "hello".getBytes())));
        when(environmentReader.getMandatoryString(anyString())).thenReturn(null);

        // Then
        assertThrows(MissingEnvironmentVariableException.class, () -> controller.render("fileId"));
    }

    @Test
    @DisplayName("Throws RestClientException on render")
    void renderRestClientException() {
        // Given
        when(fileTransferStrategy.get(anyString())).thenReturn(Optional.of(new File(null, null, "hello".getBytes())));
        when(environmentReader.getMandatoryString(anyString())).thenReturn("anything");
        when(restTemplate.postForObject(anyString(), anyMap(), any())).thenThrow(new RestClientException("anything"));

        // When

        // Then
        assertThrows(RestClientException.class, () -> controller.render("fileId"));
    }

    @Test
    @DisplayName("Exception handler for no body returns bad request")
    void noBodyException() {
        // Given

        // When
        ResponseEntity<?> response = controller.noBodyException();

        // Then
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.BAD_REQUEST)));
        assertThat(response.getBody(), is(equalTo("Request required a body")));
    }

    @Test
    @DisplayName("Exception handler when response")
    void responseException() {
        // Given

        // When
        ResponseEntity<?> response = controller.responseException(new ResponseException());

        // Then
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.BAD_REQUEST)));
        assertThat((String) response.getBody(), containsString("Api Response failed.")); //
    }

    @Test
    @DisplayName("Exception handler when response")
    void validationException() {
        // Given

        // When
        ResponseEntity<?> response = controller.validationException();

        // Then
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.BAD_REQUEST)));
        assertThat(response.getBody(), is(equalTo("Validation failed")));
    }

    @Test
    @DisplayName("Exception handler logs error and returns 500")
    void exceptionHandler() {
        // Given
        Exception e = new Exception();

        // When
        ResponseEntity<?> response = controller.exceptionHandler(e);

        // Then
        verify(logger).error("Unhandled exception", e);
        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    @DisplayName("Test to delete files from S3 bucket & mongodb")
    void deleteFiles() {
        // Given

        // When
        ResponseEntity<?> response = controller.delete();

        // Then
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(accountMaintenanceService, times(1)).deleteCompleteSubmissions();
    }

    @Test
    @DisplayName("Exception handler when delete complete submission activity failed and returns 500")
    void deleteCompleteSubExceptionHandler() {

        ResponseEntity<?> response = controller.deleteCompleteSubException();

        // Then
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.INTERNAL_SERVER_ERROR)));
        assertThat(response.getBody(), is(equalTo("Delete complete submission activity failed")));
    }

}