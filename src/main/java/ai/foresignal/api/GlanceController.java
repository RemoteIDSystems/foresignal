package ai.foresignal.api;

import ai.foresignal.api.support.ETags;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.security.restrict.ReadScope;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.stats.glance.GlanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code getGlance} (prompt 038; OpenAPI tag {@code Stats}): the ≤ 2 KB status document for widgets,
 * watches and e-ink, as JSON or ({@code format=text} / {@code Accept: text/plain}) as at most 8 lines of
 * 40 ASCII characters.
 *
 * <p>Answers are cached on the server for {@value #CACHE_SECONDS} s per (caller class, place, format),
 * where the caller class is the scope and restriction ({@link ReadCaller#canonical()}) plus the
 * excluded sources, so two callers that may see different things never share an entry. Responses carry
 * a strong ETag over the body ({@code If-None-Match} → 304) and {@code Cache-Control: private,
 * max-age=15}.
 */
@RestController
@Tag(name = "Stats")
public class GlanceController {

    /** Seconds a glance is cached, here and by clients. */
    public static final int CACHE_SECONDS = 15;

    /** The most cached entries before the cache is cleared. */
    static final int MAX_ENTRIES = 1_000;

    /** {@code text/plain} with UTF-8 (the body is ASCII). */
    static final MediaType TEXT = MediaType.parseMediaType("text/plain;charset=UTF-8");

    private final GlanceService glance;
    private final ReadCallerResolver callers;
    private final Clock clock;
    private final Map<String, Cached> cache = new ConcurrentHashMap<>();

    /** Creates the controller. */
    public GlanceController(GlanceService glance, ReadCallerResolver callers, Clock clock) {
        this.glance = glance;
        this.callers = callers;
        this.clock = clock;
    }

    /** A cached answer. */
    private record Cached(String body, String etag, Instant expires) {}

    /** {@code getGlance}. */
    @GetMapping("/v1/glance")
    @Operation(operationId = "getGlance", tags = "Stats")
    @RestrictedRead("alerts honor the restriction and place=; private-source alerts follow the 023 gate")
    public ResponseEntity<String> getGlance(
            @RequestParam(required = false) String place,
            @RequestParam(required = false) String format,
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String accept,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        String fmt = QueryParams.oneOf("format", format, List.of("json", "text"), null);
        boolean text = "text".equals(fmt) || (fmt == null && wantsText(accept));
        String placeId = place == null || place.isBlank() ? null : place.strip();
        ReadCaller caller = placeId == null ? callers.resolve(request)
                : callers.resolveScoped(request, new ReadScope(List.of(placeId), null, null, null));
        java.util.Set<String> excluded = callers.excludedSources(caller);
        String key = caller.canonical() + "|" + String.join(",", new TreeSet<>(excluded)) + "|"
                + (placeId == null ? "" : placeId) + "|" + (text ? "text" : "json");
        Instant now = clock.instant();
        Cached entry = cache.get(key);
        if (entry == null || !now.isBefore(entry.expires())) {
            GlanceService.Request r = new GlanceService.Request(caller, excluded, placeId);
            String body = text ? glance.text(r) : glance.json(r);
            entry = new Cached(body, ETags.forDocument(body), now.plusSeconds(CACHE_SECONDS));
            if (cache.size() >= MAX_ENTRIES) {
                cache.clear();
            }
            cache.put(key, entry);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setETag(entry.etag());
        headers.setCacheControl(CacheControl.maxAge(Duration.ofSeconds(CACHE_SECONDS)).cachePrivate());
        headers.set(HttpHeaders.VARY, ETags.VARY);
        if (ETags.matches(ifNoneMatch, entry.etag())) {
            return ResponseEntity.status(304).headers(headers).build();
        }
        return ResponseEntity.ok().headers(headers).contentType(text ? TEXT : MediaType.APPLICATION_JSON)
                .body(entry.body());
    }

    /** Drops every cached answer (tests). */
    public void clearCache() {
        cache.clear();
    }

    /** True when {@code accept} prefers {@code text/plain} over JSON. */
    static boolean wantsText(String accept) {
        if (accept == null || accept.isBlank()) {
            return false;
        }
        double best = -1;
        boolean text = false;
        for (MediaType type : MediaType.parseMediaTypes(accept)) {
            boolean isText = "text".equals(type.getType()) && "plain".equals(type.getSubtype());
            boolean isJson = "json".equals(type.getSubtype()) || type.getSubtype().endsWith("+json");
            if ((isText || isJson) && type.getQualityValue() > best) {
                best = type.getQualityValue();
                text = isText;
            }
        }
        return text;
    }
}
