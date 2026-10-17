package ai.foresignal.outlook.series;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hipparchus.optim.InitialGuess;
import org.hipparchus.optim.MaxEval;
import org.hipparchus.optim.PointValuePair;
import org.hipparchus.optim.SimpleBounds;
import org.hipparchus.optim.nonlinear.scalar.GoalType;
import org.hipparchus.optim.nonlinear.scalar.ObjectiveFunction;
import org.hipparchus.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;

/**
 * Additive-error exponential smoothing in state-space form (prompt 126; Hyndman, Koehler, Ord and Snyder,
 * <i>Forecasting with Exponential Smoothing</i>, 2008, class 1): ETS(A,N,N), ETS(A,A,N), ETS(A,Ad,N) and the
 * additive-season variants. Pure and deterministic.
 *
 * <p>Recursions, with {@code φ = 1} for an undamped trend and no trend term without one:
 * <pre>
 *   ŷ(t) = l(t−1) + φ·b(t−1) + s(t−m)        e(t) = y(t) − ŷ(t)
 *   l(t) = l(t−1) + φ·b(t−1) + α·e(t)
 *   b(t) = φ·b(t−1) + β·e(t)
 *   s(t) = s(t−m) + γ·e(t)
 * </pre>
 * The forecast mean {@code h} steps ahead is {@code l(T) + φ_h·b(T) + s(T+h−m(k+1))}, {@code φ_h = φ + … + φ^h}
 * ({@code h} undamped), and its variance {@code σ²·(1 + Σ_{j=1}^{h−1} c_j²)} with {@code c_j = α + β·φ_j + γ·d_j},
 * {@code d_j = 1} when {@code j} is a multiple of {@code m} (Hyndman et al. 2008, Table 6.1).
 *
 * <p>Parameters are estimated by maximum likelihood: the Gaussian likelihood concentrated in {@code σ² = SSE/n}, so
 * the objective is {@code n·ln(SSE/n)}, minimized with the Hipparchus BOBYQA bounded optimizer over a unit box mapped
 * to the admissible region {@code 0 < α < 1}, {@code 0 < β < α}, {@code 0 < γ < 1 − α}, {@code 0.8 ≤ φ ≤ 0.98}.
 * ETS(A,N,N) also estimates its initial level; the other forms take their initial states from the classical
 * decomposition of the first seasons.
 */
public final class EtsModel {

    /** The trend forms. */
    public enum Trend {
        /** No trend. */
        NONE,
        /** An additive trend. */
        ADDITIVE,
        /** An additive damped trend. */
        DAMPED
    }

    /**
     * A model form.
     *
     * @param trend the trend
     * @param season the season length (1 for none)
     */
    public record Spec(Trend trend, int season) {

        /** True with a seasonal component. */
        public boolean seasonal() {
            return season > 1;
        }
    }

    /**
     * Parameters and initial states.
     *
     * @param alpha the level smoothing
     * @param beta the trend smoothing (0 without a trend)
     * @param gamma the season smoothing (0 without a season)
     * @param phi the damping (1 undamped)
     * @param l0 the initial level
     * @param b0 the initial trend
     * @param s0 the initial seasonal states, one per slot of the first season (empty without a season)
     */
    public record Params(double alpha, double beta, double gamma, double phi, double l0, double b0, double[] s0) {}

    /**
     * The end state of a filtered series.
     *
     * @param sse the sum of squared one-step errors
     * @param level the last level
     * @param trend the last trend
     * @param season the seasonal states by slot ({@code t mod m})
     * @param n the number of observations
     */
    public record State(double sse, double level, double trend, double[] season, int n) {}

    /**
     * A fitted model.
     *
     * @param spec the form
     * @param params the estimated parameters
     * @param sigma2 the innovation variance {@code SSE/n}
     * @param state the end state
     * @param logLikelihood the Gaussian log-likelihood at the estimate
     */
    public record Fit(Spec spec, Params params, double sigma2, State state, double logLikelihood) {

        /** The forecast mean {@code h} steps ahead. */
        public double mean(int h) {
            return EtsModel.mean(spec, params, state, h);
        }

        /** The forecast variance {@code h} steps ahead. */
        public double variance(int h) {
            return EtsModel.variance(spec, params, sigma2, h);
        }

        /** The parameters as a map. */
        public Map<String, Object> view() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("alpha", QuantileSet.round(params.alpha()));
            if (spec.trend() != Trend.NONE) {
                out.put("beta", QuantileSet.round(params.beta()));
            }
            if (spec.seasonal()) {
                out.put("gamma", QuantileSet.round(params.gamma()));
                out.put("season", spec.season());
            }
            if (spec.trend() == Trend.DAMPED) {
                out.put("phi", QuantileSet.round(params.phi()));
            }
            out.put("l0", QuantileSet.round(params.l0()));
            out.put("sigma2", QuantileSet.round(sigma2));
            out.put("logLikelihood", QuantileSet.round(logLikelihood));
            return out;
        }
    }

    /** The lower bound of every unit parameter. */
    static final double LO = 1e-4;

    /** The upper bound of every unit parameter. */
    static final double HI = 1 - 1e-4;

    /** The optimizer's evaluation budget. */
    static final int MAX_EVAL = 4_000;

    private EtsModel() {}

    /** Filters {@code y} through the model with fixed parameters. */
    public static State filter(double[] y, Spec spec, Params p) {
        int m = Math.max(1, spec.season());
        double[] season = new double[m];
        if (spec.seasonal()) {
            System.arraycopy(p.s0(), 0, season, 0, m);
        }
        double phi = spec.trend() == Trend.DAMPED ? p.phi() : 1;
        boolean trend = spec.trend() != Trend.NONE;
        double l = p.l0();
        double b = trend ? p.b0() : 0;
        double sse = 0;
        for (int t = 0; t < y.length; t++) {
            int slot = t % m;
            double s = spec.seasonal() ? season[slot] : 0;
            double damped = trend ? phi * b : 0;
            double e = y[t] - (l + damped + s);
            sse += e * e;
            l = l + damped + p.alpha() * e;
            if (trend) {
                b = damped + p.beta() * e;
            }
            if (spec.seasonal()) {
                season[slot] = s + p.gamma() * e;
            }
        }
        return new State(sse, l, b, season, y.length);
    }

    /** The forecast mean {@code h} steps after the end state. */
    public static double mean(Spec spec, Params p, State state, int h) {
        double out = state.level() + dampSum(spec, p, h) * state.trend();
        if (spec.seasonal()) {
            int slot = (state.n() - 1 + h) % spec.season();
            out += state.season()[slot];
        }
        return out;
    }

    /** {@code φ_h}: {@code φ + … + φ^h} damped, {@code h} undamped, 0 without a trend. */
    public static double dampSum(Spec spec, Params p, int h) {
        return switch (spec.trend()) {
            case NONE -> 0;
            case ADDITIVE -> h;
            case DAMPED -> {
                double s = 0;
                double f = 1;
                for (int i = 1; i <= h; i++) {
                    f *= p.phi();
                    s += f;
                }
                yield s;
            }
        };
    }

    /** The forecast variance {@code σ²·(1 + Σ_{j=1}^{h−1} c_j²)}. */
    public static double variance(Spec spec, Params p, double sigma2, int h) {
        double sum = 1;
        for (int j = 1; j < h; j++) {
            double c = p.alpha() + p.beta() * dampSum(spec, p, j)
                    + (spec.seasonal() && j % spec.season() == 0 ? p.gamma() : 0);
            sum += c * c;
        }
        return sigma2 * sum;
    }

    /** Classical initial states and default smoothing parameters. */
    public static Params initial(double[] y, Spec spec) {
        int m = Math.max(1, spec.season());
        double[] s0 = new double[spec.seasonal() ? m : 0];
        double l0;
        double b0 = 0;
        if (spec.seasonal()) {
            double first = mean(y, 0, m);
            if (spec.trend() != Trend.NONE && y.length >= 2 * m) {
                b0 = (mean(y, m, 2 * m) - first) / m;
            }
            l0 = first - b0 * (m + 1) / 2.0;
            double sum = 0;
            for (int i = 0; i < m; i++) {
                s0[i] = y[i] - (first + b0 * (i - (m - 1) / 2.0));
                sum += s0[i];
            }
            for (int i = 0; i < m; i++) {
                s0[i] -= sum / m;
            }
        } else {
            if (spec.trend() != Trend.NONE && y.length >= 2) {
                int k = Math.min(y.length - 1, 4);
                b0 = (y[k] - y[0]) / k;
            }
            l0 = y[0] - b0;
        }
        return new Params(0.3, spec.trend() == Trend.NONE ? 0 : 0.09, spec.seasonal() ? 0.14 : 0,
                spec.trend() == Trend.DAMPED ? 0.89 : 1, l0, b0, s0);
    }

    /**
     * Fits the model by maximum likelihood.
     *
     * @param estimateLevel true to estimate the initial level too (ETS(A,N,N))
     * @throws IllegalArgumentException when {@code y} has fewer than 3 points
     */
    public static Fit fit(double[] y, Spec spec, boolean estimateLevel) {
        if (y.length < 3) {
            throw new IllegalArgumentException("ETS needs at least 3 points");
        }
        Params init = initial(y, spec);
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (double v : y) {
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        double range = max - min;
        double levelLo = range > 0 ? min - 0.1 * range : min - 1;
        double levelHi = range > 0 ? max + 0.1 * range : max + 1;
        List<String> names = new ArrayList<>();
        names.add("alpha");
        if (spec.trend() != Trend.NONE) {
            names.add("beta");
        }
        if (spec.seasonal()) {
            names.add("gamma");
        }
        if (spec.trend() == Trend.DAMPED) {
            names.add("phi");
        }
        if (estimateLevel) {
            names.add("l0");
        }
        if (names.size() < BOBYQAOptimizer.MINIMUM_PROBLEM_DIMENSION) {
            names.add("l0");
        }
        int dim = names.size();
        double[] x0 = new double[dim];
        for (int i = 0; i < dim; i++) {
            x0[i] = switch (names.get(i)) {
                case "alpha" -> 0.3;
                case "beta" -> 0.3;
                case "gamma" -> 0.2;
                case "phi" -> 0.5;
                default -> clamp((init.l0() - levelLo) / (levelHi - levelLo));
            };
        }
        double[] lo = new double[dim];
        double[] hi = new double[dim];
        java.util.Arrays.fill(lo, LO);
        java.util.Arrays.fill(hi, HI);
        java.util.function.Function<double[], Params> decode = u -> {
            double alpha = LO;
            double beta = 0;
            double gamma = 0;
            double phi = 1;
            double l0 = init.l0();
            double bu = 0;
            double gu = 0;
            for (int i = 0; i < dim; i++) {
                switch (names.get(i)) {
                    case "alpha" -> alpha = u[i];
                    case "beta" -> bu = u[i];
                    case "gamma" -> gu = u[i];
                    case "phi" -> phi = 0.8 + 0.18 * u[i];
                    default -> l0 = levelLo + u[i] * (levelHi - levelLo);
                }
            }
            if (spec.trend() != Trend.NONE) {
                beta = bu * alpha;
            }
            if (spec.seasonal()) {
                gamma = gu * (1 - alpha);
            }
            return new Params(alpha, beta, gamma, spec.trend() == Trend.DAMPED ? phi : 1, l0, init.b0(), init.s0());
        };
        org.hipparchus.analysis.MultivariateFunction objective = u -> {
            State s = filter(y, spec, decode.apply(u));
            double sse = s.sse();
            if (!Double.isFinite(sse)) {
                return 1e300;
            }
            return y.length * Math.log(Math.max(sse, 1e-300) / y.length);
        };
        double[] best = x0;
        try {
            BOBYQAOptimizer optimizer = new BOBYQAOptimizer(2 * dim + 1, 0.1, 1e-7);
            PointValuePair result = optimizer.optimize(new MaxEval(MAX_EVAL), new ObjectiveFunction(objective),
                    GoalType.MINIMIZE, new InitialGuess(x0), new SimpleBounds(lo, hi));
            if (result != null && Double.isFinite(result.getValue())
                    && result.getValue() <= objective.value(x0)) {
                best = result.getPoint();
            }
        } catch (RuntimeException e) {
            best = x0;
        }
        Params params = decode.apply(best);
        State state = filter(y, spec, params);
        double sigma2 = Math.max(state.sse() / y.length, 1e-12);
        double ll = -0.5 * y.length * (Math.log(2 * Math.PI * sigma2) + 1);
        return new Fit(spec, params, sigma2, state, ll);
    }

    /**
     * The nine quantiles of a fitted model {@code h} steps ahead, back-transformed with {@code expm1} and floored at
     * zero for counts fitted on {@code log1p}.
     */
    public static double[] quantiles(Fit fit, int h, boolean counts) {
        double[] q = QuantileSet.normal(fit.mean(h), Math.sqrt(fit.variance(h)));
        if (counts) {
            q = QuantileSet.floor(QuantileSet.map(q, Math::expm1), 0);
        }
        return QuantileSet.monotone(q);
    }

    private static double mean(double[] y, int from, int to) {
        double s = 0;
        for (int i = from; i < to; i++) {
            s += y[i];
        }
        return s / (to - from);
    }

    private static double clamp(double u) {
        return Math.max(LO, Math.min(HI, Double.isFinite(u) ? u : 0.5));
    }
}
