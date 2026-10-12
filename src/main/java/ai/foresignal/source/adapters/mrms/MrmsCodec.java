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

    private static boolean near(float value, float sentinel) {
        return Math.abs(value - sentinel) <= SENTINEL_TOLERANCE;
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
