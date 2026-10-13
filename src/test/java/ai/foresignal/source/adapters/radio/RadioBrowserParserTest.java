package ai.foresignal.source.adapters.radio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.MappedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@link RadioBrowserParser} on a real {@code /json/stations/search} page (captured 2026-10-13, 25 rows) and
 * hand-made edge cases: the excluded counters never reach a payload, the null island leaves no geometry, and names
 * lose their tabs.
 */
class RadioBrowserParserTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    static SourceDescriptor descriptor() {
        return new RadioBrowserAdapter("", new Random(1), null).descriptor();
    }

    @Test
    @DisplayName("the fixture: 25 stations with points, cleaned names, links, tags and the online flag")
    void fixture() {
        RadioBrowserParser.Page page = RadioBrowserParser.parse(AdapterFixtures.bytes("radio-browser",
                "stations-page.json"));
        assertThat(page.rows()).isEqualTo(25);
        assertThat(page.records()).hasSize(25);
        assertThat(page.skipped()).isEmpty();
        assertThat(page.records()).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo("radio-station");
            assertThat(r.geometry()).isNotNull();
            assertThat(r.observedAt()).isNotNull();
            assertThat(r.payload().keySet()).doesNotContainAnyElementsOf(RadioBrowserParser.EXCLUDED)
                    .isSubsetOf("name", "streamUrl", "homepage", "favicon", "tags", "countryCode", "state",
                            "languages", "codec", "bitrate", "hls", "online");
            String name = (String) r.payload().get("name");
            assertThat(name).doesNotContain("\t").isEqualTo(name.strip());
        });
        NormalizedRecord first = page.records().getFirst();
        assertThat(first.externalId()).isEqualTo("db93a00f-9191-46ab-9e87-ec9b373b3eee");
        assertThat(first.observedAt()).isEqualTo(Instant.parse("2026-01-14T22:54:45Z"));
        assertThat(first.payload()).containsEntry("name", "Arrow Classic Rock")
                .containsEntry("streamUrl", "http://stream.gal.io/arrow").containsEntry("homepage",
                        "https://www.arrow.nl/").containsEntry("countryCode", "NL").containsEntry("codec", "MP3")
                .containsEntry("bitrate", 192L).containsEntry("hls", false).containsEntry("online", true)
                .doesNotContainKeys("tags", "languages", "state");
        assertThat(first.geometry().getCoordinate().y).isEqualTo(52.07963259545092);
        assertThat(page.records().stream().filter(r -> r.payload().containsKey("tags"))).hasSize(21);
        FieldSpecContract.assertPayloadKeysDeclared(descriptor(), page.records());
    }

    @Test
    @DisplayName("edge cases: tab name, null island, unsafe links, tag cap, bad id, no change time, range, repeat")
    void edgeCases() {
        RadioBrowserParser.Page page = RadioBrowserParser.parse(AdapterFixtures.bytes("radio-browser",
                "edge-cases.json"));
        assertThat(page.rows()).isEqualTo(5);
        assertThat(page.skipped()).containsOnly(Map.entry(MappedRecord.MISSING_ID, 1),
                Map.entry(MappedRecord.MISSING_OBSERVED_AT, 1), Map.entry(MappedRecord.DUPLICATE_ID, 1));
        assertThat(page.records()).hasSize(2);
        NormalizedRecord tab = page.records().getFirst();
        assertThat(tab.externalId()).isEqualTo("11111111-2222-4333-8444-555555555555");
        assertThat(tab.geometry()).as("the null island").isNull();
        assertThat(tab.payload()).containsEntry("name", "Tab Prefixed Station")
                .containsEntry("streamUrl", "http://stream.example.org/live.mp3").containsEntry("countryCode", "ZZ")
                .containsEntry("languages", List.of("english", "spanish")).containsEntry("hls", true)
                .containsEntry("online", false).doesNotContainKeys("homepage", "favicon", "state", "bitrate")
                .doesNotContainKeys(RadioBrowserParser.EXCLUDED.toArray(String[]::new));
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) tab.payload().get("tags");
        assertThat(tags).hasSize(RadioBrowserParser.MAX_TAGS).startsWith("news", "talk", "weather", "a")
                .doesNotHaveDuplicates();
        assertThat(page.records().get(1).geometry()).as("latitude 95.5").isNull();
        FieldSpecContract.assertPayloadKeysDeclared(descriptor(), page.records());
    }

    @Test
    @DisplayName("text cleaning, link filtering and a page that is not an array")
    void helpers() {
        assertThat(RadioBrowserParser.clean("\u0007 Radio\u0000One \n", 200)).isEqualTo("Radio One");
        assertThat(RadioBrowserParser.clean("x".repeat(300), 200)).hasSize(200);
        assertThat(RadioBrowserParser.clean(" \t ", 200)).isNull();
        assertThat(RadioBrowserParser.httpUrl("ftp://example.org/a")).isNull();
        assertThat(RadioBrowserParser.httpUrl("https://example.org/a b")).isNull();
        assertThat(RadioBrowserParser.httpUrl("https://example.org/a")).isEqualTo("https://example.org/a");
        assertThatThrownBy(() -> RadioBrowserParser.parse("{\"error\": 1}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> RadioBrowserParser.parse("<html>".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("snapshot contract: two parses, and a flipped online flag alone (volatile), keep one static hash")
    void snapshot() {
        JsonNode rows = JSON.readTree(AdapterFixtures.bytes("radio-browser", "stations-page.json"));
        JsonNode row = rows.get(0);
        ObjectNode offline = (ObjectNode) row.deepCopy();
        offline.put("lastcheckok", 0);
        offline.put("clickcount", 99_999);
        SnapshotContract.assertStableStaticHash(descriptor(), List.of(RadioBrowserParser.station(row).record(),
                RadioBrowserParser.station(row).record(), RadioBrowserParser.station(offline).record()));
    }
}
