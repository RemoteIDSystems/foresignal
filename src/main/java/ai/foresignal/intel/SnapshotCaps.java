package ai.foresignal.intel;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.json.JsonMapper;

/**
 * Size caps on stored snapshots (prompt 101): an event member's snapshot is at most 16 KB (like
 * {@code alert.record_snapshot}) and an investigation item's at most 64 KB, measured as UTF-8 JSON.
 *
 * <p>An oversized snapshot keeps its identity (source, external id, kind, version, seq, times and
 * geometry) and loses its payload, and says so with {@code truncated: true} and the original size in
 * {@code originalBytes}. If even that is too large, only the identity members survive.
 */
public final class SnapshotCaps {

    /** The cap of an event member's snapshot, bytes. */
    public static final int MEMBER_MAX_BYTES = 16 * 1024;

    /** The cap of an investigation item's snapshot, bytes. */
    public static final int ITEM_MAX_BYTES = 64 * 1024;

    /** The members kept when a snapshot must shrink to its identity. */
    static final List<String> IDENTITY = List.of("itemType", "ref", "takenAt", "sourceId", "externalId", "kind",
            "version", "seq", "observedAt", "id", "title", "severity", "status");

    private SnapshotCaps() {}

    /** The UTF-8 JSON size of {@code value}. */
    public static int size(Object value, JsonMapper json) {
        return json.writeValueAsString(value).getBytes(StandardCharsets.UTF_8).length;
    }

    /**
     * {@code snapshot} within {@code maxBytes}: unchanged when it fits; otherwise without payloads (top
     * level and inside nested objects), then reduced to its identity members.
     */
    public static Map<String, Object> cap(Map<String, Object> snapshot, int maxBytes, JsonMapper json) {
        if (snapshot == null) {
            return null;
        }
        int original = size(snapshot, json);
        if (original <= maxBytes) {
            return snapshot;
        }
        Map<String, Object> slim = withoutPayloads(snapshot);
        slim.put("truncated", true);
        slim.put("originalBytes", original);
        if (size(slim, json) <= maxBytes) {
            return slim;
        }
        Map<String, Object> identity = new LinkedHashMap<>();
        for (String key : IDENTITY) {
            Object value = snapshot.get(key);
            if (value != null && !(value instanceof Map<?, ?>) && !(value instanceof List<?>)) {
                identity.put(key, value);
            }
        }
        identity.put("truncated", true);
        identity.put("originalBytes", original);
        return identity;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> withoutPayloads(Map<String, Object> snapshot) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : snapshot.entrySet()) {
            if ("payload".equals(entry.getKey())) {
                continue;
            }
            Object value = entry.getValue();
            out.put(entry.getKey(), value instanceof Map<?, ?> nested ? withoutPayloads((Map<String, Object>) nested)
                    : value);
        }
        return out;
    }
}
