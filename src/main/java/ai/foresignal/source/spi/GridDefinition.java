package ai.foresignal.source.spi;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The native grid of a raster frame (Weather-Imagery-Design §2.3, {@code raster_grid}).
 *
 * <p>Stored data only: prompt 045 adds the projection maths. Two frames on the same grid share one
 * {@code raster_grid} row, whose id is the SHA-256 of the canonical definition JSON, so the
 * definition must be stable: the same upstream grid always yields the same values here.
 *
 * <p>Rows run north to south and columns west to east, as in the {@code .fsg} payload.
 *
 * @param type {@value #LATLON}, {@value #GEOS}, {@value #LCC} or {@value #AFFINE}
 * @param nx columns
 * @param ny rows
 * @param west western edge of the grid's bounding box, WGS84 degrees
 * @param south southern edge
 * @param east eastern edge
 * @param north northern edge
 * @param params projection parameters (for example the {@code geos} satellite longitude and sweep
 *     axis), sorted by key; numbers, strings and booleans only
 */
public record GridDefinition(String type, int nx, int ny, double west, double south, double east, double north,
        Map<String, Object> params) {

    /** A regular latitude/longitude grid. */
    public static final String LATLON = "latlon";

    /** A geostationary fixed grid. */
    public static final String GEOS = "geos";

    /** A Lambert conformal conic grid. */
    public static final String LCC = "lcc";

    /** A grid given by an affine transform. */
    public static final String AFFINE = "affine";

    /** Every grid type. */
    public static final Set<String> TYPES = Set.of(LATLON, GEOS, LCC, AFFINE);

    /** Validates the grid and sorts the parameters. */
    public GridDefinition {
        if (type == null || !TYPES.contains(type)) {
            throw new IllegalArgumentException("Grid type must be one of " + TYPES + ", was " + type);
        }
        if (nx <= 0 || ny <= 0) {
            throw new IllegalArgumentException("Grid dimensions must be positive, were " + nx + "x" + ny);
        }
        if (!(south < north) || south < -90 || north > 90 || west < -180 || east > 180 || west == east) {
            throw new IllegalArgumentException("Grid bounds are not a valid WGS84 box: " + west + "," + south + ","
                    + east + "," + north);
        }
        params = params == null ? Map.of() : Collections.unmodifiableMap(new TreeMap<>(params));
    }

    /** A regular lat/lon grid covering the box. */
    public static GridDefinition latLon(int nx, int ny, double west, double south, double east, double north) {
        return new GridDefinition(LATLON, nx, ny, west, south, east, north, Map.of());
    }

    /** Samples per band. */
    public long cells() {
        return (long) nx * ny;
    }
}
