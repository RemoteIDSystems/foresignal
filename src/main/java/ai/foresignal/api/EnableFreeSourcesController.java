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
 * {@code POST /v1/sources/enable-free} (prompt 145, admin): enables every currently disabled built-in source that
 * runs without operator input (no required credential, no required parameter, parameters valid with nothing
 * supplied) and is not held off by its descriptor (prompt 146: {@code wiki-edit-surge}), and answers 200 with {@code {"enabled": [ids], "count": N, "skipped": M}}. Idempotent; the operator's
 * one call that brings an install seeded before prompt 145 to the free-sources default.
 */
@RestController
@Tag(name = "Sources")
public class EnableFreeSourcesController {

    private final SourceService sources;

    /** Creates the controller. */
    public EnableFreeSourcesController(SourceService sources) {
        this.sources = sources;
    }

    /**
     * {@code enableFreeSources}.
     *
     * @return the OpenAPI {@code EnableFreeSourcesResult}
     */
    @PostMapping("/v1/sources/enable-free")
    @Operation(operationId = "enableFreeSources", tags = "Sources")
    public Map<String, Object> enableFreeSources() {
        EnableFreeResult result = sources.enableFree();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("enabled", result.enabled());
        body.put("count", result.count());
        body.put("skipped", result.skipped());
        return body;
    }
}
