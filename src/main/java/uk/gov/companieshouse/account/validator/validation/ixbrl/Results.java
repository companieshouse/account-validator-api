package uk.gov.companieshouse.account.validator.validation.ixbrl;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name = "results")
public class Results {

    public Results(){}

    private Errors errors;

    private String validationStatus;


    private Data data;
    
    public Errors getErrors() {
        return errors;
    }

    @XmlAttribute(name= "errors")
    public void setErrors(Errors errors) {
        this.errors = errors;
    }

    public Data getData() {
        return data;
    }

    @XmlElement(name= "data")
    public void setData(Data data) {
        this.data = data;
    }


    public String getValidationStatus() {
        return validationStatus;
    }

    @XmlAttribute(name= "validationStatus")
    public void setValidationStatus(String validationStatus) {
        this.validationStatus = validationStatus;
    }
}
