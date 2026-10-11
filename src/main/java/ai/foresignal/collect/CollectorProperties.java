package ai.foresignal.collect;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Collector settings, bound from {@code foresignal.collector.*} (Architecture §5.1, §5.5;
 * Core-Data-Model §7).
 *
 * @param scheduled whether the scheduler and the streaming supervisor start with the application;
 *     false in the test profile, where several cached contexts share one database and an enabled
 *     fake source must not start fetching behind another test's back
 * @param reconcileInterval how often enabled sources are compared with the running set
 * @param shutdownWait how long shutdown waits for in-flight fetches and the final stream flushes
 * @param ingestGrace added to the remaining fetch time to get the ingest submit and await timeout
 * @param stream the streaming sink and reconnect settings
 */
@ConfigurationProperties("foresignal.collector")
public record CollectorProperties(
        @DefaultValue("true") boolean scheduled,
        @DefaultValue("2s") Duration reconcileInterval,
        @DefaultValue("30s") Duration shutdownWait,
        @DefaultValue("30s") Duration ingestGrace,
        @DefaultValue Stream stream) {

    /** Fills missing values with the defaults. */
    public CollectorProperties {
        reconcileInterval = reconcileInterval == null ? Duration.ofSeconds(2) : reconcileInterval;
        shutdownWait = shutdownWait == null ? Duration.ofSeconds(30) : shutdownWait;
        ingestGrace = ingestGrace == null ? Duration.ofSeconds(30) : ingestGrace;
        stream = stream == null ? Stream.defaults() : stream;
    }

    /** The production defaults. */
    public static CollectorProperties defaults() {
        return new CollectorProperties(
                true, Duration.ofSeconds(2), Duration.ofSeconds(30), Duration.ofSeconds(30), Stream.defaults());
    }

    /**
     * Streaming settings.
     *
     * @param flushInterval the longest a buffered record waits before a flush is due
     * @param flushMaxEntries buffered entries that make a flush due immediately
     * @param allBufferMax the cap on an ALL source's buffer, at which {@code accept} blocks
     * @param acceptBlockTimeout how long {@code accept} blocks at the cap before dropping the record
     * @param submitTimeout how long a flush waits for room in the ingest queue before keeping the
     *     records buffered and trying again
     * @param reconnectLadder the waits before successive reconnect attempts; the last repeats
     * @param ladderResetAfter how long a connection must hold before the ladder starts again
     * @param credentialsRetry the wait after the upstream rejected the credentials
     */
    public record Stream(
            @DefaultValue("2s") Duration flushInterval,
            @DefaultValue("2000") int flushMaxEntries,
            @DefaultValue("50000") int allBufferMax,
            @DefaultValue("30s") Duration acceptBlockTimeout,
            @DefaultValue("5s") Duration submitTimeout,
            @DefaultValue({"5s", "15s", "60s", "300s", "900s"}) List<Duration> reconnectLadder,
            @DefaultValue("10m") Duration ladderResetAfter,
            @DefaultValue("1h") Duration credentialsRetry) {

        /** Validates the sizes and fills missing values with the defaults. */
        public Stream {
            flushInterval = flushInterval == null ? Duration.ofSeconds(2) : flushInterval;
            if (flushMaxEntries < 1) {
                throw new IllegalArgumentException("foresignal.collector.stream.flush-max-entries must be positive");
            }
            if (allBufferMax < 1) {
                throw new IllegalArgumentException("foresignal.collector.stream.all-buffer-max must be positive");
            }
            acceptBlockTimeout = acceptBlockTimeout == null ? Duration.ofSeconds(30) : acceptBlockTimeout;
            submitTimeout = submitTimeout == null ? Duration.ofSeconds(5) : submitTimeout;
            reconnectLadder =
                    reconnectLadder == null || reconnectLadder.isEmpty()
                            ? defaultLadder()
                            : List.copyOf(reconnectLadder);
            ladderResetAfter = ladderResetAfter == null ? Duration.ofMinutes(10) : ladderResetAfter;
            credentialsRetry = credentialsRetry == null ? Duration.ofHours(1) : credentialsRetry;
        }

        /** The production defaults. */
        public static Stream defaults() {
            return new Stream(
                    Duration.ofSeconds(2),
                    2000,
                    50_000,
                    Duration.ofSeconds(30),
                    Duration.ofSeconds(5),
                    defaultLadder(),
                    Duration.ofMinutes(10),
                    Duration.ofHours(1));
        }

        /** The wait before reconnect attempt {@code attempt} (0-based); the last step repeats. */
        public Duration reconnectDelay(int attempt) {
            int index = Math.max(0, Math.min(attempt, reconnectLadder.size() - 1));
            return reconnectLadder.get(index);
        }

        private static List<Duration> defaultLadder() {
            return List.of(
                    Duration.ofSeconds(5),
                    Duration.ofSeconds(15),
                    Duration.ofSeconds(60),
                    Duration.ofSeconds(300),
                    Duration.ofSeconds(900));
        }
    }
}
