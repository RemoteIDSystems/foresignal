package ai.foresignal.inbound;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.mapping.MappingPreviewService;
import ai.foresignal.mapping.SampleCapture;
import ai.foresignal.security.user.AuthenticatedSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * A signed-in user's own custom sources under {@code /v1/me/custom-sources} (prompt SG-001): the self-service twin of
 * the operator {@link CustomSourcesController} ({@code /v1/custom-sources}). A user holding {@code self.sources}
 * defines, lists, reads, updates, deletes and re-keys the custom sources they own, tests a mapping against their
 * captured samples, and reads those samples.
 *
 * <p>Custom sources belong to a user, so every operation here needs a signed-in session; an API token is {@code 401}.
 * Create forces {@code owner_user_id} to the caller through {@link CustomSourceService#create(JsonNode, UUID)}; every
 * other operation resolves the row by id <em>and</em> owner ({@link CustomSourceService#getOwned}), so a source the
 * caller does not own — an operator-created source, an external provider, another user's source, or an unknown id — is
 * {@code 404}, never {@code 403} and never another user's data. The created-source shape (the one-time ingest key and
 * any preset setup) and the row view are shared with the operator controller, so the two surfaces agree. The operator
 * {@code /v1/custom-sources} surface is unchanged.
 */
@RestController
@Tag(name = "Sources")
public class MyCustomSourcesController {

    private final CustomSourceService service;
    private final CustomSourcesController operator;
    private final MappingPreviewService preview;
    private final SampleCapture samples;
    private final IngestRecordLimiter recordLimiter;

    /** Creates the controller. */
    public MyCustomSourcesController(CustomSourceService service, CustomSourcesController operator,
            MappingPreviewService preview, SampleCapture samples, IngestRecordLimiter recordLimiter) {
        this.service = service;
        this.operator = operator;
        this.preview = preview;
        this.samples = samples;
        this.recordLimiter = recordLimiter;
    }

    /** {@code listMyCustomSources}: the caller's custom sources, ordered by id. */
    @GetMapping("/v1/me/custom-sources")
    @Operation(operationId = "listMyCustomSources", tags = "Sources")
    public List<Map<String, Object>> listMyCustomSources(HttpServletRequest request) {
        UUID owner = requireSession(request).userId();
        return service.listOwned(owner).stream().map(operator::view).toList();
    }

    /** {@code createMyCustomSource}: 201 with the ingest key, shown once; {@code ownerUserId} is forced to the caller. */
    @PostMapping("/v1/me/custom-sources")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createMyCustomSource", tags = "Sources")
    public Map<String, Object> createMyCustomSource(@RequestBody JsonNode body, HttpServletRequest request) {
        UUID owner = requireSession(request).userId();
        return operator.createdResponse(service.create(body, owner), request);
    }

    /** {@code getMyCustomSource}: 404 unless the caller owns it. */
    @GetMapping("/v1/me/custom-sources/{sourceId}")
    @Operation(operationId = "getMyCustomSource", tags = "Sources")
    public Map<String, Object> getMyCustomSource(@PathVariable String sourceId, HttpServletRequest request) {
        UUID owner = requireSession(request).userId();
        return operator.view(service.getOwned(sourceId, owner));
    }

    /** {@code updateMyCustomSource}: a full replace of a source the caller owns; 404 otherwise. */
    @PutMapping("/v1/me/custom-sources/{sourceId}")
    @Operation(operationId = "updateMyCustomSource", tags = "Sources")
    public Map<String, Object> updateMyCustomSource(@PathVariable String sourceId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        UUID owner = requireSession(request).userId();
        return operator.view(service.update(sourceId, body, owner));
    }

    /** {@code deleteMyCustomSource}: 204; 404 unless the caller owns it. */
    @DeleteMapping("/v1/me/custom-sources/{sourceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteMyCustomSource", tags = "Sources")
    public void deleteMyCustomSource(@PathVariable String sourceId, HttpServletRequest request) {
        UUID owner = requireSession(request).userId();
        service.delete(sourceId, owner);
        recordLimiter.forget(sourceId);
    }

    /** {@code rotateMyCustomSourceIngestKey}: the new key, shown once; 404 unless the caller owns the source. */
    @PostMapping("/v1/me/custom-sources/{sourceId}/ingest-key/rotate")
    @Operation(operationId = "rotateMyCustomSourceIngestKey", tags = "Sources")
    public Map<String, Object> rotateMyCustomSourceIngestKey(@PathVariable String sourceId,
            HttpServletRequest request) {
        UUID owner = requireSession(request).userId();
        return Map.of("ingestKey", service.rotateKey(sourceId, owner));
    }

    /** {@code testMyCustomSourceMapping}: the proposed or stored mapping over the caller's samples; 404 if not owned. */
    @PostMapping("/v1/me/custom-sources/{sourceId}/mapping/test")
    @Operation(operationId = "testMyCustomSourceMapping", tags = "Sources")
    public Map<String, Object> testMyCustomSourceMapping(@PathVariable String sourceId,
            @RequestBody(required = false) JsonNode body, HttpServletRequest request) {
        UUID owner = requireSession(request).userId();
        service.getOwned(sourceId, owner);
        return preview.test(sourceId, body);
    }

    /** {@code listMyCustomSourceSamples}: the captured raw requests, newest first; 404 unless the caller owns it. */
    @GetMapping("/v1/me/custom-sources/{sourceId}/samples")
    @Operation(operationId = "listMyCustomSourceSamples", tags = "Sources")
    public List<Map<String, Object>> listMyCustomSourceSamples(@PathVariable String sourceId,
            HttpServletRequest request) {
        UUID owner = requireSession(request).userId();
        service.getOwned(sourceId, owner);
        return samples.list(sourceId).stream().map(SampleCapture.Sample::view).toList();
    }

    /** The request's session, or {@code 401}: these operations are never reachable with an API token. */
    private static AuthenticatedSession requireSession(HttpServletRequest request) {
        return AuthenticatedSession.of(request).orElseThrow(() -> new ForesignalException(HttpStatus.UNAUTHORIZED,
                ErrorCodes.UNAUTHORIZED, "This operation needs a signed-in session, not an API token."));
    }
}
