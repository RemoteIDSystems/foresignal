package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.user.AuthenticatedSession;
import ai.foresignal.source.view.UserSourceViewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Shared collection and per-user source views (prompt R2-010): {@code getMySources} and {@code updateMySources}
 * ({@code self.sources}) for the caller's own view, {@code getCollectionSources} and {@code updateCollectionSources}
 * ({@code collection.manage}) for the instance collection set. The permissions are enforced by the token filter
 * (R2-004); a view belongs to a user, so the per-user operations need a signed-in session.
 */
@RestController
@Tag(name = "Sources")
public class SourceViewsController {

    private final UserSourceViewService views;

    /**
     * Creates the controller.
     *
     * @param views the source view service
     */
    public SourceViewsController(UserSourceViewService views) {
        this.views = views;
    }

    /** {@code getMySources}: the caller's view, one entry per source they may see. */
    @GetMapping("/v1/me/sources")
    @Operation(operationId = "getMySources", tags = "Sources")
    public Map<String, Object> getMySources(HttpServletRequest request) {
        return views.view(session(request).userId(), CurrentToken.of(request));
    }

    /** {@code updateMySources}: replaces the caller's view. */
    @PutMapping("/v1/me/sources")
    @Operation(operationId = "updateMySources", tags = "Sources")
    public Map<String, Object> updateMySources(@RequestBody(required = false) JsonNode body,
            HttpServletRequest request) {
        return views.replace(session(request).userId(), CurrentToken.of(request), body);
    }

    /** {@code getCollectionSources}: the instance collection set. */
    @GetMapping("/v1/sources/collection")
    @Operation(operationId = "getCollectionSources", tags = "Sources")
    public Map<String, Object> getCollectionSources() {
        return views.collectionSet();
    }

    /** {@code updateCollectionSources}: sets which of the listed sources the instance collects. */
    @PutMapping("/v1/sources/collection")
    @Operation(operationId = "updateCollectionSources", tags = "Sources")
    public Map<String, Object> updateCollectionSources(@RequestBody(required = false) JsonNode body) {
        return views.replaceCollection(body);
    }

    private static AuthenticatedSession session(HttpServletRequest request) {
        return AuthenticatedSession.of(request).orElseThrow(() -> new ForesignalException(HttpStatus.UNAUTHORIZED,
                ErrorCodes.UNAUTHORIZED, "This operation needs a signed-in session, not an API token."));
    }
}
