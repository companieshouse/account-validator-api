package uk.gov.companieshouse.account.validator.service;

import java.io.File;

public interface FelixValidationService {

    boolean validate(String iXbrlData, String location);
}

