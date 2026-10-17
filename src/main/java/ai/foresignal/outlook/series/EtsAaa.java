package ai.foresignal.outlook.series;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code ets-aaa} (prompt 126): ETS(A,A,A), additive errors, an additive trend and an additive season (weekly for
 * daily data), fitted by maximum likelihood ({@link EtsModel}; initial states from the classical decomposition of the
 * first two seasons). Count series are modeled on {@code log1p(x)}. Intervals from the ETS variance formula with
 * {@code c_j = α + β·j + γ·d_j}. Needs a season longer than 1.
 */
public final class EtsAaa implements ForecastMethod {

    /** The method id. */
    public static final String ID = "ets-aaa";

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
        return Math.max(3 * Math.max(2, season), 14);
    }

    @Override
    public MethodForecast forecast(double[] y, int season, boolean counts, int[] horizons) {
        if (season < 2) {
            throw new IllegalArgumentException("ets-aaa needs a season");
        }
        if (y.length < minHistory(season)) {
            throw new IllegalArgumentException("ets-aaa needs at least " + minHistory(season) + " points");
        }
        double[] data = counts ? SeriesPrep.log1p(y) : y;
        EtsModel.Fit fit = EtsModel.fit(data, new EtsModel.Spec(EtsModel.Trend.ADDITIVE, season), false);
        Map<Integer, double[]> out = new LinkedHashMap<>();
        for (int h : horizons) {
            out.put(h, EtsModel.quantiles(fit, h, counts));
        }
        Map<String, Object> params = fit.view();
        params.put("scale", counts ? "log1p" : "level");
        return new MethodForecast(ID, out, params);
    }
}
