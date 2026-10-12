package ai.foresignal.api;

import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SecuritySettingsService;
import ai.foresignal.settings.ServerSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * {@code getSecuritySettings}, {@code updateSecuritySettings}, {@code getServerSettings} and
 * {@code updateServerSettings} (prompt 023). Admin only ({@code /v1/settings/**}).
 */
@RestController
@Tag(name = "Settings")
public class ServerSecuritySettingsController {

    private final SecuritySettingsService security;
    private final ServerSettingsService server;

    /** Creates the controller. */
    public ServerSecuritySettingsController(SecuritySettingsService security, ServerSettingsService server) {
        this.security = security;
        this.server = server;
    }

    /** {@code getSecuritySettings}. */
    @GetMapping("/v1/settings/security")
    @Operation(operationId = "getSecuritySettings", tags = "Settings")
    public SecuritySettings getSecuritySettings() {
        return security.get();
    }

    /** {@code updateSecuritySettings}: replaces the settings; the token filter picks them up at once. */
    @PutMapping("/v1/settings/security")
    @Operation(operationId = "updateSecuritySettings", tags = "Settings")
    public SecuritySettings updateSecuritySettings(@RequestBody JsonNode body) {
        return security.update(body);
    }

    /** {@code getServerSettings}: the settings in force with their warnings. */
    @GetMapping("/v1/settings/server")
    @Operation(operationId = "getServerSettings", tags = "Settings")
    public ServerSettingsService.View getServerSettings() {
        return server.view();
    }

    /** {@code updateServerSettings}: replaces the settings; the response carries {@code warnings}. */
    @PutMapping("/v1/settings/server")
    @Operation(operationId = "updateServerSettings", tags = "Settings")
    public ServerSettingsService.View updateServerSettings(@RequestBody JsonNode body) {
        return server.update(body);
    }
}
