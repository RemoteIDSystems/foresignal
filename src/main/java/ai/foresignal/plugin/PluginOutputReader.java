package ai.foresignal.plugin;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.inbound.format.GeoJsonIngestMapper;
import ai.foresignal.inbound.format.ParsedIngest;
import ai.foresignal.source.spi.SourceDescriptor;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads a parser plugin's output (prompt 141, ABI v1): strict UTF-8 JSON {@code {"records":[...],"complete":bool}}.
 *
 * <p>{@code records} is required and holds at most {@link PluginLimits#maxRecords()} items in the
 * {@code NormalizedRecordPreview} shape of the {@code native} ingest format ({@code externalId}, optional
 * {@code kind}, {@code observedAt}, {@code eventTime}, {@code validFrom}, {@code validUntil}, {@code geometry},
 * {@code payload}, {@code tombstone}, {@code quality}); they are mapped by that format's {@link GeoJsonIngestMapper},
 * so a bad item is a per-item rejection, never a failed call. {@code complete} is optional (false when absent) and
 * must be a boolean. Other members are ignored. The records then take the normal ingest path, where the normalizer
 * (prompt 008) applies its own checks.
 */
public final class PluginOutputReader {

    private final GeoJsonIngestMapper mapper;
    private final JsonMapper json;

    /**
     * Creates the reader.
     *
     * @param mapper the native format's record mapper
     * @param json the JSON mapper
     */
    public PluginOutputReader(GeoJsonIngestMapper mapper, JsonMapper json) {
        this.mapper = Objects.requireNonNull(mapper);
        this.json = Objects.requireNonNull(json);
    }

    /**
     * Reads one output.
     *
     * @param plugin the plugin name, for messages
     * @param output the output bytes
     * @param descriptor the calling source's descriptor
     * @param limits the limits (record cap)
     * @return the mapped output
     * @throws PluginException ({@link PluginException.Reason#INVALID_OUTPUT}) when the output is not UTF-8, not JSON,
     *     not the ABI's object, or has too many records
     */
    public ParserOutput read(String plugin, byte[] output, SourceDescriptor descriptor, PluginLimits limits) {
        String text;
        try {
            text = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(output))
                    .toString();
        } catch (CharacterCodingException e) {
            throw invalid(plugin, "the output is not valid UTF-8");
        }
        JsonNode root;
        try {
            root = json.readTree(text);
        } catch (JacksonException e) {
            throw invalid(plugin, "the output is not valid JSON: " + e.getOriginalMessage());
        }
        if (root == null || !root.isObject()) {
            throw invalid(plugin, "the output must be a JSON object {\"records\":[...],\"complete\":bool}");
        }
        JsonNode records = root.get("records");
        if (records == null || !records.isArray()) {
            throw invalid(plugin, "the output has no records array");
        }
        if (records.size() > limits.maxRecords()) {
            throw invalid(plugin, "the output has " + records.size() + " records, over the " + limits.maxRecords()
                    + "-record limit");
        }
        JsonNode complete = root.get("complete");
        if (complete != null && !complete.isNull() && !complete.isBoolean()) {
            throw invalid(plugin, "complete must be a boolean");
        }
        ParsedIngest parsed;
        try {
            parsed = mapper.map(root, descriptor);
        } catch (InvalidInputException e) {
            throw invalid(plugin, e.getMessage());
        }
        return new ParserOutput(parsed, complete != null && complete.asBoolean(false));
    }

    private static PluginException invalid(String plugin, String message) {
        return new PluginException(plugin, PluginException.Reason.INVALID_OUTPUT, message);
    }
}
