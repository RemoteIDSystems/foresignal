package ai.foresignal.search;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Geometry;

/**
 * One searchable document ({@code embedding_document}, Architecture §20): the latest persisted
 * version of a record, a briefing run (029) or an enriched alert (030).
 *
 * <p>Hibernate creates the table and maps only these columns. The vector slots
 * ({@code embedding_a}/{@code model_a}, {@code embedding_b}/{@code model_b}), the generated
 * {@code tsv} column and the GIN, GiST, btree and HNSW indexes are native DDL owned by
 * {@link EmbeddingSchemaManager}, because their dimensions are dynamic and JPA cannot express them.
 * Every write goes through {@link EmbeddingStore}'s native SQL; the entity exists for the schema and
 * for reads in tests.
 */
@Entity
@IdClass(EmbeddingDocumentId.class)
@Table(name = "embedding_document")
public class EmbeddingDocumentEntity {

    @Id
    @Column(name = "doc_type", length = 16, nullable = false)
    private String docType;

    @Id
    @Column(name = "doc_id", length = 400, nullable = false)
    private String docId;

    @Column(name = "source_id", length = 64)
    private String sourceId;

    @Column(name = "kind", length = 64)
    private String kind;

    /** The persisted version seq the text came from (records); cite with it. */
    @Column(name = "seq")
    private Long seq;

    @Column(name = "geom", columnDefinition = "geometry(Geometry,4326)")
    private Geometry geom;

    /** {@code COALESCE(event_time, observed_at)} of that version. */
    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "effective_time")
    private Instant effectiveTime;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "observed_at")
    private Instant observedAt;

    @Column(name = "text", columnDefinition = "text", nullable = false)
    private String text;

    @Column(name = "text_hash", nullable = false)
    private long textHash;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected EmbeddingDocumentEntity() {}

    /** The document type. */
    public String getDocType() {
        return docType;
    }

    /** The document id. */
    public String getDocId() {
        return docId;
    }

    /** The source, or null for documents without one. */
    public String getSourceId() {
        return sourceId;
    }

    /** The record kind, or null. */
    public String getKind() {
        return kind;
    }

    /** The persisted version seq the text came from, or null. */
    public Long getSeq() {
        return seq;
    }

    /** The geometry, or null. */
    public Geometry getGeom() {
        return geom;
    }

    /** The effective time, or null. */
    public Instant getEffectiveTime() {
        return effectiveTime;
    }

    /** The observation time, or null. */
    public Instant getObservedAt() {
        return observedAt;
    }

    /** The embedded text. */
    public String getText() {
        return text;
    }

    /** The 64-bit hash of {@link #getText()}. */
    public long getTextHash() {
        return textHash;
    }

    /** When the row last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
