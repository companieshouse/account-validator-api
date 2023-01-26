package uk.gov.companieshouse.account.validator.service.retry;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * Will attempt to retry the operation after a short delay. Each unsuccessful retry will increase
 * the delay. This is to prevent "retry storms" where a service is down and other services send a
 * barrage of retry attempts preventing the service from restarting.
 */
public record IncrementalBackoff(Duration baseDelay, Duration delayIncrement,
                                 Duration timeout,
                                 Duration maxDelay) implements RetryStrategy {

    @Override
    public <T> T attempt(Supplier<T> func) {
        Duration delay = baseDelay;
        Instant timeoutInstant = Instant.now().plusMillis(timeout.toMillis());

        while (true) {
            try {
                return func.get();
            } catch (RetryException e) {
                // See if sleeping again will exceed the timeout. If so, re-throw the exception
                if (Instant.now().plusMillis(delay.toMillis()).isAfter(timeoutInstant)) {
                    throw new RuntimeException(new TimeoutException());
                }

                try {
                    //noinspection BusyWait
                    Thread.sleep(delay.toMillis());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted");
                }
                delay = delay.plus(delayIncrement);
                delay = Duration.ofMillis(Math.min(delay.toMillis(), maxDelay.toMillis()));
            }
        }
    }
}
