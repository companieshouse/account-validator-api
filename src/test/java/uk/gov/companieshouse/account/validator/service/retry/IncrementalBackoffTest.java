package uk.gov.companieshouse.account.validator.service.retry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.Duration;
import java.util.function.Supplier;

class IncrementalBackoffTest {

    IncrementalBackoff retryStrategy;

    @BeforeEach
    void setUp() {
        retryStrategy = new IncrementalBackoff(Duration.ofSeconds(1),
                Duration.ofSeconds(1),
                Duration.ofSeconds(30),
                Duration.ofSeconds(10));
    }

    @Test
    @DisplayName("Attempt returns value immediately when not retrying")
    void attemptHappyPath() {
        // Given
        Supplier<Integer> fn = () -> 42;

        // When
        int value = retryStrategy.attempt(fn);

        // Then
        assertThat(value, is(equalTo(42)));
    }

    @Test
    @DisplayName("Retries when retry exception is thrown")
    void retry() {
        // Given
        Supplier<Integer> fn = spy(new Supplier<>() {
            private int count = 0;

            @Override
            public Integer get() {
                // Will retry first time and resolve the second.
                if (count >= 1) return 42;
                count += 1;
                throw new RetryException();
            }
        });

        // When
        int value = retryStrategy.attempt(fn);

        // Then
        assertThat(value, is(equalTo(42)));
        verify(fn, times(2)).get();
    }

    @Test
    @DisplayName("Timeout exception is thrown after a ertain time")
    void timeout() {
        // Given
        retryStrategy = new IncrementalBackoff(Duration.ofSeconds(1),
                Duration.ofSeconds(1),
                Duration.ofSeconds(1),
                Duration.ofSeconds(10));

        Supplier<Integer> fn = () -> {
            throw new RetryException();
        };

        // When
        Executable attempt = () -> retryStrategy.attempt(fn);

        // Then
        assertThrows(RuntimeException.class, attempt);
    }

    @Test
    @DisplayName("Interruption exception is handled")
    void interrupted() {
        // Given
        retryStrategy = new IncrementalBackoff(Duration.ofSeconds(1),
                Duration.ofSeconds(10),
                Duration.ofSeconds(30),
                Duration.ofSeconds(20));
        Supplier<Integer> fn = () -> {
            throw new RetryException();
        };

        // When
        Thread.currentThread().interrupt();
        Executable attempt = () -> retryStrategy.attempt(fn);

        // Then
        assertThrows(RuntimeException.class, attempt);
    }
}