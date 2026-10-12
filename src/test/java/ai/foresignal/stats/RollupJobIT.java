package ai.foresignal.stats;

import static ai.foresignal.testsupport.stats.StatsTestSources.OTHER_KIND;
import static ai.foresignal.testsupport.stats.StatsTestSources.POINTS;
import static ai.foresignal.testsupport.stats.StatsTestSources.POINT_KIND;
import static ai.foresignal.testsupport.stats.StatsTestSources.SECRET;
import static ai.foresignal.testsupport.stats.StatsTestSources.SECRET_KIND;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.ingest.WriterState;
import ai.foresignal.ingest.WriterStatus;
import ai.foresignal.retention.RetentionRunResult;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.stats.StatsItSupport;
import ai.foresignal.testsupport.stats.StatsTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

/**
 * The activity rollup job (prompt 038) against a real PostGIS, records seeded only through
 * {@code TestRecords}: closed-hour detection, exact measures across hours and kinds, HEARTBEAT left
 * out, daily distinct entities computed from raw rows, the durable cursor across a restart, the
 * backfill bound, private sources and the retention clamp.
 */
@IntegrationTest
@Import(StatsTestSources.class)
class RollupJobIT extends StatsItSupport {

    private static WriterStatus writer(WriterState state, Instant lastIngestedAt) {
        return new WriterStatus(state, List.of(), 0, lastIngestedAt, 0);
    }

    @Test
    @DisplayName("an hour closes 2 min after its end once the writer has passed it, or after 10 quiet minutes")
    void closedHours() {
        Instant end = H1;
        assertThat(rollup.closed(T0, end.plusSeconds(60), writer(WriterState.RUNNING, end.plusSeconds(30))))
                .as("within the 2 min delay").isFalse();
        assertThat(rollup.closed(T0, end.plusSeconds(121), writer(WriterState.RUNNING, end.plusSeconds(30))))
                .as("writer passed the end").isTrue();
        assertThat(rollup.closed(T0, end.plusSeconds(180), writer(WriterState.RUNNING, end.minusSeconds(5))))
                .as("writer still inside the hour, not yet quiet").isFalse();
        assertThat(rollup.closed(T0, end.plus(Duration.ofMinutes(RollupJob.QUIET_MINUTES)),
                writer(WriterState.RUNNING, end.minusSeconds(5)))).as("quiet for 10 minutes").isTrue();
        assertThat(rollup.closed(T0, end.plus(Duration.ofHours(5)), writer(WriterState.CLOCK_BEHIND, end.plusSeconds(1))))
                .as("never while the clock guard holds batches").isFalse();
        assertThat(rollup.closed(T0, end.plusSeconds(121), writer(WriterState.RUNNING, null))).as("no ingest yet")
                .isTrue();
        assertThat(rollup.closed(T0, end.plusSeconds(121), null)).as("no writer").isTrue();
    }

    @Test
    @DisplayName("exact counts per measure for versions across two hours and two kinds; totals rows and cell rows agree")
    void exactCounts() {
        seed(POINTS, T0.plusSeconds(600), point("p1", POINT_KIND, 10, 50, T0.plusSeconds(600), 1),
                point("p2", OTHER_KIND, 11, 51, T0.plusSeconds(600), 1));
        seed(POINTS, T0.plusSeconds(1200), point("p1", POINT_KIND, 10, 50, T0.plusSeconds(1200), 2));
        seed(POINTS, H1.plusSeconds(600), NormalizedRecord.tombstone("p1", POINT_KIND, H1.plusSeconds(600)),
                point("p3", POINT_KIND, 12, 52, H1.plusSeconds(600), 1));

        int hours = rollUpAt(T0.plus(Duration.ofMinutes(135)));

        assertThat(hours).as("hours T0 and T0+1h are closed; T0+2h is not").isEqualTo(2);
        Map<String, Object> first = row(0, T0, POINTS, POINT_KIND, H3Cells.ALL_CELLS);
        assertThat(first).containsEntry("versions", 2L).containsEntry("entities", 1L).containsEntry("firsts", 1L)
                .containsEntry("tombstones", 0L).containsEntry("changes", 1L).containsEntry("edges", 0L);
        assertThat(row(0, T0, POINTS, OTHER_KIND, H3Cells.ALL_CELLS)).containsEntry("versions", 1L)
                .containsEntry("firsts", 1L);
        Map<String, Object> second = row(0, H1, POINTS, POINT_KIND, H3Cells.ALL_CELLS);
        assertThat(second).containsEntry("versions", 2L).containsEntry("entities", 2L).containsEntry("firsts", 1L)
                .containsEntry("tombstones", 1L);

        long p1Cell = cells.cell(10, 50, StatsProperties.defaults().cellRes());
        Map<String, Object> cellRow = row(0, T0, POINTS, POINT_KIND, p1Cell);
        assertThat(cellRow).containsEntry("versions", 2L).containsEntry("entities", 1L);
        assertThat((Double) cellRow.get("cell_lon")).isCloseTo(10, org.assertj.core.data.Offset.offset(1.0));
        assertThat(sum("versions", 0, POINTS, true)).as("cell rows sum to the totals rows")
                .isEqualTo(sum("versions", 0, POINTS, false)).isEqualTo(5);

        RollupCursor cursor = rollup.cursor();
        assertThat(cursor.lastHour()).isEqualTo(H1);
        assertThat(cursor.since()).isEqualTo(T0);
        assertThat(cursor.lastDay()).isEqualTo(T0.minus(Duration.ofDays(1)));
        assertThat(cursor.seq()).as("the open day holds the clamp at its start").isZero();
        assertThat(jdbc.queryForObject("SELECT value::text FROM app_setting WHERE key = ?", String.class,
                SettingKeys.STATS_ROLLUP_CURSOR)).contains("lastHour").contains("lastDay").contains("seq");
    }

    @Test
    @DisplayName("HEARTBEAT versions are not counted")
    void heartbeatExcluded() {
        Instant t = T0.plusSeconds(300);
        seed(TestIngestSources.TTL, t, NormalizedRecord.builder("h1", TestIngestSources.TTL_KIND).observedAt(t)
                .put("v", 1).build());
        Instant again = t.plusSeconds(580);
        seed(TestIngestSources.TTL, again, NormalizedRecord.builder("h1", TestIngestSources.TTL_KIND)
                .observedAt(again).put("v", 1).build());
        assertThat(jdbc.queryForList("SELECT persist_reason FROM record_version WHERE source_id = ? ORDER BY seq",
                Short.class, TestIngestSources.TTL)).as("the keyframe came due with equal content")
                .containsExactly((short) 1, (short) 8);

        rollUpAt(T0.plus(Duration.ofMinutes(75)));

        assertThat(row(0, T0, TestIngestSources.TTL, TestIngestSources.TTL_KIND, H3Cells.ALL_CELLS))
                .containsEntry("versions", 1L).containsEntry("entities", 1L);
    }

    @Test
    @DisplayName("a day's distinct entities come from raw rows and differ from the sum of its hours")
    void dailyDistinctEntities() {
        seed(POINTS, T0.plusSeconds(600), point("e1", POINT_KIND, 10, 50, T0.plusSeconds(600), 1));
        seed(POINTS, H1.plusSeconds(600), point("e1", POINT_KIND, 10, 50, H1.plusSeconds(600), 2));

        rollUpAt(T0.plus(Duration.ofHours(24)).plus(Duration.ofMinutes(15)));

        assertThat(sum("entities", 0, POINTS, false)).as("one entity in each of two hours").isEqualTo(2);
        assertThat(row(1, T0, POINTS, POINT_KIND, H3Cells.ALL_CELLS)).containsEntry("entities", 1L)
                .containsEntry("versions", 2L);
        assertThat(rollup.cursor().lastDay()).isEqualTo(T0);
        assertThat(rollup.cursor().seq()).as("the day is rolled, so its seqs are released").isEqualTo(2);
    }

    @Test
    @DisplayName("the cursor survives a restart: nothing is rolled up twice, and a rebuild is idempotent")
    void cursorSurvivesRestart() {
        seed(POINTS, T0.plusSeconds(600), point("r1", POINT_KIND, 10, 50, T0.plusSeconds(600), 1));
        rollUpAt(T0.plus(Duration.ofMinutes(75)));
        long before = sum("versions", 0, POINTS, false);
        RollupCursor cursor = rollup.cursor();

        rollup.resetState();
        assertThat(rollup.cursor()).as("reloaded from app_setting").isEqualTo(cursor);
        RollupJob.RunResult again = rollup.runOnce();

        assertThat(again.hours()).isZero();
        assertThat(sum("versions", 0, POINTS, false)).isEqualTo(before).isEqualTo(1);
        RollupJob.RebuildPlan plan = rollup.rebuildNow(T0, H1);
        assertThat(plan.hours()).containsExactly(T0);
        assertThat(sum("versions", 0, POINTS, false)).as("recomputed, not added").isEqualTo(1);
    }

    @Test
    @DisplayName("the first start backfills at most foresignal.stats.backfill (30 d), 6 hours per run")
    void backfillBound() {
        seed(POINTS, T0.plusSeconds(600), point("old", POINT_KIND, 10, 50, T0.plusSeconds(600), 1));
        Instant now = T0.plus(Duration.ofDays(40));
        clock.setInstant(now);

        RollupJob.RunResult result = rollup.runOnce();

        Instant bound = now.minus(Duration.ofDays(30)).truncatedTo(java.time.temporal.ChronoUnit.HOURS);
        assertThat(result.cursor().since()).isEqualTo(bound);
        assertThat(result.hours()).isEqualTo(6);
        assertThat(result.cursor().lastHour()).isEqualTo(bound.plus(Duration.ofHours(5)));
        assertThat(rows(POINTS)).as("the old version is outside the backfill").isZero();
    }

    @Test
    @DisplayName("private sources are skipped unless stats.includePrivate[sourceId] is set; rows hold no entity ids")
    void privateSourceSkipped() {
        seed(SECRET, T0.plusSeconds(600), point("s1", SECRET_KIND, 10, 50, T0.plusSeconds(600), 1));
        rollUpAt(T0.plus(Duration.ofMinutes(75)));
        assertThat(rows(SECRET)).isZero();

        statsSettings.update(SECRET, null, true);
        rollup.rebuildNow(T0, H1);

        assertThat(row(0, T0, SECRET, SECRET_KIND, H3Cells.ALL_CELLS)).containsEntry("versions", 1L);
        assertThat(jdbc.queryForList("SELECT column_name FROM information_schema.columns WHERE table_name ="
                + " 'activity_rollup'", String.class)).doesNotContain("external_id");
    }

    @Test
    @DisplayName("statsEnabled=false leaves a source out")
    void disabledSource() {
        statsSettings.update(POINTS, false, null);
        seed(POINTS, T0.plusSeconds(600), point("d1", POINT_KIND, 10, 50, T0.plusSeconds(600), 1));
        rollUpAt(T0.plus(Duration.ofMinutes(75)));
        assertThat(rows(POINTS)).isZero();
        assertThat(statsSettings.get().enabled(POINTS)).isFalse();
    }

    @Test
    @DisplayName("the retention clamp holds raw rows the rollup has not finished with")
    void retentionClamp() {
        seed(POINTS, T0.plusSeconds(600), point("c1", POINT_KIND, 10, 50, T0.plusSeconds(600), 1));
        seed(POINTS, H1, point("c2", POINT_KIND, 10, 50, H1, 1));
        rollUpAt(T0.plus(Duration.ofMinutes(135)));
        assertThat(rollup.cursor().seq()).as("the open day holds every seq").isZero();

        RetentionRunResult result = runAt(T0.plus(Duration.ofDays(8)));

        assertThat(versions(POINTS, "c1")).as("held back by the rollup cursor").isEqualTo(1);
        assertThat(result.consumerClamp().active()).isTrue();
        assertThat(result.consumerClamp().consumer()).isEqualTo(SettingKeys.STATS_ROLLUP_CURSOR);
        assertThat(job.consumers()).extracting(c -> c.name()).contains(SettingKeys.STATS_ROLLUP_CURSOR);
    }
}
