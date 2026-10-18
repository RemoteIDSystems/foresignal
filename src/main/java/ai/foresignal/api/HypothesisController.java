package ai.foresignal.api;

import ai.foresignal.api.support.QueryParams;
import ai.foresignal.hypothesis.CollectionGapEntity;
import ai.foresignal.hypothesis.HypothesisAnalysis;
import ai.foresignal.hypothesis.HypothesisDrafter;
import ai.foresignal.hypothesis.HypothesisEntity;
import ai.foresignal.hypothesis.HypothesisInputs;
import ai.foresignal.hypothesis.HypothesisRevisionEntity;
import ai.foresignal.hypothesis.HypothesisService;
import ai.foresignal.hypothesis.HypothesisSetEntity;
import ai.foresignal.hypothesis.HypothesisViews;
import ai.foresignal.hypothesis.HypothesisVocabulary;
import ai.foresignal.hypothesis.IndicatorEntity;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The rolling hypotheses API (prompt 125; OpenAPI tag {@code Hypotheses}). Sets, revisions, the ACH matrix,
 * information value, indicators and collection gaps are reads; creating and changing sets, hypotheses, indicators,
 * evidence, assumptions and reviews are {@code agent}; an LLM draft is {@code agent} and always authenticated
 * ({@code AccessPolicy}). Private sets are invisible without private access. Probabilities change only through
 * revisions.
 */
@RestController
@Tag(name = "Hypotheses")
public class HypothesisController {

    private static final String READ_NOTE = "hypothesis sets hold judgments, numbers and refs, no record content;"
            + " private sets need private access";

    private final HypothesisService service;
    private final HypothesisAnalysis analysis;
    private final HypothesisDrafter drafter;
    private final IntelVisibility visibility;
    private final ai.foresignal.collab.EvidenceItemService evidenceItems;
    private final ai.foresignal.object.ObjectCallers callers;

    /** Creates the controller. */
    public HypothesisController(HypothesisService service, HypothesisAnalysis analysis, HypothesisDrafter drafter,
            IntelVisibility visibility, ai.foresignal.collab.EvidenceItemService evidenceItems,
            ai.foresignal.object.ObjectCallers callers) {
        this.evidenceItems = evidenceItems;
        this.callers = callers;
        this.service = service;
        this.analysis = analysis;
        this.drafter = drafter;
        this.visibility = visibility;
    }

    // ------------------------------------------------------------------------- sets

    /** {@code listHypothesisSets}: newest first; list items omit indicators and assumptions. */
    @GetMapping("/v1/hypotheses/sets")
    @RestrictedRead(READ_NOTE)
    @Operation(operationId = "listHypothesisSets", tags = "Hypotheses")
    public Map<String, Object> listHypothesisSets(@RequestParam(required = false) String status,
            @RequestParam(required = false) String cadence, @RequestParam(required = false) String situationId,
            @RequestParam(required = false) String region, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        String s = status == null ? null : QueryParams.oneOf("status", status, HypothesisVocabulary.SET_STATUSES,
                null);
        String c = cadence == null ? null : QueryParams.oneOf("cadence", cadence, HypothesisVocabulary.CADENCES,
                null);
        boolean seesPrivate = visibility.seesPrivate(request);
        HypothesisService.Page<HypothesisSetEntity> page = service.list(s, c, situationId, region, cursor,
                QueryParams.intInRange("limit", limit, 1, 500, 100), seesPrivate);
        List<Map<String, Object>> items = new ArrayList<>();
        for (HypothesisSetEntity set : page.items()) {
            items.add(service.view(set, false));
        }
        return HypothesisViews.page(items, page.nextCursor());
    }

    /** {@code createHypothesisSet}: 201 with the set, its first revision recorded. */
    @PostMapping("/v1/hypotheses/sets")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createHypothesisSet", tags = "Hypotheses")
    @ApiResponse(responseCode = "201", description = "Created")
    public Map<String, Object> createHypothesisSet(@RequestBody JsonNode body, HttpServletRequest request) {
        HypothesisInputs.SetDraft draft = HypothesisInputs.set(body);
        HypothesisSetEntity set = service.create(draft, admin(request) ? "operator" : "agent", actor(request));
        return service.view(set, true);
    }

    /** {@code draftHypothesisSet}: an LLM draft returned with warnings; nothing is created. */
    @PostMapping("/v1/hypotheses/sets/draft")
    @Operation(operationId = "draftHypothesisSet", tags = "Hypotheses")
    public Map<String, Object> draftHypothesisSet(@RequestBody JsonNode body) {
        return drafter.draft(HypothesisDrafter.DraftRequest.parse(body));
    }

    /** {@code getHypothesisSet}: hypotheses with change, indicators, assumptions and the linked question. */
    @GetMapping("/v1/hypotheses/sets/{setId}")
    @RestrictedRead(READ_NOTE)
    @Operation(operationId = "getHypothesisSet", tags = "Hypotheses")
    public Map<String, Object> getHypothesisSet(@PathVariable String setId, HttpServletRequest request) {
        return service.view(service.get(setId, visibility.seesPrivate(request)), true);
    }

    /** {@code updateHypothesisSet}: a merge patch of description, cadence or status (optimistic lock). */
    @PatchMapping("/v1/hypotheses/sets/{setId}")
    @Operation(operationId = "updateHypothesisSet", tags = "Hypotheses")
    public Map<String, Object> updateHypothesisSet(@PathVariable String setId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        boolean seesPrivate = visibility.seesPrivate(request);
        return service.view(service.patch(setId, HypothesisInputs.setPatch(body), seesPrivate), true);
    }

    /** {@code listHypothesisRevisions}: newest first. */
    @GetMapping("/v1/hypotheses/sets/{setId}/revisions")
    @RestrictedRead(READ_NOTE)
    @Operation(operationId = "listHypothesisRevisions", tags = "Hypotheses")
    public Map<String, Object> listHypothesisRevisions(@PathVariable String setId,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit,
            HttpServletRequest request) {
        HypothesisSetEntity set = service.get(setId, visibility.seesPrivate(request));
        HypothesisService.Page<HypothesisRevisionEntity> page = service.revisionsOf(set.getId(), cursor,
                QueryParams.intInRange("limit", limit, 1, 500, 100));
        List<Map<String, Object>> items = new ArrayList<>();
        for (HypothesisRevisionEntity r : page.items()) {
            items.add(HypothesisViews.revision(r));
        }
        return HypothesisViews.page(items, page.nextCursor());
    }

    /** {@code getAchMatrix}: an audit view. */
    @GetMapping("/v1/hypotheses/sets/{setId}/matrix")
    @RestrictedRead(READ_NOTE)
    @Operation(operationId = "getAchMatrix", tags = "Hypotheses")
    public Map<String, Object> getAchMatrix(@PathVariable String setId, HttpServletRequest request) {
        return analysis.matrix(setId, visibility.seesPrivate(request));
    }

    /** {@code reviewHypothesisSet}: 201 with the review revision. */
    @PostMapping("/v1/hypotheses/sets/{setId}/review")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "reviewHypothesisSet", tags = "Hypotheses")
    @ApiResponse(responseCode = "201", description = "Revision recorded")
    public Map<String, Object> reviewHypothesisSet(@PathVariable String setId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        return HypothesisViews.revision(service.review(setId, HypothesisInputs.review(body), actor(request),
                visibility.seesPrivate(request)));
    }

    /** {@code getInformationValue}: pending indicators ranked by expected information gain. */
    @GetMapping("/v1/hypotheses/sets/{setId}/information-value")
    @RestrictedRead(READ_NOTE)
    @Operation(operationId = "getInformationValue", tags = "Hypotheses")
    public Map<String, Object> getInformationValue(@PathVariable String setId, HttpServletRequest request) {
        return analysis.informationValue(setId, visibility.seesPrivate(request));
    }

    // ------------------------------------------------------------------------- hypotheses

    /** {@code addHypothesis}: 201; an exclusive set is renormalized and a revision recorded. */
    @PostMapping("/v1/hypotheses/sets/{setId}/hypotheses")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "addHypothesis", tags = "Hypotheses")
    @ApiResponse(responseCode = "201", description = "Created")
    public Map<String, Object> addHypothesis(@PathVariable String setId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        HypothesisEntity h = service.addHypothesis(setId, HypothesisInputs.hypothesis(body), actor(request),
                visibility.seesPrivate(request));
        return HypothesisViews.hypothesis(h, null, null);
    }

    /** {@code updateHypothesis}: a merge patch; a probability override is an {@code operator} revision. */
    @PatchMapping("/v1/hypotheses/{hypothesisId}")
    @Operation(operationId = "updateHypothesis", tags = "Hypotheses")
    public Map<String, Object> updateHypothesis(@PathVariable String hypothesisId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        HypothesisEntity h = service.patchHypothesis(hypothesisId, HypothesisInputs.hypothesisPatch(body),
                actor(request), visibility.seesPrivate(request));
        return HypothesisViews.hypothesis(h, null, null);
    }

    // ------------------------------------------------------------------------- indicators

    /** {@code createIndicator}: 201 with the indicator and its watch rule. */
    @PostMapping("/v1/hypotheses/sets/{setId}/indicators")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createIndicator", tags = "Hypotheses")
    @ApiResponse(responseCode = "201", description = "Created")
    public Map<String, Object> createIndicator(@PathVariable String setId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        IndicatorEntity i = service.createIndicator(setId, HypothesisInputs.indicator(body),
                admin(request) ? "operator" : "agent", visibility.seesPrivate(request));
        return HypothesisViews.indicator(i);
    }

    /** {@code listIndicators}: newest first. */
    @GetMapping("/v1/indicators")
    @RestrictedRead(READ_NOTE)
    @Operation(operationId = "listIndicators", tags = "Hypotheses")
    public Map<String, Object> listIndicators(@RequestParam(required = false) String state,
            @RequestParam(required = false) String setId, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        String st = state == null ? null : QueryParams.oneOf("state", state, HypothesisVocabulary.INDICATOR_STATES,
                null);
        HypothesisService.Page<IndicatorEntity> page = service.listIndicators(st, setId, cursor,
                QueryParams.intInRange("limit", limit, 1, 500, 100), visibility.seesPrivate(request));
        List<Map<String, Object>> items = new ArrayList<>();
        for (IndicatorEntity i : page.items()) {
            items.add(HypothesisViews.indicator(i));
        }
        return HypothesisViews.page(items, page.nextCursor());
    }

    /** {@code updateIndicator}: a merge patch; signal edits replace the watch rule. */
    @PatchMapping("/v1/indicators/{indicatorId}")
    @Operation(operationId = "updateIndicator", tags = "Hypotheses")
    public Map<String, Object> updateIndicator(@PathVariable String indicatorId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        return HypothesisViews.indicator(service.patchIndicator(indicatorId, HypothesisInputs.indicatorPatch(body),
                actor(request), visibility.seesPrivate(request)));
    }

    /** {@code retireIndicator}: 200 with the retired indicator; its watch rule is disabled. */
    @DeleteMapping("/v1/indicators/{indicatorId}")
    @Operation(operationId = "retireIndicator", tags = "Hypotheses")
    public Map<String, Object> retireIndicator(@PathVariable String indicatorId, HttpServletRequest request) {
        return HypothesisViews.indicator(service.retireIndicator(indicatorId, visibility.seesPrivate(request)));
    }

    // ------------------------------------------------------------------------- evidence and assumptions

    /** {@code addAchEvidence}: 201; re-reports attach to their evidence group's row. */
    @PostMapping("/v1/hypotheses/sets/{setId}/evidence")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "addAchEvidence", tags = "Hypotheses")
    @ApiResponse(responseCode = "201", description = "Added")
    public Map<String, Object> addAchEvidence(@PathVariable String setId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        // Prompt 130: evidence items are cited only when the caller can see them.
        ai.foresignal.object.ObjectCaller caller = callers.of(request);
        HypothesisInputs.EvidenceDraft draft = evidenceItems.citable(HypothesisInputs.evidence(body), caller);
        // Prompt 134: the consuming principal lets circular reporting be detected.
        return HypothesisViews.evidence(service.addEvidence(setId, draft, actor(request),
                visibility.seesPrivate(request), caller.principal()), false);
    }

    /** {@code removeAchEvidence}: 204; an {@code evidence-removed} revision reverses what the row applied. */
    @DeleteMapping("/v1/hypotheses/sets/{setId}/evidence/{evidenceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "removeAchEvidence", tags = "Hypotheses")
    @ApiResponse(responseCode = "204", description = "Removed")
    public void removeAchEvidence(@PathVariable String setId, @PathVariable String evidenceId,
            HttpServletRequest request) {
        service.removeEvidence(setId, evidenceId, actor(request), visibility.seesPrivate(request));
    }

    /** {@code createKeyAssumption}: 201. */
    @PostMapping("/v1/hypotheses/sets/{setId}/assumptions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createKeyAssumption", tags = "Hypotheses")
    @ApiResponse(responseCode = "201", description = "Created")
    public Map<String, Object> createKeyAssumption(@PathVariable String setId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        return HypothesisViews.assumption(service.createAssumption(setId, HypothesisInputs.assumption(body),
                actor(request), visibility.seesPrivate(request)));
    }

    /** {@code updateKeyAssumption}: a merge patch; a broken linchpin flags the set for review. */
    @PatchMapping("/v1/hypotheses/sets/{setId}/assumptions/{assumptionId}")
    @Operation(operationId = "updateKeyAssumption", tags = "Hypotheses")
    public Map<String, Object> updateKeyAssumption(@PathVariable String setId, @PathVariable String assumptionId,
            @RequestBody JsonNode body, HttpServletRequest request) {
        return HypothesisViews.assumption(service.patchAssumption(setId, assumptionId,
                HypothesisInputs.assumptionPatch(body), actor(request), visibility.seesPrivate(request)));
    }

    /** {@code listCollectionGaps}: newest first. */
    @GetMapping("/v1/collection-gaps")
    @RestrictedRead(READ_NOTE)
    @Operation(operationId = "listCollectionGaps", tags = "Hypotheses")
    public Map<String, Object> listCollectionGaps(@RequestParam(required = false) String setId,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit,
            HttpServletRequest request) {
        HypothesisService.Page<CollectionGapEntity> page = analysis.listGaps(setId, cursor,
                QueryParams.intInRange("limit", limit, 1, 500, 100), visibility.seesPrivate(request));
        List<Map<String, Object>> items = new ArrayList<>();
        for (CollectionGapEntity g : page.items()) {
            items.add(HypothesisViews.gap(g));
        }
        return HypothesisViews.page(items, page.nextCursor());
    }

    private static boolean admin(HttpServletRequest request) {
        return CurrentToken.of(request).map(t -> t.scope() == Scope.ADMIN).orElse(true);
    }

    private static String actor(HttpServletRequest request) {
        return CurrentToken.of(request).map(t -> t.id().toString()).orElse("operator");
    }
}
