package ai.foresignal.api;

import ai.foresignal.settings.OutboundSettings;
import ai.foresignal.settings.OutboundSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * {@code getOutboundSettings} and {@code updateOutboundSettings} (prompt 035): the admin overrides of
 * the outbound URL policy. Admin only ({@code /v1/settings/**}).
 */
@RestController
@Tag(name = "Settings")
public class OutboundSettingsController {

    private final OutboundSettingsService outbound;

    /** Creates the controller. */
    public OutboundSettingsController(OutboundSettingsService outbound) {
        this.outbound = outbound;
    }

    /** {@code getOutboundSettings}: the overrides in force (empty lists when none are set). */
    @GetMapping("/v1/settings/outbound")
    @Operation(operationId = "getOutboundSettings", tags = "Settings")
    public OutboundSettings getOutboundSettings() {
        return outbound.get();
    }

    /** {@code updateOutboundSettings}: replaces the overrides; the policy picks them up at once. */
    @PutMapping("/v1/settings/outbound")
    @Operation(operationId = "updateOutboundSettings", tags = "Settings")
    public OutboundSettings updateOutboundSettings(@RequestBody JsonNode body) {
        return outbound.update(body);
    }
}
