package ai.foresignal.inbound;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collect.BudgetGovernor;
import ai.foresignal.media.ImageFormat;
import ai.foresignal.security.IngestCaller;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.RemoteMediaReceiver;
import ai.foresignal.source.spi.SourceDescriptor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code ingestSourceMedia} and {@code ingestSourceRasters} (prompt 054, Weather-Imagery-Design §4.7 b
 * and §6.6): a remote station (for example a Raspberry Pi at the antenna) pushes decoded images and
 * whole pass bundles. Syncing SatDump's output into the inbox with rsync or Syncthing stays the
 * recommended path (§4.7 a); these endpoints are for stations that cannot.
 *
 * <ul>
 *   <li><b>Auth:</b> {@code INGEST_KEY_OR_ADMIN} on {@code /v1/ingest/**} (the token filter): an admin
 *       token, or the source's {@code ingestKey} credential ({@code IngestAuthenticator}). A device key or
 *       a signature-only caller is refused.
 *   <li><b>Routing:</b> {@code media} and {@code rasters} are literal path segments, so these mappings win
 *       over 024's {@code /v1/ingest/{sourceId}/{format}}, and both names are reserved in the
 *       {@code IngestFormatRegistry}, so no format can take them.
 *   <li><b>Sources:</b> only those whose descriptor sets {@code acceptsRemoteMedia} (the
 *       {@code satdump-*} family); any other is {@code 404 NOT_FOUND}.
 *   <li><b>Budget:</b> each push costs one unit of the source's budget ({@code 429 RATE_LIMITED} with
 *       {@code Retry-After} when spent).
 * </ul>
 *
 * <p>{@code /media}: one image ({@code file}, ≤ 25 MB) and {@code meta {passId?, composite, timestamp,
 * satellite}}; the running station guards, stores and thumbnails it and attaches it to a pass (the 049
 * join rule, else a minted pass), answering {@code 202 {mediaId, sha256, passId}}. A station whose stream
 * is not running cannot take it ({@code 404}). {@code /rasters}: {@code meta {passFolder, files[],
 * product?}} plus the file parts ({@code files}), written atomically by {@link RemoteBundles} where the
 * inbox loop processes them like a ready pass, answering {@code 202 {uploadId, passFolder}}.
 */
@RestController
@Tag(name = "Ingest")
public class RemoteUploadController {

    /** The id prefix of a SatDump station source, whose remainder is the station name. */
    static final String SATDUMP_PREFIX = "satdump-";

    private final SourceRegistry registry;
    private final BudgetGovernor budgets;
    private final RemoteMediaRegistry receivers;
    private final RemoteBundles bundles;
    private final JsonMapper json;

    /** Creates the controller. */
    public RemoteUploadController(SourceRegistry registry, BudgetGovernor budgets, RemoteMediaRegistry receivers,
            RemoteBundles bundles, JsonMapper json) {
        this.registry = registry;
        this.budgets = budgets;
        this.receivers = receivers;
        this.bundles = bundles;
        this.json = json;
    }

    /** {@code ingestSourceMedia}: one pushed image. */
    @PostMapping("/v1/ingest/{sourceId}/media")
    @Operation(operationId = "ingestSourceMedia", tags = "Ingest")
    public ResponseEntity<Map<String, Object>> ingestSourceMedia(@PathVariable String sourceId,
            HttpServletRequest request) {
        caller(request, sourceId);
        accepting(sourceId);
        MultipartHttpServletRequest multipart = multipart(request);
        MultipartFile file = multipart.getFile("file");
        if (file == null || file.isEmpty()) {
            throw ValidationException.forField("file", "an image part named 'file' is required");
        }
        if (file.getSize() > RemoteBundles.MAX_FILE_BYTES) {
            throw RemoteBundles.tooLarge("The image is " + file.getSize() + " bytes, over the "
                    + RemoteBundles.MAX_FILE_BYTES + "-byte limit.");
        }
        JsonNode meta = metaJson(multipart);
        String composite = text(meta, "composite");
        String satellite = text(meta, "satellite");
        String timestamp = text(meta, "timestamp");
        if (composite == null || composite.isBlank() || composite.length() > RemoteBundles.MAX_NAME) {
            throw ValidationException.forField("meta.composite", "is required (at most 128 characters)");
        }
        if (satellite == null || satellite.isBlank() || satellite.length() > 128) {
            throw ValidationException.forField("meta.satellite", "is required (at most 128 characters)");
        }
        Instant time;
        try {
            time = Instant.parse(timestamp == null ? "" : timestamp);
        } catch (DateTimeParseException e) {
            throw ValidationException.forField("meta.timestamp", "must be an ISO-8601 instant");
        }
        String passId = text(meta, "passId");
        RemoteMediaReceiver receiver = receivers.find(sourceId).orElseThrow(() -> new NotFoundException(
                "Source '" + sourceId + "' is not running, so it cannot take pushed images now."));
        charge(sourceId);
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the image part: " + e.getMessage(), e);
        }
        String type = contentType(file, bytes);
        RemoteMediaReceiver.Accepted accepted;
        try {
            accepted = receiver.receive(new RemoteMediaReceiver.Upload(bytes, type,
                    passId == null || passId.isBlank() ? null : passId, composite.strip(), time, satellite.strip()));
        } catch (AdapterException e) {
            throw new ForesignalException(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_FAILED,
                    "The image could not be stored: " + e.getMessage());
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mediaId", accepted.mediaId());
        body.put("sha256", accepted.sha256());
        body.put("passId", accepted.passId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(body);
    }

    /** {@code ingestSourceRasters}: one pass bundle. */
    @PostMapping("/v1/ingest/{sourceId}/rasters")
    @Operation(operationId = "ingestSourceRasters", tags = "Ingest")
    public ResponseEntity<Map<String, Object>> ingestSourceRasters(@PathVariable String sourceId,
            HttpServletRequest request) {
        caller(request, sourceId);
        accepting(sourceId);
        MultipartHttpServletRequest multipart = multipart(request);
        RemoteBundles.Meta meta = bundles.meta(metaBytes(multipart));
        List<RemoteBundles.Part> parts = new ArrayList<>();
        for (MultipartFile file : multipart.getFiles("files")) {
            if (file.getSize() > RemoteBundles.MAX_FILE_BYTES) {
                throw RemoteBundles.tooLarge("The file '" + file.getOriginalFilename() + "' is " + file.getSize()
                        + " bytes, over the " + RemoteBundles.MAX_FILE_BYTES + "-byte limit.");
            }
            parts.add(new RemoteBundles.Part(file.getOriginalFilename(), file.getSize(), file::getInputStream));
        }
        if (parts.isEmpty()) {
            throw ValidationException.forField("files", "at least one file part named 'files' is required");
        }
        charge(sourceId);
        RemoteBundles.Accepted accepted = bundles.write(sourceId.substring(SATDUMP_PREFIX.length()), meta, parts);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("uploadId", accepted.uploadId());
        body.put("passFolder", accepted.passFolder());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(body);
    }

    /** The filter's caller, which must be an admin token or a key of this source (not a device key). */
    private static IngestCaller caller(HttpServletRequest request, String sourceId) {
        IngestCaller caller = InboundRequests.caller(request, sourceId);
        if (caller.device() || caller.signatureOnly()) {
            throw new ForesignalException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED,
                    "Pushing images and pass bundles needs the source's ingest key or an admin token.");
        }
        return caller;
    }

    /** The descriptor of a source that accepts remote media, or {@code 404}. */
    private SourceDescriptor accepting(String sourceId) {
        return registry.find(sourceId).map(SourceRegistry.Registration::descriptor)
                .filter(SourceDescriptor::acceptsRemoteMedia)
                .filter(d -> d.id().startsWith(SATDUMP_PREFIX))
                .orElseThrow(() -> NotFoundException.of("Source accepting remote media", sourceId));
    }

    private void charge(String sourceId) {
        if (!budgets.tryConsume(sourceId)) {
            long wait = budgets.secondsUntilAvailable(sourceId);
            if (budgets.status(sourceId).isMonthlyCapReached()) {
                // The money cap of the source's budget (Budget.monthlyCapUsd): refused with BUDGET_EXCEEDED.
                throw new ForesignalException(HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.BUDGET_EXCEEDED,
                        "The source's monthly cap is spent; retry after " + wait + " s.")
                        .withHeader(HttpHeaders.RETRY_AFTER, Long.toString(wait));
            }
            throw new ForesignalException(HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.RATE_LIMITED,
                    "The source's request budget is spent; retry after " + wait + " s.")
                    .withHeader(HttpHeaders.RETRY_AFTER, Long.toString(wait));
        }
    }

    private static MultipartHttpServletRequest multipart(HttpServletRequest request) {
        if (request instanceof MultipartHttpServletRequest multipart) {
            return multipart;
        }
        throw new ForesignalException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ErrorCodes.UNSUPPORTED_MEDIA_TYPE,
                "This endpoint reads multipart/form-data.");
    }

    /** The {@code meta} part's bytes, sent as a file part or a form field. */
    private static byte[] metaBytes(MultipartHttpServletRequest multipart) {
        MultipartFile part = multipart.getFile("meta");
        try {
            if (part != null && !part.isEmpty()) {
                if (part.getSize() > 64 * 1024) {
                    throw ValidationException.forField("meta", "must be at most 64 KB");
                }
                return part.getBytes();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the meta part: " + e.getMessage(), e);
        }
        String field = multipart.getParameter("meta");
        if (field == null || field.isBlank()) {
            throw ValidationException.forField("meta", "a JSON part named 'meta' is required");
        }
        return field.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private JsonNode metaJson(MultipartHttpServletRequest multipart) {
        try {
            JsonNode node = json.readTree(metaBytes(multipart));
            if (node == null || !node.isObject()) {
                throw ValidationException.forField("meta", "must be a JSON object");
            }
            return node;
        } catch (JacksonException e) {
            throw ValidationException.forField("meta", "must be JSON");
        }
    }

    /** The declared type when it is an image type, else the sniffed one. */
    private static String contentType(MultipartFile file, byte[] bytes) {
        String declared = file.getContentType();
        if (declared != null && ImageFormat.ofContentType(declared).isPresent()) {
            return declared;
        }
        try {
            return ai.foresignal.media.ImageGuard.sniff(bytes, Math.min(bytes.length, 8)).contentType();
        } catch (ai.foresignal.source.spi.ImageRejectedException e) {
            throw new ValidationException("The image was rejected: " + e.reason(),
                    List.of(new ai.foresignal.api.error.FieldError("file", e.reason())));
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.path(field);
        return v.isString() ? v.asString() : null;
    }
}
