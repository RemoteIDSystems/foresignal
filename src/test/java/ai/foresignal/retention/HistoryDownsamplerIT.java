package ai.foresignal.retention;

import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.history.LeafInfo;
import ai.foresignal.system.HistoryStats;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.TestPartitions;
import ai.foresignal.testsupport.retention.DownsampleItSupport;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import io.micrometer.core.instrument.MeterRegistry;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * History downsampling's rewrite and swap end to end (prompt 044): leaf bounds and indexes after the
 * swap, a concurrent writer, the lock-timeout path, startup recovery at both crash points, the
 * unchanged track floor, eligibility (raw window, drop horizon, consumer bound), the {@code downsampled}
 * flag on {@code /versions} and the {@code /v1/system} counters.
 */
@IntegrationTest
@Import(RetentionTestSources.class)
class HistoryDownsamplerIT extends DownsampleItSupport {

    @Autowired DataSource dataSource;
    @Autowired MeterRegistry meters;
    @Autowired HistoryStats historyStats;
    @Autowired MockMvc mvc;

    @AfterEach
    void restoreBackoff() {
        downsampler.baseBackoffMillis(200);
    }

    LeafInfo leaf(String name) {
        return TestPartitions.leaves(jdbc, HistoryTier.TRACK).stream()
                .filter(l -> l.name().equals(name)).findFirst().orElseThrow();
    }

    long count(String table) {
        return jdbc.queryForObject("SELECT count(*) FROM " + table, Long.class);
    }

    @Test
    @DisplayName("the thinned copy replaces the leaf: same bounds, four linked indexes, no copy left, DONE, floor unchanged")
    void rewriteAndSwap() {
        flyEveryMinute("a", 1.0, 30);
        long rowsBefore = count(LEAF);
        pushPastRawWindow();
        runAt(MARKER.plus(Duration.ofMinutes(1)));
        long floorBefore = state.floorSeq(TRACK);
        double failuresBefore = meters.counter(HistoryDownsampler.FAILURES_METRIC).count();
        double keptBefore = meters.counter(HistoryDownsampler.ROWS_AFTER_METRIC).count();

        runDue();

        LeafInfo leaf = leaf(LEAF);
        assertThat(leaf.fromSeq()).isZero();
        assertThat(leaf.toSeq()).isEqualTo(BLOCK);
        assertThat(leaf.detachPending()).isFalse();
        assertThat(tableExists(LEAF + HistoryDownsampler.COPY_SUFFIX)).isFalse();
        assertThat(indexes(LEAF)).hasSize(4).allMatch(name -> name.startsWith(LEAF + "_") && !name.contains("_ds"));
        assertThat(jdbc.queryForObject("""
                SELECT count(*) FROM pg_inherits i JOIN pg_class c ON c.oid = i.inhrelid
                 WHERE c.relkind = 'i' AND c.relname IN (SELECT indexname FROM pg_indexes WHERE tablename = ?)
                """, Long.class, LEAF)).as("every leaf index is attached to its partitioned parent").isEqualTo(4L);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM pg_constraint k JOIN pg_class c ON c.oid = k.conrelid"
                + " WHERE c.relname = ? AND k.conname = ?", Long.class, LEAF, HistoryDownsampler.BOUNDS_CONSTRAINT))
                .isZero();

        Map<String, Object> row = bookkeeping(LEAF);
        assertThat(row).containsEntry("status", "DONE").containsEntry("from_seq", 0L).containsEntry("to_seq", BLOCK)
                .containsEntry("rows_before", rowsBefore).containsEntry("rows_after", count(LEAF));
        assertThat(count(LEAF)).isLessThan(rowsBefore);
        assertThat(row.get("completed_at")).isNotNull();
        assertThat(state.floorSeq(TRACK)).as("track floorSeq is unchanged").isEqualTo(floorBefore);
        assertThat(ranges.contains(HistoryTier.TRACK, 1)).isTrue();
        assertThat(ranges.contains(HistoryTier.TRACK, BLOCK)).isFalse();
        assertThat(meters.counter(HistoryDownsampler.FAILURES_METRIC).count()).isEqualTo(failuresBefore);
        assertThat(meters.counter(HistoryDownsampler.ROWS_AFTER_METRIC).count() - keptBefore).isEqualTo(count(LEAF));

        historyStats.invalidate();
        HistoryStats.Tier track = historyStats.snapshot().tier(HistoryTier.TRACK);
        assertThat(track.downsampledLeaves()).isEqualTo(1);
        assertThat(track.downsampledRows()).isEqualTo(count(LEAF));
        assertThat(historyStats.snapshot().tier(HistoryTier.STANDARD).downsampledLeaves()).isZero();

        // A second run finds nothing new to rewrite.
        runAt(DUE.plus(Duration.ofHours(1)));
        assertThat(bookkeeping(LEAF)).containsEntry("rows_after", count(LEAF));
    }

    @Test
    @DisplayName("/versions flags versions in a thinned leaf; a prevSeq removed by downsampling is returned as stored")
    void versionsFlag() throws Exception {
        flyEveryMinute("a", 1.0, 10);
        pushPastRawWindow();
        runDue();

        mvc.perform(get("/v1/records/" + TRACK + "/a/versions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].downsampled").value(true))
                .andExpect(jsonPath("$.items[1].downsampled").value(true));
        List<Long> kept = seqs(TRACK, "a");
        Long prev = jdbc.queryForObject("SELECT prev_seq FROM record_version WHERE seq = ?", Long.class, kept.get(2));
        assertThat(prev).as("the predecessor of minute 5 was minute 4").isNotNull();
        assertThat(kept).as("removed by downsampling, yet returned as stored").doesNotContain(prev);
        mvc.perform(get("/v1/records/" + TRACK + "/a/versions"))
                .andExpect(jsonPath("$.items[2].prevSeq").value(prev));
        mvc.perform(get("/v1/records/" + TRACK + "/marker/versions"))
                .andExpect(jsonPath("$.items[0].downsampled").value(false));
    }

    @Test
    @DisplayName("writer batches into the newest leaf succeed while the swap runs")
    void concurrentWriter() throws Exception {
        flyEveryMinute("a", 1.0, 30);
        pushPastRawWindow();
        awaitTriggerCursor();
        clock.setInstant(DUE);
        Instant now = DUE;
        AtomicBoolean burst = new AtomicBoolean(true);
        AtomicLong persisted = new AtomicLong();
        List<Throwable> failures = new CopyOnWriteArrayList<>();
        Thread producer = Thread.ofVirtual().start(() -> {
            int round = 0;
            while (burst.get() || round < 20) {
                List<NormalizedRecord> batch = new ArrayList<>();
                for (int i = 0; i < 20; i++) {
                    batch.add(aircraft("w" + round + "-" + i, now.plusMillis(round), 50.0 + i * 0.01, "W"));
                }
                try {
                    persisted.addAndGet(records.ingestAt(TRACK, now, batch.toArray(NormalizedRecord[]::new)).persisted());
                } catch (Throwable t) {
                    failures.add(t);
                }
                round++;
            }
        });
        try {
            Thread.sleep(50);
            job.run();
        } finally {
            burst.set(false);
            producer.join(60_000);
        }

        assertThat(failures).as("writer failures during the swap").isEmpty();
        assertThat(bookkeeping(LEAF)).containsEntry("status", "DONE");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM record_version WHERE source_id = ? AND external_id LIKE 'w%'",
                Long.class, TRACK)).as("every burst version survives").isEqualTo(persisted.get());
        assertThat(jdbc.queryForObject("SELECT min(seq) FROM record_version WHERE external_id LIKE 'w%'", Long.class))
                .isGreaterThanOrEqualTo(BLOCK);
    }

    @Test
    @Timeout(120)
    @DisplayName("a swap that cannot get its locks retries 5 times, then fails cleanly with no copy left")
    void lockTimeout() throws Exception {
        flyEveryMinute("a", 1.0, 10);
        pushPastRawWindow();
        long rowsBefore = count(LEAF);
        double failuresBefore = meters.counter(HistoryDownsampler.FAILURES_METRIC).count();
        downsampler.baseBackoffMillis(10);
        HistoryDownsampler.Outcome outcome;
        try (Connection holder = dataSource.getConnection()) {
            holder.setAutoCommit(false);
            try (Statement statement = holder.createStatement()) {
                // ACCESS SHARE on the leaf: the INSERT ... SELECT can still read it, the DETACH cannot lock it.
                statement.execute("LOCK TABLE " + LEAF + " IN ACCESS SHARE MODE");
            }
            long start = System.nanoTime();
            outcome = downsampler.downsample(leaf(LEAF), Duration.ofMinutes(5));
            assertThat(Duration.ofNanos(System.nanoTime() - start))
                    .as("six attempts of 3 s each").isGreaterThanOrEqualTo(Duration.ofSeconds(15));
            holder.rollback();
        }

        assertThat(outcome.status()).isEqualTo(DownsampleStatus.FAILED);
        assertThat(outcome.error()).contains("lock timeout");
        assertThat(bookkeeping(LEAF)).containsEntry("status", "FAILED");
        assertThat(tableExists(LEAF + HistoryDownsampler.COPY_SUFFIX)).as("no orphan copy").isFalse();
        assertThat(count(LEAF)).isEqualTo(rowsBefore);
        assertThat(leaf(LEAF).toSeq()).isEqualTo(BLOCK);
        assertThat(ranges.contains(HistoryTier.TRACK, 1)).isFalse();
        assertThat(meters.counter(HistoryDownsampler.FAILURES_METRIC).count()).isEqualTo(failuresBefore + 1);

        // Not DONE, so the next run tries again, and succeeds.
        runDue();
        assertThat(bookkeeping(LEAF)).containsEntry("status", "DONE");
    }

    @Test
    @DisplayName("recovery: a copy that was never attached is dropped and the row marked FAILED")
    void recoveryBeforeSwap() {
        flyEveryMinute("a", 1.0, 10);
        long rowsBefore = count(LEAF);
        running(LEAF);
        jdbc.execute("CREATE TABLE " + LEAF + "_ds (LIKE record_version INCLUDING DEFAULTS)");

        assertThat(downsampler.recover()).isEqualTo(1);

        assertThat(tableExists(LEAF + "_ds")).isFalse();
        assertThat(bookkeeping(LEAF)).containsEntry("status", "FAILED");
        assertThat(count(LEAF)).isEqualTo(rowsBefore);
        assertThat(leaf(LEAF).toSeq()).isEqualTo(BLOCK);
    }

    @Test
    @DisplayName("recovery: a committed swap is finished: the original dropped, the copy renamed, DONE")
    void recoveryAfterSwap() {
        flyEveryMinute("a", 1.0, 10);
        long rows = count(LEAF);
        running(LEAF);
        // The state a crash right after step 6 leaves behind.
        jdbc.execute("CREATE TABLE " + LEAF + "_ds (LIKE record_version INCLUDING DEFAULTS)");
        jdbc.execute("INSERT INTO " + LEAF + "_ds SELECT * FROM " + LEAF);
        jdbc.execute("ALTER TABLE " + LEAF + "_ds ADD CONSTRAINT " + HistoryDownsampler.BOUNDS_CONSTRAINT
                + " CHECK (history_tier = 1 AND seq >= 0 AND seq < " + BLOCK + ")");
        jdbc.execute("ALTER TABLE record_version_trk DETACH PARTITION " + LEAF);
        jdbc.execute("ALTER TABLE record_version_trk ATTACH PARTITION " + LEAF + "_ds FOR VALUES FROM (0) TO (" + BLOCK + ")");

        assertThat(downsampler.recover()).isEqualTo(1);

        assertThat(tableExists(LEAF + "_ds")).isFalse();
        LeafInfo leaf = leaf(LEAF);
        assertThat(leaf.fromSeq()).isZero();
        assertThat(leaf.toSeq()).isEqualTo(BLOCK);
        assertThat(count(LEAF)).isEqualTo(rows);
        assertThat(bookkeeping(LEAF)).containsEntry("status", "DONE").containsEntry("rows_after", rows);
        assertThat(ranges.contains(HistoryTier.TRACK, 1)).isTrue();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM pg_constraint WHERE conname = ?", Long.class,
                HistoryDownsampler.BOUNDS_CONSTRAINT)).isZero();
    }

    @Test
    @DisplayName("not eligible: inside the raw window, near the drop horizon, above the consumer bound, or disabled")
    void eligibility() {
        flyEveryMinute("a", 1.0, 10);
        pushPastRawWindow();

        runAt(MARKER.plus(Duration.ofHours(23)));
        assertThat(bookkeeping(LEAF)).as("inside the 24 h raw window").isEmpty();

        awaitTriggerCursor();
        consumer.set(BLOCK - 10);
        runDue();
        assertThat(bookkeeping(LEAF)).as("a consumer has not processed the whole leaf").isEmpty();
        consumer.clear();

        runAt(MARKER.plus(Duration.ofDays(3)).minus(Duration.ofMinutes(55)));
        assertThat(bookkeeping(LEAF)).as("within an hour of the drop").isEmpty();
        assertThat(leaf(LEAF).toSeq()).as("and not yet dropped").isEqualTo(BLOCK);
    }

    @Test
    @DisplayName("trackRawWindow null disables downsampling")
    void disabled() {
        settings.put(ai.foresignal.settings.SettingKeys.RETENTION, new ai.foresignal.settings.RetentionSettings(
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                ai.foresignal.settings.RetentionSettings.DOWNSAMPLING_DISABLED, null));
        settings.invalidateCache();
        flyEveryMinute("a", 1.0, 10);
        pushPastRawWindow();

        runDue();

        assertThat(bookkeeping(LEAF)).isEmpty();
        assertThat(seqs(TRACK, "a")).hasSize(11);
    }

    /** Inserts a RUNNING row for {@code leaf} covering the first block. */
    private void running(String leaf) {
        jdbc.update("INSERT INTO history_downsample (leaf_name, history_tier, from_seq, to_seq, default_interval_s,"
                + " started_at, status) VALUES (?, 1, 0, ?, 300, now(), 'RUNNING')", leaf, BLOCK);
    }
}
