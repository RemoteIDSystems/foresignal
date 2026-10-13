package ai.foresignal.derive;

import ai.foresignal.ingest.CanonicalJson;
import ai.foresignal.ingest.ContentHasher;
import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;

/**
 * What a derived source last emitted per entity (prompt 052): the canonical content and the
 * {@code observedAt}. It makes emits on change only, keeps {@code observedAt} from going backwards, and
 * skips redundant tombstones. Seeded from the source's heads before every reconcile.
 *
 * <p>Not thread-safe: each source's ledger is used only by that source's runner thread.
 */
public final class EmitLedger {

    /**
     * The last emit of one entity.
     *
     * @param content the canonical payload and geometry
     * @param observedAt the observation time it was emitted at
     * @param tombstone true when the entity is tombstoned
     */
    record Entry(String content, Instant observedAt, boolean tombstone) {}

    private final Map<String, Entry> entries = new HashMap<>();

    /** Replaces the ledger with {@code heads}. */
    public void seed(List<HeadSnapshot> heads) {
        entries.clear();
        for (HeadSnapshot head : heads) {
            entries.put(key(head.kind(), head.externalId()),
                    new Entry(head.tombstone() ? null : content(head.payload(), head.geometry()), head.observedAt(),
                            head.tombstone()));
        }
    }

    /**
     * Decides an emit: null when it is identical to the entity's previous emit, otherwise the record with
     * its {@code observedAt} raised to the previous one where it was earlier. Records the emit.
     */
    public NormalizedRecord admit(NormalizedRecord record) {
        String key = key(record.kind(), record.externalId());
        Entry previous = entries.get(key);
        if (record.tombstone()) {
            if (previous == null || previous.tombstone()) {
                return null;
            }
            Instant at = later(previous.observedAt(), record.observedAt());
            entries.put(key, new Entry(null, at, true));
            return at.equals(record.observedAt()) ? record
                    : NormalizedRecord.tombstone(record.externalId(), record.kind(), at);
        }
        String content = content(record.payload(), record.geometry());
        if (previous != null && !previous.tombstone() && content.equals(previous.content())) {
            return null;
        }
        Instant at = previous == null ? record.observedAt() : later(previous.observedAt(), record.observedAt());
        entries.put(key, new Entry(content, at, false));
        if (at == null || at.equals(record.observedAt())) {
            return record;
        }
        return new NormalizedRecord(record.externalId(), record.kind(), at, record.eventTime(), record.geometry(),
                record.validUntil(), record.validFrom(), record.payload(), false, record.quality(), record.observers());
    }

    /** Forgets a rejected emit, so the next identical emit is tried again. */
    public void forget(String kind, String externalId) {
        entries.remove(key(kind, externalId));
    }

    /** The number of entities known. */
    public int size() {
        return entries.size();
    }

    /** The canonical text of a payload and a geometry. */
    static String content(Map<String, Object> payload, Geometry geometry) {
        return CanonicalJson.write(payload == null ? Map.of() : payload) + "|"
                + (geometry == null ? "" : ContentHasher.wkt(geometry));
    }

    private static Instant later(Instant a, Instant b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.isAfter(b) ? a : b;
    }

    private static String key(String kind, String externalId) {
        return kind + "\u0000" + externalId;
    }
}
