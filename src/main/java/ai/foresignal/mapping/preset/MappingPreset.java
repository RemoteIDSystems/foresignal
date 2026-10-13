package ai.foresignal.mapping.preset;

import ai.foresignal.source.spi.FieldThreshold;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.MotionSpec;
import java.time.Duration;
import java.util.List;
import tools.jackson.databind.JsonNode;

/**
 * One built-in device preset (OpenAPI {@code MappingPreset}, prompt 059), read from
 * {@code src/main/resources/mapping/presets/{id}.json}.
 *
 * <p>The members the API publishes are kept in {@link #document()}; the ones the server also needs are parsed
 * here: the descriptor defaults a custom source created from the preset gets (ingest policy, retention,
 * thresholds, motion), how the format behaves (merging, native reply), and where a {@code lon}/{@code lat}
 * parameter goes.
 *
 * @param id the preset id, also the {@code preset} value of a custom source
 * @param document the preset file as written
 * @param format the ingest format id used at {@code /v1/ingest/{sourceId}/{format}}
 * @param mapping the {@code RecordMapping}, or null when the format is built in ({@code alertmanager})
 * @param ingestPolicy the default ingest policy, or null for ALL
 * @param retention the default retention, or null
 * @param thresholds the field thresholds of a SAMPLED policy
 * @param motion the dead-reckoning fields, or null
 * @param allowsGet true when the format accepts GET
 * @param mergeCurrent true when each record is merged over the entity's current state (split messages)
 * @param locationMode where {@code lon}/{@code lat} go: {@code const} (the mapping's geometry), {@code fallback}
 *     (the mapping's geometry fallback), {@code station} (the format's {@code station} option) or null (ignored)
 */
public record MappingPreset(
        String id,
        JsonNode document,
        String format,
        JsonNode mapping,
        IngestPolicy ingestPolicy,
        Duration retention,
        List<FieldThreshold> thresholds,
        MotionSpec motion,
        boolean allowsGet,
        boolean mergeCurrent,
        String locationMode) {

    /** Copies the thresholds. */
    public MappingPreset {
        thresholds = thresholds == null ? List.of() : List.copyOf(thresholds);
    }

    /** The title. */
    public String title() {
        return document.path("title").asString(id);
    }

    /** The parameter names the preset accepts. */
    public List<String> paramNames() {
        java.util.ArrayList<String> out = new java.util.ArrayList<>();
        document.path("params").forEach(p -> out.add(p.path("name").asString()));
        return out;
    }

    /** The raw {@code setup.urlTemplate}, with {@code {publicBaseUrl}}, {@code {sourceId}} and {@code {key}}. */
    public String urlTemplate() {
        return document.path("setup").path("urlTemplate").asString("");
    }

    /** The raw {@code setup.instructions} (markdown with placeholders). */
    public String instructions() {
        return document.path("setup").path("instructions").asString("");
    }
}
