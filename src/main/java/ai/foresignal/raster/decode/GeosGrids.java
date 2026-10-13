package ai.foresignal.raster.decode;

import ai.foresignal.raster.GeosGrid;
import ai.foresignal.source.spi.GridDefinition;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * Geostationary grid definitions for adapters that do not read NetCDF (prompt 049: SatDump xRIT
 * frames), as stateless helpers like the decoders in this package (Architecture §3).
 *
 * <p>The model is the CGMS LRIT/HRIT normalized geostationary projection (CGMS 03 §4.4.3.2) and the
 * GOES-R PUG Vol. 3 §5.1.2.8 fixed grid, implemented by {@link GeosGrid}: written from those public
 * specifications, with no SatDump or other GPL code (Weather-Imagery-Design §3).
 *
 * <p>Scan-plane coordinates follow the convention of the PROJ {@code geos} projection, which SatDump's
 * {@code projection_cfg} also uses: a projected coordinate in meters is the scan angle in radians times
 * the satellite height above the ellipsoid. Column {@code i} (from 0) is at
 * {@code offset_x + i · scalar_x} and row {@code j} at {@code offset_y + j · scalar_y}.
 */
public final class GeosGrids {

    private static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);

    /** Azimuths sampled for the visible-disc outline. */
    public static final int DISC_VERTICES = 180;

    private GeosGrids() {}

    /**
     * The grid definition of an image in the scan plane.
     *
     * @param nx columns
     * @param ny rows (north to south)
     * @param lon0 sub-satellite longitude, degrees
     * @param height satellite height above the ellipsoid, meters
     * @param sweepX true for the GOES ({@code x}) sweep axis, false for the Meteosat ({@code y}) one
     * @param scalarX meters of scan plane per column
     * @param scalarY meters of scan plane per row (negative when rows run north to south)
     * @param offsetX scan-plane x of column 0, meters
     * @param offsetY scan-plane y of row 0, meters
     * @return a {@code geos} definition whose bounding box is the outline of the visible part
     * @throws IllegalArgumentException when a parameter is out of range or no edge is on the Earth
     */
    public static GridDefinition fromScanPlane(int nx, int ny, double lon0, double height, boolean sweepX,
            double scalarX, double scalarY, double offsetX, double offsetY) {
        if (nx <= 0 || ny <= 0 || !(height > 0) || scalarX == 0 || scalarY == 0 || !Double.isFinite(lon0)
                || !Double.isFinite(offsetX) || !Double.isFinite(offsetY)) {
            throw new IllegalArgumentException("geos scan plane needs positive dimensions and height and non-zero scalars");
        }
        double x0 = round(offsetX / height);
        double dx = round(scalarX / height);
        double y0 = round(offsetY / height);
        double dy = round(scalarY / height);
        double lon = round(normalizeLon(lon0));
        GeosGrid grid = new GeosGrid(nx, ny, lon, height, GeosGrid.GRS80_A, GeosGrid.GRS80_B, sweepX, x0, dx, y0, dy,
                new double[] {-180, -90, 180, 90});
        double[] box = box(grid, nx, ny, lon);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("lon0", lon);
        params.put("h", height);
        params.put("a", GeosGrid.GRS80_A);
        params.put("b", GeosGrid.GRS80_B);
        params.put("sweep", sweepX ? "x" : "y");
        params.put("x0", x0);
        params.put("dx", dx);
        params.put("y0", y0);
        params.put("dy", dy);
        return new GridDefinition(GridDefinition.GEOS, nx, ny, box[0], box[1], box[2], box[3], params);
    }

    /**
     * The WGS84 box of the visible part of a grid. A lattice of up to 129 × 129 points over the cell
     * edges is projected (not only the four sides, which for a full disk lie beyond the limb), and the
     * longitudes are unwrapped around {@code lon0}, so a disc that crosses ±180° gives
     * {@code west > east}.
     *
     * @throws IllegalArgumentException when no point of the grid is on the Earth
     */
    static double[] box(GeosGrid grid, int nx, int ny, double lon0) {
        int stepsX = Math.min(128, nx);
        int stepsY = Math.min(128, ny);
        double minU = Double.POSITIVE_INFINITY;
        double maxU = Double.NEGATIVE_INFINITY;
        double south = Double.POSITIVE_INFINITY;
        double north = Double.NEGATIVE_INFINITY;
        for (int a = 0; a <= stepsX; a++) {
            double i = -0.5 + (double) a * nx / stepsX;
            for (int b = 0; b <= stepsY; b++) {
                double j = -0.5 + (double) b * ny / stepsY;
                double[] p = grid.indexToLonLat(i, j);
                if (Double.isNaN(p[0]) || Double.isNaN(p[1])) {
                    continue;
                }
                double u = lon0 + wrap180(p[0] - lon0);
                minU = Math.min(minU, u);
                maxU = Math.max(maxU, u);
                south = Math.min(south, p[1]);
                north = Math.max(north, p[1]);
            }
        }
        if (minU > maxU) {
            throw new IllegalArgumentException("No point of the geostationary grid lies on the Earth");
        }
        double west;
        double east;
        if (maxU - minU >= 360) {
            west = -180;
            east = 180;
        } else {
            west = normalizeLon(minU);
            east = normalizeLon(maxU);
            if (west == 180 && east < 180) {
                west = -180;
            }
        }
        return new double[] {round6(west), round6(Math.max(-90, south)), round6(east), round6(Math.min(90, north))};
    }

    /**
     * True when a point is on the Earth's disc seen from a satellite at {@code lon0} and
     * {@code height} (on the GRS80 ellipsoid).
     */
    public static boolean visible(double lon0, double height, double lon, double lat) {
        GeosGrid grid = probe(lon0, height);
        return !Double.isNaN(grid.scanAngles(lon, lat)[0]);
    }

    /**
     * The visible disc seen from {@code lon0} (Weather-Imagery-Design §4.5 item 1: the footprint of a
     * geostationary frame), split at the antimeridian.
     *
     * <p>Along each of {@value #DISC_VERTICES} azimuths from the sub-satellite point, the farthest
     * visible great-circle distance is found by bisection against the ellipsoid visibility test, so the
     * outline follows the ellipsoid rather than a sphere.
     *
     * @return the disc as a MultiPolygon in SRID 4326, one part, or two when it crosses ±180°
     */
    public static MultiPolygon visibleDisc(double lon0, double height) {
        GeosGrid grid = probe(lon0, height);
        double center = normalizeLon(lon0);
        Coordinate[] ring = new Coordinate[DISC_VERTICES + 1];
        for (int k = 0; k < DISC_VERTICES; k++) {
            double azimuth = 2 * Math.PI * k / DISC_VERTICES;
            double lo = 0;
            double hi = Math.PI / 2;
            for (int step = 0; step < 40; step++) {
                double mid = (lo + hi) / 2;
                double[] p = destination(center, azimuth, mid);
                if (!Double.isNaN(grid.scanAngles(normalizeLon(p[0]), p[1])[0])) {
                    lo = mid;
                } else {
                    hi = mid;
                }
            }
            double[] edge = destination(center, azimuth, lo);
            // Unwrapped longitude relative to the center, so the ring is continuous.
            double lon = center + wrap180(edge[0] - center);
            ring[k] = new Coordinate(round6(lon), round6(edge[1]));
        }
        ring[DISC_VERTICES] = new Coordinate(ring[0]);
        Polygon unwrapped = GEOMETRY.createPolygon(ring);
        List<Polygon> parts = new ArrayList<>();
        for (int shift = -1; shift <= 1; shift++) {
            double offset = shift * 360.0;
            Polygon window = box(-180 + offset, -90, 180 + offset, 90);
            Geometry piece = unwrapped.intersection(window);
            for (int i = 0; i < piece.getNumGeometries(); i++) {
                if (piece.getGeometryN(i) instanceof Polygon polygon && !polygon.isEmpty()) {
                    Polygon moved = (Polygon) polygon.copy();
                    double dx = -offset;
                    moved.apply((org.locationtech.jts.geom.CoordinateFilter) c -> c.x = c.x + dx);
                    moved.geometryChanged();
                    parts.add(moved);
                }
            }
        }
        return GEOMETRY.createMultiPolygon(parts.toArray(Polygon[]::new));
    }

    private static GeosGrid probe(double lon0, double height) {
        return new GeosGrid(1, 1, normalizeLon(lon0), height, GeosGrid.GRS80_A, GeosGrid.GRS80_B, true, 0, 1e-5, 0,
                -1e-5, new double[] {-180, -90, 180, 90});
    }

    /** The point {@code distance} radians from {@code (lon, 0)} along {@code azimuth} (spherical). */
    private static double[] destination(double lon, double azimuth, double distance) {
        double lat1 = 0;
        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(distance)
                + Math.cos(lat1) * Math.sin(distance) * Math.cos(azimuth));
        double lon2 = Math.toRadians(lon) + Math.atan2(Math.sin(azimuth) * Math.sin(distance) * Math.cos(lat1),
                Math.cos(distance) - Math.sin(lat1) * Math.sin(lat2));
        return new double[] {Math.toDegrees(lon2), Math.toDegrees(lat2)};
    }

    private static Polygon box(double west, double south, double east, double north) {
        return GEOMETRY.createPolygon(new Coordinate[] {new Coordinate(west, south), new Coordinate(east, south),
                new Coordinate(east, north), new Coordinate(west, north), new Coordinate(west, south)});
    }

    private static double wrap180(double degrees) {
        double d = ((degrees + 180) % 360 + 360) % 360 - 180;
        return d;
    }

    private static double normalizeLon(double lon) {
        double d = wrap180(lon);
        return d == -180 ? 180 : d;
    }

    private static double round(double v) {
        return Math.round(v * 1e12) / 1e12;
    }

    private static double round6(double v) {
        return Math.round(v * 1e6) / 1e6;
    }
}
