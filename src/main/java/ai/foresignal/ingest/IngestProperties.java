package ai.foresignal.ingest;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Ingest writer settings, bound from {@code foresignal.ingest.*} (Core-Data-Model §6).
 *
 * @param queueCapacity how many commands may wait for the writer before producers back off
 * @param writer the writer thread's settings
 */
@ConfigurationProperties("foresignal.ingest")
public record IngestProperties(@DefaultValue("64") int queueCapacity, @DefaultValue Writer writer) {

    /** Validates the capacity and fills a missing writer section. */
    public IngestProperties {
        if (queueCapacity < 1) {
            throw new IllegalArgumentException("foresignal.ingest.queue-capacity must be positive, got " + queueCapacity);
        }
        if (writer == null) {
            writer = Writer.defaults();
        }
    }

    /** The production defaults. */
    public static IngestProperties defaults() {
        return new IngestProperties(64, Writer.defaults());
    }

    /**
     * The writer thread.
     *
     * @param exclusive hold the single-writer advisory lock on a dedicated connection (invariant
     *     I1); true in production, false in tests, where several cached contexts share a database
     * @param lockTimeout {@code SET LOCAL lock_timeout} for each batch transaction
     * @param keepalive how often the lock connection is checked with {@code SELECT 1}
     * @param lockRetryMax the ceiling of the reacquire backoff, which starts at 1 s and doubles
     * @param clockTolerance how far the clock may be behind the last ingest time before batches are
     *     held, and how far wall-clock and monotonic time may diverge before a jump is counted
     * @param clockRecheck how often a held writer re-checks the clock
     * @param shutdownDrain how long shutdown waits for the queue to drain
     * @param retryBackoff the waits before each retry of a batch that failed transiently; its size
     *     is the number of retries
     */
    public record Writer(
            @DefaultValue("true") boolean exclusive,
            @DefaultValue("10s") Duration lockTimeout,
            @DefaultValue("30s") Duration keepalive,
            @DefaultValue("30s") Duration lockRetryMax,
            @DefaultValue("5m") Duration clockTolerance,
            @DefaultValue("30s") Duration clockRecheck,
            @DefaultValue("30s") Duration shutdownDrain,
            @DefaultValue({"500ms", "1s", "2s"}) List<Duration> retryBackoff) {

        /** Fills missing values with the defaults. */
        public Writer {
            lockTimeout = lockTimeout == null ? Duration.ofSeconds(10) : lockTimeout;
            keepalive = keepalive == null ? Duration.ofSeconds(30) : keepalive;
            lockRetryMax = lockRetryMax == null ? Duration.ofSeconds(30) : lockRetryMax;
            clockTolerance = clockTolerance == null ? Duration.ofMinutes(5) : clockTolerance;
            clockRecheck = clockRecheck == null ? Duration.ofSeconds(30) : clockRecheck;
            shutdownDrain = shutdownDrain == null ? Duration.ofSeconds(30) : shutdownDrain;
            retryBackoff = retryBackoff == null ? defaultBackoff() : List.copyOf(retryBackoff);
        }

        /** The production defaults. */
        public static Writer defaults() {
            return new Writer(
                    true,
                    Duration.ofSeconds(10),
                    Duration.ofSeconds(30),
                    Duration.ofSeconds(30),
                    Duration.ofMinutes(5),
                    Duration.ofSeconds(30),
                    Duration.ofSeconds(30),
                    defaultBackoff());
        }

        /** A copy with a different {@code exclusive} flag. */
        public Writer withExclusive(boolean value) {
            return new Writer(value, lockTimeout, keepalive, lockRetryMax, clockTolerance, clockRecheck, shutdownDrain, retryBackoff);
        }

        /** A copy with a different {@code clockRecheck}. */
        public Writer withClockRecheck(Duration value) {
            return new Writer(exclusive, lockTimeout, keepalive, lockRetryMax, clockTolerance, value, shutdownDrain, retryBackoff);
        }

        private static List<Duration> defaultBackoff() {
            return List.of(Duration.ofMillis(500), Duration.ofSeconds(1), Duration.ofSeconds(2));
        }
    }
}
