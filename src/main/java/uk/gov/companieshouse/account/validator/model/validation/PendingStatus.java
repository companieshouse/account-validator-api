package uk.gov.companieshouse.account.validator.model.validation;

import java.util.Optional;

/*
 * Pending status represents a request that has been submitted for validation but has not yet completed
 */
public record PendingStatus() implements RequestStatus {

    public static final String STATUS_PENDING = "PENDING";

    @Override
    public String getStatus() {
        return STATUS_PENDING;
    }

    @Override
    public Optional<ValidationResult> getResult() {
        return Optional.empty();
    }
}
