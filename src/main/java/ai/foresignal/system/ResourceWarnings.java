package ai.foresignal.system;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * The host-resource warnings of {@code /v1/system.warnings} (prompt R2-014), from the cached {@link ResourceMonitor}
 * reading: {@code DISK_LOW} (warning) and {@code DISK_CRITICAL} (critical), {@code MEMORY_LOW} (warning) and
 * {@code MEMORY_CRITICAL} (critical). Either severity degrades the system status.
 */
@Component
public class ResourceWarnings implements SystemWarningContributor {

    private final ResourceMonitor monitor;

    /**
     * Creates the contributor.
     *
     * @param monitor the resource monitor
     */
    public ResourceWarnings(ResourceMonitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public List<SystemWarnings.Warning> warnings() {
        return of(monitor.snapshot());
    }

    /**
     * The warnings of a reading.
     *
     * @param status the reading
     * @return its warnings, empty when everything is {@code ok}
     */
    static List<SystemWarnings.Warning> of(ResourceStatus status) {
        List<SystemWarnings.Warning> out = new ArrayList<>();
        add(out, "DISK", "Disk space", status.disk());
        add(out, "MEMORY", "Memory", status.memory());
        return out;
    }

    private static void add(List<SystemWarnings.Warning> out, String code, String what, ResourceGauge gauge) {
        if (gauge == null || "ok".equals(gauge.status())) {
            return;
        }
        boolean critical = "critical".equals(gauge.status());
        out.add(new SystemWarnings.Warning(code + (critical ? "_CRITICAL" : "_LOW"), critical ? "critical" : "warning",
                ResourceMonitor.message(what, gauge) + " See GET /v1/system/resources and the thresholds at"
                        + " /v1/settings/thresholds."));
    }
}
