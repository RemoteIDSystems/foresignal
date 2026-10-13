package ai.foresignal.source.adapters.wx;

/**
 * The quantization of derived weather values (prompt 052; Weather-Imagery-Design §7.2), so a steady
 * situation produces an identical payload and therefore no new version.
 */
public final class WxRound {

    /** "None within 100 km" and "no approaching core". */
    public static final int NONE = 999;

    private WxRound() {}

    /**
     * A distance: {@value #NONE} when NaN or beyond 100 km; to 0.5 km below 10 km, else to 1 km.
     */
    public static double km(double d) {
        if (Double.isNaN(d) || d > 100) {
            return NONE;
        }
        if (d < 10) {
            return Math.round(d * 2) / 2.0;
        }
        return Math.round(d);
    }

    /** Reflectivity to 0.5 dBZ. */
    public static double dbz(double v) {
        return Math.round(v * 2) / 2.0;
    }

    /** A value to a whole unit (mm/h, mm, µg/m³, m/s). */
    public static long whole(double v) {
        return Math.round(v);
    }

    /** An ETA in minutes: rounded up to 5 min; {@value #NONE} when none. */
    public static int etaMin(double minutes) {
        if (Double.isNaN(minutes) || minutes < 0) {
            return NONE;
        }
        return (int) (Math.ceil(minutes / 5.0 - 1e-9) * 5);
    }

    /** A value to two significant figures. */
    public static double sig2(double v) {
        if (v == 0 || Double.isNaN(v) || Double.isInfinite(v)) {
            return 0;
        }
        return new java.math.BigDecimal(v).round(new java.math.MathContext(2, java.math.RoundingMode.HALF_UP))
                .doubleValue();
    }

    /** A speed to 5 km/h. */
    public static long speedKmh(double v) {
        return Math.round(v / 5.0) * 5;
    }

    /** A bearing to 10°, in 0..350. */
    public static int bearing10(double deg) {
        long b = Math.round((((deg % 360) + 360) % 360) / 10.0) * 10;
        return (int) (b % 360);
    }
}
