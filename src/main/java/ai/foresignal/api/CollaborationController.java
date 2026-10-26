package ai.foresignal.api;

import ai.foresignal.api.dto.ApiTokenDto;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.collab.GroupService;
import ai.foresignal.collab.MeService;
import ai.foresignal.collab.NotificationPreferenceService;
import ai.foresignal.collab.PersonalInbox;
import ai.foresignal.collab.PrincipalAdminService;
import ai.foresignal.collab.PrincipalViews;
import ai.foresignal.collab.ShareService;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.user.AuthenticatedSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Principals, groups, preferences, the personal inbox, access lists and shares (prompt 130, tag
 * {@code Collaboration}): {@code getMe}, {@code listMyNotificationPreferences},
 * {@code replaceMyNotificationPreferences}, {@code listInbox}, {@code listPrincipals}, {@code createPrincipal},
 * {@code getPrincipal}, {@code updatePrincipal}, {@code createPrincipalToken}, {@code listGroups},
 * {@code createGroup}, {@code getGroup}, {@code updateGroup}, {@code deleteGroup}, {@code addGroupMembers},
 * {@code removeGroupMembers}, {@code getObjectAcl}, {@code replaceObjectAcl}, {@code shareObject},
 * {@code listSharedWithMe} and {@code listMyObjects}. Every operation needs a token.
 */
@RestController
@Tag(name = "Collaboration")
public class CollaborationController {

    private final ObjectCallers callers;
    private final MeService me;
    private final NotificationPreferenceService preferences;
    private final PersonalInbox inbox;
    private final PrincipalAdminService principals;
    private final GroupService groups;
    private final ShareService shares;

    /** Creates the controller. */
    public CollaborationController(ObjectCallers callers, MeService me, NotificationPreferenceService preferences,
            PersonalInbox inbox, PrincipalAdminService principals, GroupService groups, ShareService shares) {
        this.callers = callers;
        this.me = me;
        this.preferences = preferences;
        this.inbox = inbox;
        this.principals = principals;
        this.groups = groups;
        this.shares = shares;
    }

    // ------------------------------------------------------------------------- me

    /** {@code getMe}: the calling principal, token, scopes, groups and preferences. */
    @GetMapping("/v1/me")
    @Operation(operationId = "getMe", tags = "Collaboration")
    public Map<String, Object> getMe(HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        CurrentToken token = CurrentToken.of(request).orElseThrow(() -> new ForesignalException(
                HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "This operation needs a token."));
        return me.me(token.token(), caller,
                AuthenticatedSession.of(request).orElse(null));
    }

    /** {@code listMyNotificationPreferences}. */
    @GetMapping("/v1/me/notification-preferences")
    @Operation(operationId = "listMyNotificationPreferences", tags = "Collaboration")
    public List<Map<String, Object>> listMyNotificationPreferences(HttpServletRequest request) {
        return preferences.list(callers.authenticated(request).principal());
    }

    /** {@code replaceMyNotificationPreferences}. */
    @PutMapping("/v1/me/notification-preferences")
    @Operation(operationId = "replaceMyNotificationPreferences", tags = "Collaboration")
    public List<Map<String, Object>> replaceMyNotificationPreferences(@RequestBody JsonNode body,
            HttpServletRequest request) {
        return preferences.replace(callers.authenticated(request), body);
    }

    /** {@code listInbox}: the caller's personal inbox, newest first. */
    @GetMapping("/v1/inbox")
    @Operation(operationId = "listInbox", tags = "Collaboration")
    public Map<String, Object> listInbox(@RequestParam(required = false) String unread,
            @RequestParam(required = false) String category, @RequestParam(required = false) String since,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit,
            HttpServletRequest request) {
        return inbox.list(callers.authenticated(request), QueryParams.bool("unread", unread, false),
                category == null || category.isBlank() ? null : category.strip(), QueryParams.instant("since", since),
                cursor, QueryParams.intInRange("limit", limit, 1, 200, 50));
    }

    // ------------------------------------------------------------------------- principals

    /** {@code listPrincipals}: handles, display names, kinds and avatars only. */
    @GetMapping("/v1/principals")
    @Operation(operationId = "listPrincipals", tags = "Collaboration")
    public Map<String, Object> listPrincipals(@RequestParam(required = false) String q,
            @RequestParam(required = false) String kind, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        callers.authenticated(request);
        return principals.list(q, kind, cursor, QueryParams.intInRange("limit", limit, 1, 500, 100));
    }

    /** {@code createPrincipal} (admin). */
    @PostMapping("/v1/principals")
    @Operation(operationId = "createPrincipal", tags = "Collaboration")
    @ApiResponse(responseCode = "201", description = "Created")
    public ResponseEntity<Map<String, Object>> createPrincipal(@RequestBody JsonNode body,
            HttpServletRequest request) {
        PrincipalEntity p = principals.create(body, callers.authenticated(request));
        return ResponseEntity.created(URI.create("/v1/principals/" + p.getId())).body(PrincipalViews.full(p, 0));
    }

    /** {@code getPrincipal}: the summary, or the full principal for admins and the principal itself. */
    @GetMapping("/v1/principals/{principalId}")
    @Operation(operationId = "getPrincipal", tags = "Collaboration")
    public Map<String, Object> getPrincipal(@PathVariable String principalId, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        PrincipalEntity p = principals.get(principalId);
        if (caller.admin() || p.getId().equals(caller.principal())) {
            return PrincipalViews.full(p, principals.tokenCount(p.getId()));
        }
        return PrincipalViews.summary(p);
    }

    /** {@code updatePrincipal} (admin; JSON merge patch). */
    @PatchMapping(value = "/v1/principals/{principalId}", consumes = "application/merge-patch+json")
    @Operation(operationId = "updatePrincipal", tags = "Collaboration")
    public Map<String, Object> updatePrincipal(@PathVariable String principalId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        PrincipalEntity p = principals.patch(principalId, body, callers.authenticated(request));
        return PrincipalViews.full(p, principals.tokenCount(p.getId()));
    }

    /** {@code createPrincipalToken} (admin): a token owned by the principal, shown once. */
    @PostMapping("/v1/principals/{principalId}/tokens")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createPrincipalToken", tags = "Collaboration")
    public ApiTokenDto createPrincipalToken(@PathVariable String principalId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        TokenService.CreatedToken created = principals.issueToken(principalId, body, callers.authenticated(request));
        return ApiTokenDto.of(created.entity(), created.principalHandle(), created.plaintext());
    }

    // ------------------------------------------------------------------------- groups

    /** {@code listGroups}. */
    @GetMapping("/v1/groups")
    @Operation(operationId = "listGroups", tags = "Collaboration")
    public Map<String, Object> listGroups(@RequestParam(required = false) String q,
            @RequestParam(required = false) String member, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        callers.authenticated(request);
        return groups.list(q, member, cursor, QueryParams.intInRange("limit", limit, 1, 500, 100));
    }

    /** {@code createGroup} (agent): the creator becomes a manager. */
    @PostMapping("/v1/groups")
    @Operation(operationId = "createGroup", tags = "Collaboration")
    @ApiResponse(responseCode = "201", description = "Created")
    public ResponseEntity<Map<String, Object>> createGroup(@RequestBody JsonNode body, HttpServletRequest request) {
        var g = groups.create(body, callers.authenticated(request));
        return ResponseEntity.created(URI.create("/v1/groups/" + g.getId())).body(groups.view(g, true));
    }

    /** {@code getGroup}: with members. */
    @GetMapping("/v1/groups/{groupId}")
    @Operation(operationId = "getGroup", tags = "Collaboration")
    public Map<String, Object> getGroup(@PathVariable String groupId, HttpServletRequest request) {
        callers.authenticated(request);
        return groups.view(groups.get(groupId), true);
    }

    /** {@code updateGroup} (managers or admin). */
    @PatchMapping(value = "/v1/groups/{groupId}", consumes = "application/merge-patch+json")
    @Operation(operationId = "updateGroup", tags = "Collaboration")
    public Map<String, Object> updateGroup(@PathVariable String groupId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        return groups.view(groups.patch(groupId, body, callers.authenticated(request)), true);
    }

    /** {@code deleteGroup} (managers or admin). */
    @DeleteMapping("/v1/groups/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteGroup", tags = "Collaboration")
    public void deleteGroup(@PathVariable String groupId, HttpServletRequest request) {
        groups.delete(groupId, callers.authenticated(request));
    }

    /** {@code addGroupMembers} (managers or admin). */
    @PostMapping("/v1/groups/{groupId}/members")
    @Operation(operationId = "addGroupMembers", tags = "Collaboration")
    public Map<String, Object> addGroupMembers(@PathVariable String groupId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        return groups.view(groups.addMembers(groupId, body, callers.authenticated(request)), true);
    }

    /**
     * {@code removeGroupMembers} (managers or admin; a principal may remove itself): {@code ?principalId=},
     * repeatable or comma-separated.
     */
    @DeleteMapping("/v1/groups/{groupId}/members")
    @Operation(operationId = "removeGroupMembers", tags = "Collaboration")
    public Map<String, Object> removeGroupMembers(@PathVariable String groupId,
            @RequestParam(required = false) List<String> principalId, HttpServletRequest request) {
        return groups.view(groups.removeMembers(groupId, QueryParams.multi("principalId", principalId),
                callers.authenticated(request)), true);
    }

    // ------------------------------------------------------------------------- access lists

    /** {@code getObjectAcl} (owner or editor). */
    @GetMapping("/v1/objects/{ref}/acl")
    @Operation(operationId = "getObjectAcl", tags = "Collaboration")
    public Map<String, Object> getObjectAcl(@PathVariable String ref, HttpServletRequest request) {
        return shares.acl(ref, callers.authenticated(request));
    }

    /** {@code replaceObjectAcl} (agent; owner or editor). */
    @PutMapping("/v1/objects/{ref}/acl")
    @Operation(operationId = "replaceObjectAcl", tags = "Collaboration")
    public Map<String, Object> replaceObjectAcl(@PathVariable String ref, @RequestBody JsonNode body,
            HttpServletRequest request) {
        return shares.replace(ref, body, callers.authenticated(request), "replaceObjectAcl");
    }

    /** {@code shareObject} (agent; owner or editor): notifies the grantees. */
    @PostMapping("/v1/objects/{ref}/share")
    @Operation(operationId = "shareObject", tags = "Collaboration")
    public Map<String, Object> shareObject(@PathVariable String ref, @RequestBody JsonNode body,
            HttpServletRequest request) {
        return shares.share(ref, body, callers.authenticated(request), "shareObject");
    }

    /** {@code listSharedWithMe}. */
    @GetMapping("/v1/shared-with-me")
    @Operation(operationId = "listSharedWithMe", tags = "Collaboration")
    public Map<String, Object> listSharedWithMe(@RequestParam(required = false) String type,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit,
            HttpServletRequest request) {
        return shares.sharedWithMe(callers.authenticated(request), blank(type), cursor,
                QueryParams.intInRange("limit", limit, 1, 200, 50));
    }

    /** {@code listMyObjects}. */
    @GetMapping("/v1/my")
    @Operation(operationId = "listMyObjects", tags = "Collaboration")
    public Map<String, Object> listMyObjects(@RequestParam(required = false) String type,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit,
            HttpServletRequest request) {
        return shares.mine(callers.authenticated(request), blank(type), cursor,
                QueryParams.intInRange("limit", limit, 1, 200, 50));
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
