package ai.foresignal.trigger;

import ai.foresignal.store.TriggerMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.locationtech.jts.geom.MultiPolygon;

/**
 * A rule compiled for evaluation (prompt 017): the form {@link RuleSqlBuilder}, {@link AlertPass}
 * and {@link TriggerPersistGate} share, built from a stored rule by {@link RuleSpecs}.
 *
 * @param id the rule id
 * @param name the rule name
 * @param enabled whether it is evaluated
 * @param sources the watched sources; empty means any
 * @param kinds the watched kinds; empty means any
 * @param tags tags copied to alerts
 * @param conditions the conditions, ANDed
 * @param fence the materialized geofence, or null
 * @param fenceInner the fence shrunk by the hysteresis band, or null
 * @param fenceOuter the fence grown by the hysteresis band, or null
 * @param hysteresisMeters the band in meters (0 without)
 * @param mode match, enter or exit
 * @param onlyNew fire only on first versions
 * @param reappearAsEnter count REAPPEAR as an enter
 * @param cooldown the cooldown on {@code observed_at} ({@link Duration#ZERO} for none)
 * @param cooldownScope per entity or per rule
 * @param severity the alert severity
 * @param autoResolve automatic resolution, or null for off
 * @param maxAlertsPerHour the storm cap; null follows the app setting, 0 is unlimited
 * @param autoDisableOnStorm disable the rule when the cap is hit
 * @param activeFrom the start of the active period, or null
 * @param activeUntil the end of the active period, or null
 * @param activeWindow recurring local-time windows, or null for always
 * @param forecastHorizon the forecast horizon, or null
 * @param messageTemplate the message template, or null for the default
 * @param snoozedUntil the rule snooze expiry, or null
 * @param enrich whether prompt 030 enriches its alerts
 * @param anchor the moving anchor of a "near me" rule (prompt 036), or null; its fence is the anchor's
 *     current position, so {@code fence} is null
 * @param near the stored {@code near} clause (prompt 036: places with their versions, a point, or an
 *     anchor), for the alert context; null without one
 * @param sensitive true when the rule references a watchlist: its alerts are sensitive (prompt 036)
 */
public record RuleSpec(
        UUID id,
        String name,
        boolean enabled,
        Set<String> sources,
        Set<String> kinds,
        List<String> tags,
        List<RuleCondition> conditions,
        MultiPolygon fence,
        MultiPolygon fenceInner,
        MultiPolygon fenceOuter,
        double hysteresisMeters,
        TriggerMode mode,
        boolean onlyNew,
        boolean reappearAsEnter,
        Duration cooldown,
        CooldownScope cooldownScope,
        Severity severity,
        AutoResolve autoResolve,
        Integer maxAlertsPerHour,
        boolean autoDisableOnStorm,
        Instant activeFrom,
        Instant activeUntil,
        ActiveWindow activeWindow,
        Duration forecastHorizon,
        String messageTemplate,
        Instant snoozedUntil,
        boolean enrich,
        NearAnchor anchor,
        java.util.Map<String, Object> near,
        boolean sensitive) {

    /** A rule without the prompt 036 members (no anchor, no near context, not sensitive). */
    public RuleSpec(UUID id, String name, boolean enabled, Set<String> sources, Set<String> kinds, List<String> tags,
            List<RuleCondition> conditions, MultiPolygon fence, MultiPolygon fenceInner, MultiPolygon fenceOuter,
            double hysteresisMeters, TriggerMode mode, boolean onlyNew, boolean reappearAsEnter, Duration cooldown,
            CooldownScope cooldownScope, Severity severity, AutoResolve autoResolve, Integer maxAlertsPerHour,
            boolean autoDisableOnStorm, Instant activeFrom, Instant activeUntil, ActiveWindow activeWindow,
            Duration forecastHorizon, String messageTemplate, Instant snoozedUntil, boolean enrich) {
        this(id, name, enabled, sources, kinds, tags, conditions, fence, fenceInner, fenceOuter, hysteresisMeters, mode,
                onlyNew, reappearAsEnter, cooldown, cooldownScope, severity, autoResolve, maxAlertsPerHour,
                autoDisableOnStorm, activeFrom, activeUntil, activeWindow, forecastHorizon, messageTemplate, snoozedUntil,
                enrich, null, null, false);
    }

    /** Copies the collections and fills defaults. */
    public RuleSpec {
        sources = sources == null ? Set.of() : Set.copyOf(sources);
        kinds = kinds == null ? Set.of() : Set.copyOf(kinds);
        tags = tags == null ? List.of() : List.copyOf(tags);
        conditions = conditions == null ? List.of() : List.copyOf(conditions);
        mode = mode == null ? TriggerMode.MATCH : mode;
        cooldown = cooldown == null ? Duration.ZERO : cooldown;
        cooldownScope = cooldownScope == null ? CooldownScope.RECORD : cooldownScope;
        severity = severity == null ? Severity.WARNING : severity;
        near = near == null ? null : java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(near));
    }

    /** True for a moving-anchor ("near me") rule (prompt 036). */
    public boolean anchored() {
        return anchor != null;
    }

    /** This rule with {@code value} as its anchor (tests and tools). */
    public RuleSpec withAnchor(NearAnchor value) {
        return new RuleSpec(id, name, enabled, sources, kinds, tags, conditions, fence, fenceInner, fenceOuter,
                hysteresisMeters, mode, onlyNew, reappearAsEnter, cooldown, cooldownScope, severity, autoResolve,
                maxAlertsPerHour, autoDisableOnStorm, activeFrom, activeUntil, activeWindow, forecastHorizon,
                messageTemplate, snoozedUntil, enrich, value, near, sensitive);
    }

    /** True when the rule has a geofence (a static one; an anchor rule's fence moves). */
    public boolean hasFence() {
        return fence != null;
    }

    /** True when the rule's predicate depends on the geometry (a fence or an anchor). */
    public boolean spatial() {
        return fence != null || anchor != null;
    }

    /**
     * True when edges use the hysteresis band: a positive band, a fence, both band fences, and an
     * edge mode.
     */
    public boolean hysteresis() {
        return hysteresisMeters > 0 && fence != null && fenceInner != null && fenceOuter != null
                && mode != TriggerMode.MATCH;
    }

    /** True when the rule watches {@code sourceId} (or every source). */
    public boolean watchesSource(String sourceId) {
        return sources.isEmpty() || sources.contains(sourceId);
    }

    /** True when the rule watches {@code kind} (or every kind). */
    public boolean watchesKind(String kind) {
        return kinds.isEmpty() || kinds.contains(kind);
    }

    /** True when the rule applies to a version of {@code sourceId} and {@code kind}. */
    public boolean appliesTo(String sourceId, String kind) {
        return watchesSource(sourceId) && watchesKind(kind);
    }

    /**
     * True when the rule is active for a version observed at {@code observedAt}: inside
     * {@code [activeFrom, activeUntil)} and inside the active window when there is one.
     */
    public boolean isActiveAt(Instant observedAt) {
        if (activeFrom != null && observedAt.isBefore(activeFrom)) {
            return false;
        }
        if (activeUntil != null && !observedAt.isBefore(activeUntil)) {
            return false;
        }
        return activeWindow == null || activeWindow.isActive(observedAt);
    }

    /** True when the rule is snoozed at {@code now}. */
    public boolean snoozedAt(Instant now) {
        return snoozedUntil != null && snoozedUntil.isAfter(now);
    }

    /** The template to render, the default when none is set. */
    public String template() {
        return messageTemplate == null || messageTemplate.isBlank() ? MessageTemplate.DEFAULT : messageTemplate;
    }

    /** The effective storm cap: the rule's own, else {@code defaultCap}; 0 means unlimited. */
    public int stormCap(int defaultCap) {
        return maxAlertsPerHour != null ? maxAlertsPerHour : defaultCap;
    }
}
