package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.PublicUrls;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.briefing.BriefingHtmlRenderer;
import ai.foresignal.collab.AccessAction;
import ai.foresignal.collab.CollabInputs;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.report.ReportDrafter;
import ai.foresignal.report.ReportEntity;
import ai.foresignal.report.ReportPublisher;
import ai.foresignal.report.ReportRevisionEntity;
import ai.foresignal.report.ReportSendEntity;
import ai.foresignal.report.ReportSender;
import ai.foresignal.report.ReportService;
import ai.foresignal.report.ReportShareTarget;
import ai.foresignal.report.ReportVocabulary;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.share.ShareLinkEntity;
import ai.foresignal.share.ShareLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Reports (prompt 132): drafted from a source, edited by their authors, published as immutable revisions with pinned
 * evidence, rendered to Markdown, HTML (TLP banner) and a JSON bundle, sent to principals and groups here, and shared
 * read-only through a 112 link when TLP allows. Every operation needs a token; a report the caller may not see is
 * {@code 404 UNKNOWN_REPORT}. Reads need {@code viewer}, edits and publishing {@code editor}, sending
 * {@code editor} and agent scope, deletion, retraction and links {@code owner}. Display ids ({@code RPT-yyyy-nnnn})
 * work in every path.
 */
@RestController
@Tag(name = "Reports")
public class ReportsController {

    private final ReportService reports;
    private final ReportDrafter drafter;
    private final ReportPublisher publisher;
    private final ReportSender sender;
    private final ReportShareTarget shareTarget;
    private final ShareLinkService links;
    private final ObjectCallers callers;
    private final ReadCallerResolver readCallers;
    private final AppSettingService settings;
    private final Clock clock;

    /** Creates the controller. */
    public ReportsController(ReportService reports, ReportDrafter drafter, ReportPublisher publisher,
            ReportSender sender, ReportShareTarget shareTarget, ShareLinkService links, ObjectCallers callers,
            ReadCallerResolver readCallers, AppSettingService settings, Clock clock) {
        this.reports = reports;
        this.drafter = drafter;
        this.publisher = publisher;
        this.sender = sender;
        this.shareTarget = shareTarget;
        this.links = links;
        this.callers = callers;
        this.readCallers = readCallers;
        this.settings = settings;
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- CRUD

    /** {@code listReports}: reports the caller owns, authors or that are shared with it. */
    @GetMapping("/v1/reports")
    @Operation(operationId = "listReports", tags = "Reports")
    public Map<String, Object> listReports(@RequestParam(required = false) String status,
            @RequestParam(required = false) String scope, @RequestParam(required = false) String sourceRef,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit,
            HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        if (status != null && !ReportVocabulary.isStatus(status)) {
            throw ValidationException.forField("status", "must be one of " + ReportVocabulary.STATUSES);
        }
        String which = QueryParams.oneOf("scope", scope, List.of("mine", "shared", "all"), "all");
        String source = sourceRef == null || sourceRef.isBlank() ? null
                : ObjectRef.parse(sourceRef.strip(), "sourceRef").format();
        int max = QueryParams.intInRange("limit", limit, 1, 500, 100);
        return reports.list(caller, status, which, source, cursor, max);
    }

    /** {@code createReport}: a draft owned by the caller. */
    @PostMapping("/v1/reports")
    @Operation(operationId = "createReport", tags = "Reports")
    @ApiResponse(responseCode = "201", description = "Created")
    public ResponseEntity<Map<String, Object>> createReport(@RequestBody JsonNode body, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        ReportEntity r = reports.create(body, caller);
        return created(r, caller);
    }

    /** {@code draftReport}: a deterministic draft from a source (optionally with a labeled LLM narrative). */
    @PostMapping("/v1/reports/draft")
    @Operation(operationId = "draftReport", tags = "Reports")
    @ApiResponse(responseCode = "201", description = "Created")
    public ResponseEntity<Map<String, Object>> draftReport(@RequestBody JsonNode body, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        ReportEntity r = drafter.draft(body, caller);
        return created(r, caller);
    }

    private ResponseEntity<Map<String, Object>> created(ReportEntity r, ObjectCaller caller) {
        ReportEntity fresh = reports.get(r.getId());
        return ResponseEntity.created(URI.create("/v1/reports/" + r.getId())).eTag(ReportService.etag(fresh))
                .body(reports.view(fresh, caller));
    }

    /** {@code getReport}: with the draft version as ETag. */
    @GetMapping("/v1/reports/{reportId}")
    @Operation(operationId = "getReport", tags = "Reports")
    public ResponseEntity<Map<String, Object>> getReport(@PathVariable String reportId, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        ReportEntity r = reports.require(reportId, caller, AccessAction.VIEW);
        return ResponseEntity.ok().eTag(ReportService.etag(r)).body(reports.view(r, caller));
    }

    /** {@code updateReport}: a JSON merge patch (editors), optimistic lock on {@code version} or {@code If-Match}. */
    @PatchMapping(value = "/v1/reports/{reportId}", consumes = {"application/merge-patch+json", "application/json"})
    @Operation(operationId = "updateReport", tags = "Reports")
    public ResponseEntity<Map<String, Object>> updateReport(@PathVariable String reportId,
            @RequestHeader(value = "If-Match", required = false) String ifMatch, @RequestBody JsonNode body,
            HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        ReportEntity r = reports.patch(reportId, body, ifMatch, caller);
        return ResponseEntity.ok().eTag(ReportService.etag(r)).body(reports.view(r, caller));
    }

    /** {@code deleteReport}: owner only. */
    @DeleteMapping("/v1/reports/{reportId}")
    @Operation(operationId = "deleteReport", tags = "Reports")
    @ApiResponse(responseCode = "204", description = "Deleted")
    public ResponseEntity<Void> deleteReport(@PathVariable String reportId, HttpServletRequest request) {
        reports.delete(reportId, callers.authenticated(request));
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------------- blocks

    /** {@code createReportBlock}: after {@code after} or at the end (editors, drafts only). */
    @PostMapping("/v1/reports/{reportId}/blocks")
    @Operation(operationId = "createReportBlock", tags = "Reports")
    @ApiResponse(responseCode = "201", description = "Created")
    public ResponseEntity<Map<String, Object>> createReportBlock(@PathVariable String reportId,
            @RequestParam(required = false) String after,
            @RequestHeader(value = "If-Match", required = false) String ifMatch, @RequestBody JsonNode body,
            HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        Map<String, Object> block = reports.addBlock(reportId, body, after, ifMatch, caller);
        ReportEntity r = reports.get(reportId);
        return ResponseEntity.created(URI.create("/v1/reports/" + r.getId() + "/blocks/" + block.get("id")))
                .eTag(ReportService.etag(r)).body(reports.blockView(block, ai.foresignal.collab.PrincipalViews.cache()));
    }

    /** {@code updateReportBlock}: a JSON merge patch of one block (editors, drafts only). */
    @PatchMapping(value = "/v1/reports/{reportId}/blocks/{blockId}",
            consumes = {"application/merge-patch+json", "application/json"})
    @Operation(operationId = "updateReportBlock", tags = "Reports")
    public ResponseEntity<Map<String, Object>> updateReportBlock(@PathVariable String reportId,
            @PathVariable String blockId, @RequestHeader(value = "If-Match", required = false) String ifMatch,
            @RequestBody JsonNode body, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        Map<String, Object> block = reports.patchBlock(reportId, blockId, body, ifMatch, caller);
        ReportEntity r = reports.get(reportId);
        return ResponseEntity.ok().eTag(ReportService.etag(r))
                .body(reports.blockView(block, ai.foresignal.collab.PrincipalViews.cache()));
    }

    /** {@code deleteReportBlock}: an LLM narrative block included (editors, drafts only). */
    @DeleteMapping("/v1/reports/{reportId}/blocks/{blockId}")
    @Operation(operationId = "deleteReportBlock", tags = "Reports")
    @ApiResponse(responseCode = "204", description = "Deleted")
    public ResponseEntity<Void> deleteReportBlock(@PathVariable String reportId, @PathVariable String blockId,
            @RequestHeader(value = "If-Match", required = false) String ifMatch, HttpServletRequest request) {
        reports.deleteBlock(reportId, blockId, ifMatch, callers.authenticated(request));
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------------- publish

    /** {@code publishReport}: freezes an immutable revision with pinned references and its renditions. */
    @PostMapping("/v1/reports/{reportId}/publish")
    @Operation(operationId = "publishReport", tags = "Reports")
    @ApiResponse(responseCode = "201", description = "Revision")
    public ResponseEntity<Map<String, Object>> publishReport(@PathVariable String reportId,
            HttpServletRequest request) {
        ReportRevisionEntity rev = publisher.publish(reportId, callers.authenticated(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(publisher.revisionView(rev));
    }

    /** {@code retractReport}: marks a revision withdrawn (owner) and notifies its recipients. */
    @PostMapping("/v1/reports/{reportId}/retract")
    @Operation(operationId = "retractReport", tags = "Reports")
    public Map<String, Object> retractReport(@PathVariable String reportId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        return publisher.revisionView(publisher.retract(reportId, body, callers.authenticated(request)));
    }

    /** {@code listReportRevisions}: newest first. */
    @GetMapping("/v1/reports/{reportId}/revisions")
    @Operation(operationId = "listReportRevisions", tags = "Reports")
    public Map<String, Object> listReportRevisions(@PathVariable String reportId,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit,
            HttpServletRequest request) {
        int max = QueryParams.intInRange("limit", limit, 1, 500, 100);
        return publisher.revisions(reportId, cursor, max, callers.authenticated(request));
    }

    /** {@code getReportRendition}: Markdown, HTML (TLP banner) or the JSON bundle; {@code draft=true} previews. */
    @GetMapping("/v1/reports/{reportId}/rendition")
    @Operation(operationId = "getReportRendition", tags = "Reports")
    public ResponseEntity<Object> getReportRendition(@PathVariable String reportId,
            @RequestParam(required = false) String format, @RequestParam(required = false) String revision,
            @RequestParam(required = false) String draft, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        String f = QueryParams.oneOf("format", format, List.of("md", "html", "json"), null);
        if (f == null) {
            throw ValidationException.forField("format", "is required: md, html or json");
        }
        Integer number = revision == null || revision.isBlank() ? null
                : QueryParams.intInRange("revision", revision, 1, Integer.MAX_VALUE, 1);
        boolean preview = QueryParams.bool("draft", draft, false);
        return rendition(f, publisher.rendition(reportId, f, number, preview, caller));
    }

    /** A rendition response: inert HTML (sandboxing CSP, {@code nosniff}), Markdown or the JSON bundle. */
    static ResponseEntity<Object> rendition(String format, Object body) {
        return switch (format) {
            case "html" -> ResponseEntity.ok().contentType(new MediaType("text", "html",
                    java.nio.charset.StandardCharsets.UTF_8))
                    .header("Content-Security-Policy", BriefingHtmlRenderer.CONTENT_SECURITY_POLICY)
                    .header("X-Content-Type-Options", "nosniff")
                    .header("Referrer-Policy", "no-referrer")
                    .cacheControl(CacheControl.noStore()).body(body);
            case "md" -> ResponseEntity.ok().contentType(new MediaType("text", "markdown",
                    java.nio.charset.StandardCharsets.UTF_8)).header("X-Content-Type-Options", "nosniff")
                    .cacheControl(CacheControl.noStore()).body(body);
            default -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).cacheControl(CacheControl.noStore())
                    .body(body);
        };
    }

    // ------------------------------------------------------------------------- send and links

    /** {@code sendReport}: viewer grants, notifications and per-recipient TLP checks (owner or editor). */
    @PostMapping("/v1/reports/{reportId}/send")
    @Operation(operationId = "sendReport", tags = "Reports")
    @ApiResponse(responseCode = "201", description = "Send")
    public ResponseEntity<Map<String, Object>> sendReport(@PathVariable String reportId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        ReportSendEntity send = sender.send(reportId, body, callers.authenticated(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(sender.view(send));
    }

    /** {@code createReportShareLink}: a 112 key serving the published HTML (owner; CLEAR, or allowed GREEN). */
    @PostMapping("/v1/reports/{reportId}/share-link")
    @Operation(operationId = "createReportShareLink", tags = "Reports")
    @ApiResponse(responseCode = "201", description = "Created")
    public ResponseEntity<Map<String, Object>> createReportShareLink(@PathVariable String reportId,
            @RequestBody(required = false) JsonNode body, HttpServletRequest request) {
        ObjectCaller caller = callers.authenticated(request);
        ReportEntity r = reports.require(reportId, caller, AccessAction.DELETE);
        Instant expiresAt = null;
        if (body != null && !body.isNull()) {
            CollabInputs.object(body, "ReportShareLinkInput", Set.of("expiresAt"));
            expiresAt = CollabInputs.instant(body, "expiresAt");
        }
        ShareLinkService.Created created = links.create(new ShareLinkService.Create(ReportShareTarget.TYPE, r.getId(),
                null, null, expiresAt, null), ShareLinksController.caller(request, readCallers));
        Map<String, Object> out = ShareLinkService.json(created.link(), clock.instant());
        out.put("key", created.key());
        out.put("url", PublicUrls.base(settings, request) + "/v1/k/" + created.key() + "/report");
        return ResponseEntity.status(HttpStatus.CREATED).body(out);
    }

    /** {@code openReportShareLink}: the published HTML behind a report share key. */
    @GetMapping("/v1/k/{key}/report")
    @Operation(operationId = "openReportShareLink", tags = "Reports")
    @RestrictedRead("serves only a published report rendition labeled CLEAR (or GREEN when allowed), no records")
    public ResponseEntity<Object> openReportShareLink(@PathVariable String key, HttpServletRequest request) {
        ShareLinkEntity link = links.use(key, request.getRemoteAddr());
        if (!ReportShareTarget.TYPE.equals(link.getTargetType())) {
            throw new ai.foresignal.api.error.NotFoundException(ai.foresignal.api.error.ErrorCodes.UNKNOWN_REPORT,
                    "This share link does not point to a report.");
        }
        ResponseEntity<Object> out = rendition("html", shareTarget.html(link));
        return ResponseEntity.status(out.getStatusCode()).headers(h -> {
            h.addAll(out.getHeaders());
            h.set(HttpHeaders.CACHE_CONTROL, CacheControl.noStore().getHeaderValue());
        }).body(out.getBody());
    }
}
