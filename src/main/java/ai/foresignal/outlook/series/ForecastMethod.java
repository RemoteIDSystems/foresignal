package ai.foresignal.outlook.series;

import java.util.Map;

/**
 * One forecasting method of the series forecaster (prompt 126): a pure function of the history, the season length,
 * whether the series counts, and the horizons, returning quantiles at every {@link QuantileSet#LEVELS} level per
 * horizon. Implementations are stateless and deterministic (seeded where they sample).
 */
public interface ForecastMethod {

    /** The method id ({@code seasonal-naive}, {@code ets-ann}, {@code ets-aaa}, {@code nb-glm}, {@code damped-trend}). */
    String id();

    /** True when the method applies to a count series ({@code counts}) or a continuous one. */
    boolean supports(boolean counts);

    /** The fewest usable points the method needs for season length {@code season}. */
    int minHistory(int season);

    /**
     * Forecasts {@code horizons} steps after the last point of {@code y}.
     *
     * @param y the history in time order, gaps already filled ({@link SeriesPrep#fill})
     * @param season the season length in steps (1 for none)
     * @param counts true for a count series (non-negative quantiles; transformed where the method says)
     * @param horizons the steps ahead (each ≥ 1)
     * @return the forecast
     * @throws IllegalArgumentException when the history is too short
     */
    MethodForecast forecast(double[] y, int season, boolean counts, int[] horizons);

    /**
     * A method's forecast.
     *
     * @param method the method id
     * @param quantiles per horizon (the steps ahead), the nine quantiles of {@link QuantileSet#LEVELS}, monotone
     * @param params the fitted parameters
     */
    record MethodForecast(String method, Map<Integer, double[]> quantiles, Map<String, Object> params) {}
}
