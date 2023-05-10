package uk.gov.companieshouse.account.validator.exceptionhandler;

public class XBRLValidationException extends Exception {
    public XBRLValidationException() {
    }

    public XBRLValidationException(String message) {
        super(message);
    }

    public XBRLValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public XBRLValidationException(Throwable cause) {
        super(cause);
    }

    public XBRLValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
