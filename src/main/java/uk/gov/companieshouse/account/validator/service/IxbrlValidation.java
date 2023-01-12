package uk.gov.companieshouse.account.validator.service;


public interface IxbrlValidation {

    boolean validate(String ixbrl, String location);
}
