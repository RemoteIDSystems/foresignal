package ai.foresignal.media.outlet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * The consensus of one outlet on one dimension over the enabled providers (prompt 133, table
 * {@code outlet_consensus}), recomputed on every import ({@link Consensus}). {@code id} is
 * {@code {outletId}|{dimension}}; {@code providers} holds the per-provider breakdown.
 */
@Entity
@Table(name = "outlet_consensus", indexes = {
        @Index(name = "outlet_consensus_outlet_idx", columnList = "outlet_id"),
        @Index(name = "outlet_consensus_dimension_bucket_idx", columnList = "dimension, bucket")})
public class OutletConsensusEntity {

    @Id
    @Column(name = "id", length = 120, nullable = false)
    private String id;

    @Column(name = "outlet_id", length = 26, nullable = false)
    private String outletId;

    @Column(name = "dimension", length = 80, nullable = false)
    private String dimension;

    @Column(name = "median")
    private Double median;

    @Column(name = "n_providers", nullable = false)
    private int nProviders;

    @Column(name = "disagreement")
    private Double disagreement;

    @Column(name = "bucket", length = 16)
    private String bucket;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "providers")
    private List<Map<String, Object>> providers = new ArrayList<>();

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt;

    /** For JPA. */
    protected OutletConsensusEntity() {}

    /** The id. */
    public String getId() {
        return id;
    }
}
