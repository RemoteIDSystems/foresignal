package ai.foresignal.source.formats.geojson;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.source.spi.RecordLimits;
import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads GeoJSON (RFC 7946) into JTS geometries with SRID 4326, using Jackson 3's tree model.
 *
 * <p>Replaces {@code jts-io-common}'s reader, which depends on the unmaintained
 * {@code json-simple:1.1.1} and would have exposed it to untrusted upstream GeoJSON
 * (docs/Conventions.md, "GeoJSON reading").
 *
 * <p>Supports Point, MultiPoint, LineString, MultiLineString, Polygon, MultiPolygon and
 * GeometryCollection, and unwraps a Feature to its geometry and a FeatureCollection to a
 * GeometryCollection of its features' geometries (features with a null geometry are skipped).
 *
 * <p>Enforces the Core-Data-Model §3 limits while reading, so a hostile document cannot make it
 * allocate: the vertex count is checked <em>before</em> any geometry is constructed, every
 * ordinate must be finite, and GeometryCollection nesting is bounded. Every failure is an
 * {@link InvalidInputException}.
 *
 * <p>Thread-safe and stateless apart from its configuration.
 */
public final class GeoJsonReader {

    /** WGS 84. */
    public static final int SRID = 4326;

    /** How deeply GeometryCollections (and a FeatureCollection's wrapper) may nest. */
    public static final int DEFAULT_MAX_COLLECTION_DEPTH = 8;

    private static final GeometryFactory FACTORY = new GeometryFactory(new PrecisionModel(), SRID);

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final int maxVertices;
    private final int maxCollectionDepth;

    /** A reader with the default limits: 10,000 vertices and a collection depth of 8. */
    public GeoJsonReader() {
        this(RecordLimits.DEFAULT_MAX_GEOMETRY_VERTICES, DEFAULT_MAX_COLLECTION_DEPTH);
    }

    /**
     * A reader with explicit limits.
     *
     * @param maxVertices the most positions a document may contain
     * @param maxCollectionDepth how deeply collections may nest
     */
    public GeoJsonReader(int maxVertices, int maxCollectionDepth) {
        if (maxVertices < 1 || maxCollectionDepth < 1) {
            throw new IllegalArgumentException("GeoJSON reader limits must be positive");
        }
        this.maxVertices = maxVertices;
        this.maxCollectionDepth = maxCollectionDepth;
    }

    /**
     * Parses a GeoJSON document.
     *
     * @throws InvalidInputException when it is not JSON, not GeoJSON, or breaks a limit
     */
    public Geometry read(String json) {
        if (json == null || json.isBlank()) {
            throw new InvalidInputException("GeoJSON is empty");
        }
        JsonNode node;
        try {
            node = JSON.readTree(json);
        } catch (JacksonException e) {
            throw new InvalidInputException("GeoJSON is not valid JSON: " + e.getOriginalMessage(), e);
        }
        return read(node);
    }

    /**
     * Converts an already parsed GeoJSON tree.
     *
     * @throws InvalidInputException when it is not GeoJSON or breaks a limit
     */
    public Geometry read(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            throw new InvalidInputException("GeoJSON is empty");
        }
        int vertices = countVertices(node, 0);
        if (vertices > maxVertices) {
            throw new InvalidInputException(
                    "GeoJSON has more than " + maxVertices + " vertices");
        }
        try {
            return build(node, 0);
        } catch (IllegalArgumentException e) {
            // JTS rejects, for example, an unclosed ring or a ring of fewer than four positions.
            throw new InvalidInputException("GeoJSON geometry is malformed: " + e.getMessage(), e);
        }
    }

    // ---------- pre-pass: count before constructing ----------

    private int countVertices(JsonNode node, int depth) {
        if (!node.isObject()) {
            throw new InvalidInputException("GeoJSON must be an object, but got " + node.getNodeType());
        }
        String type = type(node);
        switch (type) {
            case "Feature" -> {
                JsonNode geometry = node.get("geometry");
                return geometry == null || geometry.isNull() ? 0 : countVertices(geometry, depth);
            }
            case "FeatureCollection", "GeometryCollection" -> {
                if (depth >= maxCollectionDepth) {
                    throw new InvalidInputException(
                            "GeoJSON collections nest deeper than " + maxCollectionDepth);
                }
                String member = type.equals("FeatureCollection") ? "features" : "geometries";
                JsonNode members = node.get(member);
                if (members == null || !members.isArray()) {
                    throw new InvalidInputException("GeoJSON " + type + " needs a '" + member + "' array");
                }
                int total = 0;
                for (JsonNode child : members) {
                    total += countVertices(child, depth + 1);
                    if (total > maxVertices) {
                        return total;
                    }
                }
                return total;
            }
            case "Point", "MultiPoint", "LineString", "MultiLineString", "Polygon", "MultiPolygon" -> {
                JsonNode coordinates = node.get("coordinates");
                if (coordinates == null || !coordinates.isArray()) {
                    throw new InvalidInputException("GeoJSON " + type + " needs a 'coordinates' array");
                }
                return countPositions(coordinates, nesting(type), type);
            }
            default -> throw new InvalidInputException("Unsupported GeoJSON type '" + type + "'");
        }
    }

    /** How many array levels sit above a single position for each geometry type. */
    private static int nesting(String type) {
        return switch (type) {
            case "Point" -> 0;
            case "MultiPoint", "LineString" -> 1;
            case "MultiLineString", "Polygon" -> 2;
            default -> 3; // MultiPolygon
        };
    }

    private int countPositions(JsonNode array, int levels, String type) {
        if (levels == 0) {
            return 1;
        }
        int total = 0;
        for (JsonNode child : array) {
            if (!child.isArray()) {
                throw new InvalidInputException("GeoJSON " + type + " coordinates are nested wrongly");
            }
            total += countPositions(child, levels - 1, type);
            if (total > maxVertices) {
                return total;
            }
        }
        return total;
    }

    // ---------- construction ----------

    private Geometry build(JsonNode node, int depth) {
        String type = type(node);
        return switch (type) {
            case "Feature" -> {
                JsonNode geometry = node.get("geometry");
                if (geometry == null || geometry.isNull()) {
                    throw new InvalidInputException("GeoJSON Feature has no geometry");
                }
                yield build(geometry, depth);
            }
            case "FeatureCollection" -> {
                List<Geometry> parts = new ArrayList<>();
                for (JsonNode feature : node.get("features")) {
                    if (!"Feature".equals(type(feature))) {
                        throw new InvalidInputException(
                                "GeoJSON FeatureCollection members must be Features");
                    }
                    JsonNode geometry = feature.get("geometry");
                    if (geometry != null && !geometry.isNull()) {
                        parts.add(build(geometry, depth + 1));
                    }
                }
                yield FACTORY.createGeometryCollection(parts.toArray(Geometry[]::new));
            }
            case "GeometryCollection" -> {
                List<Geometry> parts = new ArrayList<>();
                for (JsonNode child : node.get("geometries")) {
                    parts.add(build(child, depth + 1));
                }
                yield FACTORY.createGeometryCollection(parts.toArray(Geometry[]::new));
            }
            case "Point" -> point(node.get("coordinates"));
            case "MultiPoint" -> {
                List<Point> points = new ArrayList<>();
                for (JsonNode position : node.get("coordinates")) {
                    points.add(point(position));
                }
                yield FACTORY.createMultiPoint(points.toArray(Point[]::new));
            }
            case "LineString" -> lineString(node.get("coordinates"));
            case "MultiLineString" -> {
                List<LineString> lines = new ArrayList<>();
                for (JsonNode line : node.get("coordinates")) {
                    lines.add(lineString(line));
                }
                yield FACTORY.createMultiLineString(lines.toArray(LineString[]::new));
            }
            case "Polygon" -> polygon(node.get("coordinates"));
            case "MultiPolygon" -> {
                List<Polygon> polygons = new ArrayList<>();
                for (JsonNode polygon : node.get("coordinates")) {
                    polygons.add(polygon(polygon));
                }
                yield FACTORY.createMultiPolygon(polygons.toArray(Polygon[]::new));
            }
            default -> throw new InvalidInputException("Unsupported GeoJSON type '" + type + "'");
        };
    }

    private static String type(JsonNode node) {
        JsonNode type = node == null ? null : node.get("type");
        if (type == null || !type.isString()) {
            throw new InvalidInputException("GeoJSON object has no 'type'");
        }
        return type.stringValue();
    }

    private static Point point(JsonNode position) {
        return FACTORY.createPoint(coordinate(position));
    }

    private static LineString lineString(JsonNode positions) {
        return FACTORY.createLineString(coordinates(positions));
    }

    private static Polygon polygon(JsonNode rings) {
        if (rings.isEmpty()) {
            return FACTORY.createPolygon();
        }
        LinearRing shell = FACTORY.createLinearRing(coordinates(rings.get(0)));
        LinearRing[] holes = new LinearRing[rings.size() - 1];
        for (int i = 1; i < rings.size(); i++) {
            holes[i - 1] = FACTORY.createLinearRing(coordinates(rings.get(i)));
        }
        return FACTORY.createPolygon(shell, holes);
    }

    private static Coordinate[] coordinates(JsonNode positions) {
        Coordinate[] result = new Coordinate[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            result[i] = coordinate(positions.get(i));
        }
        return result;
    }

    private static Coordinate coordinate(JsonNode position) {
        if (position == null || !position.isArray() || position.size() < 2) {
            throw new InvalidInputException("A GeoJSON position needs at least two numbers");
        }
        double x = ordinate(position.get(0));
        double y = ordinate(position.get(1));
        if (position.size() >= 3) {
            return new Coordinate(x, y, ordinate(position.get(2)));
        }
        return new CoordinateXY(x, y);
    }

    private static double ordinate(JsonNode value) {
        if (value == null || !value.isNumber()) {
            throw new InvalidInputException("A GeoJSON ordinate must be a number");
        }
        double ordinate = value.doubleValue();
        if (!Double.isFinite(ordinate)) {
            throw new InvalidInputException("A GeoJSON ordinate must be finite, but was " + ordinate);
        }
        return ordinate;
    }
}
