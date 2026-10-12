package ai.foresignal.search;

import static ai.foresignal.testsupport.search.SearchTestSources.BULLETIN;
import static ai.foresignal.testsupport.search.SearchTestSources.BULLETIN_KIND;
import static ai.foresignal.testsupport.search.SearchTestSources.NEWS;
import static ai.foresignal.testsupport.search.SearchTestSources.NEWS_KIND;
import static ai.foresignal.testsupport.search.SearchTestSources.PLAIN;
import static ai.foresignal.testsupport.search.SearchTestSources.PLAIN_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.support.BboxParser;
import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.records.RecordsTestSources;
import ai.foresignal.testsupport.search.SearchItSupport;
import ai.foresignal.testsupport.search.SearchTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/** {@link SearchService} over real documents (prompt 027): ranking, fusion, filters, degradation. */
@IntegrationTest
@Import(SearchTestSources.class)
class SearchServiceIT extends SearchItSupport {

    @Autowired SearchService search;

    private static final String FLOOD_TEXT = NEWS_KIND + " | Search test source " + NEWS
            + " | Flood flood flood in the valley | Rivers rising";

    private Map<String, Object> run(String q, SearchRequest.Mode mode) {
        return run(new SearchRequest(q, mode, Set.of(), List.of(), List.of(), null, null, null, false, 25));
    }

    private Map<String, Object> run(SearchRequest request) {
        return search.search(request, Set.of());
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> items(Map<String, Object> results) {
        return (List<Map<String, Object>>) results.get("items");
    }

    private static List<Object> ids(Map<String, Object> results) {
        return items(results).stream().map(i -> i.get("docId")).toList();
    }

    private void seedNews() {
        records.ingest(NEWS,
                news("flood", "Flood flood flood in the valley", "Rivers rising", 10, 10),
                news("mixed", "Flood watch", "Dry elsewhere", 20, 20),
                news("quake", "Earthquake felt", "Minor shaking", -100, 40));
        awaitCaughtUp();
    }

    @Test
    @DisplayName("text: ts_rank_cd ranks the document with more matches first and snippets highlight the terms")
    void textRanking() {
        semanticReady("p8", 8);
        seedNews();
        Map<String, Object> results = run("flood", SearchRequest.Mode.TEXT);
        assertThat(results.get("mode")).isEqualTo("text");
        assertThat(results.get("degraded")).isEqualTo(false);
        assertThat(ids(results)).containsExactly(NEWS + ":flood", NEWS + ":mixed");
        Map<String, Object> first = items(results).getFirst();
        assertThat((String) first.get("snippet")).contains("<b>Flood</b>");
        assertThat((double) first.get("score")).isGreaterThan((double) items(results).get(1).get("score"));
    }

    @Test
    @DisplayName("semantic: the document whose text equals the query is nearest")
    void semanticOrdering() {
        semanticReady("p8", 8);
        seedNews();
        Map<String, Object> results = run(FLOOD_TEXT, SearchRequest.Mode.SEMANTIC);
        assertThat(results.get("mode")).isEqualTo("semantic");
        assertThat(ids(results)).hasSize(3);
        assertThat(ids(results).getFirst()).isEqualTo(NEWS + ":flood");
        assertThat((double) items(results).getFirst().get("score")).isCloseTo(1.0, org.assertj.core.data.Offset.offset(1e-5));
    }

    @Test
    @DisplayName("hybrid: reciprocal rank fusion puts the document found by both lists first")
    void hybridFusion() {
        semanticReady("p8", 8);
        seedNews();
        Map<String, Object> results = run("flood", SearchRequest.Mode.HYBRID);
        assertThat(results.get("mode")).isEqualTo("hybrid");
        assertThat(results.get("degraded")).isEqualTo(false);
        assertThat(ids(results)).contains(NEWS + ":flood", NEWS + ":mixed", NEWS + ":quake");
        List<Map<String, Object>> items = items(results);
        for (int i = 1; i < items.size(); i++) {
            assertThat((double) items.get(i - 1).get("score")).isGreaterThanOrEqualTo((double) items.get(i).get("score"));
        }
        assertThat(ids(results).indexOf(NEWS + ":quake")).isEqualTo(2);
    }

    @Test
    @DisplayName("filters: source, kind, bbox, effective time window, observed axis and docType")
    void filters() {
        semanticReady("p8", 8);
        seedNews();
        Instant earlier = T.minus(Duration.ofDays(3));
        records.ingest(BULLETIN, NormalizedRecord.builder("b1", BULLETIN_KIND).observedAt(T).eventTime(earlier)
                .point(10.5, 10.5).put("body", "Flood bulletin for the valley").build());
        awaitCaughtUp();
        worker.embedDocument("briefing", "run-1", "Daily flood briefing", null, T, null, null);

        assertThat(ids(run(new SearchRequest("flood", SearchRequest.Mode.HYBRID, Set.of(), List.of(BULLETIN), List.of(),
                null, null, null, false, 25)))).containsExactly(BULLETIN + ":b1");
        assertThat(ids(run(new SearchRequest("flood", SearchRequest.Mode.TEXT, Set.of(), List.of(), List.of(NEWS_KIND),
                null, null, null, false, 25)))).containsOnly(NEWS + ":flood", NEWS + ":mixed");
        assertThat(ids(run(new SearchRequest("flood", SearchRequest.Mode.SEMANTIC, Set.of(), List.of(), List.of(),
                BboxParser.parse("9,9,11,11"), null, null, false, 25)))).containsOnly(NEWS + ":flood", BULLETIN + ":b1");
        // The bulletin's effective time is its event time, three days back.
        assertThat(ids(run(new SearchRequest("flood", SearchRequest.Mode.TEXT, Set.of("record"), List.of(), List.of(),
                null, T.minus(Duration.ofDays(1)), null, false, 25)))).doesNotContain(BULLETIN + ":b1");
        assertThat(ids(run(new SearchRequest("flood", SearchRequest.Mode.TEXT, Set.of("record"), List.of(), List.of(),
                null, earlier.minusSeconds(1), earlier.plusSeconds(1), false, 25)))).containsExactly(BULLETIN + ":b1");
        // On the observed axis it was seen now.
        assertThat(ids(run(new SearchRequest("flood", SearchRequest.Mode.TEXT, Set.of("record"), List.of(), List.of(),
                null, T.minus(Duration.ofDays(1)), null, true, 25)))).contains(BULLETIN + ":b1");
        assertThat(ids(run(new SearchRequest("flood", SearchRequest.Mode.TEXT, Set.of("briefing"), List.of(), List.of(),
                null, null, null, false, 25)))).containsExactly("run-1");
    }

    @Test
    @DisplayName("record hits carry the persisted seq and the current record")
    void recordHitsCarrySeq() {
        semanticReady("p8", 8);
        IngestOutcome outcome = records.ingest(NEWS, news("cite", "Citable flood", "Seq", 1, 1));
        awaitCaughtUp();
        Map<String, Object> item = items(run("citable", SearchRequest.Mode.HYBRID)).getFirst();
        assertThat(item.get("docType")).isEqualTo("record");
        assertThat(item.get("seq")).isEqualTo(outcome.toSeq());
        assertThat(item.get("source")).isEqualTo(NEWS);
        assertThat(item.get("kind")).isEqualTo(NEWS_KIND);
        assertThat(item.get("title")).isEqualTo("Citable flood");
        assertThat(item.get("effectiveTime")).isEqualTo(T);
        assertThat(item.get("geometry")).isNotNull();
        @SuppressWarnings("unchecked")
        Map<String, Object> record = (Map<String, Object>) item.get("record");
        assertThat(record).isNotNull();
        assertThat(((Number) record.get("seq")).longValue()).isEqualTo(outcome.toSeq());
        assertThat(record).containsKeys("version", "live", "title");
    }

    @Test
    @DisplayName("text search finds records without documents through record_head.search_text")
    void textFallbackToSearchText() {
        records.ingest(PLAIN, NormalizedRecord.builder("p1", PLAIN_KIND).observedAt(T).point(1, 1)
                .put("name", "Harbor crane").build());
        Map<String, Object> results = run("crane", SearchRequest.Mode.TEXT);
        assertThat(ids(results)).containsExactly(PLAIN + ":p1");
        assertThat(items(results).getFirst().get("seq")).isNotNull();
    }

    @Test
    @DisplayName("without an embedding provider: hybrid degrades to text with a reason, semantic is 409")
    void degradedWithoutProvider() {
        records.ingest(PLAIN, NormalizedRecord.builder("p2", PLAIN_KIND).observedAt(T).put("name", "Flood gauge").build());
        Map<String, Object> results = run("flood", SearchRequest.Mode.HYBRID);
        assertThat(results.get("degraded")).isEqualTo(true);
        assertThat((String) results.get("reason")).contains("No embedding provider");
        assertThat(ids(results)).containsExactly(PLAIN + ":p2");
        assertThatThrownBy(() -> run("flood", SearchRequest.Mode.SEMANTIC))
                .isInstanceOf(ConflictException.class)
                .hasFieldOrPropertyWithValue("code", "SEMANTIC_UNAVAILABLE");
    }

    @Test
    @DisplayName("a failing provider degrades hybrid to text; semantic surfaces the provider error")
    void failingProvider() {
        semanticReady("p8", 8);
        seedNews();
        fakes.fail("p8");
        Map<String, Object> results = run("flood", SearchRequest.Mode.HYBRID);
        assertThat(results.get("degraded")).isEqualTo(true);
        assertThat(ids(results)).contains(NEWS + ":flood");
        assertThatThrownBy(() -> run("flood", SearchRequest.Mode.SEMANTIC))
                .isInstanceOf(ai.foresignal.llm.client.LlmProviderException.class);
    }

    @Test
    @DisplayName("private sources are excluded for callers that may not see them")
    void privateExcluded() {
        records.ingest(RecordsTestSources.PRIVATE, NormalizedRecord.builder("s1", RecordsTestSources.PRIVATE_KIND)
                .observedAt(T).put("name", "secret harbor").build());
        records.ingest(PLAIN, NormalizedRecord.builder("p3", PLAIN_KIND).observedAt(T).put("name", "public harbor").build());
        Map<String, Object> hidden = search.search(new SearchRequest("harbor", SearchRequest.Mode.TEXT, Set.of(),
                List.of(), List.of(), null, null, null, false, 25), Set.of(RecordsTestSources.PRIVATE));
        assertThat(ids(hidden)).containsExactly(PLAIN + ":p3");
    }
}
