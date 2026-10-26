package ai.foresignal.retention;

import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.store.RetentionStateEntity;
import ai.foresignal.store.query.SeqClockQuery;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * The state of one retention run, shared by its {@link RetentionStep}s (prompt 019).
 *
 * <p>Holds the run's clock reading and settings, computes clamped cutoffs, and accumulates what the
 * steps did: deleted counts per table and per source, floors, purge candidates, skipped sources and
 * the consumer clamp. Used by one thread at a time (steps run sequentially).
 */
public final class RetentionRun {

    /** A (source, kind) whose versions are gone below {@code floorSeq}, for the head step. */
    public record PurgeCandidate(String sourceId, String kind, long floorSeq, Duration window) {}

    /** A consumer whose cursor fell behind {@code now − 2 × window}, releasing the clamp. */
    public record Lag(String consumer, long cursorSeq, Duration window) {}

    private final Instant now;
    private final RetentionSettings settings;
    private final NamedParameterJdbcTemplate jdbc;
    private final SeqClockQuery seqClock;
    private final List<RetentionConsumer> consumers;

    private final Map<String, Long> deletedByTable = new LinkedHashMap<>();
    private final Map<String, Long> versionsBySource = new TreeMap<>();
    private final Map<String, Map<String, Long>> floors = new TreeMap<>();
    private final List<PurgeCandidate> candidates = new ArrayList<>();
    private final Set<String> skipped = new TreeSet<>();
    private final Set<String> blockedLeaves = new TreeSet<>();
    private final Map<Class<?>, Object> memo = new java.util.HashMap<>();
    private final Map<String, Long> purged = new TreeMap<>();
    private final Map<String, Lag> lags = new LinkedHashMap<>();
    private final List<String> errors = new ArrayList<>();
    private Duration largestStandardWindow;
    private Map<String, Duration> sourceCaps = Map.of();
    private int partitionsDropped;
    private long headsPurged;
    private boolean clampActive;
    private String clampConsumer;
    private Long clampCursor;

    /**
     * Creates a run.
     *
     * @param now the run's single clock reading
     * @param settings the retention settings read at the start of the run
     * @param jdbc the template steps use; statements run outside any transaction (autocommit)
     * @param seqClock the {@code seq_clock} translator
     * @param consumers the registered consumers
     */
    public RetentionRun(Instant now, RetentionSettings settings, NamedParameterJdbcTemplate jdbc,
            SeqClockQuery seqClock, List<RetentionConsumer> consumers) {
        this.now = now;
        this.settings = settings;
        this.jdbc = jdbc;
        this.seqClock = seqClock;
        this.consumers = List.copyOf(consumers);
    }

    /** The run's clock reading. */
    public Instant now() {
        return now;
    }

    /** The retention settings of this run. */
    public RetentionSettings settings() {
        return settings;
    }

    /**
     * Caps the STANDARD windows of some sources for this run only (prompt R2-016: an archive run moves the cutoff of
     * the sources it archives, and of no other). Set before any step plans; a window is only ever shortened.
     *
     * @param caps the cap per source id
     */
    public void capSources(Map<String, Duration> caps) {
        this.sourceCaps = caps == null ? Map.of() : Map.copyOf(caps);
    }

    /**
     * The run-only cap of {@code sourceId}'s STANDARD windows.
     *
     * @param sourceId the source
     * @return the cap, or null when the source is not capped this run
     */
    public Duration sourceCap(String sourceId) {
        return sourceCaps.get(sourceId);
    }

    /** The template; every statement autocommits. */
    public NamedParameterJdbcTemplate jdbc() {
        return jdbc;
    }

    /**
     * {@code cutoffSeq(now − window)} clamped by the consumers: every seq below the result may be
     * deleted. A consumer whose cursor is older than {@code now − 2 × window} does not clamp; it is
     * recorded as lagging.
     */
    public long cutoff(Duration window) {
        return clamp(rawCutoff(window), window);
    }

    /** {@code cutoffSeq(now − window)} before the consumer clamp. */
    public long rawCutoff(Duration window) {
        return seqClock.cutoffSeq(jdbc, now.minus(window));
    }

    /**
     * Applies the consumer clamp to {@code raw}, a cutoff computed for {@code window}.
     *
     * @return {@code min(raw, min(cursor) + 1)} over the consumers that are not lagging
     */
    public long clamp(long raw, Duration window) {
        long cutoff = raw;
        long releaseBound = -1;
        for (RetentionConsumer consumer : consumers) {
            OptionalLong cursor;
            try {
                cursor = consumer.cursorSeq();
            } catch (RuntimeException e) {
                continue;
            }
            if (cursor.isEmpty() || cursor.getAsLong() + 1 >= raw) {
                continue;
            }
            long seq = cursor.getAsLong();
            if (releaseBound < 0) {
                releaseBound = seqClock.cutoffSeq(jdbc, now.minus(window.multipliedBy(2)));
            }
            if (seq < releaseBound) {
                lags.putIfAbsent(consumer.name(), new Lag(consumer.name(), seq, window));
                continue;
            }
            if (seq + 1 < cutoff) {
                cutoff = seq + 1;
                clampActive = true;
                if (clampCursor == null || seq < clampCursor) {
                    clampCursor = seq;
                    clampConsumer = consumer.name();
                }
            }
        }
        return Math.max(0L, cutoff);
    }

    /**
     * {@code min(cursor) + 1} over the consumers that report a cursor, without the lag release of
     * {@link #clamp}: every seq below the result has been processed by every such consumer. History
     * downsampling (prompt 044) rewrites only below it, so live triggers never see a thinned range.
     * {@link Long#MAX_VALUE} when no consumer reports a cursor.
     */
    public long consumerBound() {
        long bound = Long.MAX_VALUE;
        for (RetentionConsumer consumer : consumers) {
            OptionalLong cursor;
            try {
                cursor = consumer.cursorSeq();
            } catch (RuntimeException e) {
                return 0L;
            }
            if (cursor.isPresent()) {
                bound = Math.min(bound, Math.max(0L, cursor.getAsLong() + 1));
            }
        }
        return bound;
    }

    /** Adds {@code count} to the deleted rows of {@code table}. */
    public void deleted(String table, long count) {
        deletedByTable.merge(table, count, Long::sum);
    }

    /** Deleted rows per table. */
    public Map<String, Long> deletedByTable() {
        return Collections.unmodifiableMap(deletedByTable);
    }

    /** Deleted rows of {@code table}; 0 when none. */
    public long deletedFrom(String table) {
        return deletedByTable.getOrDefault(table, 0L);
    }

    /** Adds deleted versions of {@code sourceId}. */
    public void versionsDeleted(String sourceId, long count) {
        versionsBySource.merge(sourceId, count, Long::sum);
        deleted("record_version", count);
    }

    /** Deleted versions per source. */
    public Map<String, Long> versionsBySource() {
        return Collections.unmodifiableMap(versionsBySource);
    }

    /**
     * Records that every version of {@code sourceId} (and {@code kind}, or
     * {@link RetentionStateEntity#ALL_KINDS}) below {@code floorSeq} is gone.
     */
    public void floor(String sourceId, String kind, long floorSeq) {
        floors.computeIfAbsent(sourceId, id -> new TreeMap<>())
                .merge(kind == null ? RetentionStateEntity.ALL_KINDS : kind, floorSeq, Math::max);
    }

    /** The floors recorded this run: source → kind → floor. */
    public Map<String, Map<String, Long>> floors() {
        return Collections.unmodifiableMap(floors);
    }

    /** Adds a head-purge candidate. */
    public void candidate(PurgeCandidate candidate) {
        candidates.add(candidate);
    }

    /** The head-purge candidates, in the order they were added. */
    public List<PurgeCandidate> candidates() {
        return List.copyOf(candidates);
    }

    /** Marks {@code sourceId} skipped: a delete failed, so no HeadPurge for it this run. */
    public void skip(String sourceId) {
        skipped.add(sourceId);
    }

    /** True when {@code sourceId} was skipped. */
    public boolean skipped(String sourceId) {
        return skipped.contains(sourceId);
    }

    /** The skipped sources, ordered. */
    public Set<String> skippedSources() {
        return Collections.unmodifiableSet(skipped);
    }

    /**
     * Keeps the TRACK leaf {@code leafName} this run (prompt 111): a hold copy or a cold-archive write
     * of rows in it failed, so it must not be dropped before they are written out. The TRACK step
     * stops dropping at the first blocked leaf.
     */
    public void blockLeaf(String leafName) {
        blockedLeaves.add(leafName);
    }

    /** True when {@code leafName} was blocked this run. */
    public boolean leafBlocked(String leafName) {
        return blockedLeaves.contains(leafName);
    }

    /** The blocked TRACK leaves, ordered. */
    public Set<String> blockedLeaves() {
        return Collections.unmodifiableSet(blockedLeaves);
    }

    /**
     * The value of {@code type} computed once per run (prompt 111): the first call runs
     * {@code compute}, later calls return the same value. {@link RetentionPlanner} keeps its plans here
     * so the copy, archive and delete steps of one run act on exactly the same cutoffs and leaves.
     */
    public <T> T memo(Class<T> type, java.util.function.Supplier<T> compute) {
        Object value = memo.get(type);
        if (value == null) {
            value = compute.get();
            memo.put(type, value);
        }
        return type.cast(value);
    }

    /** Raises the largest STANDARD window seen this run to at least {@code window}. */
    public void standardWindow(Duration window) {
        if (largestStandardWindow == null || window.compareTo(largestStandardWindow) > 0) {
            largestStandardWindow = window;
        }
    }

    /** The largest STANDARD window seen, or {@code retention.default} when none was. */
    public Duration largestStandardWindow() {
        return largestStandardWindow == null ? settings.defaultRetention() : largestStandardWindow;
    }

    /** Counts a dropped partition. */
    public void partitionDropped() {
        partitionsDropped++;
    }

    /** Partitions dropped this run. */
    public int partitionsDropped() {
        return partitionsDropped;
    }

    /** Records that a head purge removed {@code count} heads of {@code sourceId} at {@code watermarkSeq}. */
    public void headsPurged(String sourceId, long count, long watermarkSeq) {
        headsPurged += count;
        if (count > 0) {
            purged.merge(sourceId, watermarkSeq, Math::max);
        }
    }

    /** Heads purged this run. */
    public long headsPurged() {
        return headsPurged;
    }

    /** Sources whose heads were purged, with the watermark to record as their {@code purgeSeq}. */
    public Map<String, Long> purgedSources() {
        return Collections.unmodifiableMap(purged);
    }

    /** Consumers that released the clamp this run. */
    public List<Lag> lags() {
        return List.copyOf(lags.values());
    }

    /** True when some cutoff was held back by a consumer. */
    public boolean clampActive() {
        return clampActive;
    }

    /** The consumer that held a cutoff back furthest, or null. */
    public String clampConsumer() {
        return clampConsumer;
    }

    /** That consumer's cursor, or null. */
    public Long clampCursor() {
        return clampCursor;
    }

    /** Records a step failure. */
    public void error(String message) {
        errors.add(message);
    }

    /** Step failures, in order. */
    public List<String> errors() {
        return List.copyOf(errors);
    }
}
