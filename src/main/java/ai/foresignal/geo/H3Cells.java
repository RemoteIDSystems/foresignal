package ai.foresignal.geo;

import com.uber.h3core.H3Core;
import com.uber.h3core.util.LatLng;
import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * H3 cell conversions (prompt 092), shared with the sources that map a cell index to geometry. Wraps Uber's
 * H3 binding; the boundary is a JTS polygon in SRID 4326, with longitudes normalised so a cell that crosses
 * the antimeridian does not become a degenerate polygon spanning the globe.
 */
public final class H3Cells {

    private static final H3Core H3 = create();
    private static final GeometryFactory FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private static H3Core create() {
        try {
            return H3Core.newInstance();
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Could not load the H3 native library", e);
        }
    }

    private H3Cells() {}

    /** The H3 cell of {@code (lat, lon)} at resolution {@code res}. */
    public static long latLngToCell(double lat, double lon, int res) {
        return H3.latLngToCell(lat, lon, res);
    }

    /** The polygon boundary of a cell index, in SRID 4326. */
    public static Polygon cellToPolygon(long cell) {
        if (!H3.isValidCell(cell)) {
            throw new IllegalArgumentException("Not a valid H3 cell: " + cell);
        }
        List<LatLng> boundary = H3.cellToBoundary(cell);
        List<Coordinate> coordinates = new ArrayList<>(boundary.size() + 1);
        boolean crossesAntimeridian = false;
        double minLon = Double.MAX_VALUE;
        double maxLon = -Double.MAX_VALUE;
        for (LatLng point : boundary) {
            minLon = Math.min(minLon, point.lng);
            maxLon = Math.max(maxLon, point.lng);
        }
        if (maxLon - minLon > 180) {
            crossesAntimeridian = true;
        }
        for (LatLng point : boundary) {
            double lon = crossesAntimeridian && point.lng < 0 ? point.lng + 360 : point.lng;
            coordinates.add(new Coordinate(lon, point.lat));
        }
        coordinates.add(new Coordinate(coordinates.get(0)));
        LinearRing ring = FACTORY.createLinearRing(coordinates.toArray(new Coordinate[0]));
        return FACTORY.createPolygon(ring);
    }

    /** The polygon boundary of a cell address ({@code "842830bffffffff"}). */
    public static Polygon cellToPolygon(String address) {
        return cellToPolygon(stringToCell(address));
    }

    /** The numeric cell of an H3 address. */
    public static long stringToCell(String address) {
        return H3.stringToH3(address);
    }

    /** True when {@code cell} is a valid H3 index. */
    public static boolean isValid(long cell) {
        return H3.isValidCell(cell);
    }

    /** True when {@code address} is a valid H3 address. */
    public static boolean isValid(String address) {
        if (address == null) {
            return false;
        }
        try {
            return H3.isValidCell(H3.stringToH3(address));
        } catch (RuntimeException e) {
            return false;
        }
    }
}
