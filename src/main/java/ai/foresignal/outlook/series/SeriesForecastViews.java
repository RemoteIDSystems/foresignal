package ai.foresignal.outlook.series;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The JSON shapes of the series forecast API (prompt 126): {@code SeriesForecast} and {@code SeriesForecastModel}.
 * A forecast of a {@code LOCAL_ONLY} series is served in standardized units ({@code (q − mean)/sd} of the recent
 * history; {@code units: "standardised"}, the spec's published wire value) to callers the egress policy refuses raw
 * values to.
 */
public final class SeriesForecastViews {

    private SeriesForecastViews() {}

    /**
     * The {@code SeriesForecast} of the latest forecast rows.
     *
     * @param rows the rows of one run (one per horizon)
     * @param horizon only this horizon, or null
     * @param standardized true to serve standardized units
     * @param questionIds the ledger questions of the series
     */
    public static Map<String, Object> forecast(List<SeriesForecastEntity> rows, Integer horizon, boolean standardized,
            List<String> questionIds) {
        SeriesForecastEntity first = rows.get(0);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ref", first.getRef());
        out.put("method", first.getMethod());
        out.put("version", first.getVersion());
        out.put("madeAt", first.getMadeAt().toString());
        out.put("asOfSeq", first.getAsofSeq());
        out.put("manifestId", first.getManifestId());
        out.put("units", standardized ? "standardised" : "native");
        out.put("unit", standardized ? null : first.getUnit());
        List<Map<String, Object>> forecasts = new ArrayList<>();
        for (SeriesForecastEntity row : rows) {
            if (horizon != null && row.getHorizon() != horizon) {
                continue;
            }
            Map<String, Object> f = new LinkedHashMap<>();
            f.put("horizon", row.getHorizon());
            f.put("targetPeriod", Map.of("start", row.getTargetStart().toString(), "end",
                    row.getTargetEnd().toString()));
            f.put("quantiles", standardized ? standardize(row.getQuantiles(), row.getStandardization())
                    : row.getQuantiles());
            forecasts.add(f);
        }
        out.put("forecasts", forecasts);
        out.put("questionIds", questionIds);
        List<Map<String, Object>> caveats = new ArrayList<>();
        if (standardized) {
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("code", "LOCAL_ONLY_WITHHELD");
            c.put("text", "The series' license keeps raw values local; quantiles are in standard deviations from the"
                    + " mean of the last year of history.");
            caveats.add(c);
        }
        out.put("caveats", caveats);
        return out;
    }

    /** Quantiles in standard deviations from the history mean (all zero for a constant history). */
    static Map<String, Double> standardize(Map<String, Double> quantiles, Map<String, Double> standardization) {
        double mean = standardization == null ? 0 : standardization.getOrDefault("mean", 0.0);
        double sd = standardization == null ? 0 : standardization.getOrDefault("sd", 0.0);
        Map<String, Double> out = new LinkedHashMap<>();
        quantiles.forEach((k, v) -> out.put(k, sd > 0 ? QuantileSet.round((v - mean) / sd) : 0.0));
        return out;
    }

    /**
     * The {@code SeriesForecastModel} of a model.
     *
     * @param withheld true to leave the fitted parameters out (they are on the scale of a {@code LOCAL_ONLY} series)
     */
    public static Map<String, Object> model(SeriesForecastModelEntity m, boolean withheld) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ref", m.getRef());
        out.put("method", m.getMethod());
        out.put("params", withheld ? Map.of("withheld", true) : m.getParams());
        out.put("backtest", m.getBacktest());
        out.put("selectedAt", m.getSelectedAt().toString());
        out.put("nextSelectionAt", m.getSelectedAt().plus(java.time.Duration.ofDays(
                SeriesForecastService.RESELECT_DAYS)).toString());
        out.put("version", m.getVersion());
        out.put("manifestId", m.getManifestId());
        return out;
    }
}
