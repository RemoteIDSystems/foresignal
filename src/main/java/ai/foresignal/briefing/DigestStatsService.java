package ai.foresignal.briefing;

import ai.foresignal.api.mapper.AlertMapper;
import ai.foresignal.collect.HealthService;
import ai.foresignal.collect.SourceHealthView;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.retention.RetentionState;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.store.query.ReadQueryRunner;
import ai.foresignal.store.query.SeqClockQuery;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Computes {@link DigestStats} for briefing runs and {@code GET /v1/digest} (prompt 029,
 * Core-Data-Model §8, §12, §13).
 *
 * <ul>
 *   <li><b>Seq bounds:</b> {@code toSeq = ChangeBus.lastPublishedSeq()} is read before any query;
 *       {@code fromSeq} is the last seq ingested before {@code periodFrom} ({@code S(periodFrom⁻)} through
 *       {@code seq_clock}, exact to the batch, where the minute-granular {@code cutoffSeq} would count
 *       the rest of the cutoff minute) for a period, or {@code sinceSeq} for a cursor.
 *       {@code gap} is true when {@code fromSeq < RetentionState.lowestRetainedSeq(scope sources)}.
 *   <li><b>Delta:</b> the previous equal-length period ({@code S((fromTime − length)⁻)} to
 *       {@code fromSeq}); null when it reaches below the retention floor.
 *   <li><b>Alerts</b> are keyed on alert id: {@code sinceAlertId < id <= untilAlertId}, where
 *       {@code untilAlertId} is the largest id when the bounds were read.
 *   <li><b>Health:</b> Foresignal stores no health transitions, so the section is the current
 *       snapshot of degraded and failing sources ({@code healthSnapshot = true}).
 *   <li><b>Private sources</b> ({@code hiddenSources}) appear in counts only: their records, alerts
 *       and health are not listed.
 * </ul>
 */
@Service
public class DigestStatsService {

    private static final Logger log = LoggerFactory.getLogger(DigestStatsService.class);

    /** The most alerts listed. */
    public static final int MAX_ALERT_ITEMS = 20;

    /** The default catch-up window of {@code /v1/digest}. */
    public static final Duration DEFAULT_WINDOW = Duration.ofHours(24);

    private final ReadQueryRunner runner;
    private final SeqClockQuery seqClock;
    private final ChangeBus changeBus;
    private final RetentionState retention;
    private final DigestStatsProvider provider;
    private final HealthService health;
    private final AlertStore alerts;
    private final AlertMapper alertMapper;
    private final BriefingLinks links;
    private final SourceRegistry registry;
    private final ObjectProvider<BriefingSectionContributor> contributors;
    private final Clock clock;

    /** Creates the service. */
    public DigestStatsService(ReadQueryRunner runner, SeqClockQuery seqClock, ChangeBus changeBus,
            RetentionState retention, DigestStatsProvider provider, HealthService health, AlertStore alerts,
            AlertMapper alertMapper, BriefingLinks links, SourceRegistry registry,
            ObjectProvider<BriefingSectionContributor> contributors, Clock clock) {
        this.runner = runner;
        this.seqClock = seqClock;
        this.changeBus = changeBus;
        this.retention = retention;
        this.provider = provider;
        this.health = health;
        this.alerts = alerts;
        this.alertMapper = alertMapper;
        this.links = links;
        this.registry = registry;
        this.contributors = contributors;
        this.clock = clock;
    }

    /**
     * One digest to compute.
     *
     * @param sinceSeq the cursor ({@code /v1/digest}), or null to start at {@code periodFrom}
     * @param periodFrom the period start (briefings), or null for a cursor
     * @param periodTo the period end (briefings), or null for now
     * @param sinceAlertId alerts after this id, or null for the first alert at or after the period start
     *     ({@code now − 24 h} for a cursor without one)
     * @param scope the filters
     * @param perKind notable records per kind
     * @param hiddenSources sources counted but never listed
     * @param zone the timezone section headings use
     * @param briefing the briefing scope for contributors, or null
     */
    public record Request(Long sinceSeq, Instant periodFrom, Instant periodTo, Long sinceAlertId,
            ScopeResolver.Resolved scope, int perKind, Set<String> hiddenSources, ZoneId zone, BriefingScope briefing,
            ai.foresignal.security.restrict.ReadRestriction restriction) {

        /** Copies the hidden sources; a null restriction becomes none. */
        public Request {
            hiddenSources = hiddenSources == null ? Set.of() : Set.copyOf(hiddenSources);
            zone = zone == null ? ZoneOffset.UTC : zone;
            restriction = restriction == null ? ai.foresignal.security.restrict.ReadRestriction.NONE : restriction;
        }

        /** An unrestricted request (the shape before prompt 035). */
        public Request(Long sinceSeq, Instant periodFrom, Instant periodTo, Long sinceAlertId,
                ScopeResolver.Resolved scope, int perKind, Set<String> hiddenSources, ZoneId zone,
                BriefingScope briefing) {
            this(sinceSeq, periodFrom, periodTo, sinceAlertId, scope, perKind, hiddenSources, zone, briefing, null);
        }
    }

    /**
     * The bounds of a digest, read before its statistics.
     *
     * @param fromSeq the exclusive lower seq bound
     * @param toSeq the inclusive upper seq bound
     * @param fromTime the period start
     * @param toTime the period end
     * @param sinceAlertId alerts after this id
     * @param untilAlertId up to this id
     */
    public record Bounds(long fromSeq, long toSeq, Instant fromTime, Instant toTime, long sinceAlertId,
            long untilAlertId) {}

    /** Reads the bounds of {@code request}: {@code toSeq} first, then the cursors. */
    public Bounds bounds(Request request) {
        long toSeq = changeBus.lastPublishedSeq();
        Instant now = clock.instant();
        return runner.read(jdbc -> {
            long fromSeq;
            Instant fromTime;
            if (request.sinceSeq() != null) {
                fromSeq = Math.min(Math.max(0, request.sinceSeq()), toSeq);
                fromTime = timeOfSeq(jdbc, fromSeq, now);
            } else {
                Instant from = request.periodFrom() == null ? now.minus(DEFAULT_WINDOW) : request.periodFrom();
                fromSeq = Math.min(seqClock.lastSeqBefore(jdbc, from), toSeq);
                fromTime = from;
            }
            Instant toTime = request.periodTo() == null ? now : request.periodTo();
            long until = provider.maxAlertId(jdbc);
            long since;
            if (request.sinceAlertId() != null) {
                since = Math.max(0, request.sinceAlertId());
            } else {
                since = provider.alertCursorAt(jdbc,
                        request.periodFrom() != null ? request.periodFrom() : now.minus(DEFAULT_WINDOW));
            }
            return new Bounds(fromSeq, toSeq, fromTime, toTime, since, Math.max(until, since));
        });
    }

    /** The default {@code sinceSeq} of {@code /v1/digest}: the last seq ingested before {@code now − 24 h}. */
    public long defaultSinceSeq() {
        Instant from = clock.instant().minus(DEFAULT_WINDOW);
        return runner.read(jdbc -> seqClock.lastSeqBefore(jdbc, from));
    }

    /** Computes the digest of {@code request}. */
    public DigestStats compute(Request request) {
        return compute(request, bounds(request));
    }

    /** Computes the digest of {@code request} within {@code bounds} (from {@link #bounds}). */
    public DigestStats compute(Request request, Bounds bounds) {
        ScopeResolver.Resolved scope = request.scope();
        DigestStatsProvider.Filter filter = new DigestStatsProvider.Filter(scope.sources(), scope.kinds(), scope.bbox(),
                scope.ruleIds(), request.hiddenSources(), request.restriction());
        long floor = retention.lowestRetainedSeq(new HashSet<>(scope.sources()));
        boolean gap = bounds.fromSeq() < floor;
        String gapReason = gap ? "fromSeq " + bounds.fromSeq() + " is below the retention floor " + floor
                + " of the scope's sources; versions before the floor were deleted, so counts are incomplete" : null;

        DigestStats stats = runner.read(jdbc -> {
            Map<DigestStatsProvider.Key, DigestStatsProvider.ReasonCounts> current =
                    provider.counts(jdbc, filter, bounds.fromSeq(), bounds.toSeq());
            Map<DigestStatsProvider.Key, DigestStatsProvider.ReasonCounts> previous = null;
            Duration length = Duration.between(bounds.fromTime(), bounds.toTime());
            if (!gap && !length.isNegative() && !length.isZero()) {
                long prevFrom = seqClock.lastSeqBefore(jdbc, bounds.fromTime().minus(length));
                if (prevFrom >= floor && prevFrom <= bounds.fromSeq()) {
                    previous = provider.counts(jdbc, filter, prevFrom, bounds.fromSeq());
                }
            }
            List<DigestStats.Count> counts = counts(current, previous);
            Map<DigestStatsProvider.Key, Long> activeRows = provider.active(jdbc, filter, bounds.fromTime());
            List<DigestStats.Active> active = new ArrayList<>();
            activeRows.forEach((key, n) -> active.add(new DigestStats.Active(key.sourceId(), key.kind(), n, null)));
            List<DigestStats.NotableGroup> notable = notable(
                    provider.notable(jdbc, filter, bounds.fromSeq(), bounds.toSeq(), request.perKind()));
            DigestStats.Alerts alertSection = alerts(provider.alerts(jdbc, filter, bounds.sinceAlertId(),
                    bounds.untilAlertId(), MAX_ALERT_ITEMS), bounds);
            return new DigestStats(bounds.fromSeq(), bounds.toSeq(), bounds.fromTime(), bounds.toTime(), gap, gapReason,
                    counts, active, alertSection, notable, null, true, List.of());
        });
        List<DigestStats.HealthItem> healthItems = scope.includeHealth() ? health(scope, request.hiddenSources(), request.restriction()) : null;
        stats = new DigestStats(stats.fromSeq(), stats.toSeq(), stats.fromTime(), stats.toTime(), stats.gap(),
                stats.gapReason(), stats.counts(), stats.active(), stats.alerts(), stats.notable(), healthItems, true,
                List.of());
        return stats.withSections(sections(stats, request));
    }

    // ------------------------------------------------------------------------- parts

    private static List<DigestStats.Count> counts(
            Map<DigestStatsProvider.Key, DigestStatsProvider.ReasonCounts> current,
            Map<DigestStatsProvider.Key, DigestStatsProvider.ReasonCounts> previous) {
        List<DigestStats.Count> out = new ArrayList<>();
        for (Map.Entry<DigestStatsProvider.Key, DigestStatsProvider.ReasonCounts> entry : current.entrySet()) {
            long[] c = entry.getValue().byCode();
            Map<String, Long> byReason = new LinkedHashMap<>();
            for (PersistReason reason : PersistReason.values()) {
                if (c[reason.code()] > 0) {
                    byReason.put(reason.name(), c[reason.code()]);
                }
            }
            DigestStats.Previous prev = null;
            if (previous != null) {
                DigestStatsProvider.ReasonCounts p = previous.get(entry.getKey());
                long[] pc = p == null ? new long[10] : p.byCode();
                prev = new DigestStats.Previous(created(pc), changed(pc), removed(pc), null);
            }
            DigestStatsProvider.ReasonCounts r = entry.getValue();
            out.add(new DigestStats.Count(entry.getKey().sourceId(), entry.getKey().kind(), byReason, created(c),
                    changed(c), removed(c), DigestStats.Count.sum(c, PersistReason.TRIGGER_EDGE), routine(c),
                    new DigestStats.Entities(r.entitiesNew(), r.entitiesChanged(), r.entitiesRemoved(),
                            r.entitiesEdges()),
                    prev));
        }
        return out;
    }

    /** FIRST + REAPPEAR. */
    static long created(long[] c) {
        return DigestStats.Count.sum(c, PersistReason.FIRST, PersistReason.REAPPEAR);
    }

    /** SIGNIFICANT + CHANGE. */
    static long changed(long[] c) {
        return DigestStats.Count.sum(c, PersistReason.SIGNIFICANT, PersistReason.CHANGE);
    }

    /** TOMBSTONE. */
    static long removed(long[] c) {
        return DigestStats.Count.sum(c, PersistReason.TOMBSTONE);
    }

    /** SAMPLE + KEYFRAME + HEARTBEAT: routine, never change. */
    static long routine(long[] c) {
        return DigestStats.Count.sum(c, PersistReason.SAMPLE, PersistReason.KEYFRAME, PersistReason.HEARTBEAT);
    }

    private List<DigestStats.NotableGroup> notable(List<DigestStatsProvider.Row> rows) {
        Map<String, List<DigestStats.NotableItem>> byKind = new TreeMap<>();
        for (DigestStatsProvider.Row row : rows) {
            String rankField = registry.find(row.sourceId()).map(r -> r.descriptor().rankField()).orElse(null);
            byKind.computeIfAbsent(row.kind(), k -> new ArrayList<>()).add(new DigestStats.NotableItem(row.sourceId(),
                    row.externalId(), row.kind(), row.seq(), row.title(), row.live(), row.observedAt(),
                    row.effectiveTime(), row.rankValue(), row.rankValue() == null ? null : rankField,
                    links.record(row.sourceId(), row.externalId())));
        }
        List<DigestStats.NotableGroup> out = new ArrayList<>();
        byKind.forEach((kind, items) -> out.add(new DigestStats.NotableGroup(kind, items)));
        return out;
    }

    private DigestStats.Alerts alerts(DigestStatsProvider.AlertWindow window, Bounds bounds) {
        Map<String, Long> bySeverity = new LinkedHashMap<>();
        Map<String, Long> byStatus = new LinkedHashMap<>();
        Map<UUID, DigestStats.RuleCount> byRule = new LinkedHashMap<>();
        long total = 0;
        for (DigestStatsProvider.AlertGroup group : window.groups()) {
            total += group.count();
            bySeverity.merge(group.severity(), group.count(), Long::sum);
            byStatus.merge(group.state(), group.count(), Long::sum);
            byRule.merge(group.ruleId(), new DigestStats.RuleCount(group.ruleId(), group.ruleName(), group.count()),
                    (a, b) -> new DigestStats.RuleCount(a.ruleId(), a.ruleName(), a.count() + b.count()));
        }
        List<DigestStats.RuleCount> rules = new ArrayList<>(byRule.values());
        rules.sort(Comparator.comparingLong(DigestStats.RuleCount::count).reversed()
                .thenComparing(r -> r.ruleName() == null ? "" : r.ruleName()));
        List<DigestStats.AlertItem> items = new ArrayList<>();
        for (Long id : window.itemIds()) {
            Optional<AlertView> view = alerts.find(id);
            view.ifPresent(v -> items.add(new DigestStats.AlertItem(v, alertMapper.alert(v, null), links.alert(v.id()))));
        }
        return new DigestStats.Alerts(bounds.sinceAlertId(), bounds.untilAlertId(), bySeverity, byStatus, rules, items,
                total);
    }

    private List<DigestStats.HealthItem> health(ScopeResolver.Resolved scope, Set<String> hidden,
            ai.foresignal.security.restrict.ReadRestriction restriction) {
        List<DigestStats.HealthItem> out = new ArrayList<>();
        Set<String> sources = new LinkedHashSet<>(scope.sources());
        for (SourceHealthView view : health.all(Optional.of(EnumSet.of(HealthStatus.DEGRADED, HealthStatus.FAILING)))) {
            if ((!sources.isEmpty() && !sources.contains(view.sourceId())) || hidden.contains(view.sourceId())
                    || !restriction.allowsSource(view.sourceId())) {
                continue;
            }
            String error = view.lastError() != null ? view.lastError() : view.statusReason();
            out.add(new DigestStats.HealthItem(view.sourceId(), view.status().value(), null, error));
        }
        return out;
    }

    private List<BriefingSection> sections(DigestStats stats, Request request) {
        List<BriefingSection> out = new ArrayList<>();
        ScopeResolver.Resolved scope = request.scope();
        BriefingSectionContributor.Context context = new BriefingSectionContributor.Context(stats, scope.sources(),
                scope.kinds(), scope.bbox(), stats.fromTime(), stats.toTime(), request.zone(), request.briefing());
        contributors.orderedStream().forEach(contributor -> {
            try {
                contributor.contribute(context).ifPresent(out::add);
            } catch (RuntimeException e) {
                log.warn("Briefing section contributor {} failed; its section is skipped",
                        contributor.getClass().getSimpleName(), e);
            }
        });
        return out;
    }

    /**
     * The time of {@code seq} from {@code seq_clock}: the start of the latest minute whose first seq is
     * at most {@code seq + 1}; the oldest minute when the seq predates the table; {@code now} when the
     * table is empty.
     */
    Instant timeOfSeq(NamedParameterJdbcTemplate jdbc, long seq, Instant now) {
        List<OffsetDateTime> minute = jdbc.queryForList(
                "SELECT minute FROM seq_clock WHERE first_seq <= :s ORDER BY minute DESC LIMIT 1",
                new MapSqlParameterSource("s", seq + 1), OffsetDateTime.class);
        if (!minute.isEmpty() && minute.getFirst() != null) {
            return minute.getFirst().toInstant();
        }
        List<OffsetDateTime> oldest = jdbc.queryForList("SELECT min(minute) FROM seq_clock", new MapSqlParameterSource(),
                OffsetDateTime.class);
        return oldest.isEmpty() || oldest.getFirst() == null ? now : oldest.getFirst().toInstant();
    }
}
