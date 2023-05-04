package uk.gov.companieshouse.account.validator.model.felix.ixbrl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

@JacksonXmlRootElement(localName = "ErrorMessage")
public class Errors {

    private String errorMessage;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public Errors(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @JacksonXmlText
    @JsonProperty("errorMessage")
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
