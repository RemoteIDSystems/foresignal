package ai.foresignal.situation;

import ai.foresignal.search.DocInput;
import ai.foresignal.search.SearchIndexState;
import ai.foresignal.search.SearchIndexStateStore;
import ai.foresignal.search.VectorSlot;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The text-similarity input of the linker score (prompt 118 §2): the cosine between the 027 embedding of an input's
 * record and the embedding of the situation's {@code foresignal-intel} record (title and summary stem), both read
 * from the active vector slot of {@code embedding_document} and only when both were embedded by the same model.
 * Nothing is embedded here; when either vector is missing (no embedding provider, not yet indexed, a private
 * source) the term is absent.
 */
@Component
public class SituationSimilarity {

    private final JdbcTemplate jdbc;
    private final ObjectProvider<SearchIndexStateStore> states;

    /** Creates the component. */
    public SituationSimilarity(JdbcTemplate jdbc, ObjectProvider<SearchIndexStateStore> states) {
        this.jdbc = jdbc;
        this.states = states;
    }

    /** The {@code embedding_document} id of a situation's record. */
    public static String situationDocId(String situationId) {
        return DocInput.recordId(SituationRecordPublisher.SOURCE_ID, situationId);
    }

    /**
     * The cosine similarity in [−1, 1] of two record documents, or null when either is missing.
     *
     * @param inputDocId the input's record document id ({@code sourceId:externalId}), or null
     * @param situationId the situation
     */
    public Double cosine(String inputDocId, String situationId) {
        if (inputDocId == null || situationId == null) {
            return null;
        }
        SearchIndexStateStore store = states.getIfAvailable();
        if (store == null) {
            return null;
        }
        try {
            SearchIndexState state = store.get();
            if (state == null || !state.hasActive()) {
                return null;
            }
            VectorSlot slot = state.activeSlot();
            String column = slot.embeddingColumn();
            String model = slot.modelColumn();
            List<Double> rows = jdbc.queryForList("SELECT 1 - (a." + column + " <=> b." + column + ")"
                    + " FROM embedding_document a, embedding_document b"
                    + " WHERE a.doc_type = ? AND a.doc_id = ? AND b.doc_type = ? AND b.doc_id = ?"
                    + " AND a." + column + " IS NOT NULL AND b." + column + " IS NOT NULL AND a." + model + " = b." + model,
                    Double.class, DocInput.RECORD, inputDocId, DocInput.RECORD, situationDocId(situationId));
            return rows.isEmpty() || rows.get(0) == null ? null : Math.round(rows.get(0) * 1e6) / 1e6;
        } catch (RuntimeException e) {
            return null;
        }
    }
}
