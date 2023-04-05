package uk.gov.companieshouse.account.validator.service.maintenance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.account.validator.model.felix.ixbrl.Results;
import uk.gov.companieshouse.account.validator.model.validation.RequestStatus;
import uk.gov.companieshouse.account.validator.repository.RequestStatusRepository;
import uk.gov.companieshouse.account.validator.service.file.transfer.FileTransferStrategy;
import uk.gov.companieshouse.logging.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

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

    @Test
    @DisplayName("Deletes a file using the file transfer service")
    void deleteFiles() {
        // Given

        // When
        when(statusRepository.findByStatus(RequestStatus.STATE_COMPLETE)).thenReturn(createRequestStatusList());

        accountMaintenanceService.deleteCompleteSubmissions();

        // Then
        verify(statusRepository, times(1)).findByStatus(RequestStatus.STATE_COMPLETE);
        verify(fileTransferStrategy, times(5)).delete(anyString());
        verify(statusRepository, times(5)).deleteById(anyString());
    }

    @Test
    @DisplayName("When there is no file to file")
    void noFilesToDelete() {
        // Given

        // When
        when(statusRepository.findByStatus(RequestStatus.STATE_COMPLETE)).thenReturn(new ArrayList<>());

        accountMaintenanceService.deleteCompleteSubmissions();

        // Then
        verify(statusRepository, times(1)).findByStatus(RequestStatus.STATE_COMPLETE);
        verify(fileTransferStrategy, times(0)).delete(anyString());
        verify(statusRepository, times(0)).deleteById(anyString());
    }

    @Test
    @DisplayName("Throw DeleteCompleteSubException when Mongodb/ Aws S3 connection is impacted")
    void throwDeleteCompleteSubException() {
        // Given

        // When
        when(statusRepository.findByStatus(RequestStatus.STATE_COMPLETE)).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> accountMaintenanceService.deleteCompleteSubmissions());
        // Then
        verify(statusRepository, times(1)).findByStatus(RequestStatus.STATE_COMPLETE);
    }

    private List<RequestStatus> createRequestStatusList(){
        List<RequestStatus> requestStatusList = new ArrayList<RequestStatus>();
        for(int i=0;i<5;i++){
            RequestStatus completeRequestStatus = new RequestStatus("mockId-" + i, "MockFilename", RequestStatus.STATE_COMPLETE, new Results());
            requestStatusList.add(completeRequestStatus);
        }
        return requestStatusList;
    }
}
