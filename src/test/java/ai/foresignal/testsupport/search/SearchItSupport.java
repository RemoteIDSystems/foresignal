package ai.foresignal.testsupport.search;

import static org.awaitility.Awaitility.await;

import ai.foresignal.llm.LlmProviderService;
import ai.foresignal.llm.LlmSettings;
import ai.foresignal.llm.LlmSettingsService;
import ai.foresignal.llm.client.EmbeddingModelSpec;
import ai.foresignal.search.EmbeddingDocumentRepository;
import ai.foresignal.search.EmbeddingSchemaManager;
import ai.foresignal.search.EmbeddingWorker;
import ai.foresignal.search.ReindexService;
import ai.foresignal.search.SearchIndexStateStore;
import ai.foresignal.search.VectorSlot;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.search.SearchTestSources.FakeEmbeddingClients;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Shared fixtures of the search integration tests (prompt 027): fake embedding providers created
 * through the real {@link LlmProviderService}, role assignment through {@link LlmSettingsService}
 * (which publishes {@code EmbeddingModelChangedEvent}), records seeded only through
 * {@link TestRecords}, and the reindex driven step by step (its background loop is off).
 */
public abstract class SearchItSupport {

    /** The test clock's instant. */
    public static final Instant T = Instant.parse("2026-05-01T12:00:00Z");

    protected static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired protected TestRecords records;
    @Autowired protected LlmProviderService providers;
    @Autowired protected LlmSettingsService llmSettings;
    @Autowired protected AppSettingService settings;
    @Autowired protected ReindexService reindex;
    @Autowired protected EmbeddingWorker worker;
    @Autowired protected FakeEmbeddingClients fakes;
    @Autowired protected JdbcTemplate jdbc;
    @Autowired protected MutableClock clock;
    @Autowired protected EmbeddingDocumentRepository documents;
    @Autowired protected ChangeBus bus;
    @Autowired protected SearchIndexStateStore states;
    @Autowired protected EmbeddingSchemaManager schema;

    /** Fresh settings cache, fakes and clock; no worker until a test assigns a provider. */
    @BeforeEach
    void resetSearch() {
        settings.invalidateCache();
        fakes.reset();
        reindex.setBackgroundEnabled(false);
        worker.stop();
        clock.setInstant(T);
    }

    /** Creates an Ollama-type provider {@code id} whose fake embeddings are {@code dimensions} wide. */
    protected void provider(String id, int dimensions) {
        provider(id, dimensions, 1_000);
    }

    /** Creates a provider with {@code requestsPerMinute}. */
    protected void provider(String id, int dimensions, int requestsPerMinute) {
        providers.create(JSON.readTree("{\"id\":\"" + id + "\",\"type\":\"ollama\",\"embeddingModel\":\"fake-embed\","
                + "\"embeddingDimensions\":" + dimensions + ",\"requestsPerMinute\":" + requestsPerMinute + "}"));
    }

    /** Assigns {@code id} as the embedding provider (null clears the role). */
    protected void assign(String id) {
        llmSettings.update(new LlmSettings(null, id, true, true, false, 200_000, false));
    }

    /** The embedding spec of provider {@code id}. */
    protected EmbeddingModelSpec spec(String id) {
        return providers.config(id).embeddingSpec().orElseThrow();
    }

    /** A provider, assigned, with its first index built (slot A active). */
    protected void semanticReady(String id, int dimensions) {
        provider(id, dimensions);
        assign(id);
        reindex.runToCompletion();
    }

    /** Waits until the worker has processed everything published. */
    protected void awaitCaughtUp() {
        await().atMost(Duration.ofSeconds(30)).until(() -> worker.running() && worker.cursor() >= bus.lastPublishedSeq());
    }

    /** A {@code test-search-news} record at {@code (lon, lat)}. */
    protected NormalizedRecord news(String externalId, String headline, String summary, double lon, double lat) {
        return NormalizedRecord.builder(externalId, SearchTestSources.NEWS_KIND)
                .observedAt(clock.instant())
                .point(lon, lat)
                .put("headline", headline)
                .put("summary", summary)
                .put("severity", "minor")
                .build();
    }

    /** The model id stored in {@code slot} for a document, or null. */
    protected String modelOf(VectorSlot slot, String docType, String docId) {
        List<String> values = jdbc.queryForList("SELECT " + slot.modelColumn() + " FROM embedding_document"
                + " WHERE doc_type = ? AND doc_id = ?", String.class, docType, docId);
        return values.isEmpty() ? null : values.getFirst();
    }

    /** True when {@code docId} has a record document. */
    protected boolean hasRecordDocument(String docId) {
        Long n = jdbc.queryForObject("SELECT count(*) FROM embedding_document WHERE doc_type = 'record' AND doc_id = ?",
                Long.class, docId);
        return n != null && n > 0;
    }
}
