package ai.foresignal.social;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * The operator's settings of one social platform adapter (prompt 135, table {@code social_platform}): enabled (the
 * operator's intent; the source itself stays off while credentials are missing), mode, channels, deletion sync, post
 * retention, money cap, prices and platform options, plus the last announced state. Budgets and credentials stay on
 * the 004/005 source ({@code source_settings}, {@code source_credential}).
 */
@Entity
@Table(name = "social_platform")
public class SocialPlatformEntity {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "mode", length = 32)
    private String mode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "channels")
    private List<String> channels = new ArrayList<>();

    @Column(name = "deletion_sync", nullable = false)
    private boolean deletionSync;

    @Column(name = "post_retention", length = 32)
    private String postRetention;

    @Column(name = "monthly_cap_usd", precision = 12, scale = 4)
    private BigDecimal monthlyCapUsd;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "prices")
    private List<Map<String, Object>> prices = new ArrayList<>();

    @Column(name = "prices_as_of", length = 10)
    private String pricesAsOf;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options")
    private Map<String, Object> options = new LinkedHashMap<>();

    @Column(name = "state", length = 32)
    private String state;

    @Column(name = "state_reason", length = 500)
    private String stateReason;

    @Column(name = "deletion_last_run_at")
    private Instant deletionLastRunAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected SocialPlatformEntity() {}

    /**
     * A new row.
     *
     * @param id the adapter id
     * @param updatedAt when created
     */
    public SocialPlatformEntity(String id, Instant updatedAt) {
        this.id = id;
        this.updatedAt = updatedAt;
    }

    /** The adapter id. */
    public String getId() {
        return id;
    }

    /** The operator's intent to collect. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Sets the intent to collect. */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** The mode, or null. */
    public String getMode() {
        return mode;
    }

    /** Sets the mode. */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /** The channels polled regardless of topics. */
    public List<String> getChannels() {
        return channels == null ? List.of() : channels;
    }

    /** Sets the channels. */
    public void setChannels(List<String> channels) {
        this.channels = channels == null ? new ArrayList<>() : new ArrayList<>(channels);
    }

    /** Whether deletion sync is on. */
    public boolean isDeletionSync() {
        return deletionSync;
    }

    /** Sets deletion sync. */
    public void setDeletionSync(boolean deletionSync) {
        this.deletionSync = deletionSync;
    }

    /** The post retention as written by the operator, or null for the default. */
    public String getPostRetention() {
        return postRetention;
    }

    /** Sets the post retention. */
    public void setPostRetention(String postRetention) {
        this.postRetention = postRetention;
    }

    /** The money cap, or null. */
    public BigDecimal getMonthlyCapUsd() {
        return monthlyCapUsd;
    }

    /** Sets the money cap. */
    public void setMonthlyCapUsd(BigDecimal monthlyCapUsd) {
        this.monthlyCapUsd = monthlyCapUsd;
    }

    /** The prices ({@code {item, usd, per}}). */
    public List<Map<String, Object>> getPrices() {
        return prices == null ? List.of() : prices;
    }

    /** Sets the prices. */
    public void setPrices(List<Map<String, Object>> prices) {
        this.prices = prices == null ? new ArrayList<>() : new ArrayList<>(prices);
    }

    /** The date the prices were taken, or null. */
    public String getPricesAsOf() {
        return pricesAsOf;
    }

    /** Sets the prices' date. */
    public void setPricesAsOf(String pricesAsOf) {
        this.pricesAsOf = pricesAsOf;
    }

    /** Platform options. */
    public Map<String, Object> getOptions() {
        return options == null ? Map.of() : options;
    }

    /** Sets the options. */
    public void setOptions(Map<String, Object> options) {
        this.options = options == null ? new LinkedHashMap<>() : new LinkedHashMap<>(options);
    }

    /** The last announced state, or null. */
    public String getState() {
        return state;
    }

    /** Sets the last announced state. */
    public void setState(String state) {
        this.state = state;
    }

    /** Why, or null. */
    public String getStateReason() {
        return stateReason;
    }

    /** Sets why. */
    public void setStateReason(String stateReason) {
        this.stateReason = stateReason == null || stateReason.length() <= 500 ? stateReason
                : stateReason.substring(0, 500);
    }

    /** When deletion sync last ran, or null. */
    public Instant getDeletionLastRunAt() {
        return deletionLastRunAt;
    }

    /** Sets when deletion sync last ran. */
    public void setDeletionLastRunAt(Instant deletionLastRunAt) {
        this.deletionLastRunAt = deletionLastRunAt;
    }

    /** When the settings last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets when the settings last changed. */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
