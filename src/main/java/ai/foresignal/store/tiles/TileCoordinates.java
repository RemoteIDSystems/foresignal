package ai.foresignal.store.tiles;

import ai.foresignal.api.support.QueryParams;

/**
 * One XYZ tile address in the Web Mercator (EPSG:3857) tile matrix, plus the arithmetic the
 * aggregated tiles need.
 *
 * @param z zoom, 0 to {@value #MAX_ZOOM}
 * @param x column, 0 to {@code 2^z − 1}, west to east
 * @param y row, 0 to {@code 2^z − 1}, north to south
 */
public record TileCoordinates(int z, int x, int y) {

    /** Highest zoom served. */
    public static final int MAX_ZOOM = 16;

    /** Half the width of the Web Mercator square, in meters. */
    public static final double HALF_WORLD_METERS = 20037508.342789244;

    /** Validates the address. */
    public TileCoordinates {
        if (z < 0 || z > MAX_ZOOM) {
            throw new IllegalArgumentException("z must be between 0 and " + MAX_ZOOM + ", got " + z);
        }
        int max = 1 << z;
        if (x < 0 || x >= max || y < 0 || y >= max) {
            throw new IllegalArgumentException("x and y must be between 0 and " + (max - 1) + " at zoom " + z);
        }
    }

    /**
     * Parses path variables; each malformed or out-of-range value is a {@code 400 VALIDATION_FAILED}
     * naming the field.
     */
    public static TileCoordinates parse(String z, String x, String y) {
        int zoom = QueryParams.intInRange("z", z, 0, MAX_ZOOM, 0);
        int max = (1 << zoom) - 1;
        int column = QueryParams.intInRange("x", x, 0, max, 0);
        int row = QueryParams.intInRange("y", y, 0, max, 0);
        return new TileCoordinates(zoom, column, row);
    }

    /** Tiles per side at this zoom, {@code 2^z}. */
    public long tilesPerSide() {
        return 1L << z;
    }

    /** The width of this tile in EPSG:3857 meters. */
    public double widthMeters() {
        return 2 * HALF_WORLD_METERS / tilesPerSide();
    }

    /** The edge length of one aggregate cell when a tile is {@code cellsPerTile} cells wide. */
    public double cellSize(int cellsPerTile) {
        return widthMeters() / cellsPerTile;
    }

    /**
     * The index of this tile's westernmost square cell in the {@code ST_SquareGrid} tiling (origin
     * (0, 0), cell {@code i} covers {@code [i·size, (i+1)·size)}). Exact because
     * {@code cellsPerTile} is even, so the origin is always a cell edge.
     */
    public long firstColumn(int cellsPerTile) {
        return (long) x * cellsPerTile - tilesPerSide() * cellsPerTile / 2;
    }

    /** The index of this tile's southernmost square cell row, in the same tiling. */
    public long firstRow(int cellsPerTile) {
        return tilesPerSide() * cellsPerTile / 2 - (long) (y + 1) * cellsPerTile;
    }

    /** {@code z/x/y}, for logs and entity tags. */
    @Override
    public String toString() {
        return z + "/" + x + "/" + y;
    }
}
