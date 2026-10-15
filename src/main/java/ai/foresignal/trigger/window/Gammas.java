package ai.foresignal.trigger.window;

/**
 * The regularized incomplete gamma functions (prompt 100), used for the Poisson tail. Series (P) and
 * continued fraction (Q) after Numerical Recipes; accurate to about 1e-14 for the ranges used here.
 */
public final class Gammas {

    private static final int MAX_ITERATIONS = 1000;
    private static final double EPSILON = 1e-15;
    private static final double TINY = 1e-300;

    private Gammas() {}

    /** The log of the gamma function (Lanczos). */
    public static double logGamma(double x) {
        double[] coefficients = {76.18009172947146, -86.50532032941677, 24.01409824083091,
            -1.231739572450155, 0.1208650973866179e-2, -0.5395239384953e-5};
        double y = x;
        double tmp = x + 5.5;
        tmp -= (x + 0.5) * Math.log(tmp);
        double ser = 1.000000000190015;
        for (int j = 0; j < 6; j++) {
            ser += coefficients[j] / ++y;
        }
        return -tmp + Math.log(2.5066282746310005 * ser / x);
    }

    /** The lower regularized gamma {@code P(a, x)}. */
    public static double lower(double a, double x) {
        if (x < 0 || a <= 0) {
            return 0;
        }
        if (x == 0) {
            return 0;
        }
        if (x < a + 1) {
            return series(a, x);
        }
        return 1 - continuedFraction(a, x);
    }

    /** The upper regularized gamma {@code Q(a, x) = 1 - P(a, x)}. */
    public static double upper(double a, double x) {
        return 1 - lower(a, x);
    }

    private static double series(double a, double x) {
        double ap = a;
        double sum = 1.0 / a;
        double del = sum;
        for (int n = 1; n <= MAX_ITERATIONS; n++) {
            ap += 1;
            del *= x / ap;
            sum += del;
            if (Math.abs(del) < Math.abs(sum) * EPSILON) {
                break;
            }
        }
        return sum * Math.exp(-x + a * Math.log(x) - logGamma(a));
    }

    private static double continuedFraction(double a, double x) {
        double b = x + 1 - a;
        double c = 1 / TINY;
        double d = 1 / b;
        double h = d;
        for (int i = 1; i <= MAX_ITERATIONS; i++) {
            double an = -i * (i - a);
            b += 2;
            d = an * d + b;
            if (Math.abs(d) < TINY) {
                d = TINY;
            }
            c = b + an / c;
            if (Math.abs(c) < TINY) {
                c = TINY;
            }
            d = 1 / d;
            double del = d * c;
            h *= del;
            if (Math.abs(del - 1) < EPSILON) {
                break;
            }
        }
        return Math.exp(-x + a * Math.log(x) - logGamma(a)) * h;
    }
}
