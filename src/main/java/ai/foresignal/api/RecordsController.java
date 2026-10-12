package ai.foresignal.api;

import ai.foresignal.api.dto.FeatureCollectionPage;
import ai.foresignal.api.dto.KindSummaryDto;
import ai.foresignal.api.dto.RecordPage;
import ai.foresignal.api.dto.RecordVersionPage;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.mapper.KindSchemaBuilder;
import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.api.support.ApiProperties;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.BboxParser;
import ai.foresignal.api.support.ETags;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.FieldSelector;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.api.support.NearFilter;
import ai.foresignal.api.support.NearParser;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.api.support.ResponseFormat;
import ai.foresignal.api.support.StreamWriter;
import ai.foresignal.api.support.TimeField;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.AsOfQueryService;
import ai.foresignal.store.query.CurrentQuery;
import ai.foresignal.store.query.GeometryEncoding;
import ai.foresignal.store.query.HistoryQuery;
import ai.foresignal.store.query.ListOrder;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.store.query.Slices;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.LiveBus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Objects;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.security.restrict.RestrictionClamp;
import java.util.TreeMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

/**
 * Stored data for clients (OpenAPI tag {@code Records}): current state, one record, versions,
 * history, kinds and the kind schema.
 *
 * <p>Thin: queries live in {@link RecordQueryService} and {@link AsOfQueryService}, mapping in
 * {@link RecordMapper}. This class parses parameters, negotiates the format ({@code ?format=} wins
 * over {@code Accept}), applies conditional GETs and writes streaming formats.
 *
 * <p>Every operation needs the {@code read} scope, which is open while {@code security.openReads}
 * is on. Anonymous callers never see private sources.
 */
@RestController
@Tag(name = "Records")
public class RecordsController {

    /** Default page size of {@code listRecords} and {@code listRecordVersions}. */
    static final int DEFAULT_LIMIT = 500;

    /** Largest page of {@code listRecords} and {@code listRecordVersions}. */
    static final int MAX_LIMIT = 5000;

    /** Default page size of {@code queryHistory}. */
    static final int DEFAULT_HISTORY_LIMIT = 5000;

    /** Largest page of {@code queryHistory}. */
    static final int MAX_HISTORY_LIMIT = 50_000;

    /** The {@code Foresignal-Watermark-Seq} header. */
    public static final String WATERMARK_HEADER = "Foresignal-Watermark-Seq";

    static final List<ResponseFormat> LIST_FORMATS = List.of(
            ResponseFormat.JSON, ResponseFormat.GEOJSON, ResponseFormat.NDJSON, ResponseFormat.GEOJSONSEQ,
            ResponseFormat.CSV);

    static final List<ResponseFormat> RECORD_FORMATS = List.of(ResponseFormat.JSON, ResponseFormat.GEOJSON);

    static final List<String> INCLUDE_VALUES = List.of("observer", "region", "entity");

    private final RecordQueryService queries;
    private final AsOfQueryService asOfQueries;
    private final RecordMapper mapper;
    private final ReadCallerResolver callers;
    private final SourceRegistry registry;
    private final ChangeBus changeBus;
    private final LiveBus liveBus;
    private final ApiProperties properties;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the controller. */
    public RecordsController(
            RecordQueryService queries,
            AsOfQueryService asOfQueries,
            RecordMapper mapper,
            ReadCallerResolver callers,
            SourceRegistry registry,
            ChangeBus changeBus,
            LiveBus liveBus,
            ApiProperties properties,
            JsonMapper json,
            Clock clock) {
        this.queries = queries;
        this.asOfQueries = asOfQueries;
        this.mapper = mapper;
        this.callers = callers;
        this.registry = registry;
        this.changeBus = changeBus;
        this.liveBus = liveBus;
        this.properties = properties;
        this.json = json;
        this.clock = clock;
    }

    // ------------------------------------------------------------------ listRecords

    /**
     * {@code listRecords}: current state from {@code record_head ⋈ record_live}, or transaction-time
     * replay with {@code asOf}.
     *
     * <p><strong>Mirror contract</strong> for offline clients: (1) open the SSE stream with
     * {@code after = watermarkSeq}; (2) page the snapshot with {@code order=id}, whose cursor pins
     * {@code asOfSeq ?? watermarkSeq} from page 1 so entities updated later are left out and arrive
     * through {@code /v1/changes?after=<w>}; (3) apply an event only if its {@code observedAt} is at
     * least the stored one.
     */
    @GetMapping("/v1/records")
    @Operation(operationId = "listRecords", tags = "Records")
    @RestrictedRead
    public ResponseEntity<Object> listRecords(
            @RequestParam(required = false) List<String> source,
            @RequestParam(required = false) List<String> kind,
            @RequestParam(required = false) List<String> externalId,
            @RequestParam(required = false) String bbox,
            @RequestParam(required = false) String near,
            @RequestParam(required = false) String radiusKm,
            @RequestParam(required = false) String since,
            @RequestParam(required = false) String until,
            @RequestParam(required = false) String timeField,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String includeExpired,
            @RequestParam(required = false) String order,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String asOfSeq,
            @RequestParam(required = false) String asOf,
            @RequestParam(required = false) List<String> include,
            @RequestParam(required = false) String fields,
            @RequestParam(required = false) String precision,
            @RequestParam(required = false) String simplify,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit,
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String accept,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {
        return listRecords(ReadCallerResolver.scopeOf(request, ""), source, kind, externalId, bbox, near, radiusKm,
                since, until, timeField, q, includeExpired, order, sort, asOfSeq, asOf, include, fields, precision,
                simplify, format, cursor, limit, accept, ifNoneMatch, request, response);
    }

    /**
     * {@code listRecords} narrowed by {@code scope} (prompt 036): the request's own
     * {@code place}/{@code view}/{@code inWatchlist}, or a saved view's ({@code getViewRecords}).
     */
    public ResponseEntity<Object> listRecords(
            ai.foresignal.security.restrict.ReadScope scope,
            List<String> source,
            List<String> kind,
            List<String> externalId,
            String bbox,
            String near,
            String radiusKm,
            String since,
            String until,
            String timeField,
            String q,
            String includeExpired,
            String order,
            String sort,
            String asOfSeq,
            String asOf,
            List<String> include,
            String fields,
            String precision,
            String simplify,
            String format,
            String cursor,
            String limit,
            String accept,
            String ifNoneMatch,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {
        // place=, view= and inWatchlist= narrow the caller's restriction (prompt 036).
        ReadCaller caller = callers.resolveScoped(request, scope);
        Set<String> excluded = callers.excludedSources(caller);
        ReadRestriction restriction = caller.restriction();
        ResponseFormat fmt = ResponseFormat.negotiate(format, accept, LIST_FORMATS);
        List<String> sources = knownSources(QueryParams.multi("source", source), callers.hiddenSources(caller));
        List<String> kinds = QueryParams.multi("kind", kind);
        List<String> ids = QueryParams.multi("externalId", externalId);
        BboxFilter box = BboxParser.parse(bbox);
        NearFilter nearFilter = NearParser.parse(near, ReadCallerResolver.radiusForNear(request, near, radiusKm));
        Instant sinceAt = QueryParams.instant("since", since);
        Instant untilAt = QueryParams.instant("until", until);
        TimeField axis = TimeField.parse(timeField, TimeField.EFFECTIVE);
        boolean expired = QueryParams.bool("includeExpired", includeExpired, false);
        String orderValue = QueryParams.oneOf("order", order, List.of("observed", "id"), null);
        String sortValue = QueryParams.oneOf("sort", sort, List.of("distance"), null);
        Long pinned = QueryParams.nonNegativeLong("asOfSeq", asOfSeq);
        Instant asOfAt = QueryParams.instant("asOf", asOf);
        Set<String> includes = QueryParams.enumSet("include", include, INCLUDE_VALUES);
        FieldSelection selection = FieldSelector.parse(fields);
        GeometryOptions geometry = GeometryOptions.parse(precision, simplify);
        int pageSize = QueryParams.intInRange("limit", limit, 1, MAX_LIMIT, DEFAULT_LIMIT);
        boolean observers = includes.contains("observer") && caller.mayReadObservers();
        GeometryEncoding encoding = fmt == ResponseFormat.CSV ? GeometryEncoding.WKT : GeometryEncoding.GEOJSON;
        if (sinceAt != null && untilAt != null && !sinceAt.isBefore(untilAt)) {
            throw ValidationException.forField("until", "must be after since");
        }

        if (asOfAt != null) {
            AsOfQueryService.checkCombination(
                    pinned != null, "observed".equals(orderValue), sortValue != null, expired,
                    sinceAt != null || untilAt != null);
            // An absent filter becomes the restriction's (asOf needs a source or kind filter).
            List<String> asOfSources = sources.isEmpty() && kinds.isEmpty()
                    ? Objects.requireNonNullElse(RestrictionClamp.clampSources(sources, restriction), sources)
                    : sources;
            List<String> asOfKinds = sources.isEmpty() && kinds.isEmpty()
                    ? Objects.requireNonNullElse(RestrictionClamp.clampKinds(kinds, restriction), kinds)
                    : kinds;
            return asOf(new AsOfQueryService.AsOfQuery(asOfAt, asOfSources, asOfKinds, ids, box, nearFilter, cursor,
                    pageSize, selection, geometry, encoding, observers, excluded, restriction), fmt, caller, response);
        }

        ListOrder listOrder;
        if (sortValue != null) {
            if (orderValue != null) {
                throw ValidationException.forField("sort", "sort=distance and order are mutually exclusive");
            }
            if (nearFilter == null) {
                throw ValidationException.forField("sort", "sort=distance requires near and radiusKm");
            }
            listOrder = ListOrder.DISTANCE;
        } else {
            listOrder = "id".equals(orderValue) ? ListOrder.ID : ListOrder.OBSERVED;
        }
        CurrentQuery query = new CurrentQuery(sources, kinds, ids, box, nearFilter, sinceAt, untilAt, axis, q, expired,
                listOrder, pinned, cursor, pageSize, selection, geometry, encoding, observers, excluded, restriction);

        if (fmt.streaming()) {
            streamRecords(fmt, selection, response, sink -> queries.streamCurrent(query, maxRows(), sink),
                    RecordMapper.Shape.CURRENT);
            return null;
        }
        String etag = listETag(request, accept, caller);
        if (ETags.matches(ifNoneMatch, etag)) {
            return ETags.notModified(etag, caller.publicCache());
        }
        Slices.RecordSlice slice = queries.list(query);
        HttpHeaders headers = ETags.cacheHeaders(new HttpHeaders(), etag, caller.publicCache());
        headers.set(WATERMARK_HEADER, Long.toString(slice.watermarkSeq()));
        Object body;
        if (fmt == ResponseFormat.GEOJSON) {
            body = FeatureCollectionPage.of(features(slice.rows(), selection, RecordMapper.Shape.CURRENT),
                    slice.nextCursor(), slice.watermarkSeq(), slice.asOfSeq(), null, null, null);
        } else {
            body = new RecordPage(records(slice.rows(), selection, RecordMapper.Shape.CURRENT), slice.nextCursor(),
                    slice.watermarkSeq(), slice.asOfSeq(), null, null, null);
        }
        return ResponseEntity.ok().headers(headers).contentType(MediaType.parseMediaType(fmt.mediaType())).body(body);
    }

    private ResponseEntity<Object> asOf(
            AsOfQueryService.AsOfQuery query, ResponseFormat fmt, ReadCaller caller, HttpServletResponse response)
            throws IOException {
        AsOfQueryService.AsOfSlice slice = asOfQueries.list(query);
        if (fmt.streaming()) {
            List<String> columns = RecordMapper.csvColumns(query.fields(), RecordMapper.DEFAULT_CSV_COLUMNS);
            startStream(response, fmt, slice.seq());
            try (StreamWriter writer = new StreamWriter(fmt, response.getOutputStream(), json, columns)) {
                for (RecordRow row : slice.rows()) {
                    writer.write(streamItem(fmt, row, query.fields(), RecordMapper.Shape.AS_OF, columns));
                }
            }
            return null;
        }
        HttpHeaders headers = ETags.cacheHeaders(new HttpHeaders(), null, caller.publicCache());
        headers.set(WATERMARK_HEADER, Long.toString(slice.seq()));
        Object body;
        if (fmt == ResponseFormat.GEOJSON) {
            body = FeatureCollectionPage.of(features(slice.rows(), query.fields(), RecordMapper.Shape.AS_OF),
                    slice.nextCursor(), slice.seq(), null, slice.asOf(), slice.approximate(), slice.horizon());
        } else {
            body = new RecordPage(records(slice.rows(), query.fields(), RecordMapper.Shape.AS_OF), slice.nextCursor(),
                    slice.seq(), null, slice.asOf(), slice.approximate(), slice.horizon());
        }
        return ResponseEntity.ok().headers(headers).contentType(MediaType.parseMediaType(fmt.mediaType())).body(body);
    }

    // ------------------------------------------------------------------ getRecord

    /**
     * {@code getRecord}: one record's current state (head plus live). Strong ETag
     * {@code "<version>.<observedAt epoch ms>"}.
     */
    @GetMapping("/v1/records/{sourceId}/{externalId}")
    @Operation(operationId = "getRecord", tags = "Records")
    @RestrictedRead
    public ResponseEntity<Object> getRecord(
            @PathVariable String sourceId,
            @PathVariable String externalId,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) List<String> include,
            @RequestParam(required = false) String fields,
            @RequestParam(required = false) String precision,
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String accept,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        registry.descriptor(sourceId);
        ResponseFormat fmt = ResponseFormat.negotiate(format, accept, RECORD_FORMATS);
        Set<String> includes = QueryParams.enumSet("include", include, INCLUDE_VALUES);
        FieldSelection selection = FieldSelector.parse(fields);
        GeometryOptions geometry = GeometryOptions.parse(precision, null);
        boolean observers = includes.contains("observer") && caller.mayReadObservers();
        RecordRow row = queries.get(sourceId, externalId, observers, selection, geometry,
                        callers.excludedSources(caller), caller.restriction())
                .orElseThrow(() -> NotFoundException.of("Record", sourceId + ":" + externalId));
        String etag = ETags.forRecord(row.version(), row.observedAt());
        if (ETags.matches(ifNoneMatch, etag)) {
            return ETags.notModified(etag, caller.publicCache());
        }
        Object body = fmt == ResponseFormat.GEOJSON
                ? mapper.feature(row, selection, RecordMapper.Shape.CURRENT)
                : mapper.record(row, selection, RecordMapper.Shape.CURRENT);
        return ResponseEntity.ok()
                .headers(ETags.cacheHeaders(new HttpHeaders(), etag, caller.publicCache()))
                .contentType(MediaType.parseMediaType(fmt.mediaType()))
                .body(body);
    }

    // ------------------------------------------------------------------ listRecordVersions

    /** {@code listRecordVersions}: versions of one record ordered by seq. */
    @GetMapping("/v1/records/{sourceId}/{externalId}/versions")
    @Operation(operationId = "listRecordVersions", tags = "Records")
    @RestrictedRead
    public RecordVersionPage listRecordVersions(
            @PathVariable String sourceId,
            @PathVariable String externalId,
            @RequestParam(required = false) String since,
            @RequestParam(required = false) String until,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit,
            @RequestParam(required = false) List<String> include,
            HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        registry.descriptor(sourceId);
        Instant sinceAt = QueryParams.instant("since", since);
        Instant untilAt = QueryParams.instant("until", until);
        int pageSize = QueryParams.intInRange("limit", limit, 1, MAX_LIMIT, DEFAULT_LIMIT);
        Set<String> includes = QueryParams.enumSet("include", include, List.of("observer"));
        boolean observers = includes.contains("observer") && caller.mayReadObservers();
        Slices.VersionSlice slice = queries
                .versions(sourceId, externalId, sinceAt, untilAt, cursor, pageSize, observers,
                        callers.excludedSources(caller), caller.restriction())
                .orElseThrow(() -> NotFoundException.of("Record", sourceId + ":" + externalId));
        return new RecordVersionPage(records(slice.rows(), FieldSelection.ALL, RecordMapper.Shape.VERSION),
                slice.nextCursor());
    }

    // ------------------------------------------------------------------ queryHistory

    /** {@code queryHistory}: every version in a range, keyset on the selected time axis and seq. */
    @GetMapping("/v1/history")
    @Operation(operationId = "queryHistory", tags = "Records")
    @RestrictedRead
    public ResponseEntity<Object> queryHistory(
            @RequestParam(required = false) List<String> source,
            @RequestParam(required = false) List<String> kind,
            @RequestParam(required = false) List<String> externalId,
            @RequestParam(required = false) String bbox,
            @RequestParam(required = false) String near,
            @RequestParam(required = false) String radiusKm,
            @RequestParam(required = false) String since,
            @RequestParam(required = false) String until,
            @RequestParam(required = false) String timeField,
            @RequestParam(required = false) String fields,
            @RequestParam(required = false) String precision,
            @RequestParam(required = false) String simplify,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit,
            @RequestParam(required = false) List<String> include,
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String accept,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {
        ReadCaller caller = callers.resolveScoped(request);
        if (format != null && "czml".equalsIgnoreCase(format.strip())) {
            throw ValidationException.forField("format", "czml is not available yet (prompt 141)");
        }
        ResponseFormat fmt = ResponseFormat.negotiate(format, accept, LIST_FORMATS);
        Instant sinceAt = QueryParams.instant("since", since);
        if (sinceAt == null) {
            throw ValidationException.forField("since", "since is required");
        }
        Instant untilAt = QueryParams.instant("until", until);
        if (untilAt != null && !sinceAt.isBefore(untilAt)) {
            throw ValidationException.forField("until", "must be after since");
        }
        FieldSelection selection = FieldSelector.parse(fields);
        Set<String> includes = QueryParams.enumSet("include", include, List.of("observer"));
        HistoryQuery query = new HistoryQuery(
                knownSources(QueryParams.multi("source", source), callers.hiddenSources(caller)),
                QueryParams.multi("kind", kind),
                QueryParams.multi("externalId", externalId),
                BboxParser.parse(bbox),
                NearParser.parse(near, ReadCallerResolver.radiusForNear(request, near, radiusKm)),
                sinceAt,
                untilAt,
                TimeField.parse(timeField, TimeField.OBSERVED),
                cursor,
                QueryParams.intInRange("limit", limit, 1, MAX_HISTORY_LIMIT, DEFAULT_HISTORY_LIMIT),
                selection,
                GeometryOptions.parse(precision, simplify),
                fmt == ResponseFormat.CSV ? GeometryEncoding.WKT : GeometryEncoding.GEOJSON,
                includes.contains("observer") && caller.mayReadObservers(),
                callers.excludedSources(caller),
                caller.restriction());
        if (fmt.streaming()) {
            streamRecords(fmt, selection, response, sink -> queries.streamHistory(query, maxRows(), sink),
                    RecordMapper.Shape.VERSION);
            return null;
        }
        Slices.VersionSlice slice = queries.history(query);
        Object body = fmt == ResponseFormat.GEOJSON
                ? FeatureCollectionPage.of(features(slice.rows(), selection, RecordMapper.Shape.VERSION),
                        slice.nextCursor(), null, null, null, null, null)
                : new RecordVersionPage(records(slice.rows(), selection, RecordMapper.Shape.VERSION), slice.nextCursor());
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(fmt.mediaType())).body(body);
    }

    // ------------------------------------------------------------------ kinds

    /** {@code listKinds}: kinds currently present, with counts, sources and the declared display block. */
    @GetMapping("/v1/kinds")
    @Operation(operationId = "listKinds", tags = "Records")
    @RestrictedRead
    public ResponseEntity<Object> listKinds(
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String accept,
            HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        String etag = listETag(request, accept, caller);
        if (ETags.matches(ifNoneMatch, etag)) {
            return ETags.notModified(etag, caller.publicCache());
        }
        List<KindSummaryDto> kinds = new ArrayList<>();
        for (Slices.KindRow row : queries.kinds(callers.excludedSources(caller), caller.restriction())) {
            kinds.add(new KindSummaryDto(row.kind(), row.count(), row.sources(), display(row.kind())));
        }
        return ResponseEntity.ok()
                .headers(ETags.cacheHeaders(new HttpHeaders(), etag, caller.publicCache()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(kinds);
    }

    /** {@code getKindSchema}: the merged JSON Schema of one kind. 404 when no source emits it. */
    @GetMapping("/v1/kinds/{kind}/schema")
    @Operation(operationId = "getKindSchema", tags = "Records")
    @RestrictedRead
    public ResponseEntity<Object> getKindSchema(
            @PathVariable String kind,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        if (!caller.restriction().allowsKind(kind)) {
            throw NotFoundException.of("Kind", kind);
        }
        Map<String, Object> schema = KindSchemaBuilder.build(kind, registry.descriptors())
                .orElseThrow(() -> NotFoundException.of("Kind", kind));
        String etag = ETags.forDocument(json.writeValueAsString(schema));
        if (ETags.matches(ifNoneMatch, etag)) {
            return ETags.notModified(etag, caller.publicCache());
        }
        return ResponseEntity.ok()
                .headers(ETags.cacheHeaders(new HttpHeaders(), etag, caller.publicCache()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(schema);
    }

    // ------------------------------------------------------------------ helpers

    /** Rows to {@code Record} items. */
    private List<Map<String, Object>> records(List<RecordRow> rows, FieldSelection fields, RecordMapper.Shape shape) {
        List<Map<String, Object>> items = new ArrayList<>(rows.size());
        for (RecordRow row : rows) {
            items.add(mapper.record(row, fields, shape));
        }
        return items;
    }

    private List<Map<String, Object>> features(List<RecordRow> rows, FieldSelection fields, RecordMapper.Shape shape) {
        List<Map<String, Object>> items = new ArrayList<>(rows.size());
        for (RecordRow row : rows) {
            items.add(mapper.feature(row, fields, shape));
        }
        return items;
    }

    /** Streams rows produced by {@code producer} in a streaming format. */
    private void streamRecords(
            ResponseFormat fmt,
            FieldSelection fields,
            HttpServletResponse response,
            java.util.function.Consumer<RecordQueryService.RowSink> producer,
            RecordMapper.Shape shape) {
        List<String> columns = RecordMapper.csvColumns(fields, RecordMapper.DEFAULT_CSV_COLUMNS);
        StreamWriter[] writer = new StreamWriter[1];
        try {
            producer.accept(new RecordQueryService.RowSink() {
                @Override
                public void begin(long watermarkSeq) {
                    try {
                        startStream(response, fmt, watermarkSeq);
                        writer[0] = new StreamWriter(fmt, response.getOutputStream(), json, columns);
                    } catch (IOException e) {
                        throw new java.io.UncheckedIOException(e);
                    }
                }

                @Override
                public void accept(RecordRow row) {
                    writer[0].write(streamItem(fmt, row, fields, shape, columns));
                }
            });
        } finally {
            if (writer[0] != null) {
                writer[0].close();
            }
        }
    }

    private Map<String, Object> streamItem(
            ResponseFormat fmt, RecordRow row, FieldSelection fields, RecordMapper.Shape shape, List<String> columns) {
        return switch (fmt) {
            case GEOJSONSEQ -> mapper.feature(row, fields, shape);
            case CSV -> mapper.csvRow(mapper.record(row, fields, shape), columns);
            default -> mapper.record(row, fields, shape);
        };
    }

    static void startStream(HttpServletResponse response, ResponseFormat fmt, long watermarkSeq) {
        response.setStatus(200);
        response.setContentType(fmt == ResponseFormat.CSV ? fmt.mediaType() + ";charset=UTF-8" : fmt.mediaType());
        response.setCharacterEncoding("UTF-8");
        response.setHeader(WATERMARK_HEADER, Long.toString(watermarkSeq));
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader(HttpHeaders.VARY, ETags.VARY);
    }

    private int maxRows() {
        return properties.stream().maxRows();
    }

    /** {@code 404} for a source filter naming no registered source. */
    /**
     * {@code sources}, each of which must be registered and visible to the caller: a private source
     * named by a caller who may not see it is {@code 404}, like an unknown one (prompt 023).
     */
    private List<String> knownSources(List<String> sources, Set<String> excluded) {
        for (String id : sources) {
            registry.descriptor(id);
            if (excluded.contains(id)) {
                throw NotFoundException.of("Source", id);
            }
        }
        return sources;
    }

    private KindDisplay display(String kind) {
        return registry.descriptors().stream()
                .sorted(Comparator.comparing(SourceDescriptor::id))
                .map(d -> d.kindDisplay() == null ? null : d.kindDisplay().get(kind))
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /**
     * The list entity tag: the normalized query (sorted parameters, the format and the caller), the
     * change watermark, the live batch number and the current minute.
     */
    private String listETag(HttpServletRequest request, String accept, ReadCaller caller) {
        Map<String, List<String>> params = new TreeMap<>();
        request.getParameterMap().forEach((name, values) -> params.put(name, List.of(values)));
        String normalized = request.getRequestURI() + "?" + params + "|" + Optional.ofNullable(accept).orElse("")
                + "|" + caller.canonical();
        return ETags.forList(normalized, changeBus.lastPublishedSeq(), liveBus.lastPublishedBatchNo(), clock.instant());
    }
}
