package ai.foresignal.source.adapters.stac;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.raster.geotiff.Antimeridian;
import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;
import tools.jackson.databind.JsonNode;

/**
 * A STAC item footprint as a stored geometry (prompt 096): read, split at the antimeridian, simplified to
 * the store's vertex limit, and intersected with the AOI for the coverage percentage.
 *
 * <p>The footprint is read with a raised vertex cap (ordinary footprints are tiny, but a reprocessed
 * mosaic can be large), then unwrapped and cut at ±180° into a MultiPolygon (a footprint that crosses the
 * antimeridian is stored as two parts), then simplified with {@link TopologyPreservingSimplifier} at
 * doubling tolerances until it fits {@value #MAX_VERTICES} vertices. Everything carries SRID 4326.
 *
 * <p>{@link #aoiCoverPct} projects the footprint and the AOI onto a local Lambert azimuthal equal-area
 * plane centred on the AOI and divides the intersection's area by the AOI's: on an equal-area projection
 * the plane ratio is the spherical area ratio.
 */
final class Footprints {

    /** The vertex count a stored geometry may have after simplification. */
    static final int MAX_VERTICES = 9_500;

    /** The first simplification tolerance, degrees. */
    static final double START_TOLERANCE_DEG = 0.0005;

    /** The largest footprint read before simplification. */
    static final int READ_MAX_VERTICES = 200_000;

    /** At most this many doublings are tried (0.0005° · 2^24 is far beyond any extent). */
    private static final int MAX_STEPS = 24;

    /** A planar factory for the equal-area projection (degrees become kilometres). */
    private static final GeometryFactory PLANE = new GeometryFactory(new PrecisionModel(), 0);

    /** Reads a footprint only; {@code Geo.fromGeoJson} would reject a large one before simplification. */
    private static final GeoJsonReader READER = new GeoJsonReader(READ_MAX_VERTICES,
            GeoJsonReader.DEFAULT_MAX_COLLECTION_DEPTH);

    /** WGS84 mean radius, metres, for the equal-area projection. */
    private static final double R = 6_371_008.8;

    private Footprints() {}

    /**
     * Reads and prepares an item's {@code geometry} member.
     *
     * @return the prepared geometry (SRID 4326), or null when the item has no geometry
     * @throws UpstreamFormatException when the geometry is malformed or has no coordinates
     */
    static Geometry read(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        Geometry geometry;
        try {
            geometry = READER.read(node);
        } catch (InvalidInputException e) {
            throw new UpstreamFormatException("Could not parse STAC item geometry: " + e.getMessage(), "", e);
        }
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        geometry.setSRID(Geo.SRID);
        return prepare(geometry);
    }

    /**
     * {@code geometry} split at the antimeridian and simplified to fit {@value #MAX_VERTICES} vertices.
     *
     * @param geometry a polygonal geometry (SRID 4326)
     */
    static Geometry prepare(Geometry geometry) {
        if (!(geometry instanceof Polygon) && !(geometry instanceof MultiPolygon)) {
            return geometry;
        }
        Geometry unwrapped = unwrap(geometry);
        MultiPolygon split = Antimeridian.split(unwrapped);
        Geometry result = split.isEmpty() ? unwrapped : split;
        result.setSRID(Geo.SRID);
        return simplify(result);
    }

    /**
     * The footprint's intersection with the AOI as a percentage of the AOI's area, in a local equal-area
     * projection centred on the AOI (0 to 100, integer).
     */
    static int aoiCoverPct(Geometry footprint, Geometry aoi) {
        if (footprint == null || aoi == null || aoi.isEmpty()) {
            return 0;
        }
        Point centre = centre(aoi);
        Geometry aoiPlane = project(aoi, centre.getX(), centre.getY());
        double aoiArea = aoiPlane.getArea();
        if (aoiArea <= 0) {
            return 0;
        }
        Geometry footprintPlane = project(footprint, centre.getX(), centre.getY());
        double intersection = aoiPlane.intersection(footprintPlane).getArea();
        long pct = Math.round(100.0 * intersection / aoiArea);
        return (int) Math.max(0, Math.min(100, pct));
    }

    /** The centroid of the AOI's largest polygon, so a split AOI is centred on a real part. */
    private static Point centre(Geometry aoi) {
        Geometry best = null;
        double bestArea = -1;
        for (int i = 0; i < aoi.getNumGeometries(); i++) {
            Geometry part = aoi.getGeometryN(i);
            if (part.getArea() > bestArea) {
                bestArea = part.getArea();
                best = part;
            }
        }
        return (best == null ? aoi : best).getCentroid();
    }

    private static Geometry simplify(Geometry geometry) {
        if (geometry.getNumPoints() <= MAX_VERTICES) {
            return geometry;
        }
        double tolerance = START_TOLERANCE_DEG;
        for (int step = 0; step < MAX_STEPS; step++) {
            Geometry simplified = TopologyPreservingSimplifier.simplify(geometry, tolerance);
            if (simplified.getNumPoints() <= MAX_VERTICES) {
                simplified.setSRID(Geo.SRID);
                return simplified;
            }
            tolerance *= 2;
        }
        throw new UpstreamFormatException("STAC footprint has " + geometry.getNumPoints()
                + " vertices and could not be simplified below " + MAX_VERTICES, "");
    }

    /** A copy of {@code geometry} with every ring's longitudes made continuous across ±180°. */
    private static Geometry unwrap(Geometry geometry) {
        if (geometry instanceof Polygon polygon) {
            return unwrapPolygon(polygon);
        }
        if (geometry instanceof MultiPolygon multi) {
            List<Polygon> parts = new ArrayList<>(multi.getNumGeometries());
            for (int i = 0; i < multi.getNumGeometries(); i++) {
                parts.add(unwrapPolygon((Polygon) multi.getGeometryN(i)));
            }
            return geometry.getFactory().createMultiPolygon(parts.toArray(Polygon[]::new));
        }
        return geometry;
    }

    private static Polygon unwrapPolygon(Polygon polygon) {
        LinearRing shell = unwrapRing(polygon.getExteriorRing());
        LinearRing[] holes = new LinearRing[polygon.getNumInteriorRing()];
        for (int h = 0; h < holes.length; h++) {
            holes[h] = unwrapRing(polygon.getInteriorRingN(h));
        }
        return polygon.getFactory().createPolygon(shell, holes);
    }

    private static LinearRing unwrapRing(LineString ring) {
        Coordinate[] coordinates = new Coordinate[ring.getNumPoints()];
        for (int i = 0; i < coordinates.length; i++) {
            Coordinate point = ring.getCoordinateN(i);
            coordinates[i] = new Coordinate(point.x, point.y);
        }
        Antimeridian.unwrap(coordinates);
        return ring.getFactory().createLinearRing(coordinates);
    }

    /** The footprint (and any geometry) projected onto the equal-area plane centred on {@code lon0/lat0}. */
    private static Geometry project(Geometry geometry, double lon0, double lat0) {
        double sinLat0 = Math.sin(Math.toRadians(lat0));
        double cosLat0 = Math.cos(Math.toRadians(lat0));
        double lon0Rad = Math.toRadians(lon0);
        return map(geometry, sinLat0, cosLat0, lon0Rad);
    }

    private static Geometry map(Geometry geometry, double sinLat0, double cosLat0, double lon0Rad) {
        if (geometry instanceof Polygon polygon) {
            LinearRing shell = projectRing(polygon.getExteriorRing(), sinLat0, cosLat0, lon0Rad);
            LinearRing[] holes = new LinearRing[polygon.getNumInteriorRing()];
            for (int h = 0; h < holes.length; h++) {
                holes[h] = projectRing(polygon.getInteriorRingN(h), sinLat0, cosLat0, lon0Rad);
            }
            return PLANE.createPolygon(shell, holes);
        }
        if (geometry instanceof MultiPolygon multi) {
            Polygon[] parts = new Polygon[multi.getNumGeometries()];
            for (int i = 0; i < parts.length; i++) {
                parts[i] = (Polygon) map(multi.getGeometryN(i), sinLat0, cosLat0, lon0Rad);
            }
            return PLANE.createMultiPolygon(parts);
        }
        Coordinate projected = projectCoordinate(geometry.getCoordinate(), sinLat0, cosLat0, lon0Rad);
        return PLANE.createPoint(projected);
    }

    private static LinearRing projectRing(LineString ring, double sinLat0, double cosLat0, double lon0Rad) {
        Coordinate[] coordinates = new Coordinate[ring.getNumPoints()];
        for (int i = 0; i < coordinates.length; i++) {
            coordinates[i] = projectCoordinate(ring.getCoordinateN(i), sinLat0, cosLat0, lon0Rad);
        }
        return PLANE.createLinearRing(coordinates);
    }

    /** Spherical Lambert azimuthal equal-area (Snyder eq. 24-1..24-3) centred on lon0/lat0. */
    private static Coordinate projectCoordinate(Coordinate c, double sinLat0, double cosLat0, double lon0Rad) {
        double lat = Math.toRadians(c.y);
        double dLon = Math.toRadians(c.x) - lon0Rad;
        double sinLat = Math.sin(lat);
        double cosLat = Math.cos(lat);
        double cosDLon = Math.cos(dLon);
        double denominator = 1 + sinLat0 * sinLat + cosLat0 * cosLat * cosDLon;
        double k = Math.sqrt(2.0 / Math.max(denominator, 1e-12));
        double x = R * k * cosLat * Math.sin(dLon);
        double y = R * k * (cosLat0 * sinLat - sinLat0 * cosLat * cosDLon);
        return new Coordinate(x, y);
    }
}
