package ai.foresignal.source.adapters.env;

import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/** Shared helpers of the prompt 078 tests: the capture time, record lookup and re-timed snapshots. */
public final class EnvTestSupport {

    /** Just after the 2026-10-13 captures. */
    public static final Instant NOW = Instant.parse("2026-10-13T21:35:00Z");

    private EnvTestSupport() {}

    /** The record with {@code externalId}. */
    public static NormalizedRecord byId(List<NormalizedRecord> records, String externalId) {
        return records.stream().filter(r -> r.externalId().equals(externalId)).findFirst().orElseThrow();
    }

    /**
     * {@code record} observed {@code later} after its own time, with the same payload and geometry: a second
     * snapshot of unchanged content for {@code SnapshotContract}.
     */
    public static NormalizedRecord later(NormalizedRecord record, Duration later) {
        NormalizedRecord.Builder b = NormalizedRecord.builder(record.externalId(), record.kind())
                .geometry(record.geometry())
                .putAll(record.payload());
        if (record.observedAt() != null) {
            b.observedAt(record.observedAt().plus(later));
        }
        if (record.eventTime() != null) {
            b.eventTime(record.eventTime().plus(later));
        }
        return b.build();
    }
}
