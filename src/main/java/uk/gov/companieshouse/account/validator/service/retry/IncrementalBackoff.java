package uk.gov.companieshouse.account.validator.service.retry;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * Will attempt to retry the operation after a short delay. Each unsuccessful retry will increase
 * the delay. This is to prevent "retry storms" where a service is down and other services send a
 * barrage of retry attempts preventing the service from restarting.
 */
public final class IncrementalBackoff implements RetryStrategy {
    private final Duration baseDelay;
    private final Duration delayIncrement;
    private final Duration timeout;
    private final Duration maxDelay;

    public IncrementalBackoff(Duration baseDelay, Duration delayIncrement,
                              Duration timeout,
                              Duration maxDelay) {
        this.baseDelay = baseDelay;
        this.delayIncrement = delayIncrement;
        this.timeout = timeout;
        this.maxDelay = maxDelay;
    }

    public Duration getBaseDelay() {
        return baseDelay;
    }

    public Duration getDelayIncrement() {
        return delayIncrement;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public Duration getMaxDelay() {
        return maxDelay;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (IncrementalBackoff) obj;
        return Objects.equals(this.baseDelay, that.baseDelay) &&
                Objects.equals(this.delayIncrement, that.delayIncrement) &&
                Objects.equals(this.timeout, that.timeout) &&
                Objects.equals(this.maxDelay, that.maxDelay);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseDelay, delayIncrement, timeout, maxDelay);
    }

    @Override
    public String toString() {
        return "IncrementalBackoff[" +
                "baseDelay=" + baseDelay + ", " +
                "delayIncrement=" + delayIncrement + ", " +
                "timeout=" + timeout + ", " +
                "maxDelay=" + maxDelay + ']';
    }

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
