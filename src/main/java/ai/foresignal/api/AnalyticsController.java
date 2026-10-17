package ai.foresignal.api;

import ai.foresignal.analytics.AnalyticsAccess;
import ai.foresignal.analytics.eventstudy.EventStudyService;
import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.analytics.finding.FindingSeverities;
import ai.foresignal.analytics.finding.FindingViews;
import ai.foresignal.analytics.registry.AnalysisSeriesService;
import ai.foresignal.analytics.series.SeriesData;
import ai.foresignal.analytics.series.SeriesReader;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.backtest.BacktestJobEntity;
import ai.foresignal.backtest.BacktestViews;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.restrict.RestrictedRead;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The discovery API (prompt 119; OpenAPI tag {@code Analytics}): series data through the {@link SeriesReader}, the
 * analysis-series registry, analytic findings and batch event studies. Reads are {@code read}; registry changes,
 * dismissals and event studies {@code agent} ({@code AccessPolicy}). Private findings are visible only with private
 * access ({@link IntelVisibility}, EGR-1); raw values of {@code LOCAL_ONLY} series are withheld from callers the
 * {@code EgressPolicy} refuses them to (EGR-2).
 */
@RestController
@Tag(name = "Analytics")
public class AnalyticsController {

    private final SeriesReader reader;
    private final AnalysisSeriesService registry;
    private final FindingService findings;
    private final EventStudyService eventStudies;
    private final AnalyticsAccess access;
    private final ReadCallerResolver callers;
    private final IntelVisibility visibility;

    /** Creates the controller. */
    public AnalyticsController(SeriesReader reader, AnalysisSeriesService registry, FindingService findings,
            EventStudyService eventStudies, AnalyticsAccess access, ReadCallerResolver callers,
            IntelVisibility visibility) {
        this.reader = reader;
        this.registry = registry;
        this.findings = findings;
        this.eventStudies = eventStudies;
        this.access = access;
        this.callers = callers;
        this.visibility = visibility;
    }

    /** {@code getAnalyticsSeriesData}: points with {@code knownAt}, coverage flags and gaps. */
    @GetMapping("/v1/analytics/series/data")
    @RestrictedRead("series carry no geometry: spatially restricted callers and hidden sources get 404")
    @Operation(operationId = "getAnalyticsSeriesData", tags = "Analytics")
    public Map<String, Object> getAnalyticsSeriesData(@RequestParam(required = false) String ref,
            @RequestParam(required = false) String from, @RequestParam(required = false) String to,
            @RequestParam(required = false) String asOf, HttpServletRequest request) {
        SeriesRef parsed = SeriesRef.parse(ref);
        ReadCaller caller = callers.resolveScoped(request);
        SeriesReader.Meta meta = reader.meta(parsed);
        boolean withheld = access.checkSeries(parsed, meta, caller, callers.hiddenSources(caller), caller.restriction(),
                EgressPath.API_READ);
        SeriesData data = reader.read(parsed, QueryParams.instant("from", from), QueryParams.instant("to", to),
                QueryParams.instant("asOf", asOf));
        return data.view(withheld);
    }

    /** {@code listAnalysisSeries}. */
    @GetMapping("/v1/analytics/series")
    @RestrictedRead("registry rows hold references and settings, no record content")
    @Operation(operationId = "listAnalysisSeries", tags = "Analytics")
    public Map<String, Object> listAnalysisSeries(@RequestParam(required = false) List<String> family,
            @RequestParam(required = false) List<String> region, @RequestParam(required = false) String enabled,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit) {
        List<String> regions = QueryParams.multi("region", region);
        for (String code : regions) {
            if (!code.matches("^[A-Z]{2}(-[A-Z0-9]{1,3})?$")) {
                throw ValidationException.forField("region", "'" + code + "' is not a 040 region code");
            }
        }
        Boolean enabledFilter = enabled == null || enabled.isBlank() ? null : QueryParams.bool("enabled", enabled,
                true);
        return AnalysisSeriesService.view(registry.list(QueryParams.multi("family", family), regions, enabledFilter,
                cursor, QueryParams.intInRange("limit", limit, 1, 500, 100)));
    }

    /** {@code upsertAnalysisSeries}: 201 when created, 200 when replaced. */
    @PutMapping("/v1/analytics/series")
    @Operation(operationId = "upsertAnalysisSeries", tags = "Analytics")
    public ResponseEntity<Map<String, Object>> upsertAnalysisSeries(@RequestBody JsonNode body) {
        AnalysisSeriesService.Upsert upsert = registry.upsert(body);
        return ResponseEntity.status(upsert.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(AnalysisSeriesService.view(upsert.row()));
    }

    /** {@code deleteAnalysisSeries}: 204; existing findings are kept. */
    @DeleteMapping("/v1/analytics/series")
    @Operation(operationId = "deleteAnalysisSeries", tags = "Analytics")
    public ResponseEntity<Void> deleteAnalysisSeries(@RequestParam(required = false) String ref) {
        registry.delete(ref);
        return ResponseEntity.noContent().build();
    }

    /** {@code listFindings}: newest first. */
    @GetMapping("/v1/analytics/findings")
    @RestrictedRead("private findings need private access; findings carry no record content")
    @Operation(operationId = "listFindings", tags = "Analytics")
    public Map<String, Object> listFindings(@RequestParam(required = false) List<String> type,
            @RequestParam(required = false) List<String> severity, @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) List<String> region, @RequestParam(required = false) String ref,
            @RequestParam(required = false) String since, @RequestParam(required = false) String situationId,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit,
            HttpServletRequest request) {
        List<String> types = List.copyOf(QueryParams.enumSet("type", type, FindingService.TYPES));
        List<String> severities = List.copyOf(QueryParams.enumSet("severity", severity, FindingSeverities.ALL));
        List<String> statuses = List.copyOf(QueryParams.enumSet("status", status, FindingService.STATUSES));
        List<String> regions = QueryParams.multi("region", region);
        for (String code : regions) {
            if (!code.matches("^[A-Z]{2}(-[A-Z0-9]{1,3})?$")) {
                throw ValidationException.forField("region", "'" + code + "' is not a 040 region code");
            }
        }
        String refText = ref == null || ref.isBlank() ? null : SeriesRef.parse(ref).format();
        ReadCaller caller = callers.resolveScoped(request);
        boolean seesPrivate = visibility.seesPrivate(request);
        FindingService.Page page = findings.list(new FindingService.ListQuery(types, severities, statuses, regions,
                refText, QueryParams.instant("since", since), situationId == null || situationId.isBlank() ? null
                        : situationId, cursor, QueryParams.intInRange("limit", limit, 1, 500, 100)), seesPrivate);
        return FindingViews.page(page, f -> view(f, caller));
    }

    /** {@code getFinding}: evidence, inference and caveats. */
    @GetMapping("/v1/analytics/findings/{findingId}")
    @RestrictedRead("private findings need private access; findings carry no record content")
    @Operation(operationId = "getFinding", tags = "Analytics")
    public Map<String, Object> getFinding(@PathVariable String findingId, HttpServletRequest request) {
        ReadCaller caller = callers.resolveScoped(request);
        return view(findings.visible(findingId, visibility.seesPrivate(request)), caller);
    }

    /** {@code updateFinding}: dismiss with a reason (merge patch). */
    @PatchMapping("/v1/analytics/findings/{findingId}")
    @Operation(operationId = "updateFinding", tags = "Analytics")
    public Map<String, Object> updateFinding(@PathVariable String findingId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a FindingPatch object.");
        }
        for (String field : body.propertyNames()) {
            if (!Set.of("status", "reason").contains(field)) {
                throw ValidationException.forField(field, "is not a FindingPatch member");
            }
        }
        if (!body.hasNonNull("status") || !"dismissed".equals(body.get("status").asString())) {
            throw ValidationException.forField("status", "must be dismissed");
        }
        String reason = body.hasNonNull("reason") ? body.get("reason").asString() : null;
        String actor = CurrentToken.of(request).map(t -> t.id().toString()).orElse("operator");
        AnalyticFindingEntity f = findings.dismiss(findingId, reason, actor, visibility.seesPrivate(request));
        return view(f, callers.resolveScoped(request));
    }

    /** {@code createEventStudy}: 202 with the 103 job. */
    @PostMapping("/v1/analytics/event-studies")
    @Operation(operationId = "createEventStudy", tags = "Analytics")
    public ResponseEntity<Map<String, Object>> createEventStudy(@RequestBody JsonNode body,
            HttpServletRequest request) {
        String tokenId = CurrentToken.of(request).map(t -> t.id().toString()).orElse(null);
        BacktestJobEntity job = eventStudies.submit(body, tokenId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(BacktestViews.job(job));
    }

    private Map<String, Object> view(AnalyticFindingEntity f, ReadCaller caller) {
        return FindingViews.full(f, findings.linkedSituation(f).orElse(null),
                access.withheld(f, caller, EgressPath.API_READ));
    }
}
