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
import ai.foresignal.stats.RegionStatsService;
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
 * {@code country}, {@code admin1} and {@code county} (prompt 040) match through {@code record_region}
 * (versions) and the alert region snapshot (alerts), and are answered from raw rows. {@code region=},
 * {@code groupBy=region} and {@code by=region} (prompt 119) are answered from the region rollups by
 * {@link RegionStatsService}; they combine with source, kind, measure (not alerts) and bucket, and not with
 * {@code bbox}, {@code place}, {@code country}, {@code admin1}, {@code county} or another time axis. The rebuild is
 * {@code admin}.
 */
@RestController
@Tag(name = "Stats")
public class StatsController {

    /** The GeoJSON media type of grids. */
    static final MediaType GEO_JSON = MediaType.parseMediaType("application/geo+json");

    /** The region rollup filter of prompt 119 (refused on the grid). */
    static final List<String> REGION_PARAMS = List.of("region");

    /** The parameters a region-rollup read cannot honor. */
    static final List<String> NOT_WITH_REGIONS = List.of("bbox", "place", "country", "admin1", "county");

    private final StatsService stats;
    private final RollupJob rollup;
    private final ReadCallerResolver callers;

    /** Validates the 040 region filters; null only in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.reference.RegionCodes regionCodes;
    private final SourceRegistry registry;

    /** The region rollup reads (prompt 119); null only in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private RegionStatsService regionStats;

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
            @RequestParam(required = false) List<String> region,
            HttpServletRequest request) {
        ReadCaller caller = callers.resolveScoped(request);
        List<String> sources = known(QueryParams.multi("source", source), callers.hiddenSources(caller));
        String group = groupBy == null || groupBy.isBlank() ? "none" : groupBy.strip();
        List<String> regions = RegionStatsService.validRegions(QueryParams.multi("region", region));
        if (regionStats != null && ("region".equals(group) || !regions.isEmpty())) {
            regionOnly(request, measure, timeField);
            group = QueryParams.oneOf("groupBy", group, RegionStatsService.GROUP_BY_VALUES, "none");
            return json(regionStats.timeseries(sources, QueryParams.multi("kind", kind), regions,
                    required("from", from), required("to", to), bucket, StatsMeasure.parse(measure), group,
                    callers.excludedSources(caller), caller.restriction()).toMap(), caller);
        }
        group = QueryParams.oneOf("groupBy", group, StatsService.GROUP_BY_VALUES, "none");
        return json(stats.timeseries(sources, QueryParams.multi("kind", kind), BboxParser.parse(bbox),
                required("from", from), required("to", to), bucket, StatsMeasure.parse(measure), group,
                TimeField.parse(timeField, TimeField.INGESTED), callers.excludedSources(caller),
                restriction(request, caller)).toMap(), caller);
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
                callers.excludedSources(caller), restriction(request, caller)).toMap();
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
            @RequestParam(required = false) List<String> region,
            HttpServletRequest request) {
        ReadCaller caller = callers.resolveScoped(request);
        List<String> sources = known(QueryParams.multi("source", source), callers.hiddenSources(caller));
        int size = QueryParams.intInRange("limit", limit, 1, StatsService.MAX_TOP, 10);
        List<String> regions = RegionStatsService.validRegions(QueryParams.multi("region", region));
        String requestedBy = by == null ? null : by.strip();
        if (regionStats != null && ("region".equals(requestedBy) || !regions.isEmpty())) {
            regionOnly(request, measure, null);
            String regionBy = QueryParams.oneOf("by", requestedBy, List.of("source", "kind", "region"), "region");
            return json(regionStats.top(regionBy, StatsMeasure.parse(measure), required("from", from),
                    required("to", to), size, sources, QueryParams.multi("kind", kind), regions,
                    callers.excludedSources(caller), caller.restriction()).toMap(), caller);
        }
        String byValue = QueryParams.oneOf("by", by, List.of("source", "kind", "cell", "entity"), "source");
        return json(stats.top(byValue, StatsMeasure.parse(measure), required("from", from), required("to", to), size,
                sources, QueryParams.multi("kind", kind), callers.excludedSources(caller),
                restriction(request, caller)).toMap(), caller);
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

    /** 400 for the region rollup filter on the grid, which is keyed by H3 cell only. */
    static void refuseRegions(HttpServletRequest request) {
        for (String name : REGION_PARAMS) {
            String value = request.getParameter(name);
            if (value != null && !value.isBlank()) {
                throw ValidationException.forField(name, "the grid is keyed by H3 cell; use country, admin1 or county,"
                        + " or /v1/stats/timeseries with region");
            }
        }
    }

    /** 400 for parameters a region-rollup read cannot honor (prompt 119). */
    static void regionOnly(HttpServletRequest request, String measure, String timeField) {
        for (String name : NOT_WITH_REGIONS) {
            String value = request.getParameter(name);
            if (value != null && !value.isBlank()) {
                throw ValidationException.forField(name, "cannot be combined with region rollups (region, groupBy=region"
                        + " or by=region)");
            }
        }
        if (measure != null && "alerts".equalsIgnoreCase(measure.strip())) {
            throw ValidationException.forField("measure", "alerts are not rolled up by region");
        }
        if (timeField != null && !timeField.isBlank() && !"ingested".equalsIgnoreCase(timeField.strip())) {
            throw ValidationException.forField("timeField", "region rollups are on the ingested axis only");
        }
    }

    /** The caller's restriction with the request's 040 region filters ({@code 400 UNKNOWN_REGION}). */
    private ai.foresignal.security.restrict.ReadRestriction restriction(HttpServletRequest request,
            ReadCaller caller) {
        ai.foresignal.reference.RegionFilter region = regionCodes == null ? null : regionCodes.filter(
                values(request, "country"), values(request, "admin1"), values(request, "county"));
        return region == null ? caller.restriction() : caller.restriction().withRegionFilter(region);
    }

    private static List<String> values(HttpServletRequest request, String name) {
        String[] values = request.getParameterValues(name);
        return values == null ? List.of() : List.of(values);
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
