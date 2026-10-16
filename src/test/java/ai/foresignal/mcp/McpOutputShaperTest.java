package ai.foresignal.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.support.RawJson;
import ai.foresignal.ingest.TitleResolver;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/** Output shaping of MCP tool results (prompt 025): short and long forms, caps, truncation notices. */
class McpOutputShaperTest {

    private final JsonMapper json = JsonMapper.builder().build();
    private McpOutputShaper shaper;

    @BeforeEach
    void setUp() {
        SourceRegistry registry = mock(SourceRegistry.class);
        when(registry.find(anyString())).thenReturn(Optional.empty());
        shaper = new McpOutputShaper(json, registry);
    }

    private Map<String, Object> record(String externalId, String geometry, Map<String, Object> payload, boolean live) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", "usgs:" + externalId);
        out.put("source", "usgs");
        out.put("externalId", externalId);
        out.put("kind", "earthquake");
        out.put("title", "M 5.1 - near somewhere");
        out.put("version", 3);
        out.put("seq", 1234L);
        out.put("observedAt", "2026-10-12T08:00:00Z");
        out.put("persistedObservedAt", "2026-10-12T07:00:00Z");
        out.put("live", live);
        out.put("tombstone", false);
        out.put("geometry", geometry == null ? null : RawJson.of(geometry));
        out.put("payload", payload);
        return out;
    }

    @Test
    @DisplayName("short records keep identity, citation fields, a point's position and scalar key fields")
    void shortPoint() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("mag", 5.1);
        payload.put("place", "near somewhere");
        payload.put("nested", Map.of("a", 1));
        ObjectNode item = shaper.record(record("a", "{\"type\":\"Point\",\"coordinates\":[-122.4194155,37.7749295]}",
                payload, false), Detail.SHORT);

        assertThat(item.get("id").asString()).isEqualTo("usgs:a");
        assertThat(item.get("seq").asLong()).isEqualTo(1234L);
        assertThat(item.get("version").asInt()).isEqualTo(3);
        assertThat(item.get("live").asBoolean()).isFalse();
        assertThat(item.has("persistedObservedAt")).as("only live items carry it").isFalse();
        assertThat(item.get("lon").asDouble()).isEqualTo(-122.41942);
        assertThat(item.get("lat").asDouble()).isEqualTo(37.77493);
        assertThat(item.has("bbox")).isFalse();
        assertThat(item.has("geometry")).isFalse();
        assertThat(item.get("fields").get("mag").asDouble()).isEqualTo(5.1);
        assertThat(item.get("fields").has("nested")).as("containers are left out").isFalse();
    }

    @Test
    @DisplayName("live short records carry persistedObservedAt, the cited version's time")
    void liveCarriesPersistedTime() {
        ObjectNode item = shaper.record(record("a", null, Map.of(), true), Detail.SHORT);
        assertThat(item.get("live").asBoolean()).isTrue();
        assertThat(item.get("persistedObservedAt").asString()).isEqualTo("2026-10-12T07:00:00Z");
    }

    @Test
    @DisplayName("geometry is simplified to its centroid, with a bbox for non-points")
    void centroidSimplification() {
        String polygon = "{\"type\":\"Polygon\",\"coordinates\":[[[0,0],[10,0],[10,10],[0,10],[0,0]]]}";
        ObjectNode item = shaper.record(record("p", polygon, Map.of(), false), Detail.SHORT);
        assertThat(item.get("lon").asDouble()).isEqualTo(5.0);
        assertThat(item.get("lat").asDouble()).isEqualTo(5.0);
        assertThat(item.get("bbox").toString()).isEqualTo("[0.0,0.0,10.0,10.0]");

        ObjectNode change = json.createObjectNode();
        change.put("source", "usgs").put("externalId", "c").put("seq", 9);
        change.set("bbox", json.valueToTree(List.of(2.0, 4.0, 2.0, 4.0)));
        ObjectNode fromBox = shaper.shortRecord(change);
        assertThat(fromBox.get("id").asString()).isEqualTo("usgs:c");
        assertThat(fromBox.get("lon").asDouble()).isEqualTo(2.0);
        assertThat(fromBox.has("bbox")).as("a degenerate box is a point").isFalse();
    }

    @Test
    @DisplayName("long records are the full item, payload included")
    void longIsFull() {
        ObjectNode item = shaper.record(record("a", "{\"type\":\"Point\",\"coordinates\":[1,2]}",
                Map.of("mag", 5.1), false), Detail.LONG);
        assertThat(item.get("payload").get("mag").asDouble()).isEqualTo(5.1);
        assertThat(item.get("geometry").get("type").asString()).isEqualTo("Point");
    }

    @Test
    @DisplayName("long strings are cut in short items and key fields stop at the cap")
    void stringsAndFieldCap() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("text", "x".repeat(500));
        for (int i = 0; i < 20; i++) {
            payload.put("f" + i, i);
        }
        ObjectNode item = shaper.record(record("a", null, payload, false), Detail.SHORT);
        assertThat(item.get("fields").size()).isEqualTo(McpOutputShaper.MAX_KEY_FIELDS);
        assertThat(item.get("fields").get("text").asString()).hasSize(McpOutputShaper.MAX_TEXT_CHARS).endsWith("…");
    }

    @Test
    @DisplayName("a list under the cap is returned whole with total and truncated")
    void listUnderCap() {
        List<JsonNode> items = List.of(json.createObjectNode().put("id", "a"), json.createObjectNode().put("id", "b"));
        ObjectNode out = shaper.list(items, 2, false, Detail.SHORT, Map.of("watermarkSeq", 7L));
        assertThat(out.get("items").size()).isEqualTo(2);
        assertThat(out.get("total").asLong()).isEqualTo(2);
        assertThat(out.get("truncated").asBoolean()).isFalse();
        assertThat(out.get("watermarkSeq").asLong()).isEqualTo(7);
        assertThat(out.has("notice")).isFalse();
    }

    @Test
    @DisplayName("a list over 4 KB is cut from the end with a notice; long allows 50 KB; neither exceeds its cap")
    void listCaps() {
        List<JsonNode> items = new ArrayList<>();
        for (int i = 0; i < 400; i++) {
            items.add(json.createObjectNode().put("id", "usgs:" + i).put("text", "y".repeat(150)));
        }
        ObjectNode compact = shaper.list(items, 400, false, Detail.SHORT, null);
        assertThat(McpOutputShaper.size(compact)).isLessThanOrEqualTo(McpOutputShaper.COMPACT_LIMIT_BYTES);
        assertThat(compact.get("truncated").asBoolean()).isTrue();
        assertThat(compact.get("total").asLong()).isEqualTo(400);
        assertThat(compact.get("notice").asString()).contains("4 KB").contains("detail=long");
        int kept = compact.get("items").size();
        assertThat(kept).isBetween(1, 399);
        assertThat(compact.get("items").get(kept - 1).get("id").asString()).isEqualTo("usgs:" + (kept - 1));

        ObjectNode full = shaper.list(items, 400, false, Detail.LONG, null);
        assertThat(McpOutputShaper.size(full)).isLessThanOrEqualTo(McpOutputShaper.LONG_LIMIT_BYTES);
        assertThat(full.get("items").size()).isGreaterThan(kept);
        assertThat(full.get("notice").asString()).contains("50 KB");
    }

    @Test
    @DisplayName("a single item over the whole budget is trimmed, not dropped")
    void oversizedSingleItem() {
        ObjectNode big = json.createObjectNode().put("id", "usgs:big").put("seq", 1);
        big.set("payload", json.valueToTree(Map.of("blob", "z".repeat(10_000))));
        ObjectNode out = shaper.list(List.of(big), 1, false, Detail.SHORT, null);
        ArrayNode items = (ArrayNode) out.get("items");
        assertThat(items.size()).isEqualTo(1);
        assertThat(items.get(0).get("payload").asString()).startsWith("omitted:");
        assertThat(McpOutputShaper.size(out)).isLessThanOrEqualTo(McpOutputShaper.COMPACT_LIMIT_BYTES);
    }

    @Test
    @DisplayName("an object over its cap loses its bulkiest members first, then is reduced to its identity")
    void objectFit() {
        ObjectNode value = json.createObjectNode().put("id", "usgs:a").put("seq", 5);
        value.set("payload", json.valueToTree(Map.of("blob", "p".repeat(60_000))));
        value.set("geometry", json.valueToTree(Map.of("type", "Point")));
        ObjectNode out = shaper.object(value, Detail.LONG);
        assertThat(out.get("payload").asString()).startsWith("omitted:");
        assertThat(out.get("geometry").isObject()).as("smaller members stay").isTrue();
        assertThat(out.get("notice").asString()).contains("payload");
        assertThat(McpOutputShaper.size(out)).isLessThanOrEqualTo(McpOutputShaper.LONG_LIMIT_BYTES);

        ObjectNode many = json.createObjectNode().put("id", "usgs:m");
        for (int i = 0; i < 2_000; i++) {
            many.put("k" + i, i);
        }
        ObjectNode reduced = shaper.object(many, Detail.SHORT);
        assertThat(reduced.get("id").asString()).isEqualTo("usgs:m");
        assertThat(reduced.get("notice").asString()).contains("identity");
        assertThat(McpOutputShaper.size(reduced)).isLessThanOrEqualTo(McpOutputShaper.COMPACT_LIMIT_BYTES);
    }

    @Test
    @DisplayName("detail parses short and long, defaults to short and rejects anything else")
    void detailParsing() {
        assertThat(Detail.of(json.createObjectNode())).isEqualTo(Detail.SHORT);
        assertThat(Detail.of(json.createObjectNode().put("detail", "long"))).isEqualTo(Detail.LONG);
        assertThat(Detail.SHORT.limit()).isEqualTo(4096);
        assertThat(Detail.LONG.limit()).isEqualTo(51_200);
        assertThatThrownBy(() -> Detail.of(json.createObjectNode().put("detail", "full")))
                .isInstanceOf(McpToolException.class);
    }

    @Test
    @DisplayName("the title resolver order is titleField, then title > name > callsign > place")
    void titleResolverOrder() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("place", "P");
        payload.put("callsign", "C");
        payload.put("name", "N");
        assertThat(TitleResolver.resolve((String) null, payload)).isEqualTo("N");
        payload.put("title", "T");
        assertThat(TitleResolver.resolve((String) null, payload)).isEqualTo("T");
        assertThat(TitleResolver.resolve("place", payload)).isEqualTo("P");
        assertThat(TitleResolver.resolve("missing", Map.of("callsign", "C", "place", "P"))).isEqualTo("C");
        assertThat(TitleResolver.FALLBACK_FIELDS).containsExactly("title", "name", "callsign", "place", "r", "hex", "mmsi",
                "routeId");
    }

    @Test
    @DisplayName("short records keep the stored title and cut it to the text cap")
    void shortTitle() {
        Map<String, Object> mapped = record("a", null, Map.of(), false);
        mapped.put("title", "t".repeat(300));
        ObjectNode item = shaper.record(mapped, Detail.SHORT);
        assertThat(item.get("title").asString()).hasSize(McpOutputShaper.MAX_TEXT_CHARS);
    }

    @Test
    @DisplayName("a source's llmNotice rides on short and long records and on search hits (prompt 090)")
    void llmNotice() {
        SourceRegistry registry = mock(SourceRegistry.class);
        when(registry.find(anyString())).thenReturn(Optional.empty());
        SourceDescriptor noticed = SourceDescriptor.builder("usgs").name("noticed").kind("earthquake")
                .attribution("test").mode(Mode.PUSH).defaultBudget(Budget.of(60, 1_000))
                .llmNotice("Do not use for eligibility.").build();
        when(registry.find("usgs")).thenReturn(Optional.of(new SourceRegistry.Registration(noticed, null, false)));
        McpOutputShaper noticing = new McpOutputShaper(json, registry);

        assertThat(noticing.record(record("a", null, Map.of(), false), Detail.SHORT).get("llmNotice").asString())
                .isEqualTo("Do not use for eligibility.");
        assertThat(noticing.record(record("a", null, Map.of(), false), Detail.LONG).get("llmNotice").asString())
                .isEqualTo("Do not use for eligibility.");

        ObjectNode bySource = json.createObjectNode();
        noticing.searchHitNotice(Map.of("source", "usgs", "docId", "usgs:a"), bySource);
        assertThat(bySource.get("llmNotice").asString()).isEqualTo("Do not use for eligibility.");
        ObjectNode byDocId = json.createObjectNode();
        noticing.searchHitNotice(Map.of("docType", "record", "docId", "usgs:a"), byDocId);
        assertThat(byDocId.get("llmNotice").asString()).isEqualTo("Do not use for eligibility.");
        ObjectNode other = json.createObjectNode();
        noticing.searchHitNotice(Map.of("docType", "briefing", "docId", "b-1"), other);
        assertThat(other.has("llmNotice")).isFalse();
        ObjectNode none = json.createObjectNode();
        shaper.searchHitNotice(Map.of("source", "usgs"), none);
        assertThat(none.has("llmNotice")).as("a source without a notice adds none").isFalse();
    }
}
