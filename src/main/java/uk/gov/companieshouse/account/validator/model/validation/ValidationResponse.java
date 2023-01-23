package uk.gov.companieshouse.account.validator.model.validation;

public sealed interface ValidationResponse permits SuccessResponse, FileNotFoundResponse {
}
