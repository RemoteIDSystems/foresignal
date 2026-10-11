package ai.foresignal.store.history;

import ai.foresignal.source.spi.HistoryTier;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Keeps {@code record_version}'s seq-range leaves ahead of allocation, and drops old leaves
 * safely (Core-Data-Model §9).
 *
 * <p>Each tier parent ({@code record_version_std}, {@code record_version_trk}) is range-partitioned
 * on {@code seq} into leaves {@code rv_s_<start>} / {@code rv_t_<start>} covering
 * {@code [k·block, (k+1)·block)}. seq is global across tiers, so both tiers need coverage around
 * the allocator's position.
 *
 * <p><strong>Locking.</strong> Every operation holds one in-process {@link ReentrantLock} and runs
 * on its own autocommit connection from the {@link DataSource} with {@code lock_timeout = '3s'},
 * retrying lock timeouts ({@code 55P03}) up to 5 times with exponential backoff from 200 ms. This
 * class is never {@code @Transactional}: an attach must not hold catalog locks for the length of a
 * caller's transaction, and {@code DETACH ... CONCURRENTLY} cannot run inside one. Session settings
 * are reset before the connection returns to the pool.
 *
 * <p><strong>Coverage is range-based.</strong> An in-memory map of attached leaves per tier is
 * refreshed from {@code pg_inherits} and {@code pg_get_expr(relpartbound)} at start, after every
 * change and on {@link #refresh()}, so {@link #coveredUpTo} needs no SQL and the writer can call it
 * for every batch. Leaves of any width are understood, so a test can run a manager with tiny
 * blocks.
 *
 * <p>Depends only on the {@link DataSource}, a {@link MeterRegistry} and {@link HistoryProperties},
 * never on JPA, because it runs before the {@code EntityManagerFactory} is built.
 */
public class PartitionManager {

    private static final Logger log = LoggerFactory.getLogger(PartitionManager.class);

    /** The headroom gauge, tagged {@code tier}. */
    public static final String HEADROOM_GAUGE = "foresignal.history.partition.headroom";

    static final String LOCK_NOT_AVAILABLE = "55P03";
    static final String DUPLICATE_TABLE = "42P07";
    /**
     * Two sessions creating the same table at the same moment can collide on the catalog's own
     * unique index ({@code pg_type_typname_nsp_index}) instead of reporting {@code 42P07}.
     */
    static final String UNIQUE_VIOLATION = "23505";
    static final int MAX_RETRIES = 5;
    static final long BASE_BACKOFF_MILLIS = 200;

    private static final Pattern RANGE_BOUND =
            Pattern.compile("FROM \\('?(-?\\d+)'?\\) TO \\('?(-?\\d+)'?\\)");
    private static final Pattern LEAF_NAME = Pattern.compile("rv_[st]_\\d+");

    private final DataSource dataSource;
    private final HistoryProperties properties;
    private final ReentrantLock lock = new ReentrantLock();

    private volatile Map<HistoryTier, NavigableMap<Long, LeafInfo>> attached = emptyLeafMap();

    /** The last {@code record_seq.last_value} read, or -1 before the first read. */
    private volatile long lastValue = -1;

    /** Creates the manager and registers one headroom gauge per tier. */
    public PartitionManager(DataSource dataSource, MeterRegistry meterRegistry, HistoryProperties properties) {
        this.dataSource = dataSource;
        this.properties = properties;
        for (HistoryTier tier : HistoryTier.values()) {
            Gauge.builder(HEADROOM_GAUGE, this, manager -> manager.headroom(tier))
                    .description("Seq values covered by attached partitions ahead of the allocator")
                    .tag("tier", tier.wireValue())
                    .strongReference(true)
                    .register(meterRegistry);
        }
    }

    // ---------------------------------------------------------------- naming

    /** The tier parent table of {@code tier}. */
    public static String parentOf(HistoryTier tier) {
        return tier == HistoryTier.TRACK ? HistoryDdl.TRACK_PARENT : HistoryDdl.STANDARD_PARENT;
    }

    /** The leaf name for the block starting at {@code start}. */
    public static String leafName(HistoryTier tier, long start) {
        return (tier == HistoryTier.TRACK ? "rv_t_" : "rv_s_") + start;
    }

    /** The configured block size of {@code tier}. */
    public long block(HistoryTier tier) {
        return properties.block(tier);
    }

    // ---------------------------------------------------------------- coverage (no SQL)

    /**
     * The exclusive end of the contiguous attached range that contains {@code fromSeq}, or
     * {@code fromSeq} itself when no attached leaf contains it. Reads only the in-memory map.
     */
    public long coveredUpTo(HistoryTier tier, long fromSeq) {
        return coveredUpTo(attached.get(tier), fromSeq);
    }

    /** The attached leaves of {@code tier}, ordered by range. */
    public List<LeafInfo> leaves(HistoryTier tier) {
        return List.copyOf(attached.get(tier).values());
    }

    /**
     * Seq values covered ahead of the allocator: {@code coveredUpTo(tier, last_value) − last_value},
     * using the last value read by a maintenance or ensure call. NaN before the first read.
     */
    public double headroom(HistoryTier tier) {
        long last = lastValue;
        if (last < 0) {
            return Double.NaN;
        }
        return coveredUpTo(tier, last) - last;
    }

    // ---------------------------------------------------------------- operations

    /** Reloads the attached-leaf map and the allocator position from the database. */
    public void refresh() {
        withConnection(connection -> {
            loadLeaves(connection);
            lastValue = readLastValue(connection);
            return null;
        });
    }

    /** Ensures the block containing {@code toSeq} exists. */
    public void ensureFor(HistoryTier tier, long toSeq) {
        ensureRange(tier, toSeq, toSeq);
    }

    /** Creates every missing block intersecting {@code [fromSeq, toSeq]}. Idempotent. */
    public void ensureRange(HistoryTier tier, long fromSeq, long toSeq) {
        withConnection(connection -> {
            if (ensureRangeOn(connection, tier, fromSeq, toSeq)) {
                analyze(connection);
            }
            return null;
        });
    }

    /**
     * Per tier, ensures coverage of {@code [last_value, last_value + 1.5 × block]}, where
     * {@code last_value} is read from {@code record_seq}.
     */
    public void ensureAhead() {
        withConnection(connection -> {
            long last = readLastValue(connection);
            lastValue = last;
            boolean changed = false;
            for (HistoryTier tier : HistoryTier.values()) {
                long block = block(tier);
                changed |= ensureRangeOn(connection, tier, last, last + block + block / 2);
            }
            if (changed) {
                analyze(connection);
            }
            return null;
        });
    }

    /**
     * Detaches and drops one leaf. Called by retention (prompt 019) for TRACK leaves wholly older
     * than the track window.
     *
     * <p>Runs {@link #ensureAhead()} first, then {@code DETACH PARTITION ... CONCURRENTLY} with a
     * 10-minute statement timeout, then {@code DROP TABLE}. A leaf whose earlier detach was
     * interrupted is finished with {@code FINALIZE} instead.
     *
     * @throws IllegalStateException when called inside a transaction, where a concurrent detach
     *     cannot run
     * @throws IllegalArgumentException when {@code leafName} is not an attached leaf of
     *     {@code tier}, or its range reaches past the allocator (it may still receive rows)
     */
    public void drop(HistoryTier tier, String leafName) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException(
                    "PartitionManager.drop must not run inside a transaction:"
                            + " DETACH PARTITION CONCURRENTLY cannot run in a transaction block");
        }
        if (leafName == null || !LEAF_NAME.matcher(leafName).matches()) {
            throw new IllegalArgumentException("Not a history leaf name: " + leafName);
        }
        ensureAhead();
        withConnection(connection -> {
            LeafInfo leaf = loadLeaves(connection).get(tier).values().stream()
                    .filter(l -> l.name().equals(leafName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            leafName + " is not an attached leaf of " + parentOf(tier)));
            long last = readLastValue(connection);
            if (!leaf.detachPending() && leaf.toSeq() > last + 1) {
                throw new IllegalArgumentException(
                        "Refusing to drop " + leafName + ": its range [" + leaf.fromSeq() + ", "
                                + leaf.toSeq() + ") reaches past the allocator at " + last);
            }
            execute(connection, "SET statement_timeout = '10min'");
            if (leaf.detachPending()) {
                execute(connection, "ALTER TABLE " + parentOf(tier) + " DETACH PARTITION " + leafName + " FINALIZE");
            } else {
                execute(connection,
                        "ALTER TABLE " + parentOf(tier) + " DETACH PARTITION " + leafName + " CONCURRENTLY");
            }
            execute(connection, "DROP TABLE " + leafName);
            log.info("Dropped history partition {} [{}, {})", leafName, leaf.fromSeq(), leaf.toSeq());
            analyze(connection);
            loadLeaves(connection);
            return null;
        });
    }

    /**
     * Finishes every leaf left {@code inhdetachpending} by an interrupted concurrent detach:
     * {@code DETACH PARTITION ... FINALIZE}, then {@code DROP TABLE}.
     *
     * @return the number of leaves finished
     */
    public int finalizePendingDetaches() {
        return withConnection(connection -> {
            int finished = 0;
            for (HistoryTier tier : HistoryTier.values()) {
                for (LeafInfo leaf : loadLeaves(connection).get(tier).values()) {
                    if (!leaf.detachPending()) {
                        continue;
                    }
                    execute(connection, "SET statement_timeout = '10min'");
                    execute(connection,
                            "ALTER TABLE " + parentOf(tier) + " DETACH PARTITION " + leaf.name() + " FINALIZE");
                    execute(connection, "DROP TABLE " + leaf.name());
                    log.info("Finalized and dropped history partition {}", leaf.name());
                    finished++;
                }
            }
            if (finished > 0) {
                analyze(connection);
                loadLeaves(connection);
            }
            return finished;
        });
    }

    /**
     * One maintenance pass: finish pending detaches, keep coverage ahead, and warn when headroom
     * is below one block.
     */
    public void maintain() {
        finalizePendingDetaches();
        ensureAhead();
        for (HistoryTier tier : HistoryTier.values()) {
            double headroom = headroom(tier);
            if (headroom < block(tier)) {
                log.warn(
                        "History partition headroom for tier {} is {} seq, below one block ({})",
                        tier.wireValue(),
                        (long) headroom,
                        block(tier));
            }
        }
    }

    /** Refreshes planner statistics on {@code record_version}, under the manager lock. */
    public void analyze() {
        withConnection(connection -> {
            analyze(connection);
            return null;
        });
    }

    /** The scheduled maintenance pass; skipped when {@code maintenance.enabled=false}. */
    @Scheduled(
            initialDelayString = "${foresignal.history.maintenance.interval:10m}",
            fixedDelayString = "${foresignal.history.maintenance.interval:10m}")
    public void scheduledMaintenance() {
        if (!properties.maintenance().enabled()) {
            return;
        }
        try {
            maintain();
        } catch (RuntimeException e) {
            log.error("History partition maintenance failed; it will be retried", e);
        }
    }

    /** The daily {@code ANALYZE record_version} at 03:17 UTC; skipped when maintenance is disabled. */
    @Scheduled(cron = "0 17 3 * * *", zone = "UTC")
    public void scheduledAnalyze() {
        if (!properties.maintenance().enabled()) {
            return;
        }
        try {
            analyze();
        } catch (RuntimeException e) {
            log.error("Daily ANALYZE of record_version failed", e);
        }
    }

    // ---------------------------------------------------------------- internals

    /** Creates missing blocks; returns true when anything was attached. */
    private boolean ensureRangeOn(Connection connection, HistoryTier tier, long fromSeq, long toSeq)
            throws SQLException {
        long from = Math.max(0, fromSeq);
        if (toSeq < from) {
            return false;
        }
        NavigableMap<Long, LeafInfo> current = loadLeaves(connection).get(tier);
        long block = block(tier);
        boolean changed = false;
        for (long k = from / block; k <= toSeq / block; k++) {
            long start = k * block;
            long end = start + block;
            if (coveredUpTo(current, start) >= end) {
                continue;
            }
            changed |= createLeaf(connection, tier, start, end);
            current = attached.get(tier);
        }
        return changed;
    }

    /** Creates and attaches one leaf; returns false when a concurrent creator already did. */
    private boolean createLeaf(Connection connection, HistoryTier tier, long start, long end)
            throws SQLException {
        String name = leafName(tier, start);
        try {
            execute(connection, "CREATE TABLE " + name + " (LIKE " + HistoryDdl.TABLE + " INCLUDING DEFAULTS)");
        } catch (SQLException e) {
            if (!DUPLICATE_TABLE.equals(e.getSQLState()) && !UNIQUE_VIOLATION.equals(e.getSQLState())) {
                throw e;
            }
            // Exists already, from a concurrent creator or an interrupted earlier attempt. Attach it
            // below unless the re-check shows it is attached.
            if (covers(loadLeaves(connection).get(tier), start, end)) {
                return false;
            }
        }
        try {
            execute(connection, "ALTER TABLE " + parentOf(tier) + " ATTACH PARTITION " + name
                    + " FOR VALUES FROM (" + start + ") TO (" + end + ")");
        } catch (SQLException e) {
            if (LOCK_NOT_AVAILABLE.equals(e.getSQLState())) {
                throw e;
            }
            // 42P17 (overlap) or "already a partition": success when another creator got there.
            if (covers(loadLeaves(connection).get(tier), start, end)) {
                log.debug("History partition {} was attached concurrently", name);
                return false;
            }
            throw e;
        }
        loadLeaves(connection);
        log.info("Attached history partition {} [{}, {}) under {}", name, start, end, parentOf(tier));
        return true;
    }

    private static boolean covers(NavigableMap<Long, LeafInfo> leaves, long start, long end) {
        return coveredUpTo(leaves, start) >= end;
    }

    static long coveredUpTo(NavigableMap<Long, LeafInfo> leaves, long fromSeq) {
        Map.Entry<Long, LeafInfo> entry = leaves.floorEntry(fromSeq);
        if (entry == null || entry.getValue().detachPending() || !entry.getValue().contains(fromSeq)) {
            return fromSeq;
        }
        long end = entry.getValue().toSeq();
        while (true) {
            LeafInfo next = leaves.get(end);
            if (next == null || next.detachPending()) {
                return end;
            }
            end = next.toSeq();
        }
    }

    /** Reads the attached leaves of both tiers, replaces the in-memory map, and returns it. */
    private Map<HistoryTier, NavigableMap<Long, LeafInfo>> loadLeaves(Connection connection)
            throws SQLException {
        Map<HistoryTier, NavigableMap<Long, LeafInfo>> loaded = new EnumMap<>(HistoryTier.class);
        for (HistoryTier tier : HistoryTier.values()) {
            loaded.put(tier, new TreeMap<>());
        }
        try (Statement statement = connection.createStatement();
                ResultSet rows = statement.executeQuery(HistoryDdl.SELECT_LEAVES)) {
            while (rows.next()) {
                String name = rows.getString(1);
                HistoryTier tier =
                        HistoryDdl.TRACK_PARENT.equals(rows.getString(2)) ? HistoryTier.TRACK : HistoryTier.STANDARD;
                String bound = rows.getString(3);
                Matcher m = bound == null ? null : RANGE_BOUND.matcher(bound);
                if (m == null || !m.find()) {
                    log.warn("Ignoring history partition {} with unsupported bound {}", name, bound);
                    continue;
                }
                long from = Long.parseLong(m.group(1));
                long to = Long.parseLong(m.group(2));
                loaded.get(tier).put(from, new LeafInfo(name, tier, from, to, rows.getBoolean(4)));
            }
        }
        Map<HistoryTier, NavigableMap<Long, LeafInfo>> frozen = new EnumMap<>(HistoryTier.class);
        loaded.forEach((tier, map) -> frozen.put(tier, Collections.unmodifiableNavigableMap(map)));
        attached = frozen;
        return frozen;
    }

    private static long readLastValue(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet rows = statement.executeQuery(HistoryDdl.SELECT_LAST_VALUE)) {
            rows.next();
            return rows.getLong(1);
        }
    }

    private static void analyze(Connection connection) throws SQLException {
        execute(connection, HistoryDdl.ANALYZE);
    }

    private static void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private static Map<HistoryTier, NavigableMap<Long, LeafInfo>> emptyLeafMap() {
        Map<HistoryTier, NavigableMap<Long, LeafInfo>> empty = new EnumMap<>(HistoryTier.class);
        for (HistoryTier tier : HistoryTier.values()) {
            empty.put(tier, Collections.emptyNavigableMap());
        }
        return empty;
    }

    /** Work run on the manager's connection. */
    @FunctionalInterface
    interface ConnectionWork<T> {
        T run(Connection connection) throws SQLException;
    }

    /**
     * Runs {@code work} under the manager lock on an autocommit connection with
     * {@code lock_timeout = '3s'}, retrying lock timeouts. The work must be safe to repeat.
     */
    <T> T withConnection(ConnectionWork<T> work) {
        lock.lock();
        try {
            for (int attempt = 0; ; attempt++) {
                try (Connection connection = dataSource.getConnection()) {
                    boolean autoCommit = connection.getAutoCommit();
                    connection.setAutoCommit(true);
                    try {
                        execute(connection, "SET lock_timeout = '3s'");
                        return work.run(connection);
                    } finally {
                        resetSession(connection, autoCommit);
                    }
                } catch (SQLException e) {
                    if (LOCK_NOT_AVAILABLE.equals(e.getSQLState()) && attempt < MAX_RETRIES) {
                        long backoff = BASE_BACKOFF_MILLIS << attempt;
                        log.info("History partition operation hit a lock timeout; retry {} in {} ms", attempt + 1, backoff);
                        pause(backoff);
                        continue;
                    }
                    throw new IllegalStateException("History partition operation failed: " + e.getMessage(), e);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private static void resetSession(Connection connection, boolean autoCommit) {
        try {
            execute(connection, "RESET lock_timeout");
            execute(connection, "RESET statement_timeout");
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            log.debug("Could not reset a partition-maintenance session", e);
        }
    }

    private static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting to retry partition maintenance", e);
        }
    }
}
