package ai.foresignal.test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * A {@link Clock} tests can move at will.
 *
 * <p>Production code injects the {@code Clock} bean rather than calling {@code Instant.now()}, so
 * importing {@link MutableClock.TestConfig} lets a test place the application at an exact instant
 * and step it forward.
 */
public final class MutableClock extends Clock {

    /** A fixed, readable starting point for tests that do not care about the exact instant. */
    public static final Instant DEFAULT_INSTANT = Instant.parse("2026-01-01T00:00:00Z");

    private final ZoneId zone;
    private volatile Instant instant;

    public MutableClock() {
        this(DEFAULT_INSTANT, ZoneOffset.UTC);
    }

    public MutableClock(Instant instant) {
        this(instant, ZoneOffset.UTC);
    }

    public MutableClock(Instant instant, ZoneId zone) {
        this.instant = instant;
        this.zone = zone;
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId newZone) {
        return newZone.equals(zone) ? this : new MutableClock(instant, newZone);
    }

    @Override
    public Instant instant() {
        return instant;
    }

    /** Moves the clock to an absolute instant, forwards or backwards. */
    public void setInstant(Instant newInstant) {
        this.instant = newInstant;
    }

    /** Moves the clock forward by {@code amount}. A negative amount moves it backwards. */
    public void advance(Duration amount) {
        this.instant = this.instant.plus(amount);
    }

    /** Convenience for {@code advance(Duration.ofSeconds(seconds))}. */
    public void advanceSeconds(long seconds) {
        advance(Duration.ofSeconds(seconds));
    }

    /** Returns the clock to {@link #DEFAULT_INSTANT}. */
    public void reset() {
        this.instant = DEFAULT_INSTANT;
    }

    /**
     * Replaces the application {@code Clock} with a {@link MutableClock}. Import it from a test
     * with {@code @Import(MutableClock.TestConfig.class)} and inject the {@code MutableClock} to
     * control time.
     */
    @TestConfiguration
    public static class TestConfig {

        @Bean
        @Primary
        public MutableClock mutableClock() {
            return new MutableClock();
        }
    }
}
