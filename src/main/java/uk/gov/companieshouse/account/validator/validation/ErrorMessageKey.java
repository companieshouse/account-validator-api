package uk.gov.companieshouse.account.validator.validation;

/**
 * Validation error message keys
 */
public enum ErrorMessageKey {

    ACCOUNT_ID_REQUIRED("account_id_required"),
    INCORRECT_TOTAL("incorrect_total"),
    INVALID_VALUE("invalid_value"),
    SHAREHOLDERS_FUNDS_MISMATCH("shareholders_funds_mismatch"),
    TRANSACTION_ID_REQUIRED("transaction_id_required"),
    VALUE_OUTSIDE_RANGE("value_outside_range"),
    VALUE_REQUIRED("value_required"),
    FIELD_NOT_REQUIRED("field_not_required"),
    NOTE_REQUIRED("note_required"),
    INCONSISTENT_DATA("inconsistent_data"),
    INVALID_NOTE("invalid_note"),
    MAX_LENGTH_EXCEEDED("max_length_exceeded"),
    INVALID_CHARACTER("invalid_character"),
    INCORRECT_DATE("incorrect_date"),
    INVALID_DATE("invalid_date"),
    INVALID_PERIOD_END_ON_DATE_IN_FUTURE("period_end_on_date_in_future"),
    NOTE_VALUE_GREATER_THAN_BALANCE_SHEET_VALUE("note_value_greater_than_balance_sheet_value"),
    VALUE_NOT_EQUAL_TO_PREVIOUS_PERIOD_ON_BALANCE_SHEET("value_not_equal_to_previous_period_on_balance_sheet"),
    VALUE_NOT_EQUAL_TO_CURRENT_PERIOD_ON_BALANCE_SHEET("value_not_equal_to_current_period_on_balance_sheet"),
    MANDATORY_ELEMENT_MISSING("mandatory_element_missing"),
    MAXIMUM_LIMIT("maximum_limit");

    private String key;

    ErrorMessageKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

}
