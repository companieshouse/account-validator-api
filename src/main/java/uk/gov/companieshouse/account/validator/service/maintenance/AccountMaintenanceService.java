package uk.gov.companieshouse.account.validator.service.maintenance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.account.validator.exceptionhandler.DeleteCompleteSubException;
import uk.gov.companieshouse.account.validator.model.validation.RequestStatus;
import uk.gov.companieshouse.account.validator.repository.RequestStatusRepository;
import uk.gov.companieshouse.account.validator.service.file.transfer.FileTransferStrategy;
import uk.gov.companieshouse.logging.Logger;

import java.util.Collection;
import java.util.List;

/**
 * An implementation of the AccountMaintenanceService to maintain company's account files
 */
@Service
public class AccountMaintenanceService {

    private final FileTransferStrategy fileTransferStrategy;
    private final Logger logger;
    private final RequestStatusRepository statusRepository;


    @Autowired
    public AccountMaintenanceService(Logger logger, FileTransferStrategy fileTransferStrategy, RequestStatusRepository statusRepository) {
        this.logger = logger;
        this.fileTransferStrategy = fileTransferStrategy;
        this.statusRepository = statusRepository;
    }

    public static boolean isEmptyOrNull(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    public void deleteCompleteSubmissions() {
        logger.info("Inside deleteFiles method");
        try {
            List<RequestStatus> completeRequestStatusList = statusRepository.findByStatus(RequestStatus.STATE_COMPLETE);
            if (!isEmptyOrNull(completeRequestStatusList)) {
                completeRequestStatusList.forEach(requestStatus -> deleteRequest(requestStatus.getFileId()));
            }
        } catch (RuntimeException ex) {
            throw new DeleteCompleteSubException(ex);

        }
        logger.info("Exit deleteFiles method");
    }

    private void deleteRequest(String fileId) {
        fileTransferStrategy.delete(fileId);
        statusRepository.deleteById(fileId);
    }
}
