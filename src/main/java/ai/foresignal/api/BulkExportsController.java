package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.export.ExportJobEntity;
import ai.foresignal.export.ExportJobService;
import ai.foresignal.export.ExportManifests;
import ai.foresignal.export.ExportSpec;
import ai.foresignal.raster.ProductUrls;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RequestPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Bulk exports (OpenAPI tag {@code Exports}, prompt 111): async GeoParquet/GeoPackage/FlatGeobuf/GPX
 * exports with a signed manifest, plus listing, manifest retrieval and deletion. Ranges and
 * {@code ETag} are honoured on download.
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
        ReadCaller caller = callers.resolve(request);
        boolean admin = caller.scope() != null && caller.scope().includes(Scope.ADMIN);
        ExportJobService.Caller exportCaller = new ExportJobService.Caller(tokenId(request), admin,
                caller.privateAccess(), caller.restriction());
        ExportJobEntity job = exports.submit(spec, exportCaller);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(exports.view(job, urls.base(request)));
    }

    /** {@code listExports}: the caller's own jobs, or every job for an admin. */
    @GetMapping("/v1/exports")
    @Operation(operationId = "listExports", tags = "Exports")
    public Map<String, Object> listExports(@RequestParam(required = false) String limit,
            @RequestParam(required = false) String cursor, HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        boolean admin = caller.scope() != null && caller.scope().includes(Scope.ADMIN);
        int max = parseLimit(limit);
        List<ExportJobEntity> all = exports.list(tokenId(request), admin);
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < all.size() && items.size() < max; i++) {
            items.add(exports.view(all.get(i), urls.base(request)));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", null);
        return out;
    }

    /** {@code downloadExport}: the data file, or {@code ?bundle=zip}. Supports Range and ETag. */
    @GetMapping("/v1/exports/{exportId}/download")
    @Operation(operationId = "downloadExport", tags = "Exports")
    public ResponseEntity<?> downloadExport(@PathVariable String exportId,
            @RequestParam(name = "bundle", required = false) String bundle,
            @RequestParam(name = "file", required = false) String file,
            @RequestHeader(name = HttpHeaders.RANGE, required = false) String rangeHeader,
            @RequestHeader(name = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        ExportJobEntity job = exports.get(exportId);
        ensureOwned(job, request);
        Path path = "zip".equalsIgnoreCase(bundle) ? exports.bundle(job)
                : file == null || file.isBlank() ? exports.primaryFile(job) : exports.file(exportId, file);
        String sha256;
        long length;
        try {
            sha256 = ExportManifests.sha256(path);
            length = Files.size(path);
        } catch (IOException e) {
            throw new ForesignalException(HttpStatus.GONE, ErrorCodes.EXPORT_EXPIRED, "The export file is gone.");
        }
        String etag = "\"" + sha256 + "\"";
        HttpHeaders headers = new HttpHeaders();
        headers.setETag(etag);
        headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
        headers.setContentType(mediaType(path.getFileName().toString()));
        headers.setContentDispositionFormData("attachment", path.getFileName().toString());
        if (ifNoneMatch != null && ifNoneMatch.contains(sha256)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
        }
        if (rangeHeader != null && !rangeHeader.isBlank()) {
            return partial(path, length, rangeHeader, headers);
        }
        headers.setContentLength(length);
        return ResponseEntity.ok().headers(headers).body(new FileSystemResource(path));
    }

    /** {@code getExportManifest}: the signed manifest and its detached JWS as a header. */
    @GetMapping("/v1/exports/{exportId}/manifest")
    @Operation(operationId = "getExportManifest", tags = "Exports")
    public ResponseEntity<byte[]> getExportManifest(@PathVariable String exportId, HttpServletRequest request) {
        ExportJobEntity job = exports.get(exportId);
        ensureOwned(job, request);
        Path manifest = exports.file(exportId, "manifest.json");
        Path jws = exports.file(exportId, "manifest.json.jws");
        try {
            byte[] bytes = Files.readAllBytes(manifest);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Foresignal-Signature", Files.readString(jws));
            return ResponseEntity.ok().headers(headers).body(bytes);
        } catch (IOException e) {
            throw new ForesignalException(HttpStatus.GONE, ErrorCodes.EXPORT_EXPIRED, "The manifest is gone.");
        }
    }

    /** {@code deleteExport}: cancel a running job, or delete a finished one. */
    @DeleteMapping("/v1/exports/{exportId}")
    @Operation(operationId = "deleteExport", tags = "Exports")
    public ResponseEntity<Void> deleteExport(@PathVariable String exportId, HttpServletRequest request) {
        ensureOwned(exports.get(exportId), request);
        exports.delete(exportId);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<byte[]> partial(Path path, long length, String rangeHeader, HttpHeaders headers) {
        List<HttpRange> ranges = HttpRange.parseRanges(rangeHeader);
        if (ranges.isEmpty()) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).headers(headers).build();
        }
        HttpRange range = ranges.get(0);
        long start = range.getRangeStart(length);
        long end = range.getRangeEnd(length);
        if (start >= length || start > end) {
            headers.set(HttpHeaders.CONTENT_RANGE, "bytes */" + length);
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).headers(headers).build();
        }
        int size = (int) Math.min(end - start + 1, Integer.MAX_VALUE);
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            channel.position(start);
            ByteBuffer buffer = ByteBuffer.allocate(size);
            channel.read(buffer);
            headers.set(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + length);
            headers.setContentLength(size);
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).headers(headers).body(buffer.array());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void ensureOwned(ExportJobEntity job, HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        boolean admin = caller.scope() != null && caller.scope().includes(Scope.ADMIN);
        String principal = tokenId(request);
        if (!admin && job.getPrincipal() != null && !job.getPrincipal().equals(principal)) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_EXPORT, "Unknown export " + job.getId() + ".");
        }
    }

    private static int parseLimit(String raw) {
        if (raw == null || raw.isBlank()) {
            return 50;
        }
        try {
            return Math.max(1, Math.min(200, Integer.parseInt(raw)));
        } catch (NumberFormatException e) {
            return 50;
        }
    }

    private static String tokenId(HttpServletRequest request) {
        return RequestPrincipal.of(request).map(p -> p.id() == null ? null : p.id().toString()).orElse(null);
    }

    private static MediaType mediaType(String fileName) {
        if (fileName.endsWith(".parquet")) {
            return MediaType.parseMediaType("application/vnd.apache.parquet");
        }
        if (fileName.endsWith(".gpkg")) {
            return MediaType.parseMediaType("application/geopackage+sqlite3");
        }
        if (fileName.endsWith(".pmtiles")) {
            return MediaType.parseMediaType("application/vnd.pmtiles");
        }
        if (fileName.endsWith(".gpx")) {
            return MediaType.parseMediaType("application/gpx+xml");
        }
        if (fileName.endsWith(".zip")) {
            return MediaType.parseMediaType("application/zip");
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
