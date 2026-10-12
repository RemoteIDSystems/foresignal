package ai.foresignal.api;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.audit.AuditRecord;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.store.purge.EntityAudit;
import ai.foresignal.store.purge.EntityExportService;
import ai.foresignal.store.purge.EntityPurgeService;
import ai.foresignal.store.purge.ExportFormat;
import ai.foresignal.store.purge.PurgeReport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * {@code exportRecord} and {@code purgeRecord} (prompt 039): export everything retained about one
 * entity, or erase it. Both are admin only ({@code AccessPolicy}) and audited.
 */
@RestController
@Tag(name = "Records")
public class EntityPurgeController {

    private static final Set<String> PURGE_MEMBERS = Set.of("purgeAlerts");

    private final EntityExportService exports;
    private final EntityPurgeService purges;
    private final EntityAudit audit;

    /** Creates the controller. */
    public EntityPurgeController(EntityExportService exports, EntityPurgeService purges, EntityAudit audit) {
        this.exports = exports;
        this.purges = purges;
        this.audit = audit;
    }

    /**
     * {@code exportRecord}: streams the current state, every retained version (with observers), the
     * alerts referencing the entity and a manifest with counts and SHA-256, as GeoJSON, ndjson or GPX.
     */
    @GetMapping("/v1/records/{sourceId}/{externalId}/export")
    @Operation(operationId = "exportRecord", tags = "Records")
    @RestrictedRead("admin only; admin tokens carry no restriction")
    public void exportRecord(@PathVariable String sourceId, @PathVariable String externalId,
            @RequestParam(required = false) String format, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        ExportFormat fmt = ExportFormat.parse(format);
        exports.require(sourceId, externalId);
        response.setStatus(200);
        response.setContentType(fmt.mediaType());
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                .filename(fileName(sourceId, externalId, fmt)).build().toString());
        OutputStream out = new BufferedOutputStream(response.getOutputStream());
        EntityExportService.Summary summary = exports.write(sourceId, externalId, fmt, out);
        out.flush();
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("format", fmt.wireValue());
        detail.put("versions", summary.versions());
        detail.put("alerts", summary.alerts());
        detail.put("sha256", summary.sha256());
        audit.record(AuditRecord.TARGET_ENTITY_EXPORT, CurrentToken.of(request).orElse(null), "GET",
                request.getRequestURI(), "exportRecord", 200, sourceId, externalId, request.getRemoteAddr(), detail);
    }

    /** {@code purgeRecord}: erases one entity and returns the {@code PurgeReport}. */
    @PostMapping("/v1/records/{sourceId}/{externalId}/purge")
    @Operation(operationId = "purgeRecord", tags = "Records")
    public PurgeReport purgeRecord(@PathVariable String sourceId, @PathVariable String externalId,
            @RequestBody(required = false) JsonNode body, HttpServletRequest request) {
        boolean purgeAlerts = purgeAlerts(body);
        PurgeReport report = purges.purge(sourceId, externalId, purgeAlerts);
        audit.record(AuditRecord.TARGET_ENTITY_PURGE, CurrentToken.of(request).orElse(null), "POST",
                request.getRequestURI(), "purgeRecord", 200, sourceId, externalId, request.getRemoteAddr(),
                details(report, purgeAlerts));
        return report;
    }

    /**
     * The {@code purgeAlerts} member of a {@code PurgeRequest} body; false when the body is absent.
     *
     * @throws InvalidInputException for an unknown member or a non-boolean value
     */
    public static boolean purgeAlerts(JsonNode body) {
        if (body == null || body.isNull() || body.isMissingNode()) {
            return false;
        }
        if (!body.isObject()) {
            throw new InvalidInputException("The request body must be a PurgeRequest object.");
        }
        for (String member : body.propertyNames()) {
            if (!PURGE_MEMBERS.contains(member)) {
                throw new InvalidInputException("'" + member + "' is not a PurgeRequest member.");
            }
        }
        JsonNode value = body.get("purgeAlerts");
        if (value == null || value.isNull()) {
            return false;
        }
        if (!value.isBoolean()) {
            throw new InvalidInputException("purgeAlerts must be a boolean.");
        }
        return value.asBoolean();
    }

    /** The audit detail of a purge: counts only. */
    public static Map<String, Object> details(PurgeReport report, boolean purgeAlerts) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("purgeAlerts", purgeAlerts);
        detail.put("tombstoneSeq", report.tombstoneSeq());
        detail.put("versionsDeleted", report.versionsDeleted());
        detail.put("alertsRedacted", report.alertsRedacted());
        detail.put("alertsDeleted", report.alertsDeleted());
        detail.put("embeddingsDeleted", report.embeddingsDeleted());
        detail.put("notificationsRedacted", report.notificationsRedacted());
        return detail;
    }

    private static String fileName(String sourceId, String externalId, ExportFormat format) {
        String base = (sourceId + "-" + externalId).replaceAll("[^A-Za-z0-9._-]", "_");
        if (base.length() > 120) {
            base = base.substring(0, 120);
        }
        return base + "." + format.extension();
    }
}
