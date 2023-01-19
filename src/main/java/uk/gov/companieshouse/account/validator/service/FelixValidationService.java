package uk.gov.companieshouse.account.validator.service;

import uk.gov.companieshouse.account.validator.validation.ixbrl.Results;

import java.io.File;

public interface FelixValidationService {
    Results validate(String iXbrlData, String location);
}

