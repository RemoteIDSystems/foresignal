package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.backtest.BacktestJobEntity;
import ai.foresignal.backtest.BacktestViews;
import ai.foresignal.precursor.PrecursorService;
import ai.foresignal.precursor.PrecursorViews;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.restrict.RestrictedRead;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The precursor library API (prompt 120; OpenAPI tag {@code Analytics}): the library is a read; batches, candidates,
 * retire/pin, mining and re-tests are {@code agent} operations ({@code AccessPolicy}). Precursors are built from
 * non-private data only and hold selectors, series references and statistics, never record content.
 */
@RestController
@Tag(name = "Analytics")
public class PrecursorController {

    private final PrecursorService service;

    /** Creates the controller. */
    public PrecursorController(PrecursorService service) {
        this.service = service;
    }

    /** {@code listPrecursors}: newest first. */
    @GetMapping("/v1/precursors")
    @RestrictedRead("precursors hold selectors and statistics over non-private data, no record content")
    @Operation(operationId = "listPrecursors", tags = "Analytics")
    public Map<String, Object> listPrecursors(@RequestParam(required = false) List<String> status,
            @RequestParam(required = false) String kind, @RequestParam(required = false) List<String> region,
            @RequestParam(required = false) List<String> family, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        List<String> statuses = List.copyOf(QueryParams.enumSet("status", status, PrecursorService.STATUSES));
        String kindFilter = kind == null || kind.isBlank() ? null
                : QueryParams.oneOf("kind", kind, PrecursorService.KINDS, null);
        List<String> regions = QueryParams.multi("region", region);
        for (String code : regions) {
            if (!code.matches("^[A-Z]{2}(-[A-Z0-9]{1,3})?$")) {
                throw ValidationException.forField("region", "'" + code + "' is not a 040 region code");
            }
        }
        PrecursorService.Page<ai.foresignal.precursor.PrecursorEntity> page = service.list(statuses, kindFilter,
                regions, QueryParams.multi("family", family), cursor, QueryParams.intInRange("limit", limit, 1, 500,
                        100));
        return PrecursorViews.page(page.items().stream().map(PrecursorViews::precursor).toList(), page.nextCursor());
    }

    /** {@code listPrecursorBatches}: newest first. */
    @GetMapping("/v1/precursors/batches")
    @Operation(operationId = "listPrecursorBatches", tags = "Analytics")
    public Map<String, Object> listPrecursorBatches(@RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        PrecursorService.Page<ai.foresignal.precursor.PrecursorBatchEntity> page = service.batches(cursor,
                QueryParams.intInRange("limit", limit, 1, 500, 100));
        return PrecursorViews.page(page.items().stream().map(PrecursorViews::batch).toList(), page.nextCursor());
    }

    /** {@code getPrecursorBatch}. */
    @GetMapping("/v1/precursors/batches/{batchId}")
    @Operation(operationId = "getPrecursorBatch", tags = "Analytics")
    public Map<String, Object> getPrecursorBatch(@PathVariable String batchId) {
        return PrecursorViews.batch(service.batch(batchId));
    }

    /** {@code listPrecursorCandidates}: newest first. */
    @GetMapping("/v1/precursors/candidates")
    @Operation(operationId = "listPrecursorCandidates", tags = "Analytics")
    public Map<String, Object> listPrecursorCandidates(@RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        PrecursorService.Page<ai.foresignal.precursor.PrecursorCandidateEntity> page = service.candidates(cursor,
                QueryParams.intInRange("limit", limit, 1, 500, 100));
        return PrecursorViews.page(page.items().stream().map(PrecursorViews::candidate).toList(), page.nextCursor());
    }

    /** {@code createPrecursorCandidate}: 201. */
    @PostMapping("/v1/precursors/candidates")
    @Operation(operationId = "createPrecursorCandidate", tags = "Analytics")
    @ApiResponse(responseCode = "201", description = "Created")
    public ResponseEntity<Map<String, Object>> createPrecursorCandidate(@RequestBody JsonNode body,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(PrecursorViews.candidate(service.createCandidate(body,
                actor(request))));
    }

    /** {@code deletePrecursorCandidate}: 204; library precursors are kept. */
    @DeleteMapping("/v1/precursors/candidates/{candidateId}")
    @Operation(operationId = "deletePrecursorCandidate", tags = "Analytics")
    @ApiResponse(responseCode = "204", description = "Deleted")
    public ResponseEntity<Void> deletePrecursorCandidate(@PathVariable String candidateId) {
        service.deleteCandidate(candidateId);
        return ResponseEntity.noContent().build();
    }

    /** {@code minePrecursors}: 202 with the 103 job. */
    @PostMapping("/v1/precursors/mine")
    @Operation(operationId = "minePrecursors", tags = "Analytics")
    @ApiResponse(responseCode = "202", description = "Job accepted")
    public ResponseEntity<Map<String, Object>> minePrecursors(HttpServletRequest request) {
        BacktestJobEntity job = service.submit("mine", "manual", tokenId(request));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(BacktestViews.job(job));
    }

    /** {@code retestPrecursors}: 202 with the 103 job. */
    @PostMapping("/v1/precursors/retest")
    @Operation(operationId = "retestPrecursors", tags = "Analytics")
    @ApiResponse(responseCode = "202", description = "Job accepted")
    public ResponseEntity<Map<String, Object>> retestPrecursors(HttpServletRequest request) {
        BacktestJobEntity job = service.submit("retest", "manual", tokenId(request));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(BacktestViews.job(job));
    }

    /** {@code getPrecursor}. */
    @GetMapping("/v1/precursors/{precursorId}")
    @RestrictedRead("precursors hold selectors and statistics over non-private data, no record content")
    @Operation(operationId = "getPrecursor", tags = "Analytics")
    public Map<String, Object> getPrecursor(@PathVariable String precursorId) {
        return PrecursorViews.precursor(service.get(precursorId));
    }

    /** {@code updatePrecursor}: retire with a reason, or pin as operator-validated (merge patch). */
    @PatchMapping("/v1/precursors/{precursorId}")
    @Operation(operationId = "updatePrecursor", tags = "Analytics")
    public Map<String, Object> updatePrecursor(@PathVariable String precursorId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        return PrecursorViews.precursor(service.patch(precursorId, body, actor(request)));
    }

    private static String tokenId(HttpServletRequest request) {
        return CurrentToken.of(request).map(t -> t.id().toString()).orElse(null);
    }

    private static String actor(HttpServletRequest request) {
        return CurrentToken.of(request).map(t -> t.id().toString()).orElse("operator");
    }
}
