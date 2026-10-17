package ai.foresignal.baserate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One stored base rate (prompt 121, table {@code base_rate}): its definition (the reference class), the rate per
 * window, the fraction of windows with at least one event and its Jeffreys 95% interval, the sample, the period, the
 * source mix and licenses, the caveats, the computer, the origin ({@code auto}, {@code operator} or {@code agent}) and
 * the 116 manifest. Automatic rates are unique per definition and replaced in place on every refresh.
 */
@Entity
@Table(name = "base_rate", indexes = {
    @Index(name = "base_rate_key_idx", columnList = "definition_key, origin"),
    @Index(name = "base_rate_family_idx", columnList = "family, window_code")
})
public class BaseRateEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "name", length = 200)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "definition", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> definition = new LinkedHashMap<>();

    @Column(name = "definition_key", length = 64, nullable = false)
    private String definitionKey;

    @Column(name = "family", length = 64, nullable = false)
    private String family;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "region_codes", columnDefinition = "text[]")
    private String[] regionCodes = new String[0];

    @Column(name = "region_class", length = 32)
    private String regionClass;

    @Column(name = "window_code", length = 8, nullable = false)
    private String window;

    @Column(name = "rate_per_window", nullable = false)
    private double ratePerWindow;

    @Column(name = "p_at_least_one", nullable = false)
    private double pAtLeastOne;

    @Column(name = "interval_lower", nullable = false)
    private double intervalLower;

    @Column(name = "interval_upper", nullable = false)
    private double intervalUpper;

    @Column(name = "n_windows", nullable = false)
    private int nWindows;

    @Column(name = "n_events", nullable = false)
    private long nEvents;

    @Column(name = "period_from")
    private LocalDate periodFrom;

    @Column(name = "period_to")
    private LocalDate periodTo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "source_mix", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> sourceMix = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "licences", columnDefinition = "jsonb", nullable = false)
    private List<Map<String, Object>> licenses = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "unconditional", columnDefinition = "jsonb")
    private Map<String, Object> unconditional;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "caveats", columnDefinition = "jsonb", nullable = false)
    private List<Map<String, Object>> caveats = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "computer", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> computer = new LinkedHashMap<>();

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt;

    @Column(name = "origin", length = 16, nullable = false)
    private String origin;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    /** For JPA. */
    protected BaseRateEntity() {}

    /**
     * Creates a base rate.
     *
     * @param id the ULID
     * @param origin {@code auto}, {@code operator} or {@code agent}
     */
    public BaseRateEntity(String id, String origin) {
        this.id = id;
        this.origin = origin;
    }

    /** The ULID. */
    public String getId() {
        return id;
    }

    /** The name, or null. */
    public String getName() {
        return name;
    }

    /** Sets the name. */
    public void setName(String value) {
        this.name = value;
    }

    /** The {@code BaseRateDefinition}. */
    public Map<String, Object> getDefinition() {
        return definition == null ? Map.of() : definition;
    }

    /** Sets the definition and its denormalized columns. */
    public void setDefinition(BaseRateDefinition value, String key) {
        this.definition = new LinkedHashMap<>(value.toMap());
        this.definitionKey = key;
        this.family = value.family();
        this.regionCodes = value.regionCodes().toArray(String[]::new);
        this.regionClass = value.regionClass();
        this.window = value.window();
    }

    /** The definition's key. */
    public String getDefinitionKey() {
        return definitionKey;
    }

    /** The family. */
    public String getFamily() {
        return family;
    }

    /** The regions. */
    public List<String> getRegionCodes() {
        return regionCodes == null ? List.of() : Arrays.asList(regionCodes);
    }

    /** The region class, or null. */
    public String getRegionClass() {
        return regionClass;
    }

    /** The window. */
    public String getWindow() {
        return window;
    }

    /** Mean events per window. */
    public double getRatePerWindow() {
        return ratePerWindow;
    }

    /** The fraction of windows with at least one event. */
    public double getPAtLeastOne() {
        return pAtLeastOne;
    }

    /** The interval's lower bound. */
    public double getIntervalLower() {
        return intervalLower;
    }

    /** The interval's upper bound. */
    public double getIntervalUpper() {
        return intervalUpper;
    }

    /** The windows. */
    public int getNWindows() {
        return nWindows;
    }

    /** The events. */
    public long getNEvents() {
        return nEvents;
    }

    /** The first day of the period, or null. */
    public LocalDate getPeriodFrom() {
        return periodFrom;
    }

    /** The last day of the period, or null. */
    public LocalDate getPeriodTo() {
        return periodTo;
    }

    /** Events per source. */
    public Map<String, Object> getSourceMix() {
        return sourceMix == null ? Map.of() : sourceMix;
    }

    /** The licenses of the contributing sources. */
    public List<Map<String, Object>> getLicenses() {
        return licenses == null ? List.of() : licenses;
    }

    /** The unconditional rate beside a conditional one, or null. */
    public Map<String, Object> getUnconditional() {
        return unconditional;
    }

    /** The caveats. */
    public List<Map<String, Object>> getCaveats() {
        return caveats == null ? List.of() : caveats;
    }

    /** The computer ({@code DetectorRef}). */
    public Map<String, Object> getComputer() {
        return computer == null ? Map.of() : computer;
    }

    /** When it was computed. */
    public Instant getComputedAt() {
        return computedAt;
    }

    /** {@code auto}, {@code operator} or {@code agent}. */
    public String getOrigin() {
        return origin;
    }

    /** Who stored a custom rate, or null. */
    public String getCreatedBy() {
        return createdBy;
    }

    /** Sets who stored it. */
    public void setCreatedBy(String value) {
        this.createdBy = value;
    }

    /** The 116 manifest, or null. */
    public String getManifestId() {
        return manifestId;
    }

    /**
     * Copies a computed rate in.
     *
     * @param rate the computed rate
     */
    public void apply(BaseRateService.Computed rate) {
        this.ratePerWindow = rate.result().ratePerWindow();
        this.pAtLeastOne = rate.result().pAtLeastOne();
        this.intervalLower = rate.result().lower();
        this.intervalUpper = rate.result().upper();
        this.nWindows = rate.result().nWindows();
        this.nEvents = rate.result().nEvents();
        this.periodFrom = rate.result().periodFrom();
        this.periodTo = rate.result().periodTo();
        this.sourceMix = new LinkedHashMap<>(rate.result().sourceMix());
        this.licenses = new ArrayList<>(rate.licenses());
        this.unconditional = rate.unconditional();
        this.caveats = new ArrayList<>(rate.caveats());
        this.computer = new LinkedHashMap<>(rate.computer());
        this.computedAt = rate.computedAt();
        this.manifestId = rate.manifestId();
    }
}
