package ai.foresignal.ingest;

import java.time.Instant;
import java.util.Objects;

/**
 * Which heads of one source (and optionally one kind) may be purged (Core-Data-Model §13,
 * invariant I7).
 *
 * @param sourceId the source
 * @param kind only heads of this kind, or null for every kind of the source; set when the source's
 *     kinds have different retention windows and so different floors (prompt 019)
 * @param floorSeq heads with {@code seq < floorSeq} have no versions left
 * @param staleBefore only heads whose live row was last received before this are purged
 */
public record PurgeSpec(String sourceId, String kind, long floorSeq, Instant staleBefore) {

    public PurgeSpec {
        Objects.requireNonNull(sourceId, "sourceId");
        Objects.requireNonNull(staleBefore, "staleBefore");
    }

    /** A spec over every kind of {@code sourceId}. */
    public PurgeSpec(String sourceId, long floorSeq, Instant staleBefore) {
        this(sourceId, null, floorSeq, staleBefore);
    }
}
