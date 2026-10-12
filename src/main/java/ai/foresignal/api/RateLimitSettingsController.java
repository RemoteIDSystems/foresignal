package ai.foresignal.api;

import ai.foresignal.security.ratelimit.RateLimitSettings;
import ai.foresignal.security.ratelimit.RateLimitSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * {@code getRateLimits} and {@code updateRateLimits} (prompt 035): the per-client rate limits
 * enforced by {@code ClientRateLimitFilter}. Admin only ({@code /v1/settings/**}).
 */
@RestController
@Tag(name = "Settings")
public class RateLimitSettingsController {

    private final RateLimitSettingsService service;

    /** Creates the controller. */
    public RateLimitSettingsController(RateLimitSettingsService service) {
        this.service = service;
    }

    /** {@code getRateLimits}: the limits in force (the defaults until an operator sets them). */
    @GetMapping("/v1/settings/ratelimits")
    @Operation(operationId = "getRateLimits", tags = "Settings")
    public RateLimitSettings getRateLimits() {
        return service.get();
    }

    /**
     * {@code updateRateLimits}: replaces the limits; members left out take their defaults. The next
     * request is limited under the new values.
     */
    @PutMapping("/v1/settings/ratelimits")
    @Operation(operationId = "updateRateLimits", tags = "Settings")
    public RateLimitSettings updateRateLimits(@RequestBody JsonNode body) {
        return service.replace(body);
    }
}
