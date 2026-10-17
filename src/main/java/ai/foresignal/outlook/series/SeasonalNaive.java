package ai.foresignal.outlook.series;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code seasonal-naive} (prompt 126), the benchmark every other method must beat: the forecast {@code h} steps ahead
 * is the last observed value of the same season slot, {@code ŷ(T+h) = y(T + h − m·⌈h/m⌉)}. Its intervals are normal
 * with the standard deviation of the seasonal differences {@code e(t) = y(t) − y(t − m)} scaled by {@code √⌈h/m⌉}
 * (Hyndman and Athanasopoulos, FPP3 §5.5). Count series are floored at zero. With {@code m = 1} it is the naive
 * (random-walk) forecast.
 */
public final class SeasonalNaive implements ForecastMethod {

    /** The method id. */
    public static final String ID = "seasonal-naive";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public boolean supports(boolean counts) {
        return true;
    }

    @Override
    public int minHistory(int season) {
        return 2 * Math.max(1, season);
    }

    /** The seasonal-naive point forecast {@code h} steps after the end of {@code y}. */
    public static double point(double[] y, int season, int h) {
        int m = Math.max(1, season);
        int k = (h + m - 1) / m;
        return y[y.length - 1 + h - m * k];
    }

    /** The standard deviation of the seasonal differences (0 for fewer than two). */
    public static double residualSd(double[] y, int season) {
        int m = Math.max(1, season);
        int n = 0;
        double sum = 0;
        double sumSq = 0;
        for (int t = m; t < y.length; t++) {
            double e = y[t] - y[t - m];
            sum += e;
            sumSq += e * e;
            n++;
        }
        if (n < 2) {
            return 0;
        }
        double mean = sum / n;
        return Math.sqrt(Math.max(0, (sumSq - n * mean * mean) / (n - 1)));
    }

    @Override
    public MethodForecast forecast(double[] y, int season, boolean counts, int[] horizons) {
        int m = Math.max(1, season);
        if (y.length < minHistory(m)) {
            throw new IllegalArgumentException("seasonal-naive needs at least " + minHistory(m) + " points");
        }
        double sd = residualSd(y, m);
        Map<Integer, double[]> out = new LinkedHashMap<>();
        for (int h : horizons) {
            int k = (h + m - 1) / m;
            double[] q = QuantileSet.normal(point(y, m, h), sd * Math.sqrt(k));
            out.put(h, QuantileSet.monotone(counts ? QuantileSet.floor(q, 0) : q));
        }
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("season", m);
        params.put("residualSd", QuantileSet.round(sd));
        return new MethodForecast(ID, out, params);
    }
}
