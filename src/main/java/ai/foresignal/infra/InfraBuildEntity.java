package ai.foresignal.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** One infrastructure-graph build (prompt 104): when, from which inputs, and the counts by method. */
@Entity
@Table(name = "infra_build")
public class InfraBuildEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "started", nullable = false)
    private Instant started;

    @Column(name = "finished")
    private Instant finished;

    @Column(name = "rules_version", nullable = false)
    private int rulesVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "inputs")
    private Map<String, Object> inputs;

    @Column(name = "nodes", nullable = false)
    private long nodes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "edges_by_method")
    private Map<String, Object> edgesByMethod;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "edges_by_label")
    private Map<String, Object> edgesByLabel;

    @JdbcTypeCode(SqlTypes.JSON)
    @ColumnDefault("'[]'::jsonb")
    @Column(name = "warnings", nullable = false, columnDefinition = "jsonb")
    private java.util.List<String> warnings = new java.util.ArrayList<>();

    @Column(name = "error", columnDefinition = "text")
    private String error;

    /** The modification time of the imported OSM extract, when one was an input. */
    @Column(name = "osm_extract_date")
    private Instant osmExtractDate;

    /** JPA. */
    protected InfraBuildEntity() {}

    /** Creates a build. */
    public InfraBuildEntity(Instant started, int rulesVersion) {
        this.started = started;
        this.rulesVersion = rulesVersion;
    }

    public Long getId() {
        return id;
    }

    public Instant getStarted() {
        return started;
    }

    public Instant getFinished() {
        return finished;
    }

    public void setFinished(Instant finished) {
        this.finished = finished;
    }

    public int getRulesVersion() {
        return rulesVersion;
    }

    public Map<String, Object> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, Object> inputs) {
        this.inputs = inputs;
    }

    public long getNodes() {
        return nodes;
    }

    public void setNodes(long nodes) {
        this.nodes = nodes;
    }

    public Map<String, Object> getEdgesByMethod() {
        return edgesByMethod;
    }

    public void setEdgesByMethod(Map<String, Object> edgesByMethod) {
        this.edgesByMethod = edgesByMethod;
    }

    public Map<String, Object> getEdgesByLabel() {
        return edgesByLabel;
    }

    public void setEdgesByLabel(Map<String, Object> edgesByLabel) {
        this.edgesByLabel = edgesByLabel;
    }

    public java.util.List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(java.util.List<String> warnings) {
        this.warnings = warnings == null ? new java.util.ArrayList<>() : warnings;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    /** The modification time of the imported OSM extract, or null. */
    public Instant getOsmExtractDate() {
        return osmExtractDate;
    }

    /** Records the modification time of the imported OSM extract. */
    public void setOsmExtractDate(Instant osmExtractDate) {
        this.osmExtractDate = osmExtractDate;
    }

    /** {@code running}, {@code done} or {@code failed}. */
    public String status() {
        if (error != null) {
            return "failed";
        }
        return finished == null ? "running" : "done";
    }
}
