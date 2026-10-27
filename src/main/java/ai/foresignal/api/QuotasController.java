package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.security.quota.Quota;
import ai.foresignal.security.quota.QuotaService;
import ai.foresignal.security.user.AuthenticatedSession;
import ai.foresignal.security.user.TokenOwner;
import ai.foresignal.security.user.UserAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The {@code Quotas} operations (prompt R2-026): {@code getMyQuota} for the calling user, and {@code getUserQuota} and
 * {@code updateUserQuota} for an admin holding {@code users.manage}.
 *
 * <p>{@code getMyQuota} answers a session or a user's own API token (R2-025) with the user's effective quota and
 * current usage; an API token with no user has no quota and gets {@code 401}. {@code getUserQuota} returns the
 * user's own limits, null where the instance default applies; {@code updateUserQuota} replaces them whole.
 */
@RestController
@Tag(name = "Quotas")
public class QuotasController {

    private final QuotaService quotas;

    /**
     * Creates the controller.
     *
     * @param quotas the quotas
     */
    public QuotasController(QuotaService quotas) {
        this.quotas = quotas;
    }

    /** {@code getMyQuota}: the caller's effective quota and current usage. */
    @GetMapping("/v1/me/quota")
    @Operation(operationId = "getMyQuota", tags = "Quotas")
    public Map<String, Object> getMyQuota(HttpServletRequest request) {
        UUID user = AuthenticatedSession.of(request).map(AuthenticatedSession::userId)
                .or(() -> TokenOwner.of(request).map(TokenOwner::userId))
                .orElseThrow(() -> new ForesignalException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED,
                        "This operation needs a signed-in user or a user's own API token."));
        return quotas.usage(user);
    }

    /** {@code getUserQuota}: a user's own limits. */
    @GetMapping("/v1/users/{id}/quota")
    @Operation(operationId = "getUserQuota", tags = "Quotas")
    public Map<String, Object> getUserQuota(@PathVariable String id) {
        return quotas.get(userId(id)).view();
    }

    /** {@code updateUserQuota}: replaces a user's own limits; a null member uses the instance default. */
    @PutMapping("/v1/users/{id}/quota")
    @Operation(operationId = "updateUserQuota", tags = "Quotas")
    public Map<String, Object> updateUserQuota(@PathVariable String id, @RequestBody JsonNode body) {
        UUID user = userId(id);
        return quotas.replace(user, Quota.parse(body)).view();
    }

    private static UUID userId(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw UserAdminService.unknownUser();
        }
    }
}
