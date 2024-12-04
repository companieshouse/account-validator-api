package uk.gov.companieshouse.account.validator.service.factory.request.status;

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
class requestStatusFactoryTest {

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

    RequestStatusFactory requestStatusFactory;

    @BeforeEach
    public void before() {
        requestStatusFactory = new RequestStatusFactory(statusRepository);
    }

    @Test
    void requestStatusCompleteTest() {
        RequestStatus requestStatus = new RequestStatus(FILE_NAME, FILE_ID, COMPLETE, result, createTime, createTime);
        when(statusRepository.findById(FILE_ID)).thenReturn(Optional.of(requestStatus));
        RequestStatus resultStatus = requestStatusFactory.complete(FILE_ID, FILE_NAME, result);
        assertEquals(FILE_ID, resultStatus.fileId());
        assertEquals(FILE_NAME, resultStatus.fileName());
        assertEquals(result, resultStatus.result());
        assertEquals(COMPLETE, resultStatus.status());
        assertEquals(createTime, resultStatus.createdDateTime());
        assertNotEquals(createTime, resultStatus.updatedDateTime());
    }

    @Test
    void requestStatusErrorTest() {
        RequestStatus requestStatus = new RequestStatus(FILE_NAME, FILE_ID, COMPLETE, result, createTime, createTime);
        when(statusRepository.findById(FILE_ID)).thenReturn(Optional.of(requestStatus));
        RequestStatus resultStatus = requestStatusFactory.error(FILE_ID);
        assertEquals(FILE_ID, resultStatus.fileId());
        assertEquals("", resultStatus.fileName());
        assertNull(resultStatus.result());
        assertEquals(ERROR, resultStatus.status());
        assertEquals(createTime, resultStatus.createdDateTime());
        assertNotEquals(createTime, resultStatus.updatedDateTime());
    }

    @Test
    void requestStatusPendingTest() {
        when(statusRepository.findById(FILE_ID)).thenReturn(Optional.empty());
        RequestStatus resultStatus = requestStatusFactory.pending(FILE_ID, FILE_NAME, ValidationStatusApi.SENT_TO_TNDP);
        assertEquals(FILE_ID, resultStatus.fileId());
        assertEquals(FILE_NAME, resultStatus.fileName());
        assertEquals(ValidationStatusApi.SENT_TO_TNDP, resultStatus.result().getValidationStatus());
        assertEquals(PENDING, resultStatus.status());
        assertEquals(LocalDateTime.class, resultStatus.createdDateTime().getClass());
        assertEquals(LocalDateTime.class, resultStatus.updatedDateTime().getClass());
        assertEquals(resultStatus.updatedDateTime(), resultStatus.createdDateTime());
    }

    @Test
    void requestStatusPendingWithExistingUpdatedDateTimeTest() {
        RequestStatus requestStatus = new RequestStatus(FILE_NAME, FILE_ID, PENDING, result, createTime, createTime);
        when(statusRepository.findById(FILE_ID)).thenReturn(Optional.of(requestStatus));
        RequestStatus resultStatus = requestStatusFactory.pending(FILE_ID, FILE_NAME, ValidationStatusApi.SENT_TO_TNDP);
        assertEquals(FILE_ID, resultStatus.fileId());
        assertEquals(FILE_NAME, resultStatus.fileName());
        assertEquals(ValidationStatusApi.SENT_TO_TNDP, resultStatus.result().getValidationStatus());
        assertEquals(PENDING, resultStatus.status());
        assertEquals(LocalDateTime.class, resultStatus.createdDateTime().getClass());
        assertEquals(LocalDateTime.class, resultStatus.updatedDateTime().getClass());
        assertNotEquals(resultStatus.updatedDateTime(), resultStatus.createdDateTime());
    }

    @Test
    void requestStatusFromResultsPending() {
        when(result.getValidationStatus()).thenReturn(ValidationStatusApi.SENT_TO_TNDP);
        RequestStatus resultStatus = requestStatusFactory.fromResults(FILE_ID, result, FILE_NAME);
        assertEquals(FILE_ID, resultStatus.fileId());
        assertEquals(FILE_NAME, resultStatus.fileName());
        assertEquals(ValidationStatusApi.SENT_TO_TNDP, resultStatus.result().getValidationStatus());
        assertEquals(PENDING, resultStatus.status());
        assertEquals(LocalDateTime.class, resultStatus.createdDateTime().getClass());
        assertEquals(LocalDateTime.class, resultStatus.updatedDateTime().getClass());
        assertEquals(resultStatus.updatedDateTime(), resultStatus.createdDateTime());
    }

    @Test
    void requestStatusFromResultsOK() {
        when(result.getValidationStatus()).thenReturn(ValidationStatusApi.OK);
        RequestStatus requestStatus = new RequestStatus(FILE_NAME, FILE_ID, COMPLETE, result, createTime, createTime);
        when(statusRepository.findById(FILE_ID)).thenReturn(Optional.of(requestStatus));
        RequestStatus resultStatus = requestStatusFactory.fromResults(FILE_ID, result, FILE_NAME);
        assertEquals(FILE_ID, resultStatus.fileId());
        assertEquals(FILE_NAME, resultStatus.fileName());
        assertEquals(result, resultStatus.result());
        assertEquals(COMPLETE, resultStatus.status());
        assertEquals(createTime, resultStatus.createdDateTime());
        assertNotEquals(createTime, resultStatus.updatedDateTime());
    }

    @Test
    void requestStatusFromResultsERROR() {
        when(result.getValidationStatus()).thenReturn(ValidationStatusApi.ERROR);
        RequestStatus requestStatus = new RequestStatus(FILE_NAME, FILE_ID, COMPLETE, result, createTime, createTime);
        when(statusRepository.findById(FILE_ID)).thenReturn(Optional.of(requestStatus));
        RequestStatus resultStatus = requestStatusFactory.fromResults(FILE_ID, result, FILE_NAME);
        assertEquals(FILE_ID, resultStatus.fileId());
        assertEquals("", resultStatus.fileName());
        assertNull(resultStatus.result());
        assertEquals(ERROR, resultStatus.status());
        assertEquals(createTime, resultStatus.createdDateTime());
        assertNotEquals(createTime, resultStatus.updatedDateTime());
    }

    @Test
    void requestStatusFromResultsFAILED() {
        when(result.getValidationStatus()).thenReturn(ValidationStatusApi.FAILED);
        RequestStatus requestStatus = new RequestStatus(FILE_NAME, FILE_ID, COMPLETE, result, createTime, createTime);
        when(statusRepository.findById(FILE_ID)).thenReturn(Optional.of(requestStatus));
        RequestStatus resultStatus = requestStatusFactory.fromResults(FILE_ID, result, FILE_NAME);
        assertEquals(FILE_ID, resultStatus.fileId());
        assertEquals(FILE_NAME, resultStatus.fileName());
        assertEquals(result, resultStatus.result());
        assertEquals(COMPLETE, resultStatus.status());
        assertEquals(createTime, resultStatus.createdDateTime());
        assertNotEquals(createTime, resultStatus.updatedDateTime());
    }
}
