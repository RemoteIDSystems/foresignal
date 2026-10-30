package ai.foresignal.api;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.support.ETags;
import ai.foresignal.api.support.PublicUrls;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.briefing.BriefingService;
import ai.foresignal.feed.FeedService;
import ai.foresignal.feed.PodcastFeed;
import ai.foresignal.feed.RssFeedWriter;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.settings.AppSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code getBriefingPodcast} (prompt 141): {@code GET /v1/briefings/{briefingId}/podcast.rss}, an RSS 2.0 feed of the
 * briefing's runs that have synthesized audio, newest first (at most {@value #LIMIT}), each with an
 * {@code <enclosure>} to its 043 media.
 *
 * <p>Needs {@code read} (open while {@code security.openReads} is on). Podcast apps without headers use a 035 feed
 * key, {@code /v1/k/{key}/briefings/{id}/podcast.rss}; the enclosure URLs then carry the same key, so the key's
 * prefixes should cover {@code /v1/media/} as well. The same visibility rules as the Atom feed of runs apply
 * (restriction, private gate). Conditional GET: a strong ETag and {@code Last-Modified}, answered with 304.
 */
@RestController
@Tag(name = "Briefings")
public class BriefingPodcastController {

    /** The most items. */
    static final int LIMIT = 50;

    /** The RSS media type with its charset. */
    static final MediaType RSS = new MediaType("application", "rss+xml", StandardCharsets.UTF_8);

    private final FeedService feeds;
    private final RssFeedWriter rss;
    private final ReadCallerResolver callers;
    private final BriefingService briefings;
    private final AppSettingService settings;

    /** Creates the controller. */
    public BriefingPodcastController(FeedService feeds, RssFeedWriter rss, ReadCallerResolver callers,
            BriefingService briefings, AppSettingService settings) {
        this.feeds = feeds;
        this.rss = rss;
        this.callers = callers;
        this.briefings = briefings;
        this.settings = settings;
    }

    /** {@code getBriefingPodcast}: the briefing's audio runs as RSS 2.0 with enclosures. */
    @GetMapping("/v1/briefings/{briefingId}/podcast.rss")
    @Operation(operationId = "getBriefingPodcast", tags = "Briefings")
    @RestrictedRead("404 unless the briefing is visible to the caller; runs citing private sources need the gate")
    public ResponseEntity<String> getBriefingPodcast(
            @PathVariable String briefingId,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            @RequestHeader(value = HttpHeaders.IF_MODIFIED_SINCE, required = false) String ifModifiedSince,
            HttpServletRequest request) {
        UUID id;
        try {
            id = UUID.fromString(briefingId);
        } catch (IllegalArgumentException e) {
            throw NotFoundException.of("Briefing", briefingId);
        }
        ReadCaller caller = callers.resolve(request);
        briefings.requireVisible(id, caller.restriction(),
                BriefingService.EditionViewer.of(request, caller.isAnonymous()));
        PublicUrls urls = PublicUrls.of(settings, request);
        String self = "/v1/briefings/" + id + "/podcast.rss";
        boolean seesPrivate = FeedsController.seesPrivate(caller);
        PodcastFeed podcast = feeds.briefingPodcast(id, LIMIT, seesPrivate, urls, self);
        String filters = String.join("|", "podcast", id.toString(), caller.canonical(), urls.apiRoot(),
                Integer.toString(podcast.enclosures().size()));
        String etag = FeedService.etag(podcast.feed(), filters);
        boolean publicCache = caller.publicCache() && !seesPrivate;
        HttpHeaders headers = ETags.cacheHeaders(new HttpHeaders(), etag, publicCache);
        headers.set(HttpHeaders.LAST_MODIFIED, FeedService.lastModified(podcast.feed()));
        if (FeedService.notModified(podcast.feed(), etag, ifNoneMatch, ifModifiedSince)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
        }
        return ResponseEntity.ok().headers(headers).contentType(RSS).body(rss.write(podcast));
    }
}
