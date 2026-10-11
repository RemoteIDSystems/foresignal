package ai.foresignal.source.spi;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Checks an ingest policy against Core-Data-Model §5.1.
 *
 * <p>Runs twice over a policy's life: on descriptor defaults when the registry starts, and on
 * {@code source_settings.ingest_policy} when an operator sets one. Both paths must agree, so the
 * rules live here rather than in either caller.
 *
 * <p>Deliberately not a Spring bean: it is a pure function over a policy and a descriptor, which
 * makes the §5.1 matrix directly testable.
 */
public final class IngestPolicyValidator {

    /** The smallest sampling interval worth having (§5.1). */
    public static final Duration MIN_SAMPLE_INTERVAL = Duration.ofSeconds(2);

    /** The headroom the keyframe must leave under the TTL, so a version never expires unseen. */
    public static final Duration KEYFRAME_TTL_MARGIN = Duration.ofSeconds(30);

    /** A stream's effective poll interval for the keyframe constraint. */
    public static final Duration STREAM_POLL_INTERVAL = Duration.ofSeconds(2);

    private IngestPolicyValidator() {}

    /**
     * The problems with {@code policy} for {@code descriptor}, empty when it is valid.
     *
     * <p>Returns every problem rather than the first, so an operator fixing a policy sees all of it
     * at once.
     *
     * @param field the path to report errors under, for example {@code ingestPolicy}
     */
    public static List<String> problems(IngestPolicy policy, SourceDescriptor descriptor, String field) {
        List<String> problems = new ArrayList<>();
        if (policy == null) {
            return problems;
        }

        Duration keyframe = policy.keyframe();
        Duration minInterval = policy.minInterval();
        Duration edgeMinInterval = policy.edgeMinInterval();
        Duration ttl = descriptor.recordTtl();

        // "keyframe: none" is only allowed when nothing expires. With a TTL, an entity that is
        // still being observed but never re-persisted would have its only version expire.
        if (keyframe == null && ttl != null) {
            problems.add(
                    field
                            + ".keyframe is required because "
                            + descriptor.id()
                            + " has a recordTtl of "
                            + ttl
                            + "; 'none' is only allowed when the source has no TTL");
        }
        if (keyframe != null && (keyframe.isZero() || keyframe.isNegative())) {
            problems.add(field + ".keyframe must be greater than zero, but was " + keyframe);
        }

        if (minInterval != null) {
            if (minInterval.compareTo(MIN_SAMPLE_INTERVAL) < 0) {
                problems.add(
                        field
                                + ".minInterval must be at least "
                                + MIN_SAMPLE_INTERVAL.toSeconds()
                                + "s, but was "
                                + minInterval);
            }
            if (keyframe != null && minInterval.compareTo(keyframe) > 0) {
                problems.add(
                        field
                                + ".minInterval ("
                                + minInterval
                                + ") must not exceed "
                                + field
                                + ".keyframe ("
                                + keyframe
                                + ")");
            }
        }

        if (edgeMinInterval != null) {
            if (edgeMinInterval.isNegative()) {
                problems.add(field + ".edgeMinInterval must not be negative, but was " + edgeMinInterval);
            }
            if (minInterval != null && edgeMinInterval.compareTo(minInterval) > 0) {
                problems.add(
                        field
                                + ".edgeMinInterval ("
                                + edgeMinInterval
                                + ") must not exceed "
                                + field
                                + ".minInterval ("
                                + minInterval
                                + ")");
            }
        }

        if (keyframe != null && ttl != null) {
            Duration ceiling = keyframeCeiling(descriptor);
            if (ceiling.isZero() || ceiling.isNegative()) {
                problems.add(
                        "The recordTtl of "
                                + descriptor.id()
                                + " ("
                                + ttl
                                + ") is too short for its poll interval of "
                                + descriptor.effectivePollInterval()
                                + "; no keyframe can satisfy keyframe <= ttl - pollInterval - "
                                + KEYFRAME_TTL_MARGIN.toSeconds()
                                + "s");
            } else if (keyframe.compareTo(ceiling) > 0) {
                problems.add(
                        field
                                + ".keyframe ("
                                + keyframe
                                + ") must not exceed ttl - pollInterval - "
                                + KEYFRAME_TTL_MARGIN.toSeconds()
                                + "s ("
                                + ceiling
                                + ")");
            }
        }

        if (policy.requiresSignificantFields() && descriptor.significantFields().isEmpty()) {
            problems.add(
                    field
                            + ".mode "
                            + policy.mode().wireValue()
                            + " requires the descriptor to declare significantFields, because"
                            + " sampling decides what to keep by what changed significantly");
        }

        if (policy instanceof IngestPolicy.Sampled sampled
                && sampled.minDeviationMeters() != null
                && sampled.minDeviationMeters() <= 0) {
            problems.add(
                    field
                            + ".minDeviationMeters must be greater than zero when set, but was "
                            + sampled.minDeviationMeters());
        }

        return problems;
    }

    /**
     * The largest keyframe {@code descriptor} permits: {@code ttl - pollInterval - 30s}.
     *
     * <p>Returns null when the source has no TTL, in which case any keyframe is fine.
     */
    public static Duration keyframeCeiling(SourceDescriptor descriptor) {
        Duration ttl = descriptor.recordTtl();
        if (ttl == null) {
            return null;
        }
        return ttl.minus(descriptor.effectivePollInterval()).minus(KEYFRAME_TTL_MARGIN);
    }

    /**
     * The keyframe an {@code ALL} policy gets when none is stated: {@code ttl - pollInterval - 30s},
     * or none when the source has no TTL (§5.1).
     */
    public static Duration defaultAllKeyframe(SourceDescriptor descriptor) {
        Duration ceiling = keyframeCeiling(descriptor);
        if (ceiling == null || ceiling.isZero() || ceiling.isNegative()) {
            return null;
        }
        return ceiling;
    }

    /**
     * {@code policy} with the documented defaults filled in: the ALL keyframe, and
     * {@code edgeMinInterval = minInterval / 2}.
     */
    public static IngestPolicy applyDefaults(IngestPolicy policy, SourceDescriptor descriptor) {
        if (policy == null) {
            return null;
        }
        return switch (policy) {
            case IngestPolicy.All all ->
                    all.keyframe() != null ? all : new IngestPolicy.All(defaultAllKeyframe(descriptor));
            case IngestPolicy.Sampled sampled ->
                    sampled.edgeMinInterval() != null
                            ? sampled
                            : new IngestPolicy.Sampled(
                                    sampled.minInterval(),
                                    sampled.keyframe(),
                                    sampled.minDeviationMeters(),
                                    sampled.thresholds(),
                                    halfOf(sampled.minInterval()));
            case IngestPolicy.Latest latest ->
                    latest.edgeMinInterval() != null
                            ? latest
                            : new IngestPolicy.Latest(latest.keyframe(), halfOf(latest.keyframe()));
        };
    }

    private static Duration halfOf(Duration duration) {
        return duration == null ? null : duration.dividedBy(2);
    }
}
