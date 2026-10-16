package ai.foresignal.ogc.cql2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.ogc.cql2.Cql2.And;
import ai.foresignal.ogc.cql2.Cql2.Bbox;
import ai.foresignal.ogc.cql2.Cql2.Bool;
import ai.foresignal.ogc.cql2.Cql2.CompareOp;
import ai.foresignal.ogc.cql2.Cql2.Comparison;
import ai.foresignal.ogc.cql2.Cql2.Date;
import ai.foresignal.ogc.cql2.Cql2.Expr;
import ai.foresignal.ogc.cql2.Cql2.IsNull;
import ai.foresignal.ogc.cql2.Cql2.Not;
import ai.foresignal.ogc.cql2.Cql2.Num;
import ai.foresignal.ogc.cql2.Cql2.Or;
import ai.foresignal.ogc.cql2.Cql2.Point;
import ai.foresignal.ogc.cql2.Cql2.Polygon;
import ai.foresignal.ogc.cql2.Cql2.Property;
import ai.foresignal.ogc.cql2.Cql2.SIntersects;
import ai.foresignal.ogc.cql2.Cql2.Text;
import ai.foresignal.ogc.cql2.Cql2.Timestamp;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class Cql2TextParserTest {

    private static Comparison cmp(String name, CompareOp op, Cql2.Literal value) {
        return new Comparison(op, new Property(name), value);
    }

    @Test
    @DisplayName("every comparison operator parses with a property on the left")
    void comparisons() {
        assertThat(Cql2TextParser.parse("mag = 5")).isEqualTo(cmp("mag", CompareOp.EQ, new Num(new BigDecimal("5"))));
        assertThat(Cql2TextParser.parse("mag <> 5")).isEqualTo(cmp("mag", CompareOp.NE, new Num(BigDecimal.valueOf(5))));
        assertThat(Cql2TextParser.parse("mag < 5")).isEqualTo(cmp("mag", CompareOp.LT, new Num(BigDecimal.valueOf(5))));
        assertThat(Cql2TextParser.parse("mag <= 5.0")).isEqualTo(cmp("mag", CompareOp.LE, new Num(BigDecimal.valueOf(5))));
        assertThat(Cql2TextParser.parse("mag>5")).isEqualTo(cmp("mag", CompareOp.GT, new Num(BigDecimal.valueOf(5))));
        assertThat(Cql2TextParser.parse("mag >= -1.5e1")).isEqualTo(cmp("mag", CompareOp.GE,
                new Num(new BigDecimal("-15"))));
    }

    @Test
    @DisplayName("a literal on the left turns the comparison around")
    void literalFirst() {
        assertThat(Cql2TextParser.parse("5 < mag")).isEqualTo(cmp("mag", CompareOp.GT, new Num(BigDecimal.valueOf(5))));
        assertThat(Cql2TextParser.parse("'x' = name")).isEqualTo(cmp("name", CompareOp.EQ, new Text("x")));
    }

    @Test
    @DisplayName("string, boolean, TIMESTAMP and DATE literals; doubled quotes escape a quote")
    void literals() {
        assertThat(Cql2TextParser.parse("name = 'O''Hare'")).isEqualTo(cmp("name", CompareOp.EQ, new Text("O'Hare")));
        assertThat(Cql2TextParser.parse("ok = TRUE")).isEqualTo(cmp("ok", CompareOp.EQ, new Bool(true)));
        assertThat(Cql2TextParser.parse("ok = false")).isEqualTo(cmp("ok", CompareOp.EQ, new Bool(false)));
        assertThat(Cql2TextParser.parse("observedAt > TIMESTAMP('2026-10-16T12:00:00Z')")).isEqualTo(
                cmp("observedAt", CompareOp.GT, new Timestamp(Instant.parse("2026-10-16T12:00:00Z"))));
        assertThat(Cql2TextParser.parse("observedAt > timestamp('2026-10-16T14:00:00+02:00')")).isEqualTo(
                cmp("observedAt", CompareOp.GT, new Timestamp(Instant.parse("2026-10-16T12:00:00Z"))));
        assertThat(Cql2TextParser.parse("day = DATE('2026-10-16')")).isEqualTo(
                cmp("day", CompareOp.EQ, new Date(LocalDate.of(2026, 10, 16))));
    }

    @Test
    @DisplayName("IS NULL and IS NOT NULL; NOT NULL is NOT(IS NULL)")
    void isNull() {
        assertThat(Cql2TextParser.parse("depth IS NULL")).isEqualTo(new IsNull(new Property("depth")));
        assertThat(Cql2TextParser.parse("depth is not null")).isEqualTo(new Not(new IsNull(new Property("depth"))));
    }

    @Test
    @DisplayName("AND binds tighter than OR; NOT binds tightest; parentheses override; chains flatten")
    void precedence() {
        Expr a = cmp("a", CompareOp.EQ, new Num(BigDecimal.ONE));
        Expr b = cmp("b", CompareOp.EQ, new Num(BigDecimal.valueOf(2)));
        Expr c = cmp("c", CompareOp.EQ, new Num(BigDecimal.valueOf(3)));
        assertThat(Cql2TextParser.parse("a = 1 OR b = 2 AND c = 3")).isEqualTo(new Or(List.of(a, new And(List.of(b, c)))));
        assertThat(Cql2TextParser.parse("(a = 1 OR b = 2) AND c = 3")).isEqualTo(new And(List.of(new Or(List.of(a, b)), c)));
        assertThat(Cql2TextParser.parse("NOT a = 1 AND b = 2")).isEqualTo(new And(List.of(new Not(a), b)));
        assertThat(Cql2TextParser.parse("a = 1 AND (b = 2 AND c = 3)")).isEqualTo(new And(List.of(a, b, c)));
        assertThat(Cql2TextParser.parse("a = 1 or b = 2 or c = 3")).isEqualTo(new Or(List.of(a, b, c)));
    }

    @Test
    @DisplayName("S_INTERSECTS with POINT, POLYGON and BBOX, in either argument order")
    void spatial() {
        assertThat(Cql2TextParser.parse("S_INTERSECTS(geometry, POINT(-97.7 30.3))"))
                .isEqualTo(new SIntersects(new Property("geometry"), new Point(-97.7, 30.3)));
        assertThat(Cql2TextParser.parse("s_intersects(BBOX(-98, 30, -97, 31), geometry)"))
                .isEqualTo(new SIntersects(new Property("geometry"), new Bbox(-98, 30, -97, 31)));
        Expr polygon = Cql2TextParser.parse("S_INTERSECTS(geometry, POLYGON((0 0, 1 0, 1 1, 0 1, 0 0)))");
        assertThat(polygon).isInstanceOf(SIntersects.class);
        assertThat(((Polygon) ((SIntersects) polygon).geometry()).wkt()).isEqualTo("POLYGON((0.0 0.0,1.0 0.0,1.0 1.0,0.0 1.0,0.0 0.0))");
    }

    @Test
    @DisplayName("quoted identifiers and dotted or colon names are properties")
    void identifiers() {
        assertThat(Cql2TextParser.parse("\"my field\" = 1")).isEqualTo(cmp("my field", CompareOp.EQ, new Num(BigDecimal.ONE)));
        assertThat(((Comparison) Cql2TextParser.parse("ns:key = 1")).property().name()).isEqualTo("ns:key");
    }

    @Test
    @DisplayName("a property keeps its character offset for errors")
    void positions() {
        Comparison c = (Comparison) Cql2TextParser.parse("a = 1 AND   bad = 2".substring(10));
        assertThat(c.property().position()).isEqualTo("2");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "mag", "mag =", "mag = 'x", "(mag = 1", "mag = 1)", "mag ! 1", "1 = 2",
        "a = b", "mag IS 5", "S_INTERSECTS(geometry, LINESTRING(0 0, 1 1))", "S_INTERSECTS(geometry, geometry)",
        "observedAt > TIMESTAMP('yesterday')", "d = DATE('2026-13-01')",
        "S_INTERSECTS(geometry, POLYGON((0 0, 1 0, 1 1, 0 0.5)))", "AND = 1", "mag = 1 extra"})
    @DisplayName("malformed filters are refused with a position")
    void errors(String filter) {
        assertThatThrownBy(() -> Cql2TextParser.parse(filter)).isInstanceOf(Cql2Exception.class)
                .satisfies(e -> assertThat(((Cql2Exception) e).position()).isNotNull());
    }

    @Test
    @DisplayName("the error names the offset of the unexpected text")
    void errorOffset() {
        assertThatThrownBy(() -> Cql2TextParser.parse("mag = 1 junk"))
                .isInstanceOf(Cql2Exception.class)
                .satisfies(e -> assertThat(((Cql2Exception) e).position()).isEqualTo("8"))
                .hasMessageContaining("at 8");
    }

    @Test
    @DisplayName("more than 20 levels of nesting are refused")
    void depthLimit() {
        String ok = "(".repeat(18) + "a = 1" + ")".repeat(18);
        assertThat(Cql2TextParser.parse(ok)).isInstanceOf(Comparison.class);
        String deep = "NOT ".repeat(25) + "a = 1";
        assertThatThrownBy(() -> Cql2TextParser.parse(deep)).isInstanceOf(Cql2Exception.class)
                .hasMessageContaining("20");
    }

    @Test
    @DisplayName("more than 200 nodes are refused")
    void nodeLimit() {
        StringBuilder sb = new StringBuilder("a = 0");
        for (int i = 1; i < 80; i++) {
            sb.append(" OR a = ").append(i);
        }
        assertThatThrownBy(() -> Cql2TextParser.parse(sb.toString())).isInstanceOf(Cql2Exception.class)
                .hasMessageContaining("200");
        StringBuilder small = new StringBuilder("a = 0");
        for (int i = 1; i < 60; i++) {
            small.append(" OR a = ").append(i);
        }
        assertThat(Cql2TextParser.parse(small.toString())).isInstanceOf(Or.class);
    }
}
