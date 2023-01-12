package uk.gov.companieshouse.account.validator.exception;

import uk.gov.companieshouse.account.validator.model.Error;

public class DateFormatException extends RuntimeException {

    private Error error;

    public DateFormatException(Error error) {
        this.error = error;
    }

    public Error getError() {
        return error;
    }

}
