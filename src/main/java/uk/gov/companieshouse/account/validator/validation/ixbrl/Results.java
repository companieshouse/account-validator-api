package uk.gov.companieshouse.account.validator.validation.ixbrl;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "results")
public class Results {

    private Errors errors;
    
    private String validationStatus;
    
    private Data data;
    
    @XmlElement
    public Errors getErrors() {
        return errors;
    }

    public void setErrors(Errors errors) {
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
