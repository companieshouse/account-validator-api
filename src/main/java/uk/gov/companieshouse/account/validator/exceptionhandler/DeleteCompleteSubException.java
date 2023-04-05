package uk.gov.companieshouse.account.validator.exceptionhandler;

public class DeleteCompleteSubException extends RuntimeException {

    private static final long serialVersionUID = 8631969689253132097L;


    public DeleteCompleteSubException() {
        super();
    }

    public DeleteCompleteSubException(String message) {
        super(message);
    }

    public DeleteCompleteSubException(Throwable cause) {
        super(cause);
    }

    public DeleteCompleteSubException(String message, Throwable cause) {
        super(message, cause);
    }
}
