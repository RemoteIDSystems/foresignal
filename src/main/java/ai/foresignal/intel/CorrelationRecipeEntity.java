package ai.foresignal.intel;

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
 * A correlation recipe (prompt 101): its versioned definition and any operator parameter overrides.
 * Built-in recipes load from {@code src/main/resources/intel/recipes/*.json} and are refreshed on
 * start when the bundled version is newer; operator overrides are kept.
 */
@Entity
@Table(name = "correlation_recipe")
public class CorrelationRecipeEntity {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "version", nullable = false)
    private int version = 1;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "definition", nullable = false)
    private Map<String, Object> definition = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "params")
    private Map<String, Object> params;

    @Column(name = "last_run_at")
    private Instant lastRunAt;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    /** The manifest of the last run that produced candidates (prompt 116), or null. */
    @Column(name = "last_manifest_id", length = 26)
    private String lastManifestId;

    /** JPA. */
    protected CorrelationRecipeEntity() {}

    /** Creates a recipe. */
    public CorrelationRecipeEntity(String id, int version, Map<String, Object> definition) {
        this.id = id;
        this.version = version;
        this.definition = definition;
    }

    public String getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, Object> getDefinition() {
        return definition;
    }

    public void setDefinition(Map<String, Object> definition) {
        this.definition = definition;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public Instant getLastRunAt() {
        return lastRunAt;
    }

    public void setLastRunAt(Instant lastRunAt) {
        this.lastRunAt = lastRunAt;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    /** The manifest of the last run that produced candidates (prompt 116), or null. */
    public String getLastManifestId() {
        return lastManifestId;
    }

    /** Sets the manifest of the last productive run (prompt 116). */
    public void setLastManifestId(String lastManifestId) {
        this.lastManifestId = lastManifestId;
    }
}
