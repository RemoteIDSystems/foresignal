package ai.foresignal.inbound.edp.binding;

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
 * A binding of an EDP stream to a destination (prompt 138, table {@code external_binding}): the target, its
 * configuration, an optional 017 filter on item properties, the manifest version it was made for, and its state and
 * counters. {@link BindingRouter} applies enabled bindings to matching records.
 */
@Entity
@Table(name = "external_binding", indexes = {
        @Index(name = "external_binding_source_idx", columnList = "source_id, stream_id"),
        @Index(name = "external_binding_target_idx", columnList = "target")})
public class ExternalBindingEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "stream_id", length = 48, nullable = false)
    private String streamId;

    @Column(name = "target", length = 32, nullable = false)
    private String target;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config")
    private Map<String, Object> config;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "filter")
    private List<Object> filter;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "backfill", nullable = false)
    private boolean backfill = true;

    @Column(name = "state", length = 16, nullable = false)
    private String state = BindingVocabulary.ACTIVE;

    @Column(name = "applied", nullable = false)
    private long applied;

    @Column(name = "last_error", length = 2000)
    private String lastError;

    @Column(name = "target_ref", length = 600)
    private String targetRef;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "latest")
    private Map<String, Object> latest;

    @Column(name = "manifest_sha256", length = 64)
    private String manifestSha256;

    @Column(name = "created_by", length = 200, nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "last_applied_at")
    private Instant lastAppliedAt;

    /** For JPA. */
    protected ExternalBindingEntity() {}

    /** A new binding. */
    public ExternalBindingEntity(String id, String sourceId, String streamId, String target, String createdBy,
            Instant now) {
        this.id = id;
        this.sourceId = sourceId;
        this.streamId = streamId;
        this.target = target;
        this.createdBy = createdBy;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The provider ({@code ext-…}). */
    public String getSourceId() {
        return sourceId;
    }

    /** The stream id. */
    public String getStreamId() {
        return streamId;
    }

    /** Sets the stream id. */
    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    /** The target. */
    public String getTarget() {
        return target;
    }

    /** Sets the target. */
    public void setTarget(String target) {
        this.target = target;
    }

    /** The target configuration (never null). */
    public Map<String, Object> getConfig() {
        return config == null ? Map.of() : config;
    }

    /** Sets the target configuration. */
    public void setConfig(Map<String, Object> config) {
        this.config = config == null ? null : new LinkedHashMap<>(config);
    }

    /** The 017 filter conditions, or null. */
    public List<Object> getFilter() {
        return filter;
    }

    /** Sets the filter. */
    public void setFilter(List<Object> filter) {
        this.filter = filter == null ? null : new ArrayList<>(filter);
    }

    /** True when the operator has the binding switched on. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Switches the binding on or off. */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** True when it was asked to backfill from stored records. */
    public boolean isBackfill() {
        return backfill;
    }

    /** Sets the backfill flag. */
    public void setBackfill(boolean backfill) {
        this.backfill = backfill;
    }

    /** {@code active}, {@code disabled}, {@code backfilling} or {@code error}. */
    public String getState() {
        return state;
    }

    /** Sets the state. */
    public void setState(String state) {
        this.state = state;
    }

    /** How many record versions it applied. */
    public long getApplied() {
        return applied;
    }

    /** Sets the applied count. */
    public void setApplied(long applied) {
        this.applied = applied;
    }

    /** The last error, or null. */
    public String getLastError() {
        return lastError;
    }

    /** Sets the last error (trimmed to 2,000 characters). */
    public void setLastError(String lastError) {
        this.lastError = lastError == null || lastError.length() <= 2000 ? lastError : lastError.substring(0, 2000);
    }

    /** What the binding created at set-up (the chronicle layer of a {@code chronicle} binding), or null. */
    public String getTargetRef() {
        return targetRef;
    }

    /** Sets the set-up target reference. */
    public void setTargetRef(String targetRef) {
        this.targetRef = targetRef;
    }

    /** The latest applied item summary (the {@code place} target's value), or null. */
    public Map<String, Object> getLatest() {
        return latest;
    }

    /** Sets the latest applied item summary. */
    public void setLatest(Map<String, Object> latest) {
        this.latest = latest == null ? null : new LinkedHashMap<>(latest);
    }

    /** The SHA-256 of the manifest the binding was made for. */
    public String getManifestSha256() {
        return manifestSha256;
    }

    /** Sets the manifest SHA-256. */
    public void setManifestSha256(String manifestSha256) {
        this.manifestSha256 = manifestSha256;
    }

    /** Who created it. */
    public String getCreatedBy() {
        return createdBy;
    }

    /** When it was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets the update time. */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** When it last applied a record, or null. */
    public Instant getLastAppliedAt() {
        return lastAppliedAt;
    }

    /** Sets when it last applied a record. */
    public void setLastAppliedAt(Instant lastAppliedAt) {
        this.lastAppliedAt = lastAppliedAt;
    }

    /** True when the router applies it: enabled and not disabled or in error. */
    public boolean routable() {
        return enabled && (BindingVocabulary.ACTIVE.equals(state) || BindingVocabulary.BACKFILLING.equals(state));
    }
}
