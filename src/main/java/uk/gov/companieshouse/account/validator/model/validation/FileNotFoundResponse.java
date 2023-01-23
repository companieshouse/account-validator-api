package uk.gov.companieshouse.account.validator.model.validation;

public record FileNotFoundResponse(String id) implements ValidationResponse {
    private static final String MESSAGE_FORMAT = "File with id '%s' not found.";

    public String getMessage() {
        return String.format(MESSAGE_FORMAT, id);
    }
}
