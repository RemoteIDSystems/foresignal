package ai.foresignal.intel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** Snapshot caps (prompt 101): 16 KB per member, 64 KB per investigation item. */
class SnapshotCapsTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    @DisplayName("a snapshot within the cap is unchanged")
    void small() {
        Map<String, Object> snapshot = Map.of("sourceId", "s", "payload", Map.of("a", 1));
        assertThat(SnapshotCaps.cap(snapshot, SnapshotCaps.MEMBER_MAX_BYTES, JSON)).isSameAs(snapshot);
    }

    @Test
    @DisplayName("an oversized snapshot loses its payloads, keeps its identity and says it was truncated")
    void dropsPayload() {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("sourceId", "s");
        record.put("externalId", "e");
        record.put("payload", "x".repeat(20_000));
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("itemType", "record");
        snapshot.put("record", record);
        Map<String, Object> capped = SnapshotCaps.cap(snapshot, SnapshotCaps.MEMBER_MAX_BYTES, JSON);
        assertThat(SnapshotCaps.size(capped, JSON)).isLessThanOrEqualTo(SnapshotCaps.MEMBER_MAX_BYTES);
        assertThat(capped).containsEntry("truncated", true).containsKey("originalBytes");
        assertThat(capped.get("record")).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> nested = (Map<String, Object>) capped.get("record");
        assertThat(nested).containsEntry("externalId", "e").doesNotContainKey("payload");
    }

    @Test
    @DisplayName("a snapshot too large even without payloads shrinks to its identity")
    void identityOnly() {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("sourceId", "s");
        snapshot.put("title", "t");
        snapshot.put("notes", "y".repeat(70_000));
        Map<String, Object> capped = SnapshotCaps.cap(snapshot, SnapshotCaps.ITEM_MAX_BYTES, JSON);
        assertThat(capped).containsOnlyKeys("sourceId", "title", "truncated", "originalBytes");
    }
}
