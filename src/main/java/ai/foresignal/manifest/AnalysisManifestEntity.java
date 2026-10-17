package ai.foresignal.manifest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One analysis manifest (prompt 116, table {@code analysis_manifest}); {@code content_hash} is unique, so
 * identical manifests are stored once. Never deleted by retention.
 */
@Entity
@Table(name = "analysis_manifest",
        uniqueConstraints = @UniqueConstraint(name = "analysis_manifest_content_hash_key", columnNames = "content_hash"),
        indexes = @Index(name = "analysis_manifest_component_idx", columnList = "component, created_at"))
public class AnalysisManifestEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "component", length = 128, nullable = false)
    private String component;

    @Column(name = "component_version", length = 64, nullable = false)
    private String componentVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "build", nullable = false)
    private Map<String, Object> build;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config", nullable = false)
    private Map<String, Object> config;

    @Column(name = "config_hash", length = 64, nullable = false)
    private String configHash;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "registries", nullable = false)
    private Map<String, Object> registries;

    @Column(name = "asof_seq")
    private Long asofSeq;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "inputs", nullable = false)
    private Map<String, Object> inputs;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "model")
    private Map<String, Object> model;

    @Column(name = "content_hash", length = 64, nullable = false)
    private String contentHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** For JPA. */
    protected AnalysisManifestEntity() {}

    /** The id. */
    public String getId() {
        return id;
    }

    /** The component, e.g. {@code poisson-tail}. */
    public String getComponent() {
        return component;
    }

    /** The component's version. */
    public String getComponentVersion() {
        return componentVersion;
    }

    /** {@code {version, gitCommit}}. */
    public Map<String, Object> getBuild() {
        return build;
    }

    /** Parameters and thresholds. */
    public Map<String, Object> getConfig() {
        return config;
    }

    /** The SHA-256 of the canonical configuration. */
    public String getConfigHash() {
        return configHash;
    }

    /** Registry file name → SHA-256. */
    public Map<String, Object> getRegistries() {
        return registries;
    }

    /** The {@code asOfSeq}, or null. */
    public Long getAsofSeq() {
        return asofSeq;
    }

    /** {@code {refs | query, count, hash}}. */
    public Map<String, Object> getInputs() {
        return inputs;
    }

    /** LLM metadata, or null. */
    public Map<String, Object> getModel() {
        return model;
    }

    /** The SHA-256 of the canonical content. */
    public String getContentHash() {
        return contentHash;
    }

    /** When first stored. */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
