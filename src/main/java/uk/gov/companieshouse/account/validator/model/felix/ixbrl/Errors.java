package uk.gov.companieshouse.account.validator.model.felix.ixbrl;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "errors")
public class Errors {

    private String errorMessage;

    @JacksonXmlProperty(localName = "ErrorMessage")
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
