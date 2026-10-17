package ai.foresignal.forecast.score;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Calibration of binary forecasts (prompt 122, Intelligence-Design §8.3): the Murphy decomposition of the Brier score,
 * CORP reliability by pool-adjacent-violators, the 10-bin reliability table and sharpness. Pure functions of the final
 * forecasts {@code p} and the outcomes {@code o} (1 or 0).
 */
public final class Calibration {

    /** The reliability bins. */
    public static final int BINS = 10;

    /** The resolved count below which the 10-bin table replaces the CORP curve. */
    public static final int CORP_MIN = 50;

    private Calibration() {}

    /**
     * The Murphy decomposition of the Brier score over 10 equal-width bins: {@code BS = REL − RES + UNC + WBV − 2 WBC},
     * where the within-bin variance and covariance terms are kept separately so the identity is exact.
     *
     * @param brier the mean Brier score
     * @param reliability {@code (1/N) Σ nₖ (f̄ₖ − ōₖ)²}
     * @param resolution {@code (1/N) Σ nₖ (ōₖ − ō)²}
     * @param uncertainty {@code ō (1 − ō)}
     * @param withinBinVariance {@code (1/N) Σₖ Σᵢ (fᵢ − f̄ₖ)²}
     * @param withinBinCovariance {@code (1/N) Σₖ Σᵢ (fᵢ − f̄ₖ)(oᵢ − ōₖ)}
     */
    public record Murphy(double brier, double reliability, double resolution, double uncertainty,
            double withinBinVariance, double withinBinCovariance) {}

    /** The Murphy decomposition, or null without forecasts. */
    public static Murphy murphy(double[] p, double[] o) {
        int n = p.length;
        if (n == 0) {
            return null;
        }
        double[] fSum = new double[BINS];
        double[] oSum = new double[BINS];
        int[] count = new int[BINS];
        double oBar = 0;
        double brier = 0;
        for (int i = 0; i < n; i++) {
            int b = bin(p[i]);
            fSum[b] += p[i];
            oSum[b] += o[i];
            count[b]++;
            oBar += o[i];
            brier += (p[i] - o[i]) * (p[i] - o[i]);
        }
        oBar /= n;
        brier /= n;
        double rel = 0;
        double res = 0;
        for (int b = 0; b < BINS; b++) {
            if (count[b] == 0) {
                continue;
            }
            double fk = fSum[b] / count[b];
            double ok = oSum[b] / count[b];
            rel += count[b] * (fk - ok) * (fk - ok);
            res += count[b] * (ok - oBar) * (ok - oBar);
        }
        double wbv = 0;
        double wbc = 0;
        for (int i = 0; i < n; i++) {
            int b = bin(p[i]);
            double fk = fSum[b] / count[b];
            double ok = oSum[b] / count[b];
            wbv += (p[i] - fk) * (p[i] - fk);
            wbc += (p[i] - fk) * (o[i] - ok);
        }
        return new Murphy(brier, rel / n, res / n, oBar * (1 - oBar), wbv / n, wbc / n);
    }

    /** The bin of a probability: {@code [0, 0.1), …, [0.9, 1]}. */
    static int bin(double p) {
        return Math.min(BINS - 1, Math.max(0, (int) Math.floor(p * BINS)));
    }

    /**
     * One reliability bin.
     *
     * @param lower the lower edge
     * @param upper the upper edge
     * @param n the forecasts in it
     * @param meanForecast their mean, or null when empty
     * @param observedFrequency the share that happened, or null when empty
     */
    public record Bin(double lower, double upper, int n, Double meanForecast, Double observedFrequency) {}

    /** The 10-bin reliability table. */
    public static List<Bin> bins(double[] p, double[] o) {
        double[] fSum = new double[BINS];
        double[] oSum = new double[BINS];
        int[] count = new int[BINS];
        for (int i = 0; i < p.length; i++) {
            int b = bin(p[i]);
            fSum[b] += p[i];
            oSum[b] += o[i];
            count[b]++;
        }
        List<Bin> out = new ArrayList<>();
        for (int b = 0; b < BINS; b++) {
            out.add(new Bin(b / (double) BINS, (b + 1) / (double) BINS, count[b],
                    count[b] == 0 ? null : fSum[b] / count[b], count[b] == 0 ? null : oSum[b] / count[b]));
        }
        return out;
    }

    /**
     * A point of the CORP curve.
     *
     * @param forecast the forecast value
     * @param calibrated the recalibrated (isotonic) probability
     */
    public record Point(double forecast, double calibrated) {}

    /**
     * The CORP result (Dimitriadis, Gneiting and Jordan 2021): the PAV-recalibrated curve and the decomposition of the
     * mean Brier score {@code S = MCB − DSC + UNC}, where {@code MCB = S(f) − S(x̂)}, {@code DSC = S(ō) − S(x̂)} and
     * {@code UNC = S(ō)}.
     *
     * @param curve one point per distinct forecast value, in forecast order
     * @param calibrated the recalibrated probability of each input, in input order
     * @param score the mean Brier score of the forecasts
     * @param miscalibration MCB
     * @param discrimination DSC
     * @param uncertainty UNC
     */
    public record Corp(List<Point> curve, double[] calibrated, double score, double miscalibration,
            double discrimination, double uncertainty) {}

    /** The CORP result, or null without forecasts. */
    public static Corp corp(double[] p, double[] o) {
        int n = p.length;
        if (n == 0) {
            return null;
        }
        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) {
            order[i] = i;
        }
        Arrays.sort(order, Comparator.comparingDouble((Integer i) -> p[i]).thenComparingInt(i -> i));
        // Pool equal forecast values first (CORP treats ties as one block), then run PAV over the blocks.
        List<double[]> blocks = new ArrayList<>(); // {sumO, count, firstIndexInOrder, lastIndexInOrder, forecast}
        for (int k = 0; k < n; k++) {
            int i = order[k];
            if (!blocks.isEmpty() && blocks.get(blocks.size() - 1)[4] == p[i]) {
                double[] last = blocks.get(blocks.size() - 1);
                last[0] += o[i];
                last[1]++;
                last[3] = k;
            } else {
                blocks.add(new double[] {o[i], 1, k, k, p[i]});
            }
        }
        double[] fitted = pav(blocks);
        double[] calibrated = new double[n];
        List<Point> curve = new ArrayList<>();
        for (int b = 0; b < blocks.size(); b++) {
            double[] block = blocks.get(b);
            for (int k = (int) block[2]; k <= (int) block[3]; k++) {
                calibrated[order[k]] = fitted[b];
            }
            curve.add(new Point(block[4], fitted[b]));
        }
        double oBar = 0;
        for (double v : o) {
            oBar += v;
        }
        oBar /= n;
        double sF = 0;
        double sX = 0;
        double sRef = 0;
        for (int i = 0; i < n; i++) {
            sF += (p[i] - o[i]) * (p[i] - o[i]);
            sX += (calibrated[i] - o[i]) * (calibrated[i] - o[i]);
            sRef += (oBar - o[i]) * (oBar - o[i]);
        }
        sF /= n;
        sX /= n;
        sRef /= n;
        return new Corp(curve, calibrated, sF, sF - sX, sRef - sX, sRef);
    }

    /**
     * Pool-adjacent-violators over ordered blocks ({@code {sum, count, …}}): the non-decreasing step function that
     * minimizes squared error, one fitted value per input block.
     */
    static double[] pav(List<double[]> blocks) {
        int m = blocks.size();
        double[] sum = new double[m];
        double[] weight = new double[m];
        int[] size = new int[m];
        int top = -1;
        for (double[] block : blocks) {
            top++;
            sum[top] = block[0];
            weight[top] = block[1];
            size[top] = 1;
            while (top > 0 && sum[top - 1] / weight[top - 1] > sum[top] / weight[top]) {
                sum[top - 1] += sum[top];
                weight[top - 1] += weight[top];
                size[top - 1] += size[top];
                top--;
            }
        }
        double[] out = new double[m];
        int at = 0;
        for (int s = 0; s <= top; s++) {
            double value = sum[s] / weight[s];
            for (int k = 0; k < size[s]; k++) {
                out[at++] = value;
            }
        }
        return out;
    }

    /** Sharpness: the variance of the forecasts (0 for a forecaster that always says the same). */
    public static Double sharpness(double[] p) {
        if (p.length == 0) {
            return null;
        }
        double mean = 0;
        for (double v : p) {
            mean += v;
        }
        mean /= p.length;
        double var = 0;
        for (double v : p) {
            var += (v - mean) * (v - mean);
        }
        return var / p.length;
    }
}
