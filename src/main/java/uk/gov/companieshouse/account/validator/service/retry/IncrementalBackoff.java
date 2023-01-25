package uk.gov.companieshouse.account.validator.service.retry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

@Component
public record IncrementalBackoff(Duration baseDelay, Duration delayIncrement,
                                 Duration timeout,
                                 Duration maxDelay) implements RetryStrategy {
    @Autowired
    public IncrementalBackoff {
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
