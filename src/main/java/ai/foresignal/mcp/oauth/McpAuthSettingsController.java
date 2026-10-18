package ai.foresignal.mcp.oauth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * {@code getMcpAuthSettings} and {@code updateMcpAuthSettings} (prompt 141): settings {@code mcp.auth}. Admin only
 * ({@code /v1/settings/**} in {@code AccessPolicy}).
 */
@RestController
@Tag(name = "Settings")
public class McpAuthSettingsController {

    private final McpAuthSettingsService settings;

    /** Creates the controller. */
    public McpAuthSettingsController(McpAuthSettingsService settings) {
        this.settings = settings;
    }

    /** {@code getMcpAuthSettings}: the settings in force (the defaults, {@code mode: token}, when never set). */
    @GetMapping("/v1/settings/mcp-auth")
    @Operation(operationId = "getMcpAuthSettings", tags = "Settings")
    public McpAuthSettings getMcpAuthSettings() {
        return settings.get();
    }

    /** {@code updateMcpAuthSettings}: replaces the settings; {@code /mcp} follows them at once. */
    @PutMapping("/v1/settings/mcp-auth")
    @Operation(operationId = "updateMcpAuthSettings", tags = "Settings")
    public McpAuthSettings updateMcpAuthSettings(@RequestBody JsonNode body) {
        return settings.update(body);
    }
}
