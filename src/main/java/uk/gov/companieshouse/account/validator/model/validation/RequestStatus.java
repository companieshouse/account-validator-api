package uk.gov.companieshouse.account.validator.model.validation;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.account.validator.model.felix.ixbrl.Results;
import uk.gov.companieshouse.api.model.felixvalidator.ValidationStatusApi;

import java.util.Objects;

/*
 * Request status defines what stage the request is in.
 * @see CompleteStatus
 * @see PendingStatus
 */
@Document("validation_request_status")
public final class RequestStatus {

    public static final String STATE_PENDING = "pending";
    public static final String STATE_COMPLETE = "complete";
    public static final String STATE_ERROR = "error";

    @Id
    private final String fileId;

    @Field
    private final String fileName;

    @Field("status")
    private final String status;

    @Field("result")
    private final Results result;

    public RequestStatus(String fileId, String fileName, String status, Results result) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.status = status;
        this.result = result;
    }

    public static RequestStatus pending(String fileId, String fileName, ValidationStatusApi status) {
        Results results = new Results();
        results.setValidationStatus(status);
        return new RequestStatus(fileId, fileName, STATE_PENDING, results);
    }

    public static RequestStatus complete(String fileId, String fileName, Results result) {
        return new RequestStatus(fileId, fileName, STATE_COMPLETE, result);
    }

    public static RequestStatus error(String fileId) {
        return new RequestStatus(fileId, "", STATE_ERROR, null);
    }

    public static RequestStatus fromResults(String fileId, Results results, String fileName) {
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

    public String getFileId() {
        return fileId;
    }

    public String getStatus() {
        return status;
    }

    public Results getResult() {
        return result;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RequestStatus) obj;
        return Objects.equals(this.fileId, that.fileId) && Objects.equals(this.status, that.status) && Objects.equals(this.result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileId, status, result);
    }

    @Override
    public String toString() {
        return "RequestStatus[" +
                "fileId=" + fileId + ", " +
                "status=" + status + ", " +
                "result=" + result + ']';
    }
}