package ai.foresignal.place;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One place's cached forecast series of one provider ({@code forecast_series_cache}, prompt 053): written by
 * {@code nws-gridpoints} (provider {@code nws}) on every successful fetch, deleted with the place. Declared for
 * Hibernate to own the table; {@link ForecastSeriesCache} reads and writes it with SQL.
 */
@Entity
@Table(name = "forecast_series_cache")
@IdClass(ForecastSeriesCacheEntity.Key.class)
public class ForecastSeriesCacheEntity {

    @Id
    @Column(name = "place_id", nullable = false, length = 64)
    private String placeId;

    @Id
    @Column(name = "provider", nullable = false, length = 20)
    private String provider;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "valid_from")
    private Instant validFrom;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "series", nullable = false)
    private List<Map<String, Object>> series = new ArrayList<>();

    /** For JPA. */
    protected ForecastSeriesCacheEntity() {}

    /** The place. */
    public String getPlaceId() {
        return placeId;
    }

    /** The provider. */
    public String getProvider() {
        return provider;
    }

    /** The upstream's update time. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** The first step's time. */
    public Instant getValidFrom() {
        return validFrom;
    }

    /** The steps. */
    public List<Map<String, Object>> getSeries() {
        return series;
    }

    /** The composite key. */
    public static class Key implements Serializable {

        private static final long serialVersionUID = 1L;

        private String placeId;
        private String provider;

        /** For JPA. */
        public Key() {}

        @Override
        public boolean equals(Object o) {
            return o instanceof Key k && Objects.equals(placeId, k.placeId) && Objects.equals(provider, k.provider);
        }

        @Override
        public int hashCode() {
            return Objects.hash(placeId, provider);
        }
    }
}
