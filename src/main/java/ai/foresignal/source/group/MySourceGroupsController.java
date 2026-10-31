package ai.foresignal.source.group;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.security.user.AuthenticatedSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * A signed-in user's own source groups under {@code /v1/me/source-groups} (prompt SG-002). A user holding
 * {@code self.sources} creates, lists (cursor-paged), reads, updates and deletes named sets of member sources, each
 * with its own correlation and notification threshold. A group belongs to a user, so every operation needs a signed-in
 * session; an API token is {@code 401}. The owner is forced to the caller; a group the caller does not own is
 * {@code 404}, as for one that does not exist.
 */
@RestController
@Tag(name = "Sources")
public class MySourceGroupsController {

    private final SourceGroupService groups;

    /** Creates the controller. */
    public MySourceGroupsController(SourceGroupService groups) {
        this.groups = groups;
    }

    /** {@code listMySourceGroups}: {@code SourceGroupPage} ({@code items} + {@code nextCursor}). */
    @GetMapping("/v1/me/source-groups")
    @Operation(operationId = "listMySourceGroups", tags = "Sources")
    public Map<String, Object> listMySourceGroups(@RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        UUID owner = session(request).userId();
        int size = QueryParams.intInRange("limit", limit, 1, SourceGroupService.MAX_LIMIT,
                SourceGroupService.DEFAULT_LIMIT);
        SourceGroupService.Page page = groups.list(owner, cursor, size);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", page.items().stream().map(MySourceGroupsController::view).toList());
        out.put("nextCursor", page.nextCursor());
        return out;
    }

    /** {@code createMySourceGroup}: 201 with {@code Location}; {@code ownerUserId} is forced to the caller. */
    @PostMapping("/v1/me/source-groups")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createMySourceGroup", tags = "Sources")
    public ResponseEntity<Map<String, Object>> createMySourceGroup(@RequestBody JsonNode body,
            HttpServletRequest request) {
        UUID owner = session(request).userId();
        SourceGroupEntity group = groups.create(body, owner);
        return ResponseEntity.created(URI.create("/v1/me/source-groups/" + group.getId())).body(view(group));
    }

    /** {@code getMySourceGroup}: 404 unless the caller owns it. */
    @GetMapping("/v1/me/source-groups/{groupId}")
    @Operation(operationId = "getMySourceGroup", tags = "Sources")
    public Map<String, Object> getMySourceGroup(@PathVariable String groupId, HttpServletRequest request) {
        return view(groups.get(groupId, session(request).userId()));
    }

    /** {@code updateMySourceGroup}: a full replace of a group the caller owns; 404 otherwise. */
    @PutMapping("/v1/me/source-groups/{groupId}")
    @Operation(operationId = "updateMySourceGroup", tags = "Sources")
    public Map<String, Object> updateMySourceGroup(@PathVariable String groupId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        return view(groups.update(groupId, body, session(request).userId()));
    }

    /** {@code deleteMySourceGroup}: 204; 404 unless the caller owns it. */
    @DeleteMapping("/v1/me/source-groups/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteMySourceGroup", tags = "Sources")
    public void deleteMySourceGroup(@PathVariable String groupId, HttpServletRequest request) {
        groups.delete(groupId, session(request).userId());
    }

    /** The {@code SourceGroup} view of {@code group}. */
    static Map<String, Object> view(SourceGroupEntity group) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", group.getId());
        out.put("name", group.getName());
        out.put("description", group.getDescription());
        out.put("sourceIds", group.getSourceIds());
        out.put("kinds", group.getKinds());
        out.put("correlation", group.getCorrelation());
        out.put("notificationThreshold", group.getNotificationThreshold());
        out.put("ownerUserId", group.getOwnerUserId() == null ? null : group.getOwnerUserId().toString());
        out.put("editionId", group.getEditionId() == null ? null : group.getEditionId().toString());
        out.put("createdAt", group.getCreatedAt());
        out.put("updatedAt", group.getUpdatedAt());
        return out;
    }

    private static AuthenticatedSession session(HttpServletRequest request) {
        return AuthenticatedSession.of(request).orElseThrow(() -> new ForesignalException(HttpStatus.UNAUTHORIZED,
                ErrorCodes.UNAUTHORIZED, "This operation needs a signed-in session, not an API token."));
    }
}
