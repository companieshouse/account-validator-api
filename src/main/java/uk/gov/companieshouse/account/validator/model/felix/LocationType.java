package uk.gov.companieshouse.account.validator.model.felix;

/**
 * Validation error location types
 */
public enum LocationType {

    JSON_PATH("json-path"),
    QUERY_PARAMETER("query-parameter");

    private final String value;

    LocationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}