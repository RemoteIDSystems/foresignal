package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.user.AuthenticatedSession;
import ai.foresignal.security.user.UserAdminService;
import ai.foresignal.security.user.UserDataRestrictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The {@code Users} operations (prompt R2-005): {@code listUsers}, {@code createUser}, {@code getUser},
 * {@code updateUser}, {@code deleteUser}, {@code setUserRoles}, {@code suspendUser}, {@code unsuspendUser},
 * {@code resendUserInvite} and {@code adminResetUserPassword}.
 *
 * <p>All are admin-scoped with {@code x-foresignal-permission: users.manage}: the token filter lets through a session
 * holding that permission or an admin API token (R2-004). The acting admin, when the caller is a session, is recorded
 * as the creator of an invited user and is the first choice to receive a deleted user's configuration.
 *
 * <p>A one-time link that was not emailed comes back once in the response ({@code UserLinkIssued}: on
 * {@code createUser} as the user's {@code activation}, and as the {@code 202} body of {@code resendUserInvite} and
 * {@code adminResetUserPassword}); an emailed one is reported without its token.
 */
@RestController
@Tag(name = "Users")
public class UserAdminController {

    private final UserAdminService service;
    private final UserDataRestrictionService dataRestrictions;

    /**
     * Creates the controller.
     *
     * @param service user management
     * @param dataRestrictions the users' data restrictions (R2-013)
     */
    public UserAdminController(UserAdminService service, UserDataRestrictionService dataRestrictions) {
        this.service = service;
        this.dataRestrictions = dataRestrictions;
    }

    /** {@code listUsers}: a page of users, filtered by status and text. */
    @GetMapping("/v1/users")
    @Operation(operationId = "listUsers", tags = "Users")
    public Map<String, Object> listUsers(@RequestParam(required = false) String status,
            @RequestParam(required = false) String q, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        return service.list(status, q, cursor, QueryParams.intInRange("limit", limit, 1, 500, 100));
    }

    /** {@code createUser}: invites a user. */
    @PostMapping("/v1/users")
    @Operation(operationId = "createUser", tags = "Users")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createUser(@RequestBody JsonNode body, HttpServletRequest request) {
        requireObject(body);
        boolean sendInvite = true;
        JsonNode invite = body.get("sendInvite");
        if (invite != null && !invite.isNull()) {
            if (!invite.isBoolean()) {
                throw ValidationException.forField("sendInvite", "must be a boolean");
            }
            sendInvite = invite.asBoolean();
        }
        UserAdminService.Created created = service.create(text(body, "email", true), text(body, "fullName", true),
                roleIds(body), sendInvite, actor(request));
        Map<String, Object> out = new LinkedHashMap<>(created.user());
        out.put("activation", created.activation().view());
        return out;
    }

    /** {@code getUser}: one user. */
    @GetMapping("/v1/users/{id}")
    @Operation(operationId = "getUser", tags = "Users")
    public Map<String, Object> getUser(@PathVariable String id) {
        return service.get(userId(id));
    }

    /**
     * {@code updateUser}: the full name and email, and (prompt R2-013) the user's data restriction when the body carries
     * {@code dataRestriction} (null lifts it; absent leaves it).
     */
    @PutMapping("/v1/users/{id}")
    @Operation(operationId = "updateUser", tags = "Users")
    public Map<String, Object> updateUser(@PathVariable String id, @RequestBody JsonNode body) {
        UUID user = userId(id);
        requireObject(body);
        boolean setRestriction = body.has(UserDataRestrictionService.FIELD);
        TokenRestrictions restriction = setRestriction ? dataRestrictions.parse(body.get(UserDataRestrictionService.FIELD))
                : null;
        return service.update(user, text(body, "fullName", false), text(body, "email", false), setRestriction,
                restriction);
    }

    /** {@code deleteUser}: reassigns (default) or deletes the user's configuration, then deletes the user. */
    @DeleteMapping("/v1/users/{id}")
    @Operation(operationId = "deleteUser", tags = "Users")
    public Map<String, Object> deleteUser(@PathVariable String id, @RequestParam(required = false) String reassign,
            HttpServletRequest request) {
        UUID user = userId(id);
        return service.delete(user, QueryParams.bool("reassign", reassign, true), actor(request),
                request.getRemoteAddr()).view();
    }

    /** {@code setUserRoles}: replaces the user's role set. */
    @PostMapping("/v1/users/{id}/roles")
    @Operation(operationId = "setUserRoles", tags = "Users")
    public Map<String, Object> setUserRoles(@PathVariable String id, @RequestBody JsonNode body) {
        UUID user = userId(id);
        requireObject(body);
        return service.setRoles(user, roleIds(body));
    }

    /** {@code suspendUser}: active to suspended; every session ends. */
    @PostMapping("/v1/users/{id}/suspend")
    @Operation(operationId = "suspendUser", tags = "Users")
    public Map<String, Object> suspendUser(@PathVariable String id) {
        return service.suspend(userId(id));
    }

    /** {@code unsuspendUser}: suspended to active. */
    @PostMapping("/v1/users/{id}/unsuspend")
    @Operation(operationId = "unsuspendUser", tags = "Users")
    public Map<String, Object> unsuspendUser(@PathVariable String id) {
        return service.unsuspend(userId(id));
    }

    /** {@code resendUserInvite}: a fresh activation link for an invited user. */
    @PostMapping("/v1/users/{id}/resend-invite")
    @Operation(operationId = "resendUserInvite", tags = "Users")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, Object> resendUserInvite(@PathVariable String id, HttpServletRequest request) {
        return service.resendInvite(userId(id), actor(request)).view();
    }

    /** {@code adminResetUserPassword}: a reset link; every session of the user ends. */
    @PostMapping("/v1/users/{id}/password-reset")
    @Operation(operationId = "adminResetUserPassword", tags = "Users")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, Object> adminResetUserPassword(@PathVariable String id) {
        return service.resetPassword(userId(id)).view();
    }

    /** The signed-in admin making the call, or null for an API token. */
    private static UUID actor(HttpServletRequest request) {
        return AuthenticatedSession.of(request).map(AuthenticatedSession::userId).orElse(null);
    }

    /** A path id; one that is not a UUID names no user. */
    private static UUID userId(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw UserAdminService.unknownUser();
        }
    }

    private static void requireObject(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a JSON object.");
        }
    }

    private static String text(JsonNode body, String field, boolean required) {
        JsonNode value = body.get(field);
        if (value == null || value.isNull()) {
            if (required) {
                throw ValidationException.forField(field, "is required");
            }
            return null;
        }
        if (!value.isString()) {
            throw ValidationException.forField(field, "must be a string");
        }
        return value.asString();
    }

    private static List<UUID> roleIds(JsonNode body) {
        JsonNode roles = body.get("roles");
        if (roles == null || roles.isNull()) {
            throw ValidationException.forField("roles", "is required");
        }
        if (!roles.isArray()) {
            throw ValidationException.forField("roles", "must be an array of role ids");
        }
        List<UUID> out = new ArrayList<>();
        for (JsonNode role : roles) {
            if (!role.isString()) {
                throw ValidationException.forField("roles", "must be an array of role ids");
            }
            try {
                out.add(UUID.fromString(role.asString()));
            } catch (IllegalArgumentException e) {
                throw ValidationException.forField("roles", "must be an array of role ids");
            }
        }
        return out;
    }
}
