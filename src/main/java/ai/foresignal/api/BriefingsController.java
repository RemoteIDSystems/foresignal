package ai.foresignal.api;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.support.ETags;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.briefing.BriefingHtmlRenderer;
import ai.foresignal.briefing.BriefingRunEntity;
import ai.foresignal.briefing.BriefingService;
import ai.foresignal.briefing.BriefingViews;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.McpCallerResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The {@code Briefings} operations (prompt 029): {@code listBriefings}, {@code createBriefing},
 * {@code getBriefing}, {@code updateBriefing}, {@code deleteBriefing}, {@code runBriefing},
 * {@code listBriefingRuns}, {@code getBriefingRun}, {@code getLatestBriefingRun} and
 * {@code previewBriefing}.
 *
 * <p>Reads need {@code read}; writes need {@code agent}; running and previewing need {@code agent}
 * and are always authenticated, because LLM mode spends budget ({@code AccessPolicy}). Run reads
 * answer JSON by default and sanitized HTML with {@code Accept: text/html} or {@code ?format=html},
 * with the strict {@link BriefingHtmlRenderer#CONTENT_SECURITY_POLICY} and a per-run ETag. Ids that
 * are not UUIDs are simply not found.
 */
@RestController
@Tag(name = "Briefings")
@RequestMapping("/v1/briefings")
public class BriefingsController {

    private final BriefingService briefings;
    private final BriefingHtmlRenderer html;
    private final McpCallerResolver callers;

    /** Creates the controller. */
    public BriefingsController(BriefingService briefings, BriefingHtmlRenderer html, McpCallerResolver callers) {
        this.briefings = briefings;
        this.html = html;
        this.callers = callers;
    }

    /** {@code listBriefings}. */
    @GetMapping
    @Operation(operationId = "listBriefings", tags = "Briefings")
    public List<Map<String, Object>> listBriefings() {
        return briefings.list();
    }

    /** {@code createBriefing}: 201 with {@code Location}; channel problems are {@code warnings}. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createBriefing", tags = "Briefings")
    public ResponseEntity<Map<String, Object>> createBriefing(@RequestBody JsonNode body, HttpServletRequest request) {
        Map<String, Object> created = briefings.create(body, callers.resolve(request));
        return ResponseEntity.created(URI.create("/v1/briefings/" + created.get("id"))).body(created);
    }

    /** {@code previewBriefing}: renders without storing, delivering or scheduling. */
    @PostMapping("/preview")
    @Operation(operationId = "previewBriefing", tags = "Briefings")
    public Map<String, Object> previewBriefing(@RequestBody JsonNode body, HttpServletRequest request) {
        return briefings.preview(body, callers.resolve(request));
    }

    /** {@code getBriefing}. */
    @GetMapping("/{briefingId}")
    @Operation(operationId = "getBriefing", tags = "Briefings")
    public Map<String, Object> getBriefing(@PathVariable String briefingId) {
        return briefings.get(id(briefingId));
    }

    /** {@code updateBriefing}: a full replace. */
    @PutMapping("/{briefingId}")
    @Operation(operationId = "updateBriefing", tags = "Briefings")
    public Map<String, Object> updateBriefing(@PathVariable String briefingId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        return briefings.update(id(briefingId), body, callers.resolve(request));
    }

    /** {@code deleteBriefing}: 204; past runs are kept until retention removes them. */
    @DeleteMapping("/{briefingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteBriefing", tags = "Briefings")
    public void deleteBriefing(@PathVariable String briefingId) {
        briefings.delete(id(briefingId));
    }

    /** {@code runBriefing}: synchronous up to 120 s; 409 {@code BRIEFING_RUNNING} while one runs. */
    @PostMapping("/{briefingId}/run")
    @Operation(operationId = "runBriefing", tags = "Briefings")
    public Map<String, Object> runBriefing(@PathVariable String briefingId) {
        return briefings.runNow(id(briefingId));
    }

    /** {@code listBriefingRuns}: newest first. */
    @GetMapping("/{briefingId}/runs")
    @Operation(operationId = "listBriefingRuns", tags = "Briefings")
    public Map<String, Object> listBriefingRuns(@PathVariable String briefingId,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit) {
        int size = QueryParams.intInRange("limit", limit, 1, BriefingService.MAX_RUN_PAGE,
                BriefingService.DEFAULT_RUN_PAGE);
        return briefings.runs(id(briefingId), cursor, size);
    }

    /** {@code getLatestBriefingRun}: JSON or HTML; 404 before the first run. */
    @GetMapping("/{briefingId}/runs/latest")
    @Operation(operationId = "getLatestBriefingRun", tags = "Briefings")
    public ResponseEntity<Object> getLatestBriefingRun(@PathVariable String briefingId,
            @RequestParam(required = false) String format,
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String accept,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        return respond(briefings.latest(id(briefingId)), format, accept, ifNoneMatch, callers.resolve(request));
    }

    /** {@code getBriefingRun}: JSON or HTML. */
    @GetMapping("/{briefingId}/runs/{runId}")
    @Operation(operationId = "getBriefingRun", tags = "Briefings")
    public ResponseEntity<Object> getBriefingRun(@PathVariable String briefingId, @PathVariable String runId,
            @RequestParam(required = false) String format,
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String accept,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        UUID run;
        try {
            run = UUID.fromString(runId);
        } catch (IllegalArgumentException e) {
            throw NotFoundException.of("Briefing run", runId);
        }
        return respond(briefings.run(id(briefingId), run), format, accept, ifNoneMatch, callers.resolve(request));
    }

    private ResponseEntity<Object> respond(BriefingRunEntity run, String format, String accept, String ifNoneMatch,
            McpCaller caller) {
        boolean asHtml = wantsHtml(format, accept);
        String etag = etag(run, asHtml);
        boolean openReads = caller.openReads();
        if (ETags.matches(ifNoneMatch, etag)) {
            ResponseEntity<Object> notModified = ETags.notModified(etag, openReads);
            if (asHtml) {
                return ResponseEntity.status(304).headers(notModified.getHeaders())
                        .header("Content-Security-Policy", BriefingHtmlRenderer.CONTENT_SECURITY_POLICY).build();
            }
            return notModified;
        }
        HttpHeaders headers = ETags.cacheHeaders(new HttpHeaders(), etag, openReads);
        if (!asHtml) {
            return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_JSON)
                    .body(BriefingViews.run(run));
        }
        headers.set("Content-Security-Policy", BriefingHtmlRenderer.CONTENT_SECURITY_POLICY);
        headers.set("X-Content-Type-Options", "nosniff");
        headers.set("Referrer-Policy", "no-referrer");
        String footer = "Foresignal briefing run " + run.getId() + " · " + run.getStatus()
                + (run.getFinishedAt() == null ? "" : " · " + run.getFinishedAt());
        String page = html.page(run.getBriefingName(), run.getContentMarkdown(), footer);
        return ResponseEntity.ok().headers(headers).contentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8))
                .body(page);
    }

    /** The per-run, per-rendition ETag: it changes when the run completes or its deliveries are recorded. */
    static String etag(BriefingRunEntity run, boolean asHtml) {
        return ETags.forDocument(run.getId() + "|" + run.getStatus() + "|" + run.getFinishedAt() + "|"
                + run.getDeliveries() + "|" + (asHtml ? "html" : "json"));
    }

    /**
     * True for HTML: {@code format=html}, or no {@code format} and an {@code Accept} that ranks
     * {@code text/html} above JSON.
     */
    static boolean wantsHtml(String format, String accept) {
        String f = QueryParams.oneOf("format", format, List.of("json", "html"), null);
        if (f != null) {
            return f.equals("html");
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
        } catch (InvalidMediaTypeException e) {
            return false;
        }
        return htmlQ > 0 && htmlQ > jsonQ;
    }

    private static UUID id(String briefingId) {
        try {
            return UUID.fromString(briefingId);
        } catch (IllegalArgumentException e) {
            throw NotFoundException.of("Briefing", briefingId);
        }
    }
}
