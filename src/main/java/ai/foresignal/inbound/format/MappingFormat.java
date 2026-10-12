package ai.foresignal.inbound.format;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.List;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code mapping} ingest format (prompt 024): arbitrary JSON mapped to records by the source's
 * {@link RecordMapping} (its {@code formats.mapping} options).
 */
@Component
public class MappingFormat implements IngestFormat {

    /** The format id. */
    public static final String ID = "mapping";

    private final JsonMapper json;
    private final RecordMappingEngine engine;

    /** Creates the format. */
    public MappingFormat(JsonMapper json, RecordMappingEngine engine) {
        this.json = json;
        this.engine = engine;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Set<MediaType> mediaTypes() {
        return Set.of(MediaType.APPLICATION_JSON);
    }

    @Override
    public String description() {
        return "Any JSON, mapped by the source's JSON Pointer mapping (formats.mapping)";
    }

    @Override
    public List<FormatOptionSpec> options() {
        return List.of(
                new FormatOptionSpec("recordsPath", "string", false, "", "JSON Pointer to the records array"),
                new FormatOptionSpec("externalId", "string", true, null, "JSON Pointer to the external id"),
                new FormatOptionSpec("kind", "string", false, null, "JSON Pointer to the kind"),
                new FormatOptionSpec("observedAt", "string", false, null, "JSON Pointer to the observation time"),
                new FormatOptionSpec("observedAtFormat", "string", false, "ISO", "ISO, epoch-s or epoch-ms"),
                new FormatOptionSpec("lat", "string", false, null, "JSON Pointer to the latitude"),
                new FormatOptionSpec("lon", "string", false, null, "JSON Pointer to the longitude"),
                new FormatOptionSpec("geometry", "string", false, null, "JSON Pointer to a GeoJSON geometry"),
                new FormatOptionSpec("payload", "array", false, null, "[{from, to}] payload fields"));
    }

    @Override
    public ParsedIngest parse(IngestRequest request, SourceDescriptor descriptor, FormatOptions options) {
        if (options.node().isEmpty()) {
            throw new InvalidInputException("This source has no mapping (formats.mapping).");
        }
        RecordMapping mapping = RecordMapping.parse(options.node());
        return engine.apply(mapping, NativeFormat.readJson(json, request), descriptor);
    }
}
