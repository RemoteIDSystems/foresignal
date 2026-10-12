package ai.foresignal.search;

import static ai.foresignal.testsupport.search.SearchTestSources.NEWS;
import static ai.foresignal.testsupport.search.SearchTestSources.NEWS_KIND;
import static ai.foresignal.testsupport.search.SearchTestSources.OPT_OUT;
import static ai.foresignal.testsupport.search.SearchTestSources.PLAIN;
import static ai.foresignal.testsupport.search.SearchTestSources.PLAIN_KIND;
import static ai.foresignal.testsupport.search.SearchTestSources.PRIVATE_EMBED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.search.SearchItSupport;
import ai.foresignal.testsupport.search.SearchTestSources;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * {@link EmbeddingWorker} end to end (prompt 027): records seeded through {@code TestRecords}, the
 * worker following the change bus, embeddings from the deterministic fake provider.
 */
@IntegrationTest
@Import(SearchTestSources.class)
class EmbeddingWorkerIT extends SearchItSupport {

    @Autowired SourceService sources;
    @Autowired EmbeddingRetentionStep retentionStep;

    @Test
    @DisplayName("eligible records are embedded into the active slot with effective_time = COALESCE(event, observed)")
    void eligibleRecordsEmbedded() {
        semanticReady("p8", 8);
        Instant event = T.minus(Duration.ofHours(2));
        records.ingest(NEWS,
                NormalizedRecord.builder("a", NEWS_KIND).observedAt(T).eventTime(event).point(10, 20)
                        .put("headline", "River flooding").put("summary", "Levees overtopped").put("severity", "major")
                        .build(),
                news("b", "Port strike", "Dock workers walk out", 5, 5));
        awaitCaughtUp();

        EmbeddingDocumentEntity a = documents.findById(new EmbeddingDocumentId("record", NEWS + ":a")).orElseThrow();
        assertThat(a.getEffectiveTime()).isEqualTo(event);
        assertThat(a.getObservedAt()).isEqualTo(T);
        assertThat(a.getKind()).isEqualTo(NEWS_KIND);
        assertThat(a.getSourceId()).isEqualTo(NEWS);
        assertThat(a.getText()).isEqualTo(NEWS_KIND + " | Search test source " + NEWS + " | River flooding | Levees overtopped");
        assertThat(a.getTextHash()).isEqualTo(EmbeddingText.hash(a.getText()));
        assertThat(a.getSeq()).isPositive();
        assertThat(a.getGeom()).isNotNull();
        EmbeddingDocumentEntity b = documents.findById(new EmbeddingDocumentId("record", NEWS + ":b")).orElseThrow();
        assertThat(b.getEffectiveTime()).isEqualTo(T);
        assertThat(modelOf(VectorSlot.A, "record", NEWS + ":a")).isEqualTo(spec("p8").modelId());
        assertThat(jdbc.queryForObject("SELECT vector_dims(embedding_a) FROM embedding_document WHERE doc_id = ?",
                Integer.class, NEWS + ":a")).isEqualTo(8);
    }

    @Test
    @DisplayName("a source without embedFields is never embedded")
    void plainSourceNotEmbedded() {
        semanticReady("p8", 8);
        records.ingest(PLAIN, NormalizedRecord.builder("x", PLAIN_KIND).observedAt(T).put("name", "plain").build());
        awaitCaughtUp();
        assertThat(hasRecordDocument(PLAIN + ":x")).isFalse();
    }

    @Test
    @DisplayName("unchanged text is skipped by text_hash: no embedding call, but seq follows the new version")
    void unchangedTextSkipped() {
        semanticReady("p8", 8);
        String model = spec("p8").modelId();
        records.ingest(NEWS, news("a", "Quiet day", "Nothing to report", 1, 1));
        awaitCaughtUp();
        long calls = fakes.texts(model);
        long firstSeq = documents.findById(new EmbeddingDocumentId("record", NEWS + ":a")).orElseThrow().getSeq();

        clock.advance(Duration.ofMinutes(1));
        IngestOutcome second = records.ingest(NEWS, NormalizedRecord.builder("a", NEWS_KIND).observedAt(clock.instant())
                .point(1, 1).put("headline", "Quiet day").put("summary", "Nothing to report").put("severity", "major")
                .build());
        assertThat(second.persisted()).isEqualTo(1);
        awaitCaughtUp();

        assertThat(fakes.texts(model)).isEqualTo(calls);
        long seq = documents.findById(new EmbeddingDocumentId("record", NEWS + ":a")).orElseThrow().getSeq();
        assertThat(seq).isGreaterThan(firstSeq).isEqualTo(second.toSeq());

        clock.advance(Duration.ofMinutes(1));
        records.ingest(NEWS, news("a", "Busy day", "Plenty to report", 1, 1));
        awaitCaughtUp();
        assertThat(fakes.texts(model)).isEqualTo(calls + 1);
    }

    @Test
    @DisplayName("a tombstone deletes the record's document")
    void tombstoneDeletes() {
        semanticReady("p8", 8);
        records.ingest(NEWS, news("gone", "Bridge closed", "Inspection", 1, 1));
        awaitCaughtUp();
        assertThat(hasRecordDocument(NEWS + ":gone")).isTrue();

        clock.advance(Duration.ofMinutes(1));
        records.ingest(NEWS, NormalizedRecord.tombstone("gone", NEWS_KIND, clock.instant()));
        awaitCaughtUp();
        assertThat(hasRecordDocument(NEWS + ":gone")).isFalse();
    }

    @Test
    @DisplayName("a private source is never embedded, even though it declares embedFields")
    void privateNeverEmbedded() {
        semanticReady("p8", 8);
        records.ingest(PRIVATE_EMBED, NormalizedRecord.builder("me", NEWS_KIND).observedAt(T).point(1, 1)
                .put("headline", "Family location").build());
        records.ingest(NEWS, news("public", "Public item", "Visible", 1, 1));
        awaitCaughtUp();
        assertThat(hasRecordDocument(PRIVATE_EMBED + ":me")).isFalse();
        assertThat(hasRecordDocument(NEWS + ":public")).isTrue();

        reindex.requestReindex();
        reindex.runToCompletion();
        assertThat(hasRecordDocument(PRIVATE_EMBED + ":me")).isFalse();
    }

    @Test
    @DisplayName("a budget refusal pauses the worker without advancing the cursor or losing documents")
    void budgetRefusalPauses() {
        provider("tight", 8, 1);
        assign("tight");
        reindex.runToCompletion();
        records.ingest(NEWS, news("first", "First item", "One", 1, 1));
        awaitCaughtUp();
        assertThat(hasRecordDocument(NEWS + ":first")).isTrue();

        // The per-minute request budget is spent: the next batch is refused.
        records.ingest(NEWS, news("second", "Second item", "Two", 1, 1));
        records.ingest(NEWS, news("third", "Third item", "Three", 1, 1));
        await().atMost(Duration.ofSeconds(30)).until(() -> worker.pausedUntil() != null);
        long cursor = worker.cursor();
        assertThat(cursor).isLessThan(bus.lastPublishedSeq());
        assertThat(hasRecordDocument(NEWS + ":first")).isTrue();
        assertThat(settings.get(SettingKeys.EMBEDDING_CURSOR, Long.class)).contains(cursor);

        providers.replace("tight", JSON.readTree("{\"type\":\"ollama\",\"embeddingModel\":\"fake-embed\","
                + "\"embeddingDimensions\":8,\"requestsPerMinute\":1000}"));
        clock.advance(Duration.ofMinutes(2));
        worker.tick();
        awaitCaughtUp();
        assertThat(hasRecordDocument(NEWS + ":second")).isTrue();
        assertThat(hasRecordDocument(NEWS + ":third")).isTrue();
        assertThat(worker.pausedUntil()).isNull();
    }

    @Test
    @DisplayName("the cursor is durable: a stopped worker catches up from it on start")
    void cursorSurvivesRestart() {
        semanticReady("p8", 8);
        records.ingest(NEWS, news("before", "Before", "Stop", 1, 1));
        awaitCaughtUp();
        long stored = settings.get(SettingKeys.EMBEDDING_CURSOR, Long.class).orElseThrow();
        assertThat(stored).isEqualTo(worker.cursor());

        worker.stop();
        records.ingest(NEWS, news("during", "During", "Downtime", 1, 1));
        assertThat(hasRecordDocument(NEWS + ":during")).isFalse();
        settings.invalidateCache();
        worker.start();
        awaitCaughtUp();
        assertThat(hasRecordDocument(NEWS + ":during")).isTrue();
        assertThat(settings.get(SettingKeys.EMBEDDING_CURSOR, Long.class)).contains(bus.lastPublishedSeq());
    }

    @Test
    @DisplayName("embed=false: the source is not embedded and its documents are deleted")
    void embedFlagRespected() {
        semanticReady("p8", 8);
        records.ingest(OPT_OUT, NormalizedRecord.builder("o1", NEWS_KIND).observedAt(T).put("headline", "Opted").build());
        awaitCaughtUp();
        assertThat(hasRecordDocument(OPT_OUT + ":o1")).isTrue();

        sources.update(OPT_OUT, SourceSettingsUpdate.embed(false));
        await().atMost(Duration.ofSeconds(10)).until(() -> !hasRecordDocument(OPT_OUT + ":o1"));
        assertThat(sources.view(OPT_OUT).embed()).isFalse();

        records.ingest(OPT_OUT, NormalizedRecord.builder("o2", NEWS_KIND).observedAt(T).put("headline", "Out").build());
        awaitCaughtUp();
        assertThat(hasRecordDocument(OPT_OUT + ":o2")).isFalse();
        assertThat(sources.view(NEWS).embed()).isTrue();
        assertThat(sources.view(PLAIN).embed()).isFalse();
    }

    @Test
    @DisplayName("embedDocument stores a briefing document and dual-writes like records")
    void embedDocumentForBriefings() {
        semanticReady("p8", 8);
        worker.embedDocument("briefing", "run-1", "Morning briefing\nThree quakes overnight", null, T, null, null);
        assertThat(modelOf(VectorSlot.A, "briefing", "run-1")).isEqualTo(spec("p8").modelId());
        worker.embedDocument("briefing", "run-1", " ", null, T, null, null);
        assertThat(documents.findById(new EmbeddingDocumentId("briefing", "run-1"))).isEmpty();
    }

    @Test
    @DisplayName("the retention step deletes record documents whose head no longer exists")
    void retentionStepDeletesOrphans() {
        semanticReady("p8", 8);
        jdbc.update("INSERT INTO embedding_document (doc_type, doc_id, source_id, text, text_hash, updated_at)"
                + " VALUES ('record', ?, ?, 'orphan', 1, now())", NEWS + ":orphan", NEWS);
        records.ingest(NEWS, news("kept", "Kept", "Alive", 1, 1));
        awaitCaughtUp();
        assertThat(retentionStep.name()).isEqualTo("embedding-documents");
        assertThat(retentionStep.order()).isBetween(400, 700);
        jdbc.update(EmbeddingRetentionStep.DELETE_ORPHANS.replace(":limit", "10000"));
        assertThat(hasRecordDocument(NEWS + ":orphan")).isFalse();
        assertThat(hasRecordDocument(NEWS + ":kept")).isTrue();
    }

    @Test
    @DisplayName("the embedding cursor joins retention's consumer clamp while the worker runs")
    void consumerClamp(@Autowired EmbeddingCursorConsumer consumer) {
        assertThat(consumer.name()).isEqualTo("embedding.cursor");
        assertThat(consumer.cursorSeq()).isEmpty();
        semanticReady("p8", 8);
        records.ingest(NEWS, news("c", "Clamp", "Cursor", 1, 1));
        awaitCaughtUp();
        assertThat(consumer.cursorSeq()).hasValue(worker.cursor());
    }
}
