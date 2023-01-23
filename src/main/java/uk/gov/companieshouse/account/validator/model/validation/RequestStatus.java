package uk.gov.companieshouse.account.validator.model.validation;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/*
 * Request status defines what stage the request is in.
 * @see CompleteStatus
 * @see PendingStatus
 */
@Document("validation_request_status")
public record RequestStatus(@Id String fileId,
                            @Field("status") String status,
                            @Field("result") ValidationResult result) {

    public static final String STATE_PENDING = "pending";
    public static final String STATE_COMPLETE = "complete";

    public static RequestStatus pending(String fileId) {
        return new RequestStatus(fileId, STATE_PENDING, null);
    }

    public static RequestStatus complete(String fileId, ValidationResult result) {
        return new RequestStatus(fileId, STATE_COMPLETE, result);
    }
}
