package ai.foresignal.api;

import ai.foresignal.api.dto.SeriesInfoDto;
import ai.foresignal.api.dto.SeriesPageDto;
import ai.foresignal.api.dto.SeriesPointDto;
import ai.foresignal.api.dto.SeriesPointPageDto;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.source.SourceRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Energy and markets time series for clients (OpenAPI tag {@code Series}, prompt 093).
 *
 * <p>{@code GET /v1/series} lists the catalogue from {@code series-latest} records;
 * {@code GET /v1/series/{sourceId}/{seriesId}} returns one series' points in {@code periodStart} order, as JSON or
 * CSV. Both are {@code read} scope, open while {@code security.openReads} is on.
 *
 * <p>Both apply the caller's visibility like the record reads (prompt 035): a private source the caller may not
 * see is answered {@code 404} when named and is absent from the catalog, and the caller's restriction is applied
 * through {@link SeriesQueryService.Visibility}. A source {@code EgressPolicy} refuses to send the caller on
 * {@code API_READ} (a {@code LOCAL_ONLY} source for a caller not allowed local egress, EGR-2) is excluded too
 * ({@link SeriesEgress}).
 */
@RestController
@Tag(name = "Series")
public class SeriesController {

    /** Default page size. */
    static final int DEFAULT_LIMIT = 100;

    /** Largest catalogue page. */
    static final int MAX_LIMIT = 500;

    /** Default points page. */
    static final int DEFAULT_POINTS_LIMIT = 1000;

    /** Largest points page. */
    static final int MAX_POINTS_LIMIT = 5000;

    /** The CSV header. */
    static final String CSV_HEADER = "period,periodStart,periodEnd,value,unit,status";

    private final SeriesQueryService series;
    private final SourceRegistry registry;
    private final ReadCallerResolver callers;
    private final SeriesEgress egress;

    /** Creates the controller. */
    public SeriesController(SeriesQueryService series, SourceRegistry registry, ReadCallerResolver callers,
            SeriesEgress egress) {
        this.series = series;
        this.registry = registry;
        this.callers = callers;
        this.egress = egress;
    }

    /**
     * The caller's visibility, after checking that every named source exists and is not one of its hidden private
     * sources (those are answered like unknown sources, 404).
     */
    private SeriesQueryService.Visibility visibility(HttpServletRequest request, List<String> named) {
        ReadCaller caller = callers.resolveScoped(request);
        Set<String> hidden = callers.hiddenSources(caller);
        for (String id : named) {
            registry.descriptor(id);
            if (hidden.contains(id)) {
                throw NotFoundException.of("Source", id);
            }
        }
        return egress.visibility(caller, callers.excludedSources(caller), caller.restriction(), EgressPath.API_READ);
    }

    /** {@code listSeries}: the series catalogue from {@code series-latest} records. */
    @RestrictedRead("visibility: excluded sources and RestrictionClamp")
    @GetMapping("/v1/series")
    @Operation(operationId = "listSeries", tags = "Series")
    public SeriesPageDto listSeries(
            @RequestParam(required = false) List<String> source,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit,
            HttpServletRequest request) {
        List<String> sources = QueryParams.multi("source", source);
        SeriesQueryService.Visibility visibility = visibility(request, sources);
        int pageSize = QueryParams.intInRange("limit", limit, 1, MAX_LIMIT, DEFAULT_LIMIT);
        SeriesQueryService.Catalogue page = series.list(sources, q, cursor, pageSize, visibility);
        return new SeriesPageDto(page.items(), page.nextCursor());
    }

    /** {@code getSeriesPoints}: one series' points ordered by {@code periodStart}, as JSON or CSV. */
    @RestrictedRead("visibility: excluded sources and RestrictionClamp")
    @GetMapping("/v1/series/{sourceId}/{seriesId}")
    @Operation(operationId = "getSeriesPoints", tags = "Series")
    public ResponseEntity<?> getSeriesPoints(
            @PathVariable String sourceId,
            @PathVariable String seriesId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String limit,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) String cursor,
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String accept,
            HttpServletRequest request) {
        SeriesQueryService.Visibility visibility = visibility(request, List.of(sourceId));
        Instant fromAt = QueryParams.instant("from", from);
        Instant toAt = QueryParams.instant("to", to);
        if (fromAt != null && toAt != null && !fromAt.isBefore(toAt)) {
            throw ValidationException.forField("to", "must be after from");
        }
        int pageSize = QueryParams.intInRange("limit", limit, 1, MAX_POINTS_LIMIT, DEFAULT_POINTS_LIMIT);
        SeriesInfoDto info = series.info(sourceId, seriesId, visibility)
                .orElseThrow(() -> new NotFoundException(
                        "SERIES_NOT_FOUND: no series '" + seriesId + "' in source '" + sourceId + "'"));
        SeriesPointPageDto page = series.points(sourceId, info, fromAt, toAt, cursor, pageSize, visibility).page();
        boolean csv = "csv".equalsIgnoreCase(format)
                || (format == null && accept != null && accept.toLowerCase().contains("text/csv"));
        if (csv) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                    .body(toCsv(page));
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(page);
    }

    /** The CSV rendering: the fixed header plus one line per point. */
    static String toCsv(SeriesPointPageDto page) {
        StringBuilder out = new StringBuilder(CSV_HEADER).append('\n');
        for (SeriesPointDto point : page.items()) {
            out.append(csv(point.period())).append(',')
                    .append(csv(point.periodStart() == null ? null : point.periodStart().toString())).append(',')
                    .append(csv(point.periodEnd() == null ? null : point.periodEnd().toString())).append(',')
                    .append(point.value() == null ? "" : trim(point.value())).append(',')
                    .append(csv(point.unit())).append(',')
                    .append(csv(point.status())).append('\n');
        }
        return out.toString();
    }

    private static String csv(String value) {
        if (value == null) {
            return "";
        }
        if (value.indexOf(',') >= 0 || value.indexOf('"') >= 0 || value.indexOf('\n') >= 0) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }

    private static String trim(Double value) {
        return value == value.longValue() ? Long.toString(value.longValue()) : value.toString();
    }
}
