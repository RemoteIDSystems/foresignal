package ai.foresignal.inbound;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.inbound.format.CloudEventsFormat;
import ai.foresignal.inbound.format.FormatOptionSpec;
import ai.foresignal.inbound.format.IngestFormat;
import ai.foresignal.inbound.format.IngestFormatRegistry;
import ai.foresignal.inbound.format.NativeReply;
import ai.foresignal.inbound.signature.SignatureVerifiers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code ingestRecords}, {@code ingestRecordsFormat}, {@code ingestWebhookHandshake} and
 * {@code listIngestFormats} (Architecture §16, prompt 024).
 *
 * <p>Authentication happens in the token filter (ingest key, admin token, or a keyOptional
 * signature); this controller reads the capped body, verifies a configured signature
 * ({@code 401 SIGNATURE_INVALID}), parses it with the named {@link IngestFormat} and hands the
 * records to {@link PushIngestService}. The response is the v1.2 {@code IngestOutcome}, 200 whenever
 * the request itself was valid, even with per-record rejections.
 *
 * <p>Prompt 039 adds {@code ingestRecordsFormatGet} (GET for formats that declare it: OsmAnd/Traccar
 * Client and GPSLogger; any other format answers {@code 405}), and the key-in-path forms
 * {@code ingestRecordsPathKey} / {@code ingestRecordsPathKeyGet} at
 * {@code /v1/ingest/{sourceId}/k/{key}/{format}} for the formats {@link #PATH_KEY_FORMATS} (others are
 * {@code 404 FORMAT_UNKNOWN}). The {@code {key}} path variable is never read here: the key was taken
 * and redacted by {@link IngestKeyRedactionFilter} and checked by {@link IngestAuthenticator}. A
 * format with a {@link NativeReply} (OwnTracks {@code []}, Overland {@code {"result":"ok"}}, OsmAnd and
 * GPSLogger an empty 200) gets that reply instead of the {@code IngestOutcome}.
 */
@RestController
@Tag(name = "Ingest")
public class IngestController {

    private final IngestFormatRegistry formats;
    private final InboundRequests inbound;
    private final SignatureVerifiers verifiers;
    private final PushIngestService pushes;
    private final ai.foresignal.source.SourceRegistry registry;
    private final ai.foresignal.source.SourceService sources;

    /** Creates the controller. */
    public IngestController(IngestFormatRegistry formats, InboundRequests inbound, SignatureVerifiers verifiers,
            PushIngestService pushes, ai.foresignal.source.SourceRegistry registry,
            ai.foresignal.source.SourceService sources) {
        this.formats = formats;
        this.inbound = inbound;
        this.verifiers = verifiers;
        this.pushes = pushes;
        this.registry = registry;
        this.sources = sources;
    }

    /** {@code ingestRecords}: the {@code native} format (GeoJSON FeatureCollection or {@code {records}}). */
    @PostMapping("/v1/ingest/{sourceId}")
    @Operation(operationId = "ingestRecords", tags = "Ingest")
    public IngestOutcomeView ingestRecords(@PathVariable String sourceId, HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType != null && !isJson(contentType)) {
            throw new ForesignalException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ErrorCodes.UNSUPPORTED_MEDIA_TYPE,
                    "POST /v1/ingest/{sourceId} reads JSON (application/json or application/geo+json); use a named"
                            + " format for anything else.");
        }
        return inbound.push(sourceId, format(IngestFormatRegistry.DEFAULT_FORMAT), request).view();
    }

    /** {@code ingestRecordsFormat}: a named format; unknown or reserved names are {@code 404 FORMAT_UNKNOWN}. */
    @PostMapping("/v1/ingest/{sourceId}/{format}")
    @Operation(operationId = "ingestRecordsFormat", tags = "Ingest")
    public ResponseEntity<?> ingestRecordsFormat(@PathVariable String sourceId, @PathVariable String format,
            HttpServletRequest request) {
        return reply(inbound.receive(sourceId, format(format), request));
    }

    /** {@code ingestRecordsFormatGet}: a GET push (query parameters) for a format that declares GET. */
    @GetMapping("/v1/ingest/{sourceId}/{format}")
    @Operation(operationId = "ingestRecordsFormatGet", tags = "Ingest")
    public ResponseEntity<?> ingestRecordsFormatGet(@PathVariable String sourceId, @PathVariable String format,
            HttpServletRequest request) {
        return reply(inbound.receive(sourceId, format(format), request));
    }

    /** {@code ingestRecordsPathKey}: a POST push with the ingest or device key in the path. */
    @PostMapping("/v1/ingest/{sourceId}/k/{key}/{format}")
    @Operation(operationId = "ingestRecordsPathKey", tags = "Ingest")
    public ResponseEntity<?> ingestRecordsPathKey(@PathVariable String sourceId, @PathVariable String key,
            @PathVariable String format, HttpServletRequest request) {
        return reply(inbound.receive(sourceId, pathKeyFormat(format), request));
    }

    /** {@code ingestRecordsPathKeyGet}: a GET push with the ingest or device key in the path. */
    @GetMapping("/v1/ingest/{sourceId}/k/{key}/{format}")
    @Operation(operationId = "ingestRecordsPathKeyGet", tags = "Ingest")
    public ResponseEntity<?> ingestRecordsPathKeyGet(@PathVariable String sourceId, @PathVariable String key,
            @PathVariable String format, HttpServletRequest request) {
        return reply(inbound.receive(sourceId, pathKeyFormat(format), request));
    }

    /** The formats served on the key-in-path URLs (the spec's enum). */
    public static final java.util.Set<String> PATH_KEY_FORMATS =
            java.util.Set.of("osmand", "gpslogger", "owntracks", "overland", "native");

    private IngestFormat pathKeyFormat(String id) {
        if (!PATH_KEY_FORMATS.contains(id)) {
            throw unknownFormat(id);
        }
        return format(id);
    }

    /** The native reply when the format has one, else the {@code IngestOutcome}. */
    static ResponseEntity<?> reply(InboundRequests.Received received) {
        NativeReply nativeReply = received.reply();
        if (nativeReply == null) {
            return ResponseEntity.ok(received.result().view());
        }
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(nativeReply.status());
        if (nativeReply.contentType() == null) {
            return builder.build();
        }
        return builder.contentType(nativeReply.contentType()).body(nativeReply.body());
    }

    /**
     * {@code ingestWebhookHandshake}: the CloudEvents HTTP webhook abuse-protection handshake. Only
     * the {@code cloudevents} format answers; the allowed rate is the source's per-minute budget.
     */
    @RequestMapping(path = "/v1/ingest/{sourceId}/{format}", method = RequestMethod.OPTIONS)
    @Operation(operationId = "ingestWebhookHandshake", tags = "Ingest")
    public ResponseEntity<Void> ingestWebhookHandshake(@PathVariable String sourceId, @PathVariable String format,
            HttpServletRequest request) {
        if (!"cloudevents".equals(format)) {
            throw unknownFormat(format);
        }
        registry.find(sourceId).map(ai.foresignal.source.SourceRegistry.Registration::descriptor)
                .filter(PushIngestService::acceptsPush)
                .orElseThrow(() -> ai.foresignal.api.error.NotFoundException.of("Push source", sourceId));
        String origin = CloudEventsFormat.handshakeAllowedOrigin(request.getHeader("WebHook-Request-Origin"));
        if (origin == null) {
            throw new InvalidInputException("The WebHook-Request-Origin header is required.");
        }
        int rate = sources.view(sourceId).budget().perMinute();
        return ResponseEntity.ok()
                .header("WebHook-Allowed-Origin", origin)
                .header("WebHook-Allowed-Rate", Integer.toString(rate))
                .header(HttpHeaders.ALLOW, "POST, OPTIONS")
                .build();
    }

    /** {@code listIngestFormats}: every registered format with its options and verifiers, plus reserved names. */
    @GetMapping("/v1/ingest/formats")
    @Operation(operationId = "listIngestFormats", tags = "Ingest")
    public List<Map<String, Object>> listIngestFormats() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (IngestFormat format : formats.all()) {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("id", format.id());
            info.put("description", format.description());
            info.put("mediaTypes", format.mediaTypes().stream().map(MediaType::toString).sorted().toList());
            info.put("methods", format.methods().stream().map(m -> m.name()).sorted().toList());
            List<Map<String, Object>> options = new ArrayList<>();
            for (FormatOptionSpec option : format.options()) {
                Map<String, Object> o = new LinkedHashMap<>();
                o.put("name", option.name());
                o.put("type", option.type());
                o.put("required", option.required());
                if (option.defaultValue() != null) {
                    o.put("default", option.defaultValue());
                }
                if (option.description() != null) {
                    o.put("description", option.description());
                }
                options.add(o);
            }
            info.put("options", options);
            info.put("verifiers", verifiers.schemes());
            info.put("reserved", false);
            out.add(info);
        }
        for (String reserved : IngestFormatRegistry.RESERVED.stream().sorted().toList()) {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("id", reserved);
            info.put("description", "Reserved for prompt " + ("mapping-preview".equals(reserved) ? "059" : "054"));
            info.put("mediaTypes", List.of());
            info.put("reserved", true);
            out.add(info);
        }
        return out;
    }

    private IngestFormat format(String id) {
        return formats.find(id).orElseThrow(() -> unknownFormat(id));
    }

    private ForesignalException unknownFormat(String id) {
        String detail = formats.isReserved(id)
                ? "The ingest format '" + id + "' is reserved and not available yet."
                : "There is no ingest format '" + id + "'.";
        return new ForesignalException(HttpStatus.NOT_FOUND, ErrorCodes.FORMAT_UNKNOWN, detail);
    }

    private static boolean isJson(String contentType) {
        try {
            MediaType type = MediaType.parseMediaType(contentType);
            return type.isCompatibleWith(MediaType.APPLICATION_JSON) || "geo+json".equals(type.getSubtype())
                    || type.getSubtype().endsWith("+json");
        } catch (org.springframework.util.InvalidMimeTypeException e) {
            return false;
        }
    }
}
