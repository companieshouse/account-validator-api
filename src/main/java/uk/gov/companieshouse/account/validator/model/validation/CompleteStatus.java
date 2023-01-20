package uk.gov.companieshouse.account.validator.model.validation;

import java.util.Optional;

/*
 * Complete status represents a request that has completed it's validation
 */
public record CompleteStatus(
        ValidationResult result) implements RequestStatus {
    public static final String STATUS_COMPLETE = "COMPLETE";

    @Override
    public String getStatus() {
        return STATUS_COMPLETE;
    }

    @Override
    public Optional<ValidationResult> getResult() {
        return Optional.ofNullable(result);
    }
}
