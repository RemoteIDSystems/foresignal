package ai.foresignal.trigger;

import ai.foresignal.store.TriggerCondition;
import ai.foresignal.store.TriggerMode;
import ai.foresignal.store.TriggerRuleEntity;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.MultiPolygon;

/**
 * A rule exactly as {@link RuleValidator} accepted and normalized it, ready to be stored.
 *
 * @param name the name
 * @param enabled whether it is evaluated
 * @param sources the watched sources (aliases folded in)
 * @param kinds the watched kinds (aliases folded in)
 * @param tags the tags
 * @param conditions the conditions with normalized values
 * @param geofence the materialized fence, or null
 * @param geofenceRoute the original route line as GeoJSON, or null
 * @param bufferMeters the route half-width, or null
 * @param hysteresisMeters the band (0 without)
 * @param geofenceInner the shrunk fence, or null
 * @param geofenceOuter the grown fence, or null
 * @param near the near clause as given, or null
 * @param mode the edge mode
 * @param onlyNew first versions only
 * @param reappearAsEnter REAPPEAR counts as enter
 * @param cooldown the cooldown
 * @param cooldownScope per entity or per rule
 * @param severity the severity
 * @param enrich prompt 030 enrichment
 * @param enrichRadiusKm the enrichment radius
 * @param enrichWindow the enrichment window
 * @param autoResolve automatic resolution, or null
 * @param maxAlertsPerHour the storm cap, or null for the default
 * @param autoDisableOnStorm disable on storm
 * @param activeFrom start of the active period, or null
 * @param activeUntil end of the active period, or null
 * @param activeWindow recurring windows, or null
 * @param forecastHorizon the forecast horizon, or null
 * @param messageTemplate the template, or null
 * @param warnings non-fatal findings (unknown kinds)
 * @param geofenceRef the region preset used as the geofence (prompt 040), or null
 */
public record ValidatedRule(
        String name,
        boolean enabled,
        List<String> sources,
        List<String> kinds,
        List<String> tags,
        List<TriggerCondition> conditions,
        MultiPolygon geofence,
        Map<String, Object> geofenceRoute,
        Double bufferMeters,
        double hysteresisMeters,
        MultiPolygon geofenceInner,
        MultiPolygon geofenceOuter,
        Map<String, Object> near,
        TriggerMode mode,
        boolean onlyNew,
        boolean reappearAsEnter,
        Duration cooldown,
        CooldownScope cooldownScope,
        Severity severity,
        boolean enrich,
        double enrichRadiusKm,
        Duration enrichWindow,
        AutoResolve autoResolve,
        Integer maxAlertsPerHour,
        boolean autoDisableOnStorm,
        Instant activeFrom,
        Instant activeUntil,
        ActiveWindow activeWindow,
        Duration forecastHorizon,
        String messageTemplate,
        List<String> warnings,
        GeofenceRef geofenceRef) {

    /** A rule without a geofence preset (the shape before prompt 040). */
    public ValidatedRule(String name, boolean enabled, List<String> sources, List<String> kinds, List<String> tags,
            List<TriggerCondition> conditions, MultiPolygon geofence, Map<String, Object> geofenceRoute,
            Double bufferMeters, double hysteresisMeters, MultiPolygon geofenceInner, MultiPolygon geofenceOuter,
            Map<String, Object> near, TriggerMode mode, boolean onlyNew, boolean reappearAsEnter, Duration cooldown,
            CooldownScope cooldownScope, Severity severity, boolean enrich, double enrichRadiusKm,
            Duration enrichWindow, AutoResolve autoResolve, Integer maxAlertsPerHour, boolean autoDisableOnStorm,
            Instant activeFrom, Instant activeUntil, ActiveWindow activeWindow, Duration forecastHorizon,
            String messageTemplate, List<String> warnings) {
        this(name, enabled, sources, kinds, tags, conditions, geofence, geofenceRoute, bufferMeters, hysteresisMeters,
                geofenceInner, geofenceOuter, near, mode, onlyNew, reappearAsEnter, cooldown, cooldownScope, severity,
                enrich, enrichRadiusKm, enrichWindow, autoResolve, maxAlertsPerHour, autoDisableOnStorm, activeFrom,
                activeUntil, activeWindow, forecastHorizon, messageTemplate, warnings, null);
    }

    /** Copies the lists. */
    public ValidatedRule {
        sources = List.copyOf(sources);
        kinds = List.copyOf(kinds);
        tags = List.copyOf(tags);
        conditions = List.copyOf(conditions);
        warnings = List.copyOf(warnings);
    }

    /**
     * Writes every field onto {@code rule} (a full replace). Runtime state that the API does not set
     * (snooze, storm counters, last error, timestamps) is left alone.
     */
    public void applyTo(TriggerRuleEntity rule) {
        rule.setName(name);
        rule.setEnabled(enabled);
        rule.setSources(sources);
        rule.setKinds(kinds);
        rule.setTags(tags);
        rule.setConditions(new ArrayList<>(conditions));
        rule.setGeofence(geofence);
        rule.setGeofenceRoute(geofenceRoute);
        rule.setBufferMeters(bufferMeters);
        rule.setHysteresisMeters(hysteresisMeters);
        rule.setGeofenceInner(geofenceInner);
        rule.setGeofenceOuter(geofenceOuter);
        rule.setNear(near);
        rule.setGeofenceOrigin(geofence == null ? null : near != null ? "near" : "inline");
        rule.setGeofenceRef(geofenceRef == null ? null : new java.util.LinkedHashMap<>(geofenceRef.toMap()));
        rule.setMode(mode);
        rule.setOnlyNew(onlyNew);
        rule.setReappearAsEnter(reappearAsEnter);
        rule.setCooldownSeconds((int) cooldown.toSeconds());
        rule.setCooldownScope(cooldownScope.wire());
        rule.setSeverity(severity.wire());
        rule.setEnrich(enrich);
        rule.setEnrichRadiusKm(enrichRadiusKm);
        rule.setEnrichWindowSeconds(enrichWindow.toSeconds());
        rule.setAutoResolve(autoResolve == null ? null : autoResolve.toStorage());
        rule.setMaxAlertsPerHour(maxAlertsPerHour);
        rule.setAutoDisableOnStorm(autoDisableOnStorm);
        rule.setActiveFrom(activeFrom);
        rule.setActiveUntil(activeUntil);
        rule.setActiveWindow(activeWindow == null ? null : activeWindow.toMap());
        rule.setForecastHorizonSeconds(forecastHorizon == null ? null : forecastHorizon.toSeconds());
        rule.setMessageTemplate(messageTemplate);
    }
}
