package ai.foresignal.system;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Turns host-resource breaches into alerts (prompt R2-014): every 30 s it reads the cached {@link ResourceMonitor}
 * reading and reports the disk and memory levels to the {@link OperationalAlertService}, which publishes
 * {@code system.disk_low} / {@code system.disk_critical} (and the memory equivalents) on entering a level and
 * {@code system.disk_recovered} / {@code system.memory_recovered} on returning to {@code ok}. Off when
 * {@code foresignal.resources.scheduled} is false (tests call {@link #evaluate()}).
 */
@Component
public class ResourceAlertEvaluator {

    private final ResourceMonitor monitor;
    private final OperationalAlertService alerts;
    private final boolean scheduled;

    /**
     * Creates the evaluator.
     *
     * @param monitor the resource monitor
     * @param alerts the operational alert path
     * @param scheduled whether the 30-second tick runs
     */
    public ResourceAlertEvaluator(ResourceMonitor monitor, OperationalAlertService alerts,
            @Value("${foresignal.resources.scheduled:true}") boolean scheduled) {
        this.monitor = monitor;
        this.alerts = alerts;
        this.scheduled = scheduled;
    }

    /** The 30-second tick. */
    @Scheduled(fixedDelay = 30_000, initialDelay = 30_000)
    public void tick() {
        if (scheduled) {
            evaluate();
        }
    }

    /**
     * Reports the current disk and memory levels.
     *
     * @return the reading evaluated
     */
    public ResourceStatus evaluate() {
        ResourceStatus status = monitor.snapshot();
        report("disk", "Disk space", status.disk());
        report("memory", "Memory", status.memory());
        return status;
    }

    private void report(String key, String what, ResourceGauge gauge) {
        alerts.report(key, gauge.status(), (level, previous) -> {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("resource", key);
            data.put("level", level);
            data.put("previous", previous);
            data.put("freeBytes", gauge.freeBytes());
            data.put("totalBytes", gauge.totalBytes());
            data.put("usedPercent", gauge.usedPercent());
            return switch (level) {
                case "critical" -> new OperationalAlertService.Alert("system." + key + "_critical", "critical",
                        what + " is critically low", ResourceMonitor.message(what, gauge), "/v1/system/resources",
                        data);
                case "warn" -> new OperationalAlertService.Alert("system." + key + "_low", "warning",
                        what + " is low", ResourceMonitor.message(what, gauge), "/v1/system/resources", data);
                default -> new OperationalAlertService.Alert("system." + key + "_recovered", "info",
                        what + " has recovered", what + " is back above its warning threshold.",
                        "/v1/system/resources", data);
            };
        });
    }
}
