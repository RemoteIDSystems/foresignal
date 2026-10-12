package ai.foresignal.search;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ai.foresignal.store.query.PgArrays;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The native SQL of {@code embedding_document} writes and maintenance reads (prompt 027).
 *
 * <p>Vector columns are named only through {@link VectorSlot}; vectors are bound as pgvector text
 * literals ({@code [x,y,…]}) and cast. Upserts never move a record document backwards: a row is
 * replaced only when the incoming {@code seq} is at least the stored one.
 */
@Component
public class EmbeddingStore {

    private static final String GEOM = "ST_GeomFromEWKB(decode(CAST(? AS text), 'hex'))";
    private static final String VECTOR = "CAST(CAST(? AS text) AS vector)";
    private static final String NOT_BACKWARDS = " WHERE d.seq IS NULL OR EXCLUDED.seq IS NULL OR d.seq <= EXCLUDED.seq";

    private final JdbcTemplate jdbc;

    /** Creates the store. */
    public EmbeddingStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * What is stored for some documents.
     *
     * @param textHash the stored {@code text_hash}
     * @param seq the stored seq, or null
     * @param model the model id in the slot asked for, or null (also when no slot was asked for)
     */
    public record DocState(long textHash, Long seq, String model) {}

    /**
     * A stored document, for re-embedding from its text.
     *
     * @param docType the type
     * @param docId the id
     * @param text the stored text
     */
    public record StoredDoc(String docType, String docId, String text) {}

    /**
     * Vectors for one slot, aligned with the documents they belong to.
     *
     * @param modelId the model id written to {@code model_<slot>}
     * @param vectors one per document, in order
     */
    public record SlotVectors(String modelId, List<float[]> vectors) {}

    /** The stored hash, seq and {@code slot}'s model (when {@code slot} is not null) of {@code docIds}. */
    public Map<String, DocState> states(String docType, Collection<String> docIds, VectorSlot slot) {
        if (docIds.isEmpty()) {
            return Map.of();
        }
        String model = slot == null ? "NULL" : slot.modelColumn();
        Map<String, DocState> out = new HashMap<>();
        jdbc.query("SELECT doc_id, text_hash, seq, " + model + " AS model FROM embedding_document"
                        + " WHERE doc_type = ? AND doc_id = ANY(CAST(? AS text[]))",
                rs -> {
                    long seq = rs.getLong("seq");
                    out.put(rs.getString("doc_id"), new DocState(rs.getLong("text_hash"),
                            rs.wasNull() ? null : seq, rs.getString("model")));
                }, docType, PgArrays.text(docIds));
        return out;
    }

    /**
     * Inserts or replaces {@code docs} with their vectors: every slot in {@code vectors} gets its
     * vector and model id, every slot in {@code cleared} is set to null (its model could not
     * embed the new text). Slots in neither are left as they are.
     */
    public void upsert(List<DocInput> docs, Map<VectorSlot, SlotVectors> vectors, Set<VectorSlot> cleared) {
        if (docs.isEmpty()) {
            return;
        }
        StringBuilder columns = new StringBuilder(
                "doc_type, doc_id, source_id, kind, seq, geom, effective_time, observed_at, text, text_hash, updated_at");
        StringBuilder values = new StringBuilder("?, ?, ?, ?, ?, " + GEOM + ", ?, ?, ?, ?, now()");
        StringBuilder updates = new StringBuilder("source_id = EXCLUDED.source_id, kind = EXCLUDED.kind,"
                + " seq = EXCLUDED.seq, geom = EXCLUDED.geom, effective_time = EXCLUDED.effective_time,"
                + " observed_at = EXCLUDED.observed_at, text = EXCLUDED.text, text_hash = EXCLUDED.text_hash,"
                + " updated_at = EXCLUDED.updated_at");
        List<VectorSlot> slots = new ArrayList<>(vectors.keySet());
        slots.sort(null);
        for (VectorSlot slot : slots) {
            columns.append(", ").append(slot.embeddingColumn()).append(", ").append(slot.modelColumn());
            values.append(", ").append(VECTOR).append(", ?");
            updates.append(", ").append(slot.embeddingColumn()).append(" = EXCLUDED.").append(slot.embeddingColumn())
                    .append(", ").append(slot.modelColumn()).append(" = EXCLUDED.").append(slot.modelColumn());
        }
        for (VectorSlot slot : cleared) {
            if (!vectors.containsKey(slot)) {
                updates.append(", ").append(slot.embeddingColumn()).append(" = NULL, ")
                        .append(slot.modelColumn()).append(" = NULL");
            }
        }
        String sql = "INSERT INTO embedding_document AS d (" + columns + ") VALUES (" + values + ")"
                + " ON CONFLICT (doc_type, doc_id) DO UPDATE SET " + updates + NOT_BACKWARDS;
        List<Object[]> args = new ArrayList<>(docs.size());
        for (int i = 0; i < docs.size(); i++) {
            DocInput doc = docs.get(i);
            List<Object> row = new ArrayList<>(List.of(doc.docType(), doc.docId()));
            row.add(doc.sourceId());
            row.add(doc.kind());
            row.add(doc.seq());
            row.add(doc.geomHex());
            row.add(utc(doc.effectiveTime()));
            row.add(utc(doc.observedAt()));
            row.add(doc.text());
            row.add(doc.textHash());
            for (VectorSlot slot : slots) {
                SlotVectors slotVectors = vectors.get(slot);
                row.add(literal(slotVectors.vectors().get(i)));
                row.add(slotVectors.modelId());
            }
            args.add(row.toArray());
        }
        jdbc.batchUpdate(sql, args);
    }

    /**
     * Updates the filter columns of documents whose text did not change (no embedding call): seq,
     * geometry, times and kind follow the latest version, so citations and filters stay current.
     */
    public void touch(List<DocInput> docs) {
        if (docs.isEmpty()) {
            return;
        }
        List<Object[]> args = new ArrayList<>(docs.size());
        for (DocInput doc : docs) {
            args.add(new Object[] {doc.sourceId(), doc.kind(), doc.seq(), doc.geomHex(), utc(doc.effectiveTime()),
                    utc(doc.observedAt()), doc.docType(), doc.docId(), doc.seq()});
        }
        jdbc.batchUpdate("UPDATE embedding_document SET source_id = ?, kind = ?, seq = ?, geom = " + GEOM
                + ", effective_time = ?, observed_at = ?, updated_at = now()"
                + " WHERE doc_type = ? AND doc_id = ? AND (seq IS NULL OR CAST(? AS bigint) IS NULL OR seq <= ?)",
                withSeqTwice(args));
    }

    /** Writes {@code vectors} (aligned with {@code docs}) into {@code slot} with {@code modelId}. */
    public void setVectors(VectorSlot slot, String modelId, List<StoredDoc> docs, List<float[]> vectors) {
        if (docs.isEmpty()) {
            return;
        }
        List<Object[]> args = new ArrayList<>(docs.size());
        for (int i = 0; i < docs.size(); i++) {
            StoredDoc doc = docs.get(i);
            args.add(new Object[] {literal(vectors.get(i)), modelId, doc.docType(), doc.docId()});
        }
        jdbc.batchUpdate("UPDATE embedding_document SET " + slot.embeddingColumn() + " = " + VECTOR + ", "
                + slot.modelColumn() + " = ? WHERE doc_type = ? AND doc_id = ?", args);
    }

    /**
     * Deletes a document; a record document only when its stored seq is at most {@code maxSeq}
     * (null deletes whatever is stored).
     *
     * @return the rows deleted
     */
    public int delete(String docType, String docId, Long maxSeq) {
        if (maxSeq == null) {
            return jdbc.update("DELETE FROM embedding_document WHERE doc_type = ? AND doc_id = ?", docType, docId);
        }
        return jdbc.update("DELETE FROM embedding_document WHERE doc_type = ? AND doc_id = ?"
                + " AND (seq IS NULL OR seq <= ?)", docType, docId, maxSeq);
    }

    /**
     * Documents whose {@code slot} does not hold {@code modelId}, after the keyset position, in key
     * order. {@code nonRecordOnly} limits them to briefing runs and alerts.
     */
    public List<StoredDoc> missing(VectorSlot slot, String modelId, boolean nonRecordOnly, String afterType,
            String afterId, int limit) {
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT doc_type, doc_id, text FROM embedding_document WHERE ")
                .append(slot.modelColumn()).append(" IS DISTINCT FROM ?");
        args.add(modelId);
        if (nonRecordOnly) {
            sql.append(" AND doc_type <> 'record'");
        }
        if (afterType != null) {
            sql.append(" AND (doc_type, doc_id) > (?, ?)");
            args.add(afterType);
            args.add(afterId);
        }
        sql.append(" ORDER BY doc_type, doc_id LIMIT ").append(limit);
        return jdbc.query(sql.toString(),
                (rs, i) -> new StoredDoc(rs.getString("doc_type"), rs.getString("doc_id"), rs.getString("text")),
                args.toArray());
    }

    /** How many documents' {@code slot} does not hold {@code modelId}. */
    public long countMissing(VectorSlot slot, String modelId) {
        Long n = jdbc.queryForObject("SELECT count(*) FROM embedding_document WHERE " + slot.modelColumn()
                + " IS DISTINCT FROM ?", Long.class, modelId);
        return n == null ? 0 : n;
    }

    /** Every document. */
    public long count() {
        Long n = jdbc.queryForObject("SELECT count(*) FROM embedding_document", Long.class);
        return n == null ? 0 : n;
    }

    /** Briefing runs and alerts. */
    public long countNonRecord() {
        Long n = jdbc.queryForObject("SELECT count(*) FROM embedding_document WHERE doc_type <> 'record'", Long.class);
        return n == null ? 0 : n;
    }

    /** The pgvector text literal of {@code vector}; non-finite components are refused. */
    public static String literal(float[] vector) {
        StringBuilder out = new StringBuilder(vector.length * 10).append('[');
        for (int i = 0; i < vector.length; i++) {
            float value = vector[i];
            if (!Float.isFinite(value)) {
                throw new IllegalArgumentException("Embedding component " + i + " is not finite: " + value);
            }
            if (i > 0) {
                out.append(',');
            }
            out.append(value);
        }
        return out.append(']').toString();
    }

    /** {@code instant} as a UTC {@link OffsetDateTime}, or null. */
    static OffsetDateTime utc(Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    /** Reads a nullable timestamp column. */
    static Instant instant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    private static List<Object[]> withSeqTwice(List<Object[]> args) {
        List<Object[]> out = new ArrayList<>(args.size());
        for (Object[] row : args) {
            Object[] extended = new Object[row.length + 1];
            System.arraycopy(row, 0, extended, 0, row.length);
            extended[row.length] = row[row.length - 1];
            out.add(extended);
        }
        return out;
    }
}
