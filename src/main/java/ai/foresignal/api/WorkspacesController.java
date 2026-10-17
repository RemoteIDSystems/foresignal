package ai.foresignal.api;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.support.PublicUrls;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.object.ContextSummaryService;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.object.ResolveContext;
import ai.foresignal.object.WorkspaceEntity;
import ai.foresignal.object.WorkspaceService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.share.ShareLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The workspace endpoints (prompt 129, tag {@code Workspaces}; docs/Client-Contract.md §5–§6): {@code listWorkspaces},
 * {@code createWorkspace}, {@code getWorkspace}, {@code updateWorkspace}, {@code deleteWorkspace},
 * {@code putWorkspaceLayout}, {@code patchWorkspaceContext} ({@code If-Match}, 412 when stale),
 * {@code getWorkspaceSummary}, {@code getWorkspaceContextLink} and {@code shareWorkspace}. Every operation needs a
 * token; workspaces the caller neither owns nor belongs to are 404 {@code UNKNOWN_WORKSPACE}.
 */
@RestController
@Tag(name = "Workspaces")
public class WorkspacesController {

    /** The header carrying the sender's client id ({@code originClientId}). */
    public static final String CLIENT_ID_HEADER = "Foresignal-Client-Id";

    private final WorkspaceService workspaces;
    private final ContextSummaryService summaries;
    private final ObjectCallers callers;
    private final ReadCallerResolver readCallers;
    private final AppSettingService settings;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the controller. */
    public WorkspacesController(WorkspaceService workspaces, ContextSummaryService summaries, ObjectCallers callers,
            ReadCallerResolver readCallers, AppSettingService settings, JsonMapper json, Clock clock) {
        this.workspaces = workspaces;
        this.summaries = summaries;
        this.callers = callers;
        this.readCallers = readCallers;
        this.settings = settings;
        this.json = json;
        this.clock = clock;
    }

    /** {@code listWorkspaces}: the caller's own and member workspaces (layout omitted). */
    @GetMapping("/v1/workspaces")
    @Operation(operationId = "listWorkspaces", tags = "Workspaces")
    public Map<String, Object> listWorkspaces(@RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        return workspaces.list(callers.authenticated(request), cursor == null || cursor.isBlank() ? null
                : cursor.strip(), QueryParams.intInRange("limit", limit, 1, 200, 50));
    }

    /** {@code createWorkspace}: owned by the caller, context version 1. */
    @PostMapping("/v1/workspaces")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createWorkspace", tags = "Workspaces")
    public ResponseEntity<Map<String, Object>> createWorkspace(@RequestBody JsonNode body,
            HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        WorkspaceEntity w = workspaces.create(body, caller);
        return ResponseEntity.status(HttpStatus.CREATED).eTag(WorkspaceService.etag(w))
                .body(WorkspaceService.view(w, caller, true));
    }

    /** {@code getWorkspace}: with its context, version (also the ETag) and layout. */
    @GetMapping("/v1/workspaces/{workspaceId}")
    @Operation(operationId = "getWorkspace", tags = "Workspaces")
    public ResponseEntity<Map<String, Object>> getWorkspace(@PathVariable String workspaceId,
            HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        WorkspaceEntity w = workspaces.get(workspaceId, caller);
        return ResponseEntity.ok().eTag(WorkspaceService.etag(w)).body(WorkspaceService.view(w, caller, true));
    }

    /** {@code updateWorkspace}: a JSON merge patch of the metadata. */
    @PatchMapping(value = "/v1/workspaces/{workspaceId}", consumes = "application/merge-patch+json")
    @Operation(operationId = "updateWorkspace", tags = "Workspaces")
    public Map<String, Object> updateWorkspace(@PathVariable String workspaceId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        return WorkspaceService.view(workspaces.patch(workspaceId, body, caller), caller, true);
    }

    /** {@code deleteWorkspace}: owner or admin. */
    @DeleteMapping("/v1/workspaces/{workspaceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteWorkspace", tags = "Workspaces")
    public void deleteWorkspace(@PathVariable String workspaceId, HttpServletRequest request) {
        workspaces.delete(workspaceId, callers.authenticated(request));
    }

    /** {@code putWorkspaceLayout}: opaque client JSON, at most 64 KB. */
    @PutMapping("/v1/workspaces/{workspaceId}/layout")
    @Operation(operationId = "putWorkspaceLayout", tags = "Workspaces")
    public Map<String, Object> putWorkspaceLayout(@PathVariable String workspaceId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        return WorkspaceService.view(workspaces.putLayout(workspaceId, body, caller), caller, true);
    }

    /** {@code patchWorkspaceContext}: a JSON merge patch with {@code If-Match: <contextVersion>}. */
    @PatchMapping(value = "/v1/workspaces/{workspaceId}/context", consumes = "application/merge-patch+json")
    @Operation(operationId = "patchWorkspaceContext", tags = "Workspaces")
    public ResponseEntity<Map<String, Object>> patchWorkspaceContext(@PathVariable String workspaceId,
            @RequestHeader(value = "If-Match", required = false) String ifMatch,
            @RequestHeader(value = CLIENT_ID_HEADER, required = false) String clientId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        WorkspaceEntity w = workspaces.patchContext(workspaceId, ifMatch, body,
                clientId == null || clientId.isBlank() ? null : clientId.strip(), callers.authenticated(request));
        return ResponseEntity.ok().eTag(WorkspaceService.etag(w)).body(WorkspaceService.contextState(w));
    }

    /** {@code getWorkspaceSummary}: the context summary of the workspace's current context. */
    @GetMapping("/v1/workspaces/{workspaceId}/summary")
    @Operation(operationId = "getWorkspaceSummary", tags = "Workspaces")
    public Map<String, Object> getWorkspaceSummary(@PathVariable String workspaceId, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        WorkspaceEntity w = workspaces.get(workspaceId, caller);
        JsonNode summaryRequest = ContextSummaryService.requestOf(w.getContext(), json);
        if (summaryRequest == null) {
            throw new ConflictException(ErrorCodes.WORKSPACE_CONTEXT_INCOMPLETE,
                    "The workspace context has no viewport or time range to summarize.");
        }
        return summaries.summarize(summaryRequest, new ResolveContext(caller, null));
    }

    /** {@code getWorkspaceContextLink}: the deep-link payload of the current context. */
    @GetMapping("/v1/workspaces/{workspaceId}/context-link")
    @Operation(operationId = "getWorkspaceContextLink", tags = "Workspaces")
    public Map<String, Object> getWorkspaceContextLink(@PathVariable String workspaceId, HttpServletRequest request) {
        return workspaces.contextLink(workspaceId, callers.authenticated(request));
    }

    /** {@code shareWorkspace}: a read-only 112 share link to the workspace context (agent). */
    @PostMapping("/v1/workspaces/{workspaceId}/share")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "shareWorkspace", tags = "Workspaces")
    public Map<String, Object> shareWorkspace(@PathVariable String workspaceId,
            @RequestBody(required = false) JsonNode body, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        ShareLinkService.Created created = workspaces.share(workspaceId, body, caller,
                ShareLinksController.caller(request, readCallers));
        Map<String, Object> out = ShareLinkService.json(created.link(), clock.instant());
        out.put("key", created.key());
        out.put("url", PublicUrls.base(settings, request) + "/v1/k/" + created.key());
        return out;
    }
}
