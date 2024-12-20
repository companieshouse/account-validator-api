package uk.gov.companieshouse.account.validator.service.maintenance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.account.validator.exceptionhandler.DeleteCompleteSubException;
import uk.gov.companieshouse.account.validator.model.validation.RequestStatus;
import uk.gov.companieshouse.account.validator.repository.RequestStatusRepository;
import uk.gov.companieshouse.account.validator.service.file.transfer.FileTransferStrategy;
import uk.gov.companieshouse.logging.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An implementation of the AccountMaintenanceService to maintain company's
 * account files
 */
@Service
public class AccountMaintenanceService {

    private final FileTransferStrategy fileTransferStrategy;
    private final Logger logger;
    private final RequestStatusRepository statusRepository;

    @Value("${delete.files.older.than.days}")
    private int DAYS_TO_DELETE;

    @Autowired
    public AccountMaintenanceService(Logger logger, FileTransferStrategy fileTransferStrategy,
            RequestStatusRepository statusRepository) {
        this.logger = logger;
        this.fileTransferStrategy = fileTransferStrategy;
        this.statusRepository = statusRepository;
    }

    public static boolean isEmptyOrNull(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    public void deleteCompleteSubmissions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate date = getBoundaryDate(now, DAYS_TO_DELETE);
        Map<String, Object> infoContext = new HashMap<>();
        infoContext.put("Deletion of submissions older than", date);
        infoContext.put("Deletion requested at", now);
        logger.info("Deletion date range for old accounts", infoContext);
        try {
            List<RequestStatus> distinctRequestStatusesToRemove = allRequestStatusesToBeRemoved(date).stream()
                    .distinct().toList();
            if (!isEmptyOrNull(distinctRequestStatusesToRemove)) {
                distinctRequestStatusesToRemove.forEach(requestStatus -> deleteRequest(requestStatus.fileId()));
            }
            infoContext.put("Completed at", LocalDateTime.now());
            infoContext.put("Number of request statuses removed", distinctRequestStatusesToRemove.size());
        } catch (RuntimeException ex) {
            throw new DeleteCompleteSubException(ex);

        }
        logger.info("Completed deletion of old submissions", infoContext);
    }

    private LocalDate getBoundaryDate(LocalDateTime to, int minusDays) {
        LocalDateTime minusDate = to.minusDays(minusDays);
        return minusDate.toLocalDate();
    }

    private void deleteRequest(String fileId) {
        fileTransferStrategy.delete(fileId);
        statusRepository.deleteById(fileId);
    }

    private List<RequestStatus> allRequestStatusesToBeRemoved(LocalDate deleteLessThan) {

        List<RequestStatus> completeRequestStatusList = statusRepository
                .findByUpdatedDateTimeLessThan(deleteLessThan);
        List<RequestStatus> nullCreatedDateRequestStatuses = statusRepository
                .findByCreatedDateTimeIsNull();
        List<RequestStatus> toRemoveRequestStatuses = new ArrayList<>(completeRequestStatusList);
        toRemoveRequestStatuses.addAll(nullCreatedDateRequestStatuses);
        return toRemoveRequestStatuses;
    }
}
