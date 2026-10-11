package ai.foresignal.source.spi;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.Duration;

/**
 * How much of an observation stream is worth persisting as versions (Core-Data-Model §5).
 *
 * <p>This is the mechanism that keeps a position feed affordable. Without it, 40,000 aircraft
 * observations a minute would each become a stored version, and history would be dominated by rows
 * that say nothing new. With it, Foresignal persists what changed, plus a heartbeat often enough that a
 * continuously observed entity always has a recent version (invariant I6).
 *
 * <p>Sealed, because the three modes are not interchangeable settings on one object: ALL needs no
 * sampling knobs, and SAMPLED needs a deviation that LATEST has no use for. Making them separate
 * types means an invalid combination cannot be constructed, only rejected.
 *
 * <p>Serialized with a {@code mode} discriminator, matching the JSON in §5.1.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "mode")
@JsonSubTypes({
    @JsonSubTypes.Type(value = IngestPolicy.All.class, name = "ALL"),
    @JsonSubTypes.Type(value = IngestPolicy.Sampled.class, name = "SAMPLED"),
    @JsonSubTypes.Type(value = IngestPolicy.Latest.class, name = "LATEST")
})
public sealed interface IngestPolicy {

    /** The mode name used in JSON and in error messages. */
    PolicyMode mode();

    /**
     * How long an unchanged but still-observed entity may go without a new version.
     *
     * <p>Null means "none", which is only allowed when the source has no TTL.
     */
    Duration keyframe();

    /** Smallest gap between sampled versions; null for {@link All}. */
    Duration minInterval();

    /**
     * Smallest gap between versions forced by a trigger edge.
     *
     * <p>Exists so a value oscillating across a rule's threshold cannot make every observation
     * significant. Defaults to half of {@link #minInterval()}.
     */
    Duration edgeMinInterval();

    /** The three modes, for switching and for messages. */
    enum PolicyMode {
        ALL,
        SAMPLED,
        LATEST;

        public String wireValue() {
            return name();
        }
    }

    /**
     * Persist every observation whose payload differs at all.
     *
     * <p>The default, and right for discrete records: an earthquake revision is always worth
     * keeping.
     */
    record All(Duration keyframe) implements IngestPolicy {

        @Override
        public PolicyMode mode() {
            return PolicyMode.ALL;
        }

        @Override
        public Duration minInterval() {
            return null;
        }

        @Override
        public Duration edgeMinInterval() {
            return null;
        }
    }

    /**
     * Persist on significant change, and otherwise at most every {@code minInterval} when the
     * entity has moved far enough or a threshold was crossed.
     *
     * <p>For position streams. Deviation is measured against dead reckoning from the last persisted
     * position, so an entity doing exactly what was predicted does not earn a version.
     *
     * @param minInterval smallest gap between sampled versions
     * @param keyframe heartbeat interval
     * @param minDeviationMeters how far from the predicted position counts as news; null disables
     *     the distance test
     * @param thresholds whether the descriptor's field thresholds participate
     * @param edgeMinInterval smallest gap between trigger-edge versions
     */
    record Sampled(
            Duration minInterval,
            Duration keyframe,
            Double minDeviationMeters,
            boolean thresholds,
            Duration edgeMinInterval)
            implements IngestPolicy {

        @Override
        public PolicyMode mode() {
            return PolicyMode.SAMPLED;
        }
    }

    /**
     * Keep only current state plus significant changes: no periodic sampling at all.
     *
     * <p>{@link Sampled} without the interval test, for feeds where the track itself is not
     * interesting but the current position is.
     */
    record Latest(Duration keyframe, Duration edgeMinInterval) implements IngestPolicy {

        @Override
        public PolicyMode mode() {
            return PolicyMode.LATEST;
        }

        @Override
        public Duration minInterval() {
            return null;
        }
    }

    /** {@code ALL} with no keyframe, for a source with no TTL. */
    static IngestPolicy all() {
        return new All(null);
    }

    /** {@code ALL} with an explicit heartbeat. */
    static IngestPolicy all(Duration keyframe) {
        return new All(keyframe);
    }

    static IngestPolicy sampled(
            Duration minInterval, Duration keyframe, Double minDeviationMeters, boolean thresholds) {
        return new Sampled(minInterval, keyframe, minDeviationMeters, thresholds, null);
    }

    static IngestPolicy latest(Duration keyframe) {
        return new Latest(keyframe, null);
    }

    /** True when this policy needs the descriptor to declare significant fields (§5.1). */
    default boolean requiresSignificantFields() {
        return mode() != PolicyMode.ALL;
    }
}
