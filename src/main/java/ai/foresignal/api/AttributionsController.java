package ai.foresignal.api;

import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.provenance.AttributionService;
import ai.foresignal.provenance.LicencePolicy;
import ai.foresignal.provenance.LicencePolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code getAttributions} (OpenAPI tag {@code Records}, prompt 109): the aggregated attributions of a
 * scope, for SSE clients and map footers. Read scope.
 *
 * <p>{@code source} and {@code kind} are repeatable or comma-separated. The sources the caller may not
 * read (private sources, token restrictions) and those its license policy excludes are left out, and the
 * response carries {@code Foresignal-Attribution}, and one {@code Link rel="license"} per license.
 */
@RestController
@Tag(name = "Records")
public class AttributionsController {

    private final AttributionService attributions;
    private final ReadCallerResolver callers;
    private final LicencePolicyService licencePolicy;

    /** Creates the controller. */
    public AttributionsController(AttributionService attributions, ReadCallerResolver callers,
            LicencePolicyService licencePolicy) {
        this.attributions = attributions;
        this.callers = callers;
        this.licencePolicy = licencePolicy;
    }

    /** {@code getAttributions}. */
    @GetMapping("/v1/attributions")
    @Operation(operationId = "getAttributions", tags = "Records")
    public ResponseEntity<Map<String, Object>> getAttributions(@RequestParam(required = false) List<String> source,
            @RequestParam(required = false) List<String> kind, HttpServletRequest request) {
        LicencePolicy policy = LicencePolicyService.of(request);
        Set<String> excluded = licencePolicy.withExcluded(callers.excludedSources(request), policy);
        List<AttributionService.Attribution> list = attributions.scope(QueryParams.multi("source", source),
                QueryParams.multi("kind", kind), excluded);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("attributions", AttributionService.asMaps(list));
        String text = attributions.header(list);
        out.put("text", text);
        HttpHeaders headers = new HttpHeaders();
        if (text != null && !text.isEmpty()) {
            headers.set(RecordsController.ATTRIBUTION_HEADER, text);
        }
        for (String url : attributions.licenceLinks(list)) {
            headers.add(HttpHeaders.LINK, "<" + url + ">; rel=\"license\"");
        }
        headers.add(HttpHeaders.VARY, LicencePolicyService.POLICY_HEADER);
        return ResponseEntity.ok().headers(headers).body(out);
    }
}
