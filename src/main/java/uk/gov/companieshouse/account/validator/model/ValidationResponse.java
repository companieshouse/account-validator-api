package uk.gov.companieshouse.account.validator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.OptBoolean;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.account.validator.JsonDateDeserializer;
import uk.gov.companieshouse.account.validator.annotation.JsonPath;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@JsonPath(path = "$")
public class ValidationResponse {

    @Field("id")
    @JsonProperty("id")
    private String id;

    @Field("is_valid")
    @JsonProperty("is_valid")
    private boolean isValid;

    @NotNull
    @Field("tndp_response")
    @JsonProperty("tndp_response")
    private String tndpResponse;

    public String getId() {
        return id;
    }

    @JsonIgnore
    public void setId(String id) {
        this.id = id;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public String getTndpResponse() {
        return tndpResponse;
    }

    public void setTndpResponse(String tndpResponse) {
        this.tndpResponse = tndpResponse;
    }
}
