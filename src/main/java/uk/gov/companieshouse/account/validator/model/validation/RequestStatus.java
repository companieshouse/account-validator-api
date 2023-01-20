package uk.gov.companieshouse.account.validator.model.validation;

import java.util.Optional;

/*
 * Request status defines what stage the request is in.
 * @see CompleteStatus
 * @see PendingStatus
 */
public sealed interface RequestStatus permits PendingStatus, CompleteStatus {
    String getStatus();

    Optional<ValidationResult> getResult();
}
