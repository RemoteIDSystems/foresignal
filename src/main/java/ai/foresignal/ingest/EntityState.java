package ai.foresignal.ingest;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

/**
 * The running state of one entity while the writer folds a batch through the decision table
 * (Core-Data-Model §6.1–§6.2): the head part (what is persisted) and the live part (what was last
 * seen).
 *
 * <p>Immutable; each transition returns a new state. The writer loads the initial state with one
 * query (§6.2 step 6) and folds every observation of the entity through
 * {@link IngestPolicyEngine#decide} and {@link #apply}. Pure: no database access.
 *
 * @param head the persisted part; {@link Head#NONE} when the entity has no head
 * @param live the live part; null exactly when there is no head (invariant I10)
 */
public record EntityState(Head head, Live live) {

    public EntityState {
        head = head == null ? Head.NONE : head;
    }

    /**
     * The persisted part ({@code record_head}).
     *
     * @param exists false when the entity has never been persisted
     * @param seq the head's seq; within a fold, whatever the writer passed to
     *     {@link #afterPersist}
     * @param version the head's version
     * @param tombstone true when the head is a tombstone
     * @param staticHash the stored static hash
     * @param persistedObservedAt {@code observed_at} of the persisted version
     * @param persistedReceivedAt {@code received_at} of the persisted version
     * @param persistedCentroid the persisted geometry's centroid (x = longitude, y = latitude), or
     *     null
     * @param persistedBbox the persisted geometry's envelope, or null
     * @param persistedPathValues values of {@code h.payload} at the paths the writer loaded (motion,
     *     threshold and gate paths); null values mean absent
     * @param persistedGeometry the persisted geometry, when loaded (the gate needs it) or produced
     *     within the fold
     * @param persistedPayload the full persisted payload when known, which is after a PERSIST within
     *     the fold; null when only path values were loaded
     */
    public record Head(
            boolean exists,
            long seq,
            int version,
            boolean tombstone,
            long staticHash,
            Instant persistedObservedAt,
            Instant persistedReceivedAt,
            Coordinate persistedCentroid,
            Envelope persistedBbox,
            Map<String, Object> persistedPathValues,
            Geometry persistedGeometry,
            Map<String, Object> persistedPayload) {

        /** No head. */
        public static final Head NONE =
                new Head(false, 0, 0, false, 0, null, null, null, null, Map.of(), null, null);

        public Head {
            persistedPathValues =
                    persistedPathValues == null
                            ? Map.of()
                            : Collections.unmodifiableMap(new HashMap<>(persistedPathValues));
        }
    }

    /**
     * The live part ({@code record_live}).
     *
     * @param observedAt the latest accepted {@code observed_at}
     * @param lastReceivedAt the latest {@code received_at} that reached the row
     * @param validUntil the effective expiry, or null for never
     * @param fullHash the full hash of the live content
     * @param dirty true when live content differs from the persisted version
     * @param kinematic the kinematic keys overlaid on {@code h.payload}; a null value masks a key
     *     the latest observation no longer carries
     * @param geometry the live geometry (loaded for TRACK carry-forward), or null
     * @param bbox the live geometry's envelope, or null
     */
    public record Live(
            Instant observedAt,
            Instant lastReceivedAt,
            Instant validUntil,
            long fullHash,
            boolean dirty,
            Map<String, Object> kinematic,
            Geometry geometry,
            Envelope bbox) {

        public Live {
            kinematic =
                    kinematic == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(kinematic));
        }
    }

    /** An entity with no head and no live row. */
    public static EntityState absent() {
        return new EntityState(Head.NONE, null);
    }

    /** True when the entity has a head. */
    public boolean hasHead() {
        return head.exists();
    }

    /** The geometry the entity last had, for TRACK carry-forward; null when unknown. */
    public Geometry runningGeometry() {
        return live == null ? null : live.geometry();
    }

    /**
     * The persisted payload value at {@code path}: read from the full persisted payload when the
     * fold has it, else from the loaded path values. Null when absent.
     */
    public Object persistedValue(String path) {
        if (head.persistedPayload() != null) {
            return JsonValues.at(head.persistedPayload(), path);
        }
        return head.persistedPathValues().get(path);
    }

    /**
     * The current payload, {@code jsonb_strip_nulls(h.payload || l.kinematic)} (Core-Data-Model
     * §8), or null when the persisted payload is not known in full.
     */
    public Map<String, Object> currentPayload() {
        if (head.persistedPayload() == null) {
            return null;
        }
        Map<String, Object> merged = new LinkedHashMap<>(head.persistedPayload());
        if (live != null) {
            merged.putAll(live.kinematic());
        }
        merged.values().removeIf(java.util.Objects::isNull);
        return Collections.unmodifiableMap(merged);
    }

    // ---------- transitions (§6.2 steps 10–12) ----------

    /**
     * Applies {@code decision}: PERSIST → {@link #afterPersist}, LIVE → {@link #afterLive}, TOUCH →
     * {@link #afterTouch}; LATE, SKIP and TOUCH_THROTTLED leave the state unchanged (nothing is
     * written for them).
     *
     * @param seq the seq the writer assigns to a persisted version; only used for PERSIST
     */
    public EntityState apply(Decision decision, PreparedObservation prepared, SourceIngestProfile profile, long seq) {
        return switch (decision.outcome()) {
            case PERSIST -> afterPersist(prepared, profile, seq);
            case LIVE -> afterLive(prepared);
            case TOUCH -> afterTouch(prepared);
            case LATE, SKIP, TOUCH_THROTTLED -> this;
        };
    }

    /**
     * After a version is persisted: the head takes the observation's content; the live row's
     * {@code kinematic} becomes the kinematic subset of the persisted payload and {@code dirty}
     * clears. A tombstone clears geometry and {@code kinematic}.
     */
    public EntityState afterPersist(PreparedObservation prepared, SourceIngestProfile profile, long seq) {
        Observation obs = prepared.observation();
        Geometry geometry = obs.tombstone() ? null : obs.geometry();
        Map<String, Object> payload = obs.tombstone() ? Map.of() : obs.payload();
        Head newHead =
                new Head(
                        true,
                        seq,
                        head.version() + 1,
                        obs.tombstone(),
                        prepared.staticHash(),
                        obs.observedAt(),
                        obs.receivedAt(),
                        centroid(geometry),
                        envelope(geometry),
                        Map.of(),
                        geometry,
                        payload);
        Map<String, Object> kinematic =
                obs.tombstone() ? Map.of() : ContentHasher.kinematicSubset(payload, profile);
        Live newLive =
                new Live(
                        obs.observedAt(),
                        latest(live == null ? null : live.lastReceivedAt(), obs.receivedAt()),
                        obs.validUntil(),
                        prepared.fullHash(),
                        false,
                        kinematic,
                        geometry,
                        envelope(geometry));
        return new EntityState(newHead, newLive);
    }

    /**
     * After a live-only update: {@code kinematic} becomes the incoming kinematic keys plus JSON
     * null for keys the previous {@code kinematic} had but the observation lacks, and
     * {@code dirty} is set. The head is unchanged.
     */
    public EntityState afterLive(PreparedObservation prepared) {
        Observation obs = prepared.observation();
        Map<String, Object> kinematic = new LinkedHashMap<>();
        if (live != null) {
            for (String key : live.kinematic().keySet()) {
                if (!prepared.kinematic().containsKey(key)) {
                    kinematic.put(key, null);
                }
            }
        }
        kinematic.putAll(prepared.kinematic());
        Geometry geometry = obs.tombstone() ? null : obs.geometry();
        Live newLive =
                new Live(
                        obs.observedAt(),
                        latest(live == null ? null : live.lastReceivedAt(), obs.receivedAt()),
                        obs.validUntil(),
                        prepared.fullHash(),
                        true,
                        obs.tombstone() ? Map.of() : kinematic,
                        geometry,
                        envelope(geometry));
        return new EntityState(head, newLive);
    }

    /**
     * After a touch: {@code observed_at}, {@code last_received_at} and {@code valid_until} move;
     * content, hash and {@code dirty} are unchanged.
     */
    public EntityState afterTouch(PreparedObservation prepared) {
        if (live == null) {
            return this;
        }
        Observation obs = prepared.observation();
        Live newLive =
                new Live(
                        obs.observedAt(),
                        latest(live.lastReceivedAt(), obs.receivedAt()),
                        obs.validUntil(),
                        live.fullHash(),
                        live.dirty(),
                        live.kinematic(),
                        live.geometry(),
                        live.bbox());
        return new EntityState(head, newLive);
    }

    private static Instant latest(Instant a, Instant b) {
        if (a == null) {
            return b;
        }
        return b == null || a.isAfter(b) ? a : b;
    }

    private static Coordinate centroid(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        return geometry.getCentroid().getCoordinate();
    }

    private static Envelope envelope(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        return geometry.getEnvelopeInternal();
    }
}
