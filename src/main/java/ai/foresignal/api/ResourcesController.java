package ai.foresignal.api;

import ai.foresignal.settings.ResourceThresholds;
import ai.foresignal.settings.ResourceThresholdsService;
import ai.foresignal.system.ResourceMonitor;
import ai.foresignal.system.ResourceStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Host resources and their alert thresholds (prompt R2-014): {@code getResourceStatus},
 * {@code getResourceThresholds} and {@code updateResourceThresholds}. Admin scope; a session needs
 * {@code storage.manage}.
 */
@RestController
@Tag(name = "Resources")
public class ResourcesController {

    private final ResourceMonitor monitor;
    private final ResourceThresholdsService thresholds;

    /**
     * Creates the controller.
     *
     * @param monitor the resource monitor
     * @param thresholds the thresholds
     */
    public ResourcesController(ResourceMonitor monitor, ResourceThresholdsService thresholds) {
        this.monitor = monitor;
        this.thresholds = thresholds;
    }

    /**
     * {@code getResourceStatus}: the live disk, memory and database gauges (at most 30 s old) and the worst status.
     *
     * @return the reading
     */
    @GetMapping("/v1/system/resources")
    @Operation(operationId = "getResourceStatus", tags = "Resources")
    public ResourceStatus getResourceStatus() {
        return monitor.snapshot();
    }

    /**
     * {@code getResourceThresholds}: the stored thresholds, or the defaults.
     *
     * @return the thresholds
     */
    @GetMapping("/v1/settings/thresholds")
    @Operation(operationId = "getResourceThresholds", tags = "Resources")
    public ResourceThresholds getResourceThresholds() {
        return thresholds.get();
    }

    /**
     * {@code updateResourceThresholds}: replaces the thresholds; the next reading is rated against them.
     *
     * @param body a {@code ResourceThresholds} object
     * @return the stored thresholds
     */
    @PutMapping("/v1/settings/thresholds")
    @Operation(operationId = "updateResourceThresholds", tags = "Resources")
    public ResourceThresholds updateResourceThresholds(@RequestBody JsonNode body) {
        return thresholds.put(ResourceThresholdsService.parse(body));
    }
}
