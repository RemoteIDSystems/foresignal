package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.support.BboxParser;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.config.EdgeMode;
import ai.foresignal.federation.FederationFeedService;
import ai.foresignal.federation.FederationStreamService;
import ai.foresignal.security.CurrentToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * The federation feed served to peers (OpenAPI tag {@code Federation}, prompt 113): the change feed, the SSE
 * stream and the snapshot. Each needs a token with the read scope and the {@code federation} capability (023/035);
 * the token's restriction columns (sources, kinds, bbox, area) still apply. Every record passes the one
 * {@code EgressPolicy} on {@code FEDERATION} ({@link FederationFeedService}). Left out in edge mode.
 */
@RestController
@Tag(name = "Federation")
@ConditionalOnProperty(name = EdgeMode.PROPERTY, havingValue = EdgeMode.FULL, matchIfMissing = true)
public class FederationController {

    private final FederationFeedService feed;
    private final FederationStreamService streams;
    private final ReadCallerResolver callers;

    /** Creates the controller. */
    public FederationController(FederationFeedService feed, FederationStreamService streams,
            ReadCallerResolver callers) {
        this.feed = feed;
        this.streams = streams;
        this.callers = callers;
    }

    /** {@code listFederationChanges}. */
    @GetMapping("/v1/federation/changes")
    @Operation(operationId = "listFederationChanges", tags = "Federation")
    @ai.foresignal.security.restrict.RestrictedRead("the token's restriction is applied in FederationQueries")
    public Map<String, Object> listFederationChanges(@RequestParam(required = false) String after,
            @RequestParam(required = false) String limit, @RequestParam(required = false) List<String> source,
            @RequestParam(required = false) List<String> kind, @RequestParam(required = false) String bbox,
            HttpServletRequest request) {
        FederationFeedService.Caller caller = caller(request, callers);
        Long afterSeq = QueryParams.nonNegativeLong("after", after);
        FederationFeedService.Page page = feed.changes(afterSeq == null ? 0L : afterSeq,
                QueryParams.intInRange("limit", limit, 1, FederationFeedService.MAX_LIMIT, FederationFeedService.MAX_LIMIT),
                QueryParams.multi("source", source), QueryParams.multi("kind", kind), BboxParser.parse(bbox), caller);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", page.items());
        out.put("lastSeq", page.lastSeq());
        out.put("hasMore", page.hasMore());
        out.put("watermarkSeq", page.watermarkSeq());
        return out;
    }

    /** {@code getFederationSnapshot}. */
    @GetMapping("/v1/federation/snapshot")
    @Operation(operationId = "getFederationSnapshot", tags = "Federation")
    @ai.foresignal.security.restrict.RestrictedRead("the token's restriction is applied in FederationQueries")
    public Map<String, Object> getFederationSnapshot(@RequestParam(required = false) String atSeq,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit,
            @RequestParam(required = false) List<String> source, @RequestParam(required = false) List<String> kind,
            @RequestParam(required = false) String bbox, HttpServletRequest request) {
        FederationFeedService.Caller caller = caller(request, callers);
        FederationFeedService.SnapshotPage page = feed.snapshot(QueryParams.nonNegativeLong("atSeq", atSeq), cursor,
                QueryParams.intInRange("limit", limit, 1, FederationFeedService.MAX_LIMIT, FederationFeedService.MAX_LIMIT),
                QueryParams.multi("source", source), QueryParams.multi("kind", kind), BboxParser.parse(bbox), caller);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", page.items());
        out.put("atSeq", page.atSeq());
        out.put("nextCursor", page.nextCursor());
        return out;
    }

    /** {@code streamFederation}. */
    @GetMapping("/v1/federation/stream")
    @Operation(operationId = "streamFederation", tags = "Federation")
    @ai.foresignal.security.restrict.RestrictedRead("the token's restriction is applied per page and per live update")
    public SseEmitter streamFederation(@RequestHeader(value = "Last-Event-ID", required = false) String lastEventId,
            @RequestParam(required = false) String after, @RequestParam(required = false) String live,
            @RequestParam(required = false) List<String> source, @RequestParam(required = false) List<String> kind,
            @RequestParam(required = false) String bbox, HttpServletRequest request) {
        FederationFeedService.Caller caller = caller(request, callers);
        Long resume = lastEventId != null && !lastEventId.isBlank()
                ? QueryParams.nonNegativeLong("Last-Event-ID", lastEventId.strip())
                : QueryParams.nonNegativeLong("after", after);
        return streams.open(resume == null ? 0L : resume, QueryParams.bool("live", live, false),
                QueryParams.multi("source", source), QueryParams.multi("kind", kind), BboxParser.parse(bbox), caller);
    }

    /**
     * The federation caller of {@code request}: a token with the read scope (checked by the token filter) and the
     * {@code federation} capability, else {@code 403 RESTRICTED}.
     */
    static FederationFeedService.Caller caller(HttpServletRequest request, ReadCallerResolver callers) {
        CurrentToken token = CurrentToken.of(request).orElseThrow(() -> new ForesignalException(HttpStatus.FORBIDDEN,
                ErrorCodes.RESTRICTED, "The federation feed needs a token with the federation capability."));
        if (!token.restrictions().federationAllowed()) {
            throw new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.RESTRICTED,
                    "This token lacks the federation capability (restrictions.federation).");
        }
        return new FederationFeedService.Caller(callers.restriction(request), token.restrictions().exerciseIncluded(),
                false);
    }
}
