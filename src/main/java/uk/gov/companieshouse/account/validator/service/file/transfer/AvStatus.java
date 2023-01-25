package uk.gov.companieshouse.account.validator.service.file.transfer;

public enum AvStatus {
    INFECTED("infected"),
    CLEAN("clean"),
    NOT_SCANNED("not-scanned");

    final String statusString;

    AvStatus(String statusString) {
        this.statusString = statusString;
    }
}
