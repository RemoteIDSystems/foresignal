package ai.foresignal.api;

import ai.foresignal.analytics.AnalyticsAccess;
import ai.foresignal.analytics.series.SeriesReader;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.outlook.index.IndexFitService;
import ai.foresignal.outlook.index.IndexViews;
import ai.foresignal.outlook.index.RegionalIndexDefEntity;
import ai.foresignal.outlook.index.RegionalIndexService;
import ai.foresignal.outlook.regional.OutlookViews;
import ai.foresignal.outlook.regional.RegionalOutlookEntity;
import ai.foresignal.outlook.regional.RegionalOutlookService;
import ai.foresignal.outlook.series.SeriesForecastEntity;
import ai.foresignal.outlook.series.SeriesForecastService;
import ai.foresignal.outlook.series.SeriesForecastViews;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.restrict.RestrictedRead;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Series forecasts, regional outlooks and regional indices (prompt 126; OpenAPI tag {@code Outlooks}). Reads are
 * {@code read}; defining an index, fitting its weights and applying weights are {@code agent} ({@code AccessPolicy}).
 * A forecast of a {@code LOCAL_ONLY} series is served in standardized units, and its model without fitted parameters,
 * to callers the {@code EgressPolicy} refuses raw values to (EGR-2). Outlooks and indices are derived counts and
 * composites with no record content.
 */
@RestController
@Tag(name = "Outlooks")
public class OutlookController {

    private final SeriesForecastService series;
    private final RegionalOutlookService outlooks;
    private final RegionalIndexService indices;
    private final IndexFitService fits;
    private final SeriesReader reader;
    private final AnalyticsAccess access;
    private final ReadCallerResolver callers;

    /** Creates the controller. */
    public OutlookController(SeriesForecastService series, RegionalOutlookService outlooks,
            RegionalIndexService indices, IndexFitService fits, SeriesReader reader, AnalyticsAccess access,
            ReadCallerResolver callers) {
        this.series = series;
        this.outlooks = outlooks;
        this.indices = indices;
        this.fits = fits;
        this.reader = reader;
        this.access = access;
        this.callers = callers;
    }

    // ------------------------------------------------------------------------- series forecasts

    /** {@code getSeriesForecast}: the latest quantile forecasts; 409 before a method is selected. */
    @GetMapping("/v1/analytics/series/forecast")
    @RestrictedRead("series carry no geometry: spatially restricted callers and hidden sources get 404")
    @Operation(operationId = "getSeriesForecast", tags = "Outlooks")
    public Map<String, Object> getSeriesForecast(@RequestParam(required = false) String ref,
            @RequestParam(required = false) String horizon, HttpServletRequest request) {
        SeriesRef parsed = SeriesRef.parse(ref);
        Integer h = horizon(horizon);
        boolean withheld = withheld(parsed, request);
        List<SeriesForecastEntity> rows = series.latest(parsed.format());
        boolean standardized = withheld && rows.get(0).isLocalOnly();
        return SeriesForecastViews.forecast(rows, h, standardized, series.questionIds(parsed.format()));
    }

    /** {@code getSeriesForecastModel}: the selected method and its backtest. */
    @GetMapping("/v1/analytics/series/forecast/model")
    @RestrictedRead("series carry no geometry: spatially restricted callers and hidden sources get 404")
    @Operation(operationId = "getSeriesForecastModel", tags = "Outlooks")
    public Map<String, Object> getSeriesForecastModel(@RequestParam(required = false) String ref,
            HttpServletRequest request) {
        SeriesRef parsed = SeriesRef.parse(ref);
        boolean withheld = withheld(parsed, request);
        return SeriesForecastViews.model(series.model(parsed.format()), withheld);
    }

    /** True when the caller may read the series but not its raw values (404 when not at all). */
    private boolean withheld(SeriesRef ref, HttpServletRequest request) {
        ReadCaller caller = callers.resolveScoped(request);
        SeriesReader.Meta meta = reader.meta(ref);
        return access.checkSeries(ref, meta, caller, callers.hiddenSources(caller), caller.restriction(),
                EgressPath.API_READ);
    }

    /** Parses {@code horizon} (1, 7 or 28), or null. */
    static Integer horizon(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String value = QueryParams.oneOf("horizon", raw.strip(), List.of("1", "7", "28"), null);
        return Integer.valueOf(value);
    }

    // ------------------------------------------------------------------------- outlooks

    /** {@code listRegionalOutlooks}: the latest outlook per region and family. */
    @GetMapping("/v1/outlooks")
    @RestrictedRead("outlooks are derived counts with no record content")
    @Operation(operationId = "listRegionalOutlooks", tags = "Outlooks")
    public Map<String, Object> listRegionalOutlooks(@RequestParam(required = false) String family,
            @RequestParam(required = false) String region, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        String regionFilter = region == null || region.isBlank() ? null : region(region);
        String familyFilter = family == null || family.isBlank() ? null : family.strip();
        RegionalOutlookService.Page page = outlooks.list(familyFilter, regionFilter, cursor,
                QueryParams.intInRange("limit", limit, 1, 500, 100));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", page.items().stream().map(o -> OutlookViews.outlook(o, null)).toList());
        out.put("nextCursor", page.nextCursor());
        return out;
    }

    /** {@code getRegionalOutlook}: the six windows, past windows against actuals, and skill. */
    @GetMapping("/v1/outlooks/{region}/{family}")
    @RestrictedRead("outlooks are derived counts with no record content")
    @Operation(operationId = "getRegionalOutlook", tags = "Outlooks")
    public Map<String, Object> getRegionalOutlook(@PathVariable String region, @PathVariable String family) {
        RegionalOutlookEntity o = outlooks.latest(region, family);
        Map<String, Object> out = OutlookViews.outlook(o, outlooks.history(region, family, 104));
        out.put("skill", outlooks.skill(region, family));
        return out;
    }

    static String region(String raw) {
        String code = raw.strip();
        if (!code.matches("^[A-Z]{2}(-[A-Z0-9]{1,3})?$")) {
            throw ValidationException.forField("region", "'" + code + "' is not a 040 region code");
        }
        return code;
    }

    // ------------------------------------------------------------------------- indices

    /** {@code listRegionalIndices}. */
    @GetMapping("/v1/indices")
    @RestrictedRead("index definitions hold references and weights, no record content")
    @Operation(operationId = "listRegionalIndices", tags = "Outlooks")
    public Map<String, Object> listRegionalIndices(@RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        RegionalIndexService.Page page = indices.list(cursor, QueryParams.intInRange("limit", limit, 1, 500, 100));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", page.items().stream().map(d -> IndexViews.def(d, indices.ready(d))).toList());
        out.put("nextCursor", page.nextCursor());
        return out;
    }

    /** {@code getRegionalIndex}: the definition, skill, weights origin and values with their decomposition. */
    @GetMapping("/v1/indices/{indexId}")
    @RestrictedRead("index values are composites with no record content")
    @Operation(operationId = "getRegionalIndex", tags = "Outlooks")
    public Map<String, Object> getRegionalIndex(@PathVariable String indexId,
            @RequestParam(required = false) String region, @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        RegionalIndexDefEntity def = indices.get(indexId);
        String regionFilter = region == null || region.isBlank() ? null : region(region);
        LocalDate fromDay = date("from", from);
        LocalDate toDay = date("to", to);
        return IndexViews.detail(def, indices.ready(def), regionFilter, fromDay, toDay, indices.values(indexId,
                regionFilter, fromDay, toDay), indices.latestFit(indexId));
    }

    /** {@code putRegionalIndex}: 201 when created, 200 when replaced. */
    @PutMapping("/v1/indices/{indexId}")
    @Operation(operationId = "putRegionalIndex", tags = "Outlooks")
    @ApiResponse(responseCode = "200", description = "Updated")
    @ApiResponse(responseCode = "201", description = "Created")
    public ResponseEntity<Map<String, Object>> putRegionalIndex(@PathVariable String indexId,
            @RequestBody JsonNode body, HttpServletRequest request) {
        RegionalIndexService.Put put = indices.put(indexId, body, actor(request));
        return ResponseEntity.status(put.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(IndexViews.def(put.def(), indices.ready(put.def())));
    }

    /** {@code fitRegionalIndex}: 202 with the queued fit (a 103 job). */
    @PostMapping("/v1/indices/{indexId}/fit")
    @Operation(operationId = "fitRegionalIndex", tags = "Outlooks")
    @ApiResponse(responseCode = "202", description = "Fit accepted")
    public ResponseEntity<Map<String, Object>> fitRegionalIndex(@PathVariable String indexId,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(IndexViews.fit(fits.submit(indexId,
                tokenId(request))));
    }

    /** {@code applyRegionalIndexWeights}: logged; bumps {@code weightsVersion}. */
    @PostMapping("/v1/indices/{indexId}/weights")
    @Operation(operationId = "applyRegionalIndexWeights", tags = "Outlooks")
    public Map<String, Object> applyRegionalIndexWeights(@PathVariable String indexId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        RegionalIndexDefEntity def = indices.applyWeights(indexId, body, actor(request));
        return IndexViews.def(def, indices.ready(def));
    }

    /** Parses an optional {@code YYYY-MM-DD} parameter (400 when malformed). */
    public static LocalDate date(String name, String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw.strip());
        } catch (DateTimeParseException e) {
            throw ValidationException.forField(name, "must be a date (YYYY-MM-DD)");
        }
    }

    private static String tokenId(HttpServletRequest request) {
        return CurrentToken.of(request).map(t -> t.id().toString()).orElse(null);
    }

    private static String actor(HttpServletRequest request) {
        return CurrentToken.of(request).map(t -> t.id().toString()).orElse("operator");
    }
}
