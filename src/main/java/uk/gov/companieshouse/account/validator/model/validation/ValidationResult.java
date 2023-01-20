package uk.gov.companieshouse.account.validator.model.validation;

import java.util.Set;

/**
 * This type mirrors the type output by the felix XBRL validator.
 *
 * @see <a href="https://github.com/companieshouse/felixvalidator/blob/f2e246eff0a914a567c2d43075b997194111411c/src/main/java/uk/gov/ch/felixvalidator/FelixXBRLValidatorServlet.java#L284">FelixXBRLValidatorServlet#buildResponseXML</a>
 */
public record ValidationResult(Set<String> errorMessages, ValidationData data,
                               ValidationStatus validationStatus) {

}