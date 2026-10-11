package ai.foresignal.ingest;

import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.source.spi.FieldThreshold;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.IngestPolicyValidator;
import ai.foresignal.source.spi.MotionSpec;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.RecordLimits;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Everything ingest needs to know about one source, resolved once (Core-Data-Model §5, §6.1).
 *
 * <p>Immutable. Built from the descriptor, the effective ingest policy
 * ({@code SourceService.effectiveIngestPolicy}) and the retention window, and cached by
 * {@link SourceIngestProfiles}, which rebuilds it when settings change.
 *
 * @param sourceId the source
 * @param tier STANDARD or TRACK
 * @param observedAtMode the fixed time base
 * @param ttl the record TTL, or null when records never expire
 * @param ttlAnchor which timestamp the TTL and keyframes count from
 * @param policy the effective policy, defaults applied
 * @param limits per-record caps
 * @param skewTolerance how far ahead {@code observed_at} may be before it is flagged
 * @param allowNullIsland true when (0,0) is a real location for the source
 * @param kinds the declared kinds
 * @param volatileFields top-level keys excluded from hashes and {@code kinematic}
 * @param significantFields top-level keys of the static hash; empty means every non-volatile key
 * @param motion speed and course fields, or null
 * @param thresholds numeric change thresholds
 * @param searchFields payload paths concatenated into the search text
 * @param titleField the descriptor's title field, or null
 * @param entityKeys entity key type to payload field
 * @param privateSource true for personal data
 * @param privatePrecisionDecimals coordinate decimals kept when private
 * @param recipe the hash recipe ({@link ContentHasher#recipe})
 * @param pollInterval the descriptor's poll interval, 2 s for streams and other non-polled modes
 * @param keyframe the policy keyframe, else the ALL default {@code ttl − pollInterval − 30 s},
 *     else null (none)
 * @param window the retention window: {@code retention.trackWindow} for TRACK, the effective
 *     retention for STANDARD
 * @param touchMin {@code clamp(ttl / 10, 30 s, 1 h)}, or 1 h without a TTL
 */
public record SourceIngestProfile(
        String sourceId,
        HistoryTier tier,
        ObservedAtMode observedAtMode,
        Duration ttl,
        TtlAnchor ttlAnchor,
        IngestPolicy policy,
        RecordLimits limits,
        Duration skewTolerance,
        boolean allowNullIsland,
        Set<String> kinds,
        Set<String> volatileFields,
        Set<String> significantFields,
        MotionSpec motion,
        List<FieldThreshold> thresholds,
        List<String> searchFields,
        String titleField,
        Map<String, String> entityKeys,
        boolean privateSource,
        int privatePrecisionDecimals,
        int recipe,
        Duration pollInterval,
        Duration keyframe,
        Duration window,
        Duration touchMin) {

    /** The lower bound of {@link #touchMin}. */
    public static final Duration TOUCH_MIN_FLOOR = Duration.ofSeconds(30);

    /** The upper bound of {@link #touchMin}, and its value without a TTL. */
    public static final Duration TOUCH_MIN_CEILING = Duration.ofHours(1);

    public SourceIngestProfile {
        Objects.requireNonNull(sourceId, "sourceId");
        Objects.requireNonNull(policy, "policy");
        Objects.requireNonNull(window, "window");
        kinds = Set.copyOf(kinds);
        volatileFields = Set.copyOf(volatileFields);
        significantFields = Set.copyOf(significantFields);
        thresholds = List.copyOf(thresholds);
        searchFields = List.copyOf(searchFields);
        entityKeys = Map.copyOf(entityKeys);
    }

    /**
     * Builds a profile.
     *
     * @param descriptor the source's descriptor
     * @param effectivePolicy the policy in force; null means the descriptor's default. Defaults
     *     ({@link IngestPolicyValidator#applyDefaults}) are applied either way
     * @param window the retention window, see {@link #window(SourceDescriptor, Optional,
     *     RetentionSettings)}
     */
    public static SourceIngestProfile of(
            SourceDescriptor descriptor, IngestPolicy effectivePolicy, Duration window) {
        IngestPolicy policy =
                IngestPolicyValidator.applyDefaults(
                        effectivePolicy != null ? effectivePolicy : descriptor.defaultIngestPolicy(),
                        descriptor);
        return new SourceIngestProfile(
                descriptor.id(),
                descriptor.historyTier(),
                descriptor.observedAtMode(),
                descriptor.recordTtl(),
                descriptor.ttlAnchor(),
                policy,
                descriptor.limits(),
                descriptor.skewTolerance(),
                descriptor.allowNullIsland(),
                descriptor.kinds(),
                Set.copyOf(descriptor.volatileFields()),
                Set.copyOf(descriptor.significantFields()),
                descriptor.motion(),
                descriptor.thresholds(),
                descriptor.searchFields(),
                descriptor.titleField(),
                descriptor.entityKeys(),
                descriptor.privateSource(),
                descriptor.privatePrecisionDecimals(),
                ContentHasher.recipe(descriptor.volatileFields(), descriptor.significantFields()),
                descriptor.effectivePollInterval(),
                keyframe(policy, descriptor),
                window,
                touchMin(descriptor.recordTtl()));
    }

    /**
     * A profile from the descriptor alone: its default policy and default retention, with
     * {@code retention} supplying the global windows. For tests and {@code SnapshotContract}.
     */
    public static SourceIngestProfile forDescriptor(SourceDescriptor descriptor, RetentionSettings retention) {
        return of(
                descriptor,
                null,
                window(descriptor, Optional.ofNullable(descriptor.defaultRetention()), retention));
    }

    /** {@link #forDescriptor(SourceDescriptor, RetentionSettings)} with the shipped retention defaults. */
    public static SourceIngestProfile forDescriptor(SourceDescriptor descriptor) {
        return forDescriptor(descriptor, RetentionSettings.defaults());
    }

    /**
     * The retention window: TRACK uses {@code retention.trackWindow}; STANDARD uses the effective
     * retention (the source setting, else the descriptor's {@code defaultRetention}, as
     * {@code SourceService.effectiveRetention} resolves it), else {@code retention.default}.
     */
    public static Duration window(
            SourceDescriptor descriptor, Optional<Duration> effectiveRetention, RetentionSettings retention) {
        RetentionSettings settings = retention == null ? RetentionSettings.defaults() : retention;
        if (descriptor.isTrack()) {
            return settings.trackWindow();
        }
        return effectiveRetention.orElse(settings.defaultRetention());
    }

    /**
     * The keyframe: the policy's, else the ALL default {@code ttl − pollInterval − 30 s}, else null
     * (none).
     */
    public static Duration keyframe(IngestPolicy policy, SourceDescriptor descriptor) {
        if (policy != null && policy.keyframe() != null) {
            return policy.keyframe();
        }
        return IngestPolicyValidator.defaultAllKeyframe(descriptor);
    }

    /** {@code clamp(ttl / 10, 30 s, 1 h)}, or 1 h without a TTL. */
    public static Duration touchMin(Duration ttl) {
        if (ttl == null) {
            return TOUCH_MIN_CEILING;
        }
        Duration tenth = ttl.dividedBy(10);
        if (tenth.compareTo(TOUCH_MIN_FLOOR) < 0) {
            return TOUCH_MIN_FLOOR;
        }
        if (tenth.compareTo(TOUCH_MIN_CEILING) > 0) {
            return TOUCH_MIN_CEILING;
        }
        return tenth;
    }

    /** {@code window / 4}: the received-axis keyframe that keeps I6 under retention. */
    public Duration retentionKeyframe() {
        return window.dividedBy(4);
    }

    /** {@code min(keyframe, window / 4)}; {@code window / 4} when the keyframe is none. */
    public Duration effectiveKeyframe() {
        Duration retentionKeyframe = retentionKeyframe();
        return keyframe == null || keyframe.compareTo(retentionKeyframe) > 0 ? retentionKeyframe : keyframe;
    }

    /** True for TRACK sources. */
    public boolean isTrack() {
        return tier == HistoryTier.TRACK;
    }

    /** {@code x.observed_at} or {@code x.received_at}, per {@link #ttlAnchor}. */
    public Instant anchor(Instant observedAt, Instant receivedAt) {
        return ttlAnchor == TtlAnchor.RECEIVED ? receivedAt : observedAt;
    }

    /** The anchor of an observation. */
    public Instant anchor(Observation observation) {
        return anchor(observation.observedAt(), observation.receivedAt());
    }

    /**
     * The persisted-payload paths the policy needs from the head regardless of trigger rules:
     * motion speed and course fields, and threshold fields.
     */
    public Set<String> basePaths() {
        Set<String> paths = new LinkedHashSet<>();
        if (motion != null) {
            paths.add(motion.speedField());
            paths.add(motion.courseField());
        }
        for (FieldThreshold threshold : thresholds) {
            paths.add(threshold.field());
        }
        return paths;
    }

    /**
     * Every persisted-payload path the writer must load for this source: {@link #basePaths()} plus
     * the gate's paths from {@code snapshot}. A method rather than a component, because the gate
     * snapshot changes with trigger rules, independently of the profile.
     */
    public Set<String> loadPaths(GateSnapshot snapshot) {
        Set<String> paths = new LinkedHashSet<>(basePaths());
        if (snapshot != null) {
            paths.addAll(snapshot.spec(sourceId).paths());
        }
        return paths;
    }

    /** True when the writer must load {@code h.persisted_geom} for the gate. */
    public boolean needsPersistedGeometry(GateSnapshot snapshot) {
        return snapshot != null && snapshot.spec(sourceId).needsGeometry();
    }
}
