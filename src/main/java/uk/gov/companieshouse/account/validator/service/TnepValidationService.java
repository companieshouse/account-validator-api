package uk.gov.companieshouse.account.validator.service;

public interface TnepValidationService {

    boolean validate(String data, String location);

}

