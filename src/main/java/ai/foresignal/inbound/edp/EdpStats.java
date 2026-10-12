package ai.foresignal.inbound.edp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The activity of one provider, stored as {@code external_source.stats} (prompt 024).
 *
 * @param streams per-stream counts, keyed by stream id
 * @param lastValidationErrors the most recent item rejections (at most {@value #MAX_ERRORS})
 * @param status provider-reported health keyed by stream id, {@value #SOURCE_KEY} for the source
 *     and {@value #SNAPSHOT_KEY} for a held snapshot deletion
 */
public record EdpStats(
        Map<String, StreamStats> streams, List<EdpItemRejection> lastValidationErrors, Map<String, StatusReport> status) {

    /** How many rejections are kept. */
    public static final int MAX_ERRORS = 20;

    /** The status key of source-level reports. */
    public static final String SOURCE_KEY = "_source";

    /** The status key of a held snapshot deletion. */
    public static final String SNAPSHOT_KEY = "_snapshot";

    /** Replaces nulls with empty collections. */
    public EdpStats {
        streams = streams == null ? new LinkedHashMap<>() : new LinkedHashMap<>(streams);
        lastValidationErrors = lastValidationErrors == null ? new ArrayList<>() : new ArrayList<>(lastValidationErrors);
        status = status == null ? new LinkedHashMap<>() : new LinkedHashMap<>(status);
    }

    /** No activity yet. */
    public static EdpStats empty() {
        return new EdpStats(null, null, null);
    }

    /**
     * One stream's counts.
     *
     * @param items items accepted
     * @param rejected items rejected
     * @param lastItemAt when an item was last accepted, or null
     */
    public record StreamStats(long items, long rejected, Instant lastItemAt) {}

    /**
     * One provider-reported health.
     *
     * @param health {@code ok}, {@code degraded}, {@code blocked}, {@code layout-changed} or
     *     {@code geo-restricted}
     * @param message the provider's message, or null
     * @param at when it was reported
     * @param lastUnhealthyAt when anything but {@code ok} was last reported, or null
     */
    public record StatusReport(String health, String message, Instant at, Instant lastUnhealthyAt) {

        /** True when the report is anything but {@code ok}. */
        public boolean unhealthy() {
            return health != null && !"ok".equals(health);
        }
    }
}
