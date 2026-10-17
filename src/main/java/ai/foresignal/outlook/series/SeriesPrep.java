package ai.foresignal.outlook.series;

import java.util.Arrays;

/**
 * Prepares a series for the forecasting methods (prompt 126): leading gaps are dropped and every inner gap (a missing
 * or uncovered bucket, NaN) is filled with the value one season earlier, else the previous value, so no method ever
 * sees a fabricated trend. Pure.
 */
public final class SeriesPrep {

    private SeriesPrep() {}

    /** The series with leading NaNs removed and inner NaNs filled (an empty array when nothing is usable). */
    public static double[] fill(double[] values, int season) {
        int first = 0;
        while (first < values.length && !Double.isFinite(values[first])) {
            first++;
        }
        if (first == values.length) {
            return new double[0];
        }
        double[] out = Arrays.copyOfRange(values, first, values.length);
        for (int i = 1; i < out.length; i++) {
            if (!Double.isFinite(out[i])) {
                out[i] = season > 1 && i >= season && Double.isFinite(out[i - season]) ? out[i - season] : out[i - 1];
            }
        }
        return out;
    }

    /** The number of finite values. */
    public static int usable(double[] values) {
        int n = 0;
        for (double v : values) {
            if (Double.isFinite(v)) {
                n++;
            }
        }
        return n;
    }

    /** {@code log1p} of every value (counts are floored at 0 first). */
    public static double[] log1p(double[] values) {
        double[] out = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            out[i] = Math.log1p(Math.max(0, values[i]));
        }
        return out;
    }
}
