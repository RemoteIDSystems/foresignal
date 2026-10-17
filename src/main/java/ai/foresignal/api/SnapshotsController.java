package ai.foresignal.api;

import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.export.ExportJobEntity;
import ai.foresignal.export.SnapshotService;
import ai.foresignal.raster.ProductUrls;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * {@code atSeq} snapshots (OpenAPI tag {@code Exports}, prompt 111; admin). A snapshot is an export
 * job of kind {@code SNAPSHOT}, polled and downloaded through {@code /v1/exports/{id}}.
 *
 * <p>Left out in edge mode (prompt 113: exports (snapshots)).
 */
@RestController
@Tag(name = "Exports")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = ai.foresignal.config.EdgeMode.PROPERTY,
        havingValue = ai.foresignal.config.EdgeMode.FULL, matchIfMissing = true)
public class SnapshotsController {

    private final SnapshotService snapshots;
    private final ReadCallerResolver callers;
    private final ProductUrls urls;

    /** Creates the controller. */
    public SnapshotsController(SnapshotService snapshots, ReadCallerResolver callers, ProductUrls urls) {
        this.snapshots = snapshots;
        this.callers = callers;
        this.urls = urls;
    }

    /** {@code createSnapshot}: the state at a seq, 202 with the {@code Snapshot} job. */
    @PostMapping("/v1/snapshots")
    @Operation(operationId = "createSnapshot", tags = "Exports")
    public ResponseEntity<Map<String, Object>> createSnapshot(@RequestBody JsonNode body, HttpServletRequest request) {
        ExportJobEntity job = snapshots.create(body, ExportCallers.of(request, callers));
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .location(URI.create("/v1/exports/" + job.getId()))
                .body(snapshots.view(job, urls.base(request)));
    }
}
