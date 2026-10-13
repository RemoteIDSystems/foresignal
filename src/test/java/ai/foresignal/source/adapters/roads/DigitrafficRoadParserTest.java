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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * {@link DigitrafficRoadParser} on Fintraffic's real v2 traffic announcements, road works and (empty) weight
 * restrictions captured 2026-10-13, and hand-made edge cases. Contact data never reaches a payload.
 */
class DigitrafficRoadParserTest {

    static final Instant NOW = Instant.parse("2026-10-13T19:00:00Z");

    static ParsedEvents parse(String name) {
        return DigitrafficRoadParser.parse(AdapterFixtures.bytes("digitraffic-road", name), NOW);
    }

    static SourceDescriptor descriptor() {
        return new DigitrafficRoadAdapter("", () -> "9.9.9", null).descriptor();
    }

    static long count(List<NormalizedRecord> records, String key, Object value) {
        return records.stream().filter(r -> value.equals(r.payload().get(key))).count();
    }

    @Test
    @DisplayName("traffic announcements: 8 situations, closures found from the feature names, Finnish text")
    void trafficAnnouncements() {
        ParsedEvents parsed = parse("traffic-announcements.json");
        List<NormalizedRecord> records = parsed.records();
        assertThat(records).hasSize(8);
        assertThat(parsed.endedIds()).isEmpty();
        assertThat(count(records, "category", "closure")).isEqualTo(3);
        assertThat(count(records, "category", "other")).isEqualTo(5);
        assertThat(count(records, "isClosure", true)).isEqualTo(3);
        assertThat(count(records, "severity", "unknown")).isEqualTo(8);
        assertThat(records.stream().filter(r -> r.validUntil() != null)).hasSize(3);
        NormalizedRecord first = records.getFirst();
        assertThat(first.externalId()).isEqualTo("GUID50471971");
        assertThat(first.observedAt()).isEqualTo(Instant.parse("2026-10-07T07:16:37.173Z"));
        assertThat(first.payload()).containsEntry("subtype", "general").containsEntry("language", "fi")
                .containsEntry("headline", "Tie 1821, Turku. Liikennetiedote.")
                .containsEntry("roads", List.of("1821")).containsEntry("direction", "both")
                .containsEntry("endsAt", "2026-11-30T13:00:00Z");
        assertThat(first.geometry().getGeometryType()).isEqualTo("MultiLineString");
        FieldSpecContract.assertPayloadKeysDeclared(descriptor(), records);
    }

    @Test
    @DisplayName("road works: 14 current, 3 closed early (ended), phase severities and lane restrictions")
    void roadWorks() {
        ParsedEvents parsed = parse("roadworks.json");
        List<NormalizedRecord> records = parsed.records();
        assertThat(records).hasSize(14);
        assertThat(parsed.endedIds()).containsExactlyInAnyOrder("GUID50472247", "GUID50472162", "GUID50471503");
        assertThat(count(records, "category", "roadwork")).isEqualTo(14);
        assertThat(count(records, "severity", "major")).isEqualTo(11);
        assertThat(count(records, "severity", "severe")).isEqualTo(2);
        assertThat(count(records, "severity", "minor")).isEqualTo(1);
        assertThat(count(records, "status", "planned")).isEqualTo(5);
        NormalizedRecord first = records.getFirst();
        assertThat(first.externalId()).isEqualTo("GUID50472288");
        assertThat(first.validFrom()).isEqualTo(Instant.parse("2026-10-13T17:30:00Z"));
        assertThat(first.validUntil()).isEqualTo(Instant.parse("2026-10-14T03:00:00Z"));
        assertThat(first.payload()).containsEntry("subtype", "road-work").containsEntry("severity", "severe")
                .containsEntry("lanesAffected", "single-lane-closed").containsEntry("direction", "unknown")
                .containsEntry("roads", List.of("51", "Länsiväylä"));
        FieldSpecContract.assertPayloadKeysDeclared(descriptor(), records);
    }

    @Test
    @DisplayName("an empty weight-restriction document parses to nothing")
    void empty() {
        ParsedEvents parsed = parse("weight-restrictions.json");
        assertThat(parsed.records()).isEmpty();
        assertThat(parsed.skipped()).isEmpty();
    }

    @Test
    @DisplayName("no phone number or e-mail address from contact reaches any payload")
    void contactDropped() {
        List<NormalizedRecord> all = new ArrayList<>();
        for (String name : List.of("traffic-announcements.json", "roadworks.json", "edge-cases.json")) {
            all.addAll(parse(name).records());
        }
        assertThat(all).isNotEmpty().allSatisfy(r -> {
            assertThat(r.payload()).doesNotContainKeys("contact", "phone", "email");
            assertThat(r.payload().toString()).doesNotContain("02002100").doesNotContain("@fintraffic.fi")
                    .doesNotContain("edge.contact@example.invalid").doesNotContain("+358");
        });
    }

    @Test
    @DisplayName("edge cases: accident report, ended and retracted, closure by restriction, units, skips")
    void edgeCases() {
        ParsedEvents parsed = parse("edge-cases.json");
        assertThat(parsed.endedIds()).containsExactly("GUID-EDGE-ENDED", "GUID-EDGE-RETRACTED");
        assertThat(parsed.skipped()).containsOnly(Map.entry(ParsedEvents.MISSING_OBSERVED_AT, 1),
                Map.entry(ParsedEvents.MISSING_ID, 1));
        assertThat(parsed.records()).hasSize(3);
        NormalizedRecord accident = parsed.records().get(0);
        assertThat(accident.payload()).containsEntry("category", "incident").containsEntry("subtype",
                        "accident-report").containsEntry("isClosure", true).containsEntry("speedLimitKph", 40.0)
                .containsEntry("language", "sv").containsEntry("direction", "unknown")
                .containsEntry("description", "Väg 51, Esbo. Kör försiktigt.").doesNotContainKey("endsAt");
        assertThat(accident.validUntil()).isNull();
        NormalizedRecord closedWork = parsed.records().get(1);
        assertThat(closedWork.payload()).containsEntry("isClosure", true).containsEntry("severity", "severe")
                .containsEntry("lanesAffected", "lane-closed").containsEntry("status", "planned");
        NormalizedRecord weight = parsed.records().get(2);
        assertThat(weight.geometry()).isNull();
        assertThat(weight.payload()).containsEntry("category", "restriction").containsEntry("language", "fi")
                .doesNotContainKey("speedLimitKph");
    }

    @ParameterizedTest
    @CsvSource({
        "ROAD_WORK, , false, roadwork",
        "WEIGHT_RESTRICTION, , false, restriction",
        "EXEMPTED_TRANSPORT, , false, restriction",
        "TRAFFIC_ANNOUNCEMENT, ACCIDENT_REPORT, false, incident",
        "TRAFFIC_ANNOUNCEMENT, PRELIMINARY_ACCIDENT_REPORT, true, incident",
        "TRAFFIC_ANNOUNCEMENT, GENERAL, true, closure",
        "TRAFFIC_ANNOUNCEMENT, GENERAL, false, other",
        "TRAFFIC_ANNOUNCEMENT, UNCONFIRMED_OBSERVATION, false, other",
        "SOMETHING_NEW, , false, other",
    })
    @DisplayName("situation and announcement type → category")
    void categories(String situationType, String announcementType, boolean closure, String category) {
        assertThat(DigitrafficRoadParser.category(situationType, announcementType, closure)).isEqualTo(category);
    }

    @ParameterizedTest
    @CsvSource({"low, minor", "high, major", "highest, severe"})
    @DisplayName("road-work phase severity → severity")
    void severities(String upstream, String severity) {
        assertThat(DigitrafficRoadParser.SEVERITY.get(DigitrafficRoadParser.norm(upstream))).isEqualTo(severity);
    }

    @Test
    @DisplayName("lower-case v2 enumerations and upper-case v1 ones normalize alike")
    void enumerations() {
        assertThat(DigitrafficRoadParser.norm("traffic announcement")).isEqualTo("TRAFFIC_ANNOUNCEMENT");
        assertThat(DigitrafficRoadParser.norm("ROAD_WORK")).isEqualTo("ROAD_WORK");
        assertThat(DigitrafficRoadParser.norm("preliminary accident report"))
                .isEqualTo("PRELIMINARY_ACCIDENT_REPORT");
        assertThat(DigitrafficRoadParser.norm(" ")).isNull();
    }

    @Test
    @DisplayName("two parses share each situation's static hash (adapter rule 7)")
    void stableHash() {
        List<NormalizedRecord> first = parse("roadworks.json").records();
        List<NormalizedRecord> second = parse("roadworks.json").records();
        for (NormalizedRecord a : first) {
            NormalizedRecord b = second.stream().filter(r -> r.externalId().equals(a.externalId())).findFirst()
                    .orElseThrow();
            SnapshotContract.assertStableStaticHash(descriptor(), List.of(a, b));
        }
    }

    @Test
    @DisplayName("a document that is not a FeatureCollection is a format error")
    void formatErrors() {
        assertThatThrownBy(() -> DigitrafficRoadParser.parse("{\"type\": \"Feature\"}".getBytes(
                StandardCharsets.UTF_8), NOW)).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> DigitrafficRoadParser.parse("not json".getBytes(StandardCharsets.UTF_8), NOW))
                .isInstanceOf(UpstreamFormatException.class);
    }
}
