package ai.foresignal.api;

import ai.foresignal.settings.AppSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The infrastructure rules settings (prompt 104): enable/disable and parameter overrides. */
@RestController
@Tag(name = "Infrastructure")
@RequestMapping("/v1/settings/infra-rules")
public class InfraRulesController {

    /** The settings key. */
    public static final String KEY = "infra.rules";

    private final AppSettingService settings;
    private final JsonMapper json;

    /** Creates the controller. */
    public InfraRulesController(AppSettingService settings, JsonMapper json) {
        this.settings = settings;
        this.json = json;
    }

    /** {@code getInfraRules}. */
    @GetMapping
    @SuppressWarnings("unchecked")
    @Operation(operationId = "getInfraRules", tags = "Infrastructure")
    public Map<String, Object> getInfraRules() {
        return (Map<String, Object>) settings.get(KEY, Map.class).orElseGet(LinkedHashMap::new);
    }

    /** {@code updateInfraRules}. */
    @PutMapping
    @Operation(operationId = "updateInfraRules", tags = "Infrastructure")
    public Map<String, Object> updateInfraRules(@RequestBody JsonNode body) {
        Map<String, Object> value = json.readValue(body.toString(),
                new tools.jackson.core.type.TypeReference<Map<String, Object>>() {});
        settings.put(KEY, value);
        return value;
    }
}
