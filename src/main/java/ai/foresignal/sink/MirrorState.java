package ai.foresignal.sink;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

/**
 * The bookkeeping of a STATE_MIRROR sink (prompt 110, Core-Data-Model §10.2): which records the external copy
 * holds, with the {@code observedAt} each was mirrored at, and when each expires. Pure and single-threaded: the
 * sink's run thread owns it.
 *
 * <ul>
 *   <li><em>Last writer wins:</em> an update older than the mirrored {@code observedAt} is ignored, so a late
 *       LiveBus update can never overwrite a newer persisted one (or the reverse).
 *   <li><em>Removal:</em> a tombstone, or a record that no longer passes the filter or the egress gate, removes the
 *       record, but only when it was mirrored: a removal is never sent for something the target never received,
 *       so a refused record's existence does not leak.
 *   <li><em>Expiry:</em> a record with {@code validUntil} is removed once it passes, unless a newer update replaced
 *       it meanwhile.
 * </ul>
 */
public final class MirrorState {

    /** What the sink must do with an update. */
    public enum Action {
        /** Send the record to the target ({@link SinkInstance#upsert}). */
        UPSERT,
        /** Remove the record from the target ({@link SinkInstance#remove}). */
        REMOVE,
        /** Nothing: the update is stale, or a removal of something never mirrored. */
        IGNORE,
        /** Nothing, and the update counts as dropped: it is older than the mirrored state. */
        STALE
    }

    /**
     * A record due for removal.
     *
     * @param sourceId its source
     * @param externalId its external id
     */
    public record Removal(String sourceId, String externalId) {}

    private record Expiry(Instant at, String key, Instant observedAt, String sourceId, String externalId) {}

    private final Map<String, Instant> mirrored = new HashMap<>();
    private final PriorityQueue<Expiry> expiries = new PriorityQueue<>((a, b) -> a.at().compareTo(b.at()));

    /**
     * Decides what to do with {@code record} and updates the bookkeeping.
     *
     * @param passes true when the record passes the sink's filter and egress gate
     */
    public Action apply(SinkRecord record, boolean passes) {
        String key = record.key();
        if (!passes || record.tombstone()) {
            return mirrored.remove(key) != null ? Action.REMOVE : Action.IGNORE;
        }
        Instant prior = mirrored.get(key);
        Instant observed = record.observedAt();
        if (prior != null && observed != null && observed.isBefore(prior)) {
            return Action.STALE;
        }
        Instant stamp = observed != null ? observed : prior != null ? prior : Instant.EPOCH;
        mirrored.put(key, stamp);
        if (record.validUntil() != null) {
            expiries.add(new Expiry(record.validUntil(), key, stamp, record.sourceId(), record.externalId()));
        }
        return Action.UPSERT;
    }

    /** The mirrored records whose {@code validUntil} is at or before {@code now}; they are forgotten. */
    public List<Removal> expire(Instant now) {
        List<Removal> out = new ArrayList<>();
        while (!expiries.isEmpty() && !expiries.peek().at().isAfter(now)) {
            Expiry due = expiries.poll();
            Instant current = mirrored.get(due.key());
            if (current != null && Objects.equals(current, due.observedAt())) {
                mirrored.remove(due.key());
                out.add(new Removal(due.sourceId(), due.externalId()));
            }
        }
        return out;
    }

    /** The next expiry time, or null when nothing expires. */
    public Instant nextExpiry() {
        Expiry next = expiries.peek();
        return next == null ? null : next.at();
    }

    /** True when {@code sourceId|externalId} is mirrored. */
    public boolean contains(String sourceId, String externalId) {
        return mirrored.containsKey(sourceId + "|" + externalId);
    }

    /** How many records are mirrored. */
    public int size() {
        return mirrored.size();
    }

    /** Forgets everything (a resync). */
    public void clear() {
        mirrored.clear();
        expiries.clear();
    }
}
