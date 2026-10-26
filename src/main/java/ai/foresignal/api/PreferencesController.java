package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.preference.UserPreferencesService;
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
 * Per-user preferences (prompt R2-012): {@code getMyPreferences} and {@code updateMyPreferences}
 * ({@code self.preferences}, enforced by the token filter). Preferences belong to a user, so both need a signed-in
 * session; an API token is {@code 401}.
 */
@RestController
@Tag(name = "Preferences")
public class PreferencesController {

    private final UserPreferencesService preferences;

    /**
     * Creates the controller.
     *
     * @param preferences the preference service
     */
    public PreferencesController(UserPreferencesService preferences) {
        this.preferences = preferences;
    }

    /** {@code getMyPreferences}: the caller's preferences with the defaults filled in. */
    @GetMapping("/v1/me/preferences")
    @Operation(operationId = "getMyPreferences", tags = "Preferences")
    public Map<String, Object> getMyPreferences(HttpServletRequest request) {
        return preferences.get(session(request));
    }

    /** {@code updateMyPreferences}: replaces the caller's preferences. */
    @PutMapping("/v1/me/preferences")
    @Operation(operationId = "updateMyPreferences", tags = "Preferences")
    public Map<String, Object> updateMyPreferences(@RequestBody(required = false) JsonNode body,
            HttpServletRequest request) {
        return preferences.update(session(request), body);
    }

    private static AuthenticatedSession session(HttpServletRequest request) {
        return AuthenticatedSession.of(request).orElseThrow(() -> new ForesignalException(HttpStatus.UNAUTHORIZED,
                ErrorCodes.UNAUTHORIZED, "This operation needs a signed-in session, not an API token."));
    }
}
