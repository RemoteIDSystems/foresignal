package ai.foresignal.api;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.stats.metrics.MetricProjection;
import ai.foresignal.stats.metrics.MetricSettings;
import ai.foresignal.stats.metrics.MetricSettingsService;
import ai.foresignal.stats.metrics.ProjectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Metric projections (prompt 038): {@code getMetrics} ({@code read}; Prometheus sends
 * {@code Authorization: Bearer}), and {@code getMetricSettings} / {@code updateMetricSettings}
 * ({@code admin}, under {@code /v1/settings/**}).
 *
 * <p>{@code /v1/metrics} is not the open actuator endpoint: it needs the {@code read} scope (open only
 * while {@code security.openReads} is on). A caller sees the projections of the sources it may read:
 * private sources follow the 023 gate (scrape them with an {@code agent} token), sources and kinds
 * outside a restricted token's restriction are left out, and a token restricted by area or a request
 * filter sees none, because a gauge carries no geometry to judge.
 */
@RestController
@Tag(name = "Stats")
public class MetricsController {

    private final ProjectionService projections;
    private final MetricSettingsService settings;
    private final ReadCallerResolver callers;

    /** Creates the controller. */
    public MetricsController(ProjectionService projections, MetricSettingsService settings, ReadCallerResolver callers) {
        this.projections = projections;
        this.settings = settings;
        this.callers = callers;
    }

    /** {@code getMetrics}: the exposition, text format 0.0.4 or OpenMetrics by {@code Accept}. */
    @GetMapping("/v1/metrics")
    @Operation(operationId = "getMetrics", tags = "Stats")
    public ResponseEntity<String> getMetrics(
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String accept,
            HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        Set<String> excluded = callers.excludedSources(caller);
        ReadRestriction restriction = caller.restriction();
        ProjectionService.Exposition exposition = projections.scrape(accept, p -> visible(p, excluded, restriction));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.parseMediaType(exposition.contentType()))
                .body(exposition.body());
    }

    /** True when a caller excluding {@code excluded} under {@code restriction} may see {@code p}. */
    static boolean visible(MetricProjection p, Set<String> excluded, ReadRestriction restriction) {
        if (excluded.contains(p.source()) || !restriction.allowsSource(p.source())) {
            return false;
        }
        if (restriction.region() != null || restriction.filter() != null) {
            return false;
        }
        if (restriction.kinds() != null) {
            return p.kind() != null && restriction.allowsKind(p.kind());
        }
        return true;
    }

    /** {@code getMetricSettings}. */
    @GetMapping("/v1/settings/metrics")
    @Operation(operationId = "getMetricSettings", tags = "Settings")
    public MetricSettings getMetricSettings() {
        return settings.get();
    }

    /** {@code updateMetricSettings}: validates and replaces the projections; the series are reseeded. */
    @PutMapping("/v1/settings/metrics")
    @Operation(operationId = "updateMetricSettings", tags = "Settings")
    public MetricSettings updateMetricSettings(@RequestBody JsonNode body) {
        return settings.update(body);
    }
}
