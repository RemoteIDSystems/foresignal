package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.Actors;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.social.correlation.CorrelationViews;
import ai.foresignal.social.correlation.CorrelationVocabulary;
import ai.foresignal.social.correlation.LeadTimeStatistics;
import ai.foresignal.social.correlation.SocialCueService;
import ai.foresignal.social.correlation.SocialSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Social versus physical (OpenAPI tag {@code Social}, prompt 137): comparisons of claims with independent physical
 * evidence and their revisions (read), cues (read; a manual cue is {@code agent} and answers 202), lead-time
 * statistics (read), and the instance social settings ({@code admin}, under {@code /v1/settings/**}). A comparison's
 * {@code unobserved} means no corresponding change despite adequate coverage, never "false".
 */
@RestController
@Tag(name = "Social")
public class SocialCorrelationController {

    private static final String ULID = "^[0-9A-HJKMNP-TV-Z]{26}$";

    private final CorrelationViews views;
    private final SocialCueService cues;
    private final LeadTimeStatistics leadTimes;
    private final SocialSettingsService settings;

    /** Creates the controller. */
    public SocialCorrelationController(CorrelationViews views, SocialCueService cues, LeadTimeStatistics leadTimes,
            SocialSettingsService settings) {
        this.views = views;
        this.cues = cues;
        this.leadTimes = leadTimes;
        this.settings = settings;
    }

    /** {@code listSocialComparisons}: newest evaluation first. */
    @GetMapping("/v1/social/comparisons")
    @Operation(operationId = "listSocialComparisons", tags = "Social")
    public Map<String, Object> listSocialComparisons(@RequestParam(required = false) List<String> status,
            @RequestParam(required = false) String situationId, @RequestParam(required = false) String narrativeId,
            @RequestParam(required = false) String claimId, @RequestParam(required = false) String family,
            @RequestParam(required = false) String region, @RequestParam(required = false) String since,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit) {
        int size = QueryParams.intInRange("limit", limit, 1, CorrelationViews.MAX_LIMIT, CorrelationViews.DEFAULT_LIMIT);
        return views.comparisons(new ArrayList<>(QueryParams.enumSet("status", status, CorrelationVocabulary.STATUSES)),
                ulid("situationId", situationId), ulid("narrativeId", narrativeId), ulid("claimId", claimId),
                blank(family), region(region), QueryParams.instant("since", since), cursor, size).toMap();
    }

    /** {@code getSocialComparison}: with revisions; 404 {@code UNKNOWN_COMPARISON}. */
    @GetMapping("/v1/social/comparisons/{comparisonId}")
    @Operation(operationId = "getSocialComparison", tags = "Social")
    public Map<String, Object> getSocialComparison(@PathVariable String comparisonId) {
        return views.detail(comparisonId);
    }

    /** {@code listSocialCues}: newest first. */
    @GetMapping("/v1/social/cues")
    @Operation(operationId = "listSocialCues", tags = "Social")
    public Map<String, Object> listSocialCues(@RequestParam(required = false) List<String> status,
            @RequestParam(required = false) String since, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        int size = QueryParams.intInRange("limit", limit, 1, CorrelationViews.MAX_LIMIT, CorrelationViews.DEFAULT_LIMIT);
        return views.cues(new ArrayList<>(QueryParams.enumSet("status", status, CorrelationVocabulary.CUE_STATUSES)),
                QueryParams.instant("since", since), cursor, size).toMap();
    }

    /** {@code getSocialCue}: 404 {@code UNKNOWN_CUE}. */
    @GetMapping("/v1/social/cues/{cueId}")
    @Operation(operationId = "getSocialCue", tags = "Social")
    public Map<String, Object> getSocialCue(@PathVariable String cueId) {
        return views.cueDetail(cueId);
    }

    /**
     * {@code createSocialCue} (agent): 202 with the queued cue; 409 {@code PLATFORM_DISABLED}, 429
     * {@code CUE_LIMIT_REACHED} or {@code BUDGET_EXCEEDED}.
     */
    @PostMapping("/v1/social/cues")
    @Operation(operationId = "createSocialCue", tags = "Social")
    public ResponseEntity<Map<String, Object>> createSocialCue(@RequestBody(required = false) JsonNode body,
            HttpServletRequest request) {
        Map<String, Object> cue = cues.createManual(SocialCueService.parse(body), Actors.of(request));
        return ResponseEntity.status(HttpStatus.ACCEPTED).location(URI.create("/v1/social/cues/" + cue.get("id")))
                .body(cue);
    }

    /** {@code getSocialLeadTimes}. */
    @GetMapping("/v1/social/lead-times")
    @Operation(operationId = "getSocialLeadTimes", tags = "Social")
    public Map<String, Object> getSocialLeadTimes(@RequestParam(required = false) String claimType,
            @RequestParam(required = false) String platform, @RequestParam(required = false) String region,
            @RequestParam(required = false) String foundBy, @RequestParam(required = false) String since) {
        if (platform != null && !platform.matches("^[a-z0-9][a-z0-9-]{0,31}$")) {
            throw ValidationException.forField("platform", "must be a network id");
        }
        String found = foundBy == null ? null : QueryParams.oneOf("foundBy", foundBy, CorrelationVocabulary.FOUND_BY,
                null);
        return leadTimes.stats(new LeadTimeStatistics.Filters(blank(claimType), blank(platform), region(region), found,
                QueryParams.instant("since", since)));
    }

    /** {@code getSocialSettings} (admin). */
    @GetMapping("/v1/settings/social")
    @Operation(operationId = "getSocialSettings", tags = "Social")
    public Map<String, Object> getSocialSettings() {
        return settings.get();
    }

    /** {@code updateSocialSettings} (admin): replaces the settings. */
    @PutMapping("/v1/settings/social")
    @Operation(operationId = "updateSocialSettings", tags = "Social")
    public Map<String, Object> updateSocialSettings(@RequestBody(required = false) JsonNode body) {
        return settings.put(body);
    }

    private static String blank(String s) {
        return s == null || s.isBlank() ? null : s.strip();
    }

    private static String region(String s) {
        String r = blank(s);
        if (r != null && !r.matches("^[A-Z]{2}(-[A-Z0-9]{1,3})?$")) {
            throw ValidationException.forField("region", "must be a 040 region code");
        }
        return r;
    }

    private static String ulid(String field, String s) {
        String v = blank(s);
        if (v != null && !v.matches(ULID)) {
            throw ValidationException.forField(field, "must be a ULID");
        }
        return v;
    }
}
