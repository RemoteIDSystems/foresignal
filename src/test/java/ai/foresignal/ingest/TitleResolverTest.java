package ai.foresignal.ingest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** One title resolver for heads, tiles, MCP and Ask. */
class TitleResolverTest {

    @Test
    @DisplayName("titleField wins when present")
    void titleFieldWins() {
        assertThat(TitleResolver.resolve("headline", Map.of("headline", "Quake", "title", "Other"))).isEqualTo("Quake");
    }

    @Test
    @DisplayName("falls back to title > name > callsign > place")
    void fallbackOrder() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("place", "10 km N of X");
        assertThat(TitleResolver.resolve((String) null, payload)).isEqualTo("10 km N of X");
        payload.put("callsign", "UAL1");
        assertThat(TitleResolver.resolve((String) null, payload)).isEqualTo("UAL1");
        payload.put("name", "Plant");
        assertThat(TitleResolver.resolve((String) null, payload)).isEqualTo("Plant");
        payload.put("title", "Title");
        assertThat(TitleResolver.resolve((String) null, payload)).isEqualTo("Title");
    }

    @Test
    @DisplayName("an absent or blank titleField falls back")
    void blankTitleFieldFallsBack() {
        assertThat(TitleResolver.resolve("headline", Map.of("headline", "   ", "name", "N"))).isEqualTo("N");
        assertThat(TitleResolver.resolve("headline", Map.of("name", "N"))).isEqualTo("N");
    }

    @Test
    @DisplayName("values are stringified and trimmed; non-scalars are ignored")
    void stringifies() {
        assertThat(TitleResolver.resolve("mag", Map.of("mag", 4.0))).isEqualTo("4");
        assertThat(TitleResolver.resolve("flag", Map.of("flag", true))).isEqualTo("true");
        assertThat(TitleResolver.resolve((String) null, Map.of("name", "  Padded  "))).isEqualTo("Padded");
        assertThat(TitleResolver.resolve("obj", Map.of("obj", Map.of("a", 1), "place", "P"))).isEqualTo("P");
    }

    @Test
    @DisplayName("titles are cut to 256 characters")
    void truncates() {
        assertThat(TitleResolver.resolve((String) null, Map.of("title", "t".repeat(300)))).hasSize(256);
    }

    @Test
    @DisplayName("null when nothing yields a title")
    void nullWhenAbsent() {
        assertThat(TitleResolver.resolve((String) null, Map.of("other", "x"))).isNull();
        assertThat(TitleResolver.resolve((String) null, Map.of())).isNull();
        assertThat(TitleResolver.resolve((String) null, null)).isNull();
        assertThat(TitleResolver.resolve((SourceIngestProfile) null, Map.of("name", "N"))).isEqualTo("N");
    }

    @Test
    @DisplayName("the profile overload reads the descriptor's titleField")
    void profileOverload() {
        SourceIngestProfile profile =
                SourceIngestProfile.forDescriptor(IngestFixtures.eventsDescriptor().titleField("headline").build());
        assertThat(TitleResolver.resolve(profile, Map.of("headline", "H", "title", "T"))).isEqualTo("H");
    }
}
