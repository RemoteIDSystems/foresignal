package ai.foresignal.outlook.regional;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One row of {@code regional_outlook_model} (prompt 126): the monthly fit of one region and family (coefficients,
 * dispersion, the pooled intercept) and its rolling-origin evaluation over the last 26 weeks against seasonal naive
 * and climatology. The key is {@code {region}|{family}}.
 */
@Entity
@Table(name = "regional_outlook_model", indexes = {
    @Index(name = "regional_outlook_model_family_idx", columnList = "family")
})
public class RegionalOutlookModelEntity {

    @Id
    @Column(name = "id", length = 120, nullable = false)
    private String id;

    @Column(name = "region", length = 16, nullable = false)
    private String region;

    @Column(name = "family", length = 64, nullable = false)
    private String family;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "fit", columnDefinition = "jsonb")
    private Map<String, Object> fit = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "evaluation", columnDefinition = "jsonb")
    private Map<String, Object> evaluation = new LinkedHashMap<>();

    @Column(name = "fitted_at", nullable = false)
    private Instant fittedAt;

    @Column(name = "version", length = 32, nullable = false)
    private String version;

    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    /** JPA. */
    protected RegionalOutlookModelEntity() {}

    /** Creates a row. */
    public RegionalOutlookModelEntity(String region, String family, Instant fittedAt, String version) {
        this.id = key(region, family);
        this.region = region;
        this.family = family;
        this.fittedAt = fittedAt;
        this.version = version;
    }

    /** The key of a region and family. */
    public static String key(String region, String family) {
        return region + "|" + family;
    }

    /** The key. */
    public String getId() {
        return id;
    }

    /** The region. */
    public String getRegion() {
        return region;
    }

    /** The family. */
    public String getFamily() {
        return family;
    }

    /** The fit ({@code RegionalOutlookModel.Fit.view()} plus the pooled intercept). */
    public Map<String, Object> getFit() {
        return fit;
    }

    /** Sets the fit. */
    public void setFit(Map<String, Object> fit) {
        this.fit = fit == null ? new LinkedHashMap<>() : new LinkedHashMap<>(fit);
    }

    /** The rolling-origin evaluation. */
    public Map<String, Object> getEvaluation() {
        return evaluation;
    }

    /** Sets the evaluation. */
    public void setEvaluation(Map<String, Object> evaluation) {
        this.evaluation = evaluation == null ? new LinkedHashMap<>() : new LinkedHashMap<>(evaluation);
    }

    /** When it was fitted. */
    public Instant getFittedAt() {
        return fittedAt;
    }

    /** Sets when it was fitted. */
    public void setFittedAt(Instant fittedAt) {
        this.fittedAt = fittedAt;
    }

    /** The model version. */
    public String getVersion() {
        return version;
    }

    /** Sets the version. */
    public void setVersion(String version) {
        this.version = version;
    }

    /** The 116 manifest, or null. */
    public String getManifestId() {
        return manifestId;
    }

    /** Sets the manifest. */
    public void setManifestId(String manifestId) {
        this.manifestId = manifestId;
    }
}
