package ai.foresignal.outlook.series;

import ai.foresignal.analytics.math.NbGlm;
import ai.foresignal.analytics.math.NbSampling;
import ai.foresignal.analytics.math.Seeds;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code nb-glm} (prompt 126) for count series: a negative-binomial regression
 * {@code log μ(t) = β₀ + β₁·t + day-of-week + 2 annual Fourier pairs + β·log1p(y(t − L))} with {@code L} the season
 * (7 for daily data; 1 without a season), fitted by IRLS ({@link NbGlm}: Poisson start, NB dispersion by moments).
 * Day-of-week dummies apply to a weekly season only; the annual period is 365.25 days (52.18 weeks for weekly data).
 *
 * <p>Quantiles come from the NB predictive distribution with a parametric bootstrap of {@code β} for parameter
 * uncertainty: {@value #DRAWS} coefficient draws from {@code N(β̂, Cov)}, each simulating {@value #PATHS_PER_DRAW}
 * paths forward (lags beyond the history take the simulated counts), seeded from the data so the result is
 * deterministic. The empirical quantiles of the simulated counts at each horizon are monotone by construction.
 */
public final class NbGlmMethod implements ForecastMethod {

    /** The method id. */
    public static final String ID = "nb-glm";

    /** Coefficient draws of the parametric bootstrap. */
    public static final int DRAWS = 200;

    /** Simulated paths per coefficient draw. */
    public static final int PATHS_PER_DRAW = 5;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public boolean supports(boolean counts) {
        return counts;
    }

    @Override
    public int minHistory(int season) {
        return Math.max(4 * Math.max(1, season), 28);
    }

    /** The annual period in steps for a season length (52.18 weeks for weekly data, 365.25 days otherwise). */
    static double annualPeriod(int season) {
        return season == 52 ? 365.25 / 7 : 365.25;
    }

    /** The lag of the autoregressive term. */
    static int lag(int season) {
        return season > 1 && season != 52 ? season : 1;
    }

    /**
     * The design row at step {@code t} with lagged count {@code lagged}.
     *
     * @param t the step (0 = first point of the history)
     * @param season the season length
     * @param lagged the count {@code L} steps earlier
     */
    public static double[] row(int t, int season, double lagged) {
        List<Double> r = new ArrayList<>();
        r.add(1.0);
        r.add(t / 365.25);
        if (season == 7) {
            int dow = t % 7;
            for (int d = 1; d < 7; d++) {
                r.add(dow == d ? 1.0 : 0.0);
            }
        }
        double period = annualPeriod(season);
        for (int k = 1; k <= 2; k++) {
            double a = 2 * Math.PI * k * t / period;
            r.add(Math.sin(a));
            r.add(Math.cos(a));
        }
        r.add(Math.log1p(Math.max(0, lagged)));
        double[] out = new double[r.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = r.get(i);
        }
        return out;
    }

    /** Fits the regression to a history. */
    public static NbGlm.Fit fit(double[] y, int season) {
        int lag = lag(season);
        int n = y.length - lag;
        double[][] x = new double[n][];
        double[] target = new double[n];
        for (int t = lag; t < y.length; t++) {
            x[t - lag] = row(t, season, y[t - lag]);
            target[t - lag] = Math.max(0, Math.rint(y[t]));
        }
        return NbGlm.fit(x, target);
    }

    @Override
    public MethodForecast forecast(double[] y, int season, boolean counts, int[] horizons) {
        if (y.length < minHistory(season)) {
            throw new IllegalArgumentException("nb-glm needs at least " + minHistory(season) + " points");
        }
        NbGlm.Fit fit = fit(y, season);
        int lag = lag(season);
        int maxH = 0;
        for (int h : horizons) {
            maxH = Math.max(maxH, h);
        }
        double sum = 0;
        for (double v : y) {
            sum += v;
        }
        NbSampling sampler = new NbSampling(Seeds.of(ID, String.valueOf(y.length), String.valueOf(sum),
                String.valueOf(season)));
        double[][] chol = NbGlm.cholesky(fit.covariance());
        int paths = DRAWS * PATHS_PER_DRAW;
        double[][] samples = new double[maxH + 1][paths];
        double[] extended = new double[y.length + maxH];
        System.arraycopy(y, 0, extended, 0, y.length);
        int path = 0;
        for (int d = 0; d < DRAWS; d++) {
            double[] beta = sampler.multivariateNormal(fit.beta(), chol);
            for (int k = 0; k < PATHS_PER_DRAW; k++) {
                for (int s = 1; s <= maxH; s++) {
                    int t = y.length - 1 + s;
                    double[] r = row(t, season, extended[t - lag]);
                    double eta = 0;
                    for (int j = 0; j < beta.length; j++) {
                        eta += beta[j] * r[j];
                    }
                    double mu = Math.exp(Math.max(-30, Math.min(30, eta)));
                    int draw = sampler.negBin(mu, fit.theta());
                    extended[t] = draw;
                    samples[s][path] = draw;
                }
                path++;
            }
        }
        Map<Integer, double[]> out = new LinkedHashMap<>();
        for (int h : horizons) {
            out.put(h, QuantileSet.monotone(QuantileSet.empirical(samples[h])));
        }
        Map<String, Object> params = new LinkedHashMap<>();
        List<Double> coefficients = new ArrayList<>();
        for (double b : fit.beta()) {
            coefficients.add(QuantileSet.round(b));
        }
        params.put("coefficients", coefficients);
        params.put("theta", QuantileSet.round(fit.theta()));
        params.put("lag", lag);
        params.put("converged", fit.converged());
        params.put("draws", DRAWS);
        params.put("pathsPerDraw", PATHS_PER_DRAW);
        return new MethodForecast(ID, out, params);
    }
}
