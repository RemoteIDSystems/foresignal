package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.social.SocialPlatformService;
import ai.foresignal.social.SocialViews;
import ai.foresignal.social.SocialVocabulary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Social platforms, coverage, volume and posts (OpenAPI tag {@code Social}, prompt 135): the platform adapters with
 * their state, quotas, spend and settings ({@code admin} to change), collection coverage per platform and region,
 * mention volume with seasonal baselines, and stored {@code social-post} records. Credentials use the source credential
 * endpoints ({@code PUT /v1/sources/{id}/credentials}).
 */
@RestController
@Tag(name = "Social")
public class SocialController {

    private static final Pattern REGION = Pattern.compile("^[A-Z]{2}(-[A-Z0-9]{1,3})?$");
    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final SocialPlatformService platforms;
    private final SocialViews views;
    private final ReadCallerResolver readers;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the controller. */
    public SocialController(SocialPlatformService platforms, SocialViews views, ReadCallerResolver readers,
            JsonMapper json, Clock clock) {
        this.platforms = platforms;
        this.views = views;
        this.readers = readers;
        this.json = json;
        this.clock = clock;
    }

    /** {@code listSocialPlatforms}. */
    @GetMapping("/v1/social/platforms")
    @Operation(operationId = "listSocialPlatforms", tags = "Social")
    public List<Map<String, Object>> listSocialPlatforms() {
        return platforms.list();
    }

    /** {@code getSocialPlatform}: 404 {@code UNKNOWN_PLATFORM}. */
    @GetMapping("/v1/social/platforms/{platformId}")
    @Operation(operationId = "getSocialPlatform", tags = "Social")
    public Map<String, Object> getSocialPlatform(@PathVariable String platformId) {
        return platforms.view(platformId);
    }

    /** {@code updateSocialPlatform} (admin): 400 {@code VALIDATION_FAILED}, 404 {@code UNKNOWN_PLATFORM}. */
    @PutMapping("/v1/social/platforms/{platformId}")
    @Operation(operationId = "updateSocialPlatform", tags = "Social")
    public Map<String, Object> updateSocialPlatform(@PathVariable String platformId,
            @RequestBody(required = false) JsonNode body) {
        SocialPlatformService.definition(platformId);
        if (body == null || !body.isObject()) {
            throw ValidationException.forField("body", "must be a SocialPlatformSettings object");
        }
        return platforms.update(platformId, json.convertValue(body, MAP));
    }

    /** {@code getSocialCoverage}: default window the last 24 hours. */
    @GetMapping("/v1/social/coverage")
    @Operation(operationId = "getSocialCoverage", tags = "Social")
    public Map<String, Object> getSocialCoverage(@RequestParam(required = false) String platform,
            @RequestParam(required = false) String region, @RequestParam(required = false) String since,
            @RequestParam(required = false) String until, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        String net = SocialViews.network("platform", platform);
        String regionCode = region(region);
        Instant to = QueryParams.instant("until", until);
        Instant end = to == null ? clock.instant() : to;
        Instant from = QueryParams.instant("since", since);
        int size = QueryParams.intInRange("limit", limit, 1, SocialViews.MAX_LIMIT, SocialViews.DEFAULT_LIMIT);
        return views.coverage(net, regionCode, from == null ? end.minus(Duration.ofHours(24)) : from, end, cursor,
                size).toMap();
    }

    /** {@code getSocialVolume}: exactly one of {@code topic}, {@code place}, {@code entity}. */
    @GetMapping("/v1/social/volume")
    @Operation(operationId = "getSocialVolume", tags = "Social")
    public Map<String, Object> getSocialVolume(@RequestParam(required = false) String topic,
            @RequestParam(required = false) String place, @RequestParam(required = false) String entity,
            @RequestParam(required = false) String platform, @RequestParam(required = false) String language,
            @RequestParam(required = false) String metric, @RequestParam(required = false) String interval,
            @RequestParam(required = false) String since, @RequestParam(required = false) String until) {
        List<String[]> scopes = new ArrayList<>();
        if (topic != null && !topic.isBlank()) {
            scopes.add(new String[] {SocialVocabulary.SCOPE_TOPIC, topic.strip()});
        }
        if (place != null && !place.isBlank()) {
            scopes.add(new String[] {SocialVocabulary.SCOPE_PLACE, place.strip()});
        }
        if (entity != null && !entity.isBlank()) {
            String id = entity.strip();
            scopes.add(new String[] {SocialVocabulary.SCOPE_ENTITY, id.startsWith("entity:") ? id.substring(7) : id});
        }
        if (scopes.size() != 1) {
            throw ValidationException.forField("topic", "exactly one of topic, place and entity is required");
        }
        String m = QueryParams.oneOf("metric", metric, SocialVocabulary.METRICS, SocialVocabulary.POSTS);
        String i = QueryParams.oneOf("interval", interval, List.of("PT1H", "P1D"), "PT1H");
        String lang = language == null || language.isBlank() ? null : language.strip().toLowerCase(
                java.util.Locale.ROOT);
        if (lang != null && !lang.matches("^([a-z]{2,3}|other)$")) {
            throw ValidationException.forField("language", "'" + language + "' is not a language bucket");
        }
        Instant to = QueryParams.instant("until", until);
        Instant end = to == null ? clock.instant() : to;
        Instant from = QueryParams.instant("since", since);
        return views.volume(scopes.get(0)[0], scopes.get(0)[1], SocialViews.network("platform", platform), lang, m, i,
                from == null ? end.minus(Duration.ofDays(7)) : from, end);
    }

    /** {@code listSocialPosts}: newest first. */
    @GetMapping("/v1/social/posts")
    @RestrictedRead("posts of sources outside the restriction are left out; spatial restrictions see none")
    @Operation(operationId = "listSocialPosts", tags = "Social")
    public Map<String, Object> listSocialPosts(@RequestParam(required = false) String topic,
            @RequestParam(required = false) String platform, @RequestParam(required = false) String q,
            @RequestParam(required = false) String cueId, @RequestParam(required = false) String since,
            @RequestParam(required = false) String until, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        if (q != null && q.length() > 500) {
            throw ValidationException.forField("q", "must be at most 500 characters");
        }
        if (cueId != null && !cueId.isBlank() && !cueId.strip().matches("^[0-9A-HJKMNP-TV-Z]{26}$")) {
            throw ValidationException.forField("cueId", "is not a ULID");
        }
        int size = QueryParams.intInRange("limit", limit, 1, SocialViews.MAX_LIMIT, SocialViews.DEFAULT_LIMIT);
        return views.posts(blank(topic), SocialViews.network("platform", platform), blank(q), blank(cueId),
                QueryParams.instant("since", since), QueryParams.instant("until", until), cursor, size,
                filter(request)).toMap();
    }

    /** {@code getSocialPost}: 404 {@code NOT_FOUND}. */
    @GetMapping("/v1/social/posts/{platform}/{postId}")
    @RestrictedRead("posts of sources outside the restriction are not found; spatial restrictions see none")
    @Operation(operationId = "getSocialPost", tags = "Social")
    public Map<String, Object> getSocialPost(@PathVariable String platform, @PathVariable String postId,
            HttpServletRequest request) {
        return views.post(SocialViews.network("platform", platform), postId, filter(request));
    }

    private SocialViews.PostFilter filter(HttpServletRequest request) {
        ReadCaller caller = readers.resolveScoped(request);
        return SocialViews.PostFilter.of(caller.restriction(), readers.excludedSources(caller));
    }

    private static String region(String region) {
        if (region == null || region.isBlank()) {
            return null;
        }
        String r = region.strip().toUpperCase(java.util.Locale.ROOT);
        if (!REGION.matcher(r).matches()) {
            throw ValidationException.forField("region", "'" + region + "' is not a 040 region code");
        }
        return r;
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
