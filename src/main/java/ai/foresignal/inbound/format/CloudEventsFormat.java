package ai.foresignal.inbound.format;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * The {@code cloudevents} ingest format (prompt 024): CloudEvents 1.0 in structured mode
 * ({@code application/cloudevents+json}), batch mode ({@code application/cloudevents-batch+json})
 * and the HTTP binary mode ({@code ce-*} headers, the body is {@code data}).
 *
 * <p>externalId is {@code subject}, else {@code source + "/" + id}; the kind comes from the
 * {@code typeKinds} option, else the source's single kind. The HTTP webhook abuse-protection
 * handshake ({@code OPTIONS} with {@code WebHook-Request-Origin}) is answered by the ingest
 * controller with {@link #handshakeAllowedOrigin(String)}.
 */
@Component
public class CloudEventsFormat implements IngestFormat {

    /** The format id. */
    public static final String ID = "cloudevents";

    /** Structured mode. */
    public static final MediaType STRUCTURED = MediaType.parseMediaType("application/cloudevents+json");

    /** Batch mode. */
    public static final MediaType BATCH = MediaType.parseMediaType("application/cloudevents-batch+json");

    private final JsonMapper json;
    private final GeoJsonIngestMapper mapper;

    /** Creates the format. */
    public CloudEventsFormat(JsonMapper json, GeoJsonIngestMapper mapper) {
        this.json = json;
        this.mapper = mapper;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Set<MediaType> mediaTypes() {
        return Set.of(STRUCTURED, BATCH, MediaType.APPLICATION_JSON, MediaType.ALL);
    }

    @Override
    public String description() {
        return "CloudEvents 1.0: structured, batch and HTTP binary mode";
    }

    @Override
    public List<FormatOptionSpec> options() {
        return List.of(new FormatOptionSpec("typeKinds", "object", false, null,
                "CloudEvents type -> kind; else the source's single kind, else rejected"));
    }

    /**
     * The origin to echo in {@code WebHook-Allowed-Origin} for a validation handshake.
     *
     * @return the trimmed origin, or null when none was given
     */
    public static String handshakeAllowedOrigin(String requestOrigin) {
        if (requestOrigin == null || requestOrigin.isBlank()) {
            return null;
        }
        return requestOrigin.strip();
    }

    @Override
    public ParsedIngest parse(IngestRequest request, SourceDescriptor descriptor, FormatOptions options) {
        Map<String, String> typeKinds = options.stringMap("typeKinds");
        ParsedIngest.Builder out = ParsedIngest.builder();
        if (request.isContentType(BATCH)) {
            JsonNode body = NativeFormat.readJson(json, request);
            if (!body.isArray()) {
                throw new InvalidInputException("A CloudEvents batch must be a JSON array.");
            }
            int index = 0;
            for (JsonNode event : body) {
                map(index++, event, descriptor, typeKinds, out);
            }
            out.items(index);
        } else if (request.header("ce-id") != null || request.header("ce-specversion") != null) {
            map(0, binaryEvent(request), descriptor, typeKinds, out);
        } else if (request.isContentType(STRUCTURED) || request.isContentType(MediaType.APPLICATION_JSON)) {
            JsonNode body = NativeFormat.readJson(json, request);
            if (!body.isObject()) {
                throw new InvalidInputException("A structured CloudEvent must be a JSON object.");
            }
            map(0, body, descriptor, typeKinds, out);
        } else {
            throw new InvalidInputException(
                    "Send a structured or batch CloudEvent, or binary mode with ce-* headers.");
        }
        return out.build();
    }

    /** Rebuilds a structured event from binary-mode headers and the body. */
    ObjectNode binaryEvent(IngestRequest request) {
        ObjectNode event = JsonNodeFactory.instance.objectNode();
        putHeader(event, "specversion", request.header("ce-specversion"));
        putHeader(event, "id", request.header("ce-id"));
        putHeader(event, "source", request.header("ce-source"));
        putHeader(event, "type", request.header("ce-type"));
        putHeader(event, "subject", request.header("ce-subject"));
        putHeader(event, "time", request.header("ce-time"));
        if (request.hasBody()) {
            JsonNode data;
            try {
                data = json.readTree(request.body());
            } catch (JacksonException e) {
                data = JsonNodeFactory.instance.stringNode(new String(request.body(), StandardCharsets.UTF_8));
            }
            event.set("data", data);
        }
        return event;
    }

    private static void putHeader(ObjectNode event, String name, String value) {
        if (value != null) {
            event.put(name, value);
        }
    }

    private void map(int index, JsonNode event, SourceDescriptor descriptor, Map<String, String> typeKinds,
            ParsedIngest.Builder out) {
        if (event == null || !event.isObject()) {
            out.reject(index, null, "INVALID_ITEM", "a CloudEvent must be an object");
            return;
        }
        String specversion = GeoJsonIngestMapper.text(event.get("specversion"));
        String id = GeoJsonIngestMapper.text(event.get("id"));
        String source = GeoJsonIngestMapper.text(event.get("source"));
        String type = GeoJsonIngestMapper.text(event.get("type"));
        String subject = GeoJsonIngestMapper.text(event.get("subject"));
        String externalId = subject != null && !subject.isBlank()
                ? subject
                : (source != null && id != null ? source + "/" + id : null);
        if (!"1.0".equals(specversion) || isBlank(id) || isBlank(source) || isBlank(type)) {
            out.reject(index, externalId, "INVALID_ITEM", "specversion 1.0, id, source and type are required");
            return;
        }
        String mapped = typeKinds.get(type);
        String kind = mapped != null
                ? GeoJsonIngestMapper.resolveKind(mapped, descriptor)
                : GeoJsonIngestMapper.resolveKind(null, descriptor);
        if (kind == null) {
            out.reject(index, externalId, "UNKNOWN_KIND", "no kind for CloudEvents type " + type);
            return;
        }
        Instant time;
        try {
            time = IngestTimes.parse(event.get("time"));
        } catch (InvalidInputException e) {
            out.reject(index, externalId, "INVALID_ITEM", e.getMessage());
            return;
        }
        JsonNode data = event.get("data");
        Geometry geometry;
        try {
            geometry = geometry(data);
        } catch (InvalidInputException e) {
            out.reject(index, externalId, "GEOMETRY_INVALID", e.getMessage());
            return;
        }
        NormalizedRecord.Builder record = NormalizedRecord.builder(externalId, kind).observedAt(time).geometry(geometry);
        if (data != null && data.isObject()) {
            Map<String, Object> payload = mapper.toMap(data);
            payload.remove("geometry");
            record.putAll(payload);
        } else if (data != null && !data.isNull()) {
            record.put("data", mapper.toJava(data));
        }
        record.put("ceType", type).put("ceSource", source);
        out.add(index, record.build());
    }

    private Geometry geometry(JsonNode data) {
        if (data == null || !data.isObject()) {
            return null;
        }
        JsonNode geometry = data.get("geometry");
        if (geometry != null && !geometry.isNull()) {
            return mapper.geometry(geometry);
        }
        JsonNode lat = data.get("lat");
        JsonNode lon = data.get("lon");
        if (lat != null && lon != null && lat.isNumber() && lon.isNumber()) {
            double y = lat.doubleValue();
            double x = lon.doubleValue();
            if (!Double.isFinite(x) || !Double.isFinite(y) || y < -90 || y > 90 || x < -180 || x > 180) {
                throw new InvalidInputException("lat/lon are out of range");
            }
            return Geo.factory().createPoint(new Coordinate(x, y));
        }
        return null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
