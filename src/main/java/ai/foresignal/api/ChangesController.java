package ai.foresignal.api;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.dto.ChangePage;
import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.api.support.ApiProperties;
import ai.foresignal.api.support.BboxParser;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.FieldSelector;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.api.support.ResponseFormat;
import ai.foresignal.api.support.StreamWriter;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.store.query.ChangesQuery;
import ai.foresignal.store.query.GeometryEncoding;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.store.query.Slices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

/**
 * The ordered change feed (OpenAPI tag {@code Changes}): {@code GET /v1/changes}. The SSE stream
 * ({@code /v1/changes/stream}) is {@link StreamController} (prompt 015).
 *
 * <p>Every new version in commit order ({@code seq > after}); seq is gap-tolerant but strictly
 * increasing in commit order, so {@code after=<lastSeq>} never misses a change. Each change carries
 * {@code prevSeq}, {@code persistReason} and {@code validUntil}. Kinds in
 * {@code foresignal.api.changes.inlinePayloadKinds} (default {@code raster-frame}) always carry
 * their payload. When {@code after} is below the purge horizon the response is
 * {@code 410 RESYNC_REQUIRED} and the client re-snapshots.
 *
 * <p>Needs the {@code read} scope, open while {@code security.openReads} is on.
 */
@RestController
@Tag(name = "Changes")
public class ChangesController {

    /** Default page size. */
    static final int DEFAULT_LIMIT = 1000;

    /** Largest page. */
    static final int MAX_LIMIT = 10_000;

    static final List<ResponseFormat> FORMATS =
            List.of(ResponseFormat.JSON, ResponseFormat.NDJSON, ResponseFormat.GEOJSONSEQ, ResponseFormat.CSV);

    private final RecordQueryService queries;
    private final RecordMapper mapper;
    private final ReadCallerResolver callers;
    private final SourceRegistry registry;
    private final ApiProperties properties;
    private final JsonMapper json;

    /** Creates the controller. */
    public ChangesController(
            RecordQueryService queries,
            RecordMapper mapper,
            ReadCallerResolver callers,
            SourceRegistry registry,
            ApiProperties properties,
            JsonMapper json) {
        this.queries = queries;
        this.mapper = mapper;
        this.callers = callers;
        this.registry = registry;
        this.properties = properties;
        this.json = json;
    }

    /** {@code listChanges}. */
    @GetMapping("/v1/changes")
    @Operation(operationId = "listChanges", tags = "Changes")
    public ResponseEntity<Object> listChanges(
            @RequestParam(required = false) String after,
            @RequestParam(required = false) List<String> source,
            @RequestParam(required = false) List<String> kind,
            @RequestParam(required = false) List<String> externalId,
            @RequestParam(required = false) String bbox,
            @RequestParam(required = false) String payload,
            @RequestParam(required = false) String limit,
            @RequestParam(required = false) String fields,
            @RequestParam(required = false) String format,
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String accept,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {
        ReadCaller caller = callers.resolve(request);
        ResponseFormat fmt = ResponseFormat.negotiate(format, accept, FORMATS);
        Long afterSeq = QueryParams.nonNegativeLong("after", after);
        List<String> sources = QueryParams.multi("source", source);
        Set<String> hidden = callers.excludedSources(caller);
        for (String id : sources) {
            registry.descriptor(id);
            if (hidden.contains(id)) {
                // A private source the caller may not see is reported like an unknown one (023).
                throw NotFoundException.of("Source", id);
            }
        }
        FieldSelection selection = FieldSelector.parse(fields);
        ChangesQuery query = new ChangesQuery(
                afterSeq == null ? 0L : afterSeq,
                sources,
                QueryParams.multi("kind", kind),
                QueryParams.multi("externalId", externalId),
                BboxParser.parse(bbox),
                QueryParams.bool("payload", payload, false),
                QueryParams.intInRange("limit", limit, 1, MAX_LIMIT, DEFAULT_LIMIT),
                selection,
                GeometryOptions.DEFAULT,
                fmt == ResponseFormat.CSV ? GeometryEncoding.WKT : GeometryEncoding.GEOJSON,
                callers.excludedSources(caller));
        if (fmt.streaming()) {
            stream(query, fmt, selection, response);
            return null;
        }
        Slices.ChangeSlice slice = queries.changes(query);
        List<Map<String, Object>> items = new ArrayList<>(slice.rows().size());
        for (RecordRow row : slice.rows()) {
            items.add(mapper.change(row, selection));
        }
        return ResponseEntity.ok()
                .header(RecordsController.WATERMARK_HEADER, Long.toString(slice.watermarkSeq()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ChangePage(items, slice.lastSeq(), slice.hasMore(), slice.watermarkSeq(), slice.purgeHorizon()));
    }

    private void stream(ChangesQuery query, ResponseFormat fmt, FieldSelection selection, HttpServletResponse response) {
        List<String> columns = RecordMapper.csvColumns(selection, RecordMapper.DEFAULT_CHANGE_CSV_COLUMNS);
        StreamWriter[] writer = new StreamWriter[1];
        try {
            queries.streamChanges(query, properties.stream().maxRows(), new RecordQueryService.RowSink() {
                @Override
                public void begin(long watermarkSeq) {
                    try {
                        RecordsController.startStream(response, fmt, watermarkSeq);
                        writer[0] = new StreamWriter(fmt, response.getOutputStream(), json, columns);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }

                @Override
                public void accept(RecordRow row) {
                    Map<String, Object> item = switch (fmt) {
                        case GEOJSONSEQ -> mapper.changeFeature(row, selection);
                        case CSV -> mapper.csvRow(mapper.change(row, selection), columns);
                        default -> mapper.change(row, selection);
                    };
                    writer[0].write(item);
                }
            });
        } finally {
            if (writer[0] != null) {
                writer[0].close();
            }
        }
    }
}
