package ai.foresignal.system;

import ai.foresignal.collect.SourceHealthChangedEvent;
import ai.foresignal.settings.AppSettingChangedEvent;
import ai.foresignal.settings.ResourceThresholdsService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.HealthStatus;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Raises {@code system.source_storm} when many sources fail at once (prompt R2-014): it follows every
 * {@link SourceHealthChangedEvent} and, when the number of sources now {@code failing} reaches the admin's
 * {@code sourceStorm.failedSources}, reports a {@code warn} level to the {@link OperationalAlertService};
 * {@code system.source_storm_cleared} follows once the count drops below it. Each single failing source still has its
 * own {@code system.source_failing} notification.
 */
@Component
public class SourceStormMonitor {

    /** The condition key. */
    static final String KEY = "source_storm";

    private final OperationalAlertService alerts;
    private final ResourceThresholdsService thresholds;
    private final Set<String> failing = ConcurrentHashMap.newKeySet();

    /**
     * Creates the monitor.
     *
     * @param alerts the operational alert path
     * @param thresholds the admin's thresholds
     */
    public SourceStormMonitor(OperationalAlertService alerts, ResourceThresholdsService thresholds) {
        this.alerts = alerts;
        this.thresholds = thresholds;
    }

    /**
     * Follows one source's health.
     *
     * @param event the change
     */
    @EventListener
    public void onSourceHealthChanged(SourceHealthChangedEvent event) {
        if (event.current() == HealthStatus.FAILING) {
            failing.add(event.sourceId());
        } else {
            failing.remove(event.sourceId());
        }
        evaluate();
    }

    /**
     * Re-rates the count when the thresholds change.
     *
     * @param event the change
     */
    @EventListener
    public void onSettingChanged(AppSettingChangedEvent event) {
        if (SettingKeys.THRESHOLDS.equals(event.key())) {
            evaluate();
        }
    }

    /**
     * Reports the storm level for the sources failing now.
     *
     * @return {@code warn} during a storm, else {@code ok}
     */
    public synchronized String evaluate() {
        Integer bound = thresholds.get().sourceStorm().failedSources();
        Set<String> now = new TreeSet<>(failing);
        String level = bound != null && now.size() >= bound ? "warn" : "ok";
        alerts.report(KEY, level, (current, previous) -> {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("failingSources", now.size());
            data.put("threshold", bound);
            data.put("sourceIds", now.stream().toList());
            if ("ok".equals(current)) {
                return new OperationalAlertService.Alert("system.source_storm_cleared", "info",
                        "The source failure storm has cleared",
                        now.size() + " sources are failing now, below the storm threshold.", "/v1/sources", data);
            }
            return new OperationalAlertService.Alert("system.source_storm", "warning",
                    now.size() + " sources are failing",
                    now.size() + " sources are failing at once (threshold " + bound + "). This often means a network,"
                            + " egress or upstream outage rather than one broken source.",
                    "/v1/sources", data);
        });
        return level;
    }

    /** The sources failing now. */
    public Set<String> failing() {
        return Set.copyOf(failing);
    }

    /** Forgets every source (tests). */
    public void reset() {
        failing.clear();
    }
}
