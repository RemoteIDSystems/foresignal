package ai.foresignal.ogc.cql2;

import ai.foresignal.ogc.cql2.Cql2.Bbox;
import ai.foresignal.ogc.cql2.Cql2.Bool;
import ai.foresignal.ogc.cql2.Cql2.CompareOp;
import ai.foresignal.ogc.cql2.Cql2.Date;
import ai.foresignal.ogc.cql2.Cql2.Expr;
import ai.foresignal.ogc.cql2.Cql2.Geometry;
import ai.foresignal.ogc.cql2.Cql2.IsNull;
import ai.foresignal.ogc.cql2.Cql2.Not;
import ai.foresignal.ogc.cql2.Cql2.Num;
import ai.foresignal.ogc.cql2.Cql2.Operand;
import ai.foresignal.ogc.cql2.Cql2.Point;
import ai.foresignal.ogc.cql2.Cql2.Polygon;
import ai.foresignal.ogc.cql2.Cql2.Property;
import ai.foresignal.ogc.cql2.Cql2.SIntersects;
import ai.foresignal.ogc.cql2.Cql2.Text;
import ai.foresignal.ogc.cql2.Cql2.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses basic CQL2-JSON (OGC 21-065r2 §9) with Jackson into the same {@link Cql2} tree as
 * {@link Cql2TextParser}.
 *
 * <p>Supported: {@code {"op": "and"|"or"|"not"|"="|"<>"|"<"|"<="|">"|">="|"isNull"|"s_intersects",
 * "args": […]}}, {@code {"property": "name"}}, string/number/boolean literals, {@code {"timestamp": "…"}},
 * {@code {"date": "…"}}, GeoJSON {@code Point} and {@code Polygon}, and {@code {"bbox": [4 numbers]}}.
 * Positions in errors are JSON pointers ({@code /args/1}).
 */
public final class Cql2JsonParser {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private Cql2JsonParser() {}

    /**
     * Parses a CQL2-JSON filter given as text.
     *
     * @throws Cql2Exception on invalid JSON, an unsupported construct (with its pointer) or a limit
     */
    public static Expr parse(String json) {
        if (json == null || json.isBlank()) {
            throw new Cql2Exception("The filter is empty", "");
        }
        JsonNode node;
        try {
            node = MAPPER.readTree(json);
        } catch (JacksonException e) {
            throw new Cql2Exception("The filter is not valid JSON: " + e.getOriginalMessage(), "");
        }
        return parse(node);
    }

    /**
     * Parses a CQL2-JSON filter tree.
     *
     * @throws Cql2Exception on an unsupported construct or when a limit is exceeded
     */
    public static Expr parse(JsonNode node) {
        return Cql2.checkLimits(expr(node, "", 1));
    }

    private static Expr expr(JsonNode node, String at, int depth) {
        if (depth > Cql2.MAX_DEPTH) {
            throw new Cql2Exception("The filter is nested more than " + Cql2.MAX_DEPTH + " levels deep", at);
        }
        if (node == null || !node.isObject() || !node.has("op")) {
            throw new Cql2Exception("Expected an object with \"op\" and \"args\"", at);
        }
        String op = node.get("op").asString();
        JsonNode args = node.get("args");
        if (args == null || !args.isArray()) {
            throw new Cql2Exception("\"args\" must be an array", at + "/args");
        }
        switch (op.toLowerCase(Locale.ROOT)) {
            case "and", "or": {
                if (args.size() < 2) {
                    throw new Cql2Exception("\"" + op + "\" needs at least two arguments", at + "/args");
                }
                List<Expr> parts = new ArrayList<>();
                for (int i = 0; i < args.size(); i++) {
                    parts.add(expr(args.get(i), at + "/args/" + i, depth + 1));
                }
                return "and".equalsIgnoreCase(op) ? Cql2.and(parts) : Cql2.or(parts);
            }
            case "not":
                arity(args, 1, op, at);
                return new Not(expr(args.get(0), at + "/args/0", depth + 1));
            case "isnull": {
                arity(args, 1, op, at);
                Operand operand = operand(args.get(0), at + "/args/0");
                if (!(operand instanceof Property p)) {
                    throw new Cql2Exception("isNull needs a property", at + "/args/0");
                }
                return new IsNull(p);
            }
            case "s_intersects": {
                arity(args, 2, op, at);
                Object a = geomArg(args.get(0), at + "/args/0");
                Object b = geomArg(args.get(1), at + "/args/1");
                if (a instanceof Property p && b instanceof Geometry g) {
                    return new SIntersects(p, g);
                }
                if (a instanceof Geometry g && b instanceof Property p) {
                    return new SIntersects(p, g);
                }
                throw new Cql2Exception("s_intersects needs one geometry property and one geometry literal", at + "/args");
            }
            default: {
                CompareOp compare = CompareOp.of(op);
                if (compare == null) {
                    throw new Cql2Exception("Unsupported operator \"" + op + "\"", at + "/op");
                }
                arity(args, 2, op, at);
                return Cql2.compare(compare, operand(args.get(0), at + "/args/0"),
                        operand(args.get(1), at + "/args/1"), at);
            }
        }
    }

    private static void arity(JsonNode args, int n, String op, String at) {
        if (args.size() != n) {
            throw new Cql2Exception("\"" + op + "\" needs " + n + " argument" + (n == 1 ? "" : "s"), at + "/args");
        }
    }

    private static Operand operand(JsonNode node, String at) {
        if (node == null || node.isNull()) {
            throw new Cql2Exception("A null literal is not supported; use isNull", at);
        }
        if (node.isString()) {
            return new Text(node.asString());
        }
        if (node.isNumber()) {
            return new Num(node.decimalValue());
        }
        if (node.isBoolean()) {
            return new Bool(node.booleanValue());
        }
        if (node.isObject()) {
            if (node.has("property")) {
                String name = node.get("property").asString();
                if (name == null || name.isEmpty()) {
                    throw new Cql2Exception("Empty property name", at + "/property");
                }
                return new Property(name, at + "/property");
            }
            if (node.has("timestamp")) {
                String raw = node.get("timestamp").asString();
                try {
                    return new Timestamp(OffsetDateTime.parse(raw).toInstant());
                } catch (DateTimeParseException e) {
                    try {
                        return new Timestamp(Instant.parse(raw));
                    } catch (DateTimeParseException e2) {
                        throw new Cql2Exception("Invalid timestamp '" + raw + "'", at + "/timestamp");
                    }
                }
            }
            if (node.has("date")) {
                String raw = node.get("date").asString();
                try {
                    return new Date(LocalDate.parse(raw));
                } catch (DateTimeParseException e) {
                    throw new Cql2Exception("Invalid date '" + raw + "'", at + "/date");
                }
            }
            if (node.has("op")) {
                throw new Cql2Exception("Functions and nested expressions are not supported as operands", at);
            }
        }
        throw new Cql2Exception("Expected a property or a literal", at);
    }

    private static Object geomArg(JsonNode node, String at) {
        if (node != null && node.isObject()) {
            if (node.has("property")) {
                return new Property(node.get("property").asString(), at + "/property");
            }
            if (node.has("bbox")) {
                JsonNode b = node.get("bbox");
                if (!b.isArray() || b.size() != 4) {
                    throw new Cql2Exception("bbox needs four numbers", at + "/bbox");
                }
                double[] v = new double[4];
                for (int i = 0; i < 4; i++) {
                    v[i] = number(b.get(i), at + "/bbox/" + i);
                }
                return new Bbox(v[0], v[1], v[2], v[3]);
            }
            if (node.has("type")) {
                String type = node.get("type").asString();
                JsonNode coords = node.get("coordinates");
                if ("Point".equals(type)) {
                    if (coords == null || !coords.isArray() || coords.size() < 2) {
                        throw new Cql2Exception("A Point needs [lon, lat]", at + "/coordinates");
                    }
                    return new Point(number(coords.get(0), at + "/coordinates/0"),
                            number(coords.get(1), at + "/coordinates/1"));
                }
                if ("Polygon".equals(type)) {
                    if (coords == null || !coords.isArray() || coords.isEmpty()) {
                        throw new Cql2Exception("A Polygon needs rings", at + "/coordinates");
                    }
                    List<List<Double>> rings = new ArrayList<>();
                    for (int r = 0; r < coords.size(); r++) {
                        rings.add(ring(coords.get(r), at + "/coordinates/" + r));
                    }
                    return new Polygon(rings);
                }
                throw new Cql2Exception("Unsupported geometry " + type + "; use Point, Polygon or bbox", at + "/type");
            }
        }
        throw new Cql2Exception("Expected a geometry property or a geometry literal", at);
    }

    private static List<Double> ring(JsonNode ring, String at) {
        if (!ring.isArray() || ring.size() < 4) {
            throw new Cql2Exception("A polygon ring needs at least four positions and must be closed", at);
        }
        List<Double> flat = new ArrayList<>();
        for (int i = 0; i < ring.size(); i++) {
            JsonNode p = ring.get(i);
            if (!p.isArray() || p.size() < 2) {
                throw new Cql2Exception("A position needs [lon, lat]", at + "/" + i);
            }
            flat.add(number(p.get(0), at + "/" + i + "/0"));
            flat.add(number(p.get(1), at + "/" + i + "/1"));
        }
        if (!flat.get(0).equals(flat.get(flat.size() - 2)) || !flat.get(1).equals(flat.getLast())) {
            throw new Cql2Exception("A polygon ring needs at least four positions and must be closed", at);
        }
        return flat;
    }

    private static double number(JsonNode node, String at) {
        if (node == null || !node.isNumber()) {
            throw new Cql2Exception("Expected a number", at);
        }
        return node.doubleValue();
    }
}
