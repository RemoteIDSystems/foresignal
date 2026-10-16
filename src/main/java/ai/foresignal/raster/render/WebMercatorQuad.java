package ai.foresignal.raster.render;

/** The {@code WebMercatorQuad} tile matrix set (EPSG:3857) — the v1 default (prompt 108). */
public final class WebMercatorQuad implements TileMatrixSet {

    /** The set's identifier. */
    public static final String ID = "WebMercatorQuad";

    /** The CRS URI. */
    public static final String CRS = "http://www.opengis.net/def/crs/EPSG/0/3857";

    /** The singleton. */
    public static final WebMercatorQuad INSTANCE = new WebMercatorQuad();

    private WebMercatorQuad() {}

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
        return WebMercator.tiles(z);
    }

    @Override
    public int tilesY(int z) {
        return WebMercator.tiles(z);
    }

    @Override
    public double[] tileBounds(int z, int x, int y) {
        return WebMercator.tileBounds(z, x, y);
    }

    @Override
    public int[] tileRange(int z, double west, double south, double east, double north) {
        return WebMercator.tileRange(z, west, south, east, north);
    }

    @Override
    public IndexMap.PixelLocator locator(int z, int x, int y, int size) {
        return IndexMap.tile(z, x, y, size);
    }
}
