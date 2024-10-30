package uk.gov.companieshouse.account.validator.service.handler.request.status;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.account.validator.model.felix.ixbrl.Results;
import uk.gov.companieshouse.account.validator.model.validation.RequestStatus;
import uk.gov.companieshouse.account.validator.repository.RequestStatusRepository;
import uk.gov.companieshouse.api.model.felixvalidator.ValidationStatusApi;

@ExtendWith(MockitoExtension.class)
public class RequestStatusHandlerTest {

    private static final String COMPLETE = RequestStatus.STATE_COMPLETE;

    private static final String ERROR = RequestStatus.STATE_ERROR;

    private static final String PENDING = RequestStatus.STATE_PENDING;

    private static final String FILE_NAME = "fileName";

    private static final String FILE_ID = "fileId";

    @Mock
    Results result;

    @Mock
    LocalDateTime createTime;

    @Mock
    LocalDateTime localDateTime;

    @Mock
    RequestStatusRepository statusRepository;

    RequestStatusHandler requestStatusHandler;

    @BeforeEach
    public void before() {
        requestStatusHandler = new RequestStatusHandler(statusRepository);
    }

    @Test
    public void requestStatusCompleteTest() {
        RequestStatus requestStatus = new RequestStatus(FILE_NAME, FILE_ID, COMPLETE, result, createTime, createTime);
        when(statusRepository.findById(FILE_ID)).thenReturn(Optional.of(requestStatus));
        RequestStatus resultStatus = requestStatusHandler.complete(FILE_ID, FILE_NAME, result);
        assertEquals(FILE_ID, resultStatus.fileId());
        assertEquals(FILE_NAME, resultStatus.fileName());
        assertEquals(result, resultStatus.result());
        assertEquals(COMPLETE, resultStatus.status());
        assertEquals(createTime, resultStatus.createdDateTime());
        assertNotEquals(createTime, resultStatus.modifiedDateTime());
    }

    @Test
    public void requestStatusErrorTest() {
        RequestStatus requestStatus = new RequestStatus(FILE_NAME, FILE_ID, COMPLETE, result, createTime, createTime);
        when(statusRepository.findById(FILE_ID)).thenReturn(Optional.of(requestStatus));
        RequestStatus resultStatus = requestStatusHandler.error(FILE_ID);
        assertEquals(FILE_ID, resultStatus.fileId());
        assertEquals("", resultStatus.fileName());
        assertNull(resultStatus.result());
        assertEquals(ERROR, resultStatus.status());
        assertEquals(createTime, resultStatus.createdDateTime());
        assertNotEquals(createTime, resultStatus.modifiedDateTime());
    }

    @Test
    public void requestStatusPendingTest() {
        when(statusRepository.findById(FILE_ID)).thenReturn(Optional.empty());
        RequestStatus resultStatus = requestStatusHandler.pending(FILE_ID, FILE_NAME, ValidationStatusApi.SENT_TO_TNDP);
        assertEquals(FILE_ID, resultStatus.fileId());
        assertEquals(FILE_NAME, resultStatus.fileName());
        assertEquals(ValidationStatusApi.SENT_TO_TNDP, resultStatus.result().getValidationStatus());
        assertEquals(PENDING, resultStatus.status());
        assertEquals(LocalDateTime.class, resultStatus.createdDateTime().getClass());
        assertEquals(LocalDateTime.class, resultStatus.modifiedDateTime().getClass());
        assertEquals(resultStatus.modifiedDateTime(), resultStatus.createdDateTime());
    }

    @Test
    public void requestStatusPendingWithExistingModifiedDateTimeTest() {
        RequestStatus requestStatus = new RequestStatus(FILE_NAME, FILE_ID, PENDING, result, createTime, createTime);
        when(statusRepository.findById(FILE_ID)).thenReturn(Optional.of(requestStatus));
        RequestStatus resultStatus = requestStatusHandler.pending(FILE_ID, FILE_NAME, ValidationStatusApi.SENT_TO_TNDP);
        assertEquals(FILE_ID, resultStatus.fileId());
        assertEquals(FILE_NAME, resultStatus.fileName());
        assertEquals(ValidationStatusApi.SENT_TO_TNDP, resultStatus.result().getValidationStatus());
        assertEquals(PENDING, resultStatus.status());
        assertEquals(LocalDateTime.class, resultStatus.createdDateTime().getClass());
        assertEquals(LocalDateTime.class, resultStatus.modifiedDateTime().getClass());
        assertNotEquals(resultStatus.modifiedDateTime(), resultStatus.createdDateTime());
    }

    @Test
    public void requestStatusFromResultsPending() {
        when(result.getValidationStatus()).thenReturn(ValidationStatusApi.SENT_TO_TNDP);
        RequestStatus resultStatus = requestStatusHandler.fromResults(FILE_ID, result, FILE_NAME);
        assertEquals(FILE_ID, resultStatus.fileId());
        assertEquals(FILE_NAME, resultStatus.fileName());
        assertEquals(ValidationStatusApi.SENT_TO_TNDP, resultStatus.result().getValidationStatus());
        assertEquals(PENDING, resultStatus.status());
        assertEquals(LocalDateTime.class, resultStatus.createdDateTime().getClass());
        assertEquals(LocalDateTime.class, resultStatus.modifiedDateTime().getClass());
        assertEquals(resultStatus.modifiedDateTime(), resultStatus.createdDateTime());
    }

    @Test
    public void requestStatusFromResultsOK() {
        when(result.getValidationStatus()).thenReturn(ValidationStatusApi.OK);
        RequestStatus requestStatus = new RequestStatus(FILE_NAME, FILE_ID, COMPLETE, result, createTime, createTime);
        when(statusRepository.findById(FILE_ID)).thenReturn(Optional.of(requestStatus));
        RequestStatus resultStatus = requestStatusHandler.fromResults(FILE_ID, result, FILE_NAME);
        assertEquals(FILE_ID, resultStatus.fileId());
        assertEquals(FILE_NAME, resultStatus.fileName());
        assertEquals(result, resultStatus.result());
        assertEquals(COMPLETE, resultStatus.status());
        assertEquals(createTime, resultStatus.createdDateTime());
        assertNotEquals(createTime, resultStatus.modifiedDateTime());
    }

    @Test
    public void requestStatusFromResultsERROR() {
        when(result.getValidationStatus()).thenReturn(ValidationStatusApi.ERROR);
        RequestStatus requestStatus = new RequestStatus(FILE_NAME, FILE_ID, COMPLETE, result, createTime, createTime);
        when(statusRepository.findById(FILE_ID)).thenReturn(Optional.of(requestStatus));
        RequestStatus resultStatus = requestStatusHandler.fromResults(FILE_ID, result, FILE_NAME);
        assertEquals(FILE_ID, resultStatus.fileId());
        assertEquals("", resultStatus.fileName());
        assertNull(resultStatus.result());
        assertEquals(ERROR, resultStatus.status());
        assertEquals(createTime, resultStatus.createdDateTime());
        assertNotEquals(createTime, resultStatus.modifiedDateTime());
    }

    @Test
    public void requestStatusFromResultsFAILED() {
        when(result.getValidationStatus()).thenReturn(ValidationStatusApi.FAILED);
        RequestStatus requestStatus = new RequestStatus(FILE_NAME, FILE_ID, COMPLETE, result, createTime, createTime);
        when(statusRepository.findById(FILE_ID)).thenReturn(Optional.of(requestStatus));
        RequestStatus resultStatus = requestStatusHandler.fromResults(FILE_ID, result, FILE_NAME);
        assertEquals(FILE_ID, resultStatus.fileId());
        assertEquals(FILE_NAME, resultStatus.fileName());
        assertEquals(result, resultStatus.result());
        assertEquals(COMPLETE, resultStatus.status());
        assertEquals(createTime, resultStatus.createdDateTime());
        assertNotEquals(createTime, resultStatus.modifiedDateTime());
    }
}
