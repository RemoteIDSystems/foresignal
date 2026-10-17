package ai.foresignal.outlook.index;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One row of {@code regional_index_value} (prompt 126): an index's value for one region and day with its
 * decomposition ({@code components}: each component's raw window value, normalized value, weight and contribution,
 * which sum to the value before floors), the floors applied, the 24-hour and 7-day deltas, the weights version, the
 * seq read up to and a 116 manifest.
 */
@Entity
@Table(name = "regional_index_value", indexes = {
    @Index(name = "regional_index_value_def_region_idx", columnList = "def_id, region, day"),
    @Index(name = "regional_index_value_def_day_idx", columnList = "def_id, day")
}, uniqueConstraints = @UniqueConstraint(name = "regional_index_value_uq", columnNames = {"def_id", "region", "day"}))
public class RegionalIndexValueEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "def_id", length = 64, nullable = false)
    private String defId;

    @Column(name = "region", length = 16, nullable = false)
    private String region;

    @Column(name = "day", nullable = false)
    private LocalDate day;

    @Column(name = "value", nullable = false)
    private double value;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "components", columnDefinition = "jsonb")
    private List<Map<String, Object>> components = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "floors_applied", columnDefinition = "jsonb")
    private List<String> floorsApplied = new ArrayList<>();

    @Column(name = "delta_24h")
    private Double delta24h;

    @Column(name = "delta_7d")
    private Double delta7d;

    @Column(name = "weights_version", nullable = false)
    private int weightsVersion;

    @Column(name = "asof_seq", nullable = false)
    private long asofSeq;

    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt;

    /** JPA. */
    protected RegionalIndexValueEntity() {}

    /** Creates a row. */
    public RegionalIndexValueEntity(String id, String defId, String region, LocalDate day, double value,
            Instant computedAt) {
        this.id = id;
        this.defId = defId;
        this.region = region;
        this.day = day;
        this.value = value;
        this.computedAt = computedAt;
    }

    /** The id (ULID). */
    public String getId() {
        return id;
    }

    /** The index. */
    public String getDefId() {
        return defId;
    }

    /** The region. */
    public String getRegion() {
        return region;
    }

    /** The day. */
    public LocalDate getDay() {
        return day;
    }

    /** The value, 0–100. */
    public double getValue() {
        return value;
    }

    /** Sets the value. */
    public void setValue(double value) {
        this.value = value;
    }

    /** The decomposition. */
    public List<Map<String, Object>> getComponents() {
        return components;
    }

    /** Sets the decomposition. */
    public void setComponents(List<Map<String, Object>> components) {
        List<Map<String, Object>> copy = new ArrayList<>();
        for (Map<String, Object> c : components) {
            copy.add(new LinkedHashMap<>(c));
        }
        this.components = copy;
    }

    /** The floor rules applied. */
    public List<String> getFloorsApplied() {
        return floorsApplied;
    }

    /** Sets the floor rules applied. */
    public void setFloorsApplied(List<String> floorsApplied) {
        this.floorsApplied = new ArrayList<>(floorsApplied);
    }

    /** The change from the previous day, or null. */
    public Double getDelta24h() {
        return delta24h;
    }

    /** Sets the 24-hour delta. */
    public void setDelta24h(Double delta24h) {
        this.delta24h = delta24h;
    }

    /** The change from seven days earlier, or null. */
    public Double getDelta7d() {
        return delta7d;
    }

    /** Sets the 7-day delta. */
    public void setDelta7d(Double delta7d) {
        this.delta7d = delta7d;
    }

    /** The weights version used. */
    public int getWeightsVersion() {
        return weightsVersion;
    }

    /** Sets the weights version. */
    public void setWeightsVersion(int weightsVersion) {
        this.weightsVersion = weightsVersion;
    }

    /** The seq the inputs were read up to. */
    public long getAsofSeq() {
        return asofSeq;
    }

    /** Sets the seq. */
    public void setAsofSeq(long asofSeq) {
        this.asofSeq = asofSeq;
    }

    /** The 116 manifest, or null. */
    public String getManifestId() {
        return manifestId;
    }

    /** Sets the manifest. */
    public void setManifestId(String manifestId) {
        this.manifestId = manifestId;
    }

    /** When it was computed. */
    public Instant getComputedAt() {
        return computedAt;
    }

    /** Sets when it was computed. */
    public void setComputedAt(Instant computedAt) {
        this.computedAt = computedAt;
    }
}
