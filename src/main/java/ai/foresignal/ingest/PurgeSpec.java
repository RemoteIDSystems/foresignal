package ai.foresignal.ingest;

import java.time.Instant;
import java.util.Objects;

/**
 * Which heads of one source may be purged (Core-Data-Model §13, invariant I7).
 *
 * @param sourceId the source
 * @param floorSeq heads with {@code seq < floorSeq} have no versions left
 * @param staleBefore only heads whose live row was last received before this are purged
 */
public record PurgeSpec(String sourceId, long floorSeq, Instant staleBefore) {

    public PurgeSpec {
        Objects.requireNonNull(sourceId, "sourceId");
        Objects.requireNonNull(staleBefore, "staleBefore");
    }
}
