package ai.foresignal.baserate.analog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * The signature of one situation on one day of its life (prompt 121, table {@code situation_signature},
 * {@code situation-signature} {@value SignatureBuilder#VERSION}): named numeric features (standardized family counts,
 * escalation slope, theme, region class, entity classes, severity path) and the reference of its summary embedding
 * (027), computed on activation and on each day of its first 30. Primary key {@code (situation_id, day_n)}.
 */
@Entity
@Table(name = "situation_signature")
@IdClass(SituationSignatureEntity.Key.class)
public class SituationSignatureEntity {

    /** The composite primary key. */
    public static class Key implements Serializable {

        private static final long serialVersionUID = 1L;

        private String situationId;
        private int dayN;

        /** JPA. */
        public Key() {}

        /**
         * Creates a key.
         *
         * @param situationId the situation
         * @param dayN the day of its life (1–30)
         */
        public Key(String situationId, int dayN) {
            this.situationId = situationId;
            this.dayN = dayN;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Key k && Objects.equals(situationId, k.situationId) && dayN == k.dayN;
        }

        @Override
        public int hashCode() {
            return Objects.hash(situationId, dayN);
        }
    }

    @Id
    @Column(name = "situation_id", length = 26, nullable = false)
    private String situationId;

    @Id
    @Column(name = "day_n", nullable = false)
    private int dayN;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "features", columnDefinition = "float8[]", nullable = false)
    private double[] features = new double[0];

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "feature_names", columnDefinition = "text[]", nullable = false)
    private String[] featureNames = new String[0];

    @Column(name = "embedding_ref", length = 400)
    private String embeddingRef;

    @Column(name = "version", length = 16, nullable = false)
    private String version;

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt;

    /** For JPA. */
    protected SituationSignatureEntity() {}

    /**
     * Creates a signature.
     *
     * @param situationId the situation
     * @param dayN the day of its life
     * @param signature the features
     * @param embeddingRef the summary embedding's document id, or null
     * @param computedAt when it was computed
     */
    public SituationSignatureEntity(String situationId, int dayN, SignatureBuilder.Signature signature,
            String embeddingRef, Instant computedAt) {
        this.situationId = situationId;
        this.dayN = dayN;
        this.features = signature.values().clone();
        this.featureNames = signature.names().toArray(String[]::new);
        this.embeddingRef = embeddingRef;
        this.version = SignatureBuilder.VERSION;
        this.computedAt = computedAt;
    }

    /** The situation. */
    public String getSituationId() {
        return situationId;
    }

    /** The day of its life. */
    public int getDayN() {
        return dayN;
    }

    /** The features as a {@link SignatureBuilder.Signature}. */
    public SignatureBuilder.Signature signature() {
        return new SignatureBuilder.Signature(featureNames == null ? List.of() : Arrays.asList(featureNames),
                features == null ? new double[0] : features.clone());
    }

    /** The summary embedding's document id, or null. */
    public String getEmbeddingRef() {
        return embeddingRef;
    }

    /** The signature version. */
    public String getVersion() {
        return version;
    }

    /** When it was computed. */
    public Instant getComputedAt() {
        return computedAt;
    }
}
