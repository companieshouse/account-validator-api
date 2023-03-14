package uk.gov.companieshouse.account.validator.exceptionhandler;

public class ResponseException extends RuntimeException {

    private static final long serialVersionUID = 8631969689253132097L;


    public ResponseException() {
        super();
    }

    public ResponseException(String message) {
        super(message);
    }

    public ResponseException(Throwable cause) {
        super(cause);
    }

    public ResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
