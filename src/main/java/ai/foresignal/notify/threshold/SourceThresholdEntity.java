package ai.foresignal.notify.threshold;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * A user's per-source notification threshold ({@code source_threshold}, prompt SG-003): the minimum alert severity
 * from the source that notifies, plus mute and an optional cooldown. Keyed on the owner's principal and the source,
 * matching the alert enforcement path.
 */
@Entity
@Table(name = "source_threshold")
@IdClass(SourceThresholdId.class)
public class SourceThresholdEntity {

    @Id
    @Column(name = "owner_principal_id", length = 64, nullable = false)
    private String ownerPrincipalId;

    @Id
    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "min_severity", length = 16, nullable = false)
    private String minSeverity;

    @Column(name = "mute", nullable = false)
    private boolean mute;

    /** Seconds between notifications from this source, or null for none. */
    @Column(name = "cooldown_seconds")
    private Long cooldownSeconds;

    /** For JPA. */
    protected SourceThresholdEntity() {}

    /** A new entry for {@code ownerPrincipalId} and {@code sourceId}. */
    public SourceThresholdEntity(String ownerPrincipalId, String sourceId) {
        this.ownerPrincipalId = ownerPrincipalId;
        this.sourceId = sourceId;
    }

    /** The owning user's principal. */
    public String getOwnerPrincipalId() {
        return ownerPrincipalId;
    }

    /** The source. */
    public String getSourceId() {
        return sourceId;
    }

    /** The minimum notify severity wire value. */
    public String getMinSeverity() {
        return minSeverity;
    }

    /** Sets the minimum notify severity. */
    public void setMinSeverity(String minSeverity) {
        this.minSeverity = minSeverity;
    }

    /** Whether the source is muted. */
    public boolean isMute() {
        return mute;
    }

    /** Sets the mute flag. */
    public void setMute(boolean mute) {
        this.mute = mute;
    }

    /** The cooldown in seconds, or null for none. */
    public Long getCooldownSeconds() {
        return cooldownSeconds;
    }

    /** Sets the cooldown in seconds; null for none. */
    public void setCooldownSeconds(Long cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }
}
