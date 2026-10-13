package ai.foresignal.source.adapters.roads;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.kit.feature.FeatureGeometry;
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
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * {@link Open511Parser} on DriveBC's real {@code /events} page (captured 2026-10-13) and hand-made edge cases.
 */
class Open511ParserTest {

    static final Instant NOW = Instant.parse("2026-10-13T19:00:00Z");

    static ParsedEvents fixture() {
        return Open511Parser.parse(AdapterFixtures.bytes("open511-drivebc", "events.json"), "en", NOW);
    }

    static SourceDescriptor descriptor() {
        return new Open511Adapter(Open511Server.REVIEWED.getFirst(), null).descriptor();
    }

    static Map<Object, Long> count(List<NormalizedRecord> records, String key) {
        return records.stream().collect(Collectors.groupingBy(r -> String.valueOf(r.payload().get(key)),
                Collectors.counting()));
    }

    @Test
    @DisplayName("the DriveBC fixture: 25 events, categories, severities, closures, schedules and geometry")
    void driveBc() {
        ParsedEvents parsed = fixture();
        List<NormalizedRecord> records = parsed.records();
        assertThat(records).hasSize(25);
        assertThat(parsed.endedIds()).isEmpty();
        assertThat(parsed.skipped()).isEmpty();
        assertThat(parsed.nextUrl()).isNull();
        assertThat(count(records, "category")).containsOnly(Map.entry("incident", 7L), Map.entry("roadwork", 15L),
                Map.entry("weather", 3L));
        assertThat(count(records, "severity")).containsOnly(Map.entry("major", 5L), Map.entry("minor", 20L));
        assertThat(count(records, "status")).containsOnly(Map.entry("active", 22L), Map.entry("planned", 3L));
        assertThat(records.stream().filter(r -> Boolean.TRUE.equals(r.payload().get("isClosure")))).hasSize(5);
        assertThat(records.stream().filter(r -> r.validFrom() != null)).hasSize(25);
        assertThat(records.stream().filter(r -> r.validUntil() != null)).hasSize(13);
        assertThat(records).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo("road-event");
            assertThat(r.eventTime()).isNull();
            assertThat(r.geometry()).isNotNull();
            assertThat(r.geometry().getSRID()).isEqualTo(4326);
            assertThat(r.payload()).containsEntry("format", "open511").containsEntry("language", "en")
                    .doesNotContainKeys("+ivr_message", "ivr_message", "+linear_reference_km");
        });
        FieldSpecContract.assertPayloadKeysDeclared(descriptor(), records);
    }

    @Test
    @DisplayName("one event in full: id, updated as observedAt, the first sentence as headline, the open interval")
    void oneEvent() {
        NormalizedRecord r = fixture().records().getFirst();
        assertThat(r.externalId()).isEqualTo("drivebc.ca/RIDE-102765");
        assertThat(r.observedAt()).isEqualTo(Instant.parse("2026-09-24T18:02:28Z"));
        assertThat(r.validFrom()).isEqualTo(Instant.parse("2026-09-24T18:02:00Z"));
        assertThat(r.validUntil()).isNull();
        assertThat(r.payload()).containsEntry("headline",
                        "Mowing from Highway 5, At Avola Overhead Bridge to At Blue River Bridge.")
                .containsEntry("subtype", "road-construction").containsEntry("roads", List.of("Highway 5"))
                .containsEntry("direction", "both").containsEntry("lanesAffected", "all-lanes-open")
                .containsEntry("isClosure", false).containsEntry("startsAt", "2026-09-24T18:02:00Z")
                .containsEntry("url", "https://api.open511.gov.bc.ca/events/drivebc.ca/RIDE-102765")
                .doesNotContainKey("endsAt");
        assertThat(r.geometry().getGeometryType()).isEqualTo("LineString");
    }

    @Test
    @DisplayName("edge cases: ARCHIVED ended, missing id/updated and bad geometry skipped, intervals, next_url")
    void edgeCases() {
        ParsedEvents parsed = Open511Parser.parse(AdapterFixtures.bytes("open511-drivebc", "edge-cases.json"), "en",
                NOW);
        assertThat(parsed.endedIds()).containsExactly("drivebc.ca/EDGE-ARCHIVED");
        assertThat(parsed.skipped()).containsOnly(Map.entry(ParsedEvents.MISSING_ID, 1),
                Map.entry(ParsedEvents.MISSING_OBSERVED_AT, 1), Map.entry(ParsedEvents.BAD_GEOMETRY, 1));
        assertThat(parsed.nextUrl()).isEqualTo("/events?format=json&status=ACTIVE&limit=500&offset=500");
        assertThat(parsed.ids()).containsExactly("drivebc.ca/EDGE-INTERVALS", "drivebc.ca/EDGE-OPEN-END",
                "drivebc.ca/EDGE-UNKNOWN-TYPE");

        NormalizedRecord intervals = parsed.records().get(0);
        assertThat(intervals.validFrom()).as("the earliest start").isEqualTo(Instant.parse("2026-10-19T03:00:00Z"));
        assertThat(intervals.validUntil()).as("the end of the last interval")
                .isEqualTo(Instant.parse("2026-10-21T11:00:00Z"));
        assertThat(intervals.payload()).containsEntry("category", "event").containsEntry("subtype", "parade")
                .containsEntry("severity", "moderate").containsEntry("headline", "Highway 99 night closures")
                .containsEntry("description", "Night closures for a parade. Use Highway 1.")
                .containsEntry("roads", List.of("Highway 99", "Main Street")).containsEntry("direction", "northbound")
                .containsEntry("lanesAffected", "some-lanes-closed").containsEntry("isClosure", true)
                .containsEntry("status", "planned");
        assertThat(intervals.geometry().getGeometryType()).isEqualTo("MultiLineString");

        NormalizedRecord openEnd = parsed.records().get(1);
        assertThat(openEnd.validUntil()).as("the last interval is open-ended").isNull();
        assertThat(openEnd.payload()).containsEntry("category", "weather").containsEntry("severity", "unknown")
                .containsEntry("headline", "Weather condition").doesNotContainKey("endsAt");

        NormalizedRecord unknown = parsed.records().get(2);
        assertThat(unknown.geometry()).isNull();
        assertThat(unknown.payload()).containsEntry("category", "other").containsEntry("severity", "unknown");
    }

    @ParameterizedTest
    @CsvSource({
        "CONSTRUCTION, roadwork",
        "INCIDENT, incident",
        "SPECIAL_EVENT, event",
        "WEATHER_CONDITION, weather",
        "ROAD_CONDITION, weather",
        "SOMETHING_ELSE, other",
    })
    @DisplayName("event_type → category")
    void categories(String type, String category) {
        assertThat(single("\"event_type\": \"" + type + "\"").payload()).containsEntry("category", category);
    }

    @ParameterizedTest
    @CsvSource({"MINOR, minor", "MODERATE, moderate", "MAJOR, major", "UNKNOWN, unknown", "OTHER, unknown"})
    @DisplayName("severity → severity")
    void severities(String upstream, String severity) {
        assertThat(single("\"event_type\": \"INCIDENT\", \"severity\": \"" + upstream + "\"").payload())
                .containsEntry("severity", severity);
    }

    @Test
    @DisplayName("a geometry over 9,500 vertices is simplified and flagged")
    void simplification() {
        StringBuilder coords = new StringBuilder();
        for (int i = 0; i < 20_000; i++) {
            if (i > 0) {
                coords.append(',');
            }
            coords.append('[').append(-123 + i * 0.00001).append(',').append(49 + (i % 2) * 0.00001).append(']');
        }
        NormalizedRecord r = single("\"event_type\": \"CONSTRUCTION\", \"geography\": {\"type\": \"LineString\","
                + " \"coordinates\": [" + coords + "]}");
        assertThat(r.geometry().getNumPoints()).isLessThanOrEqualTo(FeatureGeometry.MAX_VERTICES);
        assertThat(r.payload()).containsEntry(FeatureGeometry.SIMPLIFIED_KEY, true)
                .containsKey(FeatureGeometry.TOLERANCE_KEY);
        FieldSpecContract.assertPayloadKeysDeclared(descriptor(), List.of(r));
    }

    @Test
    @DisplayName("two parses of the fixture share each event's static hash (adapter rule 7)")
    void stableHash() {
        List<NormalizedRecord> first = fixture().records();
        List<NormalizedRecord> second = fixture().records();
        for (NormalizedRecord a : first) {
            NormalizedRecord b = second.stream().filter(r -> r.externalId().equals(a.externalId())).findFirst()
                    .orElseThrow();
            SnapshotContract.assertStableStaticHash(descriptor(), List.of(a, b));
        }
    }

    @Test
    @DisplayName("a document without an events array, or not JSON, is a format error")
    void formatErrors() {
        assertThatThrownBy(() -> Open511Parser.parse("{\"pagination\": {}}".getBytes(StandardCharsets.UTF_8), "en",
                NOW)).isInstanceOf(UpstreamFormatException.class).hasMessageContaining("events");
        assertThatThrownBy(() -> Open511Parser.parse("<html/>".getBytes(StandardCharsets.UTF_8), "en", NOW))
                .isInstanceOf(UpstreamFormatException.class);
    }

    /** Parses one hand-made ACTIVE event with {@code members} and returns its record. */
    static NormalizedRecord single(String members) {
        String geography = members.contains("\"geography\"") ? ""
                : ", \"geography\": {\"type\": \"Point\", \"coordinates\": [-123.1, 49.2]}";
        String json = "{\"events\": [{\"id\": \"drivebc.ca/T-1\", \"status\": \"ACTIVE\", \"updated\":"
                + " \"2026-10-13T10:00:00-07:00\", " + members + geography + "}], \"pagination\": {\"offset\": \"0\"}}";
        List<NormalizedRecord> records = Open511Parser.parse(json.getBytes(StandardCharsets.UTF_8), "en", NOW)
                .records();
        assertThat(records).hasSize(1);
        return records.getFirst();
    }
}
