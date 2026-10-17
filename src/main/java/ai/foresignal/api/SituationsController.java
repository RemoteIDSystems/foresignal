package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.situation.SituationBriefService;
import ai.foresignal.situation.SituationEntity;
import ai.foresignal.situation.SituationLifecycle;
import ai.foresignal.situation.SituationLinker;
import ai.foresignal.situation.SituationMapper;
import ai.foresignal.situation.SituationService;
import ai.foresignal.situation.SituationSnapshots;
import ai.foresignal.situation.SituationThemes;
import ai.foresignal.situation.SituationViews;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Situations and storylines (prompt 118). Reads are scoped {@code read}; creating, editing, member changes, merge,
 * split, brief regeneration and the linker dry run {@code agent} ({@code AccessPolicy}). Every path applies the
 * private-situation rule ({@link IntelVisibility}, EGR-1): a private situation is listed, read or changed only for a
 * caller with private access and is {@code 404 UNKNOWN_SITUATION} to anyone else.
 */
@RestController
@Tag(name = "Situations")
@RequestMapping("/v1/situations")
public class SituationsController {

    private final SituationService situations;
    private final SituationViews views;
    private final SituationSnapshots snapshots;
    private final SituationBriefService briefs;
    private final SituationLinker linker;
    private final IntelVisibility visibility;

    /** Creates the controller. */
    public SituationsController(SituationService situations, SituationViews views, SituationSnapshots snapshots,
            SituationBriefService briefs, SituationLinker linker, IntelVisibility visibility) {
        this.situations = situations;
        this.views = views;
        this.snapshots = snapshots;
        this.briefs = briefs;
        this.linker = linker;
        this.visibility = visibility;
    }

    /** {@code listSituations}: by latest activity, keyset on {@code lastActivityAt}, {@code id}. */
    @GetMapping
    @RestrictedRead
    @Operation(operationId = "listSituations", tags = "Situations")
    public Map<String, Object> listSituations(
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) List<String> theme,
            @RequestParam(required = false) List<String> region,
            @RequestParam(required = false) String bbox,
            @RequestParam(required = false) String since,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit,
            HttpServletRequest request) {
        List<String> statuses = QueryParams.multi("status", status);
        for (String value : statuses) {
            if (!SituationLifecycle.ALL.contains(value)) {
                throw ValidationException.forField("status", "must be one of " + SituationLifecycle.ALL);
            }
        }
        List<String> themes = QueryParams.multi("theme", theme);
        for (String value : themes) {
            if (!SituationThemes.valid(value)) {
                throw ValidationException.forField("theme", "must be one of " + SituationThemes.ALL);
            }
        }
        List<String> regions = QueryParams.multi("region", region);
        for (String value : regions) {
            if (!value.matches("^[A-Z]{2}(-[A-Z0-9]{1,3})?$")) {
                throw ValidationException.forField("region", "'" + value + "' is not a 040 region code");
            }
        }
        int size = QueryParams.intInRange("limit", limit, 1, 500, 100);
        SituationService.ListQuery query = new SituationService.ListQuery(statuses, themes, regions, bbox,
                QueryParams.instant("since", since), q, cursor, size);
        return views.page(situations.list(query, visibility.seesPrivate(request)));
    }

    /** {@code getSituation}: evidence, inference, then labeled speculation. */
    @GetMapping("/{situationId}")
    @RestrictedRead
    @Operation(operationId = "getSituation", tags = "Situations")
    public Map<String, Object> getSituation(@PathVariable String situationId, HttpServletRequest request) {
        return views.full(situations.visible(situationId, visibility.seesPrivate(request)));
    }

    /** {@code listSituationMembers}: most recently observed first. */
    @GetMapping("/{situationId}/members")
    @RestrictedRead
    @Operation(operationId = "listSituationMembers", tags = "Situations")
    public Map<String, Object> listSituationMembers(@PathVariable String situationId,
            @RequestParam(required = false) List<String> type,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit,
            HttpServletRequest request) {
        situations.visible(situationId, visibility.seesPrivate(request));
        return views.members(situationId, QueryParams.multi("type", type), role, cursor,
                QueryParams.intInRange("limit", limit, 1, 500, 100));
    }

    /** {@code getSituationTimeline}: members and log entries in time order. */
    @GetMapping("/{situationId}/timeline")
    @RestrictedRead
    @Operation(operationId = "getSituationTimeline", tags = "Situations")
    public Map<String, Object> getSituationTimeline(@PathVariable String situationId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit,
            HttpServletRequest request) {
        situations.visible(situationId, visibility.seesPrivate(request));
        return views.timeline(situationId, QueryParams.instant("from", from), QueryParams.instant("to", to), cursor,
                QueryParams.intInRange("limit", limit, 1, 500, 100));
    }

    /** {@code getSituationChanges}: the snapshot diff since a time, without an LLM. */
    @GetMapping("/{situationId}/changes")
    @RestrictedRead
    @Operation(operationId = "getSituationChanges", tags = "Situations")
    public Map<String, Object> getSituationChanges(@PathVariable String situationId,
            @RequestParam(required = false) String since, HttpServletRequest request) {
        SituationEntity situation = situations.visible(situationId, visibility.seesPrivate(request));
        Instant from = QueryParams.instant("since", since);
        if (from == null) {
            throw ValidationException.forField("since", "is required");
        }
        return snapshots.changes(situation, from);
    }

    /** {@code createSituation}. */
    @PostMapping
    @Operation(operationId = "createSituation", tags = "Situations")
    public ResponseEntity<Map<String, Object>> createSituation(@RequestBody JsonNode body, HttpServletRequest request) {
        SituationEntity situation = situations.create(body, actor(request), origin(request),
                visibility.seesPrivate(request));
        return ResponseEntity.created(URI.create("/v1/situations/" + situation.getId())).body(views.full(situation));
    }

    /** {@code updateSituation}: a JSON merge patch. */
    @PatchMapping("/{situationId}")
    @Operation(operationId = "updateSituation", tags = "Situations")
    public Map<String, Object> updateSituation(@PathVariable String situationId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        return views.full(situations.patch(situationId, body, actor(request), visibility.seesPrivate(request)));
    }

    /** {@code addSituationMember}: 201 when added, 200 when already a member. */
    @PostMapping("/{situationId}/members")
    @Operation(operationId = "addSituationMember", tags = "Situations")
    public ResponseEntity<Map<String, Object>> addSituationMember(@PathVariable String situationId,
            @RequestBody JsonNode body, HttpServletRequest request) {
        SituationService.AddResult result = situations.addMember(situationId, body, actor(request), origin(request),
                visibility.seesPrivate(request));
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(SituationMapper.member(result.member()));
    }

    /** {@code removeSituationMember}. */
    @DeleteMapping("/{situationId}/members/{memberId}")
    @Operation(operationId = "removeSituationMember", tags = "Situations")
    public ResponseEntity<Void> removeSituationMember(@PathVariable String situationId, @PathVariable String memberId,
            HttpServletRequest request) {
        situations.removeMember(situationId, memberId, actor(request), visibility.seesPrivate(request));
        return ResponseEntity.noContent().build();
    }

    /** {@code mergeSituation}: returns the surviving (older) situation. */
    @PostMapping("/{situationId}/merge")
    @Operation(operationId = "mergeSituation", tags = "Situations")
    public Map<String, Object> mergeSituation(@PathVariable String situationId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        String intoId = body == null || !body.hasNonNull("intoId") ? null : body.get("intoId").asString();
        Integer version = body != null && body.hasNonNull("version") ? body.get("version").asInt() : null;
        String reason = body != null && body.hasNonNull("reason") ? body.get("reason").asString() : null;
        return views.full(situations.merge(situationId, intoId, version, reason, actor(request),
                visibility.seesPrivate(request)));
    }

    /** {@code splitSituation}: 201 with the new situation. */
    @PostMapping("/{situationId}/split")
    @Operation(operationId = "splitSituation", tags = "Situations")
    public ResponseEntity<Map<String, Object>> splitSituation(@PathVariable String situationId,
            @RequestBody JsonNode body, HttpServletRequest request) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The body must be a SituationSplitInput object.");
        }
        List<String> memberIds = new ArrayList<>();
        JsonNode ids = body.get("memberIds");
        if (ids == null || !ids.isArray()) {
            throw ValidationException.forField("memberIds", "must be an array of member ids");
        }
        for (JsonNode id : ids) {
            memberIds.add(id.asString());
        }
        String title = body.hasNonNull("title") ? body.get("title").asString() : null;
        String theme = body.hasNonNull("theme") ? body.get("theme").asString() : null;
        Integer version = body.hasNonNull("version") ? body.get("version").asInt() : null;
        SituationEntity created = situations.split(situationId, memberIds, title, theme, version, actor(request),
                origin(request), visibility.seesPrivate(request));
        return ResponseEntity.created(URI.create("/v1/situations/" + created.getId())).body(views.full(created));
    }

    /** {@code regenerateSituationBrief}: 202; the brief is written on a background thread. */
    @PostMapping("/{situationId}/brief")
    @Operation(operationId = "regenerateSituationBrief", tags = "Situations")
    public ResponseEntity<Map<String, Object>> regenerateSituationBrief(@PathVariable String situationId,
            HttpServletRequest request) {
        return ResponseEntity.accepted().body(briefs.request(situationId, visibility.seesPrivate(request)));
    }

    /** {@code testSituationLinker}: a dry run into memory; nothing is stored. */
    @PostMapping("/linker/test")
    @Operation(operationId = "testSituationLinker", tags = "Situations")
    public Map<String, Object> testSituationLinker(@RequestBody(required = false) JsonNode body,
            HttpServletRequest request) {
        if (body == null || !body.path("fromSeq").isIntegralNumber() || !body.path("toSeq").isIntegralNumber()) {
            throw ValidationException.forField("fromSeq", "fromSeq and toSeq are required integers");
        }
        long fromSeq = body.get("fromSeq").asLong();
        long toSeq = body.get("toSeq").asLong();
        if (fromSeq < 0 || toSeq <= fromSeq) {
            throw ValidationException.forField("toSeq", "must be greater than fromSeq (and fromSeq ≥ 0)");
        }
        if (toSeq - fromSeq > SituationLinker.MAX_TEST_RANGE) {
            throw ValidationException.forField("toSeq",
                    "a dry run covers at most " + SituationLinker.MAX_TEST_RANGE + " seqs");
        }
        SituationLinker.DryRun run = linker.dryRun(fromSeq, toSeq, visibility.seesPrivate(request));
        List<Map<String, Object>> decisions = new ArrayList<>();
        for (SituationLinker.Decision decision : run.decisions()) {
            if (decisions.size() >= SituationLinker.MAX_TEST_DECISIONS) {
                break;
            }
            decisions.add(decision.view());
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("fromSeq", fromSeq);
        out.put("toSeq", toSeq);
        out.put("linkerVersion", ai.foresignal.situation.SituationScorer.VERSION);
        out.put("inputsConsidered", run.inputsConsidered());
        out.put("decisions", decisions);
        out.put("truncated", run.decisions().size() > SituationLinker.MAX_TEST_DECISIONS);
        return out;
    }

    /** The actor recorded on changes: the token id. */
    static String actor(HttpServletRequest request) {
        return CurrentToken.of(request).map(t -> t.id().toString()).orElse("operator");
    }

    /** {@code operator} for admin callers, {@code agent} otherwise. */
    static String origin(HttpServletRequest request) {
        return CurrentToken.of(request).map(t -> t.scope() == Scope.ADMIN ? "operator" : "agent").orElse("operator");
    }
}
