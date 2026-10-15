package ai.foresignal.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One typed dependency between two facilities (prompt 104). Every edge states how it is known
 * ({@code method}), its {@code label}, the rule that produced it and the evidence for it.
 */
@Entity
@Table(name = "infra_edge")
public class InfraEdgeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "build_id", nullable = false)
    private long buildId;

    @Column(name = "from_node", nullable = false)
    private long fromNode;

    @Column(name = "to_node", nullable = false)
    private long toNode;

    @Column(name = "relation", length = 32, nullable = false)
    private String relation;

    @Column(name = "method", length = 16, nullable = false)
    private String method;

    @Column(name = "label", length = 16, nullable = false)
    private String label;

    @Column(name = "score", nullable = false)
    private double score;

    @Column(name = "rule_id", length = 64, nullable = false)
    private String ruleId;

    @Column(name = "rule_version", nullable = false)
    private int ruleVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "params")
    private Map<String, Object> params;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "evidence")
    private Map<String, Object> evidence;

    @Column(name = "licence", length = 128)
    private String licence;

    @Column(name = "built_at", nullable = false)
    private Instant builtAt;

    /** JPA. */
    protected InfraEdgeEntity() {}

    /** Creates an edge. */
    public InfraEdgeEntity(long buildId, long fromNode, long toNode, String relation, String method, String label,
            String ruleId, int ruleVersion, Instant builtAt) {
        this.buildId = buildId;
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.relation = relation;
        this.method = method;
        this.label = label;
        this.ruleId = ruleId;
        this.ruleVersion = ruleVersion;
        this.builtAt = builtAt;
    }

    public Long getId() {
        return id;
    }

    public long getBuildId() {
        return buildId;
    }

    public long getFromNode() {
        return fromNode;
    }

    public long getToNode() {
        return toNode;
    }

    public String getRelation() {
        return relation;
    }

    public String getMethod() {
        return method;
    }

    public String getLabel() {
        return label;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getRuleId() {
        return ruleId;
    }

    public int getRuleVersion() {
        return ruleVersion;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public Map<String, Object> getEvidence() {
        return evidence;
    }

    public void setEvidence(Map<String, Object> evidence) {
        this.evidence = evidence;
    }

    public String getLicence() {
        return licence;
    }

    public void setLicence(String licence) {
        this.licence = licence;
    }

    public Instant getBuiltAt() {
        return builtAt;
    }
}
