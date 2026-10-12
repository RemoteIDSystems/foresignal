package ai.foresignal.trigger;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import tools.jackson.databind.json.JsonMapper;

/**
 * Builds the record snapshot an alert keeps (Core-Data-Model §8): kind, version, observedAt,
 * GeoJSON geometry and payload, at most 16 KB. When the whole snapshot is larger, the payload is
 * dropped and {@code truncated: true} is set; kind, version, time and geometry are always kept.
 */
public final class AlertSnapshots {

    /** The largest snapshot kept whole, in bytes of JSON. */
    public static final int MAX_BYTES = 16 * 1024;

    private AlertSnapshots() {}

    /**
     * The snapshot of {@code row} as JSON text.
     *
     * @param row the matching version
     * @param payload the parsed payload, or null
     * @param json the mapper
     */
    public static String build(MatchRow row, Map<String, Object> payload, JsonMapper json) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("kind", row.kind());
        snapshot.put("version", row.version());
        snapshot.put("observedAt", row.observedAt().toString());
        snapshot.put("geometry", row.geometry() == null ? null : json.readValue(row.geometry(), Map.class));
        snapshot.put("payload", payload);
        snapshot.put("truncated", false);
        String text = json.writeValueAsString(snapshot);
        if (text.getBytes(StandardCharsets.UTF_8).length <= MAX_BYTES) {
            return text;
        }
        snapshot.put("payload", null);
        snapshot.put("truncated", true);
        return json.writeValueAsString(snapshot);
    }
}
