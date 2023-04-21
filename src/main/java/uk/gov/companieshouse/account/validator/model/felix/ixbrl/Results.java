package uk.gov.companieshouse.account.validator.model.felix.ixbrl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "results")
public class Results {

    private List<Errors> errors;

    private String validationStatus;

    private Data data;

    @XmlElement
    @JsonProperty("errorMessages")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Errors> getErrors() {
        return errors;
    }

    public void setErrors(List<Errors> errors) {
        this.errors = errors;
    }

    @XmlElement
    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @XmlAttribute
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
