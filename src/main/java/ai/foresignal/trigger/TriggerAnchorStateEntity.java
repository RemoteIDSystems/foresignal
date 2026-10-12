package ai.foresignal.trigger;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

/**
 * Where the reverse ("anchor side") evaluation of a moving-anchor rule last ran (prompt 036, table
 * {@code trigger_anchor_state}). Hibernate owns the DDL; {@link AnchorEvaluator} reads and writes the
 * rows with native SQL.
 */
@Entity
@Table(name = "trigger_anchor_state")
public class TriggerAnchorStateEntity {

    /** The primary key: rule and anchor. */
    @Embeddable
    public static class Key implements Serializable {

        private static final long serialVersionUID = 1L;

        @Column(name = "rule_id", nullable = false)
        private UUID ruleId;

        @Column(name = "anchor_source", length = 64, nullable = false)
        private String anchorSource;

        @Column(name = "anchor_external_id", length = 256, nullable = false)
        private String anchorExternalId;

        protected Key() {
            // for JPA
        }

        /** A key. */
        public Key(UUID ruleId, String anchorSource, String anchorExternalId) {
            this.ruleId = ruleId;
            this.anchorSource = anchorSource;
            this.anchorExternalId = anchorExternalId;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Key k && Objects.equals(ruleId, k.ruleId) && Objects.equals(anchorSource, k.anchorSource)
                    && Objects.equals(anchorExternalId, k.anchorExternalId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ruleId, anchorSource, anchorExternalId);
        }
    }

    @EmbeddedId
    private Key id;

    /** The anchor position the last reverse evaluation used. */
    @Column(name = "eval_geom", columnDefinition = "geometry(Point,4326)")
    private Point evalGeom;

    /** The anchor's seq at that evaluation. */
    @Column(name = "eval_seq")
    private Long evalSeq;

    /** When it ran. */
    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "eval_at")
    private Instant evalAt;

    /** {@code active} or {@code stale}. */
    @Column(name = "status", length = 8, nullable = false)
    private String status = "active";

    protected TriggerAnchorStateEntity() {
        // for JPA
    }

    /** The key. */
    public Key getId() {
        return id;
    }

    /** The evaluated position. */
    public Point getEvalGeom() {
        return evalGeom;
    }

    /** The evaluated seq. */
    public Long getEvalSeq() {
        return evalSeq;
    }

    /** When it was evaluated. */
    public Instant getEvalAt() {
        return evalAt;
    }

    /** {@code active} or {@code stale}. */
    public String getStatus() {
        return status;
    }
}
