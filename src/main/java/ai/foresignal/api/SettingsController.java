package ai.foresignal.api;

import ai.foresignal.settings.CorsSettings;
import ai.foresignal.settings.CorsSettingsService;
import ai.foresignal.settings.SettingsTransferService;
import ai.foresignal.settings.WatchdogSettings;
import ai.foresignal.settings.WatchdogSettingsService;
import ai.foresignal.system.WatchdogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * {@code getCors}, {@code updateCors}, {@code getWatchdogSettings}, {@code updateWatchdogSettings},
 * {@code exportSettings} and {@code importSettings} (prompt 019). Admin only ({@code /v1/settings/**}).
 */
@RestController
@Tag(name = "Settings")
public class SettingsController {

    private final CorsSettingsService cors;
    private final WatchdogSettingsService watchdog;
    private final WatchdogService watchdogService;
    private final SettingsTransferService transfer;

    /** Creates the controller. */
    public SettingsController(CorsSettingsService cors, WatchdogSettingsService watchdog,
            WatchdogService watchdogService, SettingsTransferService transfer) {
        this.cors = cors;
        this.watchdog = watchdog;
        this.watchdogService = watchdogService;
        this.transfer = transfer;
    }

    /** {@code getCors}. */
    @GetMapping("/v1/settings/cors")
    @Operation(operationId = "getCors", tags = "Settings")
    public CorsSettings getCors() {
        return cors.get();
    }

    /** {@code updateCors}: every origin must be {@code *} (reads only) or {@code scheme://host[:port]}. */
    @PutMapping("/v1/settings/cors")
    @Operation(operationId = "updateCors", tags = "Settings")
    public CorsSettings updateCors(@RequestBody CorsSettings body) {
        cors.update(body);
        return cors.get();
    }

    /** {@code getWatchdogSettings}. */
    @GetMapping("/v1/settings/watchdog")
    @Operation(operationId = "getWatchdogSettings", tags = "Settings")
    public WatchdogSettings getWatchdogSettings() {
        return watchdog.get();
    }

    /** {@code updateWatchdogSettings}: replaces the settings; the next tick pings with them. */
    @PutMapping("/v1/settings/watchdog")
    @Operation(operationId = "updateWatchdogSettings", tags = "Settings")
    public WatchdogSettings updateWatchdogSettings(@RequestBody JsonNode body) {
        WatchdogSettings value = watchdog.put(WatchdogSettingsService.parse(body));
        watchdogService.resetSchedule();
        return value;
    }

    /**
     * {@code exportSettings}. {@code includePlaces} and {@code includeWatchlists} belong to prompt 036
     * and change nothing yet.
     */
    @GetMapping("/v1/settings/export")
    @Operation(operationId = "exportSettings", tags = "Settings")
    public Map<String, Object> exportSettings(
            @RequestParam(name = "includePlaces", required = false) Boolean includePlaces,
            @RequestParam(name = "includeWatchlists", required = false) Boolean includeWatchlists) {
        return transfer.export();
    }

    /** {@code importSettings}: atomic; 400 with every error and nothing applied. */
    @PutMapping("/v1/settings/import")
    @Operation(operationId = "importSettings", tags = "Settings")
    public SettingsTransferService.ImportReport importSettings(@RequestBody JsonNode body) {
        return transfer.importBundle(body);
    }
}
