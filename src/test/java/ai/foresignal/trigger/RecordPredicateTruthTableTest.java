package ai.foresignal.trigger;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.place.PlaceTestSources;
import ai.foresignal.watch.WatchlistService;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link RecordPredicate}, the compiled form rules and saved views share (prompt 036): on every row of
 * {@code trigger/operator-truth-table.json}, including the {@code inWatchlist}/{@code notInWatchlist}
 * rows, the SQL rendering and the Java evaluation agree with the fixture and with each other: they
 * hold on the same rows, and SQL yields NULL exactly where Java reports
 * {@link Evaluation#AMBIGUOUS}. Identity conditions (no path) switch on the source the same way in
 * both.
 */
@IntegrationTest
@Import(PlaceTestSources.class)
class RecordPredicateTruthTableTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final String SOURCE = "test-air-a";

    @Autowired NamedParameterJdbcTemplate jdbc;
    @Autowired OperatorRegistry operators;
    @Autowired WatchlistService watchlists;

    final Map<String, UUID> lists = new HashMap<>();

    /**
     * One row of either section.
     *
     * @param path the condition path
     * @param op the operator
     * @param value the raw value (a watchlist key for the watchlist rows)
     * @param payloadJson the payload as JSON text
     * @param payload the payload as a map
     * @param expected whether the condition holds
     * @param ambiguous whether it is ambiguous
     * @param watchlist true for a watchlist row
     */
    record Row(String path, String op, Object value, String payloadJson, Map<String, Object> payload, boolean expected,
            boolean ambiguous, boolean watchlist) {

        @Override
        public String toString() {
            return path + " " + op + " " + value + " on " + payloadJson;
        }
    }

    static JsonNode table() {
        try (InputStream in = RecordPredicateTruthTableTest.class.getResourceAsStream(TruthTable.RESOURCE)) {
            return JSON.readTree(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static Stream<Row> rows() {
        JsonNode root = table();
        List<Row> out = new ArrayList<>();
        for (String section : List.of("rows", "watchlistRows")) {
            for (JsonNode node : root.get(section)) {
                Map<String, Object> payload = JSON.readValue(node.get("payload").toString(), new TypeReference<>() {});
                out.add(new Row(node.get("path").asString(), node.get("op").asString(),
                        JSON.treeToValue(node.get("value"), Object.class), node.get("payload").toString(), payload,
                        node.get("expected").asBoolean(), node.get("ambiguous").asBoolean(),
                        "watchlistRows".equals(section)));
            }
        }
        return out.stream();
    }

    @BeforeEach
    void createLists() {
        for (JsonNode list : table().get("watchlists")) {
            UUID id = watchlists.create(JSON.readTree("{\"name\":\"" + list.get("key").asString() + "\",\"identity\":\""
                    + list.get("identity").asString() + "\",\"normalize\":\"" + list.get("normalize").asString() + "\"}"))
                    .getId();
            StringBuilder items = new StringBuilder("{\"items\":[");
            for (JsonNode value : list.get("values")) {
                items.append(items.length() > 10 ? "," : "").append("{\"value\":").append(value).append('}');
            }
            watchlists.add(id, JSON.readTree(items.append("]}").toString()));
            lists.put(list.get("key").asString(), id);
        }
    }

    RuleCondition condition(Row row, String path, Map<String, String> identityPaths) {
        Operator op = operators.find(row.op()).orElseThrow();
        Object raw = row.watchlist() ? lists.get((String) row.value()).toString() : row.value();
        Object value = op.normalize(JsonTrees.toJava(JSON.valueToTree(raw)));
        return new RuleCondition(path, op, value, identityPaths);
    }

    /** {@code [holds, sqlNull]} of {@code condition} in SQL over the payload of a record of {@code source}. */
    boolean[] sql(RuleCondition condition, String payloadJson, String source) {
        MapSqlParameterSource params = new MapSqlParameterSource("payload", payloadJson).addValue("src", source);
        RecordPredicate.Binder binder = (key, v, type) -> {
            if (!params.hasValue(key)) {
                params.addValue(key, v);
            }
            return "CAST(:" + key + " AS " + type + ")";
        };
        List<RuleCondition> conditions = List.of(condition);
        String raw = RecordPredicate.conditionParts(conditions, "CAST(:src AS text)", "CAST(:payload AS jsonb)", binder)
                .getFirst();
        String coalesced = RecordPredicate.conditionsSql(conditions, "CAST(:src AS text)", "CAST(:payload AS jsonb)",
                binder);
        Map<String, Object> result = jdbc.queryForMap("SELECT " + coalesced + " AS holds, (" + raw + ") IS NULL AS ambiguous",
                params);
        return new boolean[] {(Boolean) result.get("holds"), (Boolean) result.get("ambiguous")};
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rows")
    @DisplayName("SQL and Java agree with the fixture on every row, inWatchlist included")
    void agree(Row row) {
        RuleCondition condition = condition(row, row.path(), null);
        boolean[] sql = sql(condition, row.payloadJson(), SOURCE);
        Evaluation java = condition.evaluate(row.payload());
        assertThat(sql[0]).as("SQL holds").isEqualTo(row.expected());
        assertThat(sql[1]).as("SQL NULL").isEqualTo(row.ambiguous());
        assertThat(java.holds()).as("Java holds").isEqualTo(row.expected());
        assertThat(java == Evaluation.AMBIGUOUS).as("Java ambiguous").isEqualTo(row.ambiguous());
        assertThat(RecordPredicate.of(List.of(condition)).testConditions(SOURCE, row.payload())).isEqualTo(row.expected());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rows")
    @DisplayName("an identity condition (no path) reads the source's field in both, and never matches another source")
    void identity(Row row) {
        if (!row.watchlist() || row.path().contains(".")) {
            return;
        }
        RuleCondition condition = condition(row, null, Map.of(SOURCE, row.path()));
        boolean[] sql = sql(condition, row.payloadJson(), SOURCE);
        Evaluation java = condition.evaluate(SOURCE, row.payload());
        assertThat(sql[0]).isEqualTo(row.expected());
        assertThat(java.holds()).isEqualTo(row.expected());
        assertThat(sql[1]).isEqualTo(java == Evaluation.AMBIGUOUS);

        boolean[] other = sql(condition, row.payloadJson(), "test-air-none");
        Evaluation otherJava = condition.evaluate("test-air-none", row.payload());
        assertThat(other[0]).isFalse();
        assertThat(other[1]).as("a source without the identity is NULL in SQL").isTrue();
        assertThat(otherJava).isEqualTo(Evaluation.AMBIGUOUS);
    }

    @Test
    @DisplayName("the whole predicate: sources, kinds and area render and test the same way")
    void wholePredicate() {
        org.locationtech.jts.geom.GeometryFactory factory = new org.locationtech.jts.geom.GeometryFactory(
                new org.locationtech.jts.geom.PrecisionModel(), 4326);
        org.locationtech.jts.geom.Geometry area = factory.toGeometry(new org.locationtech.jts.geom.Envelope(0, 1, 0, 1));
        area.setSRID(4326);
        RecordPredicate predicate = new RecordPredicate(Set.of(SOURCE), Set.of("test-aircraft"), List.of(), area);
        Map<String, Object> cases = new LinkedHashMap<>();
        cases.put(SOURCE + "|test-aircraft|POINT(0.5 0.5)", true);
        cases.put(SOURCE + "|test-aircraft|POINT(2 2)", false);
        cases.put("test-air-b|test-aircraft|POINT(0.5 0.5)", false);
        cases.put(SOURCE + "|other|POINT(0.5 0.5)", false);
        for (Map.Entry<String, Object> c : cases.entrySet()) {
            String[] parts = c.getKey().split("\\|");
            MapSqlParameterSource params = new MapSqlParameterSource("src", parts[0]).addValue("kind", parts[1])
                    .addValue("wkt", parts[2]);
            int[] n = {0};
            String sql = predicate.sql("CAST(:src AS text)", "CAST(:kind AS text)", "ST_GeomFromText(:wkt, 4326)", null,
                    RecordPredicate.binder(v -> {
                        String name = "b" + n[0]++;
                        params.addValue(name, v);
                        return ":" + name;
                    }));
            Boolean holds = jdbc.queryForObject("SELECT " + sql, params, Boolean.class);
            org.locationtech.jts.geom.Geometry geometry;
            try {
                geometry = new org.locationtech.jts.io.WKTReader(factory).read(parts[2]);
            } catch (org.locationtech.jts.io.ParseException e) {
                throw new IllegalStateException(e);
            }
            assertThat(holds).as(c.getKey()).isEqualTo(c.getValue());
            assertThat(predicate.test(parts[0], parts[1], geometry, Map.of())).as(c.getKey()).isEqualTo(c.getValue());
        }
    }
}
