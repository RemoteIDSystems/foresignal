package ai.foresignal.news;

import ai.foresignal.search.DocInput;
import ai.foresignal.search.SearchIndexState;
import ai.foresignal.search.SearchIndexStateStore;
import ai.foresignal.search.VectorSlot;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Article title embeddings for the story clustering (prompt 133): the 027 vectors of {@code news-article} records
 * (their descriptors embed {@code title}), read from the active vector slot of {@code embedding_document}. Nothing is
 * embedded here; an article not yet indexed (no provider, a reindex, the worker behind) has no vector and the story
 * resolver falls back to title tokens.
 */
@Component
public class TitleEmbeddings {

    private final JdbcTemplate jdbc;
    private final ObjectProvider<SearchIndexStateStore> states;

    /** Creates the component. */
    public TitleEmbeddings(JdbcTemplate jdbc, ObjectProvider<SearchIndexStateStore> states) {
        this.jdbc = jdbc;
        this.states = states;
    }

    /**
     * Vectors of one model.
     *
     * @param model the embedding model, or null when there is no active index
     * @param vectors record key ({@code source/externalId}) → vector, for the records that have one
     */
    public record TitleVectors(String model, Map<String, float[]> vectors) {}

    /** The vectors of the given records ({@code source/externalId} keys). */
    public TitleVectors vectors(Collection<String> recordKeys) {
        Map<String, float[]> out = new LinkedHashMap<>();
        SearchIndexStateStore store = states.getIfAvailable();
        if (store == null || recordKeys == null || recordKeys.isEmpty()) {
            return new TitleVectors(null, out);
        }
        try {
            SearchIndexState state = store.get();
            if (state == null || !state.hasActive()) {
                return new TitleVectors(null, out);
            }
            VectorSlot slot = state.activeSlot();
            Map<String, String> keyOfDoc = new LinkedHashMap<>();
            for (String key : new LinkedHashSet<>(recordKeys)) {
                int slash = key.indexOf('/');
                if (slash > 0) {
                    keyOfDoc.put(DocInput.recordId(key.substring(0, slash), key.substring(slash + 1)), key);
                }
            }
            String[] model = new String[1];
            jdbc.query("SELECT doc_id, " + slot.embeddingColumn() + "::text AS v, " + slot.modelColumn() + " AS m"
                    + " FROM embedding_document WHERE doc_type = ? AND doc_id = ANY(?) AND " + slot.embeddingColumn()
                    + " IS NOT NULL", rs -> {
                        float[] v = Vectors.parse(rs.getString("v"));
                        String m = rs.getString("m");
                        if (v == null || m == null || (model[0] != null && !model[0].equals(m))) {
                            return;
                        }
                        model[0] = m;
                        out.put(keyOfDoc.get(rs.getString("doc_id")), v);
                    }, DocInput.RECORD, keyOfDoc.keySet().toArray(String[]::new));
            return new TitleVectors(model[0], out);
        } catch (RuntimeException e) {
            return new TitleVectors(null, Map.of());
        }
    }
}
