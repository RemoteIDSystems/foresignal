package ai.foresignal.source.adapters.goes;

/**
 * The GOES IR code space (prompt 046, Weather-Imagery-Design §2.4): brightness temperature in kelvin
 * to u16 codes with scale 0.01 and offset 150, so {@code code = round((K − 150) × 100)} and
 * {@code K = code × 0.01 + 150}. A missing sample ({@code NaN}: the CMI {@code _FillValue}, or a DQF
 * above 1) is {@value #NO_DATA}. Pure functions.
 */
public final class GoesCodec {

    /** The u16 code of a missing sample. */
    public static final int NO_DATA = 65_535;

    /** The largest value code: 805.34 K. */
    public static final int MAX_CODE = 65_534;

    /** The physical value of code 0, K. */
    public static final double OFFSET_K = 150.0;

    /** Kelvin per code. */
    public static final double SCALE_K = 0.01;

    private GoesCodec() {}

    /** The u16 code of brightness temperature {@code kelvin}; below 150 K clamps to 0. */
    public static int brightness(float kelvin) {
        if (Float.isNaN(kelvin) || Float.isInfinite(kelvin)) {
            return NO_DATA;
        }
        long code = Math.round((kelvin - OFFSET_K) / SCALE_K);
        return (int) Math.max(0, Math.min(MAX_CODE, code));
    }

    /** {@link #brightness} over a band. */
    public static int[] brightness(float[] kelvin) {
        int[] out = new int[kelvin.length];
        for (int i = 0; i < kelvin.length; i++) {
            out[i] = brightness(kelvin[i]);
        }
        return out;
    }
}
