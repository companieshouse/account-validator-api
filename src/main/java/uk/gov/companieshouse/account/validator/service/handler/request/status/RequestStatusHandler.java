package uk.gov.companieshouse.account.validator.service.handler.request.status;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.account.validator.model.felix.ixbrl.Results;
import uk.gov.companieshouse.account.validator.model.validation.RequestStatus;
import uk.gov.companieshouse.account.validator.repository.RequestStatusRepository;
import uk.gov.companieshouse.api.model.felixvalidator.ValidationStatusApi;

@Component
public class RequestStatusHandler {

    private final RequestStatusRepository statusRepository;

    @Autowired
    public RequestStatusHandler(
            RequestStatusRepository statusRepository) {
        this.statusRepository = statusRepository;
    }

    public RequestStatus pending(String fileId, String fileName, ValidationStatusApi status) {
        Results results = new Results();
        results.setValidationStatus(status);
        LocalDateTime createdDateTime = getCreatedDateTime(fileId);
        // Either it does not exist - set to createdDate or set new localDateTime
        LocalDateTime modifiedDateTime = isModifiedDateTimePresenter(fileId) ? LocalDateTime.now() : createdDateTime;
        return new RequestStatus(fileId, fileName, RequestStatus.STATE_PENDING, results, createdDateTime,
                modifiedDateTime);
    }

    public RequestStatus complete(String fileId, String fileName, Results result) {
        return new RequestStatus(fileId, fileName, RequestStatus.STATE_COMPLETE, result, getCreatedDateTime(fileId),
                LocalDateTime.now());
    }

    public RequestStatus error(String fileId) {
        return new RequestStatus(fileId, "", RequestStatus.STATE_ERROR, null, getCreatedDateTime(fileId),
                LocalDateTime.now());
    }

    public RequestStatus fromResults(String fileId, Results results, String fileName) {
        switch (results.getValidationStatus()) {
            case ERROR:
                return error(fileId);
            case FAILED:
            case OK:
                return complete(fileId, fileName, results);
            default:
                return pending(fileId, fileName, results.getValidationStatus());
        }
    }

    private LocalDateTime getCreatedDateTime(String fileId) {
        return statusRepository.findById(fileId)
                .map(RequestStatus::createdDateTime)
                .orElse(LocalDateTime.now());
    }

    private boolean isModifiedDateTimePresenter(String fileId) {
        return statusRepository.findById(fileId)
                .map(RequestStatus::modifiedDateTime)
                .isPresent();
    }
}
