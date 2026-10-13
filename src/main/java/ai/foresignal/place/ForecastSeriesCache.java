package ai.foresignal.place;

import ai.foresignal.source.spi.ForecastSeriesSink;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code forecast_series_cache} table (prompt 053): per place and provider, the newest forecast series an
 * adapter stored ({@link ForecastSeriesSink}), replaced whole on each successful fetch and deleted when the place
 * is deleted ({@link PlaceChangedEvent}). Read by {@code ForecastSeriesService} for the place weather endpoint,
 * the {@code weather_forecast} MCP tool and the series merge.
 */
@Component
public class ForecastSeriesCache implements ForecastSeriesSink {

    private static final TypeReference<List<Map<String, Object>>> ITEMS = new TypeReference<>() {};

    private final JdbcTemplate jdbc;
    private final JsonMapper json;

    /** Creates the cache. */
    public ForecastSeriesCache(JdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    /**
     * One cached series.
     *
     * @param placeId the place
     * @param provider the provider
     * @param updatedAt the upstream's update time
     * @param validFrom the first step's time, or null
     * @param items the steps, oldest first
     */
    public record Entry(String placeId, String provider, Instant updatedAt, Instant validFrom,
            List<Map<String, Object>> items) {

        /** Copies the steps. */
        public Entry {
            items = items == null ? List.of() : List.copyOf(items);
        }
    }

    @Override
    public void put(String placeId, String provider, Instant updatedAt, Instant validFrom, List<Map<String, Object>> items) {
        String series = json.writeValueAsString(items == null ? List.of() : items);
        jdbc.update("""
                insert into forecast_series_cache (place_id, provider, updated_at, valid_from, series)
                values (?, ?, ?, ?, ?::jsonb)
                on conflict (place_id, provider) do update
                   set updated_at = excluded.updated_at, valid_from = excluded.valid_from, series = excluded.series
                """, placeId, provider, Timestamp.from(updatedAt), validFrom == null ? null : Timestamp.from(validFrom),
                series);
    }

    @Override
    public void remove(String placeId, String provider) {
        jdbc.update("delete from forecast_series_cache where place_id = ? and provider = ?", placeId, provider);
    }

    /** One place's series of one provider, or empty. */
    public Optional<Entry> find(String placeId, String provider) {
        List<Entry> rows = jdbc.query("""
                select place_id, provider, updated_at, valid_from, series::text as series
                  from forecast_series_cache where place_id = ? and provider = ?
                """, (rs, n) -> new Entry(rs.getString("place_id"), rs.getString("provider"),
                rs.getTimestamp("updated_at").toInstant(),
                rs.getTimestamp("valid_from") == null ? null : rs.getTimestamp("valid_from").toInstant(),
                json.readValue(rs.getString("series"), ITEMS)), placeId, provider);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /** Deletes every provider's series of a deleted place. */
    @EventListener
    public void onPlaceChanged(PlaceChangedEvent event) {
        if (event.deleted()) {
            jdbc.update("delete from forecast_series_cache where place_id = ?", event.placeId());
        }
    }
}
