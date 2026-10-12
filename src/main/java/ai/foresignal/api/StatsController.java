package ai.foresignal.api;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxParser;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.api.support.TimeField;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.stats.RollupJob;
import ai.foresignal.stats.StatsMeasure;
import ai.foresignal.stats.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The stats API (prompt 038; OpenAPI tag {@code Stats}): {@code getStatsTimeseries},
 * {@code getStatsGrid}, {@code getStatsTop} and {@code rebuildStatsRollup}.
 *
 * <p>The reads need the {@code read} scope (open while {@code security.openReads} is on), honor the
 * caller's restriction and the {@code place=}/{@code view=}/{@code inWatchlist=} filters (prompts 035,
 * 036) and never count private sources for callers that fail the private gate. The region filters
 * ({@code country}, {@code admin1}, {@code county}, {@code region}, {@code groupBy=region},
 * {@code by=region}) belong to prompts 040 and 119 and are refused until then. The rebuild is
 * {@code admin}.
 */
@RestController
@Tag(name = "Stats")
public class StatsController {

    /** The GeoJSON media type of grids. */
    static final MediaType GEO_JSON = MediaType.parseMediaType("application/geo+json");

    /** The parameters of prompts 040 and 119, refused until then. */
    static final List<String> REGION_PARAMS = List.of("country", "admin1", "county", "region");

    private final StatsService stats;
    private final RollupJob rollup;
    private final ReadCallerResolver callers;
    private final SourceRegistry registry;

    /** Creates the controller. */
    public StatsController(StatsService stats, RollupJob rollup, ReadCallerResolver callers, SourceRegistry registry) {
        this.stats = stats;
        this.rollup = rollup;
        this.callers = callers;
        this.registry = registry;
    }

    /** {@code getStatsTimeseries}. */
    @GetMapping("/v1/stats/timeseries")
    @Operation(operationId = "getStatsTimeseries", tags = "Stats")
    @RestrictedRead("restriction and place= are part of the rollup and raw SQL")
    public ResponseEntity<Object> getStatsTimeseries(
            @RequestParam(required = false) List<String> source,
            @RequestParam(required = false) List<String> kind,
            @RequestParam(required = false) String bbox,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String bucket,
            @RequestParam(required = false) String measure,
            @RequestParam(required = false) String groupBy,
            @RequestParam(required = false) String timeField,
            HttpServletRequest request) {
        refuseRegions(request);
        ReadCaller caller = callers.resolveScoped(request);
        List<String> sources = known(QueryParams.multi("source", source), callers.hiddenSources(caller));
        String group = groupBy == null || groupBy.isBlank() ? "none" : groupBy.strip();
        if ("region".equals(group)) {
            throw ValidationException.forField("groupBy", "groupBy=region arrives with the region rollups (prompt 119)");
        }
        group = QueryParams.oneOf("groupBy", group, StatsService.GROUP_BY_VALUES, "none");
        return json(stats.timeseries(sources, QueryParams.multi("kind", kind), BboxParser.parse(bbox),
                required("from", from), required("to", to), bucket, StatsMeasure.parse(measure), group,
                TimeField.parse(timeField, TimeField.INGESTED), callers.excludedSources(caller), caller.restriction())
                .toMap(), caller);
    }

    /** {@code getStatsGrid}. */
    @GetMapping("/v1/stats/grid")
    @Operation(operationId = "getStatsGrid", tags = "Stats")
    @RestrictedRead("restriction and place= are part of the rollup and raw SQL")
    public ResponseEntity<Object> getStatsGrid(
            @RequestParam(required = false) List<String> source,
            @RequestParam(required = false) List<String> kind,
            @RequestParam(required = false) String bbox,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String measure,
            @RequestParam(required = false) String res,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) String sizeKm,
            HttpServletRequest request) {
        refuseRegions(request);
        ReadCaller caller = callers.resolveScoped(request);
        List<String> sources = known(QueryParams.multi("source", source), callers.hiddenSources(caller));
        Integer resolution = res == null || res.isBlank() ? null : QueryParams.intInRange("res", res, 0, 15, 0);
        String modeValue = QueryParams.oneOf("mode", mode, List.of("rollup", "raw"), "rollup");
        Double size = sizeKm == null || sizeKm.isBlank() ? null
                : QueryParams.doubleInRange("sizeKm", sizeKm, 0.001, 10_000);
        Map<String, Object> body = stats.grid(sources, QueryParams.multi("kind", kind), BboxParser.parse(bbox),
                required("from", from), required("to", to), StatsMeasure.parse(measure), resolution, modeValue, size,
                callers.excludedSources(caller), caller.restriction()).toMap();
        return ResponseEntity.ok()
                .headers(ai.foresignal.api.support.ETags.cacheHeaders(new HttpHeaders(), null, caller.publicCache()))
                .contentType(GEO_JSON)
                .body(body);
    }

    /** {@code getStatsTop}. */
    @GetMapping("/v1/stats/top")
    @Operation(operationId = "getStatsTop", tags = "Stats")
    @RestrictedRead("restriction and place= are part of the rollup and raw SQL")
    public ResponseEntity<Object> getStatsTop(
            @RequestParam(required = false) String by,
            @RequestParam(required = false) String measure,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String limit,
            @RequestParam(required = false) List<String> source,
            @RequestParam(required = false) List<String> kind,
            HttpServletRequest request) {
        refuseRegions(request);
        if (by != null && "region".equals(by.strip())) {
            throw ValidationException.forField("by", "by=region arrives with the region rollups (prompt 119)");
        }
        String byValue = QueryParams.oneOf("by", by, List.of("source", "kind", "cell", "entity"), "source");
        ReadCaller caller = callers.resolveScoped(request);
        List<String> sources = known(QueryParams.multi("source", source), callers.hiddenSources(caller));
        int size = QueryParams.intInRange("limit", limit, 1, StatsService.MAX_TOP, 10);
        return json(stats.top(byValue, StatsMeasure.parse(measure), required("from", from), required("to", to), size,
                sources, QueryParams.multi("kind", kind), callers.excludedSources(caller), caller.restriction())
                .toMap(), caller);
    }

    /**
     * {@code rebuildStatsRollup}: recomputes the closed hourly and daily buckets in {@code [from, to)}
     * whose raw versions are still retained (per source), in the background; 202 with the plan.
     */
    @PostMapping("/v1/stats/rollup/rebuild")
    @Operation(operationId = "rebuildStatsRollup", tags = "Stats")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, Object> rebuildStatsRollup(@RequestBody JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a StatsRollupRebuildRequest object.");
        }
        Instant from = required("from", body.hasNonNull("from") ? body.get("from").asString() : null);
        Instant to = required("to", body.hasNonNull("to") ? body.get("to").asString() : null);
        RollupJob.RebuildPlan plan = rollup.rebuild(from, to);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("from", plan.from().toString());
        out.put("to", plan.to().toString());
        out.put("hours", plan.hours().size());
        out.put("days", plan.days().size());
        out.put("status", "queued");
        return out;
    }

    // ------------------------------------------------------------------ helpers

    private static ResponseEntity<Object> json(Map<String, Object> body, ReadCaller caller) {
        return ResponseEntity.ok()
                .headers(ai.foresignal.api.support.ETags.cacheHeaders(new HttpHeaders(), null, caller.publicCache()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    /** 400 for the region filters of prompts 040 and 119. */
    static void refuseRegions(HttpServletRequest request) {
        for (String name : REGION_PARAMS) {
            String value = request.getParameter(name);
            if (value != null && !value.isBlank()) {
                throw ValidationException.forField(name, "region filters arrive with region tagging (prompt 040)"
                        + " and the region rollups (prompt 119)");
            }
        }
    }

    private static Instant required(String name, String raw) {
        Instant value = QueryParams.instant(name, raw);
        if (value == null) {
            throw ValidationException.forField(name, "is required");
        }
        return value;
    }

    private List<String> known(List<String> sources, Set<String> hidden) {
        for (String id : sources) {
            registry.descriptor(id);
            if (hidden.contains(id)) {
                throw NotFoundException.of("Source", id);
            }
        }
        return sources;
    }
}
