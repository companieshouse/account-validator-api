package uk.gov.companieshouse.account.validator.validation.ixbrl;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "data")
public class Data {

    public Data(){}
    @XmlElement(name = "BalanceSheetDate")
    private String balanceSheetDate;

    @XmlElement(name = "AccountsType")
    private String accountsType;

    @XmlElement(name = "CompaniesHouseRegisteredNumber")
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
