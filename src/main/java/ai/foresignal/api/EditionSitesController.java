package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.support.ETags;
import ai.foresignal.api.support.PublicUrls;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.briefing.BriefingEntity;
import ai.foresignal.briefing.BriefingHtmlRenderer;
import ai.foresignal.briefing.BriefingRunEntity;
import ai.foresignal.briefing.BriefingService;
import ai.foresignal.briefing.EditionContext;
import ai.foresignal.briefing.EditionResolver;
import ai.foresignal.briefing.EditionSiteService;
import ai.foresignal.briefing.EditionSiteShareTarget;
import ai.foresignal.feed.FeedService;
import ai.foresignal.security.restrict.PathKeyFilter;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.share.ShareLinkEntity;
import ai.foresignal.share.ShareLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
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
 * The hosted edition site (prompt NP-005): {@code getEditionSite} and its share-link URL
 * {@code openEditionSiteShareLink}.
 *
 * <ul>
 *   <li>{@code GET /v1/editions/{briefingId}/site}: the edition's latest successful run as a standalone HTML page
 *       ({@link BriefingHtmlRenderer#page}, in the edition's language), always current; {@code Accept:
 *       application/json} or {@code ?format=json} returns the {@code EditionSite} with the canonical, feed-key and
 *       share URLs. Needs {@code read} (open while {@code security.openReads} is on). The edition's feed key reaches it
 *       as {@code /v1/k/{key}/editions/{briefingId}/site}.
 *   <li>{@code GET /v1/k/{key}/site}: the same page behind the edition's share link ({@code fss_}, target type
 *       {@value EditionSiteShareTarget#TYPE}); the key in the path is the credential and grants this page only. An
 *       unknown key is {@code 401} and a revoked or expired one {@code 410 SHARE_EXPIRED} (refused by
 *       {@code PathKeyFilter} as for every share link); a link to another target type, an edition deleted or with its
 *       site off is {@code 404}.
 *   <li>Visibility as the podcast feed: {@code 404} unless the edition is visible to the caller (prompt 035
 *       restriction; NP-003 owner, instance credential or the edition's own keys), runs citing private sources only
 *       behind the private gate (never on a share link, nor non-redistributable ones), {@code 404} while the site is
 *       off or before the first successful run.
 *   <li>Conditional GET: a strong ETag per run, rendition and language, and {@code Last-Modified} (when the run
 *       finished); {@code 304} on a match. The page carries the strict
 *       {@link BriefingHtmlRenderer#CONTENT_SECURITY_POLICY}.
 *   <li>The page has no assets: its styles are inline and the policy loads nothing else, so no asset URL needs a key.
 * </ul>
 *
 * <p>Left out in edge mode, with the briefings (prompt 113).
 */
@RestController
@Tag(name = "Briefings")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = ai.foresignal.config.EdgeMode.PROPERTY,
        havingValue = ai.foresignal.config.EdgeMode.FULL, matchIfMissing = true)
public class EditionSitesController {

    private final BriefingService briefings;
    private final EditionSiteService sites;
    private final EditionSiteShareTarget shareTarget;
    private final ShareLinkService shares;
    private final BriefingHtmlRenderer html;
    private final ReadCallerResolver callers;
    private final AppSettingService settings;
    private final ObjectProvider<EditionResolver> editions;

    /** Creates the controller. */
    public EditionSitesController(BriefingService briefings, EditionSiteService sites,
            EditionSiteShareTarget shareTarget, ShareLinkService shares, BriefingHtmlRenderer html,
            ReadCallerResolver callers, AppSettingService settings, ObjectProvider<EditionResolver> editions) {
        this.briefings = briefings;
        this.sites = sites;
        this.shareTarget = shareTarget;
        this.shares = shares;
        this.html = html;
        this.callers = callers;
        this.settings = settings;
        this.editions = editions;
    }

    /** {@code getEditionSite}: the latest successful run as HTML, or the {@code EditionSite} as JSON. */
    @GetMapping("/v1/editions/{briefingId}/site")
    @Operation(operationId = "getEditionSite", tags = "Briefings")
    @RestrictedRead("404 unless the edition is visible to the caller; runs citing private sources need the gate")
    public ResponseEntity<Object> getEditionSite(@PathVariable String briefingId,
            @RequestParam(required = false) String format,
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String accept,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            @RequestHeader(value = HttpHeaders.IF_MODIFIED_SINCE, required = false) String ifModifiedSince,
            HttpServletRequest request) {
        UUID id = id(briefingId);
        boolean asJson = wantsJson(format, accept);
        ReadCaller caller = callers.resolve(request);
        BriefingService.EditionViewer viewer = BriefingService.EditionViewer.of(request, caller.isAnonymous());
        briefings.requireVisible(id, caller.restriction(), viewer);
        BriefingEntity entity = briefings.load(id);
        if (!entity.isSiteEnabled()) {
            throw new NotFoundException(ErrorCodes.NOT_FOUND, "The site of edition " + id + " is off.");
        }
        boolean seesPrivate = FeedsController.seesPrivate(caller);
        BriefingRunEntity run = sites.latest(id).orElse(null);
        if (asJson) {
            return siteJson(entity, run, request, viewer);
        }
        if (run == null || (!seesPrivate && briefings.citesPrivate(run))) {
            throw new NotFoundException(ErrorCodes.NOT_FOUND, "Edition " + id + " has no successful run to show yet.");
        }
        boolean publicCache = caller.publicCache() && !seesPrivate && entity.getOwnerUserId() == null;
        return page(entity, run, ifNoneMatch, ifModifiedSince, publicCache);
    }

    /** {@code openEditionSiteShareLink}: the site behind an edition's share link; anything else is {@code 404}. */
    @GetMapping("/v1/k/{key}/site")
    @Operation(operationId = "openEditionSiteShareLink", tags = "Briefings")
    @RestrictedRead("serves only the shared edition's latest run, never private or non-redistributable sources")
    public ResponseEntity<Object> openEditionSiteShareLink(@PathVariable String key,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            @RequestHeader(value = HttpHeaders.IF_MODIFIED_SINCE, required = false) String ifModifiedSince,
            HttpServletRequest request) {
        ShareLinkEntity link = shares.use(key, request.getRemoteAddr());
        if (!EditionSiteShareTarget.TYPE.equals(link.getTargetType())) {
            throw EditionSiteShareTarget.gone();
        }
        BriefingEntity entity = shareTarget.served(link);
        BriefingRunEntity run = sites.latest(entity.getId()).orElseThrow(EditionSiteShareTarget::gone);
        if (briefings.citesPrivate(run) || briefings.citesAny(run, shares.deniedSources())) {
            throw EditionSiteShareTarget.gone();
        }
        return page(entity, run, ifNoneMatch, ifModifiedSince, false);
    }

    /** The {@code EditionSite} object; the key URLs only for a credential that is not itself a path key. */
    private ResponseEntity<Object> siteJson(BriefingEntity entity, BriefingRunEntity run, HttpServletRequest request,
            BriefingService.EditionViewer viewer) {
        String base = PublicUrls.base(settings, request);
        UUID id = entity.getId();
        boolean showKeys = !viewer.anonymous() && request.getAttribute(PathKeyFilter.KEY_BASE_ATTRIBUTE) == null;
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("briefingId", id.toString());
        out.put("url", base + EditionSiteService.sitePath(id));
        out.put("feedKeyUrl", showKeys ? sites.key(id, EditionSiteService.FEED)
                .map(k -> base + "/v1/k/" + k + "/editions/" + id + "/site").orElse(null) : null);
        out.put("shareUrl", showKeys && entity.isSiteShareable() ? sites.key(id, EditionSiteService.SHARE)
                .map(k -> base + "/v1/k/" + k + "/site").orElse(null) : null);
        out.put("latestRunId", run == null ? null : run.getId().toString());
        out.put("updatedAt", run == null || run.getFinishedAt() == null ? null : run.getFinishedAt().toString());
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("private, no-cache");
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_JSON).body(out);
    }

    /** The standalone page of {@code run}, with its validators, answered {@code 304} when the client has it. */
    private ResponseEntity<Object> page(BriefingEntity entity, BriefingRunEntity run, String ifNoneMatch,
            String ifModifiedSince, boolean publicCache) {
        String language = language(entity);
        Instant finished = run.getFinishedAt() == null ? run.getStartedAt() : run.getFinishedAt();
        String etag = ETags.forDocument("edition-site|" + run.getId() + "|" + run.getStatus() + "|" + finished + "|"
                + language);
        HttpHeaders headers = ETags.cacheHeaders(new HttpHeaders(), etag, publicCache);
        headers.set(HttpHeaders.LAST_MODIFIED, FeedService.HTTP_DATE.format(finished));
        headers.set("Content-Security-Policy", BriefingHtmlRenderer.CONTENT_SECURITY_POLICY);
        headers.set("X-Content-Type-Options", "nosniff");
        headers.set("Referrer-Policy", "no-referrer");
        if (notModified(etag, finished, ifNoneMatch, ifModifiedSince)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
        }
        String footer = entity.getName() + " · Foresignal edition · " + finished;
        String body = html.page(entity.getName(), run.getContentMarkdown(), footer, language);
        return ResponseEntity.ok().headers(headers)
                .contentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8)).body(body);
    }

    /** The edition's language (NP-003), or null. */
    private String language(BriefingEntity entity) {
        EditionResolver resolver = editions.getIfAvailable();
        EditionContext context = resolver == null ? null : resolver.resolve(entity);
        return context == null ? null : context.language();
    }

    /**
     * True when the client has this rendition: {@code If-None-Match} matches the ETag, or, without one,
     * {@code If-Modified-Since} is not before the run finished.
     */
    static boolean notModified(String etag, Instant finished, String ifNoneMatch, String ifModifiedSince) {
        if (ifNoneMatch != null && !ifNoneMatch.isBlank()) {
            return ETags.matches(ifNoneMatch, etag);
        }
        if (ifModifiedSince == null || ifModifiedSince.isBlank()) {
            return false;
        }
        try {
            Instant since = ZonedDateTime.parse(ifModifiedSince.strip(), DateTimeFormatter.RFC_1123_DATE_TIME)
                    .toInstant();
            return !finished.truncatedTo(ChronoUnit.SECONDS).isAfter(since);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * True for the JSON {@code EditionSite}: {@code format=json}, or no {@code format} and an {@code Accept} that ranks
     * JSON above HTML. HTML otherwise (the default).
     */
    static boolean wantsJson(String format, String accept) {
        String f = QueryParams.oneOf("format", format, List.of("json", "html"), null);
        if (f != null) {
            return f.equals("json");
        }
        if (accept == null || accept.isBlank()) {
            return false;
        }
        double htmlQ = -1;
        double jsonQ = -1;
        try {
            for (MediaType type : MediaType.parseMediaTypes(accept)) {
                if (type.getType().equals("text") && type.getSubtype().equals("html")) {
                    htmlQ = Math.max(htmlQ, type.getQualityValue());
                } else if (type.getType().equals("application")
                        && (type.getSubtype().equals("json") || type.getSubtype().endsWith("+json"))) {
                    jsonQ = Math.max(jsonQ, type.getQualityValue());
                }
            }
        } catch (org.springframework.http.InvalidMediaTypeException e) {
            return false;
        }
        return jsonQ > 0 && jsonQ > htmlQ;
    }

    private static UUID id(String briefingId) {
        try {
            return UUID.fromString(briefingId);
        } catch (IllegalArgumentException e) {
            throw NotFoundException.of("Briefing", briefingId);
        }
    }
}
