package uk.gov.companieshouse.account.validator.model.validation;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.account.validator.model.felix.ixbrl.Results;

import java.time.LocalDateTime;
/*
 * Request status defines what stage the request is in.
 * @see CompleteStatus
 * @see PendingStatus
 */
@Document("validation_request_status")
public record RequestStatus(
    @Id String fileId, 
    @Field String fileName, 
    @Field String status, 
    @Field Results result, 
    @Field("created") LocalDateTime createdDateTime, 
    @Field("modified") LocalDateTime modifiedDateTime)  {

    public static final String STATE_PENDING = "pending";
    public static final String STATE_COMPLETE = "complete";
    public static final String STATE_ERROR = "error";

    @Override
    public String toString() {
        return "RequestStatus[" +
                "fileId=" + fileId + ", " +
                "status=" + status + ", " +
                "result=" + result + ']';
    }
}