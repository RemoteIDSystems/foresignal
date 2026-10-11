package ai.foresignal.ingest;

import ai.foresignal.source.spi.FieldThreshold;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.MotionSpec;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

/**
 * Decides, per observation, whether to persist a version, update live state, touch, or drop
 * (Core-Data-Model §5.2). Pure: no database, no clock, no Spring.
 *
 * <p>Used by the writer (prompt 009) in three steps per observation of an entity:
 *
 * <ol>
 *   <li>{@link #prepare}: TRACK carry-forward, then the static and full hashes;
 *   <li>{@link #gateFlip} and {@link #decide}: the decision table, first match wins;
 *   <li>{@link EntityState#apply}: the running state after the outcome.
 * </ol>
 *
 * <p><strong>The decision table</strong>, with {@code Δt = observed_at − h.persisted_observed_at},
 * {@code keyframeDue = (anchor(obs) − h.persisted_anchor ≥ keyframe) OR (received_at −
 * h.persisted_received_at ≥ window/4)} and {@code expired = l.valid_until IS NOT NULL AND
 * l.valid_until < anchor(obs)}:
 *
 * <ol>
 *   <li>{@code observed_at < l.observed_at} → LATE (equal timestamps pass)
 *   <li>tombstone and (no head or head tombstoned) → SKIP
 *   <li>tombstone → PERSIST TOMBSTONE
 *   <li>no head → PERSIST FIRST
 *   <li>head tombstoned or expired → PERSIST REAPPEAR
 *   <li>static hash differs → PERSIST SIGNIFICANT
 *   <li>policy ALL and full hash differs → PERSIST CHANGE
 *   <li>keyframeDue → PERSIST HEARTBEAT if {@code full = l.full AND NOT l.dirty}, else KEYFRAME
 *   <li>gate flip and {@code Δt ≥ edgeMinInterval} (null = 0) → PERSIST TRIGGER_EDGE
 *   <li>SAMPLED, {@code Δt ≥ minInterval}, and (deviation ≥ minDeviationMeters, or a threshold
 *       exceeded when {@code thresholds=true}) → PERSIST SAMPLE
 *   <li>full hash equal → TOUCH if {@code received − l.last_received ≥ touchMin} or
 *       {@code observed − l.observed ≥ touchMin}, else TOUCH_THROTTLED
 *   <li>otherwise → LIVE
 * </ol>
 *
 * <p>LATEST is SAMPLED without row 10. Keyframe "none" disables the anchor-axis half of row 8; the
 * retention keyframe ({@code window/4}) always applies.
 *
 * <p><strong>Out-of-order observations.</strong> Gap-Analysis §8.A item 2's guard ("store the late
 * version, don't regress the head") is superseded by Core-Data-Model's LATE rule: a late
 * observation is dropped with no side effects, which keeps invariant I4 (accepted
 * {@code observed_at} never decreases per entity).
 */
public final class IngestPolicyEngine {

    private IngestPolicyEngine() {}

    /**
     * Prepares {@code observation} against the running state: for a TRACK source, a non-tombstone
     * observation without geometry inherits the running geometry and is flagged
     * {@link QaFlags#POSITION_STALE}; then both hashes and the kinematic subset are computed. Carry
     * forward happens before hashing, so losing a position never flips the static hash.
     */
    public static PreparedObservation prepare(
            Observation observation, EntityState state, SourceIngestProfile profile) {
        Observation obs = observation;
        if (profile.isTrack()
                && !obs.tombstone()
                && obs.geometry() == null
                && state != null
                && state.runningGeometry() != null) {
            obs = obs.withCarriedGeometry(state.runningGeometry());
        }
        long staticHash = ContentHasher.staticHash(obs, profile);
        long fullHash = ContentHasher.fullHash(obs, profile);
        Map<String, Object> kinematic =
                obs.tombstone() ? Map.of() : ContentHasher.kinematicSubset(obs.payload(), profile);
        return new PreparedObservation(obs, staticHash, fullHash, kinematic);
    }

    /**
     * Whether the gate reports a predicate flip between the persisted state ({@code h.payload}
     * paths and {@code h.persisted_geom}) and the candidate (the prepared observation, after
     * carry-forward). False without a head, since FIRST already persists.
     */
    public static boolean gateFlip(
            PreparedObservation prepared, EntityState state, SourceIngestProfile profile, GateSnapshot snapshot) {
        if (snapshot == null || state == null || !state.hasHead()) {
            return false;
        }
        GateSpec spec = snapshot.spec(profile.sourceId());
        if (spec == null || spec.isEmpty()) {
            return false;
        }
        Observation obs = prepared.observation();
        Map<String, Object> persistedValues = new HashMap<>();
        Map<String, Object> candidateValues = new HashMap<>();
        for (String path : spec.paths()) {
            persistedValues.put(path, state.persistedValue(path));
            candidateValues.put(path, JsonValues.at(obs.payload(), path));
        }
        GateState persisted =
                new GateState(persistedValues, spec.needsGeometry() ? state.head().persistedGeometry() : null);
        GateState candidate = new GateState(candidateValues, spec.needsGeometry() ? obs.geometry() : null);
        return snapshot.flips(profile.sourceId(), obs.kind(), persisted, candidate);
    }

    /**
     * Applies the decision table (Core-Data-Model §5.2) exactly, first match wins.
     *
     * @param prepared the prepared observation
     * @param state the running state of the entity
     * @param profile the source's profile
     * @param gateFlip whether {@link #gateFlip} reported a flip
     */
    public static Decision decide(
            PreparedObservation prepared, EntityState state, SourceIngestProfile profile, boolean gateFlip) {
        Observation obs = prepared.observation();
        EntityState s = state == null ? EntityState.absent() : state;
        EntityState.Head h = s.head();
        EntityState.Live l = s.live();

        // Row 1: LATE. Equal timestamps pass, in arrival order.
        if (l != null && l.observedAt() != null && obs.observedAt().isBefore(l.observedAt())) {
            return Decision.of(Outcome.LATE);
        }
        // Row 2: a tombstone for nothing.
        if (obs.tombstone() && (!h.exists() || h.tombstone())) {
            return Decision.of(Outcome.SKIP);
        }
        // Row 3.
        if (obs.tombstone()) {
            return Decision.persist(PersistReason.TOMBSTONE);
        }
        // Row 4.
        if (!h.exists()) {
            return Decision.persist(PersistReason.FIRST);
        }
        // Row 5.
        Instant anchor = profile.anchor(obs);
        boolean expired = l != null && l.validUntil() != null && l.validUntil().isBefore(anchor);
        if (h.tombstone() || expired) {
            return Decision.persist(PersistReason.REAPPEAR);
        }
        // Row 6.
        if (prepared.staticHash() != h.staticHash()) {
            return Decision.persist(PersistReason.SIGNIFICANT);
        }
        IngestPolicy policy = profile.policy();
        boolean fullEqual = l != null && prepared.fullHash() == l.fullHash();
        // Row 7.
        if (policy.mode() == IngestPolicy.PolicyMode.ALL && !fullEqual) {
            return Decision.persist(PersistReason.CHANGE);
        }
        // Row 8.
        if (keyframeDue(obs, h, profile)) {
            boolean heartbeat = fullEqual && !l.dirty();
            return Decision.persist(heartbeat ? PersistReason.HEARTBEAT : PersistReason.KEYFRAME);
        }
        Duration dt = h.persistedObservedAt() == null ? null : Duration.between(h.persistedObservedAt(), obs.observedAt());
        // Row 9.
        if (gateFlip && dt != null) {
            Duration edgeMin = policy.edgeMinInterval() == null ? Duration.ZERO : policy.edgeMinInterval();
            if (dt.compareTo(edgeMin) >= 0) {
                return Decision.persist(PersistReason.TRIGGER_EDGE);
            }
        }
        // Row 10. LATEST is SAMPLED without this row.
        if (policy instanceof IngestPolicy.Sampled sampled
                && dt != null
                && sampled.minInterval() != null
                && dt.compareTo(sampled.minInterval()) >= 0
                && (deviates(sampled, prepared, s, profile, dt)
                        || (sampled.thresholds() && thresholdExceeded(profile, s, obs.payload())))) {
            return Decision.persist(PersistReason.SAMPLE);
        }
        // Row 11.
        if (fullEqual) {
            Duration touchMin = profile.touchMin();
            boolean receivedDue =
                    l.lastReceivedAt() == null
                            || Duration.between(l.lastReceivedAt(), obs.receivedAt()).compareTo(touchMin) >= 0;
            boolean observedDue =
                    l.observedAt() == null
                            || Duration.between(l.observedAt(), obs.observedAt()).compareTo(touchMin) >= 0;
            return Decision.of(receivedDue || observedDue ? Outcome.TOUCH : Outcome.TOUCH_THROTTLED);
        }
        // Row 12.
        return Decision.of(Outcome.LIVE);
    }

    /**
     * Row 8's condition: the anchor-axis keyframe (skipped when the keyframe is none) or the
     * received-axis retention keyframe ({@code window/4}) is due.
     */
    static boolean keyframeDue(Observation obs, EntityState.Head h, SourceIngestProfile profile) {
        Duration keyframe = profile.keyframe();
        if (keyframe != null) {
            Instant persistedAnchor = profile.anchor(h.persistedObservedAt(), h.persistedReceivedAt());
            if (persistedAnchor != null
                    && Duration.between(persistedAnchor, profile.anchor(obs)).compareTo(keyframe) >= 0) {
                return true;
            }
        }
        return h.persistedReceivedAt() != null
                && Duration.between(h.persistedReceivedAt(), obs.receivedAt()).compareTo(profile.retentionKeyframe())
                        >= 0;
    }

    private static boolean deviates(
            IngestPolicy.Sampled sampled,
            PreparedObservation prepared,
            EntityState state,
            SourceIngestProfile profile,
            Duration dt) {
        Double minDeviation = sampled.minDeviationMeters();
        if (minDeviation == null) {
            return false;
        }
        return deviationMeters(prepared.observation().geometry(), state, profile, dt) >= minDeviation;
    }

    /**
     * The deviation in meters of {@code candidate} from the dead-reckoned persisted position
     * (persisted speed and course from {@code h.payload}'s motion paths), or the plain distance
     * without motion; 0 when either side lacks geometry.
     */
    public static double deviationMeters(
            Geometry candidate, EntityState state, SourceIngestProfile profile, Duration dt) {
        Coordinate persisted = state.head().persistedCentroid();
        if (persisted == null || candidate == null || candidate.isEmpty()) {
            return 0.0;
        }
        Coordinate candidateCentroid = candidate.getCentroid().getCoordinate();
        MotionSpec motion = profile.motion();
        Double speed = null;
        Double course = null;
        MotionSpec.SpeedUnit unit = null;
        if (motion != null) {
            speed = JsonValues.finiteNumber(state.persistedValue(motion.speedField()));
            course = JsonValues.finiteNumber(state.persistedValue(motion.courseField()));
            unit = motion.speedUnit();
        }
        return DeadReckoning.deviationMeters(persisted, speed, unit, course, dt, candidateCentroid);
    }

    /**
     * True when any threshold field differs between {@code h.payload} and the candidate by at least
     * its delta, with angular wrap where set. Missing or non-numeric on either side is not
     * exceeded.
     */
    public static boolean thresholdExceeded(
            SourceIngestProfile profile, EntityState state, Map<String, Object> candidatePayload) {
        for (FieldThreshold threshold : profile.thresholds()) {
            Double persisted = JsonValues.finiteNumber(state.persistedValue(threshold.field()));
            Double candidate = JsonValues.finiteNumber(JsonValues.at(candidatePayload, threshold.field()));
            if (persisted != null && candidate != null && threshold.exceeded(persisted, candidate)) {
                return true;
            }
        }
        return false;
    }
}
