package ai.foresignal.search;

import static ai.foresignal.testsupport.search.SearchTestSources.NEWS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.search.SearchItSupport;
import ai.foresignal.testsupport.search.SearchTestSources;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * The zero-downtime reindex (prompt 027): a model change from 8 to 16 dimensions is built in the
 * other slot while searches keep answering from the old one, then swapped in.
 */
@IntegrationTest
@Import(SearchTestSources.class)
class ReindexIT extends SearchItSupport {

    @Autowired SearchService search;
    @Autowired SearchStatusService status;

    private void seed(int count) {
        List<NormalizedRecord> batch = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            batch.add(news("r" + i, "Storm report " + i, "Wind damage in district " + i, i % 90, i % 45));
        }
        records.ingest(NEWS, batch.toArray(NormalizedRecord[]::new));
        awaitCaughtUp();
    }

    /** The embedded text of seeded record {@code i}: querying with it puts that record first. */
    private static String textOf(int i) {
        return SearchTestSources.NEWS_KIND + " | Search test source " + NEWS + " | Storm report " + i
                + " | Wind damage in district " + i;
    }

    private Map<String, Object> semantic(String q) {
        return search.search(new SearchRequest(q, SearchRequest.Mode.SEMANTIC, Set.of(), List.of(), List.of(), null,
                null, null, false, 10), Set.of());
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> items(Map<String, Object> results) {
        return (List<Map<String, Object>>) results.get("items");
    }

    private void changeModelTo16() {
        provider("p16", 16);
        assign("p16");
    }

    @Test
    @DisplayName("8 → 16 dimensions: searches answer from the old slot during the backfill, then the new slot after the swap")
    void modelChangeSwaps() {
        semanticReady("p8", 8);
        seed(5);
        String old = spec("p8").modelId();

        changeModelTo16();
        String fresh = spec("p16").modelId();
        SearchIndexState state = states.get();
        assertThat(state.activeSlot()).isEqualTo(VectorSlot.A);
        assertThat(state.activeModelId()).isEqualTo(old);
        assertThat(state.pending().slot()).isEqualTo(VectorSlot.B);
        assertThat(state.pending().total()).isEqualTo(5);
        assertThat(schema.dimensions(VectorSlot.B)).hasValue(16);

        Map<String, Object> during = semantic(textOf(3));
        assertThat(during.get("degraded")).isEqualTo(false);
        assertThat(items(during)).isNotEmpty();
        assertThat(items(during).getFirst().get("docId")).isEqualTo(NEWS + ":r3");
        assertThat(fakes.texts(old)).isPositive();

        reindex.runToCompletion();

        state = states.get();
        assertThat(state.pending()).isNull();
        assertThat(state.activeSlot()).isEqualTo(VectorSlot.B);
        assertThat(state.activeModelId()).isEqualTo(fresh);
        assertThat(state.activeSpec().dimensions()).isEqualTo(16);
        assertThat(schema.dimensions(VectorSlot.A)).isEmpty();
        assertThat(schema.hasValidIndex(VectorSlot.B)).isTrue();
        assertThat(modelOf(VectorSlot.B, "record", NEWS + ":r0")).isEqualTo(fresh);

        Map<String, Object> after = semantic(textOf(3));
        assertThat(items(after).getFirst().get("docId")).isEqualTo(NEWS + ":r3");
        assertThat(status.status()).containsEntry("activeModel", fresh).containsEntry("dimensions", 16);
    }

    @Test
    @DisplayName("records arriving during the backfill are written to both slots")
    void dualWriteDuringBackfill() {
        semanticReady("p8", 8);
        seed(3);
        changeModelTo16();
        records.ingest(NEWS, news("late", "Late arrival", "During backfill", 1, 1));
        awaitCaughtUp();
        assertThat(modelOf(VectorSlot.A, "record", NEWS + ":late")).isEqualTo(spec("p8").modelId());
        assertThat(modelOf(VectorSlot.B, "record", NEWS + ":late")).isEqualTo(spec("p16").modelId());

        reindex.runToCompletion();
        assertThat(modelOf(VectorSlot.B, "record", NEWS + ":late")).isEqualTo(spec("p16").modelId());
    }

    @Test
    @DisplayName("a restart mid-backfill resumes from done and the keyset position without re-embedding")
    void resumesAfterRestart() {
        semanticReady("p8", 8);
        seed(EmbeddingIndexer.BATCH + 6);
        changeModelTo16();
        String fresh = spec("p16").modelId();

        assertThat(reindex.step()).isTrue();
        SearchIndexState.Pending pending = states.get().pending();
        assertThat(pending.done()).isEqualTo(EmbeddingIndexer.BATCH);
        assertThat(pending.afterKey()).isEqualTo(NEWS);
        assertThat(pending.afterId()).isNotNull();
        long afterFirstBatch = fakes.texts(fresh);
        assertThat(afterFirstBatch).isEqualTo(EmbeddingIndexer.BATCH);

        // A restart: the runner is gone and the state is re-read from app_setting.
        reindex.resetState();
        settings.invalidateCache();
        reindex.reconcile();
        reindex.runToCompletion();

        assertThat(fakes.texts(fresh)).isEqualTo(EmbeddingIndexer.BATCH + 6);
        assertThat(states.get().activeModelId()).isEqualTo(fresh);
    }

    @Test
    @DisplayName("an outgoing model that is no longer callable degrades semantic search to text until the swap")
    void outgoingModelUnavailable() {
        semanticReady("p8", 8);
        seed(3);
        changeModelTo16();
        providers.delete("p8");

        Map<String, Object> results = semantic("Storm");
        assertThat(results.get("degraded")).isEqualTo(true);
        assertThat((String) results.get("reason")).contains("no longer callable");
        assertThat(items(results)).isNotEmpty();
        assertThat(status.status()).containsEntry("degraded", true);

        reindex.runToCompletion();
        Map<String, Object> after = semantic("Storm report 1");
        assertThat(after.get("degraded")).isEqualTo(false);
        assertThat(status.status()).containsEntry("degraded", false);
    }

    @Test
    @DisplayName("cancel clears pending and the pending slot; the active slot keeps answering")
    void cancel() {
        semanticReady("p8", 8);
        seed(2);
        changeModelTo16();
        assertThat(states.get().pending()).isNotNull();

        reindex.cancel();
        assertThat(states.get().pending()).isNull();
        assertThat(states.get().activeSlot()).isEqualTo(VectorSlot.A);
        assertThat(schema.dimensions(VectorSlot.B)).isEmpty();
        assertThat(items(semantic("Storm report 1"))).isNotEmpty();
        reindex.cancel();
    }

    @Test
    @DisplayName("a second reindex while one runs is 409 REINDEX_RUNNING; the same model may be rebuilt")
    void concurrentReindexConflicts() {
        semanticReady("p8", 8);
        reindex.requestReindex();
        assertThat(states.get().pending().modelId()).isEqualTo(spec("p8").modelId());
        assertThat(states.get().pending().slot()).isEqualTo(VectorSlot.B);
        assertThatThrownBy(() -> reindex.requestReindex())
                .isInstanceOf(ConflictException.class)
                .hasFieldOrPropertyWithValue("code", "REINDEX_RUNNING");
        reindex.runToCompletion();
        assertThat(states.get().activeSlot()).isEqualTo(VectorSlot.B);
    }

    @Test
    @DisplayName("status reports progress, a 10-minute rate and an ETA while backfilling")
    @SuppressWarnings("unchecked")
    void statusProgressAndEta() {
        semanticReady("p8", 8);
        seed(EmbeddingIndexer.BATCH * 2 + 8);
        changeModelTo16();

        Map<String, Object> idle = (Map<String, Object>) status.status().get("reindex");
        assertThat(idle.get("state")).isEqualTo("backfilling");
        assertThat(idle.get("done")).isEqualTo(0L);

        clock.advance(Duration.ofMinutes(1));
        reindex.step();
        Map<String, Object> progress = (Map<String, Object>) status.status().get("reindex");
        assertThat(progress.get("state")).isEqualTo("backfilling");
        assertThat(progress.get("fromModel")).isEqualTo(spec("p8").modelId());
        assertThat(progress.get("toModel")).isEqualTo(spec("p16").modelId());
        assertThat(progress.get("total")).isEqualTo((long) (EmbeddingIndexer.BATCH * 2 + 8));
        assertThat(progress.get("done")).isEqualTo((long) EmbeddingIndexer.BATCH);
        assertThat((Double) progress.get("percent")).isBetween(40.0, 50.0);
        assertThat(progress.get("ratePerMinute")).isEqualTo((double) EmbeddingIndexer.BATCH);
        assertThat((Long) progress.get("etaSeconds")).isEqualTo(
                (long) Math.ceil((EmbeddingIndexer.BATCH + 8) / (double) EmbeddingIndexer.BATCH * 60));
        assertThat(progress.get("startedAt")).isNotNull();
        assertThat(status.status()).containsEntry("reindexing", true).containsEntry("semanticEnabled", true);

        reindex.runToCompletion();
        assertThat(((Map<String, Object>) status.status().get("reindex")).get("state")).isEqualTo("idle");
    }

    @Test
    @DisplayName("the first index: semantic search is off while it builds, then on")
    void firstIndex() {
        records.ingest(NEWS, news("pre", "Before any model", "Seeded first", 1, 1));
        provider("p8", 8);
        assign("p8");
        assertThat(status.availability().available()).isFalse();
        assertThat(status.availability().reason()).contains("first semantic index");
        assertThatThrownBy(() -> semantic("Before")).isInstanceOf(ConflictException.class)
                .hasFieldOrPropertyWithValue("code", "SEMANTIC_UNAVAILABLE");

        reindex.runToCompletion();
        assertThat(status.availability().available()).isTrue();
        assertThat(modelOf(VectorSlot.A, "record", NEWS + ":pre")).isEqualTo(spec("p8").modelId());
    }

    @Test
    @DisplayName("clearing the embedding role cancels a pending reindex and stops the worker")
    void roleCleared() {
        semanticReady("p8", 8);
        changeModelTo16();
        assign(null);
        assertThat(states.get().pending()).isNull();
        assertThat(worker.running()).isFalse();
        assertThat(status.availability().reason()).contains("No embedding provider");
    }
}
