package uk.gov.companieshouse.account.validator.validation;

/**
 * Validation error types
 */
public enum ErrorType {

    SERVICE("ch:service"),
    VALIDATION("ch:validation");

    private String type;

    ErrorType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
