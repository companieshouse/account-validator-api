package uk.gov.companieshouse.account.validator.exception;

public class MissingEnvironmentVariableException extends RuntimeException {

    private static final long serialVersionUID = 8631969689253132097L;

    public MissingEnvironmentVariableException(String message) {
            super(message);
    }
}
