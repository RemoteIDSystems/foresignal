package ai.foresignal.stats;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.ingest.IngestWriter;
import ai.foresignal.ingest.WriterState;
import ai.foresignal.ingest.WriterStatus;
import ai.foresignal.retention.RetentionState;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.store.query.SeqClockQuery;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import javax.sql.DataSource;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Fills {@code activity_rollup} from raw history behind a durable seq cursor (prompt 038), never from
 * an in-memory listener, so a crash never makes counts drift.
 *
 * <ul>
 *   <li><b>Cursor:</b> {@code app_setting['stats.rollup.cursor'] = {lastHour, lastDay, seq}}
 *       ({@link RollupCursor}), written in the same transaction as the rows of each bucket.
 *   <li><b>Closed hours:</b> hour {@code H} is closed once {@code now ≥ H + 1 h + closeDelay} (2 min)
 *       and the writer can no longer commit a version stamped inside it: its last ingested time has
 *       passed {@code H + 1 h}, or it has been quiet for {@value #QUIET_MINUTES} minutes after the hour
 *       (a batch takes its {@code ingested_at} when its transaction begins). Nothing closes while the
 *       writer's clock guard holds batches ({@code CLOCK_BEHIND}).
 *   <li><b>Buckets:</b> hour {@code H} covers {@code seq ∈ (S(H⁻), S((H + 1 h)⁻)]}; one streamed pass
 *       aggregates it ({@link RollupComputer}). Days are computed the same way from raw rows once their
 *       24 hours are done, because distinct entities are not additive.
 *   <li><b>First start:</b> backfills from the oldest retained version, at most
 *       {@code foresignal.stats.backfill} (30 d) back; catches up at most {@code maxHoursPerRun} (6)
 *       hours per run.
 *   <li><b>Rebuild</b> ({@code POST /v1/stats/rollup/rebuild}): recomputes closed buckets, per source,
 *       only where that source's raw versions are all still retained.
 * </ul>
 *
 * <p>Runs every {@code foresignal.stats.interval} (5 min) when {@code foresignal.stats.scheduled} is
 * true; tests call {@link #runOnce()}.
 */
@Component
public class RollupJob implements IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(RollupJob.class);

    /** Minutes after an hour's end after which a quiet writer closes it. */
    public static final int QUIET_MINUTES = 10;

    /** The fetch size of the streamed bucket scan. */
    static final int FETCH_SIZE = 5_000;

    static final String READ_CURSOR = "SELECT value::text FROM app_setting WHERE key = ?";

    static final String OLDEST_VERSION = "SELECT ingested_at FROM record_version ORDER BY seq LIMIT 1";

    private final NamedParameterJdbcTemplate streaming;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final SeqClockQuery seqClock;
    private final AppSettingService settings;
    private final StatsSettingsService statsSettings;
    private final RetentionState retention;
    private final ObjectProvider<IngestWriter> writer;
    private final RollupComputer computer;
    private final StatsProperties properties;
    private final JsonMapper json;
    private final Clock clock;
    private final ReentrantLock lock = new ReentrantLock();
    private final ExecutorService rebuilds = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "stats-rollup-rebuild");
        t.setDaemon(true);
        return t;
    });

    private volatile RollupCursor cursor;

    /** Creates the job. */
    public RollupJob(DataSource dataSource, JdbcTemplate jdbc, PlatformTransactionManager transactionManager,
            SeqClockQuery seqClock, AppSettingService settings, StatsSettingsService statsSettings,
            RetentionState retention, ObjectProvider<IngestWriter> writer, H3Cells cells, StatsProperties properties,
            JsonMapper json, Clock clock) {
        JdbcTemplate fetching = new JdbcTemplate(dataSource);
        fetching.setFetchSize(FETCH_SIZE);
        this.streaming = new NamedParameterJdbcTemplate(fetching);
        this.jdbc = jdbc;
        this.transactions = new TransactionTemplate(transactionManager);
        this.transactions.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.seqClock = seqClock;
        this.settings = settings;
        this.statsSettings = statsSettings;
        this.retention = retention;
        this.writer = writer;
        this.computer = new RollupComputer(cells, properties.cellRes());
        this.properties = properties;
        this.json = json;
        this.clock = clock;
    }

    /**
     * What one run did.
     *
     * @param hours hours rolled up
     * @param days days rolled up
     * @param cursor the cursor afterwards, or null when there is none
     */
    public record RunResult(int hours, int days, RollupCursor cursor) {}

    /** The scheduled run; does nothing unless {@code foresignal.stats.scheduled}. */
    @Scheduled(fixedDelayString = "${foresignal.stats.interval:5m}", initialDelayString = "${foresignal.stats.initial-delay:2m}")
    public void scheduled() {
        if (!properties.scheduled()) {
            return;
        }
        try {
            RunResult result = runOnce();
            if (result.hours() > 0 || result.days() > 0) {
                log.info("Rolled up {} hour(s) and {} day(s); cursor {}", result.hours(), result.days(), result.cursor());
            }
        } catch (RuntimeException e) {
            log.error("The activity rollup failed; it resumes from its cursor on the next run", e);
        }
    }

    /** Rolls up every closed hour (at most {@code maxHoursPerRun}) and every completed day. */
    public RunResult runOnce() {
        lock.lock();
        try {
            RollupCursor current = loadOrInit();
            Instant now = clock.instant();
            WriterStatus status = writerStatus();
            int hours = 0;
            int days = 0;
            while (hours < properties.maxHoursPerRun() && closed(current.hoursEnd(), now, status)) {
                current = rollHour(current, current.hoursEnd());
                hours++;
                while (!current.daysEnd().plus(RollupResolution.DAY.width()).isAfter(current.hoursEnd())) {
                    current = rollDay(current, current.daysEnd());
                    days++;
                }
            }
            return new RunResult(hours, days, current);
        } finally {
            lock.unlock();
        }
    }

    /**
     * True when hour {@code [start, start + 1 h)} is closed at {@code now} given the writer's state.
     */
    public boolean closed(Instant start, Instant now, WriterStatus status) {
        Instant end = start.plus(RollupResolution.HOUR.width());
        if (now.isBefore(end.plus(properties.closeDelay()))) {
            return false;
        }
        if (status == null) {
            return true;
        }
        if (status.state() == WriterState.CLOCK_BEHIND) {
            return false;
        }
        Instant last = status.lastIngestedAt();
        return last == null || !last.isBefore(end) || !now.isBefore(end.plus(Duration.ofMinutes(QUIET_MINUTES)));
    }

    /** The cursor, or null before the first run. */
    public RollupCursor cursor() {
        RollupCursor value = cursor;
        if (value == null) {
            value = load();
            cursor = value;
        }
        return value;
    }

    /** Reloads the cursor (the test cleaner truncates {@code app_setting}). */
    @Override
    public void resetState() {
        cursor = load();
    }

    // ------------------------------------------------------------------------- rebuild

    /**
     * Plans a rebuild of the closed buckets in {@code [from, to)} and queues it.
     *
     * @return the plan, {@code status = queued}
     * @throws ValidationException when the range is empty
     */
    public RebuildPlan rebuild(Instant from, Instant to) {
        RebuildPlan plan = plan(from, to);
        rebuilds.submit(() -> {
            try {
                execute(plan);
            } catch (RuntimeException e) {
                log.error("The rollup rebuild of {} to {} failed", from, to, e);
            }
        });
        return plan;
    }

    /** Plans and runs a rebuild on the calling thread (tests). */
    public RebuildPlan rebuildNow(Instant from, Instant to) {
        RebuildPlan plan = plan(from, to);
        execute(plan);
        return plan;
    }

    /**
     * A rebuild: the closed hours and days in range.
     *
     * @param from the requested start
     * @param to the requested end
     * @param hours hour starts to recompute
     * @param days day starts to recompute
     */
    public record RebuildPlan(Instant from, Instant to, List<Instant> hours, List<Instant> days) {}

    RebuildPlan plan(Instant from, Instant to) {
        if (from == null || to == null) {
            throw ValidationException.forField(from == null ? "from" : "to", "is required");
        }
        if (!from.isBefore(to)) {
            throw ValidationException.forField("to", "must be after from");
        }
        RollupCursor c = cursor();
        List<Instant> hours = new ArrayList<>();
        List<Instant> days = new ArrayList<>();
        if (c != null) {
            for (Instant h = later(RollupResolution.HOUR.floor(from), c.start(RollupResolution.HOUR));
                    h.isBefore(to) && h.isBefore(c.hoursEnd());
                    h = h.plus(RollupResolution.HOUR.width())) {
                hours.add(h);
            }
            for (Instant d = later(RollupResolution.DAY.floor(from), c.start(RollupResolution.DAY));
                    d.isBefore(to) && d.isBefore(c.daysEnd());
                    d = d.plus(RollupResolution.DAY.width())) {
                days.add(d);
            }
        }
        return new RebuildPlan(from, to, List.copyOf(hours), List.copyOf(days));
    }

    private static Instant later(Instant a, Instant b) {
        return a.isAfter(b) ? a : b;
    }

    private void execute(RebuildPlan plan) {
        lock.lock();
        try {
            for (Instant h : plan.hours()) {
                recompute(RollupResolution.HOUR, h);
            }
            for (Instant d : plan.days()) {
                recompute(RollupResolution.DAY, d);
            }
        } finally {
            lock.unlock();
        }
    }

    /** Recomputes one closed bucket for the sources whose raw versions in it are all retained. */
    private void recompute(RollupResolution resolution, Instant bucket) {
        transactions.executeWithoutResult(status -> {
            NamedParameterJdbcTemplate t = streaming;
            long lo = seqClock.lastSeqBefore(t, bucket);
            long hi = seqClock.lastSeqBefore(t, bucket.plus(resolution.width()));
            List<String> sources = new ArrayList<>();
            for (String id : statsSettings.rolledUpSources()) {
                if (lo + 1 >= retention.lowestRetainedSeq(Set.of(id))) {
                    sources.add(id);
                }
            }
            RollupComputer.write(t, resolution, bucket, sources, computer.compute(t, lo, hi, sources));
        });
    }

    @PreDestroy
    void shutdown() {
        rebuilds.shutdownNow();
    }

    // ------------------------------------------------------------------------- buckets

    private RollupCursor rollHour(RollupCursor current, Instant hour) {
        return roll(current, RollupResolution.HOUR, hour);
    }

    private RollupCursor rollDay(RollupCursor current, Instant day) {
        return roll(current, RollupResolution.DAY, day);
    }

    /** Computes and writes one bucket and advances the cursor, in one transaction. */
    private RollupCursor roll(RollupCursor current, RollupResolution resolution, Instant bucket) {
        RollupCursor next = transactions.execute(status -> {
            NamedParameterJdbcTemplate t = streaming;
            long lo = seqClock.lastSeqBefore(t, bucket);
            long hi = seqClock.lastSeqBefore(t, bucket.plus(resolution.width()));
            List<String> sources = statsSettings.rolledUpSources();
            RollupComputer.write(t, resolution, bucket, sources, computer.compute(t, lo, hi, sources));
            Instant lastHour = resolution == RollupResolution.HOUR ? bucket : current.lastHour();
            Instant lastDay = resolution == RollupResolution.DAY ? bucket : current.lastDay();
            RollupCursor advanced = withSeq(t, lastHour, lastDay, current.since());
            settings.put(SettingKeys.STATS_ROLLUP_CURSOR, advanced);
            return advanced;
        });
        cursor = next;
        return next;
    }

    /** The cursor with {@code seq} = the last seq ingested before both the hour and the day ends. */
    private RollupCursor withSeq(NamedParameterJdbcTemplate t, Instant lastHour, Instant lastDay, Instant since) {
        Instant hoursEnd = lastHour.plus(RollupResolution.HOUR.width());
        Instant daysEnd = lastDay.plus(RollupResolution.DAY.width());
        Instant end = hoursEnd.isBefore(daysEnd) ? hoursEnd : daysEnd;
        return new RollupCursor(lastHour, lastDay, seqClock.lastSeqBefore(t, end), since);
    }

    // ------------------------------------------------------------------------- cursor

    private RollupCursor loadOrInit() {
        RollupCursor current = load();
        if (current != null) {
            cursor = current;
            return current;
        }
        RollupCursor initial = transactions.execute(status -> {
            Instant now = clock.instant();
            Instant start = RollupResolution.HOUR.floor(now.minus(properties.backfill()));
            List<OffsetDateTime> oldest = jdbc.queryForList(OLDEST_VERSION, OffsetDateTime.class);
            if (oldest.isEmpty() || oldest.getFirst() == null) {
                start = RollupResolution.HOUR.floor(now);
            } else {
                Instant first = RollupResolution.HOUR.floor(oldest.getFirst().toInstant());
                if (first.isAfter(start)) {
                    start = first;
                }
            }
            Instant lastHour = start.minus(RollupResolution.HOUR.width());
            Instant lastDay = RollupResolution.DAY.floor(start).minus(RollupResolution.DAY.width());
            RollupCursor value = withSeq(streaming, lastHour, lastDay, start);
            settings.put(SettingKeys.STATS_ROLLUP_CURSOR, value);
            return value;
        });
        log.info("Activity rollup starts at {} (backfill bound {})", initial.hoursEnd(), properties.backfill());
        cursor = initial;
        return initial;
    }

    private RollupCursor load() {
        List<String> values = jdbc.queryForList(READ_CURSOR, String.class, SettingKeys.STATS_ROLLUP_CURSOR);
        if (values.isEmpty() || values.getFirst() == null) {
            return null;
        }
        try {
            RollupCursor value = json.readValue(values.getFirst(), RollupCursor.class);
            return value.lastHour() == null || value.lastDay() == null ? null : value;
        } catch (RuntimeException e) {
            log.warn("Ignoring an unreadable rollup cursor '{}'", values.getFirst());
            return null;
        }
    }

    private WriterStatus writerStatus() {
        IngestWriter w = writer.getIfAvailable();
        return w == null ? null : w.status();
    }
}
