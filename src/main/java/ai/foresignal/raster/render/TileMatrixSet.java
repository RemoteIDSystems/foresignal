package ai.foresignal.raster.render;

/**
 * An OGC Two Dimensional Tile Matrix Set (prompt 108): how a tile {@code (z, x, y)} maps to a WGS84
 * box and to output pixels. {@link WebMercatorQuad} is EPSG:3857 (the v1 default); {@link WorldCrs84Quad}
 * is EPSG:4326 with 2×1 tiles at z0. The base tile size is 256 px; a 512 px pyramid renders natively at
 * 512.
 */
public interface TileMatrixSet {

    /** The OGC identifier ({@code WebMercatorQuad} or {@code WorldCrs84Quad}). */
    String id();

    /** The CRS URI. */
    String crs();

    /** Tiles per row at {@code z}. */
    int tilesX(int z);

    /** Tiles per column at {@code z}. */
    int tilesY(int z);

    /**
     * A tile's extent in WGS84 degrees.
     *
     * @return {@code {west, south, east, north}}
     */
    double[] tileBounds(int z, int x, int y);

    /**
     * The tiles at {@code z} that intersect a WGS84 box.
     *
     * @return {@code {minX, minY, maxX, maxY}}, inclusive
     */
    int[] tileRange(int z, double west, double south, double east, double north);

    /** The pixel locator of a {@code size × size} tile at {@code (z, x, y)}. */
    IndexMap.PixelLocator locator(int z, int x, int y, int size);
}
