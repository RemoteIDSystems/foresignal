package ai.foresignal.api;

import ai.foresignal.manifest.ManifestReplayService;
import ai.foresignal.manifest.ManifestService;
import ai.foresignal.security.CurrentToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Analysis manifests (prompt 116, agent): read a manifest and queue its replay as a 103 job (poll it with
 * {@code GET /v1/jobs/{jobId}}).
 */
@RestController
@Tag(name = "Evidence")
@RequestMapping("/v1/manifests")
public class ManifestsController {

    private final ManifestService manifests;
    private final ManifestReplayService replays;

    /** Creates the controller. */
    public ManifestsController(ManifestService manifests, ManifestReplayService replays) {
        this.manifests = manifests;
        this.replays = replays;
    }

    /** {@code getManifest}. */
    @GetMapping("/{manifestId}")
    @Operation(operationId = "getManifest", tags = "Evidence")
    public Map<String, Object> getManifest(@PathVariable String manifestId) {
        return manifests.get(manifestId).view();
    }

    /** {@code replayManifest}: 202 with the queued job. */
    @PostMapping("/{manifestId}/replay")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(operationId = "replayManifest", tags = "Evidence")
    public Map<String, Object> replayManifest(@PathVariable String manifestId, HttpServletRequest request) {
        String tokenId = CurrentToken.of(request).map(c -> c.id().toString()).orElse(null);
        return replays.submit(manifestId, tokenId);
    }
}
