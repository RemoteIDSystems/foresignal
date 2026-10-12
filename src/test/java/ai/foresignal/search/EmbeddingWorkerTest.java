package ai.foresignal.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.ingest.PersistReason;
import ai.foresignal.llm.LlmBudgetExceededException;
import ai.foresignal.llm.LlmSettingsService;
import ai.foresignal.llm.client.EmbeddingModelSpec;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.ChangeListener;
import ai.foresignal.stream.ChangeSummary;
import ai.foresignal.stream.Subscription;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * {@link EmbeddingWorker}'s batch handling without a database (prompt 027): heartbeats are skipped
 * without loading anything, other reasons are loaded by seq, and a budget refusal pauses without
 * advancing the cursor.
 */
class EmbeddingWorkerTest {

    static final Instant NOW = Instant.parse("2026-05-01T12:00:30Z");
    static final EmbeddingModelSpec SPEC = new EmbeddingModelSpec("p", "m", 8);

    final ChangeBus bus = mock(ChangeBus.class);
    final LlmSettingsService llm = mock(LlmSettingsService.class);
    final EmbeddingEligibility eligibility = mock(EmbeddingEligibility.class);
    final EmbeddingIndexer indexer = mock(EmbeddingIndexer.class);
    final EmbeddingStore store = mock(EmbeddingStore.class);
    final VersionRows versions = mock(VersionRows.class);
    final SearchIndexStateStore states = mock(SearchIndexStateStore.class);
    final AppSettingService settings = mock(AppSettingService.class);
    final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    final SemanticCapability capability = new SemanticCapability();
    EmbeddingWorker worker;

    @BeforeEach
    void setUp() {
        capability.markAvailable();
        when(llm.embeddingSpec()).thenReturn(Optional.of(SPEC));
        when(bus.lastPublishedSeq()).thenReturn(5L);
        when(bus.subscribe(anyString(), any(ChangeListener.class), eq(EmbeddingWorker.QUEUE)))
                .thenReturn(mock(Subscription.class));
        when(jdbc.queryForList(anyString(), eq(String.class), any())).thenReturn(List.of("5"));
        when(states.get()).thenReturn(new SearchIndexState(VectorSlot.A, SPEC.modelId(), SPEC, null));
        when(eligibility.sources()).thenReturn(Set.of("s"));
        worker = new EmbeddingWorker(bus, capability, llm, eligibility, indexer, store, versions, states, settings, jdbc,
                Clock.fixed(NOW, ZoneOffset.UTC), mock(PlatformTransactionManager.class));
        worker.start();
    }

    static ChangeSummary summary(long seq, String source, PersistReason reason) {
        return new ChangeSummary(seq, null, HistoryTier.STANDARD, source, "e" + seq, "k", 1, reason, NOW, null, null,
                false, 0, null, null, null);
    }

    @Test
    @DisplayName("a heartbeat-only batch advances the cursor without loading anything")
    void heartbeatSkippedWithoutLoad() {
        assertThat(worker.running()).isTrue();
        assertThat(worker.cursor()).isEqualTo(5);
        worker.onBatch(new ChangeBatch("s", 1, NOW, 6, 7,
                List.of(summary(6, "s", PersistReason.HEARTBEAT), summary(7, "other", PersistReason.FIRST))));
        verify(versions, never()).bySeq(anyList());
        verify(indexer, never()).write(anyList());
        assertThat(worker.cursor()).isEqualTo(7);
        verify(settings).put(SettingKeys.EMBEDDING_CURSOR, 7L);
    }

    @Test
    @DisplayName("other reasons of eligible sources are loaded by seq and written")
    void significantLoaded() {
        VersionRows.Row row = new VersionRows.Row(6, "s", "e6", "k", false, NOW, null, null, "{\"t\":\"x\"}");
        when(versions.bySeq(List.of(6L))).thenReturn(List.of(row));
        SourceDescriptor descriptor = SourceDescriptor.builder("s").name("S").attribution("a").kind("k").build();
        when(eligibility.descriptor("s")).thenReturn(Optional.of(descriptor));
        DocInput doc = new DocInput("record", "s:e6", "s", "k", 6L, null, NOW, NOW, "k | S | x");
        when(versions.document(row, descriptor)).thenReturn(doc);

        worker.onBatch(new ChangeBatch("s", 1, NOW, 6, 6, List.of(summary(6, "s", PersistReason.SIGNIFICANT))));

        verify(indexer).write(List.of(doc));
        assertThat(worker.cursor()).isEqualTo(6);
    }

    @Test
    @DisplayName("a budget refusal pauses until the next minute and leaves the cursor where it was")
    void budgetRefusalPauses() {
        VersionRows.Row row = new VersionRows.Row(6, "s", "e6", "k", false, NOW, null, null, "{}");
        when(versions.bySeq(List.of(6L))).thenReturn(List.of(row));
        SourceDescriptor descriptor = SourceDescriptor.builder("s").name("S").attribution("a").kind("k").build();
        when(eligibility.descriptor("s")).thenReturn(Optional.of(descriptor));
        when(versions.document(row, descriptor)).thenReturn(new DocInput("record", "s:e6", "s", "k", 6L, null, NOW, NOW, "t"));
        doThrow(new LlmBudgetExceededException("p", "over", Duration.ofSeconds(30))).when(indexer).write(anyList());

        worker.onBatch(new ChangeBatch("s", 1, NOW, 6, 6, List.of(summary(6, "s", PersistReason.FIRST))));

        assertThat(worker.cursor()).isEqualTo(5);
        assertThat(worker.pausedUntil()).isEqualTo(Instant.parse("2026-05-01T12:01:00Z"));
        assertThat(worker.lastError()).contains("over");
        // Paused: further batches are not processed until the pause ends.
        worker.onBatch(new ChangeBatch("s", 2, NOW, 7, 7, List.of(summary(7, "s", PersistReason.FIRST))));
        assertThat(worker.cursor()).isEqualTo(5);
    }

    @Test
    @DisplayName("a tombstone deletes the document instead of embedding")
    void tombstoneDeletes() {
        VersionRows.Row row = new VersionRows.Row(6, "s", "e6", "k", true, NOW, null, null, "{}");
        when(versions.bySeq(List.of(6L))).thenReturn(List.of(row));
        SourceDescriptor descriptor = SourceDescriptor.builder("s").name("S").attribution("a").kind("k").build();
        when(eligibility.descriptor("s")).thenReturn(Optional.of(descriptor));
        when(versions.document(row, descriptor)).thenReturn(null);

        worker.onBatch(new ChangeBatch("s", 1, NOW, 6, 6, List.of(summary(6, "s", PersistReason.TOMBSTONE))));

        verify(store).delete("record", "s:e6", 6L);
        assertThat(worker.cursor()).isEqualTo(6);
    }

    @Test
    @DisplayName("without pgvector or an embedding provider the worker does not start")
    void doesNotStartWithoutPrerequisites() {
        worker.stop();
        capability.markUnavailable("no pgvector");
        worker.start();
        assertThat(worker.running()).isFalse();
        capability.markAvailable();
        when(llm.embeddingSpec()).thenReturn(Optional.empty());
        worker.start();
        assertThat(worker.running()).isFalse();
    }
}
