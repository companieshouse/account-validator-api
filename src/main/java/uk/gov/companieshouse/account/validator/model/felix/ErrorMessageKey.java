package uk.gov.companieshouse.account.validator.model.felix;

/**
 * Validation error message keys
 */
public enum ErrorMessageKey {

    VALUE_OUTSIDE_RANGE("value_outside_range"),
    VALUE_REQUIRED("value_required"),
    INCONSISTENT_DATA("inconsistent_data"),
    INVALID_NOTE("invalid_note"),
    MAX_LENGTH_EXCEEDED("max_length_exceeded"),
    INVALID_CHARACTER("invalid_character"),
    INCORRECT_DATE("incorrect_date"),
    INVALID_DATE("invalid_date"),
    MANDATORY_ELEMENT_MISSING("mandatory_element_missing"),
    MAXIMUM_LIMIT("maximum_limit");

    private final String key;

    ErrorMessageKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

}
