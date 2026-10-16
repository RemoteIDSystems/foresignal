package ai.foresignal.api;

import ai.foresignal.provenance.AttributionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code getAttributions} (OpenAPI tag {@code Records}, prompt 109): the aggregated attributions of a
 * scope, for SSE clients and map footers. Read scope.
 */
@RestController
@Tag(name = "Records")
public class AttributionsController {

    private final AttributionService attributions;

    /** Creates the controller. */
    public AttributionsController(AttributionService attributions) {
        this.attributions = attributions;
    }

    /** {@code getAttributions}. */
    @GetMapping("/v1/attributions")
    @Operation(operationId = "getAttributions", tags = "Records")
    public Map<String, Object> getAttributions(@RequestParam(required = false) String source,
            @RequestParam(required = false) String kind) {
        List<AttributionService.Attribution> list = attributions.scope(source, kind);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("attributions", AttributionService.asMaps(list));
        out.put("text", attributions.header(list));
        return out;
    }
}
