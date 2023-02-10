package uk.gov.companieshouse.account.validator.exceptionhandler;

public class MissingEnvironmentVariableException extends RuntimeException {

    private static final long serialVersionUID = 8631969689253132097L;
    

    public MissingEnvironmentVariableException() {
        super();
    }
    
    public MissingEnvironmentVariableException(String message) {
            super(message);
    }
    
    public MissingEnvironmentVariableException(Throwable cause) {
            super(cause);
    }
    
    public MissingEnvironmentVariableException(String message, Throwable cause) {
            super(message, cause);
    }
}
