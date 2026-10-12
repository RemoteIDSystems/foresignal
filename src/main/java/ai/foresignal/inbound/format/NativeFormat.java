package ai.foresignal.inbound.format;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code native} ingest format (prompt 024): a GeoJSON FeatureCollection or
 * {@code {"records": [...]}}; the default of {@code POST /v1/ingest/{sourceId}}.
 */
@Component
public class NativeFormat implements IngestFormat {

    /** The format id. */
    public static final String ID = "native";

    private final JsonMapper json;
    private final GeoJsonIngestMapper mapper;

    /** Creates the format. */
    public NativeFormat(JsonMapper json, GeoJsonIngestMapper mapper) {
        this.json = json;
        this.mapper = mapper;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Set<MediaType> mediaTypes() {
        return Set.of(MediaType.APPLICATION_JSON, MediaType.parseMediaType("application/geo+json"));
    }

    @Override
    public String description() {
        return "GeoJSON FeatureCollection (id -> externalId) or {\"records\": [...]} in the NormalizedRecordPreview shape";
    }

    @Override
    public ParsedIngest parse(IngestRequest request, SourceDescriptor descriptor, FormatOptions options) {
        return mapper.map(readJson(json, request), descriptor);
    }

    /** Parses the body as JSON; an empty or malformed body is a 400. */
    static JsonNode readJson(JsonMapper json, IngestRequest request) {
        if (!request.hasBody()) {
            throw new InvalidInputException("The request body is empty.");
        }
        try {
            return json.readTree(request.body());
        } catch (JacksonException e) {
            throw new InvalidInputException("The request body is not valid JSON: " + e.getOriginalMessage(), e);
        }
    }
}
