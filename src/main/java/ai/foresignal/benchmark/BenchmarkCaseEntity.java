package ai.foresignal.benchmark;

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
 * One historical benchmark case (prompt 123, table {@code benchmark_case}): a known past episode, or an automatically
 * sampled null region-week, with what the detectors should find. Cases replay with {@code asOf} stepping from the
 * window start.
 */
@Entity
@Table(name = "benchmark_case", indexes = {
    @Index(name = "benchmark_case_source_idx", columnList = "source, id")
})
public class BenchmarkCaseEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "window_from", nullable = false)
    private Instant windowFrom;

    @Column(name = "window_to", nullable = false)
    private Instant windowTo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "region_codes", columnDefinition = "jsonb", nullable = false)
    private List<String> regionCodes = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "expectations", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> expectations = new LinkedHashMap<>();

    @Column(name = "source", length = 16, nullable = false)
    private String source;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** For JPA. */
    protected BenchmarkCaseEntity() {}

    /**
     * Creates a case.
     *
     * @param id the id
     * @param name the name
     * @param windowFrom the window start
     * @param windowTo the window end
     * @param source {@code operator}, {@code agent} or {@code auto}
     * @param createdAt when it was created
     */
    public BenchmarkCaseEntity(String id, String name, Instant windowFrom, Instant windowTo, String source,
            Instant createdAt) {
        this.id = id;
        this.name = name;
        this.windowFrom = windowFrom;
        this.windowTo = windowTo;
        this.source = source;
        this.createdAt = createdAt;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The name. */
    public String getName() {
        return name;
    }

    /** The description, or null. */
    public String getDescription() {
        return description;
    }

    /** Sets the description. */
    public void setDescription(String description) {
        this.description = description;
    }

    /** The window start. */
    public Instant getWindowFrom() {
        return windowFrom;
    }

    /** The window end. */
    public Instant getWindowTo() {
        return windowTo;
    }

    /** The 040 region codes. */
    public List<String> getRegionCodes() {
        return regionCodes;
    }

    /** Sets the region codes. */
    public void setRegionCodes(List<String> regionCodes) {
        this.regionCodes = regionCodes == null ? new ArrayList<>() : new ArrayList<>(regionCodes);
    }

    /** The expectations ({@code findings}, {@code precursors}, {@code questions}, {@code notes}, …). */
    public Map<String, Object> getExpectations() {
        return expectations;
    }

    /** Sets the expectations. */
    public void setExpectations(Map<String, Object> expectations) {
        this.expectations = expectations == null ? new LinkedHashMap<>() : new LinkedHashMap<>(expectations);
    }

    /** The source. */
    public String getSource() {
        return source;
    }

    /** Who created it, or null. */
    public String getCreatedBy() {
        return createdBy;
    }

    /** Sets who created it. */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /** When it was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
