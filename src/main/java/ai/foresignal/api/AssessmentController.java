package ai.foresignal.api;

import ai.foresignal.api.support.ETags;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.briefing.BriefingHtmlRenderer;
import ai.foresignal.cycle.AssessmentBundleService;
import ai.foresignal.cycle.AssessmentEntity;
import ai.foresignal.cycle.AssessmentService;
import ai.foresignal.cycle.CycleInputs;
import ai.foresignal.cycle.CycleScheduler;
import ai.foresignal.cycle.CycleVocabulary;
import ai.foresignal.cycle.IntelBoardService;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.situation.SituationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The assessment endpoints (prompt 128): {@code listAssessments}, {@code getLatestAssessment}, {@code getAssessment}
 * (JSON, or the 029 markdown and HTML renditions), {@code getAssessmentBundle} (JSON or a GeoParquet ZIP),
 * {@code submitAssessmentFeedback} (read, token required), {@code createSituationAssessment} (agent, always
 * authenticated, 202), {@code listSituationAssessments} and {@code getIntelBoard} (≤ 8 KB, ETag,
 * {@code Cache-Control: max-age=60}). Private assessments are 404 to callers without private access.
 */
@RestController
@Tag(name = "Assessments")
public class AssessmentController {

    static final MediaType MARKDOWN = new MediaType("text", "markdown", StandardCharsets.UTF_8);

    private final AssessmentService assessments;
    private final AssessmentBundleService bundles;
    private final CycleScheduler scheduler;
    private final SituationService situations;
    private final IntelBoardService board;
    private final IntelVisibility visibility;

    /** Creates the controller. */
    public AssessmentController(AssessmentService assessments, AssessmentBundleService bundles,
            CycleScheduler scheduler, SituationService situations, IntelBoardService board,
            IntelVisibility visibility) {
        this.assessments = assessments;
        this.bundles = bundles;
        this.scheduler = scheduler;
        this.situations = situations;
        this.board = board;
        this.visibility = visibility;
    }

    /** {@code listAssessments}: newest first, markdown and HTML omitted. */
    @GetMapping("/v1/assessments")
    @RestrictedRead("assessments are derived analysis; private ones need private access")
    @Operation(operationId = "listAssessments", tags = "Assessments")
    public Map<String, Object> listAssessments(@RequestParam(required = false) String cadence,
            @RequestParam(required = false) String since, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        String c = cadence == null || cadence.isBlank() ? null
                : QueryParams.oneOf("cadence", cadence.strip(), CycleVocabulary.ASSESSMENT_CADENCES, null);
        AssessmentService.Page<AssessmentEntity> page = assessments.list(c, null, QueryParams.instant("since", since),
                blank(cursor), QueryParams.intInRange("limit", limit, 1, 500, 100), visibility.seesPrivate(request));
        return page(page);
    }

    /** {@code getLatestAssessment}. */
    @GetMapping("/v1/assessments/latest")
    @RestrictedRead("assessments are derived analysis; private ones need private access")
    @Operation(operationId = "getLatestAssessment", tags = "Assessments")
    @ApiResponse(responseCode = "404", description = "UNKNOWN_ASSESSMENT")
    public Map<String, Object> getLatestAssessment(@RequestParam(required = false) String cadence,
            HttpServletRequest request) {
        String c = cadence == null || cadence.isBlank() ? null
                : QueryParams.oneOf("cadence", cadence.strip(), CycleVocabulary.ASSESSMENT_CADENCES, null);
        return AssessmentService.view(assessments.latest(c, visibility.seesPrivate(request)), true);
    }

    /** {@code getAssessment}: JSON, {@code format=md} or {@code format=html} (or the matching {@code Accept}). */
    @GetMapping("/v1/assessments/{assessmentId}")
    @RestrictedRead("assessments are derived analysis; private ones need private access")
    @Operation(operationId = "getAssessment", tags = "Assessments")
    @ApiResponse(responseCode = "404", description = "UNKNOWN_ASSESSMENT")
    public ResponseEntity<Object> getAssessment(@PathVariable String assessmentId,
            @RequestParam(required = false) String format,
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String accept, HttpServletRequest request) {
        String fmt = QueryParams.oneOf("format", format, List.of("json", "md", "html"), null);
        if (fmt == null && accept != null) {
            if (accept.contains("text/html")) {
                fmt = "html";
            } else if (accept.contains("text/markdown")) {
                fmt = "md";
            }
        }
        AssessmentEntity a = assessments.get(assessmentId, visibility.seesPrivate(request));
        if ("md".equals(fmt)) {
            return ResponseEntity.ok().contentType(MARKDOWN).body(a.getMarkdown() == null ? "" : a.getMarkdown());
        }
        if ("html".equals(fmt)) {
            return ResponseEntity.ok().contentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8))
                    .header("Content-Security-Policy", BriefingHtmlRenderer.CONTENT_SECURITY_POLICY)
                    .header("X-Content-Type-Options", "nosniff").header("Referrer-Policy", "no-referrer")
                    .body(a.getHtml() == null ? "" : a.getHtml());
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(AssessmentService.view(a, true));
    }

    /** {@code getAssessmentBundle}: JSON, or {@code format=geoparquet} as a ZIP. */
    @GetMapping("/v1/assessments/{assessmentId}/bundle")
    @RestrictedRead("cited objects follow their own private rule; LOCAL_ONLY raw values are withheld from the export")
    @Operation(operationId = "getAssessmentBundle", tags = "Assessments")
    @ApiResponse(responseCode = "404", description = "UNKNOWN_ASSESSMENT")
    public ResponseEntity<Object> getAssessmentBundle(@PathVariable String assessmentId,
            @RequestParam(required = false) String format, HttpServletRequest request) {
        String fmt = QueryParams.oneOf("format", format, List.of("json", "geoparquet"), "json");
        boolean seesPrivate = visibility.seesPrivate(request);
        AssessmentEntity a = assessments.get(assessmentId, seesPrivate);
        if ("geoparquet".equals(fmt)) {
            return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/zip"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"assessment-" + a.getId()
                            + ".zip\"").body(bundles.geoparquet(a, seesPrivate));
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(bundles.bundle(a, seesPrivate));
    }

    /** {@code submitAssessmentFeedback}: 201 with the recorded feedback. */
    @PostMapping("/v1/assessments/{assessmentId}/feedback")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "submitAssessmentFeedback", tags = "Assessments")
    @ApiResponse(responseCode = "404", description = "UNKNOWN_ASSESSMENT")
    public Map<String, Object> submitAssessmentFeedback(@PathVariable String assessmentId,
            @RequestBody JsonNode body, HttpServletRequest request) {
        if (body == null || !body.isObject()) {
            throw ai.foresignal.api.error.ValidationException.forField("body", "must be an AssessmentFeedbackInput");
        }
        String rating = body.path("rating").isString() ? body.get("rating").asString() : null;
        String comment = body.path("comment").isString() ? body.get("comment").asString() : null;
        return assessments.feedback(assessmentId, rating, comment,
                CurrentToken.of(request).map(CurrentToken::name).orElse("operator"), visibility.seesPrivate(request));
    }

    /** {@code createSituationAssessment}: 202 with the queued run. */
    @PostMapping("/v1/situations/{situationId}/assessment")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(operationId = "createSituationAssessment", tags = "Assessments")
    @ApiResponse(responseCode = "409", description = "CYCLE_RUN_IN_PROGRESS, LLM_NOT_CONFIGURED")
    public Map<String, Object> createSituationAssessment(@PathVariable String situationId,
            @RequestBody(required = false) JsonNode body, HttpServletRequest request) {
        String id = situations.visible(situationId, visibility.seesPrivate(request)).getId();
        CycleInputs.SituationRequest req = CycleInputs.situation(body);
        return assessments.runView(scheduler.runSituation(id, req), false);
    }

    /** {@code listSituationAssessments}: newest first. */
    @GetMapping("/v1/situations/{situationId}/assessments")
    @RestrictedRead("assessments are derived analysis; private ones need private access")
    @Operation(operationId = "listSituationAssessments", tags = "Assessments")
    @ApiResponse(responseCode = "404", description = "UNKNOWN_SITUATION")
    public Map<String, Object> listSituationAssessments(@PathVariable String situationId,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit,
            HttpServletRequest request) {
        boolean seesPrivate = visibility.seesPrivate(request);
        String id = situations.visible(situationId, seesPrivate).getId();
        return page(assessments.list(CycleVocabulary.ON_DEMAND, id, null, blank(cursor),
                QueryParams.intInRange("limit", limit, 1, 500, 100), seesPrivate));
    }

    /** {@code getIntelBoard}: ≤ 8 KB, ETag (304 when unchanged), {@code Cache-Control: max-age=60}. */
    @GetMapping("/v1/intel/board")
    @RestrictedRead("the board carries no record content; private objects need private access")
    @Operation(operationId = "getIntelBoard", tags = "Assessments")
    public ResponseEntity<String> getIntelBoard(
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        boolean seesPrivate = visibility.seesPrivate(request);
        IntelBoardService.Board b = board.board(seesPrivate);
        HttpHeaders headers = new HttpHeaders();
        headers.setETag(b.etag());
        CacheControl cache = CacheControl.maxAge(Duration.ofSeconds(IntelBoardService.CACHE_SECONDS));
        headers.setCacheControl(seesPrivate ? cache.cachePrivate() : cache);
        headers.set(HttpHeaders.VARY, ETags.VARY);
        if (ETags.matches(ifNoneMatch, b.etag())) {
            return ResponseEntity.status(304).headers(headers).build();
        }
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_JSON).body(b.body());
    }

    private static Map<String, Object> page(AssessmentService.Page<AssessmentEntity> page) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", page.items().stream().map(a -> AssessmentService.view(a, false)).toList());
        out.put("nextCursor", page.nextCursor());
        return out;
    }

    private static String blank(String raw) {
        return raw == null || raw.isBlank() ? null : raw.strip();
    }
}
