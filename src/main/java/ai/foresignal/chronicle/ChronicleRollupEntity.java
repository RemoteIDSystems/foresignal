package ai.foresignal.chronicle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * The count of one {@code rollup-only} layer in one region and hour (table {@code chronicle_rollup}, prompt 131): no
 * record is kept, only how many matching versions were seen. The region is the record's 040 tag
 * ({@code record_region}), {@code ZZ} for untagged records. Written by native upsert; unique per
 * {@code (chronicle_id, layer_id, region, hour_start)}.
 */
@Entity
@Table(name = "chronicle_rollup", uniqueConstraints = @UniqueConstraint(name = "chronicle_rollup_key",
        columnNames = {"chronicle_id", "layer_id", "region", "hour_start"}))
public class ChronicleRollupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "chronicle_id", length = 26, nullable = false)
    private String chronicleId;

    @Column(name = "layer_id", length = 26, nullable = false)
    private String layerId;

    @Column(name = "region", length = 16, nullable = false)
    private String region;

    @Column(name = "hour_start", nullable = false)
    private Instant hour;

    @Column(name = "versions", nullable = false)
    private long count;

    @Column(name = "backfilled", nullable = false)
    private boolean backfilled;

    /** For JPA. */
    protected ChronicleRollupEntity() {}

    /** The row id. */
    public Long getId() {
        return id;
    }

    /** The chronicle. */
    public String getChronicleId() {
        return chronicleId;
    }

    /** The layer. */
    public String getLayerId() {
        return layerId;
    }

    /** The 040 region code, or {@code ZZ}. */
    public String getRegion() {
        return region;
    }

    /** The hour bucket. */
    public Instant getHour() {
        return hour;
    }

    /** Matching versions seen. */
    public long getCount() {
        return count;
    }

    /** True when (part of) the count came from a backfill. */
    public boolean isBackfilled() {
        return backfilled;
    }
}
