package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.security.user.RoleAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
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
 * The {@code Roles} operations (prompt R2-006): {@code listRoles}, {@code createRole}, {@code getRole},
 * {@code updateRole}, {@code deleteRole} and {@code listPermissions}. All are admin-scoped with
 * {@code x-foresignal-permission: roles.manage} (R2-004); the rules are in {@link RoleAdminService}.
 */
@RestController
@Tag(name = "Roles")
public class RoleAdminController {

    private final RoleAdminService service;

    /**
     * Creates the controller.
     *
     * @param service role management
     */
    public RoleAdminController(RoleAdminService service) {
        this.service = service;
    }

    /** {@code listRoles}: a page of roles with their permissions and user counts. */
    @GetMapping("/v1/roles")
    @Operation(operationId = "listRoles", tags = "Roles")
    public Map<String, Object> listRoles(@RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        return service.list(cursor, QueryParams.intInRange("limit", limit, 1, 500, 100));
    }

    /** {@code createRole}: a custom role. */
    @PostMapping("/v1/roles")
    @Operation(operationId = "createRole", tags = "Roles")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createRole(@RequestBody JsonNode body) {
        requireObject(body);
        return service.create(text(body, "name"), text(body, "description"), keys(body));
    }

    /** {@code getRole}: one role. */
    @GetMapping("/v1/roles/{id}")
    @Operation(operationId = "getRole", tags = "Roles")
    public Map<String, Object> getRole(@PathVariable String id) {
        return service.get(roleId(id));
    }

    /** {@code updateRole}: replaces a custom role. */
    @PutMapping("/v1/roles/{id}")
    @Operation(operationId = "updateRole", tags = "Roles")
    public Map<String, Object> updateRole(@PathVariable String id, @RequestBody JsonNode body) {
        UUID role = roleId(id);
        requireObject(body);
        return service.update(role, text(body, "name"), text(body, "description"), keys(body));
    }

    /** {@code deleteRole}: a custom role nobody holds. */
    @DeleteMapping("/v1/roles/{id}")
    @Operation(operationId = "deleteRole", tags = "Roles")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRole(@PathVariable String id) {
        service.delete(roleId(id));
    }

    /** {@code listPermissions}: the fixed catalog, grouped. */
    @GetMapping("/v1/permissions")
    @Operation(operationId = "listPermissions", tags = "Roles")
    public Map<String, Object> listPermissions() {
        return service.permissions();
    }

    /** A path id; one that is not a UUID names no role. */
    private static UUID roleId(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw RoleAdminService.unknownRole();
        }
    }

    private static void requireObject(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a JSON object.");
        }
    }

    private static String text(JsonNode body, String field) {
        JsonNode value = body.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        if (!value.isString()) {
            throw ValidationException.forField(field, "must be a string");
        }
        return value.asString();
    }

    private static List<String> keys(JsonNode body) {
        JsonNode permissions = body.get("permissions");
        if (permissions == null || permissions.isNull()) {
            throw ValidationException.forField("permissions", "is required");
        }
        if (!permissions.isArray()) {
            throw ValidationException.forField("permissions", "must be an array of permission keys");
        }
        List<String> out = new ArrayList<>();
        for (JsonNode key : permissions) {
            if (!key.isString()) {
                throw ValidationException.forField("permissions", "must be an array of permission keys");
            }
            out.add(key.asString());
        }
        return out;
    }
}
