package uk.gov.companieshouse.account.validator.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.account.validator.model.File;
import uk.gov.companieshouse.account.validator.model.validation.RequestStatus;
import uk.gov.companieshouse.account.validator.model.validation.ValidationRequest;
import uk.gov.companieshouse.account.validator.repository.RequestStatusRepository;
import uk.gov.companieshouse.account.validator.service.AccountValidationStrategy;
import uk.gov.companieshouse.account.validator.service.file.transfer.FileTransferStrategy;
import uk.gov.companieshouse.logging.Logger;

import java.util.Optional;
import java.util.concurrent.Executor;

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
    RequestStatus requestStatus;

    @Mock
    ValidationRequest validationRequest;

    @Mock
    File file;

    AccountValidationController controller;

    @BeforeEach
    void setUp() {
        controller = new AccountValidationController(accountValidationStrategy,
                fileTransferStrategy,
                logger,
                executor,
                repository);
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
}