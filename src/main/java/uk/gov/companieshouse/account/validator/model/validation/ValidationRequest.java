package uk.gov.companieshouse.account.validator.model.validation;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ValidationRequest {
    @JsonProperty("customer_id")
    String customer_id;
    @JsonProperty("file_name")
    String file_name;
    @JsonProperty("s3_key")
    String s3_key;

    public String getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getS3_key() {
        return s3_key;
    }

    public void setS3_key(String s3_key) {
        this.s3_key = s3_key;
    }
}
