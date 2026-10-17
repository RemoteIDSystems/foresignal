package ai.foresignal.analytics.registry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One row of {@code analysis_series} (prompt 119, Intelligence-Design §5.6): a series the detectors run on.
 *
 * <p>{@code detectors} lists {@code changepoint-cusum}, {@code changepoint-bocpd} (and {@code forecast} from prompt
 * 126); {@code frequency} is {@code hour}, {@code day} or {@code week} (an hourly rollup or rule group registered as
 * {@code day} is summed to days); {@code watch_pairs} holds the event-study pairs ({@link WatchPair});
 * {@code origin} is {@code auto} (nightly auto-registration) or {@code operator} (never removed automatically);
 * {@code volume} ranks automatic rows for the cap.
 */
@Entity
@Table(name = "analysis_series", indexes = {
    @Index(name = "analysis_series_family_idx", columnList = "family"),
    @Index(name = "analysis_series_region_idx", columnList = "region_code")
})
public class AnalysisSeriesEntity {

    @Id
    @Column(name = "ref", length = 600, nullable = false)
    private String ref;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "detectors", columnDefinition = "text[]")
    private String[] detectors = new String[0];

    @Column(name = "frequency", length = 8, nullable = false)
    private String frequency = "day";

    @Column(name = "family", length = 64)
    private String family;

    @Column(name = "region_code", length = 16)
    private String regionCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "watch_pairs", columnDefinition = "jsonb")
    private List<Map<String, Object>> watchPairs = new ArrayList<>();

    @Column(name = "origin", length = 16, nullable = false)
    private String origin = "auto";

    @Column(name = "volume", nullable = false)
    private double volume;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "last_run_at")
    private Instant lastRunAt;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    /** JPA. */
    protected AnalysisSeriesEntity() {}

    /** Creates a row. */
    public AnalysisSeriesEntity(String ref, String origin, Instant now) {
        this.ref = ref;
        this.origin = origin;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** The series reference (the key). */
    public String getRef() {
        return ref;
    }

    /** True when the detectors run on it. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Sets {@link #isEnabled()}. */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** The detectors. */
    public List<String> getDetectors() {
        return detectors == null ? List.of() : Arrays.asList(detectors);
    }

    /** Sets the detectors. */
    public void setDetectors(List<String> detectors) {
        this.detectors = detectors == null ? new String[0] : detectors.toArray(String[]::new);
    }

    /** {@code hour}, {@code day} or {@code week}. */
    public String getFrequency() {
        return frequency;
    }

    /** Sets the frequency. */
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    /** The signal family, or null. */
    public String getFamily() {
        return family;
    }

    /** Sets the family. */
    public void setFamily(String family) {
        this.family = family;
    }

    /** The 040 region, or null. */
    public String getRegionCode() {
        return regionCode;
    }

    /** Sets the region. */
    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    /** The stored watch pairs. */
    public List<Map<String, Object>> getWatchPairs() {
        return watchPairs == null ? List.of() : watchPairs;
    }

    /** Sets the watch pairs. */
    public void setWatchPairs(List<Map<String, Object>> watchPairs) {
        this.watchPairs = watchPairs == null ? new ArrayList<>() : new ArrayList<>(watchPairs);
    }

    /** {@code auto} or {@code operator}. */
    public String getOrigin() {
        return origin;
    }

    /** Sets the origin. */
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    /** The ranking volume of an automatic row. */
    public double getVolume() {
        return volume;
    }

    /** Sets the volume. */
    public void setVolume(double volume) {
        this.volume = volume;
    }

    /** When the row was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When the row last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets {@link #getUpdatedAt()}. */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** The last detector run, or null. */
    public Instant getLastRunAt() {
        return lastRunAt;
    }

    /** Sets the last run. */
    public void setLastRunAt(Instant lastRunAt) {
        this.lastRunAt = lastRunAt;
    }

    /** The last run's error, or null. */
    public String getLastError() {
        return lastError;
    }

    /** Sets the last error. */
    public void setLastError(String lastError) {
        this.lastError = lastError;
    }
}
