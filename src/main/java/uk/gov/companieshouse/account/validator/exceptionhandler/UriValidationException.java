package uk.gov.companieshouse.account.validator.exceptionhandler;

public class UriValidationException extends RuntimeException {

    private static final long serialVersionUID = 8631969689253132097L;


    public UriValidationException() {
        super();
    }

    public UriValidationException(String message) {
        super(message);
    }

    public UriValidationException(Throwable cause) {
        super(cause);
    }

    public UriValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
