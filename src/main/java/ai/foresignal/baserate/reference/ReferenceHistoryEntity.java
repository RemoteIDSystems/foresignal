package ai.foresignal.baserate.reference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * One weekly count of reference history (prompt 121, table {@code reference_history}): how many events of a family
 * (a 119 signal family plus a subtype, e.g. {@code conflict:battles} or {@code seismic:m5}) one source recorded in one
 * 040 region during one ISO week (Monday, UTC). Counts only, never raw rows; each row keeps its source's license and
 * egress. Primary key {@code (family, region_code, week_start, source)}.
 *
 * <p>Written only through SQL by {@link ReferenceHistoryWriter}; the entity maps the table so Hibernate validates it against the Flyway schema.
 */
@Entity
@Table(name = "reference_history", indexes = {
    @Index(name = "reference_history_import_idx", columnList = "import_id"),
    @Index(name = "reference_history_region_idx", columnList = "region_code, family")
})
@IdClass(ReferenceHistoryEntity.Key.class)
public class ReferenceHistoryEntity {

    /**
     * The composite primary key.
     */
    public static class Key implements Serializable {

        private static final long serialVersionUID = 1L;

        private String family;
        private String regionCode;
        private LocalDate weekStart;
        private String source;

        /** JPA. */
        public Key() {}

        /**
         * Creates a key.
         *
         * @param family the family
         * @param regionCode the 040 region
         * @param weekStart the Monday of the ISO week
         * @param source the source
         */
        public Key(String family, String regionCode, LocalDate weekStart, String source) {
            this.family = family;
            this.regionCode = regionCode;
            this.weekStart = weekStart;
            this.source = source;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Key k && Objects.equals(family, k.family) && Objects.equals(regionCode, k.regionCode)
                    && Objects.equals(weekStart, k.weekStart) && Objects.equals(source, k.source);
        }

        @Override
        public int hashCode() {
            return Objects.hash(family, regionCode, weekStart, source);
        }
    }

    @Id
    @Column(name = "family", length = 64, nullable = false)
    private String family;

    @Id
    @Column(name = "region_code", length = 16, nullable = false)
    private String regionCode;

    @Id
    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @Id
    @Column(name = "source", length = 32, nullable = false)
    private String source;

    @Column(name = "count", nullable = false)
    private long count;

    @Column(name = "fatalities")
    private Long fatalities;

    @Column(name = "licence", length = 64, nullable = false)
    private String license;

    @Column(name = "egress", length = 16, nullable = false)
    private String egress;

    @Column(name = "import_id", length = 26)
    private String importId;

    /** For JPA. */
    protected ReferenceHistoryEntity() {}

    /** The family. */
    public String getFamily() {
        return family;
    }

    /** The 040 region code. */
    public String getRegionCode() {
        return regionCode;
    }

    /** The Monday of the ISO week (UTC). */
    public LocalDate getWeekStart() {
        return weekStart;
    }

    /** The source ({@code ucdp}, {@code acled}, {@code usgs}, {@code gdacs} or {@code foresignal}). */
    public String getSource() {
        return source;
    }

    /** The event count. */
    public long getCount() {
        return count;
    }

    /** The fatalities, or null when the source reports none. */
    public Long getFatalities() {
        return fatalities;
    }

    /** The license. */
    public String getLicense() {
        return license;
    }

    /** {@code OPEN} or {@code LOCAL_ONLY}. */
    public String getEgress() {
        return egress;
    }

    /** The import that wrote the row, or null for the own-history fold. */
    public String getImportId() {
        return importId;
    }
}
