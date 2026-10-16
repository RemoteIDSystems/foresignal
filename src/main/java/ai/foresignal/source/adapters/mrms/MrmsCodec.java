package ai.foresignal.source.adapters.mrms;

/**
 * The MRMS code spaces (prompt 046, Weather-Imagery-Design §2.3): physical MRMS values, including
 * MRMS's in-band sentinels, to the stored raw codes. Pure functions.
 *
 * <p>Sentinels, verified against the 2026-10-12 fixtures ({@code src/test/resources/fixtures/mrms}):
 * reflectivity −999 = no coverage and −99 = no echo; precipitation rate and flag −3 = no coverage;
 * MESH −3 = no coverage and −1 = no hail. A {@code NaN} (a bitmap-missing sample) is always no
 * coverage: <strong>no-coverage pixels never read as "no echo"</strong>.
 *
 * <ul>
 *   <li>Reflectivity, u8: 0 = no coverage, 1 = no echo or below −32 dBZ, 2..255 =
 *       {@code clamp(round(dBZ × 2) + 66, 2, 255)}; dBZ = code × 0.5 − 33.
 *   <li>Precipitation rate, u16 band 1: {@code round(mm/h × 10)} capped at 65,534; 65,535 = no
 *       coverage. Band 2 (PrecipFlag): the flag value (0 none, 1 warm stratiform, 3 snow, 6
 *       convective, 7 hail, 10 cool stratiform, 91/96 tropical); 65,535 = missing.
 *   <li>MESH, u8: 0 = no coverage, 1 = 0 mm (no hail), 2..255 = {@code clamp(round(mm) + 1, 2, 255)};
 *       mm = code − 1.
 * </ul>
 *
 * <p><b>Second wave</b> (prompt 053), verified against the 2026-10-13 fixtures and the MRMS GRIB2 table
 * NetCDF-Java ships ({@code UserTable_MRMS_v12.2.csv}):
 *
 * <ul>
 *   <li>Rotation track 60 min, u16: the file holds 0.001 s⁻¹ units on a 0.005° grid, and the table gives
 *       0 for both "missing" and "no coverage", so a zero cannot be told apart from "no rotation". Code 0 =
 *       no coverage (a bitmap-missing sample only), 1 = 0 (no rotation detected, named {@code noEcho}),
 *       2..65,535 = {@code clamp(round(value × 10) + 1, 2, 65,535)}; s⁻¹ = code × 10⁻⁴ − 10⁻⁴. The adapter
 *       reduces the grid 2 × 2 by maximum ({@link #coarsenMax}) to the 0.01° grid of the other products.
 *   <li>QPE 1 h and 24 h, u16: {@code round(mm × 10)} capped at 65,534; 65,535 = no coverage (MRMS −3) or
 *       missing (−1); both are unknown, never "no rain".
 *   <li>FLASH QPE-to-FFG ratio 1 h, u16 percent: {@code round(percent × 10)} (0.1 % precision; the file
 *       holds whole percents in steps of 5), capped at 65,534; 65,535 = no coverage (MRMS −999). The table
 *       says "non-dim", but the values are percentages: a light-rain day's p50 is 5 and its maximum 90.
 * </ul>
 */
public final class MrmsCodec {

    /** Reflectivity no coverage (MRMS −999). */
    public static final int REFLECTIVITY_NO_COVERAGE = 0;

    /** Reflectivity no echo or below −32 dBZ (MRMS −99). */
    public static final int REFLECTIVITY_NO_ECHO = 1;

    /** The u16 code of a missing precipitation sample (MRMS −3, or a missing flag). */
    public static final int U16_MISSING = 65_535;

    /** The largest precipitation-rate code: 6553.4 mm/h. */
    public static final int PRECIP_MAX_CODE = 65_534;

    /** MESH no coverage (MRMS −3). */
    public static final int MESH_NO_COVERAGE = 0;

    /** MESH 0 mm, no hail (MRMS −1). */
    public static final int MESH_ZERO = 1;

    /** MRMS reflectivity's no-coverage sentinel. */
    public static final float MRMS_NO_COVERAGE = -999f;

    /** MRMS reflectivity's no-echo sentinel. */
    public static final float MRMS_NO_ECHO = -99f;

    /** MRMS precipitation and MESH no-coverage sentinel. */
    public static final float MRMS_MISSING = -3f;

    /** MRMS MESH "no hail" value. */
    public static final float MRMS_MESH_NONE = -1f;

    /** The lowest reflectivity that keeps a value code, dBZ. */
    public static final float MIN_DBZ = -32f;

    /** Rotation no coverage (a bitmap-missing sample; MRMS has no in-band sentinel for it). */
    public static final int ROTATION_NO_COVERAGE = 0;

    /** Rotation 0 s⁻¹: MRMS 0, no rotation detected (or no radar there; the file cannot tell). */
    public static final int ROTATION_NONE = 1;

    /** The largest u16 code. */
    public static final int U16_MAX = 65_535;

    /** MRMS FLASH no-coverage sentinel. */
    public static final float MRMS_FLASH_NO_COVERAGE = -999f;

    private static final float SENTINEL_TOLERANCE = 0.25f;

    private MrmsCodec() {}

    /** The u8 reflectivity code of {@code dbz}. */
    public static int reflectivity(float dbz) {
        if (Float.isNaN(dbz) || near(dbz, MRMS_NO_COVERAGE) || dbz < MRMS_NO_COVERAGE) {
            return REFLECTIVITY_NO_COVERAGE;
        }
        if (near(dbz, MRMS_NO_ECHO) || dbz < MIN_DBZ) {
            return REFLECTIVITY_NO_ECHO;
        }
        return clamp(Math.round(dbz * 2f) + 66, 2, 255);
    }

    /** Band 1 of {@code mrms-precip-rate}: the u16 code of {@code mmPerHour}. */
    public static int precipRate(float mmPerHour) {
        if (Float.isNaN(mmPerHour) || mmPerHour < 0f) {
            // −3 is no coverage; MRMS publishes no other negative rate.
            return U16_MISSING;
        }
        return (int) Math.min(PRECIP_MAX_CODE, Math.round((double) mmPerHour * 10.0));
    }

    /** Band 2 of {@code mrms-precip-rate}: the u16 code of PrecipFlag value {@code flag}. */
    public static int precipFlag(float flag) {
        if (Float.isNaN(flag) || flag < 0f) {
            return U16_MISSING;
        }
        return (int) Math.min(PRECIP_MAX_CODE, Math.round((double) flag));
    }

    /** The u8 MESH code of {@code mm}. */
    public static int mesh(float mm) {
        if (Float.isNaN(mm) || near(mm, MRMS_MISSING) || mm < MRMS_MESH_NONE - SENTINEL_TOLERANCE) {
            return MESH_NO_COVERAGE;
        }
        if (mm < 0f) {
            return MESH_ZERO;
        }
        return clamp(Math.round(mm) + 1, 2, 255);
    }

    /** {@link #reflectivity} over a band. */
    public static int[] reflectivity(float[] dbz) {
        int[] out = new int[dbz.length];
        for (int i = 0; i < dbz.length; i++) {
            out[i] = reflectivity(dbz[i]);
        }
        return out;
    }

    /** {@link #precipRate} over a band. */
    public static int[] precipRate(float[] mmPerHour) {
        int[] out = new int[mmPerHour.length];
        for (int i = 0; i < mmPerHour.length; i++) {
            out[i] = precipRate(mmPerHour[i]);
        }
        return out;
    }

    /** {@link #precipFlag} over a band. */
    public static int[] precipFlag(float[] flags) {
        int[] out = new int[flags.length];
        for (int i = 0; i < flags.length; i++) {
            out[i] = precipFlag(flags[i]);
        }
        return out;
    }

    /** A flag band with every sample missing (the 4-minute fallback). */
    public static int[] missingFlags(int cells) {
        int[] out = new int[cells];
        java.util.Arrays.fill(out, U16_MISSING);
        return out;
    }

    /** {@link #mesh} over a band. */
    public static int[] mesh(float[] mm) {
        int[] out = new int[mm.length];
        for (int i = 0; i < mm.length; i++) {
            out[i] = mesh(mm[i]);
        }
        return out;
    }

    /**
     * The u16 rotation code of {@code value}, in the file's 0.001 s⁻¹ units.
     *
     * @param value the MRMS value (0 = none or no coverage, NaN = missing)
     */
    public static int rotation(float value) {
        if (Float.isNaN(value) || value < 0f) {
            return ROTATION_NO_COVERAGE;
        }
        long code = Math.round((double) value * 10.0) + 1;
        if (code <= ROTATION_NONE) {
            return ROTATION_NONE;
        }
        return (int) Math.max(2, Math.min(U16_MAX, code));
    }

    /** The u16 QPE code of {@code mm} (1 h or 24 h accumulation). */
    public static int qpe(float mm) {
        if (Float.isNaN(mm) || mm < 0f) {
            // −3 no coverage and −1 missing: both unknown, never zero rain.
            return U16_MISSING;
        }
        return (int) Math.min(PRECIP_MAX_CODE, Math.round((double) mm * 10.0));
    }

    /** The u16 code of a QPE-to-FFG ratio in percent: tenths of a percent. */
    public static int ffgPercent(float percent) {
        if (Float.isNaN(percent) || near(percent, MRMS_FLASH_NO_COVERAGE) || percent < 0f) {
            return U16_MISSING;
        }
        return (int) Math.min(PRECIP_MAX_CODE, Math.round((double) percent * 10.0));
    }

    /** {@link #rotation} over a band. */
    public static int[] rotation(float[] values) {
        int[] out = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            out[i] = rotation(values[i]);
        }
        return out;
    }

    /** {@link #qpe} over a band. */
    public static int[] qpe(float[] mm) {
        int[] out = new int[mm.length];
        for (int i = 0; i < mm.length; i++) {
            out[i] = qpe(mm[i]);
        }
        return out;
    }

    /** {@link #ffgPercent} over a band. */
    public static int[] ffgPercent(float[] percent) {
        int[] out = new int[percent.length];
        for (int i = 0; i < percent.length; i++) {
            out[i] = ffgPercent(percent[i]);
        }
        return out;
    }

    /**
     * Reduces a row-major code grid by {@code factor} in both directions, keeping each block's largest code.
     * Only for code spaces where a larger code means a larger value and the no-coverage code is the smallest
     * (rotation): a block is no coverage only when all its cells are.
     *
     * @param codes {@code nx × ny} codes, rows north to south
     * @param nx columns, a multiple of {@code factor}
     * @param ny rows, a multiple of {@code factor}
     * @param factor the reduction, 1 or more
     * @return {@code (nx / factor) × (ny / factor)} codes
     */
    public static int[] coarsenMax(int[] codes, int nx, int ny, int factor) {
        if (factor <= 1) {
            return codes;
        }
        if (nx % factor != 0 || ny % factor != 0 || codes.length != nx * ny) {
            throw new IllegalArgumentException("A " + nx + "x" + ny + " grid cannot be reduced by " + factor);
        }
        int ox = nx / factor;
        int oy = ny / factor;
        int[] out = new int[ox * oy];
        for (int y = 0; y < ny; y++) {
            int row = y * nx;
            int orow = (y / factor) * ox;
            for (int x = 0; x < nx; x++) {
                int c = codes[row + x];
                int o = orow + x / factor;
                if (c > out[o]) {
                    out[o] = c;
                }
            }
        }
        return out;
    }

    /**
     * Converts a decoded float band to rotation codes and reduces it in one pass (prompt 053), so the full
     * 0.005° code grid (98 M ints) is never allocated.
     */
    public static int[] rotationCoarsened(float[] values, int nx, int ny, int factor) {
        if (factor <= 1) {
            return rotation(values);
        }
        if (nx % factor != 0 || ny % factor != 0 || values.length != nx * ny) {
            throw new IllegalArgumentException("A " + nx + "x" + ny + " grid cannot be reduced by " + factor);
        }
        int ox = nx / factor;
        int[] out = new int[ox * (ny / factor)];
        for (int y = 0; y < ny; y++) {
            int row = y * nx;
            int orow = (y / factor) * ox;
            for (int x = 0; x < nx; x++) {
                int c = rotation(values[row + x]);
                int o = orow + x / factor;
                if (c > out[o]) {
                    out[o] = c;
                }
            }
        }
        return out;
    }

    /** The u8 code of a missing lightning-probability sample (prompt 106): 255. */
    public static final int LTNG_PROB_NO_DATA = 255;

    /** The u16 VIL code of {@code kgPerM2} (prompt 106): scale 0.01, 65535 = no coverage. */
    public static int vil(float kgPerM2) {
        if (Float.isNaN(kgPerM2) || kgPerM2 < 0f) {
            return U16_MISSING;
        }
        return (int) Math.min(PRECIP_MAX_CODE, Math.round((double) kgPerM2 * 100.0));
    }

    /** {@link #vil} over a band. */
    public static int[] vil(float[] kgPerM2) {
        int[] out = new int[kgPerM2.length];
        for (int i = 0; i < kgPerM2.length; i++) {
            out[i] = vil(kgPerM2[i]);
        }
        return out;
    }

    /** The u16 echo-top code of {@code km} (prompt 106): scale 0.1, 65535 = no coverage. */
    public static int echoTop(float km) {
        if (Float.isNaN(km) || km < 0f) {
            return U16_MISSING;
        }
        return (int) Math.min(PRECIP_MAX_CODE, Math.round((double) km * 10.0));
    }

    /** {@link #echoTop} over a band. */
    public static int[] echoTop(float[] km) {
        int[] out = new int[km.length];
        for (int i = 0; i < km.length; i++) {
            out[i] = echoTop(km[i]);
        }
        return out;
    }

    /** The u8 lightning-probability code of {@code percent} (prompt 106): 0..100, 255 = no data. */
    public static int lightningProbability(float percent) {
        if (Float.isNaN(percent) || near(percent, MRMS_NO_COVERAGE) || near(percent, MRMS_MISSING) || percent < 0f) {
            return LTNG_PROB_NO_DATA;
        }
        return clamp(Math.round(percent), 0, 254);
    }

    /** {@link #lightningProbability} over a band. */
    public static int[] lightningProbability(float[] percent) {
        int[] out = new int[percent.length];
        for (int i = 0; i < percent.length; i++) {
            out[i] = lightningProbability(percent[i]);
        }
        return out;
    }

    private static boolean near(float value, float sentinel) {
        return Math.abs(value - sentinel) <= SENTINEL_TOLERANCE;
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
