package ai.elaro.source;

import ai.elaro.source.spi.FieldThreshold;
import ai.elaro.source.spi.HistoryTier;
import ai.elaro.source.spi.IngestPolicy;
import ai.elaro.source.spi.IngestPolicyValidator;
import ai.elaro.source.spi.Mode;
import ai.elaro.source.spi.MotionSpec;
import ai.elaro.source.spi.ParamSpec;
import ai.elaro.source.spi.PollingAdapter;
import ai.elaro.source.spi.PushAdapter;
import ai.elaro.source.spi.SourceAdapter;
import ai.elaro.source.spi.SourceDescriptor;
import ai.elaro.source.spi.StreamingAdapter;
import ai.elaro.source.spi.TtlAnchor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Checks that one descriptor is internally consistent.
 *
 * <p>Separated from {@link SourceRegistry} so the same rules apply to compile-time adapters at
 * startup and to dynamic custom sources created at runtime. A custom source that skipped these
 * checks would be able to express things the rest of the system assumes are impossible.
 *
 * <p>Returns problems rather than throwing, so the registry can report every bad descriptor at once
 * instead of failing on the first.
 */
public final class DescriptorValidator {

    /** Kebab-case: lowercase alphanumerics separated by single hyphens. */
    static final Pattern ID_PATTERN = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    static final int MAX_ID_LENGTH = 64;

    private DescriptorValidator() {}

    /** The problems with {@code descriptor} and its {@code adapter}, empty when it is sound. */
    public static List<String> problems(SourceDescriptor descriptor, SourceAdapter adapter) {
        List<String> problems = new ArrayList<>();
        if (descriptor == null) {
            problems.add(
                    (adapter == null ? "An adapter" : adapter.getClass().getName())
                            + " returned a null descriptor");
            return problems;
        }

        String id = descriptor.id();
        checkId(id, problems);
        checkKinds(descriptor, problems);
        checkIntervalsAndBudget(descriptor, problems);
        checkParams(descriptor, problems);
        checkMode(descriptor, adapter, problems);
        checkTier(descriptor, problems);
        checkFieldSets(descriptor, problems);
        checkDefaultPolicy(descriptor, problems);

        return problems;
    }

    /** Non-fatal observations worth logging, for example fields that will never be searchable. */
    public static List<String> warnings(SourceDescriptor descriptor) {
        List<String> warnings = new ArrayList<>();
        if (descriptor == null || descriptor.significantFields().isEmpty()) {
            // Empty significantFields means every non-volatile key is significant, so nothing to
            // warn about.
            return warnings;
        }
        Set<String> significant = Set.copyOf(descriptor.significantFields());
        for (String field : descriptor.searchFields()) {
            if (!significant.contains(field)) {
                warnings.add(
                        descriptor.id()
                                + ": searchField '"
                                + field
                                + "' is not significant, so a change to it will not produce a new"
                                + " version and the stored search text can go stale");
            }
        }
        for (String field : descriptor.tileProperties()) {
            if (!significant.contains(field)) {
                warnings.add(
                        descriptor.id()
                                + ": tileProperty '"
                                + field
                                + "' is not significant, so tiles can show a value older than the"
                                + " latest observation");
            }
        }
        return warnings;
    }

    private static void checkId(String id, List<String> problems) {
        if (id == null || id.isBlank()) {
            problems.add("A descriptor has no id");
            return;
        }
        if (id.length() > MAX_ID_LENGTH) {
            problems.add(
                    "id '" + id + "' is " + id.length() + " characters; the maximum is " + MAX_ID_LENGTH);
        }
        if (!ID_PATTERN.matcher(id).matches()) {
            problems.add(
                    "id '"
                            + id
                            + "' must be kebab-case: lowercase letters and digits separated by single"
                            + " hyphens");
        }
    }

    private static void checkKinds(SourceDescriptor descriptor, List<String> problems) {
        if (descriptor.kinds().isEmpty()) {
            problems.add(descriptor.id() + " declares no kinds");
            return;
        }
        for (String kind : descriptor.kinds()) {
            if (kind == null || !ID_PATTERN.matcher(kind).matches()) {
                problems.add(
                        descriptor.id()
                                + ": kind '"
                                + kind
                                + "' must be kebab-case, like the source id");
            }
        }
    }

    private static void checkIntervalsAndBudget(SourceDescriptor descriptor, List<String> problems) {
        if (descriptor.mode().isPolled()) {
            if (descriptor.defaultInterval() == null) {
                problems.add(descriptor.id() + " is POLLING but declares no defaultInterval");
            }
            if (descriptor.minInterval() == null) {
                problems.add(descriptor.id() + " is POLLING but declares no minInterval");
            }
            if (descriptor.minInterval() != null
                    && descriptor.defaultInterval() != null
                    && descriptor.minInterval().compareTo(descriptor.defaultInterval()) > 0) {
                problems.add(
                        descriptor.id()
                                + ": minInterval ("
                                + descriptor.minInterval()
                                + ") must not exceed defaultInterval ("
                                + descriptor.defaultInterval()
                                + ")");
            }
            if (descriptor.minInterval() != null
                    && (descriptor.minInterval().isZero() || descriptor.minInterval().isNegative())) {
                problems.add(descriptor.id() + ": minInterval must be greater than zero");
            }
        }

        if (descriptor.defaultBudget() == null) {
            problems.add(descriptor.id() + " declares no defaultBudget");
        } else {
            if (descriptor.defaultBudget().perMinute() <= 0) {
                problems.add(
                        descriptor.id()
                                + ": defaultBudget.perMinute must be greater than zero, but was "
                                + descriptor.defaultBudget().perMinute());
            }
            if (descriptor.defaultBudget().perDay() <= 0) {
                problems.add(
                        descriptor.id()
                                + ": defaultBudget.perDay must be greater than zero, but was "
                                + descriptor.defaultBudget().perDay());
            }
        }
    }

    private static void checkParams(SourceDescriptor descriptor, List<String> problems) {
        Set<String> seen = new HashSet<>();
        for (ParamSpec spec : descriptor.params()) {
            if (!seen.add(spec.name())) {
                problems.add(descriptor.id() + ": duplicate param '" + spec.name() + "'");
            }
            if (spec.required() && spec.hasDefault()) {
                problems.add(
                        descriptor.id()
                                + ": param '"
                                + spec.name()
                                + "' is required and also has a default, which cannot both apply");
            }
            if (spec.hasDefault() && !spec.allowed().isEmpty()) {
                String rendered = String.valueOf(spec.defaultValue());
                if (!spec.allowed().contains(rendered)) {
                    problems.add(
                            descriptor.id()
                                    + ": param '"
                                    + spec.name()
                                    + "' has default '"
                                    + rendered
                                    + "', which is not one of "
                                    + spec.allowed());
                }
            }
        }
    }

    private static void checkMode(
            SourceDescriptor descriptor, SourceAdapter adapter, List<String> problems) {
        Mode mode = descriptor.mode();
        if (mode == null) {
            problems.add(descriptor.id() + " declares no mode");
            return;
        }
        if (adapter == null) {
            // A dynamic descriptor with no adapter behind it: only PUSH and DERIVED make sense,
            // because there is nothing to call.
            if (mode != Mode.PUSH && mode != Mode.DERIVED) {
                problems.add(
                        descriptor.id()
                                + " is "
                                + mode
                                + " but has no adapter implementation; only PUSH and DERIVED sources"
                                + " can be registered dynamically");
            }
            return;
        }
        switch (mode) {
            case POLLING -> {
                if (!(adapter instanceof PollingAdapter)) {
                    problems.add(
                            descriptor.id()
                                    + " declares mode POLLING but "
                                    + adapter.getClass().getName()
                                    + " does not implement PollingAdapter");
                }
            }
            case STREAMING -> {
                if (!(adapter instanceof StreamingAdapter)) {
                    problems.add(
                            descriptor.id()
                                    + " declares mode STREAMING but "
                                    + adapter.getClass().getName()
                                    + " does not implement StreamingAdapter");
                }
            }
            case PUSH -> {
                if (!(adapter instanceof PushAdapter)) {
                    problems.add(
                            descriptor.id()
                                    + " declares mode PUSH but "
                                    + adapter.getClass().getName()
                                    + " does not implement PushAdapter");
                }
            }
            case DERIVED -> {
                // Derived sources are computed, so they need no fetch implementation. What they
                // must not have is a TTL: liveness comes from explicit tombstones.
                if (descriptor.recordTtl() != null) {
                    problems.add(
                            descriptor.id()
                                    + " is DERIVED, so recordTtl must be null, but it is "
                                    + descriptor.recordTtl()
                                    + "; derived records are retired by explicit tombstones and startup"
                                    + " reconciliation, not expiry");
                }
            }
        }
    }

    private static void checkTier(SourceDescriptor descriptor, List<String> problems) {
        if (descriptor.historyTier() == HistoryTier.TRACK) {
            if (descriptor.recordTtl() == null) {
                problems.add(
                        descriptor.id()
                                + " is TRACK, which requires a recordTtl: a track is kept as a bounded"
                                + " window, and without a TTL it would grow without limit");
            }
            if (descriptor.ttlAnchor() != TtlAnchor.OBSERVED) {
                problems.add(
                        descriptor.id()
                                + " is TRACK, so ttlAnchor must be OBSERVED, but it is "
                                + descriptor.ttlAnchor());
            }
            if (descriptor.defaultRetention() != null) {
                problems.add(
                        descriptor.id()
                                + " is TRACK, so it must not set defaultRetention ("
                                + descriptor.defaultRetention()
                                + "); TRACK retention is fixed by the tier");
            }
            if (!descriptor.kindRetention().isEmpty()) {
                problems.add(
                        descriptor.id()
                                + " is TRACK, so it must not set kindRetention; TRACK retention is fixed"
                                + " by the tier");
            }
        }
    }

    private static void checkFieldSets(SourceDescriptor descriptor, List<String> problems) {
        checkTopLevel(descriptor, "volatileFields", descriptor.volatileFields(), problems);
        checkTopLevel(descriptor, "significantFields", descriptor.significantFields(), problems);
        checkTopLevel(descriptor, "searchFields", descriptor.searchFields(), problems);
        checkTopLevel(descriptor, "tileProperties", descriptor.tileProperties(), problems);
        checkTopLevel(descriptor, "embedFields", descriptor.embedFields(), problems);
        if (descriptor.rankField() != null) {
            checkTopLevel(descriptor, "rankField", List.of(descriptor.rankField()), problems);
        }

        Set<String> significant = Set.copyOf(descriptor.significantFields());
        Set<String> volatileFields = Set.copyOf(descriptor.volatileFields());

        for (String field : significant) {
            if (volatileFields.contains(field)) {
                problems.add(
                        descriptor.id()
                                + ": '"
                                + field
                                + "' is both volatile and significant, which cannot both be true");
            }
        }

        // A threshold exists to suppress small changes. If the field were also significant, every
        // change would already force a version and the threshold could never apply.
        for (FieldThreshold threshold : descriptor.thresholds()) {
            checkTopLevel(descriptor, "thresholds.field", List.of(threshold.field()), problems);
            if (significant.contains(threshold.field())) {
                problems.add(
                        descriptor.id()
                                + ": threshold field '"
                                + threshold.field()
                                + "' must not also be significant, or the threshold would never be"
                                + " reached");
            }
        }

        MotionSpec motion = descriptor.motion();
        if (motion != null) {
            checkTopLevel(descriptor, "motion.speedField", List.of(motion.speedField()), problems);
            checkTopLevel(descriptor, "motion.courseField", List.of(motion.courseField()), problems);
            for (String field : List.of(motion.speedField(), motion.courseField())) {
                if (significant.contains(field)) {
                    problems.add(
                            descriptor.id()
                                    + ": motion field '"
                                    + field
                                    + "' must not be significant; motion is used to predict position,"
                                    + " and treating every change as news defeats sampling");
                }
            }
        }
    }

    /** Field references must be top-level payload keys, so no dots or brackets. */
    private static void checkTopLevel(
            SourceDescriptor descriptor, String where, List<String> fields, List<String> problems) {
        for (String field : fields) {
            if (field == null || field.isBlank()) {
                problems.add(descriptor.id() + ": " + where + " contains a blank field name");
                continue;
            }
            if (field.indexOf('.') >= 0 || field.indexOf('[') >= 0 || field.indexOf('/') >= 0) {
                problems.add(
                        descriptor.id()
                                + ": "
                                + where
                                + " entry '"
                                + field
                                + "' must be a top-level payload key, not a path");
            }
        }
    }

    private static void checkDefaultPolicy(SourceDescriptor descriptor, List<String> problems) {
        IngestPolicy policy =
                IngestPolicyValidator.applyDefaults(descriptor.defaultIngestPolicy(), descriptor);
        problems.addAll(
                IngestPolicyValidator.problems(policy, descriptor, "defaultIngestPolicy"));
    }
}
