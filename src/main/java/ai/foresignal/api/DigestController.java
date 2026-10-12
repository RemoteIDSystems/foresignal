package ai.foresignal.api;

import ai.foresignal.api.support.ETags;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.briefing.DigestService;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.McpCallerResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code getDigest} (prompt 029): {@code GET /v1/digest}, the structured catch-up document. Needs
 * {@code read} (open while {@code openReads} is on). Strong ETag on {@code (toSeq, untilAlertId,
 * params)}, with {@code 304} support; pass {@code next.sinceSeq} and {@code next.sinceAlertId} on the
 * following call.
 */
@RestController
@Tag(name = "Briefings")
public class DigestController {

    private final DigestService digests;
    private final McpCallerResolver callers;

    /** Creates the controller. */
    public DigestController(DigestService digests, McpCallerResolver callers) {
        this.digests = digests;
        this.callers = callers;
    }

    /** {@code getDigest}. */
    @GetMapping("/v1/digest")
    @Operation(operationId = "getDigest", tags = "Briefings")
    public ResponseEntity<Object> getDigest(
            @RequestParam(required = false) String sinceSeq,
            @RequestParam(required = false) String sinceAlertId,
            @RequestParam(required = false) List<String> source,
            @RequestParam(required = false) List<String> kind,
            @RequestParam(required = false) String bbox,
            @RequestParam(required = false) String limit,
            @RequestParam(required = false) String includeHealth,
            @RequestParam(required = false) List<String> places,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        McpCaller caller = callers.resolve(request);
        DigestService.Params params = new DigestService.Params(
                QueryParams.nonNegativeLong("sinceSeq", sinceSeq),
                QueryParams.nonNegativeLong("sinceAlertId", sinceAlertId),
                QueryParams.multi("source", source),
                QueryParams.multi("kind", kind),
                bbox,
                QueryParams.intInRange("limit", limit, 1, DigestService.MAX_LIMIT, DigestService.DEFAULT_LIMIT),
                QueryParams.bool("includeHealth", includeHealth, true),
                QueryParams.multi("places", places));
        DigestService.Prepared prepared = digests.prepare(params, caller);
        if (ETags.matches(ifNoneMatch, prepared.etag())) {
            return ETags.notModified(prepared.etag(), caller.openReads());
        }
        return ResponseEntity.ok()
                .headers(ETags.cacheHeaders(new HttpHeaders(), prepared.etag(), caller.openReads()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(digests.render(prepared));
    }
}
