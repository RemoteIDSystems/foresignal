package ai.foresignal.api;

import ai.foresignal.analyst.AnalystExecutionService;
import ai.foresignal.analyst.AnalystProfileEntity;
import ai.foresignal.analyst.AnalystRunEntity;
import ai.foresignal.analyst.AnalystScheduler;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.analyst.AnalystService;
import ai.foresignal.analyst.AnalystViews;
import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.security.CurrentToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/** The autonomous analyst API (prompt 102): profiles, runs, notes, proposals and evaluation. *
 * <p>Left out in edge mode (prompt 113: LLM features (autonomous analyst)).
 */
@RestController
@Tag(name = "Analyst")
@RequestMapping("/v1/analyst")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = ai.foresignal.config.EdgeMode.PROPERTY,
        havingValue = ai.foresignal.config.EdgeMode.FULL, matchIfMissing = true)
public class AnalystController {

    private final AnalystService service;
    private final AnalystExecutionService execution;
    private final AnalystScheduler scheduler;
    private final IntelVisibility visibility;

    /** Creates the controller. */
    public AnalystController(AnalystService service, AnalystExecutionService execution, AnalystScheduler scheduler,
            IntelVisibility visibility) {
        this.service = service;
        this.execution = execution;
        this.scheduler = scheduler;
        this.visibility = visibility;
    }

    /** {@code listAnalystProfiles}. */
    @GetMapping("/profiles")
    @Operation(operationId = "listAnalystProfiles", tags = "Analyst")
    public Map<String, Object> listAnalystProfiles() {
        return AnalystViews.page(service.listProfiles().stream().map(AnalystViews::profile).toList());
    }

    /** {@code createAnalystProfile}. */
    @PostMapping("/profiles")
    @Operation(operationId = "createAnalystProfile", tags = "Analyst")
    @ApiResponse(responseCode = "201", description = "Created")
    public ResponseEntity<Map<String, Object>> createAnalystProfile(@RequestBody JsonNode body,
            HttpServletRequest request) {
        AnalystProfileEntity profile = service.create(body, owner(request));
        return ResponseEntity.created(URI.create("/v1/analyst/profiles/" + profile.getId()))
                .body(AnalystViews.profile(profile));
    }

    /** {@code getAnalystProfile}. */
    @GetMapping("/profiles/{profileId}")
    @Operation(operationId = "getAnalystProfile", tags = "Analyst")
    public Map<String, Object> getAnalystProfile(@PathVariable String profileId) {
        return AnalystViews.profile(service.getProfile(profileId));
    }

    /** {@code updateAnalystProfile}. */
    @PutMapping("/profiles/{profileId}")
    @Operation(operationId = "updateAnalystProfile", tags = "Analyst")
    public Map<String, Object> updateAnalystProfile(@PathVariable String profileId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        return AnalystViews.profile(service.update(profileId, body, owner(request)));
    }

    /** {@code deleteAnalystProfile}. */
    @DeleteMapping("/profiles/{profileId}")
    @Operation(operationId = "deleteAnalystProfile", tags = "Analyst")
    @ApiResponse(responseCode = "204", description = "Deleted")
    public ResponseEntity<Void> deleteAnalystProfile(@PathVariable String profileId) {
        service.delete(profileId);
        return ResponseEntity.noContent().build();
    }

    /** {@code runAnalystProfile}: 202 with the queued run. */
    @PostMapping("/profiles/{profileId}/run")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(operationId = "runAnalystProfile", tags = "Analyst")
    public Map<String, Object> runAnalystProfile(@PathVariable String profileId) {
        AnalystProfileEntity profile = service.getProfile(profileId);
        // The scheduler's guarded path: an atomic one-run-per-profile claim (409 ANALYST_RUN_IN_PROGRESS),
        // the global concurrency limit, and the runner's daily run cap.
        AnalystRunEntity run = scheduler.runNow(profile);
        return AnalystViews.run(run);
    }

    /** {@code listAnalystRuns}. */
    @GetMapping("/runs")
    @Operation(operationId = "listAnalystRuns", tags = "Analyst")
    public Map<String, Object> listAnalystRuns(@RequestParam(required = false) String profileId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String limit) {
        int size = QueryParams.intInRange("limit", limit, 1, 200, 50);
        return AnalystViews.page(service.listRuns(profileId, status, size).stream().map(AnalystViews::run).toList());
    }

    /** {@code getAnalystRun}. */
    @GetMapping("/runs/{runId}")
    @Operation(operationId = "getAnalystRun", tags = "Analyst")
    public Map<String, Object> getAnalystRun(@PathVariable String runId) {
        return AnalystViews.run(service.getRun(runId));
    }

    /** {@code listAnalystNotes}. */
    @GetMapping("/notes")
    @Operation(operationId = "listAnalystNotes", tags = "Analyst")
    public Map<String, Object> listAnalystNotes(@RequestParam(required = false) String profileId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String since,
            @RequestParam(required = false) String limit,
            HttpServletRequest request) {
        int size = QueryParams.intInRange("limit", limit, 1, 200, 50);
        Instant from = QueryParams.instant("since", since);
        return AnalystViews.page(service.listNotes(profileId, status, priority, from, size,
                visibility.seesPrivate(request)).stream().map(AnalystViews::note).toList());
    }

    /** {@code updateAnalystNote}; a private note is 404 to a caller without private access. */
    @PatchMapping("/notes/{noteId}")
    @Operation(operationId = "updateAnalystNote", tags = "Analyst")
    public Map<String, Object> updateAnalystNote(@PathVariable String noteId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        return AnalystViews.note(service.updateNoteStatus(noteId, body.path("status").asString("read"),
                visibility.seesPrivate(request)));
    }

    /**
     * {@code analystNoteFeedback}: 201 with the recorded feedback; a private note is 404 to a caller without private
     * access.
     */
    @PostMapping("/notes/{noteId}/feedback")
    @Operation(operationId = "analystNoteFeedback", tags = "Analyst")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> analystNoteFeedback(@PathVariable String noteId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        return AnalystViews.feedback(service.noteFeedback(noteId, body.path("rating").asString(),
                body.path("comment").isString() ? body.get("comment").asString() : null, actor(request),
                visibility.seesPrivate(request)));
    }

    /** {@code listAnalystProposals}. */
    @GetMapping("/proposals")
    @Operation(operationId = "listAnalystProposals", tags = "Analyst")
    public Map<String, Object> listAnalystProposals(@RequestParam(required = false) String status,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        int size = QueryParams.intInRange("limit", limit, 1, 200, 50);
        return AnalystViews.page(service.listProposals(status, size, visibility.seesPrivate(request)).stream()
                .map(AnalystViews::proposal).toList());
    }

    /** {@code confirmAnalystProposal}. */
    @PostMapping("/proposals/{proposalId}/confirm")
    @Operation(operationId = "confirmAnalystProposal", tags = "Analyst")
    public Map<String, Object> confirmAnalystProposal(@PathVariable String proposalId, HttpServletRequest request) {
        return AnalystViews.proposal(execution.confirm(proposalId, actor(request),
                visibility.seesPrivate(request)));
    }

    /** {@code rejectAnalystProposal}. */
    @PostMapping("/proposals/{proposalId}/reject")
    @Operation(operationId = "rejectAnalystProposal", tags = "Analyst")
    public Map<String, Object> rejectAnalystProposal(@PathVariable String proposalId, HttpServletRequest request) {
        return AnalystViews.proposal(execution.reject(proposalId, actor(request),
                visibility.seesPrivate(request)));
    }

    /** {@code analystProposalFeedback}: 201 with the recorded feedback. */
    @PostMapping("/proposals/{proposalId}/feedback")
    @Operation(operationId = "analystProposalFeedback", tags = "Analyst")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> analystProposalFeedback(@PathVariable String proposalId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        return AnalystViews.feedback(service.proposalFeedback(proposalId, body.path("rating").asString(),
                body.path("comment").isString() ? body.get("comment").asString() : null, actor(request),
                visibility.seesPrivate(request)));
    }

    /** {@code getAnalystEvaluation}. */
    @GetMapping("/evaluation")
    @Operation(operationId = "getAnalystEvaluation", tags = "Analyst")
    public Map<String, Object> getAnalystEvaluation(@RequestParam(required = false) String profileId,
            @RequestParam(required = false) String since) {
        Instant from = since == null ? Instant.now().minus(java.time.Duration.ofDays(30))
                : QueryParams.instant("since", since);
        return service.evaluation(profileId, from);
    }

    private static ai.foresignal.security.AuthenticatedToken owner(HttpServletRequest request) {
        return CurrentToken.of(request).map(CurrentToken::token).orElse(null);
    }

    private static String actor(HttpServletRequest request) {
        return CurrentToken.of(request).map(CurrentToken::name).orElse("operator");
    }
}
