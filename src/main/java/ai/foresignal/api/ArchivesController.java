package ai.foresignal.api;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.export.ArchiveManifestStore;
import ai.foresignal.export.ArchiveRunner;
import ai.foresignal.export.ArchiveSegment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The cold-storage archive inventory and runs (OpenAPI tag {@code Archive}, prompt R2-016; admin,
 * {@code storage.manage}): {@code listArchives}, {@code runArchive} and {@code restoreArchive}. The settings stay on
 * {@link ArchiveController} ({@code /v1/settings/archive}).
 *
 * <p>Left out in edge mode, like the rest of the archive (prompt 113).
 */
@RestController
@Tag(name = "Archive")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = ai.foresignal.config.EdgeMode.PROPERTY,
        havingValue = ai.foresignal.config.EdgeMode.FULL, matchIfMissing = true)
public class ArchivesController {

    private final ArchiveManifestStore manifests;
    private final ArchiveRunner runner;

    /** Creates the controller. */
    public ArchivesController(ArchiveManifestStore manifests, ArchiveRunner runner) {
        this.manifests = manifests;
        this.runner = runner;
    }

    /** {@code listArchives}: the segments, newest first, optionally of one status. */
    @GetMapping("/v1/archives")
    @Operation(operationId = "listArchives", tags = "Archive")
    public Map<String, Object> listArchives(@RequestParam(required = false) String status,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit) {
        if (status != null && !ArchiveSegment.STATUSES.contains(status)) {
            throw ValidationException.forField("status", "must be one of " + String.join(", ",
                    ArchiveSegment.STATUSES));
        }
        ArchiveManifestStore.Page page;
        try {
            page = manifests.page(status, cursor == null || cursor.isBlank() ? null : cursor,
                    BulkExportsController.parseLimit(limit));
        } catch (IllegalArgumentException e) {
            throw ValidationException.forField("cursor", e.getMessage());
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", page.items().stream().map(ArchivesController::view).toList());
        out.put("nextCursor", page.nextCursor());
        return out;
    }

    /** {@code runArchive}: starts a run now (202 with the segment), or answers a dry run. */
    @PostMapping("/v1/archives/run")
    @Operation(operationId = "runArchive", tags = "Archive")
    public ResponseEntity<Map<String, Object>> runArchive(@RequestBody(required = false) JsonNode body) {
        ArchiveRunner.Request request = request(body);
        ArchiveSegment segment = runner.start(request, ArchiveRunner.MANUAL);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(view(segment));
    }

    /** {@code restoreArchive}: starts restoring a segment (202); {@code confirm: true} is required. */
    @PostMapping("/v1/archives/{id}/restore")
    @Operation(operationId = "restoreArchive", tags = "Archive")
    public ResponseEntity<Map<String, Object>> restoreArchive(@PathVariable String id,
            @RequestBody(required = false) JsonNode body) {
        if (body == null || !body.isObject() || !body.has("confirm") || !body.get("confirm").isBoolean()
                || !body.get("confirm").asBoolean()) {
            throw ValidationException.forField("confirm", "must be true: a restore may be large and slow");
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(view(runner.restore(id)));
    }

    /** Parses an {@code ArchiveRunRequest}; every problem in one 400. */
    static ArchiveRunner.Request request(JsonNode body) {
        if (body == null || body.isNull()) {
            return new ArchiveRunner.Request(null, List.of(), false);
        }
        if (!body.isObject()) {
            throw new ValidationException("The request body must be an ArchiveRunRequest object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String name : body.propertyNames()) {
            if (!List.of("cutoff", "sources", "dryRun").contains(name)) {
                errors.add(new FieldError(name, "is not a member of ArchiveRunRequest"));
            }
        }
        Instant cutoff = null;
        JsonNode cutoffNode = body.get("cutoff");
        if (cutoffNode != null && !cutoffNode.isNull()) {
            try {
                cutoff = Instant.parse(cutoffNode.asString());
            } catch (DateTimeParseException | UnsupportedOperationException e) {
                errors.add(new FieldError("cutoff", "must be a date-time such as 2025-01-01T00:00:00Z"));
            }
        }
        List<String> sources = new ArrayList<>();
        JsonNode sourcesNode = body.get("sources");
        if (sourcesNode != null && !sourcesNode.isNull()) {
            if (!sourcesNode.isArray()) {
                errors.add(new FieldError("sources", "must be an array of source ids"));
            } else {
                for (JsonNode item : sourcesNode) {
                    if (!item.isString() || item.asString().isBlank()) {
                        errors.add(new FieldError("sources", "must hold source ids"));
                    } else {
                        sources.add(item.asString());
                    }
                }
            }
        }
        boolean dryRun = false;
        JsonNode dryRunNode = body.get("dryRun");
        if (dryRunNode != null && !dryRunNode.isNull()) {
            if (!dryRunNode.isBoolean()) {
                errors.add(new FieldError("dryRun", "must be a boolean"));
            } else {
                dryRun = dryRunNode.asBoolean();
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The archive run request is not valid.", errors);
        }
        return new ArchiveRunner.Request(cutoff, sources, dryRun);
    }

    /** The {@code Archive} view of a segment. */
    static Map<String, Object> view(ArchiveSegment segment) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", segment.id());
        out.put("status", segment.status());
        out.put("createdAt", segment.createdAt());
        out.put("cutoff", segment.cutoff());
        out.put("target", segment.target());
        out.put("location", segment.location());
        out.put("sizeBytes", segment.sizeBytes());
        out.put("itemCount", segment.itemCount());
        out.put("checksum", segment.checksum());
        out.put("purgedFromHot", segment.purgedFromHot());
        out.put("restoredAt", segment.restoredAt());
        out.put("trigger", segment.trigger());
        out.put("domain", segment.domain());
        out.put("sources", segment.sources());
        out.put("fromSeq", segment.fromSeq());
        out.put("toSeq", segment.toSeq());
        out.put("fromTime", segment.fromTime());
        out.put("toTime", segment.toTime());
        out.put("files", segment.files().stream().map(ArchivesController::file).toList());
        out.put("error", segment.error());
        out.put("updatedAt", segment.updatedAt());
        out.put("completedAt", segment.completedAt());
        out.put("purgedAt", segment.purgedAt());
        return out;
    }

    private static Map<String, Object> file(ArchiveSegment.SegmentFile file) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("name", file.name());
        out.put("sourceId", file.sourceId());
        out.put("tier", file.tier());
        out.put("fromSeq", file.fromSeq());
        out.put("toSeq", file.toSeq());
        out.put("fromTime", file.fromTime());
        out.put("toTime", file.toTime());
        out.put("rows", file.rows());
        out.put("bytes", file.bytes());
        out.put("sha256", file.sha256());
        out.put("location", file.location());
        return out;
    }
}
