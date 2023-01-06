package uk.gov.companieshouse.account.validator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import uk.gov.companieshouse.account.validator.annotation.JsonPath;
import uk.gov.companieshouse.account.validator.exception.DateFormatException;
import uk.gov.companieshouse.account.validator.validation.ErrorMessageKey;
import uk.gov.companieshouse.account.validator.validation.ErrorType;
import uk.gov.companieshouse.account.validator.validation.LocationType;
import uk.gov.companieshouse.account.validator.model.Error;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A {@link JsonDateDeserializer} for {@link Date}s
 */
public class JsonDateDeserializer extends JsonDeserializer<Date> {

    private static final DateFormat DATE_FORMAT ;

    static {
        DATE_FORMAT  = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        DATE_FORMAT.setLenient(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        String date = parser.getText();

        try {
            return DATE_FORMAT.parse(date);
        } catch (ParseException e) {
            Object object = parser.getCurrentValue();
            Class clazz = object.getClass();
            StringBuilder jsonPath = new StringBuilder();
            String objectJsonPath = getJsonPath(clazz);
            if (objectJsonPath != null) {
                jsonPath.append(objectJsonPath).append(".").append(parser.getCurrentName());
            }
            Error error = new Error(ErrorMessageKey.INVALID_DATE.getKey(), jsonPath.toString(), LocationType.JSON_PATH.getValue(), ErrorType.VALIDATION.getType());
            throw new DateFormatException(error);
        }
    }

    /**
     * Get the json path from the given {@link Class}
     *
     * @param objectClass
     * @return A {@link String} or null
     */
    private String getJsonPath(Class objectClass) {
        if (objectClass.isAnnotationPresent(JsonPath.class)) {
            Annotation annotation = objectClass.getAnnotation(JsonPath.class);
            JsonPath jsonPath = (JsonPath) annotation;
            return jsonPath.path();
        }
        return null;
    }

}
