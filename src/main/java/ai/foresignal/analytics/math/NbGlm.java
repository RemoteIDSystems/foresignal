package ai.foresignal.analytics.math;

import java.util.Arrays;

/**
 * A negative-binomial (NB2) regression with a log link, fitted by iteratively reweighted least squares (prompt 126,
 * Intelligence-Design §9 and §12; in-house, golden-tested).
 *
 * <p>The fit starts from a Poisson IRLS; the dispersion {@code θ} (the NB size, {@code Var = μ + μ²/θ}) is then
 * estimated by the method of moments from the Poisson fit, and the coefficients are refitted with NB weights; the
 * two steps alternate a few times. {@code θ} is capped at {@value #THETA_MAX} (a Poisson fit when the counts are not
 * overdispersed). An optional L2 penalty {@code λ·Σβ²} applies to every coefficient but the intercept (column 0), and
 * columns listed as non-negative are kept at {@code β ≥ 0} by an active-set loop (a negative coefficient is fixed at
 * zero and the rest refitted).
 *
 * <p>The returned covariance is {@code (XᵀWX + λP)⁻¹} at the solution, the usual Wald approximation used for the
 * parametric bootstrap of predictive quantiles and for per-coefficient significance.
 */
public final class NbGlm {

    /** The dispersion used when the counts are not overdispersed (effectively Poisson). */
    public static final double THETA_MAX = 1e6;

    /** The smallest dispersion. */
    public static final double THETA_MIN = 0.05;

    /** IRLS iterations per weighting. */
    static final int MAX_ITER = 50;

    /** Dispersion refinements. */
    static final int THETA_ROUNDS = 5;

    /** Convergence tolerance on the coefficients. */
    static final double TOL = 1e-8;

    /** The largest linear predictor (exp overflows above about 709). */
    static final double ETA_MAX = 30;

    private NbGlm() {}

    /**
     * A fit.
     *
     * @param beta the coefficients
     * @param covariance their covariance (Wald)
     * @param theta the NB size ({@value #THETA_MAX} for a Poisson fit)
     * @param mu the fitted means
     * @param iterations IRLS iterations used in the last weighting
     * @param converged true when the last weighting converged
     * @param logLikelihood the NB log-likelihood at the solution
     */
    public record Fit(double[] beta, double[][] covariance, double theta, double[] mu, int iterations,
            boolean converged, double logLikelihood) {

        /** The linear predictor of one row. */
        public double eta(double[] row) {
            double eta = 0;
            for (int j = 0; j < beta.length; j++) {
                eta += beta[j] * row[j];
            }
            return Math.max(-ETA_MAX, Math.min(ETA_MAX, eta));
        }

        /** The mean of one row. */
        public double mean(double[] row) {
            return Math.exp(eta(row));
        }

        /** The standard error of coefficient {@code j}. */
        public double standardError(int j) {
            double v = covariance[j][j];
            return v > 0 ? Math.sqrt(v) : Double.NaN;
        }
    }

    /** Fits a Poisson-start NB regression without penalty. */
    public static Fit fit(double[][] x, double[] y) {
        return fit(x, y, 0, null, true);
    }

    /**
     * Fits a regression.
     *
     * @param x the design (rows × columns; column 0 is the intercept)
     * @param y the counts (non-negative)
     * @param lambda the L2 penalty on every column but 0
     * @param nonNegative the columns kept at {@code β ≥ 0}, or null
     * @param negativeBinomial false for a Poisson fit (no dispersion step)
     * @throws IllegalArgumentException when the design is empty or its shape does not match
     */
    public static Fit fit(double[][] x, double[] y, double lambda, boolean[] nonNegative, boolean negativeBinomial) {
        if (x.length == 0 || x.length != y.length) {
            throw new IllegalArgumentException("the design and the counts must be non-empty and of equal length");
        }
        int p = x[0].length;
        boolean[] active = new boolean[p];
        Arrays.fill(active, true);
        Fit fit = null;
        for (int round = 0; round <= p; round++) {
            fit = fitActive(x, y, lambda, active, negativeBinomial);
            if (nonNegative == null) {
                return fit;
            }
            int worst = -1;
            double worstValue = 0;
            for (int j = 1; j < p; j++) {
                if (active[j] && j < nonNegative.length && nonNegative[j] && fit.beta()[j] < worstValue) {
                    worst = j;
                    worstValue = fit.beta()[j];
                }
            }
            if (worst < 0) {
                return fit;
            }
            active[worst] = false;
        }
        return fit;
    }

    private static Fit fitActive(double[][] x, double[] y, double lambda, boolean[] active, boolean negativeBinomial) {
        int n = y.length;
        int p = x[0].length;
        double mean = 0;
        for (double v : y) {
            mean += v;
        }
        mean = Math.max(mean / n, 1e-3);
        double[] beta = new double[p];
        beta[0] = Math.log(mean);
        double theta = THETA_MAX;
        IrlsResult r = irls(x, y, beta, theta, lambda, active);
        if (negativeBinomial) {
            for (int round = 0; round < THETA_ROUNDS; round++) {
                double next = momentTheta(y, r.mu, p);
                boolean stable = Math.abs(Math.log(next) - Math.log(theta)) < 1e-4;
                theta = next;
                r = irls(x, y, r.beta, theta, lambda, active);
                if (stable) {
                    break;
                }
            }
        }
        double ll = logLikelihood(y, r.mu, theta);
        return new Fit(r.beta, r.covariance, theta, r.mu, r.iterations, r.converged, ll);
    }

    private record IrlsResult(double[] beta, double[][] covariance, double[] mu, int iterations, boolean converged) {}

    private static IrlsResult irls(double[][] x, double[] y, double[] start, double theta, double lambda,
            boolean[] active) {
        int n = y.length;
        int p = x[0].length;
        double[] beta = start.clone();
        for (int j = 0; j < p; j++) {
            if (!active[j]) {
                beta[j] = 0;
            }
        }
        double[] mu = new double[n];
        double[][] info = new double[p][p];
        boolean converged = false;
        int iter = 0;
        for (; iter < MAX_ITER; iter++) {
            double[][] xtwx = new double[p][p];
            double[] xtwz = new double[p];
            for (int i = 0; i < n; i++) {
                double eta = 0;
                for (int j = 0; j < p; j++) {
                    eta += beta[j] * x[i][j];
                }
                eta = Math.max(-ETA_MAX, Math.min(ETA_MAX, eta));
                double m = Math.exp(eta);
                mu[i] = m;
                double w = m / (1 + m / theta);
                double z = eta + (y[i] - m) / m;
                for (int a = 0; a < p; a++) {
                    if (!active[a]) {
                        continue;
                    }
                    double xa = x[i][a] * w;
                    xtwz[a] += xa * z;
                    for (int b = a; b < p; b++) {
                        if (active[b]) {
                            xtwx[a][b] += xa * x[i][b];
                        }
                    }
                }
            }
            for (int a = 0; a < p; a++) {
                for (int b = 0; b < a; b++) {
                    xtwx[a][b] = xtwx[b][a];
                }
                if (a > 0 && active[a]) {
                    xtwx[a][a] += lambda;
                }
                if (!active[a]) {
                    xtwx[a][a] = 1;
                    xtwz[a] = 0;
                }
            }
            // A tiny ridge keeps a rank-deficient design (a constant column, an all-zero lag) solvable.
            for (int a = 0; a < p; a++) {
                xtwx[a][a] += 1e-9;
            }
            double[] next = solve(xtwx, xtwz);
            if (next == null) {
                break;
            }
            double delta = 0;
            for (int j = 0; j < p; j++) {
                if (!active[j]) {
                    next[j] = 0;
                }
                delta = Math.max(delta, Math.abs(next[j] - beta[j]));
            }
            beta = next;
            info = xtwx;
            if (delta < TOL * (1 + max(beta))) {
                converged = true;
                iter++;
                break;
            }
        }
        for (int i = 0; i < n; i++) {
            double eta = 0;
            for (int j = 0; j < p; j++) {
                eta += beta[j] * x[i][j];
            }
            mu[i] = Math.exp(Math.max(-ETA_MAX, Math.min(ETA_MAX, eta)));
        }
        double[][] cov = invert(info);
        if (cov == null) {
            cov = new double[p][p];
        }
        for (int j = 0; j < p; j++) {
            if (!active[j]) {
                Arrays.fill(cov[j], 0);
                for (int a = 0; a < p; a++) {
                    cov[a][j] = 0;
                }
            }
        }
        return new IrlsResult(beta, cov, mu, iter, converged);
    }

    /**
     * The method-of-moments dispersion {@code θ = Σμ² / Σ((y − μ)² − μ)}, bounded to [{@value #THETA_MIN},
     * {@value #THETA_MAX}] (the maximum when the counts are not overdispersed).
     */
    public static double momentTheta(double[] y, double[] mu, int parameters) {
        double num = 0;
        double den = 0;
        for (int i = 0; i < y.length; i++) {
            double r = y[i] - mu[i];
            num += mu[i] * mu[i];
            den += r * r - mu[i];
        }
        double scale = y.length > parameters ? (double) y.length / (y.length - parameters) : 1;
        den *= scale;
        if (!(den > 0)) {
            return THETA_MAX;
        }
        return Math.max(THETA_MIN, Math.min(THETA_MAX, num / den));
    }

    /** The NB2 log-likelihood of counts under means {@code mu} and size {@code theta}. */
    public static double logLikelihood(double[] y, double[] mu, double theta) {
        double ll = 0;
        for (int i = 0; i < y.length; i++) {
            ll += logPmf(y[i], mu[i], theta);
        }
        return ll;
    }

    /** The NB2 log-probability of a count (Poisson for very large {@code θ}). */
    public static double logPmf(double y, double mu, double theta) {
        double k = Math.max(0, Math.rint(y));
        double m = Math.max(mu, 1e-12);
        if (theta >= THETA_MAX) {
            return k * Math.log(m) - m - org.apache.commons.numbers.gamma.LogGamma.value(k + 1);
        }
        return org.apache.commons.numbers.gamma.LogGamma.value(k + theta)
                - org.apache.commons.numbers.gamma.LogGamma.value(theta)
                - org.apache.commons.numbers.gamma.LogGamma.value(k + 1)
                + theta * Math.log(theta / (theta + m)) + k * Math.log(m / (theta + m));
    }

    /** {@code P(Y ≤ k)} for an NB2 count with mean {@code mu} and size {@code theta}. */
    public static double cdf(double k, double mu, double theta) {
        if (k < 0) {
            return 0;
        }
        if (!(mu > 0)) {
            return 1;
        }
        if (theta >= THETA_MAX) {
            return CountTails.poissonAtMost(k, mu);
        }
        return CountTails.negBinAtMost(k, new CountTails.NegBin(theta, theta / (theta + mu)));
    }

    /** The smallest count {@code k} with {@code P(Y ≤ k) ≥ q}. */
    public static int quantile(double q, double mu, double theta) {
        if (!(mu > 0)) {
            return 0;
        }
        double sd = Math.sqrt(mu + (theta >= THETA_MAX ? 0 : mu * mu / theta));
        int hi = (int) Math.min(Integer.MAX_VALUE / 2.0, Math.ceil(mu + 20 * sd + 10));
        int lo = 0;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (cdf(mid, mu, theta) >= q) {
                hi = mid;
            } else {
                lo = mid + 1;
            }
        }
        return lo;
    }

    // ------------------------------------------------------------------------- linear algebra

    /** Solves {@code A·x = b} by Gaussian elimination with partial pivoting, or null when singular. */
    public static double[] solve(double[][] a, double[] b) {
        int n = b.length;
        double[][] m = new double[n][n + 1];
        for (int i = 0; i < n; i++) {
            System.arraycopy(a[i], 0, m[i], 0, n);
            m[i][n] = b[i];
        }
        for (int col = 0; col < n; col++) {
            int pivot = col;
            for (int r = col + 1; r < n; r++) {
                if (Math.abs(m[r][col]) > Math.abs(m[pivot][col])) {
                    pivot = r;
                }
            }
            if (Math.abs(m[pivot][col]) < 1e-300) {
                return null;
            }
            double[] tmp = m[col];
            m[col] = m[pivot];
            m[pivot] = tmp;
            for (int r = col + 1; r < n; r++) {
                double f = m[r][col] / m[col][col];
                if (f != 0) {
                    for (int c = col; c <= n; c++) {
                        m[r][c] -= f * m[col][c];
                    }
                }
            }
        }
        double[] x = new double[n];
        for (int r = n - 1; r >= 0; r--) {
            double s = m[r][n];
            for (int c = r + 1; c < n; c++) {
                s -= m[r][c] * x[c];
            }
            x[r] = s / m[r][r];
            if (!Double.isFinite(x[r])) {
                return null;
            }
        }
        return x;
    }

    /** The inverse of a square matrix by Gauss–Jordan elimination, or null when singular. */
    public static double[][] invert(double[][] a) {
        int n = a.length;
        double[][] m = new double[n][2 * n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(a[i], 0, m[i], 0, n);
            m[i][n + i] = 1;
        }
        for (int col = 0; col < n; col++) {
            int pivot = col;
            for (int r = col + 1; r < n; r++) {
                if (Math.abs(m[r][col]) > Math.abs(m[pivot][col])) {
                    pivot = r;
                }
            }
            if (Math.abs(m[pivot][col]) < 1e-300) {
                return null;
            }
            double[] tmp = m[col];
            m[col] = m[pivot];
            m[pivot] = tmp;
            double d = m[col][col];
            for (int c = 0; c < 2 * n; c++) {
                m[col][c] /= d;
            }
            for (int r = 0; r < n; r++) {
                if (r != col && m[r][col] != 0) {
                    double f = m[r][col];
                    for (int c = 0; c < 2 * n; c++) {
                        m[r][c] -= f * m[col][c];
                    }
                }
            }
        }
        double[][] out = new double[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(m[i], n, out[i], 0, n);
        }
        return out;
    }

    /**
     * The lower Cholesky factor of a symmetric positive semi-definite matrix (tiny negative pivots are treated as
     * zero), used to draw correlated normal coefficients for the parametric bootstrap.
     */
    public static double[][] cholesky(double[][] a) {
        int n = a.length;
        double[][] l = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j <= i; j++) {
                double s = a[i][j];
                for (int k = 0; k < j; k++) {
                    s -= l[i][k] * l[j][k];
                }
                if (i == j) {
                    l[i][i] = s > 0 ? Math.sqrt(s) : 0;
                } else {
                    l[i][j] = l[j][j] > 0 ? s / l[j][j] : 0;
                }
            }
        }
        return l;
    }

    private static double max(double[] v) {
        double m = 0;
        for (double x : v) {
            m = Math.max(m, Math.abs(x));
        }
        return m;
    }
}
