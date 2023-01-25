package uk.gov.companieshouse.account.validator.service.retry;

import java.util.function.Supplier;

/**
 * Allows for an operation to be retried in a manner that can be controlled by different strategies
 */
public interface RetryStrategy {
    /**
     * Attempts to run the given function. If it returns a value without throwing an exception, that
     * value is returned. Otherwise, if the function throws a RetryException, the strategy will attempt
     * a retry.
     *
     * @param func a function wrapping the operation to be retried
     * @param <T>  The return type of the function
     * @return The value returned by the function on success
     */
    <T> T attempt(Supplier<T> func);
}
