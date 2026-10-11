package ai.foresignal.store.history;

import static ai.foresignal.source.spi.HistoryTier.STANDARD;
import static ai.foresignal.source.spi.HistoryTier.TRACK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.TestPartitions;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Exercises a {@link PartitionManager} with blocks of 100 against the real partitioned table.
 *
 * <p>Each test starts with every default-sized leaf dropped ({@link TestPartitions}) and the
 * application's own manager is restored afterwards, so other test classes see the normal layout.
 * {@code record_seq} restarts at 1 before each test, so {@code ensureAhead} covers
 * {@code [1, 151]}, i.e. the leaves {@code [0,100)} and {@code [100,200)}.
 */
@IntegrationTest
class PartitionManagerIT {

    private static final long BLOCK = 100;

    @Autowired private JdbcTemplate jdbc;
    @Autowired private DataSource dataSource;
    @Autowired private TransactionTemplate transactions;
    @Autowired private PartitionManager applicationPartitions;

    private SimpleMeterRegistry meters;
    private PartitionManager partitions;

    @BeforeEach
    void startWithoutLeaves() {
        TestPartitions.dropAllLeaves(jdbc);
        meters = new SimpleMeterRegistry();
        partitions = newManager(meters, true);
        partitions.refresh();
    }

    @AfterEach
    void restoreDefaultLeaves() {
        TestPartitions.dropAllLeaves(jdbc);
        TestPartitions.restore(applicationPartitions);
    }

    // ---------------------------------------------------------------- ensure

    @Test
    @DisplayName("ensureAhead covers 1.5 blocks past last_value in both tiers, and is idempotent")
    void ensureAheadIsIdempotent() {
        partitions.ensureAhead();
        List<LeafInfo> track = TestPartitions.leaves(jdbc, TRACK);
        List<LeafInfo> standard = TestPartitions.leaves(jdbc, STANDARD);

        partitions.ensureAhead();
        partitions.ensureAhead();

        assertThat(names(TRACK)).containsExactly("rv_t_0", "rv_t_100");
        assertThat(names(STANDARD)).containsExactly("rv_s_0", "rv_s_100");
        assertThat(TestPartitions.leaves(jdbc, TRACK)).isEqualTo(track);
        assertThat(TestPartitions.leaves(jdbc, STANDARD)).isEqualTo(standard);
        assertThat(partitions.leaves(TRACK)).isEqualTo(track);
        assertThat(partitions.coveredUpTo(TRACK, 1)).isEqualTo(200);
        assertThat(partitions.coveredUpTo(STANDARD, 1)).isEqualTo(200);
    }

    @Test
    @DisplayName("ensureAhead follows the allocator")
    void ensureAheadFollowsTheSequence() {
        jdbc.queryForObject("select setval('record_seq', 1234)", Long.class);

        partitions.ensureAhead();

        // [1234, 1384] intersects the blocks starting at 1200 and 1300.
        assertThat(names(TRACK)).containsExactly("rv_t_1200", "rv_t_1300");
    }

    @Test
    @DisplayName("ensureFor creates the block holding the seq; ensureRange every block it intersects")
    void ensureForAndRange() {
        partitions.ensureFor(TRACK, 550);
        assertThat(names(TRACK)).containsExactly("rv_t_500");

        partitions.ensureRange(STANDARD, 0, 250);
        assertThat(names(STANDARD)).containsExactly("rv_s_0", "rv_s_100", "rv_s_200");
        assertThat(names(TRACK)).containsExactly("rv_t_500");
    }

    @Test
    @DisplayName("a new leaf gets the primary key and the three partitioned indexes from the attach")
    void attachedLeafHasIndexes() {
        partitions.ensureFor(TRACK, 0);

        assertThat(jdbc.queryForList(
                        "select indexdef from pg_indexes where tablename = 'rv_t_0'", String.class))
                .hasSize(4)
                .anyMatch(d -> d.contains("(seq, history_tier)"))
                .anyMatch(d -> d.contains("(source_id, external_id, seq)"))
                .anyMatch(d -> d.contains("(source_id, observed_at)"))
                .anyMatch(d -> d.toLowerCase().contains("gist (geom)"));
    }

    @Test
    @DisplayName("rows are routed to the leaf of their tier and seq range")
    void rowsAreRouted() {
        partitions.ensureRange(TRACK, 0, 199);
        partitions.ensureRange(STANDARD, 0, 199);

        insertVersion(150, TRACK);
        insertVersion(150, STANDARD);
        insertVersion(20, TRACK);

        assertThat(jdbc.queryForList(
                        "select tableoid::regclass::text from record_version order by seq, history_tier",
                        String.class))
                .containsExactly("rv_t_0", "rv_s_100", "rv_t_100");
    }

    @Test
    @DisplayName("no DEFAULT partition is ever created")
    void noDefaultPartition() {
        partitions.ensureAhead();

        assertThat(jdbc.queryForObject(
                        "select count(*) from pg_partitioned_table where partdefid <> 0", Long.class))
                .isZero();
    }

    @Test
    @DisplayName("concurrent ensureRange from two threads on one manager creates each leaf once")
    void concurrentEnsureOneManager() throws Exception {
        runConcurrently(
                () -> {
                    partitions.ensureRange(TRACK, 0, 999);
                    return null;
                },
                () -> {
                    partitions.ensureRange(TRACK, 0, 999);
                    return null;
                });

        assertTenLeaves();
    }

    @Test
    @DisplayName("concurrent ensureRange from two managers (as two processes would) creates each leaf once")
    void concurrentEnsureTwoManagers() throws Exception {
        PartitionManager other = newManager(new SimpleMeterRegistry(), true);

        runConcurrently(
                () -> {
                    partitions.ensureRange(TRACK, 0, 999);
                    return null;
                },
                () -> {
                    other.ensureRange(TRACK, 0, 999);
                    return null;
                });

        assertTenLeaves();
        other.refresh();
        assertThat(other.coveredUpTo(TRACK, 0)).isEqualTo(1000);
        assertThat(partitions.coveredUpTo(TRACK, 0)).isEqualTo(1000);
    }

    @Test
    @DisplayName("attach succeeds while another session holds an open insert transaction on a leaf")
    void attachDuringOpenInsert() throws SQLException {
        partitions.ensureAhead();
        try (Connection writer = dataSource.getConnection()) {
            writer.setAutoCommit(false);
            try (Statement statement = writer.createStatement()) {
                statement.executeUpdate(insertSql(5, TRACK));
            }
            try {
                long started = System.nanoTime();

                assertThatCode(() -> partitions.ensureRange(TRACK, 200, 450)).doesNotThrowAnyException();

                // Under lock_timeout = 3s, any lock wait would have failed and been retried, taking
                // longer than this; finishing sooner shows the attach never waited on the writer.
                assertThat(Duration.ofNanos(System.nanoTime() - started)).isLessThan(Duration.ofSeconds(3));
                assertThat(names(TRACK)).containsExactly("rv_t_0", "rv_t_100", "rv_t_200", "rv_t_300", "rv_t_400");
            } finally {
                writer.rollback();
            }
        }
    }

    // ---------------------------------------------------------------- coverage

    @Test
    @DisplayName("coveredUpTo follows contiguous leaves and stops at a gap")
    void coveredUpToStopsAtAGap() {
        partitions.ensureRange(TRACK, 0, 150);
        partitions.ensureFor(TRACK, 350);

        assertThat(partitions.coveredUpTo(TRACK, 50)).isEqualTo(200);
        assertThat(partitions.coveredUpTo(TRACK, 199)).isEqualTo(200);
        assertThat(partitions.coveredUpTo(TRACK, 250)).isEqualTo(250);
        assertThat(partitions.coveredUpTo(TRACK, 300)).isEqualTo(400);
        assertThat(partitions.coveredUpTo(STANDARD, 50)).isEqualTo(50);
    }

    @Test
    @DisplayName("a dropped low block is recreated by ensureRange")
    void droppedLowBlockIsRecreated() {
        partitions.ensureRange(TRACK, 0, 299);
        jdbc.execute("ALTER TABLE record_version_trk DETACH PARTITION rv_t_0");
        jdbc.execute("DROP TABLE rv_t_0");
        partitions.refresh();
        assertThat(partitions.coveredUpTo(TRACK, 0)).isZero();

        partitions.ensureRange(TRACK, 0, 50);

        assertThat(names(TRACK)).containsExactly("rv_t_0", "rv_t_100", "rv_t_200");
        assertThat(partitions.coveredUpTo(TRACK, 0)).isEqualTo(300);
    }

    @Test
    @DisplayName("a leaf table left unattached by an interrupted create is attached, not recreated")
    void unattachedLeafIsAttached() {
        jdbc.execute("CREATE TABLE rv_t_0 (LIKE record_version INCLUDING DEFAULTS)");

        partitions.ensureFor(TRACK, 10);

        assertThat(names(TRACK)).containsExactly("rv_t_0");
    }

    @Test
    @DisplayName("the headroom gauge reports coverage ahead of last_value per tier")
    void headroomGauge() {
        assertThat(newManager(new SimpleMeterRegistry(), true).headroom(TRACK)).isNaN();
        // Refreshed in @BeforeEach with no leaves: nothing is covered ahead.
        assertThat(headroomGauge(TRACK)).isZero();

        partitions.ensureAhead();

        // last_value is 1 and coverage reaches 200.
        assertThat(headroomGauge(TRACK)).isEqualTo(199.0);
        assertThat(headroomGauge(STANDARD)).isEqualTo(199.0);
        assertThat(partitions.headroom(TRACK)).isEqualTo(199.0);
    }

    // ---------------------------------------------------------------- drop

    @Test
    @DisplayName("drop refuses to run inside a transaction")
    void dropInsideTransactionThrows() {
        jdbc.queryForObject("select setval('record_seq', 250)", Long.class);
        partitions.ensureRange(TRACK, 0, 299);

        transactions.executeWithoutResult(status -> assertThatThrownBy(() -> partitions.drop(TRACK, "rv_t_0"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("transaction"));

        assertThat(names(TRACK)).contains("rv_t_0");
    }

    @Test
    @DisplayName("drop detaches concurrently, drops the table and refreshes coverage")
    void detachThenDrop() {
        jdbc.queryForObject("select setval('record_seq', 250)", Long.class);
        partitions.ensureRange(TRACK, 0, 299);
        insertVersion(10, TRACK);

        partitions.drop(TRACK, "rv_t_0");

        assertThat(jdbc.queryForObject("select to_regclass('rv_t_0') is null", Boolean.class)).isTrue();
        // ensureAhead ran first: [250, 400] is covered.
        assertThat(names(TRACK)).containsExactly("rv_t_100", "rv_t_200", "rv_t_300", "rv_t_400");
        assertThat(partitions.coveredUpTo(TRACK, 0)).isZero();
        assertThat(partitions.coveredUpTo(TRACK, 100)).isEqualTo(500);
        assertThat(jdbc.queryForObject("select count(*) from record_version", Long.class)).isZero();
    }

    @Test
    @DisplayName("drop refuses a leaf the allocator has not passed, an unknown leaf, and a bad name")
    void dropRefusesUnsafeLeaves() {
        partitions.ensureAhead();

        assertThatThrownBy(() -> partitions.drop(TRACK, "rv_t_100"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reaches past the allocator");
        assertThatThrownBy(() -> partitions.drop(TRACK, "rv_t_900"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not an attached leaf");
        assertThatThrownBy(() -> partitions.drop(TRACK, "rv_s_0"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> partitions.drop(TRACK, "record_head; drop table x"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Not a history leaf name");
        assertThat(names(TRACK)).containsExactly("rv_t_0", "rv_t_100");
    }

    @Test
    @DisplayName("a leaf left inhdetachpending is finalized and dropped by the next maintenance run")
    void pendingDetachIsFinalized() throws SQLException {
        jdbc.queryForObject("select setval('record_seq', 250)", Long.class);
        partitions.ensureRange(TRACK, 0, 299);
        leaveDetachPending("rv_t_0");
        assertThat(TestPartitions.leaves(jdbc, TRACK))
                .filteredOn(LeafInfo::detachPending)
                .extracting(LeafInfo::name)
                .containsExactly("rv_t_0");
        partitions.refresh();
        assertThat(partitions.coveredUpTo(TRACK, 0)).isZero();

        partitions.maintain();

        assertThat(jdbc.queryForObject("select to_regclass('rv_t_0') is null", Boolean.class)).isTrue();
        assertThat(TestPartitions.leaves(jdbc, TRACK)).noneMatch(LeafInfo::detachPending);
        assertThat(names(TRACK)).containsExactly("rv_t_100", "rv_t_200", "rv_t_300", "rv_t_400");
    }

    @Test
    @DisplayName("drop of a leaf already pending detach finishes it with FINALIZE")
    void dropFinalizesAPendingLeaf() throws SQLException {
        jdbc.queryForObject("select setval('record_seq', 250)", Long.class);
        partitions.ensureRange(TRACK, 0, 299);
        leaveDetachPending("rv_t_0");

        partitions.drop(TRACK, "rv_t_0");

        assertThat(jdbc.queryForObject("select to_regclass('rv_t_0') is null", Boolean.class)).isTrue();
        assertThat(partitions.finalizePendingDetaches()).isZero();
    }

    // ---------------------------------------------------------------- schedule

    @Test
    @DisplayName("scheduled maintenance and ANALYZE do nothing when maintenance is disabled")
    void scheduledRunsRespectTheSwitch() {
        PartitionManager disabled = newManager(new SimpleMeterRegistry(), false);

        disabled.scheduledMaintenance();
        disabled.scheduledAnalyze();
        assertThat(TestPartitions.leaves(jdbc, TRACK)).isEmpty();

        partitions.scheduledMaintenance();
        partitions.scheduledAnalyze();
        assertThat(names(TRACK)).containsExactly("rv_t_0", "rv_t_100");
    }

    @Test
    @DisplayName("naming and block sizes")
    void namingAndBlocks() {
        assertThat(PartitionManager.parentOf(TRACK)).isEqualTo("record_version_trk");
        assertThat(PartitionManager.parentOf(STANDARD)).isEqualTo("record_version_std");
        assertThat(PartitionManager.leafName(TRACK, 20_000_000)).isEqualTo("rv_t_20000000");
        assertThat(PartitionManager.leafName(STANDARD, 0)).isEqualTo("rv_s_0");
        assertThat(partitions.block(TRACK)).isEqualTo(BLOCK);
        assertThat(applicationPartitions.block(TRACK)).isEqualTo(20_000_000L);
        assertThat(applicationPartitions.block(STANDARD)).isEqualTo(250_000_000L);
    }

    // ---------------------------------------------------------------- helpers

    private PartitionManager newManager(SimpleMeterRegistry registry, boolean maintenanceEnabled) {
        return new PartitionManager(
                dataSource,
                registry,
                new HistoryProperties(
                        BLOCK, BLOCK, new HistoryProperties.Maintenance(maintenanceEnabled, Duration.ofMinutes(10))));
    }

    private List<String> names(HistoryTier tier) {
        return TestPartitions.leaves(jdbc, tier).stream().map(LeafInfo::name).toList();
    }

    private double headroomGauge(HistoryTier tier) {
        return meters.get(PartitionManager.HEADROOM_GAUGE).tag("tier", tier.wireValue()).gauge().value();
    }

    private void assertTenLeaves() {
        List<LeafInfo> leaves = TestPartitions.leaves(jdbc, TRACK);
        assertThat(leaves).extracting(LeafInfo::fromSeq)
                .containsExactlyElementsOf(LongStream.range(0, 10).map(k -> k * BLOCK).boxed().toList());
        assertThat(leaves).allMatch(l -> l.toSeq() - l.fromSeq() == BLOCK);
    }

    private static void runConcurrently(Callable<Void> first, Callable<Void> second) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            CountDownLatch start = new CountDownLatch(1);
            List<Future<Void>> futures = new ArrayList<>();
            for (Callable<Void> task : List.of(first, second)) {
                futures.add(pool.submit(() -> {
                    start.await();
                    return task.call();
                }));
            }
            start.countDown();
            // Wait for both before reporting, so a failing task never leaves the other running.
            Exception failure = null;
            for (Future<Void> future : futures) {
                try {
                    future.get(60, TimeUnit.SECONDS);
                } catch (Exception e) {
                    if (failure == null) {
                        failure = e;
                    } else {
                        failure.addSuppressed(e);
                    }
                }
            }
            if (failure != null) {
                throw failure;
            }
        } finally {
            pool.shutdownNow();
        }
    }

    /**
     * Interrupts a {@code DETACH ... CONCURRENTLY} after its first phase: another session holds a
     * lock on the tier parent, so the detach waits in its second phase until its statement timeout
     * cancels it, which leaves the leaf {@code inhdetachpending}.
     */
    private void leaveDetachPending(String leaf) throws SQLException {
        try (Connection holder = dataSource.getConnection(); Connection detacher = dataSource.getConnection()) {
            holder.setAutoCommit(false);
            try (Statement statement = holder.createStatement()) {
                statement.executeQuery("select count(*) from record_version_trk").close();
                try (Statement detach = detacher.createStatement()) {
                    detach.execute("SET statement_timeout = '1s'");
                    assertThatThrownBy(() -> detach.execute(
                                    "ALTER TABLE record_version_trk DETACH PARTITION " + leaf + " CONCURRENTLY"))
                            .isInstanceOf(SQLException.class);
                    detach.execute("RESET statement_timeout");
                }
            } finally {
                holder.rollback();
            }
        }
    }

    private void insertVersion(long seq, HistoryTier tier) {
        jdbc.update(insertSql(seq, tier));
    }

    private static String insertSql(long seq, HistoryTier tier) {
        return "insert into record_version (seq, ingested_at, observed_at, received_at, version, history_tier,"
                + " persist_reason, qa_flags, tombstone, source_id, external_id, kind, payload) values ("
                + seq + ", now(), now(), now(), 1, " + tier.code() + ", 0, 0, false, 's', 'e" + seq
                + "', 'k', '{}'::jsonb)";
    }
}
