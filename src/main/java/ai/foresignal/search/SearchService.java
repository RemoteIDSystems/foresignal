package ai.foresignal.search;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.llm.LlmBudgetExceededException;
import ai.foresignal.llm.client.LlmProviderException;
import ai.foresignal.store.query.ReadQueryRunner;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.store.query.SqlParams;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code GET /v1/search} (Architecture §20, FR-47, prompt 027).
 *
 * <ul>
 *   <li>{@code text}: {@code tsv @@ websearch_to_tsquery('simple', q)} ranked by {@code ts_rank_cd},
 *       with a {@code ts_headline} snippet, then {@code record_head.search_text ILIKE} matches for
 *       records that have no document. Always available, with or without an LLM.
 *   <li>{@code semantic}: {@code q} is embedded (budgeted) with the active spec, which during a
 *       reindex is the outgoing model, and documents are ordered by cosine distance in the active
 *       slot, restricted to vectors of the active model ({@code SET LOCAL hnsw.ef_search = 100}).
 *   <li>{@code hybrid} (default): reciprocal rank fusion of both lists ({@code k = }{@value #RRF_K}).
 * </ul>
 *
 * <p>When semantic search is unavailable, {@code semantic} is {@code 409 SEMANTIC_UNAVAILABLE} and
 * {@code hybrid} degrades to text with {@code degraded=true}. When the outgoing model of a running
 * reindex can no longer be called, both degrade to text until the swap. Filters (source, kind, bbox,
 * document type, time window on the effective or observed time) apply to every list; the caller's
 * invisible (private) sources are excluded. Record hits carry the document's persisted {@code seq}
 * and the current {@code Record}.
 */
@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    /** The reciprocal-rank-fusion constant. */
    public static final int RRF_K = 60;

    /** Longest snippet and title. */
    static final int SNIPPET_CHARS = 200;

    static final String DOC_COLUMNS = "d.doc_type, d.doc_id, d.source_id, d.kind, d.seq, d.effective_time,"
            + " d.observed_at, d.text, ST_AsGeoJSON(d.geom) AS geom_json";

    static final String HEADLINE_OPTIONS = "MaxFragments=1, MaxWords=30, MinWords=10";

    private final ReadQueryRunner runner;
    private final SearchStatusService status;
    private final EmbeddingIndexer indexer;
    private final RecordQueryService records;
    private final RecordMapper mapper;
    private final JsonMapper json;

    /** Creates the service. */
    public SearchService(ReadQueryRunner runner, SearchStatusService status, EmbeddingIndexer indexer,
            RecordQueryService records, RecordMapper mapper, JsonMapper json) {
        this.runner = runner;
        this.status = status;
        this.indexer = indexer;
        this.records = records;
        this.mapper = mapper;
        this.json = json;
    }

    /** One result before it is rendered. */
    static final class Hit {
        final String docType;
        final String docId;
        final String sourceId;
        final String kind;
        final Long seq;
        final Instant effectiveTime;
        final Instant observedAt;
        final String text;
        final String geomJson;
        final String headTitle;
        double score;
        String snippet;

        Hit(String docType, String docId, String sourceId, String kind, Long seq, Instant effectiveTime,
                Instant observedAt, String text, String geomJson, String headTitle, double score, String snippet) {
            this.docType = docType;
            this.docId = docId;
            this.sourceId = sourceId;
            this.kind = kind;
            this.seq = seq;
            this.effectiveTime = effectiveTime;
            this.observedAt = observedAt;
            this.text = text;
            this.geomJson = geomJson;
            this.headTitle = headTitle;
            this.score = score;
            this.snippet = snippet;
        }

        String key() {
            return docType + "\u0000" + docId;
        }
    }

    /**
     * Runs {@code request} for a caller who may not see {@code excludedSources}.
     *
     * @return the {@code SearchResults} document
     * @throws ConflictException {@code SEMANTIC_UNAVAILABLE} for {@code mode=semantic} without
     *     semantic search
     * @throws LlmBudgetExceededException when {@code mode=semantic} and the budget refuses the query
     */
    public Map<String, Object> search(SearchRequest request, Set<String> excludedSources) {
        SearchRequest.Mode mode = request.mode();
        int fetch = mode == SearchRequest.Mode.HYBRID ? Math.min(2 * request.limit(), 2 * SearchRequest.MAX_LIMIT)
                : request.limit();
        boolean degraded = false;
        String reason = null;
        List<Hit> semantic = null;
        if (mode != SearchRequest.Mode.TEXT) {
            SearchStatusService.Availability availability = status.availability();
            if (!availability.available()) {
                if (mode == SearchRequest.Mode.SEMANTIC) {
                    throw new ConflictException(ErrorCodes.SEMANTIC_UNAVAILABLE, availability.reason());
                }
                degraded = true;
                reason = availability.reason();
            } else {
                try {
                    semantic = semantic(request, excludedSources, availability.state(), fetch);
                    status.recovered();
                } catch (LlmBudgetExceededException e) {
                    if (mode == SearchRequest.Mode.SEMANTIC) {
                        throw e;
                    }
                    degraded = true;
                    reason = "The embedding budget is exhausted: " + e.getMessage();
                } catch (LlmProviderException | NotFoundException e) {
                    boolean reindexing = availability.state().pending() != null;
                    if (mode == SearchRequest.Mode.SEMANTIC && !reindexing) {
                        throw e;
                    }
                    degraded = true;
                    reason = reindexing
                            ? "The outgoing embedding model " + availability.state().activeModelId()
                                    + " is no longer callable (" + e.getMessage()
                                    + "); text results are returned until the new index is swapped in"
                            : "The embedding provider failed (" + e.getMessage() + "); text results are returned";
                    status.degraded(reason);
                    log.warn("Semantic search degraded to text: {}", reason);
                }
            }
        }
        List<Hit> hits;
        if (semantic == null) {
            hits = text(request, excludedSources, request.limit());
        } else if (mode == SearchRequest.Mode.SEMANTIC) {
            hits = semantic;
        } else {
            hits = fuse(text(request, excludedSources, fetch), semantic);
        }
        if (hits.size() > request.limit()) {
            hits = new ArrayList<>(hits.subList(0, request.limit()));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("mode", mode.wireValue());
        out.put("items", render(hits, excludedSources));
        out.put("degraded", degraded);
        if (reason != null) {
            out.put("reason", reason);
        }
        return out;
    }

    // ------------------------------------------------------------------------- semantic

    private List<Hit> semantic(SearchRequest request, Set<String> excluded, SearchIndexState state, int fetch) {
        float[] query = indexer.embed(state.activeSpec(), List.of(request.q())).getFirst();
        VectorSlot slot = state.activeSlot();
        String literal = EmbeddingStore.literal(query);
        return runner.read(jdbc -> {
            jdbc.getJdbcTemplate().execute("SET LOCAL hnsw.ef_search = 100");
            SqlParams p = new SqlParams();
            String vector = "CAST(" + p.text(literal) + " AS vector)";
            List<String> where = new ArrayList<>();
            where.add("d." + slot.modelColumn() + " = " + p.text(state.activeModelId()));
            where.add("d." + slot.embeddingColumn() + " IS NOT NULL");
            where.addAll(docFilters(request, excluded, p));
            String sql = "SELECT " + DOC_COLUMNS + ", d." + slot.embeddingColumn() + " <=> " + vector + " AS distance"
                    + " FROM embedding_document d WHERE " + String.join(" AND ", where)
                    + " ORDER BY d." + slot.embeddingColumn() + " <=> " + vector + " LIMIT " + fetch;
            return jdbc.query(sql, p.source(), (rs, i) -> {
                Hit hit = docHit(rs, 1.0 - rs.getDouble("distance"));
                hit.snippet = excerpt(hit.text);
                return hit;
            });
        });
    }

    // ------------------------------------------------------------------------- text

    private List<Hit> text(SearchRequest request, Set<String> excluded, int fetch) {
        return runner.read(jdbc -> {
            List<Hit> hits = new ArrayList<>(textDocuments(jdbc, request, excluded, fetch));
            if (request.includesRecords() && hits.size() < fetch) {
                Set<String> seen = new LinkedHashSet<>();
                hits.forEach(h -> seen.add(h.key()));
                for (Hit hit : textHeads(jdbc, request, excluded, fetch)) {
                    if (hits.size() >= fetch) {
                        break;
                    }
                    if (seen.add(hit.key())) {
                        hits.add(hit);
                    }
                }
            }
            return hits;
        });
    }

    private List<Hit> textDocuments(NamedParameterJdbcTemplate jdbc, SearchRequest request, Set<String> excluded,
            int fetch) {
        SqlParams p = new SqlParams();
        String q = p.text(request.q());
        List<String> where = new ArrayList<>();
        where.add("d.tsv @@ query");
        where.addAll(docFilters(request, excluded, p));
        String sql = "SELECT " + DOC_COLUMNS + ", ts_rank_cd(d.tsv, query) AS rank,"
                + " ts_headline('simple', d.text, query, '" + HEADLINE_OPTIONS + "') AS snippet"
                + " FROM embedding_document d, websearch_to_tsquery('simple', " + q + ") query"
                + " WHERE " + String.join(" AND ", where)
                + " ORDER BY rank DESC, d.doc_type, d.doc_id LIMIT " + fetch;
        return jdbc.query(sql, p.source(), (rs, i) -> {
            Hit hit = docHit(rs, rs.getDouble("rank"));
            hit.snippet = rs.getString("snippet");
            return hit;
        });
    }

    /** {@code record_head.search_text ILIKE} matches of records without a document. */
    private List<Hit> textHeads(NamedParameterJdbcTemplate jdbc, SearchRequest request, Set<String> excluded,
            int fetch) {
        SqlParams p = new SqlParams();
        String time = request.observedAxis() ? "h.persisted_observed_at"
                : "COALESCE(h.event_time, h.persisted_observed_at)";
        List<String> where = new ArrayList<>();
        where.add("NOT h.tombstone");
        where.add("h.search_text ILIKE " + p.text("%" + escapeLike(request.q().strip()) + "%") + " ESCAPE '\\'");
        where.add("NOT EXISTS (SELECT 1 FROM embedding_document d WHERE d.doc_type = 'record'"
                + " AND d.doc_id = h.source_id || ':' || h.external_id)");
        if (!request.sources().isEmpty()) {
            where.add("h.source_id = ANY(" + p.textArray(request.sources()) + ")");
        }
        if (!request.kinds().isEmpty()) {
            where.add("h.kind = ANY(" + p.textArray(request.kinds()) + ")");
        }
        if (!excluded.isEmpty()) {
            where.add("NOT (h.source_id = ANY(" + p.textArray(excluded) + "))");
        }
        if (request.bbox() != null) {
            where.add(RecordQueryService.bboxClause("l.geom", request.bbox(), p));
        }
        if (request.since() != null) {
            where.add(time + " >= " + p.instant(request.since()));
        }
        if (request.until() != null) {
            where.add(time + " < " + p.instant(request.until()));
        }
        String sql = "SELECT h.source_id, h.external_id, h.kind, h.seq, " + time + " AS effective_time,"
                + " h.persisted_observed_at AS observed_at, h.search_text, h.title, ST_AsGeoJSON(l.geom) AS geom_json"
                + " FROM record_head h JOIN record_live l ON l.source_id = h.source_id AND l.external_id = h.external_id"
                + " WHERE " + String.join(" AND ", where)
                + " ORDER BY " + time + " DESC, h.source_id, h.external_id LIMIT " + fetch;
        return jdbc.query(sql, p.source(), (rs, i) -> {
            String sourceId = rs.getString("source_id");
            String searchText = rs.getString("search_text");
            return new Hit(DocInput.RECORD, DocInput.recordId(sourceId, rs.getString("external_id")), sourceId,
                    rs.getString("kind"), rs.getLong("seq"), EmbeddingStore.instant(rs.getTimestamp("effective_time")),
                    EmbeddingStore.instant(rs.getTimestamp("observed_at")), searchText, rs.getString("geom_json"),
                    rs.getString("title"), 0.0, excerpt(searchText));
        });
    }

    // ------------------------------------------------------------------------- fusion and filters

    /** Reciprocal rank fusion: each list adds {@code 1 / (k + rank)} for each hit, rank from 1. */
    static List<Hit> fuse(List<Hit> text, List<Hit> semantic) {
        Map<String, Hit> fused = new LinkedHashMap<>();
        Map<String, Double> scores = new LinkedHashMap<>();
        for (List<Hit> list : List.of(text, semantic)) {
            for (int i = 0; i < list.size(); i++) {
                Hit hit = list.get(i);
                fused.putIfAbsent(hit.key(), hit);
                scores.merge(hit.key(), 1.0 / (RRF_K + i + 1), Double::sum);
            }
        }
        List<Hit> out = new ArrayList<>(fused.values());
        for (Hit hit : out) {
            hit.score = scores.get(hit.key());
        }
        out.sort(Comparator.comparingDouble((Hit h) -> h.score).reversed());
        return out;
    }

    private static List<String> docFilters(SearchRequest request, Set<String> excluded, SqlParams p) {
        List<String> where = new ArrayList<>();
        if (!request.docTypes().isEmpty()) {
            where.add("d.doc_type = ANY(" + p.textArray(request.docTypes()) + ")");
        }
        if (!request.sources().isEmpty()) {
            where.add("d.source_id = ANY(" + p.textArray(request.sources()) + ")");
        }
        if (!request.kinds().isEmpty()) {
            where.add("d.kind = ANY(" + p.textArray(request.kinds()) + ")");
        }
        if (!excluded.isEmpty()) {
            where.add("(d.source_id IS NULL OR NOT (d.source_id = ANY(" + p.textArray(excluded) + ")))");
        }
        if (request.bbox() != null) {
            where.add(RecordQueryService.bboxClause("d.geom", request.bbox(), p));
        }
        String time = request.observedAxis() ? "d.observed_at" : "d.effective_time";
        if (request.since() != null) {
            where.add(time + " >= " + p.instant(request.since()));
        }
        if (request.until() != null) {
            where.add(time + " < " + p.instant(request.until()));
        }
        return where;
    }

    private static Hit docHit(ResultSet rs, double score) throws SQLException {
        long seq = rs.getLong("seq");
        Long seqOrNull = rs.wasNull() ? null : seq;
        return new Hit(rs.getString("doc_type"), rs.getString("doc_id"), rs.getString("source_id"),
                rs.getString("kind"), seqOrNull, EmbeddingStore.instant(rs.getTimestamp("effective_time")),
                EmbeddingStore.instant(rs.getTimestamp("observed_at")), rs.getString("text"),
                rs.getString("geom_json"), null, score, null);
    }

    // ------------------------------------------------------------------------- rendering

    private List<Map<String, Object>> render(List<Hit> hits, Set<String> excluded) {
        List<Map.Entry<String, String>> keys = new ArrayList<>();
        for (Hit hit : hits) {
            if (DocInput.RECORD.equals(hit.docType) && hit.sourceId != null
                    && hit.docId.length() > hit.sourceId.length() + 1) {
                keys.add(Map.entry(hit.sourceId, hit.docId.substring(hit.sourceId.length() + 1)));
            }
        }
        Map<String, RecordRow> current = keys.isEmpty() ? Map.of() : records.currentByKeys(keys, excluded);
        List<Map<String, Object>> items = new ArrayList<>(hits.size());
        for (Hit hit : hits) {
            RecordRow row = current.get(hit.docId);
            Map<String, Object> record = row == null || !DocInput.RECORD.equals(hit.docType) ? null
                    : mapper.record(row, FieldSelection.ALL, RecordMapper.Shape.CURRENT);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("docType", hit.docType);
            item.put("docId", hit.docId);
            item.put("score", hit.score);
            item.put("title", title(hit, row));
            item.put("snippet", hit.snippet);
            item.put("observedAt", hit.observedAt);
            item.put("source", hit.sourceId);
            item.put("kind", hit.kind);
            item.put("geometry", hit.geomJson == null ? null : json.readTree(hit.geomJson));
            item.put("record", record);
            item.put("seq", hit.seq);
            item.put("effectiveTime", hit.effectiveTime);
            items.add(item);
        }
        return items;
    }

    /** The record's title, else the text after {@code "<kind> | <source> | "}, else the doc id. */
    static String title(Hit hit, RecordRow row) {
        if (row != null && row.title() != null && !row.title().isBlank()) {
            return row.title();
        }
        if (hit.headTitle != null && !hit.headTitle.isBlank()) {
            return hit.headTitle;
        }
        String text = hit.text;
        if (text == null || text.isBlank()) {
            return hit.docId;
        }
        if (DocInput.RECORD.equals(hit.docType)) {
            String[] parts = text.split(" \\| ", 3);
            if (parts.length == 3) {
                text = parts[2];
            }
        }
        int newline = text.indexOf('\n');
        if (newline > 0) {
            text = text.substring(0, newline);
        }
        return truncate(text.strip(), 120);
    }

    private static String excerpt(String text) {
        return text == null ? null : truncate(text.strip(), SNIPPET_CHARS);
    }

    private static String truncate(String text, int max) {
        if (text.length() <= max) {
            return text;
        }
        int end = max - 1;
        if (Character.isHighSurrogate(text.charAt(end - 1))) {
            end--;
        }
        return text.substring(0, end) + "…";
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
