package ai.foresignal.api;

import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceService.EnableFreeResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code POST /v1/sources/enable-core} (prompt 150, admin): enables every currently disabled source of the core set
 * ({@code SourceRegistry.CORE_IDS}) that runs without operator input, and answers 200 with
 * {@code {"enabled": [ids], "count": N, "skipped": M}}. Disables nothing and is idempotent; the one call by which an
 * install seeded before prompt 150 adopts the core default.
 */
@RestController
@Tag(name = "Sources")
public class EnableCoreSourcesController {

    private final SourceService sources;

    /** Creates the controller. */
    public EnableCoreSourcesController(SourceService sources) {
        this.sources = sources;
    }

    /**
     * {@code enableCoreSources}.
     *
     * @return the OpenAPI {@code EnableCoreSourcesResult}
     */
    @PostMapping("/v1/sources/enable-core")
    @Operation(operationId = "enableCoreSources", tags = "Sources")
    public Map<String, Object> enableCoreSources() {
        EnableFreeResult result = sources.enableCore();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("enabled", result.enabled());
        body.put("count", result.count());
        body.put("skipped", result.skipped());
        return body;
    }
}
