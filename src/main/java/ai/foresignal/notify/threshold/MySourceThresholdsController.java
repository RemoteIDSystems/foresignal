package ai.foresignal.notify.threshold;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.security.user.AuthenticatedSession;
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
 * A signed-in user's per-source notification thresholds under {@code /v1/me/source-thresholds} (prompt SG-003): get
 * the whole set and replace the whole set (mirroring {@code getMySources}/{@code updateMySources}), gated on
 * {@code self.alerts}. Keyed on the session's principal, matching the alert enforcement path. A threshold belongs to a
 * user, so every operation needs a signed-in session; an API token is {@code 401}.
 */
@RestController
@Tag(name = "Alerts")
public class MySourceThresholdsController {

    private final SourceThresholdService thresholds;

    /** Creates the controller. */
    public MySourceThresholdsController(SourceThresholdService thresholds) {
        this.thresholds = thresholds;
    }

    /** {@code getMySourceThresholds}: the caller's whole set. */
    @GetMapping("/v1/me/source-thresholds")
    @Operation(operationId = "getMySourceThresholds", tags = "Alerts")
    public Map<String, Object> getMySourceThresholds(HttpServletRequest request) {
        return thresholds.get(session(request).principalId());
    }

    /** {@code updateMySourceThresholds}: replaces the caller's whole set. */
    @PutMapping("/v1/me/source-thresholds")
    @Operation(operationId = "updateMySourceThresholds", tags = "Alerts")
    public Map<String, Object> updateMySourceThresholds(@RequestBody(required = false) JsonNode body,
            HttpServletRequest request) {
        return thresholds.replace(session(request).principalId(), body);
    }

    private static AuthenticatedSession session(HttpServletRequest request) {
        return AuthenticatedSession.of(request).orElseThrow(() -> new ForesignalException(HttpStatus.UNAUTHORIZED,
                ErrorCodes.UNAUTHORIZED, "This operation needs a signed-in session, not an API token."));
    }
}
