package ai.foresignal.source.adapters.roads;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * The shared {@code road-event} payload (prompt 074): truncation, closure flags, null omission, the closed
 * vocabularies and the text helpers.
 */
class RoadEventPayloadTest {

    @Test
    @DisplayName("nulls are omitted: a bare payload holds only its format")
    void nullOmission() {
        Map<String, Object> map = new RoadEventPayload("wzdx").category(null).severity(null).headline("  ")
                .description(null).road(null).road(" ").direction(null).status(null).lanesAffected("")
                .speedLimitKph(null).url(null).language(null).toMap();
        assertThat(map).containsOnlyKeys("format").containsEntry("format", "wzdx");
    }

    @Test
    @DisplayName("every key in order, times as ISO strings, roads de-duplicated")
    void fullPayload() {
        Map<String, Object> map = new RoadEventPayload("open511").category("closure").subtype("ROAD_CONSTRUCTION")
                .severity("major").headline("Highway 1 closed").description("Closed\nfor  works.")
                .road("Highway 1").road("Highway 1").road("Main St").direction("both").status("active")
                .isClosure(true).lanesAffected("all-lanes-closed").speedLimitKph(50.0)
                .startsAt(Instant.parse("2026-10-13T10:00:00Z")).endsAt(Instant.parse("2026-10-14T10:00:00Z"))
                .url("https://api.open511.gov.bc.ca/events/x").language("EN").toMap();
        assertThat(map.keySet()).containsExactly("category", "subtype", "severity", "headline", "description",
                "roads", "direction", "status", "isClosure", "lanesAffected", "speedLimitKph", "startsAt", "endsAt",
                "format", "url", "language");
        assertThat(map).containsEntry("subtype", "road-construction").containsEntry("description", "Closed for works.")
                .containsEntry("roads", List.of("Highway 1", "Main St")).containsEntry("isClosure", true)
                .containsEntry("startsAt", "2026-10-13T10:00:00Z").containsEntry("language", "en");
    }

    @Test
    @DisplayName("isClosure false is kept (it is information), a closure is true")
    void closureFlag() {
        assertThat(new RoadEventPayload("datex3").isClosure(false).toMap()).containsEntry("isClosure", false);
        assertThat(new RoadEventPayload("datex3").isClosure(true).toMap()).containsEntry("isClosure", true);
    }

    @Test
    @DisplayName("headline cut to 200 characters, description to 4 KB on a character boundary, roads to 10")
    void truncation() {
        String longText = "é".repeat(5000);
        Map<String, Object> map = new RoadEventPayload("digitraffic").headline("x".repeat(500))
                .description(longText).roads(java.util.stream.IntStream.range(0, 30).mapToObj(i -> "Road " + i)
                        .toList()).toMap();
        assertThat((String) map.get("headline")).hasSize(RoadEventPayload.MAX_HEADLINE);
        String description = (String) map.get("description");
        assertThat(description.getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(4096);
        assertThat(description).hasSize(2048).matches("é+");
        assertThat((List<?>) map.get("roads")).hasSize(RoadEventPayload.MAX_ROADS);
        assertThat(RoadEventPayload.truncateChars("ab😀cd", 3)).isEqualTo("ab");
        assertThat(RoadEventPayload.truncateUtf8("a😀", 4)).isEqualTo("a");
    }

    @Test
    @DisplayName("speed limits that are missing, zero, negative or not finite are omitted")
    void speedSentinels() {
        for (Double bad : new Double[] {null, 0.0, -5.0, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertThat(new RoadEventPayload("wzdx").speedLimitKph(bad).toMap()).doesNotContainKey("speedLimitKph");
        }
    }

    @Test
    @DisplayName("only absolute http(s) URLs without user info are kept")
    void urls() {
        assertThat(new RoadEventPayload("open511").url("https://example.org/e/1").toMap()).containsKey("url");
        for (String bad : new String[] {"javascript:alert(1)", "/events/1", "https://user@example.org/x",
                "ftp://example.org/x"}) {
            assertThat(new RoadEventPayload("open511").url(bad).toMap()).as(bad).doesNotContainKey("url");
        }
    }

    @Test
    @DisplayName("status from the schedule: planned before the start, active otherwise")
    void statusFromSchedule() {
        Instant now = Instant.parse("2026-10-13T12:00:00Z");
        assertThat(new RoadEventPayload("wzdx").statusFrom(now.plusSeconds(60), now).toMap())
                .containsEntry("status", "planned");
        assertThat(new RoadEventPayload("wzdx").statusFrom(now.minusSeconds(60), now).toMap())
                .containsEntry("status", "active");
        assertThat(new RoadEventPayload("wzdx").statusFrom(null, now).toMap()).containsEntry("status", "active");
    }

    @Test
    @DisplayName("values outside the closed vocabularies are refused")
    void vocabularies() {
        assertThatThrownBy(() -> new RoadEventPayload("geojson")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RoadEventPayload("wzdx").category("accident"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RoadEventPayload("wzdx").severity("critical"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RoadEventPayload("wzdx").direction("north"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RoadEventPayload("wzdx").status("ended"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({
        "ROAD_CONSTRUCTION, road-construction",
        "carriagewayClosures, carriageway-closures",
        "accident report, accident-report",
        "work-zone, work-zone",
        "'  ', ",
    })
    @DisplayName("upstream codes become lower-kebab case")
    void kebab(String code, String expected) {
        assertThat(RoadEventPayload.kebab(code)).isEqualTo(expected);
    }

    @Test
    @DisplayName("text helpers: words, first sentence, join without repeats")
    void helpers() {
        assertThat(RoadEventPayload.words("carriagewayClosures")).isEqualTo("Carriageway closures");
        assertThat(RoadEventPayload.words("ROAD_WORK")).isEqualTo("Road work");
        assertThat(RoadEventPayload.firstSentence("One thing. Two things.")).isEqualTo("One thing.");
        assertThat(RoadEventPayload.firstSentence("No end")).isEqualTo("No end");
        assertThat(RoadEventPayload.join(List.of("A road.", "", "A road.", "More."))).isEqualTo("A road. More.");
        assertThat(RoadEventPayload.join(List.of("", " "))).isNull();
    }
}
