package ai.foresignal.benchmark;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One benchmark run (prompt 123, table {@code benchmark_run}): the suite, the scenario generator and scorer versions,
 * the seed and parameters, the components and their versions, the per-component results ({@code
 * BenchmarkComponentResult} objects), the worst status, the 116 manifest and the times. Its id is the id of the 103 job
 * ({@code type=benchmark}) that runs it, whose status and progress the API reports.
 */
@Entity
@Table(name = "benchmark_run", indexes = {
    @Index(name = "benchmark_run_finished_idx", columnList = "finished_at"),
    @Index(name = "benchmark_run_suite_idx", columnList = "suite, id")
})
public class BenchmarkRunEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "suite", length = 16, nullable = false)
    private String suite;

    @Column(name = "suite_version", length = 64, nullable = false)
    private String suiteVersion;

    @Column(name = "scorer_version", length = 64, nullable = false)
    private String scorerVersion;

    @Column(name = "thresholds_version", length = 32)
    private String thresholdsVersion;

    @Column(name = "seed", nullable = false)
    private long seed;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "params", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> params = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "components", columnDefinition = "jsonb", nullable = false)
    private List<String> components = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "component_versions", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> componentVersions = new LinkedHashMap<>();

    @Column(name = "status", length = 8)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "results", columnDefinition = "jsonb", nullable = false)
    private List<Map<String, Object>> results = new ArrayList<>();

    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    @Column(name = "run_trigger", length = 32, nullable = false)
    private String trigger;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "error", columnDefinition = "text")
    private String error;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    /** For JPA. */
    protected BenchmarkRunEntity() {}

    /**
     * Creates a queued run.
     *
     * @param id the id (the 103 job's id)
     * @param suite {@code synthetic}, {@code historical} or {@code all}
     * @param createdAt when it was requested
     */
    public BenchmarkRunEntity(String id, String suite, Instant createdAt) {
        this.id = id;
        this.suite = suite;
        this.createdAt = createdAt;
    }

    /** The id (the job's id). */
    public String getId() {
        return id;
    }

    /** The suite. */
    public String getSuite() {
        return suite;
    }

    /** The scenario generator's name and version. */
    public String getSuiteVersion() {
        return suiteVersion;
    }

    /** Sets the scenario generator's name and version. */
    public void setSuiteVersion(String suiteVersion) {
        this.suiteVersion = suiteVersion;
    }

    /** The scorer's name and version. */
    public String getScorerVersion() {
        return scorerVersion;
    }

    /** Sets the scorer's name and version. */
    public void setScorerVersion(String scorerVersion) {
        this.scorerVersion = scorerVersion;
    }

    /** The thresholds document's version. */
    public String getThresholdsVersion() {
        return thresholdsVersion;
    }

    /** Sets the thresholds document's version. */
    public void setThresholdsVersion(String thresholdsVersion) {
        this.thresholdsVersion = thresholdsVersion;
    }

    /** The seed. */
    public long getSeed() {
        return seed;
    }

    /** Sets the seed. */
    public void setSeed(long seed) {
        this.seed = seed;
    }

    /** The suite parameters (seed, replicates, bin size). */
    public Map<String, Object> getParams() {
        return params;
    }

    /** Sets the suite parameters. */
    public void setParams(Map<String, Object> params) {
        this.params = params == null ? new LinkedHashMap<>() : new LinkedHashMap<>(params);
    }

    /** The components benchmarked. */
    public List<String> getComponents() {
        return components;
    }

    /** Sets the components benchmarked. */
    public void setComponents(List<String> components) {
        this.components = components == null ? new ArrayList<>() : new ArrayList<>(components);
    }

    /** Each component's version when the run was requested. */
    public Map<String, Object> getComponentVersions() {
        return componentVersions;
    }

    /** Sets each component's version. */
    public void setComponentVersions(Map<String, Object> componentVersions) {
        this.componentVersions = componentVersions == null ? new LinkedHashMap<>()
                : new LinkedHashMap<>(componentVersions);
    }

    /** The worst component status ({@code pass}, {@code warn}, {@code fail}), null until done. */
    public String getStatus() {
        return status;
    }

    /** Sets the worst component status. */
    public void setStatus(String status) {
        this.status = status;
    }

    /** The {@code BenchmarkComponentResult} objects. */
    public List<Map<String, Object>> getResults() {
        return results;
    }

    /** Sets the results. */
    public void setResults(List<Map<String, Object>> results) {
        this.results = results == null ? new ArrayList<>() : new ArrayList<>(results);
    }

    /** The 116 manifest, or null. */
    public String getManifestId() {
        return manifestId;
    }

    /** Sets the 116 manifest. */
    public void setManifestId(String manifestId) {
        this.manifestId = manifestId;
    }

    /** What started it: {@code manual}, {@code agent}, {@code schedule} or {@code version-change}. */
    public String getTrigger() {
        return trigger;
    }

    /** Sets what started it. */
    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    /** The principal that asked, or {@code scheduler}. */
    public String getCreatedBy() {
        return createdBy;
    }

    /** Sets the principal that asked. */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /** The error of a failed run, or null. */
    public String getError() {
        return error;
    }

    /** Sets the error. */
    public void setError(String error) {
        this.error = error;
    }

    /** When it was requested. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it started, or null. */
    public Instant getStartedAt() {
        return startedAt;
    }

    /** Sets when it started. */
    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    /** When it finished, or null. */
    public Instant getFinishedAt() {
        return finishedAt;
    }

    /** Sets when it finished. */
    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }
}
