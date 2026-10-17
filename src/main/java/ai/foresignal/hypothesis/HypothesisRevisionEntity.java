package ai.foresignal.hypothesis;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One append-only revision of a hypothesis set (prompt 125, table {@code hypothesis_revision}): the only way a
 * probability changes (ICD 203 standard 7). It holds the cause, the actor, {@code asof_seq}, the before and after state
 * of every hypothesis, the drivers with their log-LR contributions (suppressed ones included), citations, the change
 * rationale relative to the previous revision, whether a band changed, whether it includes a logged operator
 * override, the ledger forecast it produced and the updater version. Every column is {@code updatable = false}.
 */
@Entity
@Table(name = "hypothesis_revision", indexes = @Index(name = "hypothesis_revision_set_idx",
        columnList = "set_id, at"))
public class HypothesisRevisionEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false, updatable = false)
    private String id;

    @Column(name = "set_id", length = 26, nullable = false, updatable = false)
    private String setId;

    @Column(name = "at", nullable = false, updatable = false)
    private Instant at;

    @Column(name = "cause", length = 32, nullable = false, updatable = false)
    private String cause;

    @Column(name = "actor", length = 128, nullable = false, updatable = false)
    private String actor;

    @Column(name = "asof_seq", nullable = false, updatable = false)
    private long asOfSeq;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "before_state", columnDefinition = "jsonb", nullable = false, updatable = false)
    private List<Map<String, Object>> before = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "after_state", columnDefinition = "jsonb", nullable = false, updatable = false)
    private List<Map<String, Object>> after = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "drivers", columnDefinition = "jsonb", nullable = false, updatable = false)
    private List<Map<String, Object>> drivers = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "citations", columnDefinition = "jsonb", nullable = false, updatable = false)
    private List<String> citations = new ArrayList<>();

    @Column(name = "rationale", columnDefinition = "text", nullable = false, updatable = false)
    private String rationale;

    @Column(name = "rationale_origin", length = 16, nullable = false, updatable = false)
    private String rationaleOrigin;

    @Column(name = "band_changed", nullable = false, updatable = false)
    private boolean bandChanged;

    @Column(name = "override", nullable = false, updatable = false)
    private boolean override;

    @Column(name = "ledger_forecast_id", length = 26, updatable = false)
    private String ledgerForecastId;

    @Column(name = "updater_version", length = 64, nullable = false, updatable = false)
    private String updaterVersion;

    /** For JPA. */
    protected HypothesisRevisionEntity() {}

    /**
     * Creates a revision.
     *
     * @param id the ULID
     * @param setId the set
     * @param at when it was written
     * @param cause the cause
     * @param actor who or what caused it
     * @param asOfSeq the record sequence it saw
     */
    public HypothesisRevisionEntity(String id, String setId, Instant at, String cause, String actor, long asOfSeq) {
        this.id = id;
        this.setId = setId;
        this.at = at;
        this.cause = cause;
        this.actor = actor;
        this.asOfSeq = asOfSeq;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The set. */
    public String getSetId() {
        return setId;
    }

    /** When it was written. */
    public Instant getAt() {
        return at;
    }

    /** The cause. */
    public String getCause() {
        return cause;
    }

    /** The actor. */
    public String getActor() {
        return actor;
    }

    /** The record sequence it saw. */
    public long getAsOfSeq() {
        return asOfSeq;
    }

    /** The state before ({@code HypothesisProbabilityState} maps). */
    public List<Map<String, Object>> getBefore() {
        return before;
    }

    /** Sets the state before. */
    public void setBefore(List<Map<String, Object>> value) {
        this.before = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** The state after. */
    public List<Map<String, Object>> getAfter() {
        return after;
    }

    /** Sets the state after. */
    public void setAfter(List<Map<String, Object>> value) {
        this.after = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** The drivers ({@code RevisionDriver} maps). */
    public List<Map<String, Object>> getDrivers() {
        return drivers;
    }

    /** Sets the drivers. */
    public void setDrivers(List<Map<String, Object>> value) {
        this.drivers = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** The citations (object refs). */
    public List<String> getCitations() {
        return citations;
    }

    /** Sets the citations. */
    public void setCitations(List<String> value) {
        this.citations = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** The change rationale. */
    public String getRationale() {
        return rationale;
    }

    /** Sets the change rationale. */
    public void setRationale(String value) {
        this.rationale = value;
    }

    /** {@code deterministic}, {@code operator} or {@code analyst}. */
    public String getRationaleOrigin() {
        return rationaleOrigin;
    }

    /** Sets the rationale origin. */
    public void setRationaleOrigin(String value) {
        this.rationaleOrigin = value;
    }

    /** True when some hypothesis crossed an ICD 203 band. */
    public boolean isBandChanged() {
        return bandChanged;
    }

    /** Sets the band-changed flag. */
    public void setBandChanged(boolean value) {
        this.bandChanged = value;
    }

    /** True when the revision includes a logged operator override. */
    public boolean isOverride() {
        return override;
    }

    /** Sets the override flag. */
    public void setOverride(boolean value) {
        this.override = value;
    }

    /** The ledger forecast this revision recorded, or null. */
    public String getLedgerForecastId() {
        return ledgerForecastId;
    }

    /** Sets the ledger forecast. */
    public void setLedgerForecastId(String value) {
        this.ledgerForecastId = value;
    }

    /** The updater version ({@code indicator-bayes 1.0.0}). */
    public String getUpdaterVersion() {
        return updaterVersion;
    }

    /** Sets the updater version. */
    public void setUpdaterVersion(String value) {
        this.updaterVersion = value;
    }
}
