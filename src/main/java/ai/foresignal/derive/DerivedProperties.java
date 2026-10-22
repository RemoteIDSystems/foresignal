package ai.foresignal.derive;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * The DERIVED runtime's settings ({@code foresignal.derive.*}, prompt 052).
 *
 * @param enabled start the runners of enabled derived sources at startup (default true)
 * @param inboxCapacity how many change batches may wait per source before the inbox overflows; on
 *     overflow the batch is dropped and a reconcile is scheduled (frame events are coalesced per
 *     product and never count against it)
 * @param tickResolution how often due ticks are checked (default 1 s)
 * @param ingestTimeout how long an invocation waits for the writer to take and finish its batch
 * @param changeInterval the least time between two change invocations of one source (default 30 s); the
 *     batches that arrive meanwhile are merged into the next invocation. Zero runs every batch at once. A
 *     streaming input such as {@code ais-digitraffic} commits a batch every few seconds; without the
 *     interval each maritime layer ran about 24,000 times a day (2026-10-22)
 */
@ConfigurationProperties("foresignal.derive")
public record DerivedProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("64") int inboxCapacity,
        @DefaultValue("PT1S") Duration tickResolution,
        @DefaultValue("PT60S") Duration ingestTimeout,
        @DefaultValue("PT30S") Duration changeInterval) {

    /** Fills defaults for unset or invalid values. */
    public DerivedProperties {
        inboxCapacity = inboxCapacity < 1 ? 64 : inboxCapacity;
        tickResolution = tickResolution == null || tickResolution.isNegative() || tickResolution.isZero()
                ? Duration.ofSeconds(1) : tickResolution;
        ingestTimeout = ingestTimeout == null || ingestTimeout.isNegative() || ingestTimeout.isZero()
                ? Duration.ofSeconds(60) : ingestTimeout;
        changeInterval = changeInterval == null || changeInterval.isNegative() ? Duration.ofSeconds(30)
                : changeInterval;
    }

    /** The shipped defaults. */
    public static DerivedProperties defaults() {
        return new DerivedProperties(true, 64, Duration.ofSeconds(1), Duration.ofSeconds(60), Duration.ofSeconds(30));
    }
}
