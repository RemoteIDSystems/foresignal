package ai.foresignal.forecast;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A registered forecaster (prompt 122, table {@code forecaster}): a slug id ({@code base-rate}, {@code ensemble},
 * {@code operator:jsmith}, {@code llm:daily-analyst}), its type, a display name, its configuration, the version of
 * the component that produces its forecasts and whether it is active.
 */
@Entity
@Table(name = "forecaster")
public class ForecasterEntity {

    @Id
    @Column(name = "id", length = 128, nullable = false)
    private String id;

    @Column(name = "type", length = 32, nullable = false)
    private String type;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> config = new LinkedHashMap<>();

    @Column(name = "version", length = 32, nullable = false)
    private String version;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** For JPA. */
    protected ForecasterEntity() {}

    /**
     * Creates an active forecaster.
     *
     * @param id the slug
     * @param type the type
     * @param name the display name
     * @param version the version
     * @param now the creation time
     */
    public ForecasterEntity(String id, String type, String name, String version, Instant now) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.version = version;
        this.active = true;
        this.createdAt = now;
    }

    /** The slug. */
    public String getId() {
        return id;
    }

    /** The type. */
    public String getType() {
        return type;
    }

    /** The display name. */
    public String getName() {
        return name;
    }

    /** Sets the display name. */
    public void setName(String value) {
        this.name = value;
    }

    /** The configuration. */
    public Map<String, Object> getConfig() {
        return config == null ? new LinkedHashMap<>() : config;
    }

    /** Replaces the configuration. */
    public void setConfig(Map<String, Object> value) {
        this.config = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
    }

    /** The version. */
    public String getVersion() {
        return version;
    }

    /** Sets the version. */
    public void setVersion(String value) {
        this.version = value;
    }

    /** Whether it is active. */
    public boolean isActive() {
        return active;
    }

    /** Sets whether it is active. */
    public void setActive(boolean value) {
        this.active = value;
    }

    /** When it was registered. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** A numeric configuration value, or {@code fallback}. */
    public double configNumber(String key, double fallback) {
        Object v = getConfig().get(key);
        return v instanceof Number n && Double.isFinite(n.doubleValue()) ? n.doubleValue() : fallback;
    }

    /** A text configuration value, or null. */
    public String configText(String key) {
        Object v = getConfig().get(key);
        return v == null ? null : v.toString();
    }
}
