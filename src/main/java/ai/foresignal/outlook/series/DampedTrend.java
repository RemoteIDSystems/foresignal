package ai.foresignal.outlook.series;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code damped-trend} (prompt 126): ETS(A,Ad,N), additive errors with an additive damped trend and no season, for
 * continuous series, fitted by maximum likelihood ({@link EtsModel}; {@code 0.8 ≤ φ ≤ 0.98}). Intervals from the ETS
 * variance formula with {@code c_j = α + β·(φ + … + φ^j)}.
 */
public final class DampedTrend implements ForecastMethod {

    /** The method id. */
    public static final String ID = "damped-trend";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public boolean supports(boolean counts) {
        return !counts;
    }

    @Override
    public int minHistory(int season) {
        return 12;
    }

    @Override
    public MethodForecast forecast(double[] y, int season, boolean counts, int[] horizons) {
        if (y.length < minHistory(season)) {
            throw new IllegalArgumentException("damped-trend needs at least " + minHistory(season) + " points");
        }
        EtsModel.Fit fit = EtsModel.fit(y, new EtsModel.Spec(EtsModel.Trend.DAMPED, 1), false);
        Map<Integer, double[]> out = new LinkedHashMap<>();
        for (int h : horizons) {
            out.put(h, EtsModel.quantiles(fit, h, false));
        }
        Map<String, Object> params = fit.view();
        params.put("scale", "level");
        return new MethodForecast(ID, out, params);
    }
}
