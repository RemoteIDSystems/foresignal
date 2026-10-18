package ai.foresignal.inbound.edp.binding;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * One record version a binding applied (prompt 138, table {@code external_binding_applied}): the idempotency key
 * {@code (binding, source, externalId, version)} hashed, so the router, a backfill and a restart apply each version
 * at most once per binding.
 */
@Entity
@Table(name = "external_binding_applied", indexes = {
        @Index(name = "external_binding_applied_binding_idx", columnList = "binding_id, applied_at")})
public class ExternalBindingAppliedEntity {

    @Id
    @Column(name = "applied_key", length = 64, nullable = false)
    private String key;

    @Column(name = "binding_id", length = 26, nullable = false)
    private String bindingId;

    @Column(name = "external_id", length = 600, nullable = false)
    private String externalId;

    @Column(name = "version")
    private Integer version;

    @Column(name = "seq", nullable = false)
    private long seq;

    @Column(name = "action", length = 64, nullable = false)
    private String action;

    @Column(name = "target_ref", length = 600)
    private String targetRef;

    @Column(name = "applied_at", nullable = false)
    private Instant appliedAt;

    /** For JPA. */
    protected ExternalBindingAppliedEntity() {}

    /** A new row. */
    public ExternalBindingAppliedEntity(String key, String bindingId, String externalId, Integer version, long seq,
            String action, String targetRef, Instant appliedAt) {
        this.key = key;
        this.bindingId = bindingId;
        this.externalId = externalId.length() <= 600 ? externalId : externalId.substring(0, 600);
        this.version = version;
        this.seq = seq;
        this.action = action == null ? "applied" : action.length() <= 64 ? action : action.substring(0, 64);
        this.targetRef = targetRef == null || targetRef.length() <= 600 ? targetRef : targetRef.substring(0, 600);
        this.appliedAt = appliedAt;
    }

    /** The idempotency key. */
    public String getKey() {
        return key;
    }

    /** The binding. */
    public String getBindingId() {
        return bindingId;
    }

    /** The record's external id. */
    public String getExternalId() {
        return externalId;
    }

    /** The record version, or null. */
    public Integer getVersion() {
        return version;
    }

    /** The version's seq. */
    public long getSeq() {
        return seq;
    }

    /** What was done. */
    public String getAction() {
        return action;
    }

    /** What it created or changed, or null. */
    public String getTargetRef() {
        return targetRef;
    }

    /** When it was applied. */
    public Instant getAppliedAt() {
        return appliedAt;
    }
}
