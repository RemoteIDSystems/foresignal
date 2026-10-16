package ai.foresignal.ogc.cql2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.FieldType;
import ai.foresignal.store.query.SqlParams;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class Cql2SqlTranslatorTest {

    static final Cql2SqlTranslator.Columns COLUMNS = new Cql2SqlTranslator.Columns("h.source_id", "h.title",
            "l.observed_at", "h.event_time", "h.version", "p.payload", "l.geom");

    static final Cql2Queryables QUERYABLES = new Cql2Queryables(Map.of("mag", FieldType.NUMBER, "place",
            FieldType.STRING, "tsunami", FieldType.BOOLEAN, "updated", FieldType.TIMESTAMP, "props", FieldType.OBJECT,
            "x", FieldType.NUMBER));

    private static String sql(String filter, SqlParams p) {
        return Cql2SqlTranslator.translate(Cql2TextParser.parse(filter), QUERYABLES, COLUMNS, p);
    }

    private static List<Object> values(SqlParams p) {
        List<Object> out = new ArrayList<>();
        for (String name : p.source().getParameterNames()) {
            out.add(p.source().getValue(name));
        }
        return out;
    }

    @Test
    @DisplayName("a numeric payload comparison uses the rule engine's number type guard and binds the value")
    void numeric() {
        SqlParams p = new SqlParams();
        String sql = sql("mag > 5", p);
        assertThat(sql).startsWith("COALESCE(").endsWith(", false)")
                .contains("jsonb_typeof((p.payload -> CAST(:p0 AS text))) = 'number'")
                .contains("::numeric END) > CAST(:p1 AS numeric)");
        assertThat(values(p)).containsExactly("mag", new BigDecimal("5"));
    }

    @Test
    @DisplayName("= and <> type the payload by the literal, as the rule engine's == and !=")
    void equality() {
        SqlParams p = new SqlParams();
        assertThat(sql("place = 'Austin'", p)).contains("= 'string'").contains(" = CAST(:p1 AS text)");
        SqlParams q = new SqlParams();
        assertThat(sql("tsunami <> true", q)).contains("= 'boolean'").contains(" <> CAST(:p1 AS boolean)");
        assertThat(values(q)).containsExactly("tsunami", true);
        SqlParams r = new SqlParams();
        assertThat(sql("mag = 4.5", r)).contains("= 'number'").contains(" = CAST(:p1 AS numeric)");
    }

    @Test
    @DisplayName("string ordering compares typed text; boolean ordering is refused")
    void ordering() {
        SqlParams p = new SqlParams();
        assertThat(sql("place < 'M'", p)).contains("= 'string' THEN").contains(" < CAST(:p1 AS text)");
        assertThatThrownBy(() -> sql("tsunami > true", new SqlParams())).isInstanceOf(Cql2Exception.class)
                .hasMessageContaining("boolean");
    }

    @Test
    @DisplayName("timestamp and date literals compare ISO strings behind a pattern guard")
    void temporalPayload() {
        SqlParams p = new SqlParams();
        String sql = sql("updated >= TIMESTAMP('2026-10-16T00:00:00Z')", p);
        assertThat(sql).contains("::timestamptz END) >= CAST(:p").contains(" ~ CAST(:p");
        assertThat(values(p)).contains(Cql2SqlTranslator.ISO_INSTANT);
        assertThat(sql("updated = DATE('2026-10-16')", new SqlParams())).contains("::date END) = CAST(");
    }

    @Test
    @DisplayName("built-in properties are typed columns and refuse literals of another type")
    void builtIns() {
        assertThat(sql("sourceId = 'usgs'", new SqlParams())).isEqualTo("COALESCE(h.source_id = CAST(:p0 AS text), false)");
        assertThat(sql("title <> 'x'", new SqlParams())).contains("h.title <> ");
        assertThat(sql("version >= 2", new SqlParams())).isEqualTo("COALESCE(h.version >= CAST(:p0 AS numeric), false)");
        assertThat(sql("observedAt > TIMESTAMP('2026-10-16T00:00:00Z')", new SqlParams()))
                .isEqualTo("COALESCE(l.observed_at > CAST(:p0 AS timestamptz), false)");
        assertThat(sql("eventTime = DATE('2026-10-16')", new SqlParams()))
                .contains("(h.event_time AT TIME ZONE 'UTC')::date = CAST(:p0 AS date)");
        assertThatThrownBy(() -> sql("version = 'two'", new SqlParams())).isInstanceOf(Cql2Exception.class)
                .hasMessageContaining("version");
        assertThatThrownBy(() -> sql("observedAt > 5", new SqlParams())).isInstanceOf(Cql2Exception.class);
        assertThatThrownBy(() -> sql("geometry = 5", new SqlParams())).isInstanceOf(Cql2Exception.class)
                .hasMessageContaining("S_INTERSECTS");
    }

    @Test
    @DisplayName("without a title column the title is the payload's title")
    void titleFallback() {
        Cql2SqlTranslator.Columns noTitle = new Cql2SqlTranslator.Columns("v.source_id", null, "v.observed_at",
                "v.event_time", "v.version", "v.payload", "v.geom");
        SqlParams p = new SqlParams();
        String sql = Cql2SqlTranslator.translate(Cql2TextParser.parse("title = 'x'"), QUERYABLES, noTitle, p);
        assertThat(sql).contains("(v.payload ->> CAST(:p0 AS text)) = CAST(:p1 AS text)");
        assertThat(values(p)).containsExactly("title", "x");
    }

    @Test
    @DisplayName("IS NULL on a column and on a payload key (missing or JSON null)")
    void isNull() {
        assertThat(sql("eventTime IS NULL", new SqlParams())).isEqualTo("COALESCE(h.event_time IS NULL, false)");
        assertThat(sql("props IS NULL", new SqlParams()))
                .contains("(p.payload -> CAST(:p0 AS text)) IS NULL OR jsonb_typeof(");
        assertThat(sql("mag IS NOT NULL", new SqlParams())).startsWith("(NOT COALESCE(");
    }

    @Test
    @DisplayName("AND, OR and NOT keep their structure, each atom wrapped in COALESCE")
    void logic() {
        String sql = sql("(mag > 5 OR place = 'x') AND NOT tsunami = true", new SqlParams());
        assertThat(sql).startsWith("((COALESCE(").contains(" OR COALESCE(").contains(" AND (NOT COALESCE(");
    }

    @Test
    @DisplayName("S_INTERSECTS with a point, a polygon and a bbox (split at the antimeridian)")
    void spatial() {
        SqlParams p = new SqlParams();
        assertThat(sql("S_INTERSECTS(geometry, POINT(-97.7 30.3))", p))
                .contains("ST_Intersects(l.geom, ST_SetSRID(ST_MakePoint(CAST(:p0 AS double precision), CAST(:p1 AS double precision)), 4326))");
        assertThat(values(p)).containsExactly(-97.7, 30.3);
        SqlParams q = new SqlParams();
        assertThat(sql("S_INTERSECTS(geometry, POLYGON((0 0, 1 0, 1 1, 0 0)))", q))
                .contains("ST_GeomFromText(CAST(:p0 AS text), 4326)");
        assertThat(values(q)).containsExactly("POLYGON((0.0 0.0,1.0 0.0,1.0 1.0,0.0 0.0))");
        assertThat(sql("S_INTERSECTS(geometry, BBOX(-98, 30, -97, 31))", new SqlParams()))
                .contains("l.geom && ST_MakeEnvelope(").doesNotContain(" OR ");
        assertThat(sql("S_INTERSECTS(geometry, BBOX(170, -10, -170, 10))", new SqlParams())).contains(" OR ");
        assertThatThrownBy(() -> sql("S_INTERSECTS(mag, POINT(0 0))", new SqlParams())).isInstanceOf(Cql2Exception.class);
    }

    @Test
    @DisplayName("an unknown property is FILTER_INVALID with its position; object keys allow only IS NULL")
    void unknown() {
        assertThatThrownBy(() -> sql("mag > 1 AND nope = 2", new SqlParams())).isInstanceOf(Cql2Exception.class)
                .satisfies(e -> assertThat(((Cql2Exception) e).position()).isEqualTo("12"))
                .hasMessageContaining("nope");
        assertThatThrownBy(() -> sql("props = 1", new SqlParams())).isInstanceOf(Cql2Exception.class);
    }

    @Test
    @DisplayName("no filter text reaches the SQL: values and property names are parameters")
    void injection() {
        SqlParams p = new SqlParams();
        String sql = Cql2SqlTranslator.translate(Cql2TextParser.parse("\"x\" = 'a''); DROP TABLE alert; --'"),
                QUERYABLES, COLUMNS, p);
        assertThat(sql).doesNotContain("DROP").doesNotContain("alert");
    }

    @Test
    @DisplayName("where CQL2 and the 017 truth table overlap, the translation uses the same operator SQL")
    void truthTableOverlap() throws Exception {
        JsonMapper json = JsonMapper.builder().build();
        JsonNode root;
        try (InputStream in = getClass().getResourceAsStream("/trigger/operator-truth-table.json")) {
            root = json.readTree(in);
        }
        Set<String> overlapping = Set.of(">", ">=", "<", "<=", "==", "!=");
        int checked = 0;
        for (JsonNode row : root.get("rows")) {
            String op = row.get("op").asString();
            String path = row.get("path").asString();
            JsonNode value = row.get("value");
            if (!overlapping.contains(op) || path.contains(".") || !(value.isNumber() || value.isString()
                    || value.isBoolean())) {
                continue;
            }
            if ((value.isString() || value.isBoolean()) && !op.equals("==") && !op.equals("!=")) {
                continue;
            }
            String cqlOp = switch (op) {
                case "==" -> "=";
                case "!=" -> "<>";
                default -> op;
            };
            String literal = value.isString() ? "'" + value.asString().replace("'", "''") + "'" : value.toString();
            Cql2Queryables q = new Cql2Queryables(Map.of(path, FieldType.STRING));
            SqlParams p = new SqlParams();
            String sql = Cql2SqlTranslator.translate(Cql2TextParser.parse("\"" + path + "\" " + cqlOp + " " + literal), q,
                    COLUMNS, p);
            String expectedType = value.isNumber() ? "number" : value.isString() ? "string" : "boolean";
            assertThat(sql).as(row.toString()).contains("= '" + expectedType + "'")
                    .contains(" " + (op.equals("==") ? "=" : op.equals("!=") ? "<>" : op) + " CAST(");
            checked++;
        }
        assertThat(checked).isGreaterThan(10);
    }
}
