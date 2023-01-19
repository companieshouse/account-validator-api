package uk.gov.companieshouse.account.validator.validation.ixbrl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@JacksonXmlRootElement(localName="data")
public class Data {

    public Data(){}
    @JacksonXmlProperty(localName="BalanceSheetDate")
    @JsonProperty("BalanceSheetDate")
   private String balanceSheetDate;

    @JacksonXmlProperty(localName="AccountsType")
    @JsonProperty("AccountsType")
    private String accountsType;

    @JacksonXmlProperty(localName="CompaniesHouseRegisteredNumber")
    @JsonProperty("CompaniesHouseRegisteredNumber")
    private String companiesHouseRegisteredNumber;

    public String getBalanceSheetDate() {
        return balanceSheetDate;
    }

    public void setBalanceSheetDate(String balanceSheetDate) {
        this.balanceSheetDate = balanceSheetDate;
    }

    public String getAccountsType() {
        return accountsType;
    }

    public void setAccountsType(String accountsType) {
        this.accountsType = accountsType;
    }

    public String getCompaniesHouseRegisteredNumber() {
        return companiesHouseRegisteredNumber;
    }

    public void setCompaniesHouseRegisteredNumber(String companiesHouseRegisteredNumber) {
        this.companiesHouseRegisteredNumber = companiesHouseRegisteredNumber;
    }
}
