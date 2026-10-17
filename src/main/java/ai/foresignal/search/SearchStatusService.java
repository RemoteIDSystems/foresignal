package ai.foresignal.search;

import ai.foresignal.ingest.PersistReason;
import ai.foresignal.llm.LlmSettingsService;
import ai.foresignal.llm.client.EmbeddingModelSpec;
import ai.foresignal.store.query.PgArrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Whether semantic search can answer now, and the {@code SearchStatus} document
 * ({@code GET /v1/search/status}, prompt 027).
 *
 * <p>Semantic search needs pgvector, an assigned embedding provider and a built index; the first
 * missing piece is the {@code reason}. While the outgoing model of a reindex cannot be called, the
 * status is {@code degraded} and searches fall back to text until the swap.
 */
@Service
public class SearchStatusService {

    /** The reason when no embedding provider is assigned. */
    public static final String NO_PROVIDER = "No embedding provider is assigned; add one in /v1/llm/providers and"
            + " assign it as embeddingProvider with PUT /v1/llm/settings";

    /** Backlog counts stop here. */
    static final long BACKLOG_CAP = 1_000_000;

    private final SemanticCapability capability;
    private final LlmSettingsService llmSettings;
    private final SearchIndexStateStore states;
    private final EmbeddingStore store;
    private final EmbeddingEligibility eligibility;
    /** The embedding worker; absent in edge mode (prompt 113). */
    private final org.springframework.beans.factory.ObjectProvider<EmbeddingWorker> worker;
    private final ReindexService reindex;
    private final JdbcTemplate jdbc;
    private volatile String degradedReason;

    /** Creates the service. */
    public SearchStatusService(SemanticCapability capability, LlmSettingsService llmSettings,
            SearchIndexStateStore states, EmbeddingStore store, EmbeddingEligibility eligibility,
            org.springframework.beans.factory.ObjectProvider<EmbeddingWorker> worker,
            ReindexService reindex, JdbcTemplate jdbc) {
        this.capability = capability;
        this.llmSettings = llmSettings;
        this.states = states;
        this.store = store;
        this.eligibility = eligibility;
        this.worker = worker;
        this.reindex = reindex;
        this.jdbc = jdbc;
    }

    /**
     * Whether a semantic query can be answered.
     *
     * @param available true when it can
     * @param reason why not, or null
     * @param state the index state it was decided on
     */
    public record Availability(boolean available, String reason, SearchIndexState state) {}

    /** Whether semantic search can answer, and if not why. */
    public Availability availability() {
        SearchIndexState state = states.get();
        if (!capability.available()) {
            return new Availability(false, capability.reason(), state);
        }
        if (llmSettings.embeddingSpec().isEmpty()) {
            return new Availability(false, NO_PROVIDER, state);
        }
        if (!state.hasActive()) {
            SearchIndexState.Pending pending = state.pending();
            String reason = pending == null ? "No semantic index has been built yet"
                    : "The first semantic index is being built (" + pending.done() + " of " + pending.total()
                            + " documents)";
            return new Availability(false, reason, state);
        }
        return new Availability(true, null, state);
    }

    /** Records that a search fell back to text because the query could not be embedded. */
    void degraded(String reason) {
        this.degradedReason = reason;
    }

    /** Records that a semantic query succeeded. */
    void recovered() {
        this.degradedReason = null;
    }

    /** The {@code SearchStatus} document. */
    public Map<String, Object> status() {
        Availability availability = availability();
        SearchIndexState state = availability.state();
        EmbeddingModelSpec active = state.activeSpec();
        String degraded = availability.available() ? degradedReason : null;
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("semanticEnabled", availability.available());
        out.put("reason", availability.available() ? degraded : availability.reason());
        out.put("model", active == null ? null : active.model());
        out.put("dimensions", active == null ? null : active.dimensions());
        out.put("documents", store.count());
        out.put("backlog", backlog());
        out.put("reindexing", state.pending() != null);
        out.put("activeModel", state.activeModelId());
        out.put("reindex", reindex.reindexState());
        out.put("degraded", degraded != null);
        return out;
    }

    /** The {@code search} block of {@code /v1/system} ({@code SystemInfo.search}). */
    public Map<String, Object> systemBlock() {
        Availability availability = availability();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("semanticEnabled", availability.available());
        out.put("reason", availability.reason());
        out.put("documents", store.count());
        out.put("backlog", backlog());
        out.put("activeModel", availability.state().activeModelId());
        out.put("reindexState", reindex.reindexState().get("state"));
        return out;
    }

    /**
     * Versions of embeddable sources behind {@code embedding.cursor} (heartbeats excluded), counted
     * up to {@value #BACKLOG_CAP}; zero while the worker does not run.
     */
    public long backlog() {
        EmbeddingWorker running = worker.getIfAvailable();
        if (running == null || !running.running()) {
            return 0;
        }
        Set<String> sources = eligibility.sources();
        if (sources.isEmpty()) {
            return 0;
        }
        Long n = jdbc.queryForObject("SELECT count(*) FROM (SELECT 1 FROM record_version v WHERE v.seq > ?"
                        + " AND v.source_id = ANY(CAST(? AS text[])) AND v.persist_reason <> ? LIMIT " + BACKLOG_CAP + ") b",
                Long.class, running.cursor(), PgArrays.text(sources), PersistReason.HEARTBEAT.code());
        return n == null ? 0 : n;
    }
}
