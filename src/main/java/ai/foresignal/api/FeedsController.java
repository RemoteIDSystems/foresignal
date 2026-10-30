package ai.foresignal.api;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.ETags;
import ai.foresignal.api.support.PublicUrls;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.briefing.BriefingService;
import ai.foresignal.feed.AtomFeedWriter;
import ai.foresignal.feed.Feed;
import ai.foresignal.feed.FeedService;
import ai.foresignal.feed.JsonFeedWriter;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.security.visibility.Viewer;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.trigger.AlertQuery;
import ai.foresignal.trigger.AlertState;
import ai.foresignal.trigger.Severity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Atom and JSON Feed (prompt 058): {@code getAlertsAtom}, {@code getAlertsJsonFeed},
 * {@code getBriefingRunsAtom} and {@code getBriefingRunsJsonFeed}.
 *
 * <p>All need {@code read} (open while {@code security.openReads} is on); feed readers without headers
 * use a 035 feed key ({@code /v1/k/{key}/alerts/feed.atom}). The caller's restriction, the private gate
 * and {@code EgressPolicy.allows(FEED, …)} decide which entries appear. Conditional GET: a strong ETag
 * over (newest change, highest id, filters) plus {@code Last-Modified}; {@code If-None-Match} or, without
 * it, {@code If-Modified-Since} answer {@code 304}. RSS 2.0 and WebSub are not offered.
 */
@RestController
@Tag(name = "Feeds")
public class FeedsController {

    /** The default number of entries. */
    static final int DEFAULT_LIMIT = 50;

    /** The most entries. */
    static final int MAX_LIMIT = 200;

    /** The Atom media type with its charset. */
    static final MediaType ATOM = new MediaType("application", "atom+xml", StandardCharsets.UTF_8);

    /** The JSON Feed media type with its charset. */
    static final MediaType JSON_FEED = new MediaType("application", "feed+json", StandardCharsets.UTF_8);

    private final FeedService feeds;
    private final AtomFeedWriter atom;
    private final JsonFeedWriter jsonFeed;
    private final ReadCallerResolver callers;
    private final BriefingService briefings;
    private final AppSettingService settings;

    /** Creates the controller. */
    public FeedsController(FeedService feeds, AtomFeedWriter atom, JsonFeedWriter jsonFeed, ReadCallerResolver callers,
            BriefingService briefings, AppSettingService settings) {
        this.feeds = feeds;
        this.atom = atom;
        this.jsonFeed = jsonFeed;
        this.callers = callers;
        this.briefings = briefings;
        this.settings = settings;
    }

    /** {@code getAlertsAtom}: alerts as Atom with GeoRSS. */
    @GetMapping("/v1/alerts/feed.atom")
    @Operation(operationId = "getAlertsAtom", tags = "Feeds")
    @RestrictedRead("the alert query carries the caller's restriction; private sources need the private gate")
    public ResponseEntity<String> getAlertsAtom(
            @RequestParam(required = false) List<String> rule,
            @RequestParam(required = false) String minSeverity,
            @RequestParam(required = false) List<String> source,
            @RequestParam(required = false) List<String> kind,
            @RequestParam(required = false) List<String> state,
            @RequestParam(required = false) String limit,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            @RequestHeader(value = HttpHeaders.IF_MODIFIED_SINCE, required = false) String ifModifiedSince,
            HttpServletRequest request) {
        return alerts(rule, minSeverity, source, kind, state, limit, ifNoneMatch, ifModifiedSince, request, true);
    }

    /** {@code getAlertsJsonFeed}: alerts as JSON Feed 1.1 with the {@code _foresignal} extension. */
    @GetMapping("/v1/alerts/feed.json")
    @Operation(operationId = "getAlertsJsonFeed", tags = "Feeds")
    @RestrictedRead("the alert query carries the caller's restriction; private sources need the private gate")
    public ResponseEntity<String> getAlertsJsonFeed(
            @RequestParam(required = false) List<String> rule,
            @RequestParam(required = false) String minSeverity,
            @RequestParam(required = false) List<String> source,
            @RequestParam(required = false) List<String> kind,
            @RequestParam(required = false) List<String> state,
            @RequestParam(required = false) String limit,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            @RequestHeader(value = HttpHeaders.IF_MODIFIED_SINCE, required = false) String ifModifiedSince,
            HttpServletRequest request) {
        return alerts(rule, minSeverity, source, kind, state, limit, ifNoneMatch, ifModifiedSince, request, false);
    }

    /** {@code getBriefingRunsAtom}: one briefing's runs as Atom. */
    @GetMapping("/v1/briefings/{briefingId}/runs/feed.atom")
    @Operation(operationId = "getBriefingRunsAtom", tags = "Feeds")
    @RestrictedRead("404 unless the briefing is visible to the caller; runs citing private sources need the gate")
    public ResponseEntity<String> getBriefingRunsAtom(
            @PathVariable String briefingId,
            @RequestParam(required = false) String limit,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            @RequestHeader(value = HttpHeaders.IF_MODIFIED_SINCE, required = false) String ifModifiedSince,
            HttpServletRequest request) {
        return runs(briefingId, limit, ifNoneMatch, ifModifiedSince, request, true);
    }

    /** {@code getBriefingRunsJsonFeed}: one briefing's runs as JSON Feed 1.1. */
    @GetMapping("/v1/briefings/{briefingId}/runs/feed.json")
    @Operation(operationId = "getBriefingRunsJsonFeed", tags = "Feeds")
    @RestrictedRead("404 unless the briefing is visible to the caller; runs citing private sources need the gate")
    public ResponseEntity<String> getBriefingRunsJsonFeed(
            @PathVariable String briefingId,
            @RequestParam(required = false) String limit,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            @RequestHeader(value = HttpHeaders.IF_MODIFIED_SINCE, required = false) String ifModifiedSince,
            HttpServletRequest request) {
        return runs(briefingId, limit, ifNoneMatch, ifModifiedSince, request, false);
    }

    private ResponseEntity<String> alerts(List<String> rule, String minSeverity, List<String> source,
            List<String> kind, List<String> state, String limit, String ifNoneMatch, String ifModifiedSince,
            HttpServletRequest request, boolean asAtom) {
        Set<UUID> rules = new LinkedHashSet<>();
        for (String id : QueryParams.multi("rule", rule)) {
            try {
                rules.add(UUID.fromString(id));
            } catch (IllegalArgumentException e) {
                throw ValidationException.forField("rule", "'" + id + "' is not a UUID");
            }
        }
        Severity min = minSeverity == null ? null
                : Severity.fromWire(QueryParams.oneOf("minSeverity", minSeverity, Severity.WIRE_VALUES, null));
        List<String> states = List.copyOf(QueryParams.enumSet("state", state, AlertState.WIRE_VALUES));
        int size = QueryParams.intInRange("limit", limit, 1, MAX_LIMIT, DEFAULT_LIMIT);
        ReadCaller caller = callers.resolve(request);
        Set<String> excluded = callers.excludedSources(caller);
        AlertQuery query = new AlertQuery(rules, QueryParams.multi("source", source), QueryParams.multi("kind", kind),
                null, null, states, List.of(), min, null, false, excluded, caller.restriction());
        PublicUrls urls = PublicUrls.of(settings, request);
        String self = asAtom ? "/v1/alerts/feed.atom" : "/v1/alerts/feed.json";
        Feed feed = feeds.alerts(query, size, viewer(caller), urls, self);
        String filters = String.join("|", "alerts", rules.toString(), String.valueOf(min), states.toString(),
                query.sources().toString(), query.kinds().toString(), Integer.toString(size), caller.canonical(),
                asAtom ? "atom" : "json", urls.apiRoot());
        return respond(feed, filters, ifNoneMatch, ifModifiedSince, caller, asAtom);
    }

    private ResponseEntity<String> runs(String briefingId, String limit, String ifNoneMatch, String ifModifiedSince,
            HttpServletRequest request, boolean asAtom) {
        UUID id;
        try {
            id = UUID.fromString(briefingId);
        } catch (IllegalArgumentException e) {
            throw NotFoundException.of("Briefing", briefingId);
        }
        int size = QueryParams.intInRange("limit", limit, 1, MAX_LIMIT, DEFAULT_LIMIT);
        ReadCaller caller = callers.resolve(request);
        briefings.requireVisible(id, caller.restriction(),
                ai.foresignal.briefing.BriefingService.EditionViewer.of(request, caller.isAnonymous()));
        PublicUrls urls = PublicUrls.of(settings, request);
        String self = "/v1/briefings/" + id + "/runs/feed." + (asAtom ? "atom" : "json");
        Feed feed = feeds.briefingRuns(id, size, viewer(caller).canSeePrivate(), urls, self);
        String filters = String.join("|", "runs", id.toString(), Integer.toString(size), caller.canonical(),
                asAtom ? "atom" : "json", urls.apiRoot());
        return respond(feed, filters, ifNoneMatch, ifModifiedSince, caller, asAtom);
    }

    private ResponseEntity<String> respond(Feed feed, String filters, String ifNoneMatch, String ifModifiedSince,
            ReadCaller caller, boolean asAtom) {
        String etag = FeedService.etag(feed, filters);
        // A feed a caller may see private sources in (or a keyed feed) is never publicly cacheable,
        // even under openReads; matches MapController.
        boolean publicCache = caller.publicCache() && !seesPrivate(caller);
        HttpHeaders headers = ETags.cacheHeaders(new HttpHeaders(), etag, publicCache);
        headers.set(HttpHeaders.LAST_MODIFIED, FeedService.lastModified(feed));
        if (FeedService.notModified(feed, etag, ifNoneMatch, ifModifiedSince)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
        }
        return ResponseEntity.ok().headers(headers).contentType(asAtom ? ATOM : JSON_FEED)
                .body(asAtom ? atom.write(feed) : jsonFeed.write(feed));
    }

    /** The private-gate viewer of a read caller. */
    static Viewer viewer(ReadCaller caller) {
        return new Viewer(caller.scope(), caller.privateAccess(), TokenRestrictions.NONE);
    }

    /** True when {@code caller} may see private sources (prompt 039). */
    static boolean seesPrivate(ReadCaller caller) {
        return SourceVisibility.canSeePrivate(caller.scope(), caller.privateAccess());
    }
}
