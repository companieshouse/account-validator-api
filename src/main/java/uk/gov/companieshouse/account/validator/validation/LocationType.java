package uk.gov.companieshouse.account.validator.validation;

/**
 * Validation error location types
 */
public enum LocationType {

    JSON_PATH("json-path"),
    QUERY_PARAMETER("query-parameter");

    private String value;

    LocationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
