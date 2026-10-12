package ai.foresignal.raster;

import ai.foresignal.source.spi.GridDefinition;
import java.util.Map;

/**
 * A regular latitude/longitude grid (prompt 045).
 *
 * <p>Parameters ({@code raster_grid.definition.params}), all optional:
 * <ul>
 *   <li>{@code lo1}, {@code la1}: the center of the first cell (row 0, column 0: the north-west
 *       corner), degrees; longitudes in 0..360 or -180..180 are normalized once;
 *   <li>{@code dx}, {@code dy}: the cell size, degrees; {@code dy} is the step southwards (its sign is
 *       ignored, because rows always run north to south).
 * </ul>
 * Without them the grid fills the definition's box exactly: {@code dx = (east − west) / nx},
 * {@code dy = (north − south) / ny}, and the first center lies half a cell inside the north-west
 * corner (the edge convention prompt 042's footprints use). A grid whose columns span 360° wraps.
 *
 * @param nx columns
 * @param ny rows
 * @param lo1 center longitude of column 0, normalized to [-180, 180)
 * @param la1 center latitude of row 0
 * @param dx column step, degrees, positive eastwards
 * @param dy row step, degrees, positive southwards
 * @param wraps true when the columns cover the whole globe
 */
public record LatLonGrid(int nx, int ny, double lo1, double la1, double dx, double dy, boolean wraps)
        implements GridDef {

    /** Validates the steps. */
    public LatLonGrid {
        if (!(dx > 0) || !(dy > 0)) {
            throw new IllegalArgumentException("latlon dx and dy must be positive, were " + dx + ", " + dy);
        }
        lo1 = GridDef.normalizeLon(lo1);
    }

    /** The grid of {@code d}. */
    static LatLonGrid of(GridDefinition d) {
        Map<String, Object> p = d.params();
        double span = d.east() - d.west();
        if (span <= 0) {
            span += 360.0;
        }
        double dx = Math.abs(GridDef.num(p, "dx", span / d.nx()));
        double dy = Math.abs(GridDef.num(p, "dy", (d.north() - d.south()) / d.ny()));
        double lo1 = GridDef.num(p, "lo1", d.west() + dx / 2);
        double la1 = GridDef.num(p, "la1", d.north() - dy / 2);
        boolean wraps = Math.abs(d.nx() * dx - 360.0) < dx / 2;
        return new LatLonGrid(d.nx(), d.ny(), lo1, la1, dx, dy, wraps);
    }

    @Override
    public double[] lonLatToIndex(double lon, double lat) {
        if (Double.isNaN(lon) || Double.isNaN(lat)) {
            return GridDef.nowhere();
        }
        double j = (la1 - lat) / dy;
        double di = ((lon - lo1) % 360.0 + 360.0) % 360.0;
        if (di >= 360.0 - dx / 2) {
            di -= 360.0;
        }
        double i = di / dx;
        if (j < -0.5 || j > ny - 0.5) {
            return GridDef.nowhere();
        }
        if (!wraps && (i < -0.5 || i > nx - 0.5)) {
            return GridDef.nowhere();
        }
        return new double[] {i, j};
    }

    @Override
    public double[] indexToLonLat(double i, double j) {
        return new double[] {GridDef.normalizeLon(lo1 + i * dx), la1 - j * dy};
    }

    @Override
    public double[] bounds() {
        if (wraps) {
            return new double[] {-180.0, la1 - (ny - 0.5) * dy, 180.0, la1 + dy / 2};
        }
        double west = GridDef.normalizeLon(lo1 - dx / 2);
        double east = west + nx * dx;
        if (east > 180.0) {
            east = GridDef.normalizeLon(east);
        }
        return new double[] {west, la1 - (ny - 0.5) * dy, east, la1 + dy / 2};
    }

    @Override
    public boolean wrapsLongitude() {
        return wraps;
    }
}
