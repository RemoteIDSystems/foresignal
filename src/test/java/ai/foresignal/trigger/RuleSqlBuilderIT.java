package ai.foresignal.trigger;

import static ai.foresignal.testsupport.trigger.TriggerTestSources.SAMPLED;
import static ai.foresignal.testsupport.trigger.TriggerTestSources.SAMPLED_KIND;
import static ai.foresignal.trigger.TestRules.rule;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.TriggerMode;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.trigger.TriggerTestSources;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * {@link RuleSqlBuilder} against PostgreSQL: the shared truth table, and whole statements over
 * versions seeded through {@link TestRecords} (prompt 017).
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(TriggerTestSources.class)
class RuleSqlBuilderIT {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");

    @Autowired NamedParameterJdbcTemplate jdbc;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
    }

    static Stream<TruthTable.Row> rows() {
        return TruthTable.rows().stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rows")
    @DisplayName("every truth-table row evaluates in SQL exactly as the fixture says")
    void truthTable(TruthTable.Row row) {
        TestRules builder = rule();
        builder.conditions.add(row.condition());
        RuleSpec spec = builder.build();
        RuleSqlBuilder.Params params = new RuleSqlBuilder.Params();
        String predicate = RuleSqlBuilder.predicate(spec, "a", RuleSqlBuilder.FenceSide.MAIN, params);
        params.put("payload", row.payloadJson());
        Boolean holds = jdbc.queryForObject("SELECT " + predicate
                + " FROM (SELECT CAST(:payload AS jsonb) AS payload, CAST(NULL AS geometry) AS geom) a",
                params.source, Boolean.class);
        assertThat(holds).isEqualTo(row.expected());
    }

    @Test
    @DisplayName("a geofence matches inside and on the boundary, not outside or without geometry")
    void geofence() {
        RuleSpec spec = rule().fence(0, 0, 10, 10).build();
        assertThat(inside(spec, "POINT(5 5)")).isTrue();
        assertThat(inside(spec, "POINT(10 5)")).isTrue();
        assertThat(inside(spec, "POINT(11 5)")).isFalse();
        assertThat(inside(spec, null)).isFalse();
    }

    private Boolean inside(RuleSpec spec, String wkt) {
        RuleSqlBuilder.Params params = new RuleSqlBuilder.Params();
        String predicate = RuleSqlBuilder.predicate(spec, "a", RuleSqlBuilder.FenceSide.MAIN, params);
        params.put("wkt", wkt);
        return jdbc.queryForObject("SELECT " + predicate
                + " FROM (SELECT CAST('{}' AS jsonb) AS payload, ST_GeomFromText(CAST(:wkt AS text), 4326) AS geom) a",
                params.source, Boolean.class);
    }

    @Test
    @DisplayName("match, enter and exit statements over seeded versions; a missing predecessor fires nothing")
    void statements() {
        ingest("e1", 5, T);
        ingest("e1", 7, T.plusSeconds(60));
        ingest("e1", 8, T.plusSeconds(120));
        ingest("e1", 4, T.plusSeconds(180));
        long to = jdbc.getJdbcTemplate().queryForObject("SELECT max(seq) FROM record_version", Long.class);

        assertThat(seqs(rule().sources(TestIngestSources.STANDARD).when("mag", ">=", 6).build(), 0, to))
                .hasSize(2);
        List<MatchRow> enter = rows(rule().sources(TestIngestSources.STANDARD).when("mag", ">=", 6)
                .mode(TriggerMode.ENTER).build(), 0, to);
        assertThat(enter).singleElement().satisfies(r -> {
            assertThat(r.edge()).isEqualTo("enter");
            assertThat(r.version()).isEqualTo(2);
        });
        assertThat(rows(rule().sources(TestIngestSources.STANDARD).when("mag", ">=", 6).mode(TriggerMode.EXIT).build(), 0, to))
                .singleElement().satisfies(r -> assertThat(r.version()).isEqualTo(4));
        // Evaluating only the window of version 3 sees its predecessor, which matched, so no enter.
        long v3 = jdbc.getJdbcTemplate().queryForObject(
                "SELECT seq FROM record_version WHERE external_id = 'e1' AND version = 3", Long.class);
        assertThat(rows(rule().sources(TestIngestSources.STANDARD).when("mag", ">=", 6).mode(TriggerMode.ENTER).build(),
                v3 - 1, v3)).isEmpty();
    }

    @Test
    @DisplayName("HEARTBEAT versions are never selected")
    void heartbeats() {
        records.ingest(SAMPLED, NormalizedRecord.builder("h1", SAMPLED_KIND).observedAt(T).point(1, 1).put("name", "a").build());
        clock.setInstant(T.plusSeconds(3700));
        records.ingest(SAMPLED, NormalizedRecord.builder("h1", SAMPLED_KIND).observedAt(T.plusSeconds(3700))
                .point(1, 1).put("name", "a").build());
        Integer heartbeats = jdbc.getJdbcTemplate().queryForObject(
                "SELECT count(*) FROM record_version WHERE persist_reason = 8", Integer.class);
        assertThat(heartbeats).isEqualTo(1);
        long to = jdbc.getJdbcTemplate().queryForObject("SELECT max(seq) FROM record_version", Long.class);
        assertThat(rows(rule().sources(SAMPLED).build(), 0, to)).hasSize(1);
    }

    private void ingest(String id, double mag, Instant at) {
        clock.setInstant(at);
        records.ingest(TestIngestSources.STANDARD,
                NormalizedRecord.builder(id, TestIngestSources.STANDARD_KIND).observedAt(at).put("mag", mag).build());
    }

    private List<Long> seqs(RuleSpec rule, long from, long to) {
        return rows(rule, from, to).stream().map(MatchRow::seq).toList();
    }

    private List<MatchRow> rows(RuleSpec rule, long from, long to) {
        RuleSqlBuilder.RuleSql sql = RuleSqlBuilder.matches(rule, from, to, clock.instant(), false);
        return jdbc.query(sql.sql(), sql.params(), MatchRow.MAPPER);
    }
}
