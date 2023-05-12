package uk.gov.companieshouse.account.validator.model.felix.ixbrl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "results")
public class Results {

    public static String STATUS_OK = "OK";
    public static String STATUS_FAIL = "FAILED";
    public static String STATUS_ERROR = "ERROR";

    private List<Errors> errors;

    private String validationStatus;

    private Data data;

    @JacksonXmlElementWrapper(localName = "errors")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("errorMessages")
    public List<Errors> getErrors() {
        return errors;
    }

    public void setErrors(List<Errors> errors) {
        this.errors = errors;
    }

    @JacksonXmlProperty(localName = "data")
    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @JacksonXmlProperty(isAttribute = true, localName = "validationStatus")
    public String getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(String validationStatus) {
        this.validationStatus = validationStatus;
    }

    @Override
    public String toString() {
        return "Results [errors=" + errors + ", validationStatus=" + validationStatus + ", data=" + data + "]";
    }
}
