package ai.foresignal.api;

import ai.foresignal.retention.RetentionJob;
import ai.foresignal.retention.RetentionRunResult;
import ai.foresignal.retention.RetentionSettingsService;
import ai.foresignal.settings.RetentionSettings;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * {@code getRetention}, {@code updateRetention} and {@code runRetention} (prompt 019). Admin only
 * ({@code /v1/settings/**}).
 */
@RestController
public class RetentionController {

    private final RetentionSettingsService settings;
    private final RetentionJob job;

    /** Creates the controller. */
    public RetentionController(RetentionSettingsService settings, RetentionJob job) {
        this.settings = settings;
        this.job = job;
    }

    /** {@code getRetention}: the settings, with {@code sources} read from {@code source_settings}. */
    @GetMapping("/v1/settings/retention")
    public RetentionSettings getRetention() {
        return settings.get();
    }

    /** {@code updateRetention}: replaces the settings; 400 {@code VALIDATION_FAILED} or {@code RETENTION_TIER_FIXED}. */
    @PutMapping("/v1/settings/retention")
    public RetentionSettings updateRetention(@RequestBody JsonNode body) {
        return settings.update(body);
    }

    /** {@code runRetention}: runs the job synchronously; 409 {@code ALREADY_RUNNING} while one is active. */
    @PostMapping("/v1/settings/retention/run")
    public RetentionRunResult runRetention() {
        return job.run();
    }
}
