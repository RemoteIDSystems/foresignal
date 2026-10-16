package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.export.ExportJobEntity;
import ai.foresignal.export.ExportJobService;
import ai.foresignal.export.ExportSpec;
import ai.foresignal.raster.ProductUrls;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Bulk exports (OpenAPI tag {@code Exports}, prompt 111): submit, list, download, read the signed
 * manifest, cancel and delete. A job is visible to the token that created it and to admin; any other
 * caller gets {@code 404}. Downloads honor {@code Range} ({@code 206}, {@code 416}) and
 * {@code If-None-Match} against the {@code ETag}, the file's SHA-256 ({@code 304}); Spring MVC serves
 * both from the file resource.
 */
@RestController
@Tag(name = "Exports")
public class BulkExportsController {

    private final ExportJobService exports;
    private final ReadCallerResolver callers;
    private final ProductUrls urls;

    /** Creates the controller. */
    public BulkExportsController(ExportJobService exports, ReadCallerResolver callers, ProductUrls urls) {
        this.exports = exports;
        this.callers = callers;
        this.urls = urls;
    }

    /** {@code createExport}: submit a bulk export, 202 with the job. */
    @PostMapping("/v1/exports")
    @Operation(operationId = "createExport", tags = "Exports")
    public ResponseEntity<Map<String, Object>> createExport(@RequestBody JsonNode body, HttpServletRequest request) {
        ExportSpec spec = ExportSpec.parse(body);
        ExportJobEntity job = exports.submit(spec, ExportCallers.of(request, callers));
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .location(java.net.URI.create("/v1/exports/" + job.getId()))
                .body(exports.view(job, urls.base(request)));
    }

    /** {@code listExports}: the caller's jobs (all jobs for admin), newest first. */
    @GetMapping("/v1/exports")
    @Operation(operationId = "listExports", tags = "Exports")
    public Map<String, Object> listExports(@RequestParam(required = false) String kind,
            @RequestParam(required = false) String status, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        String kindFilter = oneOf("kind", kind, List.of(ExportJobEntity.EXPORT, ExportJobEntity.SNAPSHOT,
                ExportJobEntity.PACK));
        String statusFilter = oneOf("status", status, List.of(ExportJobEntity.QUEUED, ExportJobEntity.RUNNING,
                ExportJobEntity.DONE, ExportJobEntity.FAILED, ExportJobEntity.CANCELLED, ExportJobEntity.EXPIRED));
        ExportJobService.Page page = exports.list(ExportCallers.tokenId(request), ExportCallers.admin(request, callers),
                kindFilter, statusFilter, cursor, parseLimit(limit));
        List<Map<String, Object>> items = new ArrayList<>();
        String base = urls.base(request);
        for (ExportJobEntity job : page.items()) {
            items.add(exports.view(job, base));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", page.nextCursor());
        return out;
    }

    /** {@code downloadExport}: a data file, a named file, or {@code ?bundle=zip}. */
    @GetMapping("/v1/exports/{exportId}/download")
    @Operation(operationId = "downloadExport", tags = "Exports")
    public ResponseEntity<FileSystemResource> downloadExport(@PathVariable String exportId,
            @RequestParam(name = "bundle", required = false) String bundle,
            @RequestParam(name = "file", required = false) String file, HttpServletRequest request) {
        if (bundle != null && !"zip".equals(bundle)) {
            throw ValidationException.forField("bundle", "must be zip");
        }
        ExportJobEntity job = owned(exportId, request);
        ExportJobService.Download download = exports.download(job, file, bundle != null);
        return ResponseEntity.ok()
                .eTag("\"" + download.sha256() + "\"")
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(download.path().getFileName().toString()).build().toString())
                .contentType(MediaType.parseMediaType(download.mediaType()))
                .body(new FileSystemResource(download.path()));
    }

    /** {@code getExportManifest}: {@code manifest.json} with its detached JWS in {@code Foresignal-Signature}. */
    @GetMapping("/v1/exports/{exportId}/manifest")
    @Operation(operationId = "getExportManifest", tags = "Exports")
    public ResponseEntity<byte[]> getExportManifest(@PathVariable String exportId, HttpServletRequest request) {
        ExportJobService.ManifestBytes manifest = exports.manifestBytes(owned(exportId, request));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Foresignal-Signature", manifest.jws().strip())
                .body(manifest.manifest());
    }

    /** {@code deleteExport}: cancel a queued or running job, or delete a finished one. */
    @DeleteMapping("/v1/exports/{exportId}")
    @Operation(operationId = "deleteExport", tags = "Exports")
    public ResponseEntity<Void> deleteExport(@PathVariable String exportId, HttpServletRequest request) {
        exports.delete(owned(exportId, request).getId());
        return ResponseEntity.noContent().build();
    }

    private ExportJobEntity owned(String id, HttpServletRequest request) {
        return exports.visible(id, ExportCallers.tokenId(request), ExportCallers.admin(request, callers));
    }

    private static String oneOf(String field, String value, List<String> allowed) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String upper = value.strip().toUpperCase(Locale.ROOT);
        if (!allowed.contains(upper)) {
            throw ValidationException.forField(field, "must be one of " + allowed);
        }
        return upper;
    }

    static int parseLimit(String raw) {
        if (raw == null || raw.isBlank()) {
            return 100;
        }
        try {
            int value = Integer.parseInt(raw.strip());
            if (value < 1 || value > 500) {
                throw ValidationException.forField("limit", "must be 1–500");
            }
            return value;
        } catch (NumberFormatException e) {
            throw ValidationException.forField("limit", "must be an integer");
        }
    }
}
