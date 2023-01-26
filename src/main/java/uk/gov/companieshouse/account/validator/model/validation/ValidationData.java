package uk.gov.companieshouse.account.validator.model.validation;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * Validation data mirrors the type output by the felix validator in the element 'data'.
 * @see https://github.com/companieshouse/felixvalidator/blob/f2e246eff0a914a567c2d43075b997194111411c/src/main/java/uk/gov/ch/felixvalidator/FelixXBRLValidatorServlet.java#L307
 */
public record ValidationData(@JsonProperty("balance_sheet_date") String balanceSheetDate,
                             @JsonProperty("accounts_type") String accountsType,
                             @JsonProperty("companieshouse_registered_number") String companiesHouseRegisteredNumber) {
}
