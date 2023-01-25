package uk.gov.companieshouse.account.validator.service.file.transfer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AvStatus {
    INFECTED("infected"),
    CLEAN("clean"),
    NOT_SCANNED("not-scanned");

    final String statusString;

    AvStatus(final String statusString) {
        this.statusString = statusString;
    }

    @JsonCreator
    public static AvStatus create(final String val) {
        AvStatus[] units = AvStatus.values();
        for (AvStatus unit : units) {
            if (unit.getValue().equals(val)) {
                return unit;
            }
        }

        throw new IllegalArgumentException();
    }

    @JsonValue
    public String getValue() {
        return statusString;
    }
}
