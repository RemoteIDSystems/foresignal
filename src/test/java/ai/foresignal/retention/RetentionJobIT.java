package ai.foresignal.retention;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK_KIND;
import static ai.foresignal.testsupport.retention.RetentionTestSources.KINDS;
import static ai.foresignal.testsupport.retention.RetentionTestSources.LONG_KIND;
import static ai.foresignal.testsupport.retention.RetentionTestSources.SHORT_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.collect.HealthTracker;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.history.LeafInfo;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.TestPartitions;
import ai.foresignal.testsupport.retention.RetentionItSupport;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

/**
 * The retention job end to end against the real writer and partitions (prompt 019, Core-Data-Model
 * §13): TRACK partition drops, STANDARD chunked deletes, per-kind windows, head purges and I7,
 * failure handling, the 304 rule, provenance pruning and the single-run guard.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RetentionTestSources.class)
class RetentionJobIT extends RetentionItSupport {

    static final Duration DAY = Duration.ofDays(1);

    @Autowired HealthTracker health;
    @Autowired RetentionResolver resolver;

    static NormalizedRecord aircraft(String id, Instant at, double lon) {
        return NormalizedRecord.builder(id, TRACK_KIND).observedAt(at).point(lon, 50.0)
                .put("callsign", id.toUpperCase()).put("alt", 30000).put("gs", 400).put("track", 90).build();
    }

    @Test
    @DisplayName("a TRACK leaf is dropped only once its upper bound is below cutoffSeq(now − trackWindow)")
    void trackLeafDroppedOnlyPastTheWindow() {
        ingestAt(TRACK, T0, aircraft("a", T0, 1.0));
        jumpSeq(20_000_000L);
        Instant later = T0.plus(Duration.ofHours(1));
        ingestAt(TRACK, later, aircraft("c", later, 2.0));

        RetentionRunResult early = runAt(T0.plus(Duration.ofDays(2)));
        assertThat(early.partitionsDropped()).isZero();
        assertThat(leafNames()).contains("rv_t_0");
        assertThat(versions(TRACK, "a")).isEqualTo(1);

        RetentionRunResult due = runAt(later.plus(Duration.ofDays(3)).plus(Duration.ofMinutes(5)));

        assertThat(due.partitionsDropped()).isGreaterThanOrEqualTo(1);
        assertThat(leafNames()).doesNotContain("rv_t_0").contains("rv_t_20000000");
        assertThat(versions(TRACK, "a")).isZero();
        assertThat(versions(TRACK, "c")).isEqualTo(1);
        assertThat(state.floorSeq(TRACK)).isEqualTo(20_000_000L);
        // Every version of "a" is gone and its live row is stale: the writer purged its head.
        assertThat(head(TRACK, "a")).isFalse();
        assertThat(live(TRACK, "a")).isFalse();
        assertThat(head(TRACK, "c")).isTrue();
        assertThat(due.versionsDeleted()).isZero();
    }

    @Test
    @DisplayName("STANDARD deletes run in chunks of 10,000 and stop at the cutoff")
    void standardChunkedDeletes() {
        List<NormalizedRecord> batch = new ArrayList<>();
        for (int i = 0; i < 10_050; i++) {
            batch.add(record("e" + i, STANDARD_KIND, T0, i));
            if (batch.size() == 2_000) {
                ingestAt(STANDARD, T0, batch.toArray(NormalizedRecord[]::new));
                batch.clear();
            }
        }
        ingestAt(STANDARD, T0, batch.toArray(NormalizedRecord[]::new));
        Instant marker = T0.plus(Duration.ofMinutes(1));
        ingestAt(STANDARD, marker, record("marker", STANDARD_KIND, marker, 1));

        RetentionRunResult result = runAt(T0.plus(Duration.ofDays(8)));

        assertThat(result.versionsDeleted()).isEqualTo(10_050);
        assertThat(result.perSource().get(STANDARD).versionsDeleted()).isEqualTo(10_050);
        assertThat(versions(STANDARD)).isEqualTo(1);
        assertThat(versions(STANDARD, "marker")).isEqualTo(1);
        assertThat(result.headsPurged()).isEqualTo(10_050);
        assertThat(head(STANDARD, "marker")).isTrue();
        assertThat(state.floorSeq(STANDARD)).isEqualTo(result.perSource().get(STANDARD).floorSeq());
    }

    @Test
    @DisplayName("per-kind windows: a 365d kind survives the source's 7d window")
    void perKindWindows() {
        ingestAt(KINDS, T0, record("s1", SHORT_KIND, T0, 1), record("l1", LONG_KIND, T0, 1));
        Instant marker = T0.plus(Duration.ofMinutes(1));
        ingestAt(KINDS, marker, record("s2", SHORT_KIND, marker, 1), record("l2", LONG_KIND, marker, 1));

        RetentionRunResult result = runAt(T0.plus(Duration.ofDays(8)));

        assertThat(versions(KINDS, "s1")).isZero();
        assertThat(versions(KINDS, "l1")).isEqualTo(1);
        assertThat(versions(KINDS, "s2")).isEqualTo(1);
        assertThat(head(KINDS, "s1")).isFalse();
        assertThat(head(KINDS, "l1")).isTrue();
        assertThat(result.perSource().get(KINDS).versionsDeleted()).isEqualTo(1);
        assertThat(state.storedFloor(KINDS, SHORT_KIND)).isGreaterThan(state.storedFloor(KINDS, LONG_KIND));
    }

    @Test
    @DisplayName("a head survives the deletion of its referenced version while its entity is still observed")
    void observedHeadSurvives() {
        ingestAt(STANDARD, T0, record("x", STANDARD_KIND, T0, 1));
        Instant marker = T0.plus(Duration.ofMinutes(1));
        ingestAt(STANDARD, marker, record("m", STANDARD_KIND, marker, 1));
        Instant touch = T0.plus(Duration.ofHours(36));
        ingestAt(STANDARD, touch, record("x", STANDARD_KIND, touch, 1));

        runAt(T0.plus(Duration.ofDays(7)).plus(Duration.ofMinutes(2)));

        assertThat(versions(STANDARD, "x")).isZero();
        assertThat(head(STANDARD, "x")).isTrue();
        assertThat(live(STANDARD, "x")).isTrue();
    }

    @Test
    @DisplayName("after a purge a reappearing entity starts again at version 1 (FIRST) with no older versions")
    void reappearanceRestartsAtVersionOne() {
        ingestAt(STANDARD, T0, record("x", STANDARD_KIND, T0, 1));
        Instant second = T0.plus(Duration.ofSeconds(30));
        ingestAt(STANDARD, second, record("x", STANDARD_KIND, second, 2));
        Instant marker = T0.plus(Duration.ofMinutes(1));
        ingestAt(STANDARD, marker, record("m", STANDARD_KIND, marker, 1));

        runAt(T0.plus(Duration.ofDays(8)));
        assertThat(head(STANDARD, "x")).isFalse();

        Instant back = T0.plus(Duration.ofDays(9));
        ingestAt(STANDARD, back, record("x", STANDARD_KIND, back, 3));

        assertThat(versions(STANDARD, "x")).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT version FROM record_version WHERE source_id = ? AND external_id = ?",
                Integer.class, STANDARD, "x")).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT persist_reason FROM record_version WHERE source_id = ? AND external_id = ?",
                Short.class, STANDARD, "x")).isEqualTo(PersistReason.FIRST.code());
    }

    @Test
    @DisplayName("a failed delete skips the source: no floor advance and no HeadPurge for it")
    void failedDeleteSkipsSource() {
        ingestAt(STANDARD, T0, record("x", STANDARD_KIND, T0, 1));
        ingestAt(KINDS, T0, record("s1", SHORT_KIND, T0, 1));
        Instant marker = T0.plus(Duration.ofMinutes(1));
        ingestAt(STANDARD, marker, record("m", STANDARD_KIND, marker, 1));
        ingestAt(KINDS, marker, record("s2", SHORT_KIND, marker, 1));
        jdbc.execute("""
                CREATE OR REPLACE FUNCTION retention_it_refuse() RETURNS trigger LANGUAGE plpgsql AS $$
                BEGIN
                  IF OLD.source_id = 'test-standard' THEN RAISE EXCEPTION 'refused by RetentionJobIT'; END IF;
                  RETURN OLD;
                END $$
                """);
        jdbc.execute("CREATE TRIGGER retention_it_refuse BEFORE DELETE ON record_version"
                + " FOR EACH ROW EXECUTE FUNCTION retention_it_refuse()");
        try {
            RetentionRunResult result = runAt(T0.plus(Duration.ofDays(8)));

            assertThat(result.skippedSources()).containsExactly(STANDARD);
            assertThat(versions(STANDARD, "x")).isEqualTo(1);
            assertThat(head(STANDARD, "x")).isTrue();
            assertThat(state.floorSeq(STANDARD)).isZero();
            assertThat(versions(KINDS, "s1")).isZero();
            assertThat(head(KINDS, "s1")).isFalse();
        } finally {
            jdbc.execute("DROP TRIGGER IF EXISTS retention_it_refuse ON record_version");
            jdbc.execute("DROP FUNCTION IF EXISTS retention_it_refuse()");
        }
    }

    @Test
    @DisplayName("a 304 streak never ages out entities of the last full response")
    void notModifiedStreakKeepsEntities() {
        health.recordSuccess(STANDARD, 1, 1, false);
        ingestAt(STANDARD, T0, record("x", STANDARD_KIND, T0, 1));
        Instant marker = T0.plus(Duration.ofMinutes(1));
        ingestAt(STANDARD, marker, record("m", STANDARD_KIND, marker, 1));
        for (int day = 1; day <= 8; day++) {
            clock.setInstant(T0.plus(Duration.ofDays(day)));
            health.recordSuccess(STANDARD, 0, 0, true);
        }

        runAt(T0.plus(Duration.ofDays(8)));

        // The versions follow the window; the head stays because last_data_at − touchMin predates it.
        assertThat(versions(STANDARD, "x")).isZero();
        assertThat(head(STANDARD, "x")).isTrue();
    }

    @Test
    @DisplayName("fetch_log: rows without versions go after 7 days, the others after provenance (30 days)")
    void fetchLogPruning() {
        ingestAt(STANDARD, T0, record("x", STANDARD_KIND, T0, 1));
        Instant again = T0.plus(Duration.ofSeconds(10));
        ingestAt(STANDARD, again, record("x", STANDARD_KIND, again, 1));
        assertThat(fetchRows(true)).isEqualTo(1);
        assertThat(fetchRows(false)).isEqualTo(1);

        RetentionRunResult week = runAt(T0.plus(Duration.ofDays(8)));
        assertThat(fetchRows(false)).isZero();
        assertThat(fetchRows(true)).isEqualTo(1);
        assertThat(week.fetchLogDeleted()).isEqualTo(1);

        runAt(T0.plus(Duration.ofDays(31)));
        assertThat(fetchRows(true)).isZero();
    }

    @Test
    @DisplayName("seq_clock keeps minutes back to the largest window plus a day")
    void seqClockPruning() {
        ingestAt(STANDARD, T0, record("x", STANDARD_KIND, T0, 1));
        Duration largest = resolver.largestWindow();
        Instant now = T0.plus(largest).plus(DAY).plus(Duration.ofHours(1));
        Instant recent = now.minus(Duration.ofHours(2));
        ingestAt(STANDARD, recent, record("y", STANDARD_KIND, recent, 1));

        RetentionRunResult result = runAt(now);

        assertThat(result.seqClockDeleted()).isGreaterThanOrEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM seq_clock WHERE minute < ?", Long.class,
                java.sql.Timestamp.from(now.minus(largest).minus(DAY)))).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM seq_clock WHERE minute >= ?", Long.class,
                java.sql.Timestamp.from(recent.minus(Duration.ofMinutes(1))))).isPositive();
    }

    @Test
    @DisplayName("a second run while one is active is 409 ALREADY_RUNNING")
    void concurrentRunConflicts() throws Exception {
        blocking.arm();
        CompletableFuture<RetentionRunResult> first = CompletableFuture.supplyAsync(job::run);
        assertThat(blocking.awaitStarted()).isTrue();
        assertThat(job.isRunning()).isTrue();

        assertThatThrownBy(job::run)
                .isInstanceOf(ConflictException.class)
                .extracting(e -> ((ConflictException) e).getCode())
                .isEqualTo(ConflictException.ALREADY_RUNNING);

        blocking.release();
        assertThat(first.get().startedAt()).isNotNull();
        assertThat(job.isRunning()).isFalse();
    }

    private long fetchRows(boolean withVersions) {
        return jdbc.queryForObject("SELECT count(*) FROM fetch_log WHERE source_id = ? AND "
                + (withVersions ? "versions_written > 0" : "versions_written = 0"), Long.class, STANDARD);
    }

    private List<String> leafNames() {
        return TestPartitions.leaves(jdbc, HistoryTier.TRACK).stream().map(LeafInfo::name).toList();
    }
}
