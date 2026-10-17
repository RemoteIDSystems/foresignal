package ai.foresignal.outlook.series;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code ets-ann} (prompt 126): ETS(A,N,N), simple exponential smoothing with additive errors, fitted by maximum
 * likelihood ({@link EtsModel}, smoothing and initial level estimated). Count series are modeled on
 * {@code log1p(x)} and their quantiles back-transformed; other series on levels. Intervals from the ETS variance
 * formula {@code σ²·(1 + α²·(h − 1))}.
 */
public final class EtsAnn implements ForecastMethod {

    /** The method id. */
    public static final String ID = "ets-ann";

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
        return 10;
    }

    @Override
    public MethodForecast forecast(double[] y, int season, boolean counts, int[] horizons) {
        if (y.length < minHistory(season)) {
            throw new IllegalArgumentException("ets-ann needs at least " + minHistory(season) + " points");
        }
        double[] data = counts ? SeriesPrep.log1p(y) : y;
        EtsModel.Fit fit = EtsModel.fit(data, new EtsModel.Spec(EtsModel.Trend.NONE, 1), true);
        Map<Integer, double[]> out = new LinkedHashMap<>();
        for (int h : horizons) {
            out.put(h, EtsModel.quantiles(fit, h, counts));
        }
        Map<String, Object> params = fit.view();
        params.put("scale", counts ? "log1p" : "level");
        return new MethodForecast(ID, out, params);
    }
}
