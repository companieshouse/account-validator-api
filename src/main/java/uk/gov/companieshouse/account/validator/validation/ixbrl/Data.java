package uk.gov.companieshouse.account.validator.validation.ixbrl;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "data")
public class Data {

    public Data(){}
   private String balanceSheetDate;

    private String accountsType;

    private String companiesHouseRegisteredNumber;

    public String getBalanceSheetDate() {
        return balanceSheetDate;
    }

    @XmlElement(name = "BalanceSheetDate")
    public void setBalanceSheetDate(String balanceSheetDate) {
        this.balanceSheetDate = balanceSheetDate;
    }

    public String getAccountsType() {
        return accountsType;
    }

    @XmlElement(name = "AccountsType")
    public void setAccountsType(String accountsType) {
        this.accountsType = accountsType;
    }

    public String getCompaniesHouseRegisteredNumber() {
        return companiesHouseRegisteredNumber;
    }

    @XmlElement(name = "CompaniesHouseRegisteredNumber")
    public void setCompaniesHouseRegisteredNumber(String companiesHouseRegisteredNumber) {
        this.companiesHouseRegisteredNumber = companiesHouseRegisteredNumber;
    }
}
