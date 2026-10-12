package ai.foresignal.testsupport.tiles;

import ai.foresignal.store.tiles.TileCoordinates;

/** Web Mercator tile arithmetic for tests: which tile holds a point, and where in it. */
public final class TileMath {

    private TileMath() {}

    /** The tile at zoom {@code z} that holds {@code (lon, lat)}. */
    public static TileCoordinates tileFor(double lon, double lat, int z) {
        double n = 1L << z;
        int x = (int) Math.floor(fractionX(lon) * n);
        int y = (int) Math.floor(fractionY(lat) * n);
        return new TileCoordinates(z, x, y);
    }

    /** {@code (lon, lat)} in the tile coordinates (extent 4096, y down) of {@code tile}. */
    public static int[] pixel(double lon, double lat, TileCoordinates tile) {
        double n = 1L << tile.z();
        double px = (fractionX(lon) * n - tile.x()) * 4096;
        double py = (fractionY(lat) * n - tile.y()) * 4096;
        return new int[] {(int) Math.round(px), (int) Math.round(py)};
    }

    private static double fractionX(double lon) {
        return (lon + 180.0) / 360.0;
    }

    private static double fractionY(double lat) {
        double rad = Math.toRadians(lat);
        return (1 - Math.log(Math.tan(rad) + 1 / Math.cos(rad)) / Math.PI) / 2;
    }
}
