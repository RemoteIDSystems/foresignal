package ai.foresignal.exercise;

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
 * One exercise replay run (prompt 115, table {@code exercise_run}): which rules replayed which retained window
 * at which speed to which channels, its status and statistics.
 */
@Entity
@Table(name = "exercise_run", indexes = @Index(name = "exercise_run_status_idx", columnList = "status, created_at"))
public class ExerciseRunEntity {

    /** Not started yet. */
    public static final String PLANNED = "PLANNED";

    /** Replaying. */
    public static final String RUNNING = "RUNNING";

    /** Finished. */
    public static final String DONE = "DONE";

    /** Stopped by the operator. */
    public static final String CANCELED = "CANCELLED";

    /** Stopped by an error (or a restart while running). */
    public static final String FAILED = "FAILED";

    /** The status values, in lifecycle order. */
    public static final List<String> STATUSES = List.of(PLANNED, RUNNING, DONE, CANCELED, FAILED);

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rule_ids")
    private List<String> ruleIds = new ArrayList<>();

    @Column(name = "tag", length = 64)
    private String tag;

    @Column(name = "range_from")
    private Instant rangeFrom;

    @Column(name = "range_to")
    private Instant rangeTo;

    @Column(name = "from_seq")
    private Long fromSeq;

    @Column(name = "to_seq")
    private Long toSeq;

    @Column(name = "speed", length = 16, nullable = false)
    private String speed = ExerciseSpeed.X60.wire();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "channel_ids")
    private List<String> channelIds = new ArrayList<>();

    @Column(name = "enrich", nullable = false, columnDefinition = "boolean default false")
    private boolean enrich;

    @Column(name = "status", length = 16, nullable = false)
    private String status = PLANNED;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "stats")
    private Map<String, Object> stats = new LinkedHashMap<>();

    protected ExerciseRunEntity() {
        // for JPA
    }

    /** A planned run with {@code id} and {@code name}. */
    public ExerciseRunEntity(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /** The id (a ULID). */
    public String getId() {
        return id;
    }

    /** The name. */
    public String getName() {
        return name;
    }

    /** The replayed rules (resolved from the tag when one was given). */
    public List<String> getRuleIds() {
        return ruleIds == null ? List.of() : ruleIds;
    }

    /** Sets the replayed rules. */
    public void setRuleIds(List<String> ruleIds) {
        this.ruleIds = ruleIds == null ? new ArrayList<>() : new ArrayList<>(ruleIds);
    }

    /** The rule tag the run was created with, or null. */
    public String getTag() {
        return tag;
    }

    /** Sets the tag. */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /** The start of the window in time. */
    public Instant getRangeFrom() {
        return rangeFrom;
    }

    /** Sets the start of the window in time. */
    public void setRangeFrom(Instant rangeFrom) {
        this.rangeFrom = rangeFrom;
    }

    /** The end of the window in time. */
    public Instant getRangeTo() {
        return rangeTo;
    }

    /** Sets the end of the window in time. */
    public void setRangeTo(Instant rangeTo) {
        this.rangeTo = rangeTo;
    }

    /** The exclusive lower seq bound. */
    public Long getFromSeq() {
        return fromSeq;
    }

    /** Sets the exclusive lower seq bound. */
    public void setFromSeq(Long fromSeq) {
        this.fromSeq = fromSeq;
    }

    /** The inclusive upper seq bound. */
    public Long getToSeq() {
        return toSeq;
    }

    /** Sets the inclusive upper seq bound. */
    public void setToSeq(Long toSeq) {
        this.toSeq = toSeq;
    }

    /** The speed wire value. */
    public String getSpeed() {
        return speed;
    }

    /** Sets the speed wire value. */
    public void setSpeed(String speed) {
        this.speed = speed;
    }

    /** The channels named in the run. */
    public List<String> getChannelIds() {
        return channelIds == null ? List.of() : channelIds;
    }

    /** Sets the channels named in the run. */
    public void setChannelIds(List<String> channelIds) {
        this.channelIds = channelIds == null ? new ArrayList<>() : new ArrayList<>(channelIds);
    }

    /** Whether 030 enrichment may run for the run's alerts. */
    public boolean isEnrich() {
        return enrich;
    }

    /** Sets whether enrichment may run. */
    public void setEnrich(boolean enrich) {
        this.enrich = enrich;
    }

    /** The status. */
    public String getStatus() {
        return status;
    }

    /** Sets the status. */
    public void setStatus(String status) {
        this.status = status;
    }

    /** Who created the run. */
    public String getCreatedBy() {
        return createdBy;
    }

    /** Sets who created the run. */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /** When the run was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Sets when the run was created. */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /** When the replay started, or null. */
    public Instant getStartedAt() {
        return startedAt;
    }

    /** Sets when the replay started. */
    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    /** When the replay finished, or null. */
    public Instant getFinishedAt() {
        return finishedAt;
    }

    /** Sets when the replay finished. */
    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    /** The statistics ({@code ExerciseStats}). */
    public Map<String, Object> getStats() {
        return stats == null ? Map.of() : stats;
    }

    /** Sets the statistics. */
    public void setStats(Map<String, Object> stats) {
        this.stats = stats == null ? new LinkedHashMap<>() : new LinkedHashMap<>(stats);
    }

    /** True while the run is planned or replaying. */
    public boolean active() {
        return PLANNED.equals(status) || RUNNING.equals(status);
    }
}
