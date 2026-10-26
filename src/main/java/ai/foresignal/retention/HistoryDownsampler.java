package ai.foresignal.retention;

import ai.foresignal.config.StartupOrder;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.history.HistoryDdl;
import ai.foresignal.store.history.LeafInfo;
import ai.foresignal.store.history.PartitionManager;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * History downsampling (Core-Data-Model §13, prompt 044): rewrites one TRACK leaf older than
 * {@code retention.trackRawWindow} per retention run into a thinned copy and swaps it in atomically.
 *
 * <p><strong>Eligible leaf</strong> (oldest first, at most one per run): an attached {@code rv_t_*} leaf
 * with no DONE row whose upper bound is at or below {@code cutoffSeq(now − trackRawWindow)}, at or below
 * the consumer bound ({@link RetentionRun#consumerBound()}, so live triggers and every other durable
 * consumer have processed it), and above {@code cutoffSeq(now − trackWindow + 1 h)} (a leaf about to be
 * dropped is not rewritten).
 *
 * <p><strong>Keep set</strong>, computed over the leaf only: versions whose {@code persist_reason} is
 * FIRST, REAPPEAR, SIGNIFICANT, TOMBSTONE or TRIGGER_EDGE; the newest seq of each entity in the leaf;
 * every seq a {@code record_head} or an {@code alert} references; and, among the remaining rows, the
 * lowest seq per entity and {@code floor(epoch(observed_at) / interval)}, with each source's effective
 * downsample interval ({@code SourceSettings.downsampleInterval}, else
 * {@code retention.trackDownsampleInterval}).
 *
 * <p><strong>Rewrite and swap</strong>, every statement on the {@link PartitionManager} connection and
 * under its lock ({@link PartitionManager#exclusive}): a RUNNING {@code history_downsample} row; the copy
 * {@code rv_t_<start>_ds} ({@code LIKE record_version INCLUDING DEFAULTS}) filled by one
 * {@code INSERT … SELECT}; the leaf indexes built on it before attaching; a {@code CHECK} constraint
 * matching the bounds, so ATTACH skips its validation scan; then one short transaction with
 * {@code SET LOCAL lock_timeout = '3s'} that detaches the original and attaches the copy, retried up to
 * 5 times with backoff on a lock timeout before the attempt is marked FAILED and the copy dropped; then
 * the original is dropped, the copy renamed, the constraint dropped and the leaf analyzed; finally the
 * row is marked DONE and {@link DownsampleRanges} reloaded.
 *
 * <p>The writer never inserts into an eligible leaf (its range is far below the allocator), so writes
 * wait only for the swap transaction. Track {@code floorSeq} is unchanged. Only native SQL touches
 * {@code record_version} (I9). Startup recovery, and the start of every run, resolve a RUNNING row left by
 * a crash: a copy that is not attached is dropped (FAILED); a swap that committed is finished (DONE).
 *
 * <p>Metrics: {@value #ROWS_BEFORE_METRIC}, {@value #ROWS_AFTER_METRIC}, {@value #DURATION_METRIC} and
 * {@value #FAILURES_METRIC}.
 */
@Component
public class HistoryDownsampler implements RetentionStep {

    private static final Logger log = LoggerFactory.getLogger(HistoryDownsampler.class);

    /** Rows of the leaves rewritten, before thinning. */
    public static final String ROWS_BEFORE_METRIC = "foresignal.history.downsample.rows_before";

    /** Rows the thinned copies kept. */
    public static final String ROWS_AFTER_METRIC = "foresignal.history.downsample.rows_after";

    /** Duration of one leaf's rewrite. */
    public static final String DURATION_METRIC = "foresignal.history.downsample.duration";

    /** Rewrites that gave up. */
    public static final String FAILURES_METRIC = "foresignal.history.downsample.failures";

    /** The step's position: right after the TRACK leaf drops (100). */
    public static final int ORDER = 150;

    /** The suffix of the thinned copy while it is built. */
    public static final String COPY_SUFFIX = "_ds";

    /** The bounds constraint that lets ATTACH skip its validation scan. */
    public static final String BOUNDS_CONSTRAINT = "ds_bounds";

    /** Retries of the swap transaction after a lock timeout. */
    static final int MAX_RETRIES = 5;

    static final String LOCK_NOT_AVAILABLE = "55P03";

    /** The reasons that are always kept. */
    static final List<PersistReason> KEPT_REASONS = List.of(PersistReason.FIRST, PersistReason.REAPPEAR,
            PersistReason.SIGNIFICANT, PersistReason.TOMBSTONE, PersistReason.TRIGGER_EDGE);

    private static final Pattern TRACK_LEAF = Pattern.compile("rv_t_\\d+");

    private final PartitionManager partitions;
    private final DownsampleRanges ranges;
    private final SourceRegistry registry;
    private final SourceService sources;
    private final Clock clock;
    private final Counter rowsBefore;
    private final Counter rowsAfter;
    private final Counter failures;
    private final Timer duration;
    private volatile long baseBackoffMillis = 200;

    /** Creates the downsampler. */
    public HistoryDownsampler(PartitionManager partitions, DownsampleRanges ranges, SourceRegistry registry,
            SourceService sources, MeterRegistry meters, Clock clock) {
        this.partitions = partitions;
        this.ranges = ranges;
        this.registry = registry;
        this.sources = sources;
        this.clock = clock;
        this.rowsBefore = Counter.builder(ROWS_BEFORE_METRIC)
                .description("Rows of TRACK leaves rewritten by history downsampling, before thinning").register(meters);
        this.rowsAfter = Counter.builder(ROWS_AFTER_METRIC)
                .description("Rows kept by history downsampling").register(meters);
        this.failures = Counter.builder(FAILURES_METRIC)
                .description("History downsampling rewrites that gave up").register(meters);
        this.duration = Timer.builder(DURATION_METRIC)
                .description("Duration of one history downsampling rewrite").register(meters);
    }

    /**
     * The outcome of one leaf.
     *
     * @param leaf the leaf name
     * @param status DONE or FAILED
     * @param rowsBefore rows before thinning, or null when unknown
     * @param rowsAfter rows kept, or null when FAILED
     * @param error why it failed, or null
     */
    public record Outcome(String leaf, DownsampleStatus status, Long rowsBefore, Long rowsAfter, String error) {}

    @Override
    public String name() {
        return "history-downsampling";
    }

    @Override
    public int order() {
        return ORDER;
    }

    /** Rewrites at most one eligible leaf; does nothing while {@code trackRawWindow} is null. */
    @Override
    public void run(RetentionRun run) {
        RetentionSettings settings = run.settings();
        partitions.refresh();
        List<LeafInfo> leaves = partitions.leaves(HistoryTier.TRACK);
        Set<String> done = housekeeping(leaves);
        if (!settings.downsamplingEnabled()) {
            return;
        }
        long rawCutoff = run.rawCutoff(settings.trackRawWindow());
        long horizon = run.rawCutoff(settings.effectiveTrackWindow().minus(Duration.ofHours(1)));
        long bound = run.consumerBound();
        Optional<LeafInfo> leaf = select(leaves, done, rawCutoff, bound, horizon);
        if (leaf.isEmpty()) {
            return;
        }
        Outcome outcome = downsample(leaf.get(), settings.trackDownsampleInterval());
        if (outcome.status() == DownsampleStatus.FAILED) {
            run.error("history downsampling of " + outcome.leaf() + ": " + outcome.error());
        }
    }

    /**
     * The leaf to rewrite: the oldest attached {@code rv_t_*} leaf, not pending a detach and not DONE,
     * whose upper bound is at or below both {@code rawCutoff} and {@code consumerBound} and above
     * {@code dropHorizon}.
     */
    static Optional<LeafInfo> select(List<LeafInfo> leaves, Set<String> done, long rawCutoff, long consumerBound,
            long dropHorizon) {
        return leaves.stream()
                .sorted(Comparator.comparingLong(LeafInfo::fromSeq))
                .filter(l -> !l.detachPending() && TRACK_LEAF.matcher(l.name()).matches() && !done.contains(l.name()))
                .filter(l -> l.toSeq() <= rawCutoff && l.toSeq() <= consumerBound && l.toSeq() > dropHorizon)
                .findFirst();
    }

    /**
     * Rewrites {@code leaf} now, whatever its age (the retention step decides eligibility).
     *
     * @param leaf an attached TRACK leaf
     * @param defaultInterval {@code retention.trackDownsampleInterval}
     * @return the outcome; the ranges are reloaded either way
     */
    public Outcome downsample(LeafInfo leaf, Duration defaultInterval) {
        Map<String, Long> overrides = overrides();
        long start = System.nanoTime();
        Outcome outcome;
        try {
            outcome = partitions.exclusive(c -> rewrite(c, leaf, defaultInterval.toSeconds(), overrides));
        } catch (SQLException | RuntimeException e) {
            log.warn("History downsampling of {} failed", leaf.name(), e);
            markFailedQuietly(leaf.name(), e.getMessage());
            outcome = new Outcome(leaf.name(), DownsampleStatus.FAILED, null, null, e.getMessage());
        }
        duration.record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        if (outcome.status() == DownsampleStatus.DONE) {
            rowsBefore.increment(outcome.rowsBefore() == null ? 0 : outcome.rowsBefore());
            rowsAfter.increment(outcome.rowsAfter() == null ? 0 : outcome.rowsAfter());
            log.info("Downsampled history partition {}: {} rows kept of {}", leaf.name(), outcome.rowsAfter(),
                    outcome.rowsBefore());
        } else {
            failures.increment();
        }
        ranges.refresh();
        return outcome;
    }

    /** Startup recovery of interrupted rewrites (after the retention state, before the read paths). */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.HISTORY_DOWNSAMPLE)
    public void recoverOnStartup() {
        try {
            int resolved = recover();
            if (resolved > 0) {
                log.info("History downsampling recovery resolved {} interrupted rewrite(s)", resolved);
            }
        } catch (RuntimeException e) {
            log.warn("History downsampling recovery failed; it is retried on the next retention run", e);
        }
    }

    /**
     * Resolves every RUNNING {@code history_downsample} row: a copy that exists but is not attached is
     * dropped and the row marked FAILED; a copy that is attached (the swap committed) is finished and
     * the row marked DONE; a leaf that still carries the bounds constraint is finished likewise; any
     * other RUNNING row is marked FAILED (the leaf is intact, and a repeat rewrite is idempotent).
     *
     * @return the rows resolved
     */
    public int recover() {
        try {
            int resolved = partitions.exclusive(this::recoverOn);
            ranges.refresh();
            return resolved;
        } catch (SQLException e) {
            throw new IllegalStateException("History downsampling recovery failed: " + e.getMessage(), e);
        }
    }

    /** Sets the base backoff between swap attempts (tests shorten it). */
    void baseBackoffMillis(long millis) {
        this.baseBackoffMillis = millis;
    }

    // ---------------------------------------------------------------- the rewrite

    private Outcome rewrite(Connection c, LeafInfo leaf, long defaultSeconds, Map<String, Long> overrides)
            throws SQLException {
        recoverOn(c);
        String name = leaf.name();
        String copy = name + COPY_SUFFIX;
        if (!attached(c, name) || attached(c, copy)) {
            return new Outcome(name, DownsampleStatus.FAILED, null, null, name + " is no longer an attached leaf");
        }
        upsertRunning(c, leaf, defaultSeconds);
        long before;
        long after;
        try {
            execute(c, "SET statement_timeout = '30min'");
            execute(c, "DROP TABLE IF EXISTS " + copy);
            execute(c, "CREATE TABLE " + copy + " (LIKE " + HistoryDdl.TABLE + " INCLUDING DEFAULTS)");
            before = count(c, name);
            updateRowsBefore(c, name, before);
            after = insertKeepSet(c, name, copy, leaf.fromSeq(), leaf.toSeq(), defaultSeconds, overrides);
            createIndexes(c, copy);
            execute(c, "ALTER TABLE " + copy + " ADD CONSTRAINT " + BOUNDS_CONSTRAINT + " CHECK (history_tier = "
                    + HistoryTier.TRACK.code() + " AND seq >= " + leaf.fromSeq() + " AND seq < " + leaf.toSeq() + ")");
        } catch (SQLException e) {
            dropQuietly(c, copy);
            markFailed(c, name, e.getMessage());
            return new Outcome(name, DownsampleStatus.FAILED, null, null, e.getMessage());
        }
        String parent = PartitionManager.parentOf(HistoryTier.TRACK);
        if (!swap(c, parent, name, copy, leaf.fromSeq(), leaf.toSeq())) {
            dropQuietly(c, copy);
            String error = "the swap could not take its locks within the lock timeout after " + MAX_RETRIES + " retries";
            markFailed(c, name, error);
            return new Outcome(name, DownsampleStatus.FAILED, before, null, error);
        }
        finish(c, name, copy);
        markDone(c, name, after);
        return new Outcome(name, DownsampleStatus.DONE, before, after, null);
    }

    /**
     * Step 6: detaches the original and attaches the copy in one short transaction with
     * {@code SET LOCAL lock_timeout = '3s'}, retried on a lock timeout.
     *
     * @return false when every attempt timed out
     */
    private boolean swap(Connection c, String parent, String name, String copy, long from, long to)
            throws SQLException {
        for (int attempt = 0; ; attempt++) {
            c.setAutoCommit(false);
            try {
                execute(c, "SET LOCAL lock_timeout = '3s'");
                execute(c, "ALTER TABLE " + parent + " DETACH PARTITION " + name);
                execute(c, "ALTER TABLE " + parent + " ATTACH PARTITION " + copy + " FOR VALUES FROM (" + from
                        + ") TO (" + to + ")");
                c.commit();
                return true;
            } catch (SQLException e) {
                c.rollback();
                if (!LOCK_NOT_AVAILABLE.equals(e.getSQLState())) {
                    throw e;
                }
                if (attempt >= MAX_RETRIES) {
                    log.warn("History downsampling of {}: the swap timed out on its locks {} times; giving up",
                            name, attempt + 1);
                    return false;
                }
                long backoff = baseBackoffMillis << attempt;
                log.info("History downsampling of {}: the swap hit a lock timeout; retry {} in {} ms", name,
                        attempt + 1, backoff);
                pause(backoff);
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    /**
     * Step 7: drops the detached original, renames the copy into its place (indexes included), drops
     * the bounds constraint and analyzes the leaf. Idempotent, so recovery can repeat it.
     */
    private void finish(Connection c, String name, String copy) throws SQLException {
        if (exists(c, copy)) {
            if (exists(c, name) && !attached(c, name)) {
                withLockRetry(c, "DROP TABLE " + name);
            }
            withLockRetry(c, "ALTER TABLE " + copy + " RENAME TO " + name);
            renameIndexes(c, name, copy);
        }
        withLockRetry(c, "ALTER TABLE " + name + " DROP CONSTRAINT IF EXISTS " + BOUNDS_CONSTRAINT);
        execute(c, "ANALYZE " + name);
    }

    /** Step 3: the keep set, in one {@code INSERT … SELECT}; returns the rows inserted. */
    private static long insertKeepSet(Connection c, String leaf, String copy, long from, long to, long defaultSeconds,
            Map<String, Long> overrides) throws SQLException {
        List<String> ids = new ArrayList<>(overrides.keySet());
        try (PreparedStatement statement = c.prepareStatement(keepSetSql(leaf, copy, from, to, defaultSeconds, overrides))) {
            for (int i = 0; i < ids.size(); i++) {
                statement.setString(i + 1, ids.get(i));
            }
            return statement.executeLargeUpdate();
        }
    }

    /**
     * The keep-set statement (Core-Data-Model §13): reasons, newest seq per entity, head- and
     * alert-referenced seqs, and among the rest the lowest seq per entity and interval bucket. The
     * override source ids are {@code ?} parameters in the iteration order of {@code overrides}.
     */
    static String keepSetSql(String leaf, String copy, long from, long to, long defaultSeconds,
            Map<String, Long> overrides) {
        String columns = HistoryDdl.COLUMNS.stream().map(HistoryDdl.Column::name).collect(Collectors.joining(", "));
        String reasons = KEPT_REASONS.stream().map(r -> String.valueOf(r.code())).collect(Collectors.joining(", "));
        String intervalJoin = "";
        String interval = String.valueOf(Math.max(1, defaultSeconds));
        if (!overrides.isEmpty()) {
            StringJoiner values = new StringJoiner(", ", "(VALUES ", ")");
            overrides.values().forEach(seconds -> values.add("(CAST(? AS varchar), " + Math.max(1, seconds) + ")"));
            intervalJoin = " LEFT JOIN " + values + " AS iv(source_id, interval_s) ON iv.source_id = v.source_id";
            interval = "coalesce(iv.interval_s, " + interval + ")";
        }
        return "WITH pins AS ("
                + "SELECT seq FROM record_head WHERE seq >= " + from + " AND seq < " + to
                + " UNION SELECT seq FROM alert WHERE seq >= " + from + " AND seq < " + to
                + "), flagged AS ("
                + "SELECT v.*, (v.persist_reason IN (" + reasons + ")"
                + " OR v.seq = max(v.seq) OVER (PARTITION BY v.source_id, v.external_id)"
                + " OR p.seq IS NOT NULL) AS ds_pinned, " + interval + " AS ds_interval_s"
                + " FROM " + leaf + " v LEFT JOIN pins p ON p.seq = v.seq" + intervalJoin
                + "), ranked AS ("
                + "SELECT f.*, row_number() OVER (PARTITION BY f.ds_pinned, f.source_id, f.external_id,"
                + " floor(extract(epoch FROM f.observed_at) / f.ds_interval_s) ORDER BY f.seq) AS ds_rank"
                + " FROM flagged f) "
                + "INSERT INTO " + copy + " (" + columns + ") SELECT " + columns
                + " FROM ranked WHERE ds_pinned OR ds_rank = 1";
    }

    /** Step 4: the leaf indexes, built before ATTACH so it only links them to the parent's. */
    private static void createIndexes(Connection c, String copy) throws SQLException {
        execute(c, "ALTER TABLE " + copy + " ADD CONSTRAINT " + copy + "_pkey PRIMARY KEY (seq, history_tier)");
        execute(c, "CREATE INDEX " + copy + "_source_id_external_id_seq_idx ON " + copy + " (source_id, external_id, seq)");
        execute(c, "CREATE INDEX " + copy + "_source_id_observed_at_idx ON " + copy + " (source_id, observed_at)");
        execute(c, "CREATE INDEX " + copy + "_geom_idx ON " + copy + " USING GIST (geom)");
    }

    /** Renames the copy's indexes to the leaf's usual names; cosmetic, so failures are only logged. */
    private static void renameIndexes(Connection c, String name, String copy) {
        try {
            List<String> indexes = new ArrayList<>();
            try (PreparedStatement statement = c.prepareStatement(
                    "SELECT indexname FROM pg_indexes WHERE schemaname = current_schema() AND tablename = ?")) {
                statement.setString(1, name);
                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        indexes.add(rows.getString(1));
                    }
                }
            }
            for (String index : indexes) {
                if (!index.startsWith(copy + "_")) {
                    continue;
                }
                String renamed = name + index.substring(copy.length());
                if (index.equals(copy + "_pkey")) {
                    execute(c, "ALTER TABLE " + name + " RENAME CONSTRAINT " + index + " TO " + renamed);
                } else {
                    execute(c, "ALTER INDEX " + index + " RENAME TO " + renamed);
                }
            }
        } catch (SQLException e) {
            log.debug("Could not rename the indexes of {}; they keep their {} names", name, COPY_SUFFIX, e);
        }
    }

    // ---------------------------------------------------------------- recovery and housekeeping

    private int recoverOn(Connection c) throws SQLException {
        Map<String, long[]> running = new TreeMap<>();
        try (Statement statement = c.createStatement();
                ResultSet rows = statement.executeQuery(
                        "SELECT leaf_name, from_seq, to_seq FROM history_downsample WHERE status = 'RUNNING'")) {
            while (rows.next()) {
                running.put(rows.getString(1), new long[] {rows.getLong(2), rows.getLong(3)});
            }
        }
        for (String name : running.keySet()) {
            String copy = name + COPY_SUFFIX;
            boolean copyExists = exists(c, copy);
            if (copyExists && !attached(c, copy)) {
                dropQuietly(c, copy);
                markFailed(c, name, "interrupted before the swap; the copy was dropped");
                log.info("History downsampling recovery: dropped the unattached copy {}", copy);
            } else if (copyExists || hasBoundsConstraint(c, name)) {
                finish(c, name, copy);
                markDone(c, name, count(c, name));
                log.info("History downsampling recovery: finished the committed swap of {}", name);
            } else {
                markFailed(c, name, "interrupted; the leaf is intact");
            }
        }
        return running.size();
    }

    /**
     * Removes the rows of leaves retention has dropped (never a RUNNING row) and returns the DONE leaf
     * names still attached.
     */
    private Set<String> housekeeping(List<LeafInfo> leaves) {
        try {
            return partitions.exclusive(c -> {
                Array names = c.createArrayOf("varchar", leaves.stream().map(LeafInfo::name).toArray());
                try (PreparedStatement delete = c.prepareStatement("DELETE FROM history_downsample"
                        + " WHERE status <> 'RUNNING' AND NOT (leaf_name = ANY (?))")) {
                    delete.setArray(1, names);
                    delete.executeUpdate();
                }
                Set<String> done = new HashSet<>();
                try (Statement statement = c.createStatement();
                        ResultSet rows = statement.executeQuery(
                                "SELECT leaf_name FROM history_downsample WHERE status = 'DONE'")) {
                    while (rows.next()) {
                        done.add(rows.getString(1));
                    }
                }
                return done;
            });
        } catch (SQLException e) {
            throw new IllegalStateException("Could not read history_downsample: " + e.getMessage(), e);
        } finally {
            ranges.refresh();
        }
    }

    /** The per-source interval overrides of the TRACK sources, in seconds. */
    private Map<String, Long> overrides() {
        Map<String, Long> result = new TreeMap<>();
        for (SourceDescriptor descriptor : registry.descriptors()) {
            if (!descriptor.isTrack()) {
                continue;
            }
            try {
                sources.downsampleIntervalOverride(descriptor.id())
                        .ifPresent(interval -> result.put(descriptor.id(), interval.toSeconds()));
            } catch (RuntimeException e) {
                log.debug("Could not read the downsample interval of {}; using the default", descriptor.id(), e);
            }
        }
        return result;
    }

    // ---------------------------------------------------------------- bookkeeping rows

    private void upsertRunning(Connection c, LeafInfo leaf, long defaultSeconds) throws SQLException {
        try (PreparedStatement statement = c.prepareStatement(
                "INSERT INTO history_downsample (leaf_name, history_tier, from_seq, to_seq, default_interval_s,"
                        + " rows_before, rows_after, started_at, completed_at, status, error)"
                        + " VALUES (?, ?, ?, ?, ?, NULL, NULL, ?, NULL, 'RUNNING', NULL)"
                        + " ON CONFLICT (leaf_name) DO UPDATE SET history_tier = EXCLUDED.history_tier,"
                        + " from_seq = EXCLUDED.from_seq, to_seq = EXCLUDED.to_seq,"
                        + " default_interval_s = EXCLUDED.default_interval_s, rows_before = NULL, rows_after = NULL,"
                        + " started_at = EXCLUDED.started_at, completed_at = NULL, status = 'RUNNING', error = NULL")) {
            statement.setString(1, leaf.name());
            statement.setShort(2, HistoryTier.TRACK.code());
            statement.setLong(3, leaf.fromSeq());
            statement.setLong(4, leaf.toSeq());
            statement.setInt(5, (int) Math.min(Integer.MAX_VALUE, defaultSeconds));
            statement.setTimestamp(6, Timestamp.from(clock.instant()));
            statement.executeUpdate();
        }
    }

    private static void updateRowsBefore(Connection c, String name, long rows) throws SQLException {
        try (PreparedStatement statement = c.prepareStatement(
                "UPDATE history_downsample SET rows_before = ? WHERE leaf_name = ?")) {
            statement.setLong(1, rows);
            statement.setString(2, name);
            statement.executeUpdate();
        }
    }

    private void markDone(Connection c, String name, long rowsAfter) throws SQLException {
        try (PreparedStatement statement = c.prepareStatement("UPDATE history_downsample SET status = 'DONE',"
                + " rows_after = ?, completed_at = ?, error = NULL WHERE leaf_name = ?")) {
            statement.setLong(1, rowsAfter);
            statement.setTimestamp(2, Timestamp.from(clock.instant()));
            statement.setString(3, name);
            statement.executeUpdate();
        }
    }

    private void markFailed(Connection c, String name, String error) throws SQLException {
        try (PreparedStatement statement = c.prepareStatement("UPDATE history_downsample SET status = 'FAILED',"
                + " completed_at = ?, error = ? WHERE leaf_name = ?")) {
            statement.setTimestamp(1, Timestamp.from(clock.instant()));
            statement.setString(2, error == null ? "unknown error" : error);
            statement.setString(3, name);
            statement.executeUpdate();
        }
    }

    /**
     * After an unexpected failure: drops an unattached copy and marks the row FAILED. When the copy is
     * already attached (the swap committed and a later statement failed), the row stays RUNNING, so
     * the recovery at the start of the next run finishes the swap.
     */
    private void markFailedQuietly(String name, String error) {
        try {
            partitions.exclusive(c -> {
                if (attached(c, name + COPY_SUFFIX)) {
                    return null;
                }
                dropQuietly(c, name + COPY_SUFFIX);
                markFailed(c, name, error);
                return null;
            });
        } catch (SQLException | RuntimeException e) {
            log.debug("Could not mark the downsampling of {} FAILED", name, e);
        }
    }

    // ---------------------------------------------------------------- catalog helpers

    private static boolean exists(Connection c, String table) throws SQLException {
        try (PreparedStatement statement = c.prepareStatement("SELECT to_regclass(?) IS NOT NULL")) {
            statement.setString(1, table);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() && rows.getBoolean(1);
            }
        }
    }

    private static boolean attached(Connection c, String table) throws SQLException {
        try (PreparedStatement statement = c.prepareStatement("SELECT EXISTS (SELECT 1 FROM pg_inherits i"
                + " JOIN pg_class c ON c.oid = i.inhrelid JOIN pg_namespace n ON n.oid = c.relnamespace"
                + " WHERE n.nspname = current_schema() AND c.relname = ? AND NOT i.inhdetachpending)")) {
            statement.setString(1, table);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() && rows.getBoolean(1);
            }
        }
    }

    private static boolean hasBoundsConstraint(Connection c, String table) throws SQLException {
        try (PreparedStatement statement = c.prepareStatement("SELECT EXISTS (SELECT 1 FROM pg_constraint k"
                + " JOIN pg_class c ON c.oid = k.conrelid JOIN pg_namespace n ON n.oid = c.relnamespace"
                + " WHERE n.nspname = current_schema() AND c.relname = ? AND k.conname = ?)")) {
            statement.setString(1, table);
            statement.setString(2, BOUNDS_CONSTRAINT);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() && rows.getBoolean(1);
            }
        }
    }

    private static long count(Connection c, String table) throws SQLException {
        try (Statement statement = c.createStatement();
                ResultSet rows = statement.executeQuery("SELECT count(*) FROM " + table)) {
            rows.next();
            return rows.getLong(1);
        }
    }

    private static void dropQuietly(Connection c, String table) {
        try {
            if (!attached(c, table)) {
                execute(c, "DROP TABLE IF EXISTS " + table);
            }
        } catch (SQLException e) {
            log.warn("Could not drop the downsampling copy {}", table, e);
        }
    }

    /** Runs {@code sql}, retrying a lock timeout with backoff like the swap. */
    private void withLockRetry(Connection c, String sql) throws SQLException {
        for (int attempt = 0; ; attempt++) {
            try {
                execute(c, sql);
                return;
            } catch (SQLException e) {
                if (!LOCK_NOT_AVAILABLE.equals(e.getSQLState()) || attempt >= MAX_RETRIES) {
                    throw e;
                }
                pause(baseBackoffMillis << attempt);
            }
        }
    }

    private static void execute(Connection c, String sql) throws SQLException {
        try (Statement statement = c.createStatement()) {
            statement.execute(sql);
        }
    }

    private static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting to retry history downsampling", e);
        }
    }
}
