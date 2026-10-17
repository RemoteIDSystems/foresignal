package ai.foresignal.outlook.regional;

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
 * One row of {@code regional_outlook} (prompt 126): a weekly outlook of one region and family with its six 4-week
 * windows ({@code OutlookWindow}: quantiles, ledger question, and once resolved the actual count, whether it fell in
 * p10–p90, and any exceedance), the skill it was published with, and the reporting-density and detectability flags.
 * {@code updated_at} moves when a window resolves, so the {@code foresignal-outlook} record follows.
 */
@Entity
@Table(name = "regional_outlook", indexes = {
    @Index(name = "regional_outlook_region_family_idx", columnList = "region, family, published_at"),
    @Index(name = "regional_outlook_updated_idx", columnList = "updated_at"),
    @Index(name = "regional_outlook_latest_idx", columnList = "latest, family, region")
})
public class RegionalOutlookEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "region", length = 16, nullable = false)
    private String region;

    @Column(name = "region_level", length = 8, nullable = false)
    private String regionLevel;

    @Column(name = "family", length = 64, nullable = false)
    private String family;

    @Column(name = "category", length = 64, nullable = false)
    private String category;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;

    @Column(name = "model_version", length = 40, nullable = false)
    private String modelVersion;

    @Column(name = "asof_seq", nullable = false)
    private long asofSeq;

    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "windows", columnDefinition = "jsonb")
    private List<Map<String, Object>> windows = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "skill", columnDefinition = "jsonb")
    private Map<String, Object> skill;

    @Column(name = "low_reporting_density", nullable = false)
    private boolean lowReportingDensity;

    @Column(name = "low_detectability", nullable = false)
    private boolean lowDetectability;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "caveats", columnDefinition = "jsonb")
    private List<Map<String, Object>> caveats = new ArrayList<>();

    @Column(name = "source", length = 32)
    private String source;

    @Column(name = "latest", nullable = false)
    private boolean latest;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** JPA. */
    protected RegionalOutlookEntity() {}

    /** Creates a row. */
    public RegionalOutlookEntity(String id, String region, String regionLevel, String family, String category,
            Instant publishedAt, String modelVersion, long asofSeq) {
        this.id = id;
        this.region = region;
        this.regionLevel = regionLevel;
        this.family = family;
        this.category = category;
        this.publishedAt = publishedAt;
        this.modelVersion = modelVersion;
        this.asofSeq = asofSeq;
        this.updatedAt = publishedAt;
    }

    /** The id (ULID). */
    public String getId() {
        return id;
    }

    /** The region code. */
    public String getRegion() {
        return region;
    }

    /** {@code country} or {@code admin1}. */
    public String getRegionLevel() {
        return regionLevel;
    }

    /** The outlook family. */
    public String getFamily() {
        return family;
    }

    /** The ledger category. */
    public String getCategory() {
        return category;
    }

    /** When it was published. */
    public Instant getPublishedAt() {
        return publishedAt;
    }

    /** {@code regional-outlook 1.0.0}. */
    public String getModelVersion() {
        return modelVersion;
    }

    /** The seq the inputs were read up to. */
    public long getAsofSeq() {
        return asofSeq;
    }

    /** The 116 manifest, or null. */
    public String getManifestId() {
        return manifestId;
    }

    /** Sets the manifest. */
    public void setManifestId(String manifestId) {
        this.manifestId = manifestId;
    }

    /** The windows ({@code OutlookWindow} maps). */
    public List<Map<String, Object>> getWindows() {
        return windows;
    }

    /** Sets the windows. */
    public void setWindows(List<Map<String, Object>> windows) {
        List<Map<String, Object>> copy = new ArrayList<>();
        for (Map<String, Object> w : windows) {
            copy.add(new LinkedHashMap<>(w));
        }
        this.windows = copy;
    }

    /** The skill it was published with, or null. */
    public Map<String, Object> getSkill() {
        return skill;
    }

    /** Sets the skill. */
    public void setSkill(Map<String, Object> skill) {
        this.skill = skill == null ? null : new LinkedHashMap<>(skill);
    }

    /** True when the region has low reporting density (121). */
    public boolean isLowReportingDensity() {
        return lowReportingDensity;
    }

    /** Sets the reporting-density flag. */
    public void setLowReportingDensity(boolean lowReportingDensity) {
        this.lowReportingDensity = lowReportingDensity;
    }

    /** True when the region has low detectability (116). */
    public boolean isLowDetectability() {
        return lowDetectability;
    }

    /** Sets the detectability flag. */
    public void setLowDetectability(boolean lowDetectability) {
        this.lowDetectability = lowDetectability;
    }

    /** The caveats ({@code {code, text}}). */
    public List<Map<String, Object>> getCaveats() {
        return caveats;
    }

    /** Sets the caveats. */
    public void setCaveats(List<Map<String, Object>> caveats) {
        this.caveats = caveats == null ? new ArrayList<>() : new ArrayList<>(caveats);
    }

    /** The reference source the counts came from, or null. */
    public String getSource() {
        return source;
    }

    /** Sets the source. */
    public void setSource(String source) {
        this.source = source;
    }

    /** True for the newest outlook of its region and family. */
    public boolean isLatest() {
        return latest;
    }

    /** Sets whether it is the newest. */
    public void setLatest(boolean latest) {
        this.latest = latest;
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
