package ai.foresignal.trigger;

import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RestrictionClamp;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Filters for listing alerts ({@code listAlerts}) and replaying them ({@code streamAlerts}).
 *
 * @param ruleIds only these rules; empty for all
 * @param sources only these sources; empty for all
 * @param kinds only these kinds; empty for all
 * @param since only alerts fired at or after this, or null
 * @param acknowledged true for acknowledged only, false for unacknowledged only, null for both
 * @param states only these state wire values; empty for all
 * @param feedback only these feedback wire values; empty for all
 * @param minSeverity only this severity and above, or null
 * @param tag only alerts with this tag, or null
 * @param includeSuppressed include alerts recorded under a snooze
 * @param excludedSources sources the caller may not see (private sources for anonymous callers)
 * @param restriction the caller's read restriction (prompt 035): source, kind and, through the
 *     alert's record snapshot geometry, area
 * @param includeExercise include exercise replay alerts (prompt 115); false everywhere but where a caller asks
 */
public record AlertQuery(
        Set<UUID> ruleIds,
        List<String> sources,
        List<String> kinds,
        Instant since,
        Boolean acknowledged,
        List<String> states,
        List<String> feedback,
        Severity minSeverity,
        String tag,
        boolean includeSuppressed,
        Set<String> excludedSources,
        ReadRestriction restriction,
        boolean includeExercise) {

    /** A query without exercise alerts (the shape before prompt 115). */
    public AlertQuery(Set<UUID> ruleIds, List<String> sources, List<String> kinds, Instant since, Boolean acknowledged,
            List<String> states, List<String> feedback, Severity minSeverity, String tag, boolean includeSuppressed,
            Set<String> excludedSources, ReadRestriction restriction) {
        this(ruleIds, sources, kinds, since, acknowledged, states, feedback, minSeverity, tag, includeSuppressed,
                excludedSources, restriction, false);
    }

    /** An unrestricted query (the shape before prompt 035). */
    public AlertQuery(Set<UUID> ruleIds, List<String> sources, List<String> kinds, Instant since, Boolean acknowledged,
            List<String> states, List<String> feedback, Severity minSeverity, String tag, boolean includeSuppressed,
            Set<String> excludedSources) {
        this(ruleIds, sources, kinds, since, acknowledged, states, feedback, minSeverity, tag, includeSuppressed,
                excludedSources, ReadRestriction.NONE);
    }

    /** Copies the collections. */
    public AlertQuery {
        ruleIds = ruleIds == null ? Set.of() : Set.copyOf(ruleIds);
        sources = sources == null ? List.of() : List.copyOf(sources);
        kinds = kinds == null ? List.of() : List.copyOf(kinds);
        states = states == null ? List.of() : List.copyOf(states);
        feedback = feedback == null ? List.of() : List.copyOf(feedback);
        excludedSources = excludedSources == null ? Set.of() : Set.copyOf(excludedSources);
        restriction = restriction == null ? ReadRestriction.NONE : restriction;
    }

    /** This query under {@code r}. */
    public AlertQuery withRestriction(ReadRestriction r) {
        return new AlertQuery(ruleIds, sources, kinds, since, acknowledged, states, feedback, minSeverity, tag,
                includeSuppressed, excludedSources, r, includeExercise);
    }

    /** This query with or without exercise alerts (prompt 115). */
    public AlertQuery withIncludeExercise(boolean include) {
        return new AlertQuery(ruleIds, sources, kinds, since, acknowledged, states, feedback, minSeverity, tag,
                includeSuppressed, excludedSources, restriction, include);
    }

    /** No filter except hiding suppressed alerts. */
    public static AlertQuery all() {
        return new AlertQuery(null, null, null, null, null, null, null, null, null, false, null);
    }

    /** True when {@code alert} passes this filter (the same test as the SQL, for live events). */
    public boolean matches(AlertView alert) {
        if (!ruleIds.isEmpty() && !ruleIds.contains(alert.ruleId())) {
            return false;
        }
        if (!sources.isEmpty() && !sources.contains(alert.sourceId())) {
            return false;
        }
        if (!kinds.isEmpty() && (alert.kind() == null || !kinds.contains(alert.kind()))) {
            return false;
        }
        if (since != null && alert.firedAt().isBefore(since)) {
            return false;
        }
        if (acknowledged != null && acknowledged != (alert.acknowledgedAt() != null)) {
            return false;
        }
        if (!states.isEmpty() && !states.contains(alert.state())) {
            return false;
        }
        if (!feedback.isEmpty() && (alert.feedback() == null || !feedback.contains(alert.feedback()))) {
            return false;
        }
        if (minSeverity != null && alert.severityLevel().compareTo(minSeverity) < 0) {
            return false;
        }
        if (tag != null && !alert.tags().contains(tag)) {
            return false;
        }
        if (!includeSuppressed && alert.suppressed()) {
            return false;
        }
        if (!includeExercise && alert.exercise()) {
            return false;
        }
        if (restriction.alertAudience() != null && !restriction.alertAudience().allows(alert.ownerPrincipalId())) {
            // R2-008: another user's alert.
            return false;
        }
        if (restriction.regionFilter() != null && !restriction.regionFilter().test(regionOf(alert))) {
            // The region filters of prompt 040 read the alert's region snapshot.
            return false;
        }
        return !excludedSources.contains(alert.sourceId())
                && RestrictionClamp.testSnapshot(restriction, alert.sourceId(), alert.kind(), alert.recordSnapshot(),
                        alert.sensitive());
    }

    /** The alert's region snapshot (prompt 040), or null. */
    static ai.foresignal.reference.RegionTags regionOf(AlertView alert) {
        return ai.foresignal.reference.RegionTags.fromJson(alert.region());
    }
}
