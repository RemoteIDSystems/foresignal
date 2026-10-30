package ai.foresignal.api;

import ai.foresignal.news.TranslationSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * {@code getTranslationSettings} and {@code updateTranslationSettings} (prompt NP-002): news translation at ingest.
 * Admin only ({@code /v1/settings/**}, {@code system.settings}).
 */
@RestController
@Tag(name = "Settings")
public class TranslationSettingsController {

    private final TranslationSettingsService translation;

    /** Creates the controller. */
    public TranslationSettingsController(TranslationSettingsService translation) {
        this.translation = translation;
    }

    /** {@code getTranslationSettings}: the settings with the current translation spend. */
    @GetMapping("/v1/settings/translation")
    @Operation(operationId = "getTranslationSettings", tags = "Settings")
    public Map<String, Object> getTranslationSettings() {
        return translation.view(translation.get());
    }

    /** {@code updateTranslationSettings}: replaces the settings. */
    @PutMapping("/v1/settings/translation")
    @Operation(operationId = "updateTranslationSettings", tags = "Settings")
    public Map<String, Object> updateTranslationSettings(@RequestBody JsonNode body) {
        return translation.view(translation.put(TranslationSettingsService.parse(body)));
    }
}
