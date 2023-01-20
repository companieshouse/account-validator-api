package uk.gov.companieshouse.account.validator.model.validation;

/*
 * Validation data mirrors the type output by the felix validator in the element 'data'.
 * @see https://github.com/companieshouse/felixvalidator/blob/f2e246eff0a914a567c2d43075b997194111411c/src/main/java/uk/gov/ch/felixvalidator/FelixXBRLValidatorServlet.java#L307
 */
public record ValidationData(String balanceSheetDate, String accountsType,
                             String companiesHouseRegisteredNumber) {
}
