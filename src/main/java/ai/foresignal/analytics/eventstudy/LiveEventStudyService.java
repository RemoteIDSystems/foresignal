package ai.foresignal.analytics.eventstudy;

import ai.foresignal.analytics.AnalyticsSupport;
import ai.foresignal.analytics.family.SignalFamilyRegistry;
import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.analytics.finding.FindingCaveats;
import ai.foresignal.analytics.finding.FindingDraft;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.analytics.finding.FindingSeverities;
import ai.foresignal.analytics.finding.ForesignalAnalyticsGenerator;
import ai.foresignal.analytics.math.PlaceboSampler;
import ai.foresignal.analytics.math.RobustStats;
import ai.foresignal.analytics.math.Seeds;
import ai.foresignal.analytics.registry.AnalysisSeriesEntity;
import ai.foresignal.analytics.registry.AnalysisSeriesRepository;
import ai.foresignal.analytics.registry.WatchPair;
import ai.foresignal.analytics.series.SeriesData;
import ai.foresignal.analytics.series.SeriesFrequency;
import ai.foresignal.analytics.series.SeriesReader;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.attention.AttentionTopicEntity;
import ai.foresignal.attention.AttentionTopicRepository;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.intel.ForesignalIntelGenerator;
import ai.foresignal.reference.RegionTagger;
import ai.foresignal.reference.RegionTags;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.store.query.SeqClockQuery;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.statistics.distribution.NormalDistribution;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Live event studies (prompt 119, Intelligence-Design §5.3): {@code pre-event-signal} and {@code unexplained-move}
 * findings.
 *
 * <p><b>Qualifying events</b> are read from the change stream behind the durable cursor
 * {@code analysis.live.cursor} (which joins retention's consumer clamp): a {@code foresignal-intel} intelligence event
 * that becomes active with severity warning or higher, a situation turning active, and a significant
 * {@code change-point} finding of {@code foresignal-analytics}.
 *
 * <p><b>Pre-event signal:</b> for each registry row whose watch pair matches the event's family and regions, the
 * pre-event window is read with {@code asOf} = the event's {@code observedAt}, so only points known before the event
 * count (INT-2). A placebo p ≤ 0.01 and {@code |CAR| ≥ minEffect} (2 SD) make a finding. When the window's last point
 * is not yet published (a weekly series with a release lag), a re-check is scheduled after {@code releaseLagDays} and
 * the finding, if any, carries {@code LAGGED_PUBLICATION}.
 *
 * <p><b>Unexplained move</b> (daily at 04:00 UTC): a price, volume, open-interest, position, attention or tone series
 * whose latest bucket moved {@code |z| ≥ 3} against its estimation window with no qualifying event in its linked
 * regions or topic within ±2 days. A qualifying event within the next 7 days links to it ({@code stats.linkedEventId},
 * {@code stats.leadDays}) and creates a {@code pre-event-signal} that references it.
 *
 * <p>Findings describe unusual movement before events; they never claim wrongdoing or advance knowledge.
 */
@Service
public class LiveEventStudyService implements IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(LiveEventStudyService.class);

    /** Versions scanned per chunk. */
    static final long CHUNK = 10_000;

    /** The |z| of an unexplained move. */
    public static final double MOVE_Z = 3.0;

    /** The days around a move searched for a qualifying event. */
    public static final int EXPLAIN_DAYS = 2;

    /** The days after a move within which an event links to it. */
    public static final int LINK_DAYS = 7;

    /** The placebo p a pre-event signal needs. */
    public static final double PLACEBO_P = 0.01;

    /** The 117 families scanned for unexplained moves. */
    public static final Set<String> MOVE_FAMILIES = Set.of("price", "volume", "open-interest", "position",
            "attention", "tone");

    private static final NormalDistribution STANDARD = NormalDistribution.of(0, 1);
    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};
    private static final Set<String> WARNING_UP = Set.of("warning", "critical");

    private final NamedParameterJdbcTemplate jdbc;
    private final AppSettingService settings;
    private final SeqClockQuery seqClock;
    private final AnalysisSeriesRepository registry;
    private final SeriesReader reader;
    private final FindingService findings;
    private final QualifyingEvents qualifying;
    private final SignalFamilyRegistry families;
    private final AnalyticsSupport support;
    private final ObjectProvider<RegionTagger> regions;
    private final ObjectProvider<AttentionTopicRepository> topics;
    private final JsonMapper json;
    private final Clock clock;
    private final ReentrantLock lock = new ReentrantLock();
    private volatile Long cursor;

    /** Creates the service. */
    public LiveEventStudyService(NamedParameterJdbcTemplate jdbc, AppSettingService settings, SeqClockQuery seqClock,
            AnalysisSeriesRepository registry, SeriesReader reader, FindingService findings,
            QualifyingEvents qualifying, SignalFamilyRegistry families, AnalyticsSupport support,
            ObjectProvider<RegionTagger> regions, ObjectProvider<AttentionTopicRepository> topics, JsonMapper json,
            Clock clock) {
        this.jdbc = jdbc;
        this.settings = settings;
        this.seqClock = seqClock;
        this.registry = registry;
        this.reader = reader;
        this.findings = findings;
        this.qualifying = qualifying;
        this.families = families;
        this.support = support;
        this.regions = regions;
        this.topics = topics;
        this.json = json;
        this.clock = clock;
    }

    /**
     * A qualifying event seen on the change stream.
     *
     * @param recordRef the pinned record reference ({@code record:src/id@seq})
     * @param objectRef the object reference ({@code intel-event:id}, {@code situation:id}, {@code finding:id})
     * @param title the title
     * @param at the event's {@code observedAt} (the decision time)
     * @param families its canonical signal families
     * @param regions its 040 regions
     */
    public record LiveEvent(String recordRef, String objectRef, String title, Instant at, Set<String> families,
            List<String> regions) {}

    /**
     * What a poll did.
     *
     * @param events qualifying events handled
     * @param findings findings written
     * @param rechecks re-checks scheduled
     * @param cursor the cursor afterwards
     */
    public record PollResult(int events, int findings, int rechecks, long cursor) {}

    /** The durable cursor (0 before the first poll). */
    public long cursor() {
        Long value = cursor;
        if (value == null) {
            value = load();
            cursor = value;
        }
        return value;
    }

    @Override
    public void resetState() {
        cursor = load();
    }

    /** Handles every qualifying event committed up to now, then the re-checks that are due. */
    public PollResult poll() {
        lock.lock();
        try {
            Instant now = clock.instant();
            long from = cursor();
            if (from == 0) {
                // First start: begin at the head, never re-scanning history for live signals.
                long head = seqClock.seqAt(jdbc, now);
                store(head);
                return new PollResult(0, 0, processDue(now), head);
            }
            long head = seqClock.seqAt(jdbc, now);
            int events = 0;
            int written = 0;
            int rechecks = 0;
            while (from < head) {
                long to = Math.min(head, from + CHUNK);
                for (LiveEvent event : scan(from, to)) {
                    events++;
                    int[] counts = handle(event, false, event.at());
                    written += counts[0];
                    rechecks += counts[1];
                }
                from = to;
                store(from);
            }
            processDue(now);
            return new PollResult(events, written, rechecks, from);
        } finally {
            lock.unlock();
        }
    }

    /** Moves the cursor (tests start the scan below the seeded events). */
    public void rewind(long seq) {
        store(seq);
    }

    // ------------------------------------------------------------------------- scanning

    /** The qualifying events among the versions with seq in {@code (from, to]}. */
    List<LiveEvent> scan(long from, long to) {
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("from", from).addValue("to", to)
                .addValue("intel", ForesignalIntelGenerator.ID).addValue("analytics", ForesignalAnalyticsGenerator.ID);
        List<LiveEvent> out = new ArrayList<>();
        jdbc.query("SELECT v.seq, v.source_id, v.external_id, v.kind, v.observed_at, ST_AsText(v.geom) AS wkt,"
                + " v.payload::text AS payload, (SELECT pv.payload::text FROM record_version pv WHERE pv.seq ="
                + " v.prev_seq LIMIT 1) AS prev FROM record_version v WHERE v.seq > :from AND v.seq <= :to AND"
                + " v.tombstone = false AND ((v.source_id = :intel AND v.kind IN ('intelligence-event', 'situation'))"
                + " OR (v.source_id = :analytics AND v.kind = 'analytic-finding')) ORDER BY v.seq", p, rs -> {
                    Map<String, Object> payload = parse(rs.getString("payload"));
                    Map<String, Object> prev = parse(rs.getString("prev"));
                    String kind = rs.getString("kind");
                    if (!qualifies(kind, payload, prev)) {
                        return;
                    }
                    String source = rs.getString("source_id");
                    String external = rs.getString("external_id");
                    long seq = rs.getLong("seq");
                    Instant at = rs.getObject("observed_at", OffsetDateTime.class).toInstant();
                    Set<String> fams = new LinkedHashSet<>();
                    if (payload.get("signalFamilies") instanceof List<?> list) {
                        for (Object o : list) {
                            families.canonical(String.valueOf(o)).ifPresent(fams::add);
                        }
                    }
                    if (payload.get("signalFamily") != null) {
                        families.canonical(payload.get("signalFamily").toString()).ifPresent(fams::add);
                    }
                    if ("intelligence-event".equals(kind) && payload.get("type") != null) {
                        families.familyOf(SignalFamilyRegistry.Query.intelEvent(payload.get("type").toString()))
                                .ifPresent(fams::add);
                    }
                    List<String> codes = new ArrayList<>();
                    if (payload.get("regionCodes") instanceof List<?> list) {
                        for (Object o : list) {
                            codes.add(String.valueOf(o));
                        }
                    }
                    if (codes.isEmpty()) {
                        codes.addAll(tag(rs.getString("wkt")));
                    }
                    String objectRef = switch (kind) {
                        case "intelligence-event" -> "intel-event:" + external;
                        case "situation" -> "situation:" + external;
                        default -> "finding:" + external;
                    };
                    Object title = payload.get("title");
                    out.add(new LiveEvent(recordRef(source, external, seq), objectRef,
                            title == null ? objectRef : title.toString(), at, fams, codes));
                });
        return out;
    }

    /** True when a version makes its object a qualifying event (it was not one in the previous version). */
    static boolean qualifies(String kind, Map<String, Object> payload, Map<String, Object> prev) {
        switch (kind) {
            case "intelligence-event" -> {
                boolean now = "active".equals(payload.get("status")) && WARNING_UP.contains(payload.get("severity"));
                boolean before = prev != null && "active".equals(prev.get("status"))
                        && WARNING_UP.contains(prev.get("severity"));
                return now && !before;
            }
            case "situation" -> {
                return "active".equals(payload.get("status")) && (prev == null || !"active".equals(prev.get("status")));
            }
            case "analytic-finding" -> {
                boolean now = "change-point".equals(payload.get("findingType"))
                        && FindingSeverities.SIGNIFICANT.equals(payload.get("severity"));
                boolean before = prev != null && FindingSeverities.SIGNIFICANT.equals(prev.get("severity"));
                return now && !before;
            }
            default -> {
                return false;
            }
        }
    }

    // ------------------------------------------------------------------------- pre-event signals

    /**
     * Handles one qualifying event: links recent unexplained moves, then checks every matching watch pair.
     *
     * @param recheck true for a lagged re-check
     * @param asOf the knowledge cutoff of the series reads
     * @return {@code [findings written, re-checks scheduled]}
     */
    int[] handle(LiveEvent event, boolean recheck, Instant asOf) {
        int written = 0;
        int scheduled = 0;
        if (!recheck) {
            written += linkMoves(event);
        }
        for (AnalysisSeriesEntity row : registry.findByEnabledTrueOrderByRefAsc()) {
            for (WatchPair pair : WatchPair.of(row)) {
                if (!event.families().contains(pair.eventFamily()) || !pair.matches(pair.eventFamily(),
                        event.regions().isEmpty() ? List.of("ZZ") : event.regions())) {
                    continue;
                }
                try {
                    Outcome outcome = preEvent(row, pair, event, recheck, asOf);
                    if (outcome == Outcome.WRITTEN) {
                        written++;
                    } else if (outcome == Outcome.RECHECK) {
                        scheduled++;
                    }
                } catch (RuntimeException e) {
                    log.warn("Pre-event check of {} for {} failed: {}", row.getRef(), event.objectRef(), e.toString());
                }
            }
        }
        return new int[] {written, scheduled};
    }

    /** What one pre-event check did. */
    enum Outcome {
        /** A finding was written. */
        WRITTEN,
        /** Nothing unusual (or not evaluable). */
        NONE,
        /** The window was not yet published; a re-check was scheduled. */
        RECHECK
    }

    Outcome preEvent(AnalysisSeriesEntity row, WatchPair pair, LiveEvent event, boolean recheck, Instant asOf) {
        SeriesRef ref = SeriesRef.parse(row.getRef());
        SeriesReader.Meta meta = reader.meta(ref);
        SeriesFrequency frequency = meta.frequency() == SeriesFrequency.WEEK ? SeriesFrequency.WEEK
                : SeriesFrequency.DAY;
        EventStudyEngine.Windows w = EventStudyEngine.Windows.of(frequency);
        Instant t0 = frequency.floor(event.at());
        Instant from = frequency.plus(t0, w.estFrom() - 2);
        Instant to = frequency.next(t0);
        SeriesData data = reader.read(ref, from, to, asOf);
        if (data.frequency() != frequency) {
            data = data.resample(frequency);
        }
        SeriesData.Grid grid = data.grid();
        int index = EventStudyEngine.indexOf(grid.starts(), frequency, event.at());
        if (index < 0) {
            return Outcome.NONE;
        }
        int lastPre = index + w.preTo();
        boolean published = lastPre >= 0 && lastPre < grid.size() && !grid.gap()[lastPre];
        double lagDays = pair.releaseLagDays() != null ? pair.releaseLagDays() : data.releaseLagDays();
        if (!published && !recheck && lagDays > 0) {
            scheduleRecheck(row.getRef(), pair, event, event.at().plus(Duration.ofHours(Math.round(lagDays * 24) + 24)));
            return Outcome.RECHECK;
        }
        List<Instant> others = qualifying.times(pair.eventFamily(), pair.regionCodes(), from,
                event.at().minusSeconds(1));
        long seed = Seeds.of("pre-event", row.getRef(), event.objectRef());
        EventStudyEngine.Transform transform = EventStudyEngine.Transform.of(data.counts(), data.transform(),
                data.seriesFamily());
        EventStudyEngine.Result result = EventStudyEngine.run(new EventStudyEngine.Input(grid.starts(),
                grid.values(), frequency, transform, w, List.of(new EventStudyEngine.Event(event.at(),
                        event.objectRef())), others, seed, PlaceboSampler.DEFAULT_COUNT, false));
        EventStudyEngine.EventOutcome outcome = result.events().get(0);
        if (outcome.excluded() != null) {
            return Outcome.NONE;
        }
        double car = outcome.carPre();
        double placeboP = result.pre().placeboP();
        if (!(placeboP <= PLACEBO_P) || Math.abs(car) < pair.minEffect()) {
            return Outcome.NONE;
        }
        double p = result.pre().p();
        String severity = p <= FindingSeverities.SIGNIFICANT_P ? FindingSeverities.SIGNIFICANT
                : FindingSeverities.NOTABLE;
        int length = w.preTo() - w.preFrom() + 1;
        String unit = frequency == SeriesFrequency.WEEK ? "week" : "day";
        String label = AnalyticsSupport.label(ref);
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("car", AnalyticsSupport.round(car));
        stats.put("z", AnalyticsSupport.round(result.pre().z()));
        stats.put("standardizedP", AnalyticsSupport.round(p));
        stats.put("placeboP", AnalyticsSupport.round(placeboP));
        stats.put("corradoP", AnalyticsSupport.round(result.pre().corradoP()));
        stats.put("window", List.of(w.preFrom(), w.preTo()));
        stats.put("linkedEventId", event.objectRef());
        stats.put("eventAt", event.at().toString());
        stats.put("estimationPoints", outcome.estimationPoints());
        stats.put("transform", transform.wire());
        stats.put("placeboDates", result.placeboDates());
        Map<String, Object> baseRate = new LinkedHashMap<>();
        baseRate.put("value", AnalyticsSupport.round(placeboP));
        baseRate.put("unit", "placebo-share");
        baseRate.put("method", "placebo-distribution");
        baseRate.put("n", result.placeboDates());
        String pct = AnalyticsSupport.num(placeboP * 100);
        baseRate.put("text", "moves this large occur before about " + pct + "% of comparable windows with no event");
        List<Map<String, Object>> caveats = new ArrayList<>();
        caveats.add(FindingCaveats.notGroundTruth());
        if (recheck) {
            caveats.add(FindingCaveats.of(FindingCaveats.LAGGED_PUBLICATION, "The series is published with a delay;"
                    + " the pre-event window was complete only after the event, so this signal could not have been"
                    + " seen in time."));
        }
        if (AnalyticsSupport.market(data)) {
            caveats.add(FindingCaveats.dailyResolutionOnly());
        }
        String explanation = label + " moved " + AnalyticsSupport.num(car) + " SD in the " + length + " " + unit
                + (length == 1 ? "" : "s") + " before " + event.title() + "; " + baseRate.get("text") + ". This is"
                + " unusual movement before the event, not evidence of wrongdoing or advance knowledge.";
        List<String> members = new ArrayList<>();
        members.add(event.recordRef());
        FindingDraft draft = new FindingDraft("pre-event-signal", "Pre-event signal: " + label + " before "
                + event.title(), severity, null, event.regions(), frequency.plus(t0, w.preFrom()), t0, clock.instant(),
                List.of(ref.format()), members, data.asOfSeq(), stats, baseRate, caveats, EventStudyEngine.NAME,
                EventStudyEngine.VERSION, params(w, pair), explanation, support.privateSeries(data),
                pair.eventFamily(), support.localOnlySources(data), List.of(), ref.format() + "|" + event.objectRef());
        findings.record(draft);
        return Outcome.WRITTEN;
    }

    private static Map<String, Object> params(EventStudyEngine.Windows w, WatchPair pair) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("windows", w.toMap());
        out.put("minEffect", pair.minEffect());
        out.put("placeboP", PLACEBO_P);
        out.put("placeboCount", PlaceboSampler.DEFAULT_COUNT);
        out.put("eventFamily", pair.eventFamily());
        return out;
    }

    // ------------------------------------------------------------------------- re-checks

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> rechecks() {
        try {
            List<Map<String, Object>> value = settings.get(SettingKeys.ANALYSIS_LIVE_RECHECKS, List.class)
                    .orElse(List.of());
            return new ArrayList<>(value);
        } catch (RuntimeException e) {
            return new ArrayList<>();
        }
    }

    void scheduleRecheck(String ref, WatchPair pair, LiveEvent event, Instant due) {
        List<Map<String, Object>> list = rechecks();
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("ref", ref);
        entry.put("pair", pair.toMap());
        entry.put("recordRef", event.recordRef());
        entry.put("objectRef", event.objectRef());
        entry.put("title", event.title());
        entry.put("at", event.at().toString());
        entry.put("families", new ArrayList<>(event.families()));
        entry.put("regions", event.regions());
        entry.put("dueAt", due.toString());
        list.add(entry);
        settings.put(SettingKeys.ANALYSIS_LIVE_RECHECKS, list);
    }

    /** Runs the re-checks that are due at {@code now}; returns how many ran. */
    @SuppressWarnings("unchecked")
    public int processDue(Instant now) {
        List<Map<String, Object>> all = rechecks();
        List<Map<String, Object>> keep = new ArrayList<>();
        int ran = 0;
        for (Map<String, Object> entry : all) {
            Instant due = Instant.parse(String.valueOf(entry.get("dueAt")));
            if (due.isAfter(now)) {
                keep.add(entry);
                continue;
            }
            ran++;
            try {
                AnalysisSeriesEntity row = registry.findById(String.valueOf(entry.get("ref"))).orElse(null);
                WatchPair pair = WatchPair.fromMap((Map<String, Object>) entry.get("pair"));
                if (row == null || pair == null) {
                    continue;
                }
                Set<String> fams = new LinkedHashSet<>();
                for (Object f : (List<Object>) entry.getOrDefault("families", List.of())) {
                    fams.add(String.valueOf(f));
                }
                List<String> regionList = new ArrayList<>();
                for (Object r : (List<Object>) entry.getOrDefault("regions", List.of())) {
                    regionList.add(String.valueOf(r));
                }
                LiveEvent event = new LiveEvent(String.valueOf(entry.get("recordRef")),
                        String.valueOf(entry.get("objectRef")), String.valueOf(entry.get("title")),
                        Instant.parse(String.valueOf(entry.get("at"))), fams, regionList);
                preEvent(row, pair, event, true, due);
            } catch (RuntimeException e) {
                log.warn("A lagged pre-event re-check failed: {}", e.toString());
            }
        }
        if (ran > 0) {
            settings.put(SettingKeys.ANALYSIS_LIVE_RECHECKS, keep);
        }
        return ran;
    }

    // ------------------------------------------------------------------------- unexplained moves

    /** Links open unexplained moves of the last seven days to {@code event} and writes their pre-event signals. */
    int linkMoves(LiveEvent event) {
        int written = 0;
        for (AnalyticFindingEntity move : findings.detectedBetween("unexplained-move",
                event.at().minus(Duration.ofDays(LINK_DAYS)), event.at())) {
            if (!"open".equals(move.getStatus()) || move.getStats().get("linkedEventId") != null) {
                continue;
            }
            Set<String> linked = linkedRegions(move);
            boolean regionOk = linked.isEmpty() || event.regions().stream().anyMatch(r -> linked.contains(r)
                    || (r.length() > 2 && linked.contains(r.substring(0, 2))));
            if (!regionOk) {
                continue;
            }
            Instant moveAt = move.getWindowStart() != null ? move.getWindowStart() : move.getDetectedAt();
            double leadDays = Duration.between(moveAt, event.at()).toMinutes() / 1440.0;
            Optional<AnalyticFindingEntity> updated = findings.linkEvent(move.getId(), event.objectRef(), leadDays,
                    EventStudyEngine.NAME + ":" + EventStudyEngine.VERSION);
            if (updated.isEmpty()) {
                continue;
            }
            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("z", move.getStats().get("z"));
            stats.put("linkedEventId", event.objectRef());
            stats.put("leadDays", Math.round(leadDays * 100) / 100.0);
            stats.put("unexplainedMoveId", move.getId());
            stats.put("eventAt", event.at().toString());
            List<Map<String, Object>> caveats = new ArrayList<>(move.getCaveats());
            List<String> members = List.of(event.recordRef(), "finding:" + move.getId());
            String label = move.getSeriesRefs().isEmpty() ? "A series"
                    : AnalyticsSupport.label(SeriesRef.parse(move.getSeriesRefs().get(0)));
            String explanation = label + " moved unusually " + AnalyticsSupport.num(leadDays) + " days before "
                    + event.title() + " with no qualifying event at the time; " + move.getBaseRate().get("text")
                    + ". This is an observed move before the event, not evidence of wrongdoing or advance knowledge.";
            FindingDraft draft = new FindingDraft("pre-event-signal", "Pre-event signal: " + label + " "
                    + AnalyticsSupport.num(leadDays) + " days before " + event.title(), FindingSeverities.NOTABLE,
                    move.getGeom(), event.regions(), moveAt, event.at(), clock.instant(), move.getSeriesRefs(),
                    members, move.getAsOfSeq(), stats, move.getBaseRate(), caveats, EventStudyEngine.NAME,
                    EventStudyEngine.VERSION, Map.of("source", "unexplained-move", "linkDays", LINK_DAYS),
                    explanation, move.isPrivateFinding(), event.families().isEmpty() ? move.getSignalFamily()
                            : event.families().iterator().next(), move.getLocalOnlySources(), List.of(),
                    String.join(",", move.getSeriesRefs()) + "|" + event.objectRef());
            findings.record(draft);
            written++;
        }
        return written;
    }

    private Set<String> linkedRegions(AnalyticFindingEntity move) {
        Object value = move.getStats().get("linkedRegions");
        Set<String> out = new LinkedHashSet<>();
        if (value instanceof List<?> list) {
            for (Object o : list) {
                out.add(String.valueOf(o));
            }
        }
        return out;
    }

    /**
     * The daily scan for unexplained moves (04:00 UTC).
     *
     * @return the findings written
     */
    public int scanMoves(Instant now) {
        int written = 0;
        for (AnalysisSeriesEntity row : registry.findByEnabledTrueOrderByRefAsc()) {
            try {
                if (move(row, now)) {
                    written++;
                }
            } catch (RuntimeException e) {
                log.warn("Unexplained-move scan of {} failed: {}", row.getRef(), e.toString());
            }
        }
        return written;
    }

    boolean move(AnalysisSeriesEntity row, Instant now) {
        SeriesRef ref = SeriesRef.parse(row.getRef());
        SeriesReader.Meta meta = reader.meta(ref);
        if (!MOVE_FAMILIES.contains(meta.seriesFamily())) {
            return false;
        }
        SeriesFrequency frequency = meta.frequency() == SeriesFrequency.WEEK ? SeriesFrequency.WEEK
                : SeriesFrequency.DAY;
        EventStudyEngine.Windows w = EventStudyEngine.Windows.of(frequency);
        Instant to = frequency.floor(now);
        Instant from = frequency.plus(to, 2 * w.estFrom());
        SeriesData data = reader.read(ref, from, to, now);
        if (data.frequency() != frequency) {
            data = data.resample(frequency);
        }
        SeriesData.Grid grid = data.grid();
        int last = grid.size() - 1;
        if (last < 0 || grid.gap()[last]) {
            return false;
        }
        EventStudyEngine.Transform transform = EventStudyEngine.Transform.of(data.counts(), data.transform(),
                data.seriesFamily());
        double[] x = EventStudyEngine.transform(grid.values(), transform);
        double z = zAt(x, last, w);
        if (!Double.isFinite(z) || Math.abs(z) < MOVE_Z) {
            return false;
        }
        Instant moveAt = grid.starts()[last];
        Set<String> linked = linkedRegions(row, data);
        Set<String> pairFamilies = new LinkedHashSet<>();
        for (WatchPair pair : WatchPair.of(row)) {
            pairFamilies.add(pair.eventFamily());
        }
        for (QualifyingEvents.Qualifying q : qualifying.between(moveAt.minus(Duration.ofDays(EXPLAIN_DAYS)),
                frequency.next(moveAt).plus(Duration.ofDays(EXPLAIN_DAYS)))) {
            boolean familyOk = pairFamilies.isEmpty() || q.families().stream().anyMatch(pairFamilies::contains);
            if (familyOk && q.in(linked)) {
                return false;
            }
        }
        int moves = 0;
        int judged = 0;
        for (int i = Math.max(0, last + w.estFrom()); i < last; i++) {
            double zi = zAt(x, i, w);
            if (Double.isFinite(zi)) {
                judged++;
                if (Math.abs(zi) >= MOVE_Z) {
                    moves++;
                }
            }
        }
        double p = 2 * STANDARD.survivalProbability(Math.abs(z));
        String severity = p <= FindingSeverities.SIGNIFICANT_P ? FindingSeverities.SIGNIFICANT
                : FindingSeverities.NOTABLE;
        String label = AnalyticsSupport.label(ref);
        double perYear = judged == 0 ? Double.NaN : moves * frequency.perYear() / judged;
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("z", AnalyticsSupport.round(z));
        stats.put("p", AnalyticsSupport.round(p));
        stats.put("moveAt", moveAt.toString());
        stats.put("transform", transform.wire());
        stats.put("linkedRegions", new ArrayList<>(linked));
        stats.put("linkedEventId", null);
        stats.put("leadDays", null);
        Map<String, Object> baseRate = new LinkedHashMap<>();
        baseRate.put("value", AnalyticsSupport.round(perYear));
        baseRate.put("unit", "moves-per-year");
        baseRate.put("method", "detector-false-alarm-rate");
        baseRate.put("n", moves);
        baseRate.put("text", "moves this large happened in " + moves + " of the previous " + judged + " "
                + (frequency == SeriesFrequency.WEEK ? "weeks" : "days") + " of this series");
        List<Map<String, Object>> caveats = new ArrayList<>();
        caveats.add(FindingCaveats.notGroundTruth());
        if (AnalyticsSupport.market(data)) {
            caveats.add(FindingCaveats.dailyResolutionOnly());
        }
        String explanation = label + " moved " + AnalyticsSupport.num(z) + " SD on " + moveAt + " with no qualifying"
                + " event in its linked regions or topic within " + EXPLAIN_DAYS + " days; " + baseRate.get("text")
                + ". A qualifying event in the next " + LINK_DAYS + " days will be linked to it. An unexplained move"
                + " is not evidence of wrongdoing.";
        FindingDraft draft = new FindingDraft("unexplained-move", "Unexplained move: " + label + " ("
                + AnalyticsSupport.num(z) + " SD)", severity, null, new ArrayList<>(linked), moveAt,
                frequency.next(moveAt), now, List.of(ref.format()), List.of(), data.asOfSeq(), stats, baseRate,
                caveats, EventStudyEngine.NAME, EventStudyEngine.VERSION, Map.of("moveZ", MOVE_Z, "explainDays",
                        EXPLAIN_DAYS, "windows", w.toMap()), explanation, support.privateSeries(data),
                row.getFamily() != null ? row.getFamily() : data.signalFamily(), support.localOnlySources(data),
                List.of(), ref.format());
        findings.record(draft);
        return true;
    }

    /** The z of {@code x[i]} against its estimation window, or {@code NaN}. */
    static double zAt(double[] x, int i, EventStudyEngine.Windows w) {
        if (i + w.estFrom() < 0 || !Double.isFinite(x[i])) {
            return Double.NaN;
        }
        double[] est = java.util.Arrays.copyOfRange(x, i + w.estFrom(), i + w.estTo() + 1);
        if (RobustStats.count(est) < w.minEstimation()) {
            return Double.NaN;
        }
        double sd = RobustStats.sd(est);
        return sd > 0 ? (x[i] - RobustStats.mean(est)) / sd : Double.NaN;
    }

    /** The series' linked regions: its region, its topic's regions and its watch pairs' regions (empty = anywhere). */
    Set<String> linkedRegions(AnalysisSeriesEntity row, SeriesData data) {
        Set<String> out = new LinkedHashSet<>();
        if (data.regionCode() != null) {
            out.add(data.regionCode());
        }
        if (row.getRegionCode() != null) {
            out.add(row.getRegionCode());
        }
        AttentionTopicRepository repository = topics.getIfAvailable();
        if (data.topicId() != null && repository != null) {
            repository.findById(data.topicId()).map(AttentionTopicEntity::getRegionCodes).ifPresent(out::addAll);
        }
        for (WatchPair pair : WatchPair.of(row)) {
            out.addAll(pair.regionCodes());
        }
        return out;
    }

    // ------------------------------------------------------------------------- helpers

    private List<String> tag(String wkt) {
        RegionTagger tagger = regions.getIfAvailable();
        if (tagger == null || wkt == null) {
            return List.of();
        }
        try {
            Geometry geometry = new WKTReader().read(wkt);
            RegionTags tags = tagger.tag(geometry);
            List<String> out = new ArrayList<>();
            if (tags.iso2() != null) {
                out.add(tags.iso2());
            }
            if (tags.admin1() != null && tags.admin1().matches("^[A-Z]{2}-[A-Z0-9]{1,3}$")) {
                out.add(tags.admin1());
            }
            return out;
        } catch (Exception e) {
            return List.of();
        }
    }

    static String recordRef(String source, String external, long seq) {
        return "record:" + source + "/" + URLEncoder.encode(external, StandardCharsets.UTF_8).replace("+", "%20")
                + "@" + seq;
    }

    private Map<String, Object> parse(String text) {
        if (text == null) {
            return null;
        }
        try {
            return json.readValue(text, MAP);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private long load() {
        try {
            return settings.get(SettingKeys.ANALYSIS_LIVE_CURSOR, Long.class).orElse(0L);
        } catch (RuntimeException e) {
            return 0L;
        }
    }

    private void store(long seq) {
        settings.put(SettingKeys.ANALYSIS_LIVE_CURSOR, seq);
        cursor = seq;
    }
}
