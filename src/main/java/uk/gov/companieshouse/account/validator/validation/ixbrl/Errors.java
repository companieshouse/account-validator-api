package uk.gov.companieshouse.account.validator.validation.ixbrl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@JacksonXmlRootElement(localName="errors")
public class Errors {
    public Errors(){}

    @JacksonXmlProperty(localName="ErrorMessage")
    @JsonProperty("ErrorMessage")
    private String errorMessage;


    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
