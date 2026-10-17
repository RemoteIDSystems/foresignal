package ai.foresignal.trigger;

import static ai.foresignal.trigger.TestRules.box;
import static ai.foresignal.trigger.TestRules.rule;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.store.TriggerMode;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** {@link RuleSqlBuilder}: SQL text and parameters (prompt 017). */
class RuleSqlBuilderTest {

    static final Instant NOW = Instant.parse("2026-05-01T12:00:00Z");

    static RuleSqlBuilder.RuleSql sql(RuleSpec rule) {
        return RuleSqlBuilder.matches(rule, 100, 200, NOW, true);
    }

    @Nested
    @DisplayName("the frame of every statement")
    class Frame {

        @Test
        @DisplayName("seq range, HEARTBEAT excluded, ordered by seq")
        void frame() {
            RuleSqlBuilder.RuleSql s = sql(rule().when("mag", ">=", 6).build());
            assertThat(s.sql()).contains("v.seq > :fromSeq", "v.seq <= :toSeq", "v.persist_reason <> 8")
                    .endsWith(" ORDER BY v.seq");
            assertThat(s.values()).containsEntry("fromSeq", 100L).containsEntry("toSeq", 200L);
        }

        @Test
        @DisplayName("sources and kinds are bound arrays")
        void filters() {
            RuleSqlBuilder.RuleSql s = sql(rule().sources("b", "a").kinds("quake").build());
            assertThat(s.sql()).contains("v.source_id = ANY(CAST(:sources AS text[]))", "v.kind = ANY(CAST(:kinds AS text[]))");
            assertThat(s.values()).containsEntry("sources", "{\"a\",\"b\"}").containsEntry("kinds", "{\"quake\"}");
        }

        @Test
        @DisplayName("onlyNew adds version = 1")
        void onlyNew() {
            TestRules builder = rule().when("mag", ">", 1);
            builder.onlyNew = true;
            assertThat(sql(builder.build()).sql()).contains("v.version = 1");
            assertThat(sql(rule().when("mag", ">", 1).build()).sql()).doesNotContain("v.version = 1");
        }

        @Test
        @DisplayName("forecastHorizon bounds valid_from by the evaluation time")
        void horizon() {
            TestRules builder = rule().when("mag", ">", 1);
            builder.horizon = Duration.ofHours(6);
            RuleSqlBuilder.RuleSql s = sql(builder.build());
            assertThat(s.sql()).contains("(v.valid_from IS NULL OR v.valid_from <= CAST(:evalTime AS timestamptz)"
                    + " + make_interval(secs => CAST(:horizon AS double precision)))");
            assertThat(s.values()).containsEntry("horizon", 21_600.0).containsKey("evalTime");
        }
    }

    @Nested
    @DisplayName("operators")
    class Ops {

        @Test
        @DisplayName("numeric comparisons guard the JSON type and bind path and value")
        void numeric() {
            RuleSqlBuilder.RuleSql s = sql(rule().when("props.mag", ">=", 6).build());
            assertThat(s.sql()).contains("(CASE WHEN jsonb_typeof(v.payload #> CAST(:c0p AS text[])) = 'number'"
                    + " THEN (v.payload #>> CAST(:c0p AS text[]))::numeric END) >= CAST(:c0v AS numeric)");
            assertThat(s.values()).containsEntry("c0p", "{\"props\",\"mag\"}").containsEntry("c0v", new BigDecimal("6"));
        }

        @Test
        @DisplayName("== and != per value type")
        void equality() {
            String sql = sql(rule().when("a", "==", "x").when("b", "!=", true).when("c", "==", 2.5).build()).sql();
            assertThat(sql).contains("= 'string' THEN v.payload #>> CAST(:c0p AS text[]) END) = CAST(:c0v AS text)");
            assertThat(sql).contains("= 'boolean' THEN (v.payload #>> CAST(:c1p AS text[]))::boolean END) <> CAST(:c1v AS boolean)");
            assertThat(sql).contains("::numeric END) = CAST(:c2v AS numeric)");
        }

        @Test
        @DisplayName("in and notIn bind typed arrays")
        void membership() {
            RuleSqlBuilder.RuleSql s = sql(rule().when("k", "in", java.util.List.of("a", "b\"c"))
                    .when("n", "notIn", java.util.List.of(1, 2.5)).build());
            assertThat(s.sql()).contains("= ANY(CAST(:c0v AS text[]))", "<> ALL(CAST(:c1v AS numeric[]))");
            assertThat(s.values()).containsEntry("c0v", "{\"a\",\"b\\\"c\"}").containsEntry("c1v", "{1,2.5}");
        }

        @Test
        @DisplayName("exists, startsWith and matches")
        void others() {
            RuleSqlBuilder.RuleSql s = sql(rule().when("e", "exists", true).when("cs", "startsWith", "N12")
                    .when("m", "matches", "^A[0-9]+").build());
            assertThat(s.sql()).contains("((v.payload #> CAST(:c0p AS text[]) IS NOT NULL AND jsonb_typeof(v.payload #> CAST(:c0p AS text[])) <> 'null') = CAST(:c0v AS boolean))");
            assertThat(s.sql()).contains("left((CASE WHEN jsonb_typeof(v.payload #> CAST(:c1p AS text[])) = 'string' THEN v.payload #>> CAST(:c1p AS text[]) END), CAST(:c1len AS integer)) = CAST(:c1v AS text)");
            assertThat(s.sql()).contains("END) ~ CAST(:c2v AS text)");
            assertThat(s.values()).containsEntry("c1len", 3).containsEntry("c2v", "^A[0-9]+");
        }

        @Test
        @DisplayName("no value or path is ever spliced into the SQL text")
        void noInterpolation() {
            RuleSqlBuilder.RuleSql s = sql(rule().when("weird_path", "==", "'; DROP TABLE alert; --")
                    .when("x", "matches", "evil'pattern").build());
            assertThat(s.sql()).doesNotContain("DROP TABLE", "evil", "weird_path");
        }

        @Test
        @DisplayName("the predicate is wrapped in COALESCE(..., false) and is plain true without conditions")
        void coalesce() {
            assertThat(sql(rule().when("a", ">", 1).build()).sql()).contains("COALESCE((");
            assertThat(sql(rule().sources("s").build()).sql()).contains("NOT v.tombstone AND true");
        }
    }

    @Nested
    @DisplayName("modes")
    class Modes {

        @Test
        @DisplayName("match needs no predecessor join")
        void match() {
            String s = sql(rule().when("a", ">", 1).build()).sql();
            assertThat(s).doesNotContain("JOIN record_version p").contains("'match' AS edge", "NOT v.tombstone AND COALESCE(");
        }

        @Test
        @DisplayName("enter joins the predecessor on seq only and follows Core-Data-Model §11")
        void enter() {
            TestRules builder = rule().when("a", ">", 1).mode(TriggerMode.ENTER);
            builder.reappear = true;
            RuleSqlBuilder.RuleSql s = sql(builder.build());
            assertThat(s.sql()).contains(" LEFT JOIN record_version p ON p.seq = v.prev_seq WHERE ")
                    .doesNotContain("p.source_id", "p.version")
                    .contains("(v.prev_seq IS NULL OR p.tombstone OR (CAST(:reappearAsEnter AS boolean) AND v.persist_reason = 2)"
                            + " OR (p.seq IS NOT NULL AND NOT COALESCE((")
                    .contains("'enter' AS edge");
            assertThat(s.values()).containsEntry("reappearAsEnter", true);
        }

        @Test
        @DisplayName("exit needs a retained, non-tombstone predecessor that matched")
        void exit() {
            String s = sql(rule().when("a", ">", 1).mode(TriggerMode.EXIT).build()).sql();
            assertThat(s).contains("p.seq IS NOT NULL AND NOT p.tombstone AND COALESCE((")
                    .contains("AND (v.tombstone OR NOT COALESCE((")
                    .contains("'exit' AS edge");
        }

        @Test
        @DisplayName("a geofence intersects a bound WKB fence on both aliases")
        void geofence() {
            RuleSqlBuilder.RuleSql s = sql(rule().fence(0, 0, 1, 1).mode(TriggerMode.ENTER).build());
            assertThat(s.sql()).contains("v.geom IS NOT NULL AND ST_Intersects(v.geom, ST_GeomFromWKB(CAST(:fence AS bytea), 4326))")
                    .contains("p.geom IS NOT NULL AND ST_Intersects(p.geom, ST_GeomFromWKB(CAST(:fence AS bytea), 4326))");
            assertThat(s.values().get("fence")).isInstanceOf(byte[].class);
        }

        @Test
        @DisplayName("a hysteresis rule selects both edges, enter on the shrunk and exit on the grown fence")
        void hysteresis() {
            RuleSqlBuilder.RuleSql s = sql(rule().fence(0, 0, 10, 10).mode(TriggerMode.ENTER)
                    .band(box(1, 1, 9, 9), box(-1, -1, 11, 11), 500).build());
            assertThat(s.sql()).contains("CASE WHEN NOT v.tombstone AND COALESCE(", "THEN 'enter' WHEN p.seq IS NOT NULL", "THEN 'exit' END AS edge")
                    .contains(":fenceInner", ":fenceOuter").doesNotContain("CAST(:fence AS bytea)");
            assertThat(s.values()).containsKeys("fenceInner", "fenceOuter");
        }
    }

    @Nested
    @DisplayName("cooldown")
    class Cooldown {

        @Test
        @DisplayName("record scope seeds per entity on observed_at, ignoring suppressed alerts")
        void record() {
            RuleSqlBuilder.RuleSql s = sql(rule().when("a", ">", 1).cooldown(Duration.ofMinutes(15), CooldownScope.RECORD).build());
            assertThat(s.sql()).contains("NOT EXISTS (SELECT 1 FROM alert a WHERE a.rule_id = :rule"
                    + " AND a.source_id = v.source_id AND a.external_id = v.external_id"
                    + " AND NOT a.suppressed AND NOT a.exercise AND a.observed_at > v.observed_at - make_interval(secs => CAST(:cooldown AS double precision)))");
            assertThat(s.values()).containsEntry("cooldown", 900.0).containsKey("rule");
        }

        @Test
        @DisplayName("rule scope drops the entity terms")
        void rule_() {
            String s = sql(rule().when("a", ">", 1).cooldown(Duration.ofMinutes(15), CooldownScope.RULE).build()).sql();
            assertThat(s).contains("WHERE a.rule_id = :rule AND NOT a.suppressed").doesNotContain("a.external_id = v.external_id");
        }

        @Test
        @DisplayName("no cooldown, or a backtest, adds no seed")
        void none() {
            assertThat(sql(rule().when("a", ">", 1).build()).sql()).doesNotContain("FROM alert");
            RuleSpec cooled = rule().when("a", ">", 1).cooldown(Duration.ofMinutes(1), CooldownScope.RECORD).build();
            assertThat(RuleSqlBuilder.matches(cooled, 0, 1, NOW, false).sql()).doesNotContain("FROM alert");
        }
    }

    @Nested
    @DisplayName("auto-resolve")
    class Inverse {

        @Test
        @DisplayName("the exit predicate; tombstones only when only onTombstone is set")
        void inverse() {
            TestRules builder = rule().when("a", ">", 1).mode(TriggerMode.ENTER);
            builder.autoResolve = AutoResolve.ALL;
            String both = RuleSqlBuilder.inverse(builder.build(), 0, 10).sql();
            assertThat(both).contains("JOIN record_version p ON p.seq = v.prev_seq", "NOT p.tombstone AND COALESCE((",
                    "AND (v.tombstone OR NOT COALESCE((").doesNotContain("AND v.tombstone ORDER", "NOT v.tombstone ORDER");
            builder.autoResolve = new AutoResolve(false, true, true, null);
            assertThat(RuleSqlBuilder.inverse(builder.build(), 0, 10).sql()).contains("AND v.tombstone ORDER BY v.seq");
            builder.autoResolve = new AutoResolve(true, false, true, null);
            assertThat(RuleSqlBuilder.inverse(builder.build(), 0, 10).sql()).contains("AND NOT v.tombstone ORDER BY v.seq");
        }

        @Test
        @DisplayName("a hysteresis rule resolves against the grown fence")
        void band() {
            TestRules builder = rule().fence(0, 0, 10, 10).mode(TriggerMode.ENTER)
                    .band(box(1, 1, 9, 9), box(-1, -1, 11, 11), 500);
            builder.autoResolve = AutoResolve.ALL;
            Map<String, Object> values = RuleSqlBuilder.inverse(builder.build(), 0, 10).values();
            assertThat(values).containsKey("fenceOuter").doesNotContainKeys("fence", "fenceInner");
        }
    }

    @Test
    @DisplayName("040: a geofenceRef rule renders the reference union as a subquery for v and p, never a bound polygon")
    void geofenceRef() {
        RuleSpec base = rule().mode(TriggerMode.ENTER).build();
        GeofenceRef ref = new GeofenceRef(java.util.List.of("US"), java.util.List.of("US-TX"), java.util.List.of());
        RuleSpec spec = new RuleSpec(base.id(), base.name(), true, base.sources(), base.kinds(), base.tags(),
                base.conditions(), box(-110, 25, -90, 40), null, null, 0, TriggerMode.ENTER, false, false,
                base.cooldown(), base.cooldownScope(), base.severity(), null, null, false, null, null, null, null,
                null, null, false, null, null, false, ref);
        RuleSqlBuilder.RuleSql s = sql(spec);
        assertThat(s.sql()).contains("ST_Intersects(v.geom, (SELECT ST_Multi(ST_CollectionExtract(ST_Union(u.g), 3))"
                + " FROM (SELECT geom AS g FROM ref_country WHERE iso2 = ANY(CAST(:fenceRef0 AS text[])) UNION ALL"
                + " SELECT geom AS g FROM ref_admin1 WHERE code = ANY(CAST(:fenceRef1 AS text[]))) u))")
                .contains("ST_Intersects(p.geom, (SELECT")
                .doesNotContain(":fence AS bytea");
        assertThat(s.values()).containsEntry("fenceRef0", "{\"US\"}").containsEntry("fenceRef1", "{\"US-TX\"}")
                .doesNotContainKey("fence");
    }
}
