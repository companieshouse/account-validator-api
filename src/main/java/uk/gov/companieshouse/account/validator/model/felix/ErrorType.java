package uk.gov.companieshouse.account.validator.model.felix;

/**
 * Validation error types
 */
public enum ErrorType {

    SERVICE("ch:service"),
    VALIDATION("ch:validation");

    private final String type;

    ErrorType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
