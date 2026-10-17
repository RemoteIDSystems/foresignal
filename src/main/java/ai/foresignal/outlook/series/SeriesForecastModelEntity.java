package ai.foresignal.outlook.series;

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
 * One row of {@code series_forecast_model} (prompt 126): the method the rolling-origin backtest selected for a series,
 * its parameters, the backtest ({@code SeriesForecastModel.backtest}) and when it was selected; re-selected monthly.
 */
@Entity
@Table(name = "series_forecast_model")
public class SeriesForecastModelEntity {

    @Id
    @Column(name = "ref", length = 600, nullable = false)
    private String ref;

    @Column(name = "method", length = 32, nullable = false)
    private String method;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "params", columnDefinition = "jsonb")
    private Map<String, Object> params = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "backtest", columnDefinition = "jsonb")
    private Map<String, Object> backtest = new LinkedHashMap<>();

    @Column(name = "selected_at", nullable = false)
    private Instant selectedAt;

    @Column(name = "version", length = 32, nullable = false)
    private String version;

    @Column(name = "counts", nullable = false)
    private boolean counts;

    @Column(name = "season", nullable = false)
    private int season;

    @Column(name = "bucket_days", nullable = false)
    private int bucketDays;

    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    /** JPA. */
    protected SeriesForecastModelEntity() {}

    /** Creates a row. */
    public SeriesForecastModelEntity(String ref, String method, Instant selectedAt, String version) {
        this.ref = ref;
        this.method = method;
        this.selectedAt = selectedAt;
        this.version = version;
    }

    /** The series reference (the key). */
    public String getRef() {
        return ref;
    }

    /** The selected method. */
    public String getMethod() {
        return method;
    }

    /** Sets the selected method. */
    public void setMethod(String method) {
        this.method = method;
    }

    /** The parameters fitted on the full history at selection time. */
    public Map<String, Object> getParams() {
        return params;
    }

    /** Sets the parameters. */
    public void setParams(Map<String, Object> params) {
        this.params = params == null ? new LinkedHashMap<>() : new LinkedHashMap<>(params);
    }

    /** The backtest. */
    public Map<String, Object> getBacktest() {
        return backtest;
    }

    /** Sets the backtest. */
    public void setBacktest(Map<String, Object> backtest) {
        this.backtest = backtest == null ? new LinkedHashMap<>() : new LinkedHashMap<>(backtest);
    }

    /** When the method was selected. */
    public Instant getSelectedAt() {
        return selectedAt;
    }

    /** Sets when the method was selected. */
    public void setSelectedAt(Instant selectedAt) {
        this.selectedAt = selectedAt;
    }

    /** The forecaster version ({@code series-forecast 1.0.0}). */
    public String getVersion() {
        return version;
    }

    /** Sets the version. */
    public void setVersion(String version) {
        this.version = version;
    }

    /** True for a count series. */
    public boolean isCounts() {
        return counts;
    }

    /** Sets whether the series counts. */
    public void setCounts(boolean counts) {
        this.counts = counts;
    }

    /** The season length in buckets. */
    public int getSeason() {
        return season;
    }

    /** Sets the season length. */
    public void setSeason(int season) {
        this.season = season;
    }

    /** The bucket length in days (1 or 7). */
    public int getBucketDays() {
        return bucketDays;
    }

    /** Sets the bucket length. */
    public void setBucketDays(int bucketDays) {
        this.bucketDays = bucketDays;
    }

    /** The 116 manifest of the selection, or null. */
    public String getManifestId() {
        return manifestId;
    }

    /** Sets the manifest. */
    public void setManifestId(String manifestId) {
        this.manifestId = manifestId;
    }
}
