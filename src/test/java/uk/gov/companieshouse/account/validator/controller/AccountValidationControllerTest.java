package uk.gov.companieshouse.account.validator.controller;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.account.validator.exceptionhandler.MissingEnvironmentVariableException;
import uk.gov.companieshouse.account.validator.model.File;
import uk.gov.companieshouse.account.validator.model.felix.ixbrl.Results;
import uk.gov.companieshouse.account.validator.model.validation.RequestStatus;
import uk.gov.companieshouse.account.validator.model.validation.ValidationRequest;
import uk.gov.companieshouse.account.validator.repository.RequestStatusRepository;
import uk.gov.companieshouse.account.validator.service.AccountValidationStrategy;
import uk.gov.companieshouse.account.validator.service.file.transfer.FileTransferStrategy;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountValidationControllerTest {

    @Mock
    AccountValidationStrategy accountValidationStrategy;

    @Mock
    FileTransferStrategy fileTransferStrategy;

    @Mock
    Logger logger;

    @Mock
    Executor executor;

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

    @BeforeEach
    void setUp() {
        controller = new AccountValidationController(accountValidationStrategy,
                fileTransferStrategy,
                logger,
                executor,
                repository,
                restTemplate,
                environmentReader);
    }

    @Test
    @DisplayName("Submit file for validation")
    void submitForValidation() {
        // Given
        setupFile("fileId", file);
        doAnswer(a -> {
            Runnable fn = a.getArgument(0);
            fn.run();
            return null;
        }).when(executor).execute(any(Runnable.class));

        // When
        var resp = controller.submitForValidation(validationRequest);

        // Then
        assertThat(resp.getStatusCode(), is(HttpStatus.OK));
        assertThat(resp.getBody(), instanceOf(RequestStatus.class));
        verify(executor).execute(any(Runnable.class));
        verify(accountValidationStrategy).validate(any(File.class));
        verify(repository, times(2)).save(any(RequestStatus.class));
    }


    @Test
    @DisplayName("Returns 404 when the request file is not available")
    void submitForValidationFileNotFound() {
        // Given
        setupFile("fileId");

        // When
        var resp = controller.submitForValidation(validationRequest);

        // Then
        assertThat(resp.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    private void setupFile(String id, File file) {
        when(validationRequest.getId()).thenReturn(id);
        when(fileTransferStrategy.get(id)).thenReturn(Optional.ofNullable(file));
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
        Assert.assertThrows(MissingEnvironmentVariableException.class, () -> controller.render("fileId"));
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
        ResponseEntity<?> response = controller.responseException();

        // Then
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.BAD_REQUEST)));
        assertThat(response.getBody(), is(equalTo("Api Response failed")));
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
    @DisplayName("Test to delete all files")
    void deleteFiles() {
        // Given

        // When
        when(repository.findByStatus(RequestStatus.STATE_COMPLETE)).thenReturn(setupCompleteRequestStatusList());
        ResponseEntity<?> response = controller.delete();

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(repository, times(1)).findByStatus(RequestStatus.STATE_COMPLETE);
        verify(fileTransferStrategy, times(5)).delete(any(String.class));
        verify(repository, times(5)).deleteById(any(String.class));

    }

    @Test
    @DisplayName("No to delete all files")
    void noFilesDeleted() {
        // Given

        // When
        ResponseEntity<?> response = controller.delete();

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(repository, times(1)).findByStatus(RequestStatus.STATE_COMPLETE);
        verify(fileTransferStrategy, times(0)).delete(any(String.class));
        verify(repository, times(0)).deleteById(any(String.class));
    }

    private List<RequestStatus> setupCompleteRequestStatusList(){
        List<RequestStatus> completeList = new ArrayList<RequestStatus>();
        for(int i=0; i<5;i++){
            RequestStatus completeStatus = new RequestStatus("test"+i, "Testing",RequestStatus.STATE_COMPLETE,new Results());
            completeList.add(completeStatus);
        }
        return completeList;
    }
}