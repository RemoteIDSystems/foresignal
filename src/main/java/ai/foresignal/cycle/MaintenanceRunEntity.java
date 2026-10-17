package ai.foresignal.cycle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One monthly maintenance run (prompt 128 §5, table {@code maintenance_run}): the ordered steps, each with its status,
 * job id, result or error. A failing step never stops the later ones.
 */
@Entity
@Table(name = "maintenance_run")
public class MaintenanceRunEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "run_trigger", length = 16, nullable = false)
    private String trigger;

    @Column(name = "status", length = 16, nullable = false)
    private String status = "running";

    @Column(name = "cycle_run_id", length = 26)
    private String cycleRunId;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "steps", nullable = false)
    private List<Map<String, Object>> steps = new ArrayList<>();

    /** For JPA. */
    protected MaintenanceRunEntity() {}

    /** Creates a running maintenance run. */
    public MaintenanceRunEntity(String id, String trigger, Instant startedAt) {
        this.id = id;
        this.trigger = trigger;
        this.startedAt = startedAt;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** monthly-cycle, manual or schedule. */
    public String getTrigger() {
        return trigger;
    }

    /** running, done, partial or failed. */
    public String getStatus() {
        return status;
    }

    /** Sets the status. */
    public void setStatus(String status) {
        this.status = status;
    }

    /** The monthly cycle run that triggered it, or null. */
    public String getCycleRunId() {
        return cycleRunId;
    }

    /** Sets the cycle run. */
    public void setCycleRunId(String cycleRunId) {
        this.cycleRunId = cycleRunId;
    }

    /** Start time. */
    public Instant getStartedAt() {
        return startedAt;
    }

    /** Finish time, or null. */
    public Instant getFinishedAt() {
        return finishedAt;
    }

    /** Sets the finish time. */
    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    /** The steps in order. */
    public List<Map<String, Object>> getSteps() {
        return steps;
    }

    /** Sets the steps. */
    public void setSteps(List<Map<String, Object>> steps) {
        this.steps = steps == null ? new ArrayList<>() : new ArrayList<>(steps);
    }
}
