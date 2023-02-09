package uk.gov.companieshouse.account.validator.model.validation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/*
 * Validation data mirrors the type output by the felix validator in the element 'data'.
 * @see https://github.com/companieshouse/felixvalidator/blob/f2e246eff0a914a567c2d43075b997194111411c/src/main/java/uk/gov/ch/felixvalidator/FelixXBRLValidatorServlet.java#L307
 */
public final class ValidationData {
    @JsonProperty("balance_sheet_date")
    private final String balanceSheetDate;

    @JsonProperty("accounts_type")
    private final String accountsType;

    @JsonProperty("companieshouse_registered_number")
    private final String companiesHouseRegisteredNumber;

    public ValidationData(String balanceSheetDate, String accountsType, String companiesHouseRegisteredNumber) {
        this.balanceSheetDate = balanceSheetDate;
        this.accountsType = accountsType;
        this.companiesHouseRegisteredNumber = companiesHouseRegisteredNumber;
    }

    public String getBalanceSheetDate() {
        return balanceSheetDate;
    }

    public String getAccountsType() {
        return accountsType;
    }

    public String getCompaniesHouseRegisteredNumber() {
        return companiesHouseRegisteredNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationData that = (ValidationData) o;
        return Objects.equals(balanceSheetDate, that.balanceSheetDate) && Objects.equals(accountsType, that.accountsType) && Objects.equals(companiesHouseRegisteredNumber, that.companiesHouseRegisteredNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(balanceSheetDate, accountsType, companiesHouseRegisteredNumber);
    }

    @Override
    public String toString() {
        return "ValidationData{" +
                "balanceSheetDate='" + balanceSheetDate + '\'' +
                ", accountsType='" + accountsType + '\'' +
                ", companiesHouseRegisteredNumber='" + companiesHouseRegisteredNumber + '\'' +
                '}';
    }
}
