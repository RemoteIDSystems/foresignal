package ai.foresignal.social.narrative;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * One edge of a narrative's propagation graph (prompt 136, table {@code social_propagation_edge}): from one node to
 * another ({@code post:…}, {@code article:…}, {@code record:…}) with its type and time delta. The full edge list is kept
 * for the narrative's lifetime; {@link PropagationTracer} summarizes it. Declares the schema; {@link NarrativeStore}
 * writes it.
 */
@Entity
@Table(name = "social_propagation_edge", indexes = {
        @Index(name = "social_propagation_edge_narrative_idx", columnList = "narrative_id")})
public class PropagationEdgeEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "narrative_id", length = 26, nullable = false)
    private String narrativeId;

    @Column(name = "from_node", length = 500, nullable = false)
    private String fromNode;

    @Column(name = "to_node", length = 500, nullable = false)
    private String toNode;

    @Column(name = "type", length = 16, nullable = false)
    private String type;

    @Column(name = "delta_seconds", nullable = false)
    private double deltaSeconds;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** For JPA. */
    protected PropagationEdgeEntity() {}

    /** The id. */
    public String getId() {
        return id;
    }
}
