package uk.gov.companieshouse.account.validator.model.validation;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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

    @Id
//    @JsonProperty("file_id")
    private final String fileId;

    @Field("status")
    private final String status;

    @Field("result")
    private final ValidationResult result;

    public RequestStatus(String fileId, String status, ValidationResult result) {
        this.fileId = fileId;
        this.status = status;
        this.result = result;
    }

    public static RequestStatus pending(String fileId) {
        return new RequestStatus(fileId, STATE_PENDING, null);
    }

    public static RequestStatus complete(String fileId, ValidationResult result) {
        return new RequestStatus(fileId, STATE_COMPLETE, result);
    }

    public String getFileId() {
        return fileId;
    }

    public String getStatus() {
        return status;
    }

    public ValidationResult getResult() {
        return result;
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