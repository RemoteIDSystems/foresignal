package ai.foresignal.api;

import ai.foresignal.api.support.QueryParams;
import ai.foresignal.sink.SinkCursorEntity;
import ai.foresignal.sink.SinkEntity;
import ai.foresignal.sink.SinkInstance;
import ai.foresignal.sink.SinkKind;
import ai.foresignal.sink.SinkMode;
import ai.foresignal.sink.SinkRecord;
import ai.foresignal.sink.SinkService;
import ai.foresignal.sink.SinkSupervisor;
import ai.foresignal.sink.SinkTestResult;
import ai.foresignal.sink.SinkType;
import ai.foresignal.sink.SinkTypeRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Outbound sinks (OpenAPI tag {@code Sinks}, prompt 110): the type catalogue, CRUD, a one-off test, the
 * cursor status and a forced resync. Admin only; secrets are write-only.
 */
@RestController
@Tag(name = "Sinks")
public class SinksController {

    private final SinkService sinks;
    private final SinkTypeRegistry types;
    private final SinkSupervisor supervisor;

    /** Creates the controller. */
    public SinksController(SinkService sinks, SinkTypeRegistry types, SinkSupervisor supervisor) {
        this.sinks = sinks;
        this.types = types;
        this.supervisor = supervisor;
    }

    /** {@code listSinkTypes}. */
    @GetMapping("/v1/sinks/types")
    @Operation(operationId = "listSinkTypes", tags = "Sinks")
    public List<Map<String, Object>> listSinkTypes() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (SinkType type : types.all()) {
            out.add(typeMap(type));
        }
        return out;
    }

    /** {@code listSinks}. */
    @GetMapping("/v1/sinks")
    @Operation(operationId = "listSinks", tags = "Sinks")
    public Map<String, Object> listSinks(@RequestParam(required = false) String limit) {
        int size = QueryParams.intInRange("limit", limit, 1, SinkService.MAX_LIMIT, SinkService.DEFAULT_LIMIT);
        List<Map<String, Object>> items = new ArrayList<>();
        for (SinkService.View view : sinks.list()) {
            items.add(sinkMap(view.sink()));
            if (items.size() >= size) {
                break;
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", null);
        return out;
    }

    /** {@code createSink}. */
    @PostMapping("/v1/sinks")
    @Operation(operationId = "createSink", tags = "Sinks")
    public ResponseEntity<Map<String, Object>> createSink(@RequestBody JsonNode body) {
        SinkService.View view = sinks.create(body);
        return ResponseEntity.created(URI.create("/v1/sinks/" + view.sink().getId())).body(sinkMap(view.sink()));
    }

    /** {@code getSink}. */
    @GetMapping("/v1/sinks/{sinkId}")
    @Operation(operationId = "getSink", tags = "Sinks")
    public Map<String, Object> getSink(@PathVariable String sinkId) {
        return sinkMap(sinks.get(sinkId).sink());
    }

    /** {@code updateSink}. */
    @PutMapping("/v1/sinks/{sinkId}")
    @Operation(operationId = "updateSink", tags = "Sinks")
    public Map<String, Object> updateSink(@PathVariable String sinkId, @RequestBody JsonNode body) {
        return sinkMap(sinks.update(sinkId, body).sink());
    }

    /** {@code deleteSink}. */
    @DeleteMapping("/v1/sinks/{sinkId}")
    @Operation(operationId = "deleteSink", tags = "Sinks")
    public ResponseEntity<Void> deleteSink(@PathVariable String sinkId) {
        sinks.delete(sinkId);
        return ResponseEntity.noContent().build();
    }

    /** {@code testSink}. */
    @PostMapping("/v1/sinks/{sinkId}/test")
    @Operation(operationId = "testSink", tags = "Sinks")
    public Map<String, Object> testSink(@PathVariable String sinkId,
            @RequestBody(required = false) JsonNode body) {
        SinkService.View view = sinks.get(sinkId);
        SinkEntity sink = view.sink();
        SinkType type = types.get(sink.getType());
        SinkMode mode = mode(type, sink);
        SinkInstance instance = null;
        try {
            instance = type.open(sinks.config(sink, mode));
            SinkTestResult result = instance.test(synthetic(body));
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("ok", result.ok());
            out.put("phase", result.phase());
            out.put("message", result.message());
            out.put("latencyMs", result.latencyMs());
            out.put("details", result.details());
            return out;
        } catch (RuntimeException e) {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("ok", false);
            out.put("phase", "connect");
            out.put("message", e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
            out.put("latencyMs", null);
            out.put("details", Map.of());
            return out;
        } finally {
            if (instance != null) {
                instance.close();
            }
        }
    }

    /** {@code getSinkStatus}. */
    @GetMapping("/v1/sinks/{sinkId}/status")
    @Operation(operationId = "getSinkStatus", tags = "Sinks")
    public Map<String, Object> getSinkStatus(@PathVariable String sinkId, HttpServletRequest request) {
        return statusMap(sinks.get(sinkId));
    }

    /** {@code resyncSink}. */
    @PostMapping("/v1/sinks/{sinkId}/resync")
    @Operation(operationId = "resyncSink", tags = "Sinks")
    public ResponseEntity<Map<String, Object>> resyncSink(@PathVariable String sinkId) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(statusMap(sinks.resync(sinkId)));
    }

    private Map<String, Object> typeMap(SinkType type) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", type.spec().id());
        out.put("name", type.spec().name());
        out.put("description", type.spec().description());
        out.put("kind", type.spec().kind().name());
        out.put("modes", type.spec().modes().stream().map(Enum::name).toList());
        out.put("configSchema", type.spec().configSchema());
        out.put("secretFields", type.spec().secretFields());
        out.put("defaultMaxPerSecond", type.spec().defaultMaxPerSecond());
        return out;
    }

    private Map<String, Object> sinkMap(SinkEntity sink) {
        SinkType type = types.get(sink.getType());
        SinkMode mode = mode(type, sink);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", sink.getId());
        out.put("type", sink.getType());
        out.put("kind", type.spec().kind().name());
        out.put("mode", mode.name());
        out.put("name", sink.getName());
        out.put("enabled", sink.isEnabled());
        out.put("config", sink.getConfig());
        out.put("secretsSet", sinks.secrets(sink).keySet().stream().sorted().toList());
        out.put("filter", sinks.filter(sink).toMap());
        out.put("state", state(sink.getId()));
        out.put("createdAt", sink.getCreatedAt().toString());
        out.put("updatedAt", sink.getUpdatedAt().toString());
        return out;
    }

    private Map<String, Object> statusMap(SinkService.View view) {
        SinkEntity sink = view.sink();
        SinkCursorEntity cursor = view.cursor();
        long published = 0;
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sinkId", sink.getId());
        out.put("state", cursor == null ? "STOPPED" : cursor.getState());
        out.put("lastSeq", cursor == null ? null : cursor.getLastSeq());
        out.put("watermarkSeq", cursor == null ? null : cursor.getWatermarkSeq());
        out.put("lastPublishedSeq", published);
        out.put("lagSeq", cursor == null ? null : Math.max(0, published - cursor.getLastSeq()));
        out.put("lastDeliveredAt", cursor == null || cursor.getLastDeliveredAt() == null ? null
                : cursor.getLastDeliveredAt().toString());
        out.put("lastError", cursor == null ? null : cursor.getLastError());
        out.put("failingSince", cursor == null || cursor.getFailingSince() == null ? null
                : cursor.getFailingSince().toString());
        out.put("resyncCount", cursor == null ? 0 : cursor.getResyncCount());
        out.put("delivered", cursor == null ? 0 : cursor.getDelivered());
        out.put("dropped", cursor == null ? 0 : cursor.getDropped());
        out.put("denied", cursor == null ? Map.of("total", 0) : cursor.getDenied());
        SinkSupervisor.Run run = supervisor.run(sink.getId());
        if (run != null && run.instance() != null && run.instance().clients() >= 0
                && types.get(sink.getType()).spec().modes().contains(SinkMode.SERVE)) {
            Map<String, Object> clients = new LinkedHashMap<>();
            clients.put("connected", run.instance().clients());
            clients.put("max", 8);
            clients.put("rejected", 0);
            clients.put("peers", List.of());
            out.put("clients", clients);
        } else {
            out.put("clients", null);
        }
        return out;
    }

    private String state(String sinkId) {
        SinkSupervisor.Run run = supervisor.run(sinkId);
        return run == null ? "STOPPED" : SinkCursorEntity.RUNNING;
    }

    private static SinkMode mode(SinkType type, SinkEntity sink) {
        String configured = String.valueOf(sink.getConfig().getOrDefault("mode", ""));
        if ("SERVE".equalsIgnoreCase(configured) && type.spec().modes().contains(SinkMode.SERVE)) {
            return SinkMode.SERVE;
        }
        if ("PUSH".equalsIgnoreCase(configured) && type.spec().modes().contains(SinkMode.PUSH)) {
            return SinkMode.PUSH;
        }
        return type.spec().modes().contains(SinkMode.PUSH) ? SinkMode.PUSH : SinkMode.SERVE;
    }

    private static SinkRecord synthetic(JsonNode body) {
        String kind = body == null || body.get("kind") == null ? "point" : body.get("kind").asString();
        Instant now = Instant.now();
        return new SinkRecord("foresignal", "sink-test", kind, 0L, 1, now, null, false,
                new GeometryFactory().createPoint(new org.locationtech.jts.geom.Coordinate(0, 0)),
                Map.of("name", "Foresignal sink test"), ai.foresignal.provenance.LicenceTerms.unknown(), null);
    }
}
