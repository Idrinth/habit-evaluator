package de.idrinth.habitevaluator.shared.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;

/**
 * Simple circuit breaker that stops requests for a cooldown period after a failure.
 * After the cooldown elapses, the next request is allowed through as a probe.
 * If it succeeds the circuit closes; if it fails the cooldown resets.
 */
public class CircuitBreaker {

    private static final Logger logger = LoggerFactory.getLogger(CircuitBreaker.class);

    private final long cooldownMillis;
    private final Clock clock;
    private volatile Instant lastFailureTime;

    public CircuitBreaker(long cooldownMillis) {
        this(cooldownMillis, Clock.systemUTC());
    }

    CircuitBreaker(long cooldownMillis, Clock clock) {
        this.cooldownMillis = cooldownMillis;
        this.clock = clock;
    }

    /**
     * Returns true if requests are currently blocked due to a recent failure.
     */
    public boolean isOpen() {
        if (lastFailureTime == null) {
            return false;
        }
        long elapsed = clock.millis() - lastFailureTime.toEpochMilli();
        return elapsed < cooldownMillis;
    }

    /**
     * Checks whether a request is allowed. Throws IOException if the circuit is open.
     */
    public void checkState() throws IOException {
        if (isOpen()) {
            long remaining = cooldownMillis - (clock.millis() - lastFailureTime.toEpochMilli());
            logger.warn("Circuit breaker open, blocking request for {}ms more", remaining);
            throw new IOException("Circuit breaker open: requests paused after failure, retrying in " + remaining + "ms");
        }
    }

    /**
     * Records a successful request, closing the circuit.
     */
    public void recordSuccess() {
        if (lastFailureTime != null) {
            logger.info("Circuit breaker closed after successful request");
        }
        lastFailureTime = null;
    }

    /**
     * Records a failed request, opening the circuit for the cooldown period.
     */
    public void recordFailure() {
        lastFailureTime = Instant.now(clock);
        logger.warn("Circuit breaker opened, pausing requests for {}ms", cooldownMillis);
    }
}
