package uk.gov.companieshouse.account.validator.service;

import org.springframework.core.serializer.Serializer;

import java.io.File;

public interface FelixValidationService extends Serializer {

    boolean validate(String iXbrlData, String location);
}

