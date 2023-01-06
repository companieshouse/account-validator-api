package uk.gov.companieshouse.account.validator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.account.validator.JsonDateDeserializer;

import java.util.Date;

import static org.springframework.util.Assert.notNull;

/**
 * Represents a collection of accounts
 */
@Document(collection = "accounts_validator")
public class AccountValidated implements Prunable{
    
    @Id
    @Field("_id")
    @JsonProperty("id")
    private String id;

    @Field("customer_id")
    @JsonProperty("customer_id")
    private String customerId;

    @Field("file_name")
    @JsonProperty("file_name")
    private String filename;

    @Field("file_size")
    @JsonProperty("file_size")
    private String fileSize;

    @Field("s3_key")
    @JsonProperty("s3_key")
    private String s3Key;

    @Field("created_at")
    @JsonDeserialize(using = JsonDateDeserializer.class)
    @JsonProperty("created_at")
    private Date createdAt;

    @Field("status")
    @JsonProperty("status")
    private String status;

    @Field("validation")
    @JsonMerge
    @JsonProperty("validation")
    private ValidationResponse data;

    @Field("show_image")
    @JsonProperty("show_image")
    private boolean showImage;

    /**
     * Constructor
     */
    public AccountValidated() {}

    /**
     * Constructor
     *
     * @param id
     * @param validationResponse
     */
    public AccountValidated(String id, ValidationResponse validationResponse) {
        notNull(id, "Id cannot be null");
        notNull(validationResponse, "ValidationResponse cannot be null");

        this.id = id;
        this.data = validationResponse;
    }

    /**
     * {@inheritDoc}
     */
    @JsonIgnore
    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prune() {
        prune(data);
    }

    public String getId() {
        return id;
    }

    @JsonIgnore
    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ValidationResponse getData() {
        return data;
    }

    public void setData(ValidationResponse data) {
        this.data = data;
    }

    public boolean getShowImage() {
        return showImage;
    }

    public void setShowImage(boolean showImage) {
        this.showImage = showImage;
    }
}
