package uk.gov.companieshouse.account.validator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;

import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * An interface to be implemented by domain objects which can be pruned
 */
public interface Prunable {

    /**
     * Determine whether the object is empty
     *
     * @return True or false
     */
    @JsonIgnore
    boolean isEmpty();

    /**
     * Prune the domain object
     */
    default void prune() {}

    /**
     * Prune the given {@link T} if possible
     *
     * @param t
     */
    default <T> T prune(T t) {
        if (t instanceof String) {
            String string = (String)t;
            if (isBlank(string)) {
                return null;
            }
        } else if (t instanceof Collection) {
            Collection collection = (Collection)t;
            if (collection.isEmpty()) {
                return null;
            }
        } else if (t instanceof Prunable) {
            Prunable prunable = (Prunable)t;
            if (prunable.isEmpty()) {
                return null;
            } else {
                prunable.prune();
            }
        }
        return t;
    }

}
