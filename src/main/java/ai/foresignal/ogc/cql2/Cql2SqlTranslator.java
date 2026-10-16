package ai.foresignal.ogc.cql2;

import ai.foresignal.ogc.cql2.Cql2.And;
import ai.foresignal.ogc.cql2.Cql2.Bbox;
import ai.foresignal.ogc.cql2.Cql2.Bool;
import ai.foresignal.ogc.cql2.Cql2.CompareOp;
import ai.foresignal.ogc.cql2.Cql2.Comparison;
import ai.foresignal.ogc.cql2.Cql2.Date;
import ai.foresignal.ogc.cql2.Cql2.Expr;
import ai.foresignal.ogc.cql2.Cql2.IsNull;
import ai.foresignal.ogc.cql2.Cql2.Literal;
import ai.foresignal.ogc.cql2.Cql2.Not;
import ai.foresignal.ogc.cql2.Cql2.Num;
import ai.foresignal.ogc.cql2.Cql2.Or;
import ai.foresignal.ogc.cql2.Cql2.Point;
import ai.foresignal.ogc.cql2.Cql2.Polygon;
import ai.foresignal.ogc.cql2.Cql2.Property;
import ai.foresignal.ogc.cql2.Cql2.SIntersects;
import ai.foresignal.ogc.cql2.Cql2.Text;
import ai.foresignal.ogc.cql2.Cql2.Timestamp;
import ai.foresignal.ogc.cql2.Cql2Queryables.Type;
import ai.foresignal.store.query.SqlParams;
import ai.foresignal.trigger.Operator;
import ai.foresignal.trigger.OperatorRegistry;
import ai.foresignal.trigger.SqlTerm;
import java.util.stream.Collectors;

/**
 * Translates a {@link Cql2} tree to a parameterized SQL predicate over a record table (prompt 112).
 * Every value, property name and geometry is a bound parameter; nothing from the filter is spliced into
 * the SQL text.
 *
 * <p>Payload properties reuse the trigger engine's operator semantics (prompt 017,
 * {@link ai.foresignal.trigger.Operators}): a numeric comparison applies only to a JSON number (a type
 * guard), {@code =}/{@code <>} compare a number, string or boolean only with a value of the same JSON type,
 * and a value of another type, a missing key or JSON null never matches. As in a rule, each atomic predicate
 * is wrapped in {@code COALESCE(…, false)}, so {@code NOT} of a non-matching comparison is true. CQL2 adds
 * ordering of strings ({@code <} on text) and timestamp or date literals, compared with ISO-8601 strings in
 * the payload. Built-in properties are typed columns, and a literal of the wrong type is a
 * {@code FILTER_INVALID} error rather than a non-match.
 */
public final class Cql2SqlTranslator {

    /** ISO-8601 dates and instants that PostgreSQL casts to {@code timestamptz} without error. */
    static final String ISO_INSTANT = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])"
            + "([T ]([01]\\d|2[0-3]):[0-5]\\d(:[0-5]\\d(\\.\\d+)?)?(Z|z|[+-]\\d{2}(:?\\d{2})?)?)?$";

    private static final OperatorRegistry OPERATORS = OperatorRegistry.builtIn();

    /**
     * The SQL expressions of one record table.
     *
     * @param source the source id column ({@code h.source_id})
     * @param title the title expression, or null when titles are not stored (then {@code title} is the
     *     payload's {@code title})
     * @param observedAt the observed-time column
     * @param eventTime the event-time column
     * @param version the version column
     * @param payload the merged payload expression ({@code jsonb})
     * @param geometry the geometry column
     */
    public record Columns(String source, String title, String observedAt, String eventTime, String version,
            String payload, String geometry) {}

    private final Cql2Queryables queryables;
    private final Columns columns;
    private final SqlParams params;

    private Cql2SqlTranslator(Cql2Queryables queryables, Columns columns, SqlParams params) {
        this.queryables = queryables;
        this.columns = columns;
        this.params = params;
    }

    /**
     * The predicate for {@code expr}.
     *
     * @param expr the parsed filter
     * @param queryables the collection's queryables
     * @param columns the table's expressions
     * @param params where values are bound
     * @throws Cql2Exception for an unknown property (with its position) or a literal a built-in property
     *     cannot be compared with
     */
    public static String translate(Expr expr, Cql2Queryables queryables, Columns columns, SqlParams params) {
        return new Cql2SqlTranslator(queryables, columns, params).sql(expr);
    }

    private String sql(Expr expr) {
        return switch (expr) {
            case And a -> a.args().stream().map(this::sql).collect(Collectors.joining(" AND ", "(", ")"));
            case Or o -> o.args().stream().map(this::sql).collect(Collectors.joining(" OR ", "(", ")"));
            case Not n -> "(NOT " + sql(n.arg()) + ")";
            case Comparison c -> "COALESCE(" + comparison(c) + ", false)";
            case IsNull i -> "COALESCE(" + isNull(i.property()) + ", false)";
            case SIntersects s -> "COALESCE(" + intersects(s) + ", false)";
        };
    }

    // ------------------------------------------------------------------------------- comparisons

    private String comparison(Comparison c) {
        Property property = c.property();
        Type type = typeOf(property);
        if (Cql2Queryables.isBuiltIn(property.name())) {
            return builtIn(c, type);
        }
        if (type == Type.GEOMETRY || type == Type.OBJECT || type == Type.ARRAY) {
            throw new Cql2Exception("Property '" + property.name() + "' can only be tested with IS NULL",
                    property.position());
        }
        PayloadTerm term = new PayloadTerm(columns.payload(), property.name(), params);
        Literal value = c.value();
        CompareOp op = c.op();
        return switch (value) {
            case Num n -> op == CompareOp.EQ || op == CompareOp.NE
                    ? operator(op == CompareOp.EQ ? "==" : "!=", term, n.value())
                    : operator(op.symbol(), term, n.value());
            case Bool b -> {
                if (op != CompareOp.EQ && op != CompareOp.NE) {
                    throw new Cql2Exception("A boolean can only be compared with = or <>", property.position());
                }
                yield operator(op == CompareOp.EQ ? "==" : "!=", term, b.value());
            }
            case Text t -> op == CompareOp.EQ || op == CompareOp.NE
                    ? operator(op == CompareOp.EQ ? "==" : "!=", term, t.value())
                    : term.string() + " " + op.symbol() + " " + term.bind("v", t.value(), "text");
            case Timestamp ts -> "(CASE WHEN " + term.typeof() + " = 'string' AND " + term.text() + " ~ "
                    + params.text(ISO_INSTANT) + " THEN (" + term.text() + ")::timestamptz END) " + op.symbol() + " "
                    + params.instant(ts.value());
            case Date d -> "(CASE WHEN " + term.typeof() + " = 'string' AND " + term.text() + " ~ "
                    + params.text(ISO_INSTANT) + " THEN (left(" + term.text() + ", 10))::date END) " + op.symbol()
                    + " CAST(" + params.add(java.sql.Date.valueOf(d.value())) + " AS date)";
        };
    }

    private String operator(String name, SqlTerm term, Object value) {
        Operator operator = OPERATORS.find(name)
                .orElseThrow(() -> new IllegalStateException("No built-in operator " + name));
        return "(" + operator.sql(term, operator.normalize(value)) + ")";
    }

    private String builtIn(Comparison c, Type type) {
        Property property = c.property();
        String column = column(property.name());
        String op = " " + c.op().symbol() + " ";
        Literal value = c.value();
        switch (type) {
            case STRING:
                if (value instanceof Text t) {
                    return column + op + params.text(t.value());
                }
                break;
            case INTEGER:
                if (value instanceof Num n) {
                    return column + op + "CAST(" + params.add(n.value()) + " AS numeric)";
                }
                break;
            case TIMESTAMP:
                if (value instanceof Timestamp ts) {
                    return column + op + params.instant(ts.value());
                }
                if (value instanceof Date d) {
                    return "(" + column + " AT TIME ZONE 'UTC')::date" + op + "CAST("
                            + params.add(java.sql.Date.valueOf(d.value())) + " AS date)";
                }
                break;
            default:
                throw new Cql2Exception("Property '" + property.name() + "' can only be tested with "
                        + (type == Type.GEOMETRY ? "S_INTERSECTS or IS NULL" : "IS NULL"), property.position());
        }
        throw new Cql2Exception("Property '" + property.name() + "' is a " + type.name().toLowerCase(java.util.Locale.ROOT)
                + " and cannot be compared with " + describe(value), property.position());
    }

    private static String describe(Literal value) {
        return switch (value) {
            case Text t -> "a string";
            case Num n -> "a number";
            case Bool b -> "a boolean";
            case Timestamp t -> "a timestamp";
            case Date d -> "a date";
        };
    }

    // --------------------------------------------------------------------------------- IS NULL

    private String isNull(Property property) {
        typeOf(property);
        if (Cql2Queryables.isBuiltIn(property.name())) {
            return column(property.name()) + " IS NULL";
        }
        PayloadTerm term = new PayloadTerm(columns.payload(), property.name(), params);
        return "(" + term.json() + " IS NULL OR " + term.typeof() + " = 'null')";
    }

    // ---------------------------------------------------------------------------- S_INTERSECTS

    private String intersects(SIntersects s) {
        Type type = typeOf(s.property());
        if (type != Type.GEOMETRY) {
            throw new Cql2Exception("S_INTERSECTS needs the geometry property '" + Cql2Queryables.GEOMETRY + "'",
                    s.property().position());
        }
        String geom = columns.geometry();
        return switch (s.geometry()) {
            case Point p -> "ST_Intersects(" + geom + ", ST_SetSRID(ST_MakePoint(" + params.dbl(p.lon()) + ", "
                    + params.dbl(p.lat()) + "), 4326))";
            case Bbox b -> bbox(geom, b);
            case Polygon p -> "ST_Intersects(" + geom + ", ST_GeomFromText(" + params.text(p.wkt()) + ", 4326))";
        };
    }

    private String bbox(String geom, Bbox b) {
        if (b.minLon() <= b.maxLon()) {
            return envelope(geom, b.minLon(), b.minLat(), b.maxLon(), b.maxLat());
        }
        // a box across the antimeridian is two envelopes
        return "(" + envelope(geom, b.minLon(), b.minLat(), 180, b.maxLat()) + " OR "
                + envelope(geom, -180, b.minLat(), b.maxLon(), b.maxLat()) + ")";
    }

    private String envelope(String geom, double w, double s, double e, double n) {
        String env = "ST_MakeEnvelope(" + params.dbl(w) + ", " + params.dbl(s) + ", " + params.dbl(e) + ", "
                + params.dbl(n) + ", 4326)";
        return "(" + geom + " && " + env + " AND ST_Intersects(" + geom + ", " + env + "))";
    }

    // ---------------------------------------------------------------------------------- helpers

    private Type typeOf(Property property) {
        Type type = queryables.type(property.name());
        if (type == null) {
            throw new Cql2Exception("Unknown property '" + property.name() + "'; see the collection's queryables",
                    property.position());
        }
        return type;
    }

    private String column(String name) {
        return switch (name) {
            case "title" -> columns.title() != null ? columns.title()
                    : "(" + columns.payload() + " ->> " + params.text("title") + ")";
            case "sourceId" -> columns.source();
            case "observedAt" -> columns.observedAt();
            case "eventTime" -> columns.eventTime();
            case "version" -> columns.version();
            case Cql2Queryables.GEOMETRY -> columns.geometry();
            default -> throw new IllegalArgumentException(name);
        };
    }

    /** A {@link SqlTerm} for one top-level payload key, bound as a parameter. */
    static final class PayloadTerm implements SqlTerm {

        private final String payload;
        private final String key;
        private final SqlParams params;

        PayloadTerm(String payload, String name, SqlParams params) {
            this.payload = payload;
            this.params = params;
            this.key = params.text(name);
        }

        @Override
        public String json() {
            return "(" + payload + " -> " + key + ")";
        }

        @Override
        public String text() {
            return "(" + payload + " ->> " + key + ")";
        }

        @Override
        public String typeof() {
            return "jsonb_typeof(" + json() + ")";
        }

        @Override
        public String bind(String suffix, Object value, String sqlType) {
            return "CAST(" + params.add(value) + " AS " + sqlType + ")";
        }
    }
}
