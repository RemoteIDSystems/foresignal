package ai.foresignal.mapping;

import ai.foresignal.api.support.PublicUrls;
import ai.foresignal.inbound.CustomSourceService;
import ai.foresignal.mapping.preset.PresetCatalog;
import ai.foresignal.settings.AppSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * {@code previewMapping}, {@code suggestMapping}, {@code listMappingPresets}, {@code listCustomSourceSamples}
 * and {@code testCustomSourceMapping} (all admin, prompt 059; {@code suggestMapping} is also always
 * authenticated). None of them writes anything.
 */
@RestController
@Tag(name = "Mappings")
public class MappingsController {

    private final MappingPreviewService preview;
    private final MappingSuggestService suggest;
    private final CustomSourceService customSources;
    private final SampleCapture samples;
    private final AppSettingService settings;

    /** Creates the controller. */
    public MappingsController(MappingPreviewService preview, MappingSuggestService suggest,
            CustomSourceService customSources, SampleCapture samples, AppSettingService settings) {
        this.preview = preview;
        this.suggest = suggest;
        this.customSources = customSources;
        this.samples = samples;
        this.settings = settings;
    }

    /** {@code previewMapping}: what ingest would write for a sample. */
    @PostMapping("/v1/mappings/preview")
    @Operation(operationId = "previewMapping", tags = "Mappings")
    public Map<String, Object> previewMapping(@RequestBody JsonNode body) {
        return preview.preview(body);
    }

    /** {@code suggestMapping}: an LLM-drafted mapping, validated and previewed, never stored. */
    @PostMapping("/v1/mappings/suggest")
    @Operation(operationId = "suggestMapping", tags = "Mappings")
    public Map<String, Object> suggestMapping(@RequestBody JsonNode body) {
        return suggest.suggest(body);
    }

    /** {@code listMappingPresets}: the device preset catalog with setup instructions for this server. */
    @GetMapping("/v1/mappings/presets")
    @Operation(operationId = "listMappingPresets", tags = "Mappings")
    public List<Map<String, Object>> listMappingPresets(HttpServletRequest request) {
        String base = PublicUrls.base(settings, request);
        return PresetCatalog.all().stream().map(p -> PresetCatalog.view(p, base)).toList();
    }

    /** {@code listCustomSourceSamples}: the captured raw requests, newest first. */
    @GetMapping("/v1/custom-sources/{sourceId}/samples")
    @Operation(operationId = "listCustomSourceSamples", tags = "Ingest")
    public List<Map<String, Object>> listCustomSourceSamples(@PathVariable String sourceId) {
        customSources.get(sourceId);
        return samples.list(sourceId).stream().map(SampleCapture.Sample::view).toList();
    }

    /** {@code testCustomSourceMapping}: the proposed or stored mapping over the captured samples. */
    @PostMapping("/v1/custom-sources/{sourceId}/mapping/test")
    @Operation(operationId = "testCustomSourceMapping", tags = "Ingest")
    public Map<String, Object> testCustomSourceMapping(@PathVariable String sourceId,
            @RequestBody(required = false) JsonNode body) {
        return preview.test(sourceId, body);
    }
}
