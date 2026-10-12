package ai.foresignal.raster.render;

/**
 * WebMercatorQuad tile and pixel math (OGC Two Dimensional Tile Matrix Set, EPSG:3857 on the sphere
 * of radius 6378137 m): tile {@code (z, x, y)} has its origin at the north-west corner of the world,
 * {@code y} grows southwards, and each level halves the tile size.
 */
public final class WebMercator {

    /** The sphere radius, meters. */
    public static final double R = 6_378_137.0;

    /** Half the world's width, meters. */
    public static final double HALF = Math.PI * R;

    /** The largest latitude WebMercator represents, degrees. */
    public static final double MAX_LAT = 85.05112877980659;

    /** The standard tile size, pixels. */
    public static final int TILE = 256;

    private WebMercator() {}

    /** Meters east of the prime meridian at {@code lon} degrees. */
    public static double x(double lon) {
        return R * Math.toRadians(lon);
    }

    /** Meters north of the equator at {@code lat} degrees (clamped to {@link #MAX_LAT}). */
    public static double y(double lat) {
        double clamped = Math.max(-MAX_LAT, Math.min(MAX_LAT, lat));
        return R * Math.log(Math.tan(Math.PI / 4 + Math.toRadians(clamped) / 2));
    }

    /** The longitude at {@code x} meters. */
    public static double lon(double x) {
        return Math.toDegrees(x / R);
    }

    /** The latitude at {@code y} meters. */
    public static double lat(double y) {
        return Math.toDegrees(2 * Math.atan(Math.exp(y / R)) - Math.PI / 2);
    }

    /** Tiles per side at zoom {@code z}. */
    public static int tiles(int z) {
        return 1 << z;
    }

    /**
     * A tile's extent in meters.
     *
     * @return {@code {minX, minY, maxX, maxY}}
     */
    public static double[] tileMeters(int z, int x, int y) {
        double size = 2 * HALF / tiles(z);
        double minX = -HALF + x * size;
        double maxY = HALF - y * size;
        return new double[] {minX, maxY - size, minX + size, maxY};
    }

    /**
     * A tile's extent in WGS84 degrees.
     *
     * @return {@code {west, south, east, north}}
     */
    public static double[] tileBounds(int z, int x, int y) {
        double[] m = tileMeters(z, x, y);
        return new double[] {lon(m[0]), lat(m[1]), lon(m[2]), lat(m[3])};
    }

    /**
     * The tiles at zoom {@code z} that intersect a WGS84 box (not crossing the antimeridian).
     *
     * @return {@code {minX, minY, maxX, maxY}} tile indices, inclusive
     */
    public static int[] tileRange(int z, double west, double south, double east, double north) {
        int n = tiles(z);
        double size = 2 * HALF / n;
        int minX = clamp((int) Math.floor((x(west) + HALF) / size), n);
        int maxX = clamp((int) Math.floor((x(east) + HALF) / size - 1e-9), n);
        int minY = clamp((int) Math.floor((HALF - y(north)) / size), n);
        int maxY = clamp((int) Math.floor((HALF - y(south)) / size - 1e-9), n);
        return new int[] {minX, minY, Math.max(minX, maxX), Math.max(minY, maxY)};
    }

    private static int clamp(int v, int n) {
        return Math.max(0, Math.min(n - 1, v));
    }
}
