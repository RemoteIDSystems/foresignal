package ai.foresignal.store;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.MultiPolygon;

/**
 * A rule evaluated against the change stream (Architecture §8).
 *
 * <p>A source that is subscribed but not displayed can still announce itself, which is the point
 * of separating alerting from display.
 */
@Entity
@Table(name = "trigger_rule")
public class TriggerRuleEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    /** Null means "any source". */
    @Column(name = "source_id", length = 64)
    private String sourceId;

    /** Null means "any kind". */
    @Column(name = "kind", length = 64)
    private String kind;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "conditions", nullable = false)
    private List<TriggerCondition> conditions = new ArrayList<>();

    /** Null means the rule is not geographically constrained. */
    @Column(name = "geofence", columnDefinition = "geometry(MultiPolygon,4326)")
    private MultiPolygon geofence;

    @Convert(converter = TriggerModeConverter.class)
    @Column(name = "mode", nullable = false, columnDefinition = "varchar(8) default 'match'")
    private TriggerMode mode = TriggerMode.MATCH;

    /** Fire only for a record's first version, ignoring later revisions. */
    @Column(name = "only_new", nullable = false)
    private boolean onlyNew;

    /** Suppress repeat alerts for the same record for this many seconds; 0 disables it. */
    @Column(name = "cooldown_seconds", nullable = false, columnDefinition = "integer default 0")
    private int cooldownSeconds;

    @Column(name = "message_template", columnDefinition = "text")
    private String messageTemplate;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected TriggerRuleEntity() {
        // for JPA
    }

    public TriggerRuleEntity(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public List<TriggerCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<TriggerCondition> conditions) {
        this.conditions = conditions == null ? new ArrayList<>() : conditions;
    }

    public MultiPolygon getGeofence() {
        return geofence;
    }

    public void setGeofence(MultiPolygon geofence) {
        this.geofence = geofence;
    }

    public TriggerMode getMode() {
        return mode;
    }

    public void setMode(TriggerMode mode) {
        this.mode = mode == null ? TriggerMode.MATCH : mode;
    }

    public boolean isOnlyNew() {
        return onlyNew;
    }

    public void setOnlyNew(boolean onlyNew) {
        this.onlyNew = onlyNew;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public void setCooldownSeconds(int cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
