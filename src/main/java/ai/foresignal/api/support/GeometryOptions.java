package ai.foresignal.api.support;

/**
 * How returned geometries are written: coordinate {@code precision} (0–7 decimals; storage is
 * unchanged) and optional {@code simplify} tolerance in meters (0–100,000).
 *
 * <p>Simplification runs {@code ST_SimplifyPreserveTopology} in Web Mercator with the tolerance
 * scaled by {@code 1 / cos(latitude of the centroid)}, so a tolerance in meters means roughly the
 * same on the ground at every latitude. Topology is preserved <em>per feature only</em>: adjacent
 * polygons may gain gaps or overlaps. Points and multipoints pass through untouched.
 *
 * @param precision decimals kept in coordinates
 * @param simplifyMeters the tolerance, or null for none
 */
public record GeometryOptions(int precision, Double simplifyMeters) {

    /** The default: full precision, no simplification. */
    public static final GeometryOptions DEFAULT = new GeometryOptions(7, null);

    /** The largest precision. */
    public static final int MAX_PRECISION = 7;

    /** The largest tolerance. */
    public static final double MAX_SIMPLIFY_METERS = 100_000;

    /** Validates the ranges. */
    public GeometryOptions {
        if (precision < 0 || precision > MAX_PRECISION) {
            throw new IllegalArgumentException("precision must be 0-7, got " + precision);
        }
        if (simplifyMeters != null && (simplifyMeters < 0 || simplifyMeters > MAX_SIMPLIFY_METERS)) {
            throw new IllegalArgumentException("simplify must be 0-100000, got " + simplifyMeters);
        }
    }

    /** Parses {@code precision} and {@code simplify}; {@code simplify} may be null for endpoints without it. */
    public static GeometryOptions parse(String precision, String simplify) {
        int p = QueryParams.intInRange("precision", precision, 0, MAX_PRECISION, MAX_PRECISION);
        Double s = QueryParams.doubleInRange("simplify", simplify, 0, MAX_SIMPLIFY_METERS);
        return new GeometryOptions(p, s == null || s == 0.0 ? null : s);
    }

    /** True when a tolerance is set. */
    public boolean simplifies() {
        return simplifyMeters != null && simplifyMeters > 0;
    }

    /**
     * The geometry expression after simplification (without output encoding), using the named
     * parameter {@code toleranceParam} for the tolerance.
     *
     * @param column the geometry column, for example {@code l.geom}
     * @param toleranceParam the named parameter (with its colon) holding {@link #simplifyMeters}
     */
    public String simplified(String column, String toleranceParam) {
        if (!simplifies()) {
            return column;
        }
        return "(CASE WHEN " + column + " IS NULL OR GeometryType(" + column + ") IN ('POINT', 'MULTIPOINT') THEN "
                + column + " ELSE ST_Transform(ST_SimplifyPreserveTopology(ST_Transform(" + column
                + ", 3857), " + toleranceParam + " / GREATEST(cos(radians(ST_Y(ST_Centroid(" + column
                + ")))), 0.01)), 4326) END)";
    }

    /** A stable text form, for entity tags. */
    public String canonical() {
        return precision + "/" + (simplifyMeters == null ? "" : simplifyMeters);
    }
}
