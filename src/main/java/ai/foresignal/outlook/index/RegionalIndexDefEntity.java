package ai.foresignal.outlook.index;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One row of {@code regional_index_def} (prompt 126): a transparent regional index. {@code components} lists
 * {@code {key, ref (a SeriesRef template with {region}), transform, window, direction, floor?}}; {@code weights}
 * maps component keys to weights summing to 1; {@code floors} lists rules such as "a UCDP war in the last 12 months
 * keeps the index at 70 or more"; {@code target} is the outlook family (or {@code finding:{type}:{family}}) the index
 * should anticipate. {@code weights_origin} is {@code default}, {@code fitted} or {@code operator}; every weights
 * change bumps {@code weights_version} and is appended to {@code weights_log}. {@code skill} is the latest
 * out-of-sample skill of the index's implied count forecast.
 */
@Entity
@Table(name = "regional_index_def")
public class RegionalIndexDefEntity {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "components", columnDefinition = "jsonb")
    private List<Map<String, Object>> components = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "weights", columnDefinition = "jsonb")
    private Map<String, Double> weights = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "default_weights", columnDefinition = "jsonb")
    private Map<String, Double> defaultWeights = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "floors", columnDefinition = "jsonb")
    private List<Map<String, Object>> floors = new ArrayList<>();

    @Column(name = "target", length = 128, nullable = false)
    private String target;

    @Column(name = "weights_version", nullable = false)
    private int weightsVersion = 1;

    @Column(name = "weights_origin", length = 16, nullable = false)
    private String weightsOrigin = "default";

    @Column(name = "auto_apply_fitted_weights", nullable = false)
    private boolean autoApplyFittedWeights;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "built_in", nullable = false)
    private boolean builtIn;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "skill", columnDefinition = "jsonb")
    private Map<String, Object> skill;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "weights_log", columnDefinition = "jsonb")
    private List<Map<String, Object>> weightsLog = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "row_version", nullable = false)
    private long rowVersion;

    /** JPA. */
    protected RegionalIndexDefEntity() {}

    /** Creates a definition. */
    public RegionalIndexDefEntity(String id, Instant now) {
        this.id = id;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The name. */
    public String getName() {
        return name;
    }

    /** Sets the name. */
    public void setName(String name) {
        this.name = name;
    }

    /** The description, or null. */
    public String getDescription() {
        return description;
    }

    /** Sets the description. */
    public void setDescription(String description) {
        this.description = description;
    }

    /** The components. */
    public List<Map<String, Object>> getComponents() {
        return components;
    }

    /** Sets the components. */
    public void setComponents(List<Map<String, Object>> components) {
        List<Map<String, Object>> copy = new ArrayList<>();
        for (Map<String, Object> c : components) {
            copy.add(new LinkedHashMap<>(c));
        }
        this.components = copy;
    }

    /** The weights in force. */
    public Map<String, Double> getWeights() {
        return weights;
    }

    /** Sets the weights in force. */
    public void setWeights(Map<String, Double> weights) {
        this.weights = new LinkedHashMap<>(weights);
    }

    /** The definition's default weights. */
    public Map<String, Double> getDefaultWeights() {
        return defaultWeights;
    }

    /** Sets the default weights. */
    public void setDefaultWeights(Map<String, Double> defaultWeights) {
        this.defaultWeights = new LinkedHashMap<>(defaultWeights);
    }

    /** The floor rules. */
    public List<Map<String, Object>> getFloors() {
        return floors;
    }

    /** Sets the floor rules. */
    public void setFloors(List<Map<String, Object>> floors) {
        this.floors = floors == null ? new ArrayList<>() : new ArrayList<>(floors);
    }

    /** The target outlook family. */
    public String getTarget() {
        return target;
    }

    /** Sets the target. */
    public void setTarget(String target) {
        this.target = target;
    }

    /** The weights version. */
    public int getWeightsVersion() {
        return weightsVersion;
    }

    /** Sets the weights version. */
    public void setWeightsVersion(int weightsVersion) {
        this.weightsVersion = weightsVersion;
    }

    /** {@code default}, {@code fitted} or {@code operator}. */
    public String getWeightsOrigin() {
        return weightsOrigin;
    }

    /** Sets the weights origin. */
    public void setWeightsOrigin(String weightsOrigin) {
        this.weightsOrigin = weightsOrigin;
    }

    /** True to apply fitted weights automatically. */
    public boolean isAutoApplyFittedWeights() {
        return autoApplyFittedWeights;
    }

    /** Sets automatic application of fitted weights. */
    public void setAutoApplyFittedWeights(boolean autoApplyFittedWeights) {
        this.autoApplyFittedWeights = autoApplyFittedWeights;
    }

    /** True when computed daily. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Sets whether it is computed. */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** True for a built-in definition. */
    public boolean isBuiltIn() {
        return builtIn;
    }

    /** Sets whether it is built in. */
    public void setBuiltIn(boolean builtIn) {
        this.builtIn = builtIn;
    }

    /** The latest out-of-sample skill, or null. */
    public Map<String, Object> getSkill() {
        return skill;
    }

    /** Sets the skill. */
    public void setSkill(Map<String, Object> skill) {
        this.skill = skill == null ? null : new LinkedHashMap<>(skill);
    }

    /** The weights changes, oldest first. */
    public List<Map<String, Object>> getWeightsLog() {
        return weightsLog;
    }

    /** Appends a weights change. */
    public void logWeights(Map<String, Object> entry) {
        List<Map<String, Object>> copy = new ArrayList<>(weightsLog == null ? List.of() : weightsLog);
        copy.add(new LinkedHashMap<>(entry));
        this.weightsLog = copy;
    }

    /** When it was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets when it last changed. */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
