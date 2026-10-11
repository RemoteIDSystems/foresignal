package ai.foresignal.store.history;

import ai.foresignal.source.spi.HistoryTier;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Partition sizing and maintenance for {@code record_version}, bound from
 * {@code foresignal.history.*} (Core-Data-Model §9).
 *
 * @param trackBlock seq values per TRACK leaf (about 33 h at 10k versions/min)
 * @param standardBlock seq values per STANDARD leaf (about 17 d at 10k versions/min)
 * @param maintenance the scheduled maintenance run
 */
@ConfigurationProperties("foresignal.history")
public record HistoryProperties(
        @DefaultValue("20000000") long trackBlock,
        @DefaultValue("250000000") long standardBlock,
        @DefaultValue Maintenance maintenance) {

    /** Default TRACK block size. */
    public static final long DEFAULT_TRACK_BLOCK = 20_000_000L;

    /** Default STANDARD block size. */
    public static final long DEFAULT_STANDARD_BLOCK = 250_000_000L;

    /** Validates the block sizes and fills a missing maintenance section. */
    public HistoryProperties {
        if (trackBlock <= 0 || standardBlock <= 0) {
            throw new IllegalArgumentException(
                    "foresignal.history block sizes must be positive, got track-block="
                            + trackBlock
                            + ", standard-block="
                            + standardBlock);
        }
        if (maintenance == null) {
            maintenance = Maintenance.defaults();
        }
    }

    /** The production defaults. */
    public static HistoryProperties defaults() {
        return new HistoryProperties(DEFAULT_TRACK_BLOCK, DEFAULT_STANDARD_BLOCK, Maintenance.defaults());
    }

    /** The block size of {@code tier}. */
    public long block(HistoryTier tier) {
        return tier == HistoryTier.TRACK ? trackBlock : standardBlock;
    }

    /**
     * The scheduled maintenance run: finish pending detaches, then keep coverage ahead.
     *
     * @param enabled false turns off the scheduled run and the daily ANALYZE (the test profile does)
     * @param interval time between runs
     */
    public record Maintenance(
            @DefaultValue("true") boolean enabled, @DefaultValue("10m") Duration interval) {

        /** Fills a missing interval. */
        public Maintenance {
            if (interval == null || interval.isNegative() || interval.isZero()) {
                interval = Duration.ofMinutes(10);
            }
        }

        /** Enabled, every 10 minutes. */
        public static Maintenance defaults() {
            return new Maintenance(true, Duration.ofMinutes(10));
        }
    }
}
