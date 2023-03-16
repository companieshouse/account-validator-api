package uk.gov.companieshouse.account.validator.exceptionhandler;

public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 8631969689253132097L;


    public ValidationException() {
        super();
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
