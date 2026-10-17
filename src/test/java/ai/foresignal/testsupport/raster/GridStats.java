package ai.foresignal.testsupport.raster;

/**
 * The range of a decoded field (checkpoint 124 capture contract tests): the smallest and largest
 * finite samples, the number of NaN samples and the number of samples.
 *
 * @param min the smallest finite sample, or NaN when there is none
 * @param max the largest finite sample, or NaN when there is none
 * @param nan the NaN samples
 * @param count every sample
 */
public record GridStats(double min, double max, long nan, long count) {

    /** The statistics of {@code samples}. */
    public static GridStats of(float[] samples) {
        double min = Double.NaN;
        double max = Double.NaN;
        long nan = 0;
        for (float v : samples) {
            if (Float.isNaN(v)) {
                nan++;
            } else {
                min = Double.isNaN(min) ? v : Math.min(min, v);
                max = Double.isNaN(max) ? v : Math.max(max, v);
            }
        }
        return new GridStats(min, max, nan, samples.length);
    }
}
