package ai.foresignal.retention;

import ai.foresignal.config.StartupOrder;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.store.RetentionStateEntity;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * The real {@link RetentionState} (prompt 019), replacing prompt 014's default bean.
 *
 * <p>Backed by the {@code retention_state} table: loaded at startup, rewritten and republished after
 * every {@link RetentionJob} run. Readers see an immutable snapshot swapped in one volatile write.
 *
 * <ul>
 *   <li>{@code floorSeq(source)}: the {@link RetentionStateEntity#ALL_KINDS} row's floor, which a run
 *       sets to the minimum over the source's kinds; 0 when retention never ran for it.
 *   <li>{@link #lowestRetainedSeq(Set)}: the largest {@code floorSeq} over the set (every source with
 *       a row when the set is empty). A replay from {@code after} is complete only when
 *       {@code after ≥} that value; prompt 015 sends {@code reset} below it.
 *   <li>{@link #purgeSeq(String)}: advanced only when a head purge removed at least one head of the
 *       source, to {@code ChangeBus.lastPublishedSeq()} read when the purge completed. Version
 *       deletes and partition drops never advance it (Conventions "RetentionState.purgeSeq"), so
 *       prompt 014's {@code 410 RESYNC_REQUIRED} fires only for cursors that may hold a purged head.
 *   <li>{@link #window(String)}: {@link RetentionResolver#shortestWindow(String)}.
 * </ul>
 */
@Component
public class RetentionStateStore implements RetentionState, IngestStateReset {

    static final String UPSERT_FLOOR = """
            INSERT INTO retention_state (source_id, kind, floor_seq, purge_seq, purged_at, last_run_at)
            VALUES (?, ?, ?, 0, NULL, ?)
            ON CONFLICT (source_id, kind) DO UPDATE
               SET floor_seq = greatest(retention_state.floor_seq, excluded.floor_seq),
                   last_run_at = excluded.last_run_at
            """;

    static final String UPSERT_PURGE = """
            INSERT INTO retention_state (source_id, kind, floor_seq, purge_seq, purged_at, last_run_at)
            VALUES (?, ?, 0, ?, ?, ?)
            ON CONFLICT (source_id, kind) DO UPDATE
               SET purge_seq = greatest(retention_state.purge_seq, excluded.purge_seq),
                   purged_at = excluded.purged_at
            """;

    /** One source's published state. */
    record Entry(long floorSeq, long purgeSeq) {}

    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final RetentionResolver resolver;

    private volatile Map<String, Entry> snapshot;

    /** Creates the store; the snapshot is loaded on first use and at startup. */
    public RetentionStateStore(JdbcTemplate jdbc, TransactionTemplate tx, RetentionResolver resolver) {
        this.jdbc = jdbc;
        this.tx = tx;
        this.resolver = resolver;
    }

    /** Loads the snapshot once the schema exists. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.RETENTION_STATE)
    public void onApplicationReady() {
        reload();
    }

    /** Re-reads the table (the test cleaner truncates it between methods). */
    @Override
    public void resetState() {
        reload();
    }

    /** Re-reads {@code retention_state} into a fresh snapshot. */
    public synchronized void reload() {
        Map<String, Long> floors = new HashMap<>();
        Map<String, Long> purges = new HashMap<>();
        jdbc.query("SELECT source_id, kind, floor_seq, purge_seq FROM retention_state", rs -> {
            String source = rs.getString(1);
            String kind = rs.getString(2);
            long floor = rs.getLong(3);
            long purge = rs.getLong(4);
            if (RetentionStateEntity.ALL_KINDS.equals(kind)) {
                floors.put(source, floor);
                purges.merge(source, purge, Math::max);
            } else {
                // A source with per-kind rows but no source row (an interrupted write) uses the lowest.
                floors.merge(source, floor, Math::min);
            }
        });
        Map<String, Entry> next = new HashMap<>();
        for (String source : floors.keySet()) {
            next.put(source, new Entry(floors.get(source), purges.getOrDefault(source, 0L)));
        }
        for (Map.Entry<String, Long> purge : purges.entrySet()) {
            next.putIfAbsent(purge.getKey(), new Entry(0L, purge.getValue()));
        }
        snapshot = Collections.unmodifiableMap(next);
    }

    private Map<String, Entry> current() {
        Map<String, Entry> value = snapshot;
        if (value == null) {
            reload();
            value = snapshot;
        }
        return value;
    }

    @Override
    public long purgeSeq(String sourceId) {
        Entry entry = current().get(sourceId);
        return entry == null ? 0L : entry.purgeSeq();
    }

    /**
     * Every source with a recorded floor and that floor (prompt 115: an exercise replay names the source whose
     * retained history begins after the requested window start).
     */
    public Map<String, Long> floors() {
        Map<String, Long> out = new java.util.TreeMap<>();
        for (Map.Entry<String, Entry> entry : current().entrySet()) {
            if (entry.getValue().floorSeq() > 0) {
                out.put(entry.getKey(), entry.getValue().floorSeq());
            }
        }
        return out;
    }

    /** The source's floor: every version of it below this seq is gone; 0 when unknown. */
    public long floorSeq(String sourceId) {
        Entry entry = current().get(sourceId);
        return entry == null ? 0L : entry.floorSeq();
    }

    @Override
    public long lowestRetainedSeq(Set<String> sources) {
        Map<String, Entry> state = current();
        long lowest = 0L;
        if (sources == null || sources.isEmpty()) {
            for (Entry entry : state.values()) {
                lowest = Math.max(lowest, entry.floorSeq());
            }
            return lowest;
        }
        for (String source : sources) {
            Entry entry = state.get(source);
            if (entry != null) {
                lowest = Math.max(lowest, entry.floorSeq());
            }
        }
        return lowest;
    }

    @Override
    public Duration window(String sourceId) {
        return resolver.shortestWindow(sourceId);
    }

    /**
     * Writes what {@code run} did and republishes: per-kind floors and the source floor (the minimum
     * over everything the run recorded for the source, including its other-kinds group), never
     * lowered, and the purge seqs of sources whose heads were purged.
     */
    public void publish(RetentionRun run) {
        Instant now = run.now();
        Timestamp at = Timestamp.from(now);
        tx.executeWithoutResult(status -> {
            for (Map.Entry<String, Map<String, Long>> source : run.floors().entrySet()) {
                long sourceFloor = Long.MAX_VALUE;
                for (Map.Entry<String, Long> kind : source.getValue().entrySet()) {
                    if (!RetentionStateEntity.ALL_KINDS.equals(kind.getKey())) {
                        jdbc.update(UPSERT_FLOOR, source.getKey(), kind.getKey(), kind.getValue(), at);
                    }
                    sourceFloor = Math.min(sourceFloor, kind.getValue());
                }
                jdbc.update(UPSERT_FLOOR, source.getKey(), RetentionStateEntity.ALL_KINDS, sourceFloor, at);
            }
            for (Map.Entry<String, Long> purge : run.purgedSources().entrySet()) {
                jdbc.update(UPSERT_PURGE, purge.getKey(), RetentionStateEntity.ALL_KINDS, purge.getValue(), at, at);
            }
        });
        reload();
    }

    /** The per-kind floor stored for {@code sourceId} and {@code kind}, or 0 (the next run's lower bound). */
    public long storedFloor(String sourceId, String kind) {
        java.util.List<Long> rows = jdbc.queryForList(
                "SELECT floor_seq FROM retention_state WHERE source_id = ? AND kind = ?", Long.class,
                sourceId, kind == null ? RetentionStateEntity.ALL_KINDS : kind);
        return rows.isEmpty() || rows.getFirst() == null ? 0L : rows.getFirst();
    }
}
