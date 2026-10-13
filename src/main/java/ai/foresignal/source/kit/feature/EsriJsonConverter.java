package ai.foresignal.source.kit.feature;

import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * Esri JSON ({@code f=json}) features as GeoJSON features (prompt 073), for the rare spec that sets
 * {@code format: esrijson}.
 *
 * <ul>
 *   <li>{@code {x, y}} becomes a Point (a null or NaN {@code x} is no geometry), {@code points} a MultiPoint;
 *   <li>{@code paths} becomes a LineString (one path) or a MultiLineString;
 *   <li>{@code rings} becomes a Polygon or MultiPolygon. Esri marks a ring's role by its orientation:
 *       <strong>clockwise is an outer ring</strong>, counter-clockwise a hole. Each hole joins the outer ring that
 *       contains its first vertex (a hole contained by none becomes an outer ring of its own). The output follows
 *       RFC 7946: outer rings counter-clockwise, holes clockwise;
 *   <li>{@code attributes} become {@code properties}; Z and M values are dropped.
 * </ul>
 */
public final class EsriJsonConverter {

    private static final JsonNodeFactory NODES = JsonNodeFactory.instance;

    private EsriJsonConverter() {}

    /**
     * The GeoJSON features of an Esri JSON query response.
     *
     * @param root the response document
     */
    public static List<JsonNode> features(JsonNode root) {
        List<JsonNode> out = new ArrayList<>();
        JsonNode features = root.get("features");
        if (features == null || !features.isArray()) {
            return out;
        }
        for (JsonNode feature : features) {
            ObjectNode converted = NODES.objectNode();
            converted.put("type", "Feature");
            JsonNode geometry = geometry(feature.get("geometry"));
            converted.set("geometry", geometry == null ? NODES.nullNode() : geometry);
            JsonNode attributes = feature.get("attributes");
            converted.set("properties", attributes != null && attributes.isObject() ? attributes : NODES.objectNode());
            out.add(converted);
        }
        return out;
    }

    /**
     * One Esri geometry as GeoJSON, or null when it has no coordinates.
     *
     * @param g the Esri geometry object
     */
    public static JsonNode geometry(JsonNode g) {
        if (g == null || !g.isObject()) {
            return null;
        }
        if (g.has("x")) {
            JsonNode x = g.get("x");
            JsonNode y = g.get("y");
            if (x == null || y == null || !x.isNumber() || !y.isNumber() || Double.isNaN(x.doubleValue())) {
                return null;
            }
            ObjectNode point = NODES.objectNode();
            point.put("type", "Point");
            point.set("coordinates", position(x.doubleValue(), y.doubleValue()));
            return point;
        }
        if (g.has("points")) {
            ArrayNode coordinates = NODES.arrayNode();
            for (JsonNode p : g.get("points")) {
                coordinates.add(position(p));
            }
            return coordinates.isEmpty() ? null : typed("MultiPoint", coordinates);
        }
        if (g.has("paths")) {
            ArrayNode lines = NODES.arrayNode();
            for (JsonNode path : g.get("paths")) {
                lines.add(line(path));
            }
            if (lines.isEmpty()) {
                return null;
            }
            return lines.size() == 1 ? typed("LineString", lines.get(0)) : typed("MultiLineString", lines);
        }
        if (g.has("rings")) {
            return polygon(g.get("rings"));
        }
        return null;
    }

    private static JsonNode polygon(JsonNode rings) {
        List<double[][]> outers = new ArrayList<>();
        List<double[][]> holes = new ArrayList<>();
        for (JsonNode ring : rings) {
            double[][] r = ring(ring);
            if (r.length < 4) {
                continue;
            }
            if (signedArea(r) < 0) {
                outers.add(r);
            } else {
                holes.add(r);
            }
        }
        List<List<double[][]>> polygons = new ArrayList<>();
        for (double[][] outer : outers) {
            List<double[][]> polygon = new ArrayList<>();
            polygon.add(outer);
            polygons.add(polygon);
        }
        for (double[][] hole : holes) {
            List<double[][]> owner = null;
            for (List<double[][]> polygon : polygons) {
                if (contains(polygon.get(0), hole[0][0], hole[0][1])) {
                    owner = polygon;
                    break;
                }
            }
            if (owner == null) {
                // A counter-clockwise ring inside no outer ring: treat it as an outer ring of its own.
                List<double[][]> polygon = new ArrayList<>();
                polygon.add(hole);
                polygons.add(polygon);
            } else {
                owner.add(hole);
            }
        }
        if (polygons.isEmpty()) {
            return null;
        }
        ArrayNode all = NODES.arrayNode();
        for (List<double[][]> polygon : polygons) {
            ArrayNode rs = NODES.arrayNode();
            for (int i = 0; i < polygon.size(); i++) {
                double[][] r = polygon.get(i);
                boolean ccw = signedArea(r) > 0;
                // RFC 7946: the exterior counter-clockwise, holes clockwise.
                rs.add(coordinates(i == 0 ? (ccw ? r : reverse(r)) : (ccw ? reverse(r) : r)));
            }
            all.add(rs);
        }
        return all.size() == 1 ? typed("Polygon", all.get(0)) : typed("MultiPolygon", all);
    }

    /** The shoelace signed area: positive for counter-clockwise, negative for clockwise (x east, y north). */
    static double signedArea(double[][] ring) {
        double sum = 0;
        for (int i = 0; i < ring.length - 1; i++) {
            sum += ring[i][0] * ring[i + 1][1] - ring[i + 1][0] * ring[i][1];
        }
        return sum / 2;
    }

    /** Even-odd point-in-ring test. */
    static boolean contains(double[][] ring, double x, double y) {
        boolean inside = false;
        for (int i = 0, j = ring.length - 1; i < ring.length; j = i++) {
            double xi = ring[i][0];
            double yi = ring[i][1];
            double xj = ring[j][0];
            double yj = ring[j][1];
            if ((yi > y) != (yj > y) && x < (xj - xi) * (y - yi) / (yj - yi) + xi) {
                inside = !inside;
            }
        }
        return inside;
    }

    private static double[][] ring(JsonNode ring) {
        List<double[]> points = new ArrayList<>();
        for (JsonNode p : ring) {
            if (p.isArray() && p.size() >= 2 && p.get(0).isNumber() && p.get(1).isNumber()) {
                points.add(new double[] {p.get(0).doubleValue(), p.get(1).doubleValue()});
            }
        }
        if (!points.isEmpty()) {
            double[] first = points.get(0);
            double[] last = points.get(points.size() - 1);
            if (first[0] != last[0] || first[1] != last[1]) {
                points.add(new double[] {first[0], first[1]});
            }
        }
        return points.toArray(new double[0][]);
    }

    private static double[][] reverse(double[][] ring) {
        double[][] out = new double[ring.length][];
        for (int i = 0; i < ring.length; i++) {
            out[i] = ring[ring.length - 1 - i];
        }
        return out;
    }

    private static ArrayNode coordinates(double[][] ring) {
        ArrayNode out = NODES.arrayNode();
        for (double[] p : ring) {
            out.add(position(p[0], p[1]));
        }
        return out;
    }

    private static ArrayNode line(JsonNode path) {
        ArrayNode out = NODES.arrayNode();
        for (JsonNode p : path) {
            out.add(position(p));
        }
        return out;
    }

    private static ArrayNode position(JsonNode p) {
        return position(p.get(0).doubleValue(), p.get(1).doubleValue());
    }

    private static ArrayNode position(double x, double y) {
        ArrayNode out = NODES.arrayNode();
        out.add(x);
        out.add(y);
        return out;
    }

    private static ObjectNode typed(String type, JsonNode coordinates) {
        ObjectNode out = NODES.objectNode();
        out.put("type", type);
        out.set("coordinates", coordinates);
        return out;
    }
}
