package ai.foresignal.source.spi;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Where an adapter stores a place's forecast series (prompt 053, table {@code forecast_series_cache}): the
 * hourly series {@code nws-gridpoints} expands from a gridpoint forecast, read back by
 * {@code GET /v1/places/{id}/weather}, the {@code weather_forecast} MCP tool and the forecast series merge.
 *
 * <p>Adapters depend only on SPI types (Architecture §3); {@code ai.foresignal.place.ForecastSeriesCache}
 * implements it. One row per place and provider, replaced whole on each successful fetch.
 */
public interface ForecastSeriesSink {

    /**
     * Replaces a place's series.
     *
     * @param placeId the place
     * @param provider the provider, for example {@code nws}
     * @param updatedAt the upstream's update time of the forecast
     * @param validFrom the first step's time
     * @param items the steps, oldest first: maps with {@code time} and the {@code ForecastSeriesPoint} fields
     */
    void put(String placeId, String provider, Instant updatedAt, Instant validFrom, List<Map<String, Object>> items);

    /** Deletes a place's series of one provider; nothing happens when there is none. */
    void remove(String placeId, String provider);

    /** A sink that keeps nothing (test doubles and contexts without places). */
    static ForecastSeriesSink none() {
        return new ForecastSeriesSink() {
            @Override
            public void put(String placeId, String provider, Instant updatedAt, Instant validFrom,
                    List<Map<String, Object>> items) {}

            @Override
            public void remove(String placeId, String provider) {}
        };
    }
}
