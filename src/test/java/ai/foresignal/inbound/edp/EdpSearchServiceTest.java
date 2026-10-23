package ai.foresignal.inbound.edp;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link EdpSearchService#searchUri} and {@link EdpSearchService#cueResult}. */
class EdpSearchServiceTest {

    static final Instant T = Instant.parse("2026-10-11T18:00:00Z");

    @Test
    @DisplayName("the search URI encodes the query and leaves out what is null")
    void searchUri() {
        assertThat(EdpSearchService.searchUri("https://p.example/search", new EdpSearchService.Query("flood & gauge",
                null), null, null, null, 100).toString())
                .isEqualTo("https://p.example/search?q=flood+%26+gauge&limit=100");
        assertThat(EdpSearchService.searchUri("https://p.example/search?key=1", new EdpSearchService.Query("x", "de"),
                T.minusSeconds(60), T, List.of(1.0, 2.0, 3.0, 4.0), 5).toString())
                .isEqualTo("https://p.example/search?key=1&q=x&since=2026-10-11T17%3A59%3A00Z"
                        + "&until=2026-10-11T18%3A00%3A00Z&bbox=1.0%2C2.0%2C3.0%2C4.0&lang=de&limit=5");
    }

    @Test
    @DisplayName("a cue result keeps the payload and adds collection = cue and the cue id; a tombstone is unchanged")
    void cueResult() {
        NormalizedRecord record = new NormalizedRecord("levels:g1", "observation", T, T, null, null, null,
                Map.of("value", 1.5), false, null, null);
        NormalizedRecord marked = EdpSearchService.cueResult(record, "CUE1");
        assertThat(marked.payload()).containsEntry("value", 1.5).containsEntry("collection", "cue")
                .containsEntry("cueId", "CUE1");
        assertThat(marked.externalId()).isEqualTo("levels:g1");

        NormalizedRecord tombstone = new NormalizedRecord("levels:g2", "observation", T, T, null, null, null,
                Map.of(), true, null, null);
        assertThat(EdpSearchService.cueResult(tombstone, "CUE1")).isSameAs(tombstone);
    }

    @Test
    @DisplayName("a result maps to the SocialCue.externalResults item")
    void resultMap() {
        Map<String, Object> item = new EdpSearchService.Result("ext-a", 2, 5, null).toMap();
        assertThat(item.keySet()).containsExactly("sourceId", "queries", "items", "error");
        assertThat(item).containsEntry("sourceId", "ext-a").containsEntry("queries", 2).containsEntry("items", 5);
        assertThat(item.get("error")).isNull();
    }
}
