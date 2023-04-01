package uk.gov.companieshouse.account.validator.service.maintenance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.account.validator.model.validation.RequestStatus;
import uk.gov.companieshouse.account.validator.repository.RequestStatusRepository;
import uk.gov.companieshouse.account.validator.service.file.transfer.FileTransferStrategy;
import uk.gov.companieshouse.logging.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * An implementation of the AccountMaintenanceService to maintain company's account files
 */
@Service
public class AccountMaintenanceService implements AccountMaintenanceStrategy {

    private final FileTransferStrategy fileTransferStrategy;
    private final Logger logger;
    private final RequestStatusRepository statusRepository;


    @Autowired
    public AccountMaintenanceService(Logger logger, FileTransferStrategy fileTransferStrategy, RequestStatusRepository statusRepository){
        this.logger = logger;
        this.fileTransferStrategy = fileTransferStrategy;
        this.statusRepository = statusRepository;
    }

    @Override
    public void deleteFiles() {
        logger.info("Inside deleteFiles method");
        List<RequestStatus> completeRequestStatusList = statusRepository.findByStatus(RequestStatus.STATE_COMPLETE);
        if(!isEmptyOrNull(completeRequestStatusList)) {
            completeRequestStatusList.forEach(requestStatus -> deleteRequest(requestStatus.getFileId()));
        }
        logger.info("Exit deleteFiles method");
    }

    private void deleteRequest(String fileId){
        fileTransferStrategy.delete(fileId);
        statusRepository.deleteById(fileId);
    }

    public static boolean isEmptyOrNull(Collection < ? > collection) {
        return (collection == null || collection.isEmpty());
    }
}
