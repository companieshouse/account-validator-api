package uk.gov.companieshouse.account.validator.model.validation;

import org.springframework.http.ResponseEntity;

public final class ValidationResponse {
    public static ResponseEntity<?> fileNotFound() {
        return ResponseEntity.notFound().build();
    }

    public static ResponseEntity<?> requestNotFound() {
        return ResponseEntity.notFound().build();
    }

    public static ResponseEntity<RequestStatus> success(RequestStatus requestStatus) {
        return ResponseEntity.ok(requestStatus);
    }
}
