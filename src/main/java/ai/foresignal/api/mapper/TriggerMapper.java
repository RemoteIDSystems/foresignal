package ai.foresignal.api.mapper;

import ai.foresignal.settings.Durations;
import ai.foresignal.source.formats.geojson.GeoJsonWriter;
import ai.foresignal.store.TriggerCondition;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.trigger.ActiveWindow;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AutoResolve;
import ai.foresignal.trigger.MessageTemplate;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Maps stored trigger rules to the {@code TriggerRule} schema (prompt 017). */
@Component
public class TriggerMapper {

    private final GeoJsonWriter geoJson = new GeoJsonWriter();

    /** The {@code TriggerRule} object of {@code view}. */
    public Map<String, Object> rule(TriggerRuleService.RuleView view) {
        TriggerRuleEntity r = view.rule();
        AlertStore.RuleStats stats = view.stats();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", r.getId());
        out.put("name", r.getName());
        out.put("enabled", r.isEnabled());
        out.put("sources", r.getSources());
        out.put("kinds", r.getKinds());
        out.put("tags", r.getTags());
        List<Map<String, Object>> conditions = new ArrayList<>();
        for (TriggerCondition condition : r.getConditions()) {
            Map<String, Object> c = new LinkedHashMap<>();
            if (condition.path() != null) {
                // An identity condition (inWatchlist without a path, prompt 036) has none.
                c.put("path", condition.path());
            }
            c.put("op", condition.op());
            c.put("value", condition.value());
            conditions.add(c);
        }
        out.put("conditions", conditions);
        out.put("geofence", r.getNear() == null ? geoJson.write(r.getGeofence()) : null);
        out.put("geofenceRoute", r.getGeofenceRoute());
        out.put("bufferMeters", r.getBufferMeters());
        out.put("hysteresisMeters", r.getHysteresisMeters());
        out.put("near", near(r.getNear()));
        // A region preset (prompt 040); its geometry is resolved from the reference tables, never stored.
        out.put("geofenceRef", r.getGeofenceRef());
        out.put("mode", r.getMode().wireValue());
        out.put("onlyNew", r.isOnlyNew());
        out.put("reappearAsEnter", r.isReappearAsEnter());
        out.put("cooldown", Durations.format(Duration.ofSeconds(r.getCooldownSeconds())));
        out.put("cooldownScope", r.getCooldownScope());
        out.put("severity", r.getSeverity());
        out.put("enrich", r.isEnrich());
        out.put("enrichRadiusKm", r.getEnrichRadiusKm());
        out.put("enrichWindow", Durations.format(Duration.ofSeconds(r.getEnrichWindowSeconds())));
        AutoResolve auto = AutoResolve.fromStorage(r.getAutoResolve());
        out.put("autoResolve", auto == null ? null : auto.toApi());
        out.put("maxAlertsPerHour", r.getMaxAlertsPerHour());
        out.put("autoDisableOnStorm", r.isAutoDisableOnStorm());
        out.put("activeFrom", r.getActiveFrom());
        out.put("activeUntil", r.getActiveUntil());
        ActiveWindow window = ActiveWindow.fromMap(r.getActiveWindow());
        out.put("activeWindow", window == null ? null : window.toMap());
        out.put("forecastHorizon", r.getForecastHorizonSeconds() == null
                ? null : Durations.format(Duration.ofSeconds(r.getForecastHorizonSeconds())));
        out.put("messageTemplate", r.getMessageTemplate() == null ? MessageTemplate.DEFAULT : r.getMessageTemplate());
        out.put("snoozedUntil", r.getSnoozedUntil());
        out.put("mutedUntil", r.getMutedUntil());
        out.put("escalationPolicyId", r.getEscalationPolicyId());
        out.put("createdAt", r.getCreatedAt());
        out.put("updatedAt", r.getUpdatedAt());
        out.put("lastFiredAt", stats.lastFiredAt());
        out.put("alertCount", stats.alertCount());
        out.put("openAlertCount", stats.openAlertCount());
        out.put("suppressedCount", r.getSuppressedCount());
        out.put("lastStormAt", r.getLastStormAt());
        out.put("feedbackCounts", stats.feedbackCounts());
        out.put("lastError", r.getLastError());
        out.put("lastErrorAt", r.getLastErrorAt());
        return out;
    }

    /**
     * The {@code Near} object of a stored near clause: the stored form without the internal
     * {@code placeVersions} (prompt 036), which the alert context reports instead.
     */
    static java.util.Map<String, Object> near(java.util.Map<String, Object> stored) {
        if (stored == null || !stored.containsKey(ai.foresignal.trigger.RuleValidator.PLACE_VERSIONS)) {
            return stored;
        }
        java.util.Map<String, Object> out = new java.util.LinkedHashMap<>(stored);
        out.remove(ai.foresignal.trigger.RuleValidator.PLACE_VERSIONS);
        return out;
    }
}
