package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.BboxParser;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.chronicle.ChronicleEntity;
import ai.foresignal.chronicle.ChronicleExporter;
import ai.foresignal.chronicle.ChronicleLayerEntity;
import ai.foresignal.chronicle.ChronicleReplayService;
import ai.foresignal.chronicle.ChronicleService;
import ai.foresignal.chronicle.ChronicleSuggester;
import ai.foresignal.chronicle.ChronicleThreadService;
import ai.foresignal.chronicle.ChronicleVocabulary;
import ai.foresignal.collab.AccessAction;
import ai.foresignal.collab.AccessResolver;
import ai.foresignal.export.ExportJobEntity;
import ai.foresignal.export.ExportJobService;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.raster.ProductUrls;
import ai.foresignal.security.PrincipalKind;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.store.tiles.TileCoordinates;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Chronicles (prompt 131): named, shareable monitoring threads that record their own history. Every operation needs
 * a token; a chronicle the caller may not see is {@code 404 UNKNOWN_CHRONICLE} (130 access lists). Reads need the
 * {@code viewer} role, changes, layers and lifecycle {@code editor}, deletion {@code owner} with the display id as
 * {@code confirm}. Replay endpoints read chronicle storage only and apply the caller's excluded sources and read
 * restriction. Display ids ({@code CHR-yyyy-nnnn}) work in every path.
 */
@RestController
@Tag(name = "Chronicles")
@RequestMapping("/v1/chronicles")
public class ChroniclesController {

    private final ChronicleService chronicles;
    private final ChronicleThreadService thread;
    private final ChronicleReplayService replay;
    private final ChronicleSuggester suggester;
    private final ChronicleExporter exporter;
    private final ExportJobService exports;
    private final ObjectCallers callers;
    private final ReadCallerResolver readCallers;
    private final PrincipalService principals;
    private final AccessResolver access;
    private final ProductUrls urls;

    /** Creates the controller. */
    public ChroniclesController(ChronicleService chronicles, ChronicleThreadService thread,
            ChronicleReplayService replay, ChronicleSuggester suggester, ChronicleExporter exporter,
            ExportJobService exports, ObjectCallers callers, ReadCallerResolver readCallers, PrincipalService principals,
            AccessResolver access, ProductUrls urls) {
        this.chronicles = chronicles;
        this.thread = thread;
        this.replay = replay;
        this.suggester = suggester;
        this.exporter = exporter;
        this.exports = exports;
        this.callers = callers;
        this.readCallers = readCallers;
        this.principals = principals;
        this.access = access;
        this.urls = urls;
    }

    private ChronicleReplayService.Reader reader(ObjectCaller caller, HttpServletRequest request) {
        return ChronicleReplayService.Reader.of(caller, readCallers.restriction(request));
    }

    /** {@code operator} for people, {@code agent} for agent and service principals. */
    private String origin(ObjectCaller caller) {
        return principals.find(caller.principal()).map(p -> PrincipalKind.PERSON.wireValue().equals(p.getKind())
                ? ChronicleVocabulary.ORIGIN_OPERATOR : ChronicleVocabulary.ORIGIN_AGENT)
                .orElse(ChronicleVocabulary.ORIGIN_AGENT);
    }

    private static Instant instant(String name, String value) {
        return QueryParams.instant(name, value);
    }

    private static Duration duration(String name, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Duration.parse(value.strip());
        } catch (DateTimeParseException e) {
            throw ValidationException.forField(name, "must be an ISO-8601 duration such as PT5M");
        }
    }

    // ------------------------------------------------------------------------- CRUD

    /** {@code listChronicles}: the chronicles the caller owns or that are shared with it. */
    @GetMapping
    @Operation(operationId = "listChronicles", tags = "Chronicles")
    public Map<String, Object> listChronicles(@RequestParam(required = false) String status,
            @RequestParam(required = false) String scope, @RequestParam(required = false) String situationId,
            @RequestParam(required = false) String q, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        if (status != null && !ChronicleVocabulary.isStatus(status)) {
            throw ValidationException.forField("status", "must be one of " + ChronicleVocabulary.STATUSES);
        }
        String which = QueryParams.oneOf("scope", scope, List.of("mine", "shared", "all"), "all");
        int max = QueryParams.intInRange("limit", limit, 1, 500, 100);
        return chronicles.list(caller, status, which, situationId, q, cursor, max);
    }

    /** {@code createChronicle}: owned by the caller, with its workspace; proposed until activated. */
    @PostMapping
    @Operation(operationId = "createChronicle", tags = "Chronicles")
    @ApiResponse(responseCode = "201", description = "Created")
    public ResponseEntity<Map<String, Object>> createChronicle(@RequestBody JsonNode body, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        ChronicleEntity c = chronicles.create(body, caller, origin(caller), null);
        return ResponseEntity.created(URI.create("/v1/chronicles/" + c.getId()))
                .eTag(ChronicleService.etag(chronicles.get(c.getId()))).body(chronicles.view(c, caller, true));
    }

    /** {@code suggestChronicle}: a draft (not created) with its storage estimate. */
    @PostMapping("/suggest")
    @Operation(operationId = "suggestChronicle", tags = "Chronicles")
    public Map<String, Object> suggestChronicle(@RequestBody JsonNode body, HttpServletRequest request) {
        callers.authenticated(request);
        return suggester.suggest(body);
    }

    /** {@code getChronicle}: with its layers, and its version as ETag. */
    @GetMapping("/{chronicleId}")
    @Operation(operationId = "getChronicle", tags = "Chronicles")
    public ResponseEntity<Map<String, Object>> getChronicle(@PathVariable String chronicleId,
            HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        ChronicleEntity c = chronicles.require(chronicleId, caller, AccessAction.VIEW);
        return ResponseEntity.ok().eTag(ChronicleService.etag(c)).body(chronicles.view(c, caller, true));
    }

    /** {@code updateChronicle}: a JSON merge patch (owner or editor), {@code If-Match} optional. */
    @PatchMapping(value = "/{chronicleId}", consumes = {"application/merge-patch+json", "application/json"})
    @Operation(operationId = "updateChronicle", tags = "Chronicles")
    public ResponseEntity<Map<String, Object>> updateChronicle(@PathVariable String chronicleId,
            @RequestHeader(value = "If-Match", required = false) String ifMatch, @RequestBody JsonNode body,
            HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        ChronicleEntity c = chronicles.patch(chronicleId, body, ifMatch, caller);
        return ResponseEntity.ok().eTag(ChronicleService.etag(c)).body(chronicles.view(c, caller, true));
    }

    /** {@code deleteChronicle}: owner only, {@code confirm} = the display id. */
    @DeleteMapping("/{chronicleId}")
    @Operation(operationId = "deleteChronicle", tags = "Chronicles")
    @ApiResponse(responseCode = "204", description = "Deleted")
    public ResponseEntity<Void> deleteChronicle(@PathVariable String chronicleId,
            @RequestParam(required = false) String confirm, HttpServletRequest request) {
        chronicles.delete(chronicleId, confirm, callers.authenticated(request));
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------------- layers

    /** {@code createChronicleLayer}: capture for it starts from the edit's seq onward. */
    @PostMapping("/{chronicleId}/layers")
    @Operation(operationId = "createChronicleLayer", tags = "Chronicles")
    @ApiResponse(responseCode = "201", description = "Created")
    public ResponseEntity<Map<String, Object>> createChronicleLayer(@PathVariable String chronicleId,
            @RequestBody JsonNode body, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        ChronicleLayerEntity l = chronicles.addLayer(chronicleId, body, caller);
        ChronicleEntity c = chronicles.get(l.getChronicleId());
        return ResponseEntity.created(URI.create("/v1/chronicles/" + c.getId() + "/layers/" + l.getId()))
                .body(ChronicleService.layerView(l, ChronicleVocabulary.CAP_EXCEEDED.equals(c.getCapState()), true));
    }

    /** {@code updateChronicleLayer}: a JSON merge patch; the new definition applies from the edit's seq. */
    @PatchMapping(value = "/{chronicleId}/layers/{layerId}",
            consumes = {"application/merge-patch+json", "application/json"})
    @Operation(operationId = "updateChronicleLayer", tags = "Chronicles")
    public Map<String, Object> updateChronicleLayer(@PathVariable String chronicleId, @PathVariable String layerId,
            @RequestBody JsonNode body, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        ChronicleLayerEntity l = chronicles.patchLayer(chronicleId, layerId, body, caller);
        ChronicleEntity c = chronicles.get(l.getChronicleId());
        return ChronicleService.layerView(l, ChronicleVocabulary.CAP_EXCEEDED.equals(c.getCapState()), true);
    }

    /** {@code deleteChronicleLayer}: captured data stays unless {@code purge=true}. */
    @DeleteMapping("/{chronicleId}/layers/{layerId}")
    @Operation(operationId = "deleteChronicleLayer", tags = "Chronicles")
    @ApiResponse(responseCode = "204", description = "Removed")
    public ResponseEntity<Void> deleteChronicleLayer(@PathVariable String chronicleId, @PathVariable String layerId,
            @RequestParam(required = false) String purge, HttpServletRequest request) {
        chronicles.removeLayer(chronicleId, layerId, QueryParams.bool("purge", purge, false),
                callers.authenticated(request));
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------------- lifecycle

    /** {@code activateChronicle}: starts capture (accepting a proposal), with an optional backfill. */
    @PostMapping("/{chronicleId}/activate")
    @Operation(operationId = "activateChronicle", tags = "Chronicles")
    public Map<String, Object> activateChronicle(@PathVariable String chronicleId,
            @RequestBody(required = false) JsonNode body, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        return chronicles.view(chronicles.activate(chronicleId, body, caller, false), caller, true);
    }

    /** {@code pauseChronicle}. */
    @PostMapping("/{chronicleId}/pause")
    @Operation(operationId = "pauseChronicle", tags = "Chronicles")
    public Map<String, Object> pauseChronicle(@PathVariable String chronicleId, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        return chronicles.view(chronicles.pause(chronicleId, caller), caller, true);
    }

    /** {@code resumeChronicle}. */
    @PostMapping("/{chronicleId}/resume")
    @Operation(operationId = "resumeChronicle", tags = "Chronicles")
    public Map<String, Object> resumeChronicle(@PathVariable String chronicleId, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        return chronicles.view(chronicles.resume(chronicleId, caller), caller, true);
    }

    /** {@code stopChronicle}. */
    @PostMapping("/{chronicleId}/stop")
    @Operation(operationId = "stopChronicle", tags = "Chronicles")
    public Map<String, Object> stopChronicle(@PathVariable String chronicleId, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        return chronicles.view(chronicles.stop(chronicleId, caller), caller, true);
    }

    /** {@code archiveChronicle}: 202; the archive runs asynchronously. */
    @PostMapping("/{chronicleId}/archive")
    @Operation(operationId = "archiveChronicle", tags = "Chronicles")
    @ApiResponse(responseCode = "202", description = "Archiving")
    public ResponseEntity<Map<String, Object>> archiveChronicle(@PathVariable String chronicleId,
            HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(chronicles.view(chronicles.archive(chronicleId,
                caller), caller, true));
    }

    /** {@code restoreChronicle}: 202; the restore runs asynchronously. */
    @PostMapping("/{chronicleId}/restore")
    @Operation(operationId = "restoreChronicle", tags = "Chronicles")
    @ApiResponse(responseCode = "202", description = "Restoring")
    public ResponseEntity<Map<String, Object>> restoreChronicle(@PathVariable String chronicleId,
            HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(chronicles.view(chronicles.restore(chronicleId,
                caller), caller, true));
    }

    /** {@code getChronicleEstimate}: projected storage for the current layers. */
    @GetMapping("/{chronicleId}/estimate")
    @Operation(operationId = "getChronicleEstimate", tags = "Chronicles")
    public Map<String, Object> getChronicleEstimate(@PathVariable String chronicleId, HttpServletRequest request) {
        ChronicleEntity c = chronicles.require(chronicleId, callers.authenticated(request), AccessAction.VIEW);
        return chronicles.estimate(c);
    }

    /** {@code exportChronicle}: a 111 export job of chronicle storage (TLP and LOCAL_ONLY respected). */
    @PostMapping("/{chronicleId}/export")
    @Operation(operationId = "exportChronicle", tags = "Chronicles")
    @ApiResponse(responseCode = "202", description = "Queued")
    @RestrictedRead("the export decision leaves out sources the caller may not read; LOCAL_ONLY never leaves")
    public ResponseEntity<Map<String, Object>> exportChronicle(@PathVariable String chronicleId,
            @RequestBody JsonNode body, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        ChronicleEntity c = chronicles.require(chronicleId, caller, AccessAction.VIEW);
        ExportJobEntity job = exporter.submit(c, body, ExportCallers.of(request, readCallers), caller);
        String tlp = access.tlpOf(ChronicleService.ref(c.getId())).wireValue();
        return ResponseEntity.status(HttpStatus.ACCEPTED).location(URI.create("/v1/exports/" + job.getId()))
                .header("Foresignal-TLP", tlp).body(exports.view(job, urls.base(request)));
    }

    // ------------------------------------------------------------------------- thread

    /** {@code getChronicleThread}: the chronological feed with envelopes and the caller's unread count. */
    @GetMapping("/{chronicleId}/thread")
    @Operation(operationId = "getChronicleThread", tags = "Chronicles")
    @RestrictedRead("private items need private access; envelopes are resolved for the caller")
    public Map<String, Object> getChronicleThread(@PathVariable String chronicleId,
            @RequestParam(required = false) String after, @RequestParam(required = false) List<String> types,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit,
            HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        ChronicleEntity c = chronicles.require(chronicleId, caller, AccessAction.VIEW);
        List<String> typeList = QueryParams.multi("types", types);
        for (String t : typeList) {
            if (!ChronicleVocabulary.ITEM_TYPES.contains(t)) {
                throw ValidationException.forField("types", "must be among " + ChronicleVocabulary.ITEM_TYPES);
            }
        }
        return thread.page(c.getId(), caller, instant("after", after), typeList, cursor,
                QueryParams.intInRange("limit", limit, 1, 500, 100));
    }

    /** {@code markChronicleThreadRead}: moves the caller's read cursor forward. */
    @PostMapping("/{chronicleId}/thread/read")
    @Operation(operationId = "markChronicleThreadRead", tags = "Chronicles")
    @ApiResponse(responseCode = "204", description = "Moved")
    public ResponseEntity<Void> markChronicleThreadRead(@PathVariable String chronicleId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        ChronicleEntity c = chronicles.require(chronicleId, caller, AccessAction.VIEW);
        JsonNode upTo = body == null ? null : body.get("upToId");
        if (upTo == null || !upTo.isIntegralNumber()) {
            throw ValidationException.forField("upToId", "is required (a thread item id)");
        }
        thread.markRead(c.getId(), caller.principal(), upTo.asLong());
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------------- replay

    /** {@code getChronicleState}: every layer's state at {@code at} from chronicle storage only. */
    @GetMapping("/{chronicleId}/state")
    @Operation(operationId = "getChronicleState", tags = "Chronicles")
    @RestrictedRead("excluded sources and the read restriction are inside the replay SQL")
    public Map<String, Object> getChronicleState(@PathVariable String chronicleId,
            @RequestParam(required = false) String at, @RequestParam(required = false) String layer,
            @RequestParam(required = false) String bbox, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        ChronicleEntity c = chronicles.require(chronicleId, caller, AccessAction.VIEW);
        Instant time = instant("at", at);
        if (time == null) {
            throw ValidationException.forField("at", "is required (an RFC 3339 date-time)");
        }
        BboxFilter box = BboxParser.parse(bbox);
        return replay.state(c, time, layer, box == null ? null : box.envelopes(), cursor,
                QueryParams.intInRange("limit", limit, 1, 500, 100), reader(caller, request));
    }

    /** {@code getChronicleTile}: a Mapbox Vector Tile of the chronicle's state at {@code at}. */
    @GetMapping("/{chronicleId}/tiles/{z}/{x}/{y}.mvt")
    @Operation(operationId = "getChronicleTile", tags = "Chronicles")
    @RestrictedRead("excluded sources and the read restriction are inside the MVT SQL")
    public ResponseEntity<byte[]> getChronicleTile(@PathVariable String chronicleId, @PathVariable String z,
            @PathVariable String x, @PathVariable String y, @RequestParam(required = false) String at,
            @RequestParam(required = false) List<String> layers, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        ChronicleEntity c = chronicles.require(chronicleId, caller, AccessAction.VIEW);
        TileCoordinates coordinates = TileCoordinates.parse(z, x, y);
        ChronicleReplayService.TileBytes tile = replay.tile(c, coordinates, instant("at", at),
                QueryParams.multi("layers", layers), reader(caller, request));
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.noCache().cachePrivate());
        headers.set(HttpHeaders.VARY, TilesController.VARY);
        if (tile.truncated()) {
            headers.set(TilesController.TRUNCATED_HEADER, "true");
        }
        return ResponseEntity.ok().headers(headers).contentType(TilesController.MVT).body(tile.bytes());
    }

    /** {@code getChronicleFrames}: counts and changed entities per playback step. */
    @GetMapping("/{chronicleId}/frames")
    @Operation(operationId = "getChronicleFrames", tags = "Chronicles")
    @RestrictedRead("excluded sources and the read restriction are inside the frame SQL")
    public Map<String, Object> getChronicleFrames(@PathVariable String chronicleId,
            @RequestParam(required = false) String from, @RequestParam(required = false) String to,
            @RequestParam(required = false) String step, @RequestParam(required = false) List<String> layers,
            HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        ChronicleEntity c = chronicles.require(chronicleId, caller, AccessAction.VIEW);
        return replay.frames(c, instant("from", from), instant("to", to), duration("step", step),
                QueryParams.multi("layers", layers), reader(caller, request));
    }

    /** {@code getChronicleTrack}: an entity's track from captured versions (037 shape). */
    @GetMapping("/{chronicleId}/tracks/{sourceId}/{externalId}")
    @Operation(operationId = "getChronicleTrack", tags = "Chronicles")
    @RestrictedRead("excluded sources and the read restriction are inside the track SQL")
    public Map<String, Object> getChronicleTrack(@PathVariable String chronicleId, @PathVariable String sourceId,
            @PathVariable String externalId, @RequestParam(required = false) String from,
            @RequestParam(required = false) String to, @RequestParam(required = false) String maxPoints,
            HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        ChronicleEntity c = chronicles.require(chronicleId, caller, AccessAction.VIEW);
        return TimeTravelController.feature(replay.track(c, sourceId, externalId, instant("from", from),
                instant("to", to), QueryParams.intInRange("maxPoints", maxPoints, 2, 10_000, 2_000),
                reader(caller, request)));
    }

    /** {@code getChronicleSeries}: captured series points and rollups. */
    @GetMapping("/{chronicleId}/series")
    @Operation(operationId = "getChronicleSeries", tags = "Chronicles")
    @RestrictedRead("series points were captured from readable layers; a private source's points are not captured"
            + " for callers without private access")
    public Map<String, Object> getChronicleSeries(@PathVariable String chronicleId,
            @RequestParam(required = false) String ref, @RequestParam(required = false) String from,
            @RequestParam(required = false) String to, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        ChronicleEntity c = chronicles.require(chronicleId, caller, AccessAction.VIEW);
        if (ref == null || ref.isBlank()) {
            throw ValidationException.forField("ref", "is required (a series ref)");
        }
        return replay.series(c, ref, instant("from", from), instant("to", to));
    }

    /** {@code getChronicleSummary}: layer counts over time, top entities, links, storage and gaps. */
    @GetMapping("/{chronicleId}/summary")
    @Operation(operationId = "getChronicleSummary", tags = "Chronicles")
    @RestrictedRead("excluded sources and the read restriction are inside the summary SQL")
    public Map<String, Object> getChronicleSummary(@PathVariable String chronicleId, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        ChronicleEntity c = chronicles.require(chronicleId, caller, AccessAction.VIEW);
        return replay.summary(c, caller, reader(caller, request));
    }
}
