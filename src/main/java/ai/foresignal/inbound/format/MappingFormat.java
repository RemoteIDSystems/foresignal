package ai.foresignal.inbound.format;

import ai.foresignal.mapping.CompiledMapping;
import ai.foresignal.mapping.MappingCache;
import ai.foresignal.mapping.MappingEngine;
import ai.foresignal.mapping.MappingInputs;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code mapping} ingest format: any JSON document, or a form body, mapped to records by the source's
 * {@code RecordMapping} (prompt 059).
 *
 * <p>Prompt 024 shipped an interim JSON-Pointer mapping under {@code formats.mapping}; prompt 059 replaced it:
 * this format now delegates to {@link MappingEngine} with the mapping stored in the source's {@code mapping}
 * member ({@link MappingCache}). A source without one answers {@code 400}.
 */
@Component
public class MappingFormat implements IngestFormat {

    /** The format id. */
    public static final String ID = "mapping";

    private final JsonMapper json;
    private final MappingCache mappings;
    private final MappingEngine engine;

    /** Creates the format. */
    public MappingFormat(JsonMapper json, MappingCache mappings, MappingEngine engine) {
        this.json = json;
        this.mappings = mappings;
        this.engine = engine;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Set<MediaType> mediaTypes() {
        return Set.of(MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED);
    }

    @Override
    public String description() {
        return "Any JSON or form body, mapped by the source's RecordMapping (its mapping member)";
    }

    @Override
    public ParsedIngest parse(IngestRequest request, SourceDescriptor descriptor, FormatOptions options) {
        CompiledMapping mapping = mappings.require(descriptor, null);
        return engine.apply(MappingInputs.fromRequest(request, json), mapping, request.receivedAt()).toParsedIngest();
    }
}
