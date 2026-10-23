package ai.foresignal.api;

import ai.foresignal.collect.CollectorStatus;
import ai.foresignal.collect.PullGovernor;
import ai.foresignal.settings.CollectorSettings;
import ai.foresignal.settings.CollectorSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * {@code getCollectorSettings} and {@code updateCollectorSettings} (prompt 150): the collector's global pull
 * concurrency; and {@code getCollectorStatus} (prompt 160): the pull governor's live state. Admin only
 * ({@code /v1/settings/**}).
 */
@RestController
@Tag(name = "Settings")
public class CollectorSettingsController {

    private final CollectorSettingsService collector;
    private final PullGovernor governor;

    /** Creates the controller. */
    public CollectorSettingsController(CollectorSettingsService collector, PullGovernor governor) {
        this.collector = collector;
        this.governor = governor;
    }

    /** {@code getCollectorSettings}: the settings in force (the defaults when none are stored). */
    @GetMapping("/v1/settings/collector")
    @Operation(operationId = "getCollectorSettings", tags = "Settings")
    public CollectorSettings getCollectorSettings() {
        return collector.get();
    }

    /** {@code updateCollectorSettings}: replaces the settings; the pull governor resizes at once. */
    @PutMapping("/v1/settings/collector")
    @Operation(operationId = "updateCollectorSettings", tags = "Settings")
    public CollectorSettings updateCollectorSettings(@RequestBody JsonNode body) {
        return collector.update(body);
    }

    /** {@code getCollectorStatus}: the pull governor's live cap, permits in use and fetches waiting. */
    @GetMapping("/v1/settings/collector/status")
    @Operation(operationId = "getCollectorStatus", tags = "Settings")
    public CollectorStatus getCollectorStatus() {
        return governor.status();
    }
}
