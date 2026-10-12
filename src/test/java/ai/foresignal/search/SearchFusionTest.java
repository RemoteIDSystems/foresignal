package ai.foresignal.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link SearchService}'s reciprocal rank fusion and titles (prompt 027). */
class SearchFusionTest {

    static SearchService.Hit hit(String id, String text) {
        return new SearchService.Hit("record", "s:" + id, "s", "k", 1L, null, null, text, null, null, 0, null);
    }

    @Test
    @DisplayName("RRF with k = 60: a hit in both lists outranks hits in one; scores are 1/(k + rank) summed")
    void fuse() {
        List<SearchService.Hit> text = List.of(hit("a", "x"), hit("b", "x"));
        List<SearchService.Hit> semantic = List.of(hit("c", "x"), hit("b", "x"));
        List<SearchService.Hit> fused = SearchService.fuse(text, semantic);
        assertThat(fused).extracting(h -> h.docId).containsExactly("s:b", "s:a", "s:c");
        assertThat(fused.getFirst().score).isEqualTo(1.0 / 62 + 1.0 / 62);
        assertThat(fused.get(1).score).isEqualTo(1.0 / 61);
    }

    @Test
    @DisplayName("titles come from the record, else the text after kind and source, else the doc id")
    void titles() {
        assertThat(SearchService.title(hit("a", "news | Wire | Flooding | Levees"), null)).isEqualTo("Flooding | Levees");
        assertThat(SearchService.title(new SearchService.Hit("briefing", "run-1", null, null, null, null, null,
                "Morning briefing\nDetails", null, null, 0, null), null)).isEqualTo("Morning briefing");
        assertThat(SearchService.title(hit("a", null), null)).isEqualTo("s:a");
        assertThat(SearchService.title(new SearchService.Hit("record", "s:a", "s", "k", 1L, null, null, "x", null,
                "Head title", 0, null), null)).isEqualTo("Head title");
    }
}
