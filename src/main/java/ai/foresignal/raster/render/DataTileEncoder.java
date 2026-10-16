package ai.foresignal.raster.render;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data tiles (prompt 108; Weather-Imagery-Design §11): physical values as RGB, for MapLibre
 * {@code raster-dem} and client-side colouring. Two encodings:
 *
 * <ul>
 *   <li>{@code custom}: {@code v = (R·65536 + G·256 + B)·scale + offset}, i.e. the frame's own 16-bit
 *       code in R/G and B = 0, with {@code redFactor = 256·scale}, {@code greenFactor = scale},
 *       {@code blueFactor = 0} and {@code baseShift = offset} (minus {@code 32768·scale} for a signed
 *       i16 frame);</li>
 *   <li>{@code terrarium}: {@code v = (R·256 + G + B/256) − 32768}, only offered when the product's
 *       range fits.</li>
 * </ul>
 *
 * A no-data pixel is alpha 0. Pure; only {@code nearest} sampling is used.
 */
public final class DataTileEncoder {

    /** The {@code custom} encoding. */
    public static final String CUSTOM = "custom";

    /** The {@code terrarium} encoding. */
    public static final String TERRARIUM = "terrarium";

    private DataTileEncoder() {}

    /**
     * A data-tile encoding choice (prompt 108).
     *
     * @param encoding {@link #CUSTOM} or {@link #TERRARIUM}
     * @param signed the frame is i16 (codes shifted by 32768 into the unsigned field)
     */
    public record Encoding(String encoding, boolean signed) {

        /** {@code custom} for an unsigned frame. */
        public static Encoding unsignedCustom() {
            return new Encoding(CUSTOM, false);
        }

        /** {@code custom} for an i16 frame. */
        public static Encoding signedCustom() {
            return new Encoding(CUSTOM, true);
        }

        /** {@code terrarium}. */
        public static Encoding terrarium() {
            return new Encoding(TERRARIUM, false);
        }
    }

    /**
     * The encoded PNG and its manifest block.
     *
     * @param png the 32-bit PNG bytes
     * @param encoding the {@code {encoding, redFactor, greenFactor, blueFactor, baseShift}} block
     */
    public record Result(byte[] png, Map<String, Object> encoding) {}

    /**
     * Encodes an array of raw frame codes (row-major north to south, one per pixel, {@code Integer.MIN_VALUE}
     * for no data) as a data tile.
     *
     * @param codes the codes, or {@link #NO_DATA}
     * @param width pixels
     * @param height pixels
     * @param scale the product's code scale (physical = code × scale + offset)
     * @param offset the product's code offset
     * @param signed true for an i16 frame (codes are shifted by 32768 into the unsigned field)
     * @param encoding {@link #CUSTOM} or {@link #TERRARIUM}
     */
    public static Result encode(int[] codes, int width, int height, double scale, double offset, boolean signed,
            String encoding) {
        int[] argb = new int[width * height];
        boolean terrarium = TERRARIUM.equals(encoding);
        for (int p = 0; p < argb.length; p++) {
            int code = codes[p];
            if (code == NO_DATA) {
                continue;
            }
            if (terrarium) {
                double physical = code * scale + offset;
                double v = physical + 32768.0;
                if (v < 0 || v > 65535.996) {
                    continue;
                }
                argb[p] = 0xFF000000 | terrarium(v);
                continue;
            }
            int code16 = (signed ? code + 32768 : code) & 0xFFFF;
            int r = (code16 >> 8) & 0xFF;
            int g = code16 & 0xFF;
            argb[p] = 0xFF000000 | (r << 16) | (g << 8);
        }
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("encoding", terrarium ? TERRARIUM : CUSTOM);
        if (!terrarium) {
            meta.put("redFactor", 256.0 * scale);
            meta.put("greenFactor", scale);
            meta.put("blueFactor", 0.0);
            meta.put("baseShift", signed ? offset - 32768.0 * scale : offset);
        }
        return new Result(ImageEncoding.argbPng(width, height, argb), meta);
    }

    /**
     * The Terrarium RGB of {@code v} = physical + 32768: the value in 1/256 steps as one 24-bit integer,
     * so a fraction that rounds up to a whole step carries into green (and green into red) instead of
     * wrapping blue to 0.
     *
     * @param v the shifted value, 0 to 65535.996
     * @return {@code 0xRRGGBB}
     */
    static int terrarium(double v) {
        long steps = Math.round(v * 256.0);
        steps = Math.max(0, Math.min(0xFFFFFFL, steps));
        return (int) steps;
    }

    /** The no-data code in the input array. */
    public static final int NO_DATA = Integer.MIN_VALUE;
}
