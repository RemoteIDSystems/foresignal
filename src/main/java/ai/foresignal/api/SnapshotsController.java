package ai.foresignal.api;

import ai.foresignal.export.ExportJobEntity;
import ai.foresignal.export.SnapshotService;
import ai.foresignal.security.restrict.RequestPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/** {@code atSeq} snapshots (OpenAPI tag {@code Exports}, prompt 111). */
@RestController
@Tag(name = "Exports")
public class SnapshotsController {

    private final SnapshotService snapshots;

    /** Creates the controller. */
    public SnapshotsController(SnapshotService snapshots) {
        this.snapshots = snapshots;
    }

    /** {@code createSnapshot}: build the state at a seq. */
    @PostMapping("/v1/snapshots")
    @Operation(operationId = "createSnapshot", tags = "Exports")
    public ResponseEntity<Map<String, Object>> createSnapshot(@RequestBody JsonNode body, HttpServletRequest request) {
        ExportJobEntity job = snapshots.create(body, tokenId(request));
        return ResponseEntity.status(org.springframework.http.HttpStatus.ACCEPTED)
                .location(URI.create("/v1/exports/" + job.getId()))
                .body(snapshots.view(job));
    }

    private static String tokenId(HttpServletRequest request) {
        return RequestPrincipal.of(request).map(p -> p.id() == null ? null : p.id().toString()).orElse(null);
    }
}
