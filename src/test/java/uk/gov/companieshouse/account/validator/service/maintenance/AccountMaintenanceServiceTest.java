package uk.gov.companieshouse.account.validator.service.maintenance;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import uk.gov.companieshouse.account.validator.model.felix.ixbrl.Results;
import uk.gov.companieshouse.account.validator.model.validation.RequestStatus;
import uk.gov.companieshouse.account.validator.repository.RequestStatusRepository;
import uk.gov.companieshouse.account.validator.service.file.transfer.FileTransferStrategy;
import uk.gov.companieshouse.logging.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class AccountMaintenanceServiceTest {

    @Mock
    private FileTransferStrategy fileTransferStrategy;
    @Mock
    private Logger logger;
    @Mock
    private RequestStatusRepository statusRepository;

    @InjectMocks
    private AccountMaintenanceService accountMaintenanceService;

    private final static LocalDate BOUNDARY_DATE = LocalDate.now().minusDays(30);

    private static final List<String> STATUSES_TO_REMOVE = Arrays.asList(RequestStatus.STATE_COMPLETE,
            RequestStatus.STATE_ERROR);

    @BeforeEach
    void before() {
        ReflectionTestUtils.setField(accountMaintenanceService, "DAYS_TO_DELETE", 30);
    }

    @Test
    @DisplayName("Deletes a file using the file transfer service")
    void deleteFiles() {
        // Given

        // When
        when(statusRepository.findByUpdatedDateTimeLessThan(BOUNDARY_DATE))
                .thenReturn(createRequestStatusList());
        when(statusRepository.findByCreatedDateTimeIsNull()).thenReturn(
                Collections.singletonList(new RequestStatus("mockId-null", "MockFilename",
                        RequestStatus.STATE_COMPLETE,
                        new Results(), null, null)));

        accountMaintenanceService.deleteCompleteSubmissions();

        // Then
        verify(statusRepository, times(1)).findByUpdatedDateTimeLessThan(
                BOUNDARY_DATE);
        verify(fileTransferStrategy, times(7)).delete(anyString());
        verify(statusRepository, times(7)).deleteById(anyString());
    }

    @Test
    @DisplayName("When there is no file to file")
    void noFilesToDelete() {
        // Given

        // When
        when(statusRepository.findByUpdatedDateTimeLessThan(BOUNDARY_DATE))
                .thenReturn(new ArrayList<>());
        when(statusRepository.findByCreatedDateTimeIsNull())
                .thenReturn(Collections.emptyList());

        accountMaintenanceService.deleteCompleteSubmissions();

        // Then
        verify(statusRepository, times(1)).findByUpdatedDateTimeLessThan(
                BOUNDARY_DATE);
        verify(statusRepository, times(1)).findByCreatedDateTimeIsNull();
        verify(fileTransferStrategy, times(0)).delete(anyString());
        verify(statusRepository, times(0)).deleteById(anyString());
    }

    @Test
    @DisplayName("Throw DeleteCompleteSubException when Mongodb/ Aws S3 connection is impacted")
    void throwDeleteCompleteSubException() {
        // Given

        // When
        when(statusRepository.findByUpdatedDateTimeLessThan(BOUNDARY_DATE))
                .thenReturn(new ArrayList<>());
        when(statusRepository.findByCreatedDateTimeIsNull())
                .thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> accountMaintenanceService.deleteCompleteSubmissions());
        // Then
        verify(statusRepository, times(1)).findByCreatedDateTimeIsNull();
        verify(statusRepository, times(1)).findByUpdatedDateTimeLessThan(
                BOUNDARY_DATE);
        verify(fileTransferStrategy, times(0)).delete(anyString());
        verify(statusRepository, times(0)).deleteById(anyString());
    }

    private List<RequestStatus> createRequestStatusList() {
        List<RequestStatus> requestStatusList = new ArrayList<RequestStatus>();
        for (int i = 0; i < 3; i++) {
            LocalDateTime offsetDate = LocalDateTime.now().minusDays(31 + i);
            RequestStatus completeRequestStatus = new RequestStatus("mockId-" + i, "MockFilename",
                    RequestStatus.STATE_COMPLETE, new Results(), offsetDate, offsetDate);
            requestStatusList.add(completeRequestStatus);
        }
        for (int i = 0; i < 3; i++) {
            LocalDateTime offsetDate = LocalDateTime.now().minusDays(31 + i);
            RequestStatus completeRequestStatus = new RequestStatus("mockId-" + i, "MockFilename",
                    RequestStatus.STATE_ERROR, new Results(), offsetDate, offsetDate);
            requestStatusList.add(completeRequestStatus);
        }

        return requestStatusList;
    }
}
