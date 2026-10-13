package ai.foresignal.source.adapters.roads;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * {@link WzdxParser} on real WZDx 4.2 (WSDOT) and 4.0 (UDOT, NDDOT) feeds captured 2026-10-13, a refused 3.1
 * (Québec) and CWZ document, and hand-made edge cases.
 */
class WzdxParserTest {

    static final Instant NOW = Instant.parse("2026-10-13T19:00:00Z");

    static ParsedEvents parse(String name) {
        return WzdxParser.parse(AdapterFixtures.bytes("wzdx", name), NOW);
    }

    static SourceDescriptor descriptor() {
        return new WzdxAdapter(WzdxRegistry.load().stream().filter(f -> f.feedname().equals("wsdot")).findFirst()
                .orElseThrow(), null).descriptor();
    }

    @Test
    @DisplayName("WZDx 4.2 (WSDOT): 20 work zones, the 4.2 version read from feed_info")
    void wsdot42() {
        ParsedEvents parsed = parse("wsdot-4.2.json");
        List<NormalizedRecord> records = parsed.records();
        assertThat(records).hasSize(20);
        assertThat(parsed.skipped()).isEmpty();
        assertThat(records).allSatisfy(r -> {
            assertThat(r.payload()).containsEntry("category", "roadwork").containsEntry("format", "wzdx")
                    .containsEntry("subtype", "work-zone").containsEntry("severity", "unknown")
                    .containsEntry("isClosure", false).containsKey("headline").containsKey("direction");
            assertThat(r.validFrom()).isNotNull();
            assertThat(r.validUntil()).isNotNull();
            assertThat(r.geometry().getGeometryType()).isEqualTo("LineString");
        });
        assertThat(records.stream().filter(r -> "planned".equals(r.payload().get("status")))).hasSize(16);
        NormalizedRecord first = records.getFirst();
        assertThat(first.externalId()).isEqualTo("40701-E");
        assertThat(first.observedAt()).isEqualTo(Instant.parse("2024-05-31T21:29:37.1738617Z"));
        assertThat(first.validFrom()).isEqualTo(Instant.parse("2025-03-31T00:00:00Z"));
        assertThat(first.validUntil()).isEqualTo(Instant.parse("2027-11-30T00:00:00Z"));
        assertThat(first.payload()).containsEntry("roads", List.of("090")).containsEntry("direction", "eastbound")
                .containsEntry("headline", "I-90/SR 202 Soderman Creek and UNT to Patterson Creek - Fish Passage")
                .doesNotContainKey("speedLimitKph");
        assertThat(records.stream().filter(r -> r.externalId().contains("~"))).as("one repeated id, two entries")
                .hasSize(2);
        FieldSpecContract.assertPayloadKeysDeclared(descriptor(), records);
    }

    @Test
    @DisplayName("WZDx 4.0 (UDOT road_event_feed_info, NDDOT with lane impacts): read, repeated ids kept apart")
    void wzdx40() {
        ParsedEvents udot = parse("udot-4.0.json");
        assertThat(udot.records()).hasSize(20);
        assertThat(udot.skipped()).isEmpty();
        assertThat(udot.ids()).contains("2365_eastbound");
        assertThat(udot.ids().stream().filter(id -> id.contains("~"))).hasSize(2);

        ParsedEvents nddot = parse("nddot-4.0.json");
        assertThat(nddot.records()).hasSize(15);
        assertThat(nddot.records().stream().filter(r -> "moderate".equals(r.payload().get("severity")))).hasSize(6);
        assertThat(nddot.records().stream().filter(r -> "minor".equals(r.payload().get("severity")))).hasSize(9);
        assertThat(nddot.records()).allSatisfy(r -> assertThat(r.payload().toString())
                .doesNotContain("dotgisadmins@nd.gov"));
        FieldSpecContract.assertPayloadKeysDeclared(descriptor(), udot.records());
        FieldSpecContract.assertPayloadKeysDeclared(descriptor(), nddot.records());
    }

    @Test
    @DisplayName("3.1 (Québec) and CWZ 1.0 are refused with the version in the message")
    void unsupportedVersions() {
        assertThatThrownBy(() -> parse("quebec-3.1.json")).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("unsupported WZDx version 3.1");
        assertThatThrownBy(() -> parse("cwz-1.0.json")).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("unsupported WZDx version 1.0");
        assertThatThrownBy(() -> WzdxParser.parse("{\"type\": \"FeatureCollection\", \"features\": []}"
                .getBytes(StandardCharsets.UTF_8), NOW)).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("unsupported WZDx version (none)");
        assertThatThrownBy(() -> WzdxParser.parse("{\"type\": \"Feature\"}".getBytes(StandardCharsets.UTF_8), NOW))
                .isInstanceOf(UpstreamFormatException.class).hasMessageContaining("FeatureCollection");
    }

    @Test
    @DisplayName("edge cases: detour closure, restriction with speed, completed ended, skips, exact repeat, unknowns")
    void edgeCases() {
        ParsedEvents parsed = parse("edge-4.2.json");
        assertThat(parsed.endedIds()).containsExactly("done-1");
        assertThat(parsed.skipped()).containsOnly(Map.entry(ParsedEvents.MISSING_OBSERVED_AT, 1),
                Map.entry(ParsedEvents.MISSING_ID, 1), Map.entry(ParsedEvents.DUPLICATE, 1));
        assertThat(parsed.records()).hasSize(4);
        NormalizedRecord detour = parsed.records().get(0);
        assertThat(detour.externalId()).isEqualTo("detour-1");
        assertThat(detour.geometry().getGeometryType()).isEqualTo("MultiPoint");
        assertThat(detour.validUntil()).isNull();
        assertThat(detour.payload()).containsEntry("category", "closure").containsEntry("isClosure", true)
                .containsEntry("severity", "major").containsEntry("roads", List.of("US 2"))
                .containsEntry("direction", "both").containsEntry("headline", "Bridge closed.")
                .doesNotContainKey("speedLimitKph");
        NormalizedRecord restriction = parsed.records().get(1);
        assertThat(restriction.externalId()).as("a numeric id").isEqualTo("42");
        assertThat(restriction.payload()).containsEntry("category", "restriction")
                .containsEntry("headline", "I-94 height restriction").containsEntry("speedLimitKph", 56.3)
                .containsEntry("direction", "unknown").containsEntry("status", "planned");
        NormalizedRecord twin = parsed.records().get(2);
        assertThat(twin.externalId()).startsWith("twin~").hasSize("twin~".length() + 8);
        assertThat(twin.payload()).containsEntry("headline", "Work zone on SR 4").doesNotContainKey("speedLimitKph");
        NormalizedRecord mystery = parsed.records().get(3);
        assertThat(mystery.payload()).containsEntry("category", "other").containsEntry("severity", "unknown")
                .containsEntry("status", "planned");
        assertThat(parsed.records()).allSatisfy(r -> assertThat(r.payload().toString())
                .doesNotContain("jane@example.invalid").doesNotContain("Jane Example"));
    }

    @ParameterizedTest
    @CsvSource({"work-zone, roadwork", "detour, closure", "restriction, restriction", "other-thing, other"})
    @DisplayName("event_type → category")
    void categories(String type, String category) {
        assertThat(single(type, "unknown").payload()).containsEntry("category", category);
    }

    @ParameterizedTest
    @CsvSource({
        "all-lanes-closed, major, true",
        "some-lanes-closed, moderate, false",
        "alternating-one-way, moderate, false",
        "all-lanes-open, minor, false",
        "unknown, unknown, false",
        "never-seen, unknown, false",
    })
    @DisplayName("vehicle_impact → severity and isClosure")
    void impacts(String impact, String severity, boolean closure) {
        assertThat(single("work-zone", impact).payload()).containsEntry("severity", severity)
                .containsEntry("isClosure", closure).containsEntry("lanesAffected", impact);
    }

    @Test
    @DisplayName("two parses of the 4.2 fixture share each event's static hash (adapter rule 7)")
    void stableHash() {
        List<NormalizedRecord> first = parse("wsdot-4.2.json").records();
        List<NormalizedRecord> second = parse("wsdot-4.2.json").records();
        for (NormalizedRecord a : first) {
            NormalizedRecord b = second.stream().filter(r -> r.externalId().equals(a.externalId())).findFirst()
                    .orElseThrow();
            SnapshotContract.assertStableStaticHash(descriptor(), List.of(a, b));
        }
    }

    static NormalizedRecord single(String type, String impact) {
        String json = """
                {"feed_info": {"version": "4.1"}, "type": "FeatureCollection", "features": [{"id": "t1",
                 "type": "Feature", "properties": {"core_details": {"event_type": "%s", "data_source_id": "t",
                 "road_names": ["I-5"], "direction": "northbound", "update_date": "2026-10-13T10:00:00Z"},
                 "start_date": "2026-10-01T00:00:00Z", "vehicle_impact": "%s"},
                 "geometry": {"type": "LineString", "coordinates": [[-120, 45], [-120.1, 45.1]]}}]}"""
                .formatted(type, impact);
        List<NormalizedRecord> records = WzdxParser.parse(json.getBytes(StandardCharsets.UTF_8), NOW).records();
        assertThat(records).hasSize(1);
        return records.getFirst();
    }
}
