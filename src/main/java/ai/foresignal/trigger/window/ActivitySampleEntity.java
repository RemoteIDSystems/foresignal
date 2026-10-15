package ai.foresignal.trigger.window;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * One anomaly bucket's count and coverage (prompt 100, table {@code activity_sample}). Exempt from
 * version retention and pruned after 26 weeks. Keyed by rule, group and bucket start.
 */
@Entity
@Table(name = "activity_sample")
@IdClass(ActivitySampleEntity.Key.class)
public class ActivitySampleEntity {

    @Id
    @Column(name = "rule_id", nullable = false)
    private UUID ruleId;

    @Id
    @Column(name = "group_key", nullable = false, length = 256)
    private String groupKey;

    @Id
    @Column(name = "bucket_start", nullable = false)
    private Instant bucketStart;

    @Column(name = "count", nullable = false)
    private int count;

    @Column(name = "coverage_ratio")
    private Double coverageRatio;

    @Column(name = "coverage_ok", nullable = false)
    private boolean coverageOk;

    @Column(name = "flagged", nullable = false)
    private boolean flagged;

    /** JPA. */
    protected ActivitySampleEntity() {}

    /** Creates one sample. */
    public ActivitySampleEntity(UUID ruleId, String groupKey, Instant bucketStart, int count) {
        this.ruleId = ruleId;
        this.groupKey = groupKey;
        this.bucketStart = bucketStart;
        this.count = count;
    }

    public UUID getRuleId() {
        return ruleId;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public Instant getBucketStart() {
        return bucketStart;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Double getCoverageRatio() {
        return coverageRatio;
    }

    public void setCoverageRatio(Double coverageRatio) {
        this.coverageRatio = coverageRatio;
    }

    public boolean isCoverageOk() {
        return coverageOk;
    }

    public void setCoverageOk(boolean coverageOk) {
        this.coverageOk = coverageOk;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    /** The composite key. */
    public static class Key implements Serializable {

        private static final long serialVersionUID = 1L;

        private UUID ruleId;
        private String groupKey;
        private Instant bucketStart;

        /** JPA. */
        public Key() {}

        /** Creates the key. */
        public Key(UUID ruleId, String groupKey, Instant bucketStart) {
            this.ruleId = ruleId;
            this.groupKey = groupKey;
            this.bucketStart = bucketStart;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Key id)) {
                return false;
            }
            return Objects.equals(ruleId, id.ruleId) && Objects.equals(groupKey, id.groupKey)
                    && Objects.equals(bucketStart, id.bucketStart);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ruleId, groupKey, bucketStart);
        }
    }
}
