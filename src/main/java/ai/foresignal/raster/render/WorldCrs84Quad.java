package ai.foresignal.raster.render;

/**
 * The {@code WorldCRS84Quad} tile matrix set (EPSG:4326, prompt 108): 2×1 tiles at z0, each level
 * doubling, 256 px base tiles. Longitude is linear; latitude is linear in degrees (equirectangular).
 */
public final class WorldCrs84Quad implements TileMatrixSet {

    /** The set's identifier. */
    public static final String ID = "WorldCrs84Quad";

    /** The CRS URI. */
    public static final String CRS = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";

    /** The singleton. */
    public static final WorldCrs84Quad INSTANCE = new WorldCrs84Quad();

    private WorldCrs84Quad() {}

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String crs() {
        return CRS;
    }

    @Override
    public int tilesX(int z) {
        return 2 << z;
    }

    @Override
    public int tilesY(int z) {
        return 1 << z;
    }

    /** The tile width in degrees at {@code z} (180 / 2^z). */
    public static double tileWidth(int z) {
        return 180.0 / (1 << z);
    }

    @Override
    public double[] tileBounds(int z, int x, int y) {
        double step = tileWidth(z);
        double west = -180.0 + x * step;
        double north = 90.0 - y * step;
        return new double[] {west, north - step, west + step, north};
    }

    @Override
    public int[] tileRange(int z, double west, double south, double east, double north) {
        double step = tileWidth(z);
        int cols = tilesX(z);
        int rows = tilesY(z);
        double w = west;
        double e = east < west ? east + 360 : east;
        int minX = clamp((int) Math.floor((w + 180.0) / step), cols);
        int maxX = clamp((int) Math.floor((e + 180.0) / step - 1e-9), cols);
        int minY = clamp((int) Math.floor((90.0 - north) / step), rows);
        int maxY = clamp((int) Math.floor((90.0 - south) / step - 1e-9), rows);
        return new int[] {minX, minY, Math.max(minX, maxX), Math.max(minY, maxY)};
    }

    @Override
    public IndexMap.PixelLocator locator(int z, int x, int y, int size) {
        double[] b = tileBounds(z, x, y);
        double stepX = (b[2] - b[0]) / size;
        double stepY = (b[3] - b[1]) / size;
        return (px, py, out) -> {
            out[0] = b[0] + (px + 0.5) * stepX;
            out[1] = b[3] - (py + 0.5) * stepY;
        };
    }

    private static int clamp(int v, int n) {
        return Math.max(0, Math.min(n - 1, v));
    }
}
