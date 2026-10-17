package ai.foresignal.stats;

import ai.foresignal.evidence.EvidenceResolver;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.ingest.IngestWriter;
import ai.foresignal.ingest.WriterStatus;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.reference.RegionTagWorker;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.store.query.SeqClockQuery;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import javax.sql.DataSource;
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
 * Fills the region rows of {@code activity_rollup} (prompt 119, Intelligence-Design §4.2): resolution 2
 * (region-hour) and 3 (region-day), with the cursor discipline of prompt 038's {@link RollupJob}.
 *
 * <ul>
 *   <li><b>Cursor:</b> {@code app_setting['stats.region.cursor']} ({@link RollupCursor}), written in the same
 *       transaction as the rows of each bucket, so nothing is lost or counted twice across a crash.
 *   <li><b>Closed buckets only:</b> an hour is rolled when {@link RollupJob#closed} says so and, while they run, the
 *       040 region tagger and the 116 evidence resolver have passed its last seq (so its records are tagged and its
 *       canonical links exist).
 *   <li><b>Days from raw rows:</b> a day is computed from its versions once its 24 hours are done, never by summing
 *       hours (distinct entities are not additive).
 *   <li><b>Catch-up:</b> at most {@code maxHoursPerRun} (6) hours per run; the first start backfills from the oldest
 *       retained version, bounded by {@code foresignal.stats.backfill}.
 *   <li><b>Consumer clamp:</b> {@link RegionRollupCursorConsumer} holds raw rows until they are rolled up.
 * </ul>
 *
 * <p>Runs with the activity rollup ({@code foresignal.stats.scheduled}); tests call {@link #runOnce()}.
 */
@Component
public class RegionRollupJob implements IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(RegionRollupJob.class);

    private final NamedParameterJdbcTemplate streaming;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final SeqClockQuery seqClock;
    private final AppSettingService settings;
    private final StatsSettingsService statsSettings;
    private final ObjectProvider<IngestWriter> writer;
    private final RollupJob rollup;
    private final StatsProperties properties;
    private final JsonMapper json;
    private final Clock clock;
    private final ObjectProvider<RegionTagWorker> tagger;
    private final ObjectProvider<EvidenceResolver> evidence;
    private final ObjectProvider<IntelVisibility> visibility;
    private final ReentrantLock lock = new ReentrantLock();

    private volatile RollupCursor cursor;

    /** Creates the job. */
    public RegionRollupJob(DataSource dataSource, JdbcTemplate jdbc, PlatformTransactionManager transactionManager,
            SeqClockQuery seqClock, AppSettingService settings, StatsSettingsService statsSettings,
            ObjectProvider<IngestWriter> writer, RollupJob rollup, StatsProperties properties, JsonMapper json,
            Clock clock, ObjectProvider<RegionTagWorker> tagger, ObjectProvider<EvidenceResolver> evidence,
            ObjectProvider<IntelVisibility> visibility) {
        JdbcTemplate fetching = new JdbcTemplate(dataSource);
        fetching.setFetchSize(RollupJob.FETCH_SIZE);
        this.streaming = new NamedParameterJdbcTemplate(fetching);
        this.jdbc = jdbc;
        this.transactions = new TransactionTemplate(transactionManager);
        this.transactions.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.seqClock = seqClock;
        this.settings = settings;
        this.statsSettings = statsSettings;
        this.writer = writer;
        this.rollup = rollup;
        this.properties = properties;
        this.json = json;
        this.clock = clock;
        this.tagger = tagger;
        this.evidence = evidence;
        this.visibility = visibility;
    }

    /**
     * What one run did.
     *
     * @param hours region hours rolled up
     * @param days region days rolled up
     * @param cursor the cursor afterwards
     */
    public record RunResult(int hours, int days, RollupCursor cursor) {}

    /** The scheduled run; does nothing unless {@code foresignal.stats.scheduled}. */
    @Scheduled(fixedDelayString = "${foresignal.stats.interval:5m}",
            initialDelayString = "${foresignal.stats.region-initial-delay:3m}")
    public void scheduled() {
        if (!properties.scheduled()) {
            return;
        }
        try {
            RunResult result = runOnce();
            if (result.hours() > 0 || result.days() > 0) {
                log.info("Region rollup: {} hour(s), {} day(s); cursor {}", result.hours(), result.days(),
                        result.cursor());
            }
        } catch (RuntimeException e) {
            log.error("The region rollup failed; it resumes from its cursor on the next run", e);
        }
    }

    /** Rolls up every closed region hour (at most {@code maxHoursPerRun}) and every completed region day. */
    public RunResult runOnce() {
        lock.lock();
        try {
            RollupCursor current = loadOrInit();
            Instant now = clock.instant();
            WriterStatus status = writerStatus();
            int hours = 0;
            int days = 0;
            while (hours < properties.maxHoursPerRun() && rollup.closed(current.hoursEnd(), now, status)
                    && consumersPassed(current.hoursEnd().plus(RegionRollupResolution.HOUR.width()))) {
                current = roll(current, RegionRollupResolution.HOUR, current.hoursEnd());
                hours++;
                while (!current.daysEnd().plus(RegionRollupResolution.DAY.width()).isAfter(current.hoursEnd())) {
                    current = roll(current, RegionRollupResolution.DAY, current.daysEnd());
                    days++;
                }
            }
            return new RunResult(hours, days, current);
        } finally {
            lock.unlock();
        }
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

    /** The sources rolled up at the admin-1 level ({@code stats.regionLevel}). */
    public Set<String> admin1Sources() {
        Set<String> out = new LinkedHashSet<>();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> value = settings.get(SettingKeys.STATS_REGION_LEVEL, Map.class).orElse(Map.of());
            value.forEach((source, level) -> {
                if ("admin1".equals(String.valueOf(level))) {
                    out.add(source);
                }
            });
        } catch (RuntimeException e) {
            log.warn("Ignoring an unreadable {} setting", SettingKeys.STATS_REGION_LEVEL);
        }
        return out;
    }

    /** True when the running region tagger and evidence resolver have processed every seq ingested before {@code end}. */
    boolean consumersPassed(Instant end) {
        long bound = Long.MAX_VALUE;
        RegionTagWorker regions = tagger.getIfAvailable();
        if (regions != null && regions.running()) {
            bound = Math.min(bound, regions.cursor());
        }
        EvidenceResolver resolver = evidence.getIfAvailable();
        if (resolver != null && resolver.running()) {
            bound = Math.min(bound, resolver.cursor());
        }
        if (bound == Long.MAX_VALUE) {
            return true;
        }
        long needed = seqClock.lastSeqBefore(streaming, end);
        return needed <= bound;
    }

    /** Computes and writes one bucket and advances the cursor, in one transaction. */
    private RollupCursor roll(RollupCursor current, RegionRollupResolution resolution, Instant bucket) {
        Set<String> admin1 = admin1Sources();
        IntelVisibility vis = visibility.getIfAvailable();
        RollupCursor next = transactions.execute(status -> {
            long lo = seqClock.lastSeqBefore(streaming, bucket);
            long hi = seqClock.lastSeqBefore(streaming, bucket.plus(resolution.width()));
            List<String> sources = statsSettings.rolledUpSources();
            RegionRollupComputer.write(streaming, resolution, bucket, sources, RegionRollupComputer.compute(streaming,
                    lo, hi, sources, admin1, id -> vis != null && vis.isPrivateSource(id)));
            Instant lastHour = resolution == RegionRollupResolution.HOUR ? bucket : current.lastHour();
            Instant lastDay = resolution == RegionRollupResolution.DAY ? bucket : current.lastDay();
            RollupCursor advanced = withSeq(lastHour, lastDay, current.since());
            settings.put(SettingKeys.STATS_REGION_CURSOR, advanced);
            return advanced;
        });
        cursor = next;
        return next;
    }

    private RollupCursor withSeq(Instant lastHour, Instant lastDay, Instant since) {
        Instant hoursEnd = lastHour.plus(RegionRollupResolution.HOUR.width());
        Instant daysEnd = lastDay.plus(RegionRollupResolution.DAY.width());
        Instant end = hoursEnd.isBefore(daysEnd) ? hoursEnd : daysEnd;
        return new RollupCursor(lastHour, lastDay, seqClock.lastSeqBefore(streaming, end), since);
    }

    private RollupCursor loadOrInit() {
        RollupCursor current = load();
        if (current != null) {
            cursor = current;
            return current;
        }
        RollupCursor initial = transactions.execute(status -> {
            Instant now = clock.instant();
            Instant start = RegionRollupResolution.HOUR.floor(now.minus(properties.backfill()));
            List<OffsetDateTime> oldest = jdbc.queryForList(RollupJob.OLDEST_VERSION, OffsetDateTime.class);
            if (oldest.isEmpty() || oldest.getFirst() == null) {
                start = RegionRollupResolution.HOUR.floor(now);
            } else {
                Instant first = RegionRollupResolution.HOUR.floor(oldest.getFirst().toInstant());
                if (first.isAfter(start)) {
                    start = first;
                }
            }
            Instant lastHour = start.minus(RegionRollupResolution.HOUR.width());
            Instant lastDay = RegionRollupResolution.DAY.floor(start).minus(RegionRollupResolution.DAY.width());
            RollupCursor value = withSeq(lastHour, lastDay, start);
            settings.put(SettingKeys.STATS_REGION_CURSOR, value);
            return value;
        });
        log.info("Region rollup starts at {} (backfill bound {})", initial.hoursEnd(), properties.backfill());
        cursor = initial;
        return initial;
    }

    private RollupCursor load() {
        List<String> values = jdbc.queryForList(RollupJob.READ_CURSOR, String.class, SettingKeys.STATS_REGION_CURSOR);
        if (values.isEmpty() || values.getFirst() == null) {
            return null;
        }
        try {
            RollupCursor value = json.readValue(values.getFirst(), RollupCursor.class);
            return value.lastHour() == null || value.lastDay() == null ? null : value;
        } catch (RuntimeException e) {
            log.warn("Ignoring an unreadable region rollup cursor '{}'", values.getFirst());
            return null;
        }
    }

    private WriterStatus writerStatus() {
        IngestWriter w = writer.getIfAvailable();
        return w == null ? null : w.status();
    }
}
