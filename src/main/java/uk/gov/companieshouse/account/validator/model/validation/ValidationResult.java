package uk.gov.companieshouse.account.validator.model.validation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Set;

/**
 * This type mirrors the type output by the felix XBRL validator.
 *
 * @see <a href="https://github.com/companieshouse/felixvalidator/blob/f2e246eff0a914a567c2d43075b997194111411c/src/main/java/uk/gov/ch/felixvalidator/FelixXBRLValidatorServlet.java#L284">FelixXBRLValidatorServlet#buildResponseXML</a>
 */
public final class ValidationResult {
//    @JsonProperty("error_messages")
    private final Set<String> errorMessages;

//    @JsonProperty("data")
    private final ValidationData data;

//    @JsonProperty("validation_status")
    private final ValidationStatus validationStatus;

    public ValidationResult(Set<String> errorMessages, ValidationData data, ValidationStatus validationStatus) {
        this.errorMessages = errorMessages;
        this.data = data;
        this.validationStatus = validationStatus;
    }

    public Set<String> getErrorMessages() {
        return errorMessages;
    }

    public ValidationData getData() {
        return data;
    }

    public ValidationStatus getValidationStatus() {
        return validationStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationResult that = (ValidationResult) o;
        return Objects.equals(errorMessages, that.errorMessages) && Objects.equals(data, that.data) && validationStatus == that.validationStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorMessages, data, validationStatus);
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "errorMessages=" + errorMessages +
                ", data=" + data +
                ", validationStatus=" + validationStatus +
                '}';
    }
}
