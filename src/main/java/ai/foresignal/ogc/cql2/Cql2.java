package ai.foresignal.ogc.cql2;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * The abstract syntax tree of a basic CQL2 filter (OGC 21-065r2, Basic CQL2 plus Basic Spatial
 * Functions), shared by {@link Cql2TextParser} and {@link Cql2JsonParser} so that the two encodings of
 * one filter give equal trees.
 *
 * <p>Normal form, applied by both parsers: {@code AND} and {@code OR} are n-ary and flattened (nested
 * conjunctions are merged); {@code x IS NOT NULL} is {@code NOT(IS NULL(x))}; a comparison whose
 * literal is on the left is turned around ({@code 5 < x} is {@code x > 5}); {@code S_INTERSECTS} takes
 * the property first; numbers are {@link BigDecimal} without trailing zeros.
 */
public final class Cql2 {

    /** At most this many levels of nested boolean expressions. */
    public static final int MAX_DEPTH = 20;

    /** At most this many nodes (expressions, properties and literals). */
    public static final int MAX_NODES = 200;

    private Cql2() {}

    /** A boolean expression. */
    public sealed interface Expr permits And, Or, Not, Comparison, IsNull, SIntersects {}

    /** A scalar operand: a property reference or a literal. */
    public sealed interface Operand permits Property, Literal {}

    /** A literal value. */
    public sealed interface Literal extends Operand permits Text, Num, Bool, Timestamp, Date {}

    /** A geometry literal of {@code S_INTERSECTS}. */
    public sealed interface Geometry permits Point, Polygon, Bbox {}

    /** The comparison operators of Basic CQL2. */
    public enum CompareOp {
        /** {@code =}. */
        EQ("="),
        /** {@code <>}. */
        NE("<>"),
        /** {@code <}. */
        LT("<"),
        /** {@code <=}. */
        LE("<="),
        /** {@code >}. */
        GT(">"),
        /** {@code >=}. */
        GE(">=");

        private final String symbol;

        CompareOp(String symbol) {
            this.symbol = symbol;
        }

        /** The operator as written in CQL2 text and JSON. */
        public String symbol() {
            return symbol;
        }

        /** The operator with its operands swapped ({@code <} becomes {@code >}). */
        public CompareOp flipped() {
            return switch (this) {
                case LT -> GT;
                case LE -> GE;
                case GT -> LT;
                case GE -> LE;
                default -> this;
            };
        }

        /** The operator for a symbol, or null. */
        public static CompareOp of(String symbol) {
            for (CompareOp op : values()) {
                if (op.symbol.equals(symbol)) {
                    return op;
                }
            }
            return null;
        }
    }

    /**
     * A conjunction.
     *
     * @param args two or more operands, none of them an {@code And}
     */
    public record And(List<Expr> args) implements Expr {
        /** Copies the arguments. */
        public And {
            args = List.copyOf(args);
        }
    }

    /**
     * A disjunction.
     *
     * @param args two or more operands, none of them an {@code Or}
     */
    public record Or(List<Expr> args) implements Expr {
        /** Copies the arguments. */
        public Or {
            args = List.copyOf(args);
        }
    }

    /**
     * A negation.
     *
     * @param arg the negated expression
     */
    public record Not(Expr arg) implements Expr {}

    /**
     * A binary comparison of a property with a literal.
     *
     * @param op the operator
     * @param property the property (left side)
     * @param value the literal (right side)
     */
    public record Comparison(CompareOp op, Property property, Literal value) implements Expr {}

    /**
     * {@code property IS NULL}.
     *
     * @param property the tested property
     */
    public record IsNull(Property property) implements Expr {}

    /**
     * {@code S_INTERSECTS(property, geometry)}.
     *
     * @param property the geometry property
     * @param geometry the literal geometry
     */
    public record SIntersects(Property property, Geometry geometry) implements Expr {}

    /**
     * A property reference. Equality and hash code consider only the name, so the text and JSON forms of
     * one filter compare equal although their positions differ.
     *
     * @param name the queryable's name
     * @param position where the reference appears (character offset or JSON pointer), or null
     */
    public record Property(String name, String position) implements Operand {

        /** A reference without a position. */
        public Property(String name) {
            this(name, null);
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Property p && p.name.equals(name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    /**
     * A character string literal.
     *
     * @param value the string
     */
    public record Text(String value) implements Literal {}

    /**
     * A numeric literal.
     *
     * @param value the number, normalized without trailing zeros
     */
    public record Num(BigDecimal value) implements Literal {
        /** Strips trailing zeros so that {@code 5} and {@code 5.0} are equal. */
        public Num {
            value = value.signum() == 0 ? BigDecimal.ZERO : value.stripTrailingZeros();
        }
    }

    /**
     * A boolean literal.
     *
     * @param value the boolean
     */
    public record Bool(boolean value) implements Literal {}

    /**
     * {@code TIMESTAMP('…')}.
     *
     * @param value the instant
     */
    public record Timestamp(Instant value) implements Literal {}

    /**
     * {@code DATE('…')}.
     *
     * @param value the date
     */
    public record Date(LocalDate value) implements Literal {}

    /**
     * {@code POINT(lon lat)}.
     *
     * @param lon the longitude
     * @param lat the latitude
     */
    public record Point(double lon, double lat) implements Geometry {}

    /**
     * {@code POLYGON((lon lat, …), …)}; each ring is a flat list {@code lon, lat, lon, lat, …}.
     *
     * @param rings the exterior ring first, then holes
     */
    public record Polygon(List<List<Double>> rings) implements Geometry {
        /** Copies the rings. */
        public Polygon {
            rings = rings.stream().map(List::copyOf).toList();
        }

        /** The polygon as WKT. */
        public String wkt() {
            StringBuilder sb = new StringBuilder("POLYGON(");
            for (int r = 0; r < rings.size(); r++) {
                if (r > 0) {
                    sb.append(',');
                }
                sb.append('(');
                List<Double> ring = rings.get(r);
                for (int i = 0; i + 1 < ring.size(); i += 2) {
                    if (i > 0) {
                        sb.append(',');
                    }
                    sb.append(ring.get(i)).append(' ').append(ring.get(i + 1));
                }
                sb.append(')');
            }
            return sb.append(')').toString();
        }
    }

    /**
     * {@code BBOX(minLon, minLat, maxLon, maxLat)}.
     *
     * @param minLon west
     * @param minLat south
     * @param maxLon east
     * @param maxLat north
     */
    public record Bbox(double minLon, double minLat, double maxLon, double maxLat) implements Geometry {}

    /** Builds an {@code And}, flattening nested conjunctions; one argument is returned as is. */
    public static Expr and(List<Expr> args) {
        List<Expr> flat = new java.util.ArrayList<>();
        for (Expr e : args) {
            if (e instanceof And a) {
                flat.addAll(a.args());
            } else {
                flat.add(e);
            }
        }
        return flat.size() == 1 ? flat.getFirst() : new And(flat);
    }

    /** Builds an {@code Or}, flattening nested disjunctions; one argument is returned as is. */
    public static Expr or(List<Expr> args) {
        List<Expr> flat = new java.util.ArrayList<>();
        for (Expr e : args) {
            if (e instanceof Or o) {
                flat.addAll(o.args());
            } else {
                flat.add(e);
            }
        }
        return flat.size() == 1 ? flat.getFirst() : new Or(flat);
    }

    /**
     * Builds a comparison from two operands, one of which must be a property and the other a literal.
     *
     * @throws Cql2Exception when both sides are properties or both are literals
     */
    public static Comparison compare(CompareOp op, Operand left, Operand right, String position) {
        if (left instanceof Property p && right instanceof Literal l) {
            return new Comparison(op, p, l);
        }
        if (left instanceof Literal l && right instanceof Property p) {
            return new Comparison(op.flipped(), p, l);
        }
        throw new Cql2Exception("A comparison needs one property and one literal", position);
    }

    /**
     * Checks the depth and node limits of a parsed tree.
     *
     * @throws Cql2Exception when the tree is deeper than {@link #MAX_DEPTH} or has more than
     *     {@link #MAX_NODES} nodes
     */
    public static Expr checkLimits(Expr expr) {
        int[] nodes = {0};
        int depth = measure(expr, 1, nodes);
        if (depth > MAX_DEPTH) {
            throw new Cql2Exception("The filter is nested " + depth + " levels deep; at most " + MAX_DEPTH, null);
        }
        if (nodes[0] > MAX_NODES) {
            throw new Cql2Exception("The filter has " + nodes[0] + " nodes; at most " + MAX_NODES, null);
        }
        return expr;
    }

    private static int measure(Expr expr, int level, int[] nodes) {
        nodes[0]++;
        return switch (expr) {
            case And a -> a.args().stream().mapToInt(e -> measure(e, level + 1, nodes)).max().orElse(level);
            case Or o -> o.args().stream().mapToInt(e -> measure(e, level + 1, nodes)).max().orElse(level);
            case Not n -> measure(n.arg(), level + 1, nodes);
            case Comparison c -> {
                nodes[0] += 2;
                yield level;
            }
            case IsNull i -> {
                nodes[0] += 1;
                yield level;
            }
            case SIntersects s -> {
                nodes[0] += 2;
                yield level;
            }
        };
    }
}
