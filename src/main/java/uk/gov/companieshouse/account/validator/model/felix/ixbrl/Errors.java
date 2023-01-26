package uk.gov.companieshouse.account.validator.model.felix.ixbrl;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "errors")
public class Errors {

    private String errorMessage;

    @XmlElement(name = "ErrorMessage")
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "Errors [errorMessage=" + errorMessage + "]";
    }
}
