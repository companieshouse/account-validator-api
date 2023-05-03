package uk.gov.companieshouse.account.validator.model.felix.ixbrl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "data")
public class Data {

    private String balanceSheetDate;

    private String accountsType;

    private String companiesHouseRegisteredNumber;

    @JacksonXmlProperty(localName = "BalanceSheetDate")
    @JsonProperty("balance_sheet_date")
    public String getBalanceSheetDate() {
        return balanceSheetDate;
    }

    public void setBalanceSheetDate(String balanceSheetDate) {
        this.balanceSheetDate = balanceSheetDate;
    }

    @JacksonXmlProperty(localName = "AccountsType")
    @JsonProperty("accounts_type")
    public String getAccountsType() {
        return accountsType;
    }

    public void setAccountsType(String accountsType) {
        this.accountsType = accountsType;
    }

    @JacksonXmlProperty(localName = "CompaniesHouseRegisteredNumber")
    @JsonProperty("companieshouse_registered_number")
    public String getCompaniesHouseRegisteredNumber() {
        return companiesHouseRegisteredNumber;
    }

    public void setCompaniesHouseRegisteredNumber(String companiesHouseRegisteredNumber) {
        this.companiesHouseRegisteredNumber = companiesHouseRegisteredNumber;
    }

    @Override
    public String toString() {
        return "Data [balanceSheetDate=" + balanceSheetDate + ", accountsType=" + accountsType
                + ", companiesHouseRegisteredNumber=" + companiesHouseRegisteredNumber + "]";
    }
}
