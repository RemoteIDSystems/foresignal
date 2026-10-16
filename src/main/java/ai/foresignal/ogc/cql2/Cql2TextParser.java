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
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A hand-written recursive-descent parser for basic CQL2-Text (OGC 21-065r2 §8 and Annex A).
 *
 * <p>Grammar (keywords are case-insensitive):
 *
 * <pre>
 * expr       = term { OR term }
 * term       = factor { AND factor }
 * factor     = NOT factor | '(' expr ')' | predicate
 * predicate  = S_INTERSECTS '(' geomArg ',' geomArg ')'
 *            | scalar ( compOp scalar | IS [NOT] NULL )
 * scalar     = property | 'string' | number | TRUE | FALSE | TIMESTAMP('…') | DATE('…')
 * geomArg    = property | POINT(x y) | POLYGON((x y, …), …) | BBOX(x1, y1, x2, y2)
 * property   = identifier | "quoted identifier"
 * </pre>
 *
 * <p>Positions in errors are zero-based character offsets into the filter.
 */
public final class Cql2TextParser {

    private final String src;
    private int pos;
    private int depth;

    private Cql2TextParser(String src) {
        this.src = src;
    }

    /**
     * Parses a CQL2-Text filter.
     *
     * @throws Cql2Exception on a syntax error (with its offset) or when a limit is exceeded
     */
    public static Expr parse(String text) {
        if (text == null || text.isBlank()) {
            throw new Cql2Exception("The filter is empty", "0");
        }
        Cql2TextParser p = new Cql2TextParser(text);
        Expr expr = p.expr();
        p.skipSpace();
        if (p.pos < p.src.length()) {
            throw p.error("Unexpected text '" + p.preview() + "'");
        }
        return Cql2.checkLimits(expr);
    }

    private Expr expr() {
        enter();
        List<Expr> parts = new ArrayList<>();
        parts.add(term());
        while (keyword("OR")) {
            parts.add(term());
        }
        depth--;
        return Cql2.or(parts);
    }

    private Expr term() {
        List<Expr> parts = new ArrayList<>();
        parts.add(factor());
        while (keyword("AND")) {
            parts.add(factor());
        }
        return Cql2.and(parts);
    }

    private Expr factor() {
        if (keyword("NOT")) {
            enter();
            Expr inner = factor();
            depth--;
            return new Not(inner);
        }
        skipSpace();
        if (peek() == '(') {
            pos++;
            Expr inner = expr();
            expect(')');
            return inner;
        }
        return predicate();
    }

    private Expr predicate() {
        skipSpace();
        int start = pos;
        if (keyword("S_INTERSECTS")) {
            expect('(');
            Object a = geomArg();
            expect(',');
            Object b = geomArg();
            expect(')');
            if (a instanceof Property p && b instanceof Geometry g) {
                return new SIntersects(p, g);
            }
            if (a instanceof Geometry g && b instanceof Property p) {
                return new SIntersects(p, g);
            }
            throw new Cql2Exception("S_INTERSECTS needs one geometry property and one geometry literal",
                    String.valueOf(start));
        }
        Operand left = scalar();
        if (keyword("IS")) {
            boolean negated = keyword("NOT");
            if (!keyword("NULL")) {
                throw error("Expected NULL");
            }
            if (!(left instanceof Property p)) {
                throw new Cql2Exception("IS NULL needs a property", String.valueOf(start));
            }
            Expr isNull = new IsNull(p);
            return negated ? new Not(isNull) : isNull;
        }
        CompareOp op = compOp();
        Operand right = scalar();
        return Cql2.compare(op, left, right, String.valueOf(start));
    }

    private CompareOp compOp() {
        skipSpace();
        int start = pos;
        String two = pos + 2 <= src.length() ? src.substring(pos, pos + 2) : "";
        if (two.equals("<>") || two.equals("<=") || two.equals(">=")) {
            pos += 2;
            return CompareOp.of(two);
        }
        char c = peek();
        if (c == '=' || c == '<' || c == '>') {
            pos++;
            return CompareOp.of(String.valueOf(c));
        }
        pos = start;
        throw error("Expected a comparison operator, IS NULL or IS NOT NULL");
    }

    private Operand scalar() {
        skipSpace();
        int start = pos;
        char c = peek();
        if (c == '\'') {
            return new Text(string());
        }
        if (c == '-' || c == '+' || c == '.' || Character.isDigit(c)) {
            return new Num(number());
        }
        if (c == '"') {
            return new Property(quotedIdentifier(), String.valueOf(start));
        }
        if (isIdentStart(c)) {
            String word = identifier();
            String upper = word.toUpperCase(Locale.ROOT);
            switch (upper) {
                case "TRUE":
                    return new Bool(true);
                case "FALSE":
                    return new Bool(false);
                case "TIMESTAMP":
                    return new Timestamp(timestamp(instantArg(start), start));
                case "DATE":
                    return new Date(date(instantArg(start), start));
                case "AND", "OR", "NOT", "IS", "NULL":
                    pos = start;
                    throw error("Expected a property or a literal, found the keyword " + upper);
                default:
                    return new Property(word, String.valueOf(start));
            }
        }
        throw error(c == 0 ? "Unexpected end of the filter" : "Expected a property or a literal");
    }

    private Object geomArg() {
        skipSpace();
        int start = pos;
        if (peek() == '"') {
            return new Property(quotedIdentifier(), String.valueOf(start));
        }
        if (!isIdentStart(peek())) {
            throw error("Expected a geometry property or a geometry literal");
        }
        String word = identifier();
        String upper = word.toUpperCase(Locale.ROOT);
        switch (upper) {
            case "POINT": {
                expect('(');
                double x = coordinate();
                double y = coordinate();
                expect(')');
                return new Point(x, y);
            }
            case "POLYGON": {
                expect('(');
                List<List<Double>> rings = new ArrayList<>();
                do {
                    rings.add(ring());
                } while (accept(','));
                expect(')');
                return new Polygon(rings);
            }
            case "BBOX": {
                expect('(');
                double[] v = new double[4];
                for (int i = 0; i < 4; i++) {
                    if (i > 0) {
                        expect(',');
                    }
                    v[i] = coordinate();
                }
                expect(')');
                return new Bbox(v[0], v[1], v[2], v[3]);
            }
            default:
                if (skipSpaceAndPeek() == '(') {
                    pos = start;
                    throw error("Unsupported geometry " + upper + "; use POINT, POLYGON or BBOX");
                }
                return new Property(word, String.valueOf(start));
        }
    }

    private List<Double> ring() {
        int start = pos;
        expect('(');
        List<Double> ring = new ArrayList<>();
        do {
            ring.add(coordinate());
            ring.add(coordinate());
        } while (accept(','));
        expect(')');
        int n = ring.size() / 2;
        if (n < 4 || !ring.get(0).equals(ring.get(ring.size() - 2)) || !ring.get(1).equals(ring.getLast())) {
            throw new Cql2Exception("A polygon ring needs at least four positions and must be closed",
                    String.valueOf(start));
        }
        return ring;
    }

    private double coordinate() {
        skipSpace();
        return number().doubleValue();
    }

    private String instantArg(int start) {
        expect('(');
        skipSpace();
        if (peek() != '\'') {
            throw error("Expected a quoted string");
        }
        String value = string();
        expect(')');
        return value;
    }

    private static Instant timestamp(String raw, int at) {
        try {
            return OffsetDateTime.parse(raw).toInstant();
        } catch (DateTimeParseException e) {
            try {
                return Instant.parse(raw);
            } catch (DateTimeParseException e2) {
                throw new Cql2Exception("Invalid TIMESTAMP '" + raw + "' (RFC 3339 with an offset)", String.valueOf(at));
            }
        }
    }

    private static LocalDate date(String raw, int at) {
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException e) {
            throw new Cql2Exception("Invalid DATE '" + raw + "' (YYYY-MM-DD)", String.valueOf(at));
        }
    }

    // ---------------------------------------------------------------------------- lexical helpers

    private String string() {
        int start = pos;
        pos++;
        StringBuilder sb = new StringBuilder();
        while (pos < src.length()) {
            char c = src.charAt(pos++);
            if (c == '\'') {
                if (pos < src.length() && src.charAt(pos) == '\'') {
                    sb.append('\'');
                    pos++;
                    continue;
                }
                return sb.toString();
            }
            sb.append(c);
        }
        throw new Cql2Exception("Unterminated string", String.valueOf(start));
    }

    private BigDecimal number() {
        int start = pos;
        if (peek() == '-' || peek() == '+') {
            pos++;
        }
        while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.')) {
            pos++;
        }
        if (pos < src.length() && (src.charAt(pos) == 'e' || src.charAt(pos) == 'E')) {
            pos++;
            if (peek() == '-' || peek() == '+') {
                pos++;
            }
            while (pos < src.length() && Character.isDigit(src.charAt(pos))) {
                pos++;
            }
        }
        try {
            return new BigDecimal(src.substring(start, pos));
        } catch (NumberFormatException e) {
            pos = start;
            throw error("Invalid number");
        }
    }

    private String identifier() {
        int start = pos;
        while (pos < src.length() && isIdentPart(src.charAt(pos))) {
            pos++;
        }
        return src.substring(start, pos);
    }

    private String quotedIdentifier() {
        int start = pos;
        pos++;
        int end = src.indexOf('"', pos);
        if (end < 0) {
            throw new Cql2Exception("Unterminated quoted identifier", String.valueOf(start));
        }
        String name = src.substring(pos, end);
        pos = end + 1;
        if (name.isEmpty()) {
            throw new Cql2Exception("Empty quoted identifier", String.valueOf(start));
        }
        return name;
    }

    private static boolean isIdentStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private static boolean isIdentPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '.' || c == ':' || c == '-';
    }

    /** Consumes {@code word} as a whole keyword (case-insensitive) when it comes next. */
    private boolean keyword(String word) {
        skipSpace();
        int end = pos + word.length();
        if (end > src.length() || !src.regionMatches(true, pos, word, 0, word.length())) {
            return false;
        }
        if (end < src.length() && isIdentPart(src.charAt(end))) {
            return false;
        }
        pos = end;
        return true;
    }

    private boolean accept(char c) {
        skipSpace();
        if (peek() == c) {
            pos++;
            return true;
        }
        return false;
    }

    private void expect(char c) {
        if (!accept(c)) {
            throw error("Expected '" + c + "'");
        }
    }

    private char skipSpaceAndPeek() {
        int save = pos;
        skipSpace();
        char c = peek();
        pos = save;
        return c;
    }

    private void skipSpace() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
            pos++;
        }
    }

    private char peek() {
        return pos < src.length() ? src.charAt(pos) : 0;
    }

    private void enter() {
        if (++depth > Cql2.MAX_DEPTH) {
            throw error("The filter is nested more than " + Cql2.MAX_DEPTH + " levels deep");
        }
    }

    private String preview() {
        return src.substring(pos, Math.min(src.length(), pos + 20));
    }

    private Cql2Exception error(String message) {
        return new Cql2Exception(message, String.valueOf(pos));
    }
}
