package ai.foresignal.geo;

import com.uber.h3core.H3Core;
import com.uber.h3core.util.LatLng;
import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.AffineTransformation;

/**
 * H3 cell conversions (prompt 092), shared with the sources that map a cell index to geometry. Wraps Uber's
 * H3 binding; the boundary is JTS geometry in SRID 4326 with every longitude in [-180, 180]: a cell that crosses
 * the antimeridian is split along it into a two-part multipolygon rather than shifted past 180.
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

    /**
     * The boundary of a cell index, in SRID 4326, with every longitude inside [-180, 180].
     *
     * <p>Most cells come back as a {@link Polygon}. A cell that crosses the antimeridian is split along it into a
     * {@link MultiPolygon} of its two halves, so no coordinate leaves the valid range (the normalizer rejects
     * {@code x > 180}) and the shape does not become a band spanning the globe.
     *
     * @throws IllegalArgumentException when {@code cell} is not a valid H3 index
     */
    public static Geometry cellToPolygon(long cell) {
        if (!H3.isValidCell(cell)) {
            throw new IllegalArgumentException("Not a valid H3 cell: " + cell);
        }
        List<LatLng> boundary = H3.cellToBoundary(cell);
        double minLon = Double.MAX_VALUE;
        double maxLon = -Double.MAX_VALUE;
        for (LatLng point : boundary) {
            minLon = Math.min(minLon, point.lng);
            maxLon = Math.max(maxLon, point.lng);
        }
        boolean crossesAntimeridian = maxLon - minLon > 180;
        List<Coordinate> coordinates = new ArrayList<>(boundary.size() + 1);
        for (LatLng point : boundary) {
            double lon = crossesAntimeridian && point.lng < 0 ? point.lng + 360 : point.lng;
            coordinates.add(new Coordinate(lon, point.lat));
        }
        coordinates.add(new Coordinate(coordinates.get(0)));
        Polygon shifted = FACTORY.createPolygon(coordinates.toArray(new Coordinate[0]));
        if (!crossesAntimeridian) {
            return shifted;
        }
        return splitAtAntimeridian(shifted);
    }

    /**
     * Splits a polygon whose longitudes run past 180 (the negative ones shifted by +360) into the part west of the
     * antimeridian and the part east of it, shifted back by -360.
     */
    static Geometry splitAtAntimeridian(Polygon shifted) {
        Envelope env = shifted.getEnvelopeInternal();
        Geometry west = shifted.intersection(FACTORY.toGeometry(
                new Envelope(env.getMinX() - 1, 180, env.getMinY() - 1, env.getMaxY() + 1)));
        Geometry east = shifted.intersection(FACTORY.toGeometry(
                new Envelope(180, env.getMaxX() + 1, env.getMinY() - 1, env.getMaxY() + 1)));
        AffineTransformation back = AffineTransformation.translationInstance(-360, 0);
        List<Polygon> parts = new ArrayList<>();
        collectPolygons(west, parts);
        List<Polygon> eastParts = new ArrayList<>();
        collectPolygons(east, eastParts);
        for (Polygon part : eastParts) {
            parts.add((Polygon) back.transform(part));
        }
        if (parts.size() == 1) {
            Polygon only = parts.get(0);
            only.setSRID(4326);
            return only;
        }
        return FACTORY.createMultiPolygon(parts.toArray(new Polygon[0]));
    }

    private static void collectPolygons(Geometry geometry, List<Polygon> out) {
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            Geometry part = geometry.getGeometryN(i);
            if (part instanceof Polygon polygon && !polygon.isEmpty() && polygon.getArea() > 0) {
                out.add(FACTORY.createPolygon(polygon.getExteriorRing().getCoordinates()));
            }
        }
    }

    /** The boundary of a cell address ({@code "842830bffffffff"}); see {@link #cellToPolygon(long)}. */
    public static Geometry cellToPolygon(String address) {
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

    /** The cell address as the textual H3 form (prompt 099: external ids and payloads). */
    public static String cellToAddress(long cell) {
        return H3.h3ToString(cell);
    }

    /**
     * The cells within {@code k} grid steps of {@code cell} (prompt 099: the density ring).
     *
     * @param cell the centre cell
     * @param k the number of rings, at least 0
     */
    public static List<Long> gridDisk(long cell, int k) {
        return H3.gridDisk(cell, Math.max(0, k));
    }

    /** The average edge length of an H3 cell at {@code res}, km (prompt 099: ring to radius). */
    public static double averageEdgeLengthKm(int res) {
        return H3.getHexagonEdgeLengthAvg(res, com.uber.h3core.LengthUnit.km);
    }
}
