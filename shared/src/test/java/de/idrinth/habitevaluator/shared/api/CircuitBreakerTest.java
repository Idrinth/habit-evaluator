package de.idrinth.habitevaluator.shared.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class CircuitBreakerTest {

    private static final long COOLDOWN_MS = 5 * 60 * 1000; // 5 minutes

    private Clock fixedClock(long epochMillis) {
        return Clock.fixed(Instant.ofEpochMilli(epochMillis), ZoneId.of("UTC"));
    }

    @Test
    void testInitialStateIsClosed() {
        CircuitBreaker cb = new CircuitBreaker(COOLDOWN_MS, fixedClock(1000));
        assertFalse(cb.isOpen());
    }

    @Test
    void testCheckStateDoesNotThrowWhenClosed() {
        CircuitBreaker cb = new CircuitBreaker(COOLDOWN_MS, fixedClock(1000));
        assertDoesNotThrow(cb::checkState);
    }

    @Test
    void testOpensAfterFailure() {
        CircuitBreaker cb = new CircuitBreaker(COOLDOWN_MS, fixedClock(1000));
        cb.recordFailure();
        assertTrue(cb.isOpen());
    }

    @Test
    void testCheckStateThrowsWhenOpen() {
        CircuitBreaker cb = new CircuitBreaker(COOLDOWN_MS, fixedClock(1000));
        cb.recordFailure();
        IOException ex = assertThrows(IOException.class, cb::checkState);
        assertTrue(ex.getMessage().contains("Circuit breaker open"));
    }

    @Test
    void testClosesAfterSuccess() {
        CircuitBreaker cb = new CircuitBreaker(COOLDOWN_MS, fixedClock(1000));
        cb.recordFailure();
        assertTrue(cb.isOpen());
        cb.recordSuccess();
        assertFalse(cb.isOpen());
    }

    @Test
    void testClosesAfterCooldownExpires() {
        long failureTime = 1000;
        // Create breaker at failure time
        CircuitBreaker cb = new CircuitBreaker(COOLDOWN_MS, fixedClock(failureTime));
        cb.recordFailure();
        assertTrue(cb.isOpen());

        // Advance clock past cooldown - create new breaker instance with same state via a mutable clock
        // Instead, use a MutableClock approach
        MutableClock clock = new MutableClock(failureTime);
        CircuitBreaker cb2 = new CircuitBreaker(COOLDOWN_MS, clock);
        cb2.recordFailure();
        assertTrue(cb2.isOpen());

        // Advance just before cooldown
        clock.setMillis(failureTime + COOLDOWN_MS - 1);
        assertTrue(cb2.isOpen());

        // Advance to exactly cooldown
        clock.setMillis(failureTime + COOLDOWN_MS);
        assertFalse(cb2.isOpen());
    }

    @Test
    void testCheckStateAllowsRequestAfterCooldown() {
        MutableClock clock = new MutableClock(1000);
        CircuitBreaker cb = new CircuitBreaker(COOLDOWN_MS, clock);

        cb.recordFailure();
        assertThrows(IOException.class, cb::checkState);

        // Advance past cooldown
        clock.setMillis(1000 + COOLDOWN_MS);
        assertDoesNotThrow(cb::checkState);
    }

    @Test
    void testFailureAfterCooldownReopensCircuit() {
        MutableClock clock = new MutableClock(1000);
        CircuitBreaker cb = new CircuitBreaker(COOLDOWN_MS, clock);

        cb.recordFailure();
        assertTrue(cb.isOpen());

        // Advance past cooldown
        clock.setMillis(1000 + COOLDOWN_MS);
        assertFalse(cb.isOpen());

        // Another failure reopens
        cb.recordFailure();
        assertTrue(cb.isOpen());
    }

    @Test
    void testSuccessWithoutPriorFailureKeepsClosed() {
        CircuitBreaker cb = new CircuitBreaker(COOLDOWN_MS, fixedClock(1000));
        cb.recordSuccess();
        assertFalse(cb.isOpen());
    }

    @Test
    void testMultipleFailuresKeepCircuitOpen() {
        MutableClock clock = new MutableClock(1000);
        CircuitBreaker cb = new CircuitBreaker(COOLDOWN_MS, clock);

        cb.recordFailure();
        assertTrue(cb.isOpen());

        // Second failure resets the timer
        clock.setMillis(1000 + 60_000); // 1 minute later
        cb.recordFailure();

        // Should still be open relative to the second failure
        clock.setMillis(1000 + COOLDOWN_MS);
        assertTrue(cb.isOpen()); // Open because second failure was at 61000

        clock.setMillis(1000 + 60_000 + COOLDOWN_MS);
        assertFalse(cb.isOpen());
    }

    /**
     * Mutable clock for testing time-dependent circuit breaker behavior.
     */
    private static class MutableClock extends Clock {
        private long millis;

        MutableClock(long millis) {
            this.millis = millis;
        }

        void setMillis(long millis) {
            this.millis = millis;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(millis);
        }

        @Override
        public long millis() {
            return millis;
        }
    }
}
