package ai.foresignal.analytics.finding;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.manifest.ManifestRequest;
import ai.foresignal.manifest.ManifestService;
import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.reference.CountryRef;
import ai.foresignal.security.Ulids;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Stores, deduplicates, reads and dismisses analytic findings (prompt 119, Intelligence-Design §5.1).
 *
 * <ul>
 *   <li><b>Record:</b> every finding stores a 116 manifest (detector, version, parameters, {@code asOfSeq}, series
 *       and members). A finding without a geometry gets its first region's label point, so the 118 linker can place
 *       it.
 *   <li><b>Dedup:</b> a new finding of the same type and subject ({@code dedup_key}) whose window overlaps an open
 *       one supersedes it ({@code status=superseded}, {@code superseded_by}); an identical one (same severity, title,
 *       statistics and window) changes nothing, so a nightly re-run does not churn.
 *   <li><b>Events:</b> {@code finding.created}, {@code finding.updated} and {@code finding.dismissed} on the
 *       {@code analytics} topic after commit; a new significant, non-private finding sends the 018 notification
 *       {@code finding.significant} (channels opt in).
 *   <li><b>Visibility:</b> a private finding is listed, read or changed only by callers with private access and is
 *       {@code 404 UNKNOWN_FINDING} to anyone else.
 * </ul>
 */
@Service
public class FindingService {

    private static final Logger log = LoggerFactory.getLogger(FindingService.class);

    /** The finding types this prompt writes (later prompts add theirs). */
    public static final List<String> TYPES_119 = List.of("change-point", "pre-event-signal", "unexplained-move",
            "co-occurrence");

    /** Every finding type of the spec's {@code listFindings} filter. */
    public static final List<String> TYPES = List.of("change-point", "pre-event-signal", "unexplained-move",
            "co-occurrence", "precursor-active", "forecast-exceedance", "outlook-exceedance", "projected-impact",
            "coverage-blindspot", "coverage-divergence", "social-velocity", "media-reuse", "social-coordination",
            "attention-gap");

    /** The statuses. */
    public static final List<String> STATUSES = List.of("open", "superseded", "dismissed");

    private static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);

    private final AnalyticFindingRepository findings;
    private final FindingLogRepository logs;
    private final FindingEventBus bus;
    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectProvider<ManifestService> manifests;
    private final ObjectProvider<FindingNotifications> notifications;
    private final ObjectProvider<CountryCentroids> countries;
    private final Clock clock;

    /** Precursor summaries for the {@code precursor.*} events (prompt 120), when that package is present. */
    @org.springframework.beans.factory.annotation.Autowired
    private ObjectProvider<PrecursorSummaries> precursorSummaries;

    /** Payloads of the {@code outlook.*} and {@code index.*} events (prompt 126), when that package is present. */
    @org.springframework.beans.factory.annotation.Autowired
    private ObjectProvider<AnalyticsEventSummaries> eventSummaries;

    /** The 123 benchmark gate: findings of a component whose latest benchmark failed carry BENCHMARK_FAIL. */
    @org.springframework.beans.factory.annotation.Autowired
    private ObjectProvider<ai.foresignal.benchmark.BenchmarkGate> benchmarkGate;

    /** Creates the service. */
    public FindingService(AnalyticFindingRepository findings, FindingLogRepository logs, FindingEventBus bus,
            NamedParameterJdbcTemplate jdbc, ObjectProvider<ManifestService> manifests,
            ObjectProvider<FindingNotifications> notifications, ObjectProvider<CountryCentroids> countries,
            Clock clock) {
        this.findings = findings;
        this.logs = logs;
        this.bus = bus;
        this.jdbc = jdbc;
        this.manifests = manifests;
        this.notifications = notifications;
        this.countries = countries;
        this.clock = clock;
    }

    /** What {@link #record} did. */
    public enum Change {
        /** A new finding was stored. */
        CREATED,
        /** A new finding was stored and superseded at least one open finding. */
        SUPERSEDED,
        /** An identical open finding existed; nothing changed. */
        UNCHANGED
    }

    /**
     * The outcome of {@link #record}.
     *
     * @param finding the stored (or the existing identical) finding
     * @param change what happened
     */
    public record Outcome(AnalyticFindingEntity finding, Change change) {}

    /** Stores a finding, deduplicating and superseding by window. */
    @Transactional
    public Outcome record(FindingDraft draft) {
        Objects.requireNonNull(draft, "draft");
        Instant now = clock.instant();
        String dedupKey = draft.findingType() + "|" + draft.dedupScope();
        List<AnalyticFindingEntity> overlapping = new ArrayList<>();
        for (AnalyticFindingEntity open : findings.findByDedupKeyAndStatus(dedupKey, "open")) {
            if (overlaps(open, draft)) {
                if (same(open, draft)) {
                    return new Outcome(open, Change.UNCHANGED);
                }
                overlapping.add(open);
            }
        }
        AnalyticFindingEntity f = new AnalyticFindingEntity(Ulids.next(now), draft.findingType(), clip(draft.title()),
                draft.severity(), draft.detectedAt() == null ? now : draft.detectedAt(), now);
        f.setGeom(draft.geometry() != null ? draft.geometry() : regionPoint(draft.regionCodes()));
        f.setRegionCodes(draft.regionCodes());
        f.setWindowStart(draft.windowStart());
        f.setWindowEnd(draft.windowEnd());
        f.setSeriesRefs(draft.seriesRefs());
        f.setMembers(draft.members());
        f.setAsOfSeq(draft.asOfSeq());
        f.setStats(draft.stats());
        f.setBaseRate(draft.baseRate());
        f.setCaveats(withBenchmarkCaveat(draft));
        Map<String, Object> detector = new LinkedHashMap<>();
        detector.put("name", draft.detectorName());
        detector.put("version", draft.detectorVersion());
        detector.put("params", draft.detectorParams());
        f.setDetector(detector);
        f.setExplanation(draft.explanation());
        f.setPrivateFinding(draft.privateFinding());
        f.setSignalFamily(draft.signalFamily());
        f.setLocalOnlySources(draft.localOnlySources());
        f.setRawStatKeys(draft.rawStatKeys());
        f.setDedupKey(dedupKey);
        if ("precursor-active".equals(draft.findingType()) && draft.stats().get("precursorId") != null) {
            f.setPrecursorId(String.valueOf(draft.stats().get("precursorId")));
        }
        f.setManifestId(manifest(draft));
        findings.save(f);
        List<FindingLogEntity> announced = new ArrayList<>();
        announced.add(logs.save(new FindingLogEntity(f.getId(), now, actor(draft), "created", FindingEvent.CREATED,
                null)));
        for (AnalyticFindingEntity old : overlapping) {
            old.setStatus("superseded");
            old.setSupersededBy(f.getId());
            old.bumpVersion();
            old.setUpdatedAt(now);
            findings.save(old);
            announced.add(logs.save(new FindingLogEntity(old.getId(), now, actor(draft), "superseded",
                    FindingEvent.UPDATED, "superseded by " + f.getId())));
        }
        publishAfterCommit(announced, f);
        return new Outcome(f, overlapping.isEmpty() ? Change.CREATED : Change.SUPERSEDED);
    }

    /**
     * Supersedes an open finding without recording a new one in its place (prompt 127: a projection the next run of the
     * same hazard no longer produces): {@code status=superseded}, {@code superseded_by} the successor when there is one,
     * a version bump and {@code finding.updated}. Does nothing when the finding is not open.
     *
     * @param id the finding
     * @param supersededBy the successor finding, or null
     * @param actor the detector ({@code name:version})
     * @param reason why
     * @return true when the finding was superseded
     */
    @Transactional
    public boolean supersede(String id, String supersededBy, String actor, String reason) {
        AnalyticFindingEntity f = id == null ? null : findings.findById(id).orElse(null);
        if (f == null || !"open".equals(f.getStatus())) {
            return false;
        }
        Instant now = clock.instant();
        f.setStatus("superseded");
        f.setSupersededBy(supersededBy);
        f.bumpVersion();
        f.setUpdatedAt(now);
        findings.save(f);
        FindingLogEntity row = logs.save(new FindingLogEntity(f.getId(), now, actor, "superseded",
                FindingEvent.UPDATED, reason));
        publishAfterCommit(List.of(row), null);
        return true;
    }

    /**
     * Dismisses a finding with a reason.
     *
     * @throws NotFoundException 404 {@code UNKNOWN_FINDING}
     * @throws ValidationException when the reason is missing
     */
    @Transactional
    public AnalyticFindingEntity dismiss(String id, String reason, String actor, boolean seesPrivate) {
        if (reason == null || reason.isBlank() || reason.length() > 2000) {
            throw ValidationException.forField("reason", "is required (1 to 2000 characters)");
        }
        AnalyticFindingEntity f = visible(id, seesPrivate);
        if ("dismissed".equals(f.getStatus())) {
            return f;
        }
        Instant now = clock.instant();
        Map<String, Object> dismissal = new LinkedHashMap<>();
        dismissal.put("reason", reason.strip());
        dismissal.put("at", now.toString());
        dismissal.put("by", actor == null ? "operator" : actor);
        f.setStatus("dismissed");
        f.setDismissal(dismissal);
        f.bumpVersion();
        f.setUpdatedAt(now);
        findings.save(f);
        FindingLogEntity row = logs.save(new FindingLogEntity(f.getId(), now, actor, "dismissed",
                FindingEvent.DISMISSED, reason.strip()));
        publishAfterCommit(List.of(row), null);
        return f;
    }

    /**
     * Links an open finding to a later qualifying event (prompt 119: an {@code unexplained-move} followed by an event
     * within seven days): {@code stats.linkedEventId} and {@code stats.leadDays} are set and {@code finding.updated}
     * is announced. Does nothing when the finding is not open or is already linked.
     *
     * @return the finding, or empty when nothing changed
     */
    @Transactional
    public Optional<AnalyticFindingEntity> linkEvent(String id, String eventRef, double leadDays, String actor) {
        AnalyticFindingEntity f = findings.findById(id).orElse(null);
        if (f == null || !"open".equals(f.getStatus()) || f.getStats().get("linkedEventId") != null) {
            return Optional.empty();
        }
        Instant now = clock.instant();
        Map<String, Object> stats = new LinkedHashMap<>(f.getStats());
        stats.put("linkedEventId", eventRef);
        stats.put("leadDays", Math.round(leadDays * 100) / 100.0);
        f.setStats(stats);
        List<String> members = new ArrayList<>(f.getMembers());
        if (!members.contains(eventRef)) {
            members.add(eventRef);
        }
        f.setMembers(members);
        f.bumpVersion();
        f.setUpdatedAt(now);
        findings.save(f);
        FindingLogEntity row = logs.save(new FindingLogEntity(f.getId(), now, actor, "linked", FindingEvent.UPDATED,
                "linked to " + eventRef));
        publishAfterCommit(List.of(row), null);
        return Optional.of(f);
    }

    /**
     * The finding, when the caller may see it.
     *
     * @throws NotFoundException 404 {@code UNKNOWN_FINDING}
     */
    @Transactional(readOnly = true)
    public AnalyticFindingEntity visible(String id, boolean seesPrivate) {
        AnalyticFindingEntity f = id == null ? null : findings.findById(id).orElse(null);
        if (f == null || (f.isPrivateFinding() && !seesPrivate)) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_FINDING, "No finding '" + id + "'.");
        }
        return f;
    }

    /**
     * A list query.
     *
     * @param types finding types (any)
     * @param severities severities (any)
     * @param statuses statuses (any)
     * @param regions region codes (overlap)
     * @param ref a series reference the finding rests on, or null
     * @param since {@code detectedAt ≥ since}, or null
     * @param situationId findings linked to this situation, or null
     * @param cursor the cursor, or null
     * @param limit the page size
     */
    public record ListQuery(List<String> types, List<String> severities, List<String> statuses, List<String> regions,
            String ref, Instant since, String situationId, String cursor, int limit) {}

    /**
     * A page, newest first.
     *
     * @param items the findings
     * @param nextCursor the next cursor, or null
     */
    public record Page(List<AnalyticFindingEntity> items, String nextCursor) {}

    /** The findings matching a query, newest first (keyset on {@code detected_at, id}). */
    @Transactional(readOnly = true)
    public Page list(ListQuery q, boolean seesPrivate) {
        StringBuilder sql = new StringBuilder("SELECT f.id, f.detected_at FROM analytic_finding f WHERE 1 = 1");
        MapSqlParameterSource p = new MapSqlParameterSource();
        if (!seesPrivate) {
            sql.append(" AND f.private = false");
        }
        if (q.types() != null && !q.types().isEmpty()) {
            sql.append(" AND f.finding_type IN (:types)");
            p.addValue("types", q.types());
        }
        if (q.severities() != null && !q.severities().isEmpty()) {
            sql.append(" AND f.severity IN (:severities)");
            p.addValue("severities", q.severities());
        }
        if (q.statuses() != null && !q.statuses().isEmpty()) {
            sql.append(" AND f.status IN (:statuses)");
            p.addValue("statuses", q.statuses());
        }
        if (q.regions() != null && !q.regions().isEmpty()) {
            sql.append(" AND f.region_codes && CAST(:regions AS text[])");
            p.addValue("regions", ai.foresignal.store.query.PgArrays.text(q.regions()));
        }
        if (q.ref() != null) {
            sql.append(" AND :ref = ANY(f.series_refs)");
            p.addValue("ref", q.ref());
        }
        if (q.since() != null) {
            sql.append(" AND f.detected_at >= :since");
            p.addValue("since", Timestamp.from(q.since()));
        }
        if (q.situationId() != null) {
            sql.append(" AND (f.situation_id = :situation OR f.id IN (SELECT m.ref FROM situation_member m WHERE"
                    + " m.situation_id = :situation AND m.member_type = 'finding'))");
            p.addValue("situation", q.situationId());
        }
        String[] after = decode(q.cursor());
        if (after != null) {
            sql.append(" AND (f.detected_at, f.id) < (:afterAt, :afterId)");
            p.addValue("afterAt", Timestamp.from(Instant.parse(after[0])));
            p.addValue("afterId", after[1]);
        }
        sql.append(" ORDER BY f.detected_at DESC, f.id DESC LIMIT :limit");
        p.addValue("limit", q.limit() + 1);
        List<String[]> keys = jdbc.query(sql.toString(), p, (rs, n) -> new String[] {rs.getString(1),
            rs.getTimestamp(2).toInstant().toString()});
        boolean more = keys.size() > q.limit();
        List<AnalyticFindingEntity> items = new ArrayList<>();
        for (int i = 0; i < keys.size() && i < q.limit(); i++) {
            findings.findById(keys.get(i)[0]).ifPresent(items::add);
        }
        String next = null;
        if (more && !items.isEmpty()) {
            String[] last = keys.get(q.limit() - 1);
            next = encode(last[1], last[0]);
        }
        return new Page(items, next);
    }

    /** The situation a finding is a member of (118), newest link first, or empty. */
    @Transactional(readOnly = true)
    public Optional<String> linkedSituation(AnalyticFindingEntity f) {
        if (f.getSituationId() != null) {
            return Optional.of(f.getSituationId());
        }
        List<String> ids = jdbc.queryForList("SELECT situation_id FROM situation_member WHERE member_type = 'finding'"
                + " AND ref = :ref ORDER BY added_at DESC LIMIT 1", new MapSqlParameterSource("ref", f.getId()),
                String.class);
        return ids.isEmpty() ? Optional.empty() : Optional.of(ids.get(0));
    }

    /**
     * Links a forecast question to a finding (prompt 122): appends the question id to {@code question_ids} once. The
     * finding's content, status and version are unchanged.
     *
     * @return true when the link was added
     */
    @Transactional
    public boolean linkQuestion(String findingId, String questionId) {
        return jdbc.update("UPDATE analytic_finding SET question_ids = array_append(coalesce(question_ids,"
                + " CAST('{}' AS text[])), :q) WHERE id = :id AND NOT (:q = ANY(coalesce(question_ids,"
                + " CAST('{}' AS text[]))))", new MapSqlParameterSource("id", findingId).addValue("q", questionId)) > 0;
    }

    /** The open findings of a type detected in {@code [from, to]}, oldest first. */
    @Transactional(readOnly = true)
    public List<AnalyticFindingEntity> detectedBetween(String type, Instant from, Instant to) {
        return findings.findByFindingTypeAndDetectedAtBetweenOrderByDetectedAtAsc(type, from, to);
    }

    /**
     * Announces a precursor lifecycle change on the {@code analytics} topic (prompt 120): a log row whose
     * {@code finding_id} is the precursor id and whose event is {@code precursor.validated}, {@code precursor.stale}
     * or {@code precursor.retired}, published after commit and replayed like finding events.
     *
     * @param precursorId the precursor
     * @param event the {@code precursor.*} event
     * @param actor who changed it (a detector, a token id or {@code operator})
     * @param reason why, or null
     */
    @Transactional
    public void announcePrecursor(String precursorId, String event, String actor, String reason) {
        Objects.requireNonNull(precursorId, "precursorId");
        if (event == null || !event.startsWith("precursor.")) {
            throw new IllegalArgumentException("not a precursor event: " + event);
        }
        FindingLogEntity row = logs.save(new FindingLogEntity(precursorId, clock.instant(), actor,
                event.replace('.', '-'), event, reason));
        publishAfterCommit(List.of(row), null);
    }

    /**
     * Logs and announces an {@code outlook.*} or {@code index.*} event (prompt 126) or an {@code impact.*} event (prompt
     * 127) on the {@code analytics} topic; its payload is resolved through {@link AnalyticsEventSummaries} when the event
     * is sent or replayed.
     *
     * @param objectId the outlook, index value, index fit or projected-impact finding id (at most 26 characters)
     */
    @Transactional
    public void announce(String objectId, String event, String actor, String reason) {
        Objects.requireNonNull(objectId, "objectId");
        if (event == null || !FindingEvent.flat(event)) {
            throw new IllegalArgumentException("not an outlook, index or impact event: " + event);
        }
        FindingLogEntity row = logs.save(new FindingLogEntity(objectId, clock.instant(), actor,
                event.replace('.', '-').replace('_', '-'), event, reason));
        publishAfterCommit(List.of(row), null);
    }

    /** The event of a log row, or null when the finding (or precursor) no longer exists. */
    @Transactional(readOnly = true)
    public FindingEvent eventOf(FindingLogEntity row) {
        if (row.getEvent() != null && FindingEvent.flat(row.getEvent())) {
            Map<String, Object> summary = null;
            if (eventSummaries != null) {
                for (AnalyticsEventSummaries s : eventSummaries) {
                    if (s.handles(row.getEvent())) {
                        summary = s.summary(row.getEvent(), row.getFindingId());
                        break;
                    }
                }
            }
            return summary == null ? null : new FindingEvent(row.getId(), row.getEvent(), row.getFindingId(), summary,
                    row.getAt());
        }
        if (row.getEvent() != null && row.getEvent().startsWith("precursor.")) {
            PrecursorSummaries summaries = precursorSummaries == null ? null : precursorSummaries.getIfAvailable();
            Map<String, Object> summary = summaries == null ? null : summaries.summary(row.getFindingId());
            return summary == null ? null : new FindingEvent(row.getId(), row.getEvent(), row.getFindingId(), summary,
                    row.getAt());
        }
        AnalyticFindingEntity f = findings.findById(row.getFindingId()).orElse(null);
        if (f == null || row.getEvent() == null) {
            return null;
        }
        return new FindingEvent(row.getId(), row.getEvent(), f.getId(), FindingViews.summary(f,
                linkedSituation(f).orElse(null)), row.getAt());
    }

    // ------------------------------------------------------------------------- helpers

    static boolean overlaps(AnalyticFindingEntity open, FindingDraft draft) {
        Instant aStart = open.getWindowStart() != null ? open.getWindowStart() : open.getDetectedAt();
        Instant aEnd = open.getWindowEnd() != null ? open.getWindowEnd() : open.getDetectedAt();
        Instant bStart = draft.windowStart() != null ? draft.windowStart() : draft.detectedAt();
        Instant bEnd = draft.windowEnd() != null ? draft.windowEnd() : draft.detectedAt();
        if (aStart == null || aEnd == null || bStart == null || bEnd == null) {
            return true;
        }
        return !aEnd.isBefore(bStart) && !bEnd.isBefore(aStart);
    }

    /**
     * The draft's caveats, plus {@code BENCHMARK_FAIL} while the latest benchmark of the component behind the finding
     * type failed (prompt 123, BEN-2): the detector keeps running and its findings say so.
     */
    List<Map<String, Object>> withBenchmarkCaveat(FindingDraft draft) {
        ai.foresignal.benchmark.BenchmarkGate gate = benchmarkGate == null ? null : benchmarkGate.getIfAvailable();
        if (gate == null) {
            return draft.caveats();
        }
        String code = benchmarkCaveatCode(gate, draft.findingType());
        if (code == null || draft.caveats().stream().anyMatch(c -> code.equals(c.get("code")))) {
            return draft.caveats();
        }
        List<Map<String, Object>> out = new ArrayList<>(draft.caveats());
        out.add(FindingCaveats.of(code, "The latest analytic benchmark of this detector failed its thresholds; treat"
                + " the finding with extra caution until a benchmark run passes (GET /v1/benchmarks/status)."));
        return out;
    }

    private static String benchmarkCaveatCode(ai.foresignal.benchmark.BenchmarkGate gate, String findingType) {
        try {
            return gate.findingCaveat(findingType);
        } catch (RuntimeException e) {
            return null;
        }
    }

    static boolean same(AnalyticFindingEntity open, FindingDraft draft) {
        return Objects.equals(open.getSeverity(), draft.severity()) && Objects.equals(open.getTitle(),
                clip(draft.title())) && Objects.equals(open.getWindowStart(), draft.windowStart())
                && Objects.equals(open.getWindowEnd(), draft.windowEnd())
                && Objects.equals(normalized(open.getStats()), normalized(draft.stats()));
    }

    /** The statistics with numbers compared as doubles (jsonb round-trips integers and longs alike). */
    private static Object normalized(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> out = new java.util.TreeMap<>();
            map.forEach((k, v) -> out.put(String.valueOf(k), normalized(v)));
            return out;
        }
        if (value instanceof List<?> list) {
            return list.stream().map(FindingService::normalized).toList();
        }
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        return value;
    }

    private Geometry regionPoint(List<String> regionCodes) {
        CountryCentroids table = countries.getIfAvailable();
        if (table == null || regionCodes == null) {
            return null;
        }
        for (String code : regionCodes) {
            if (code == null || code.length() < 2 || "ZZ".equals(code)) {
                continue;
            }
            Optional<CountryRef> country = table.byIso2(code.substring(0, 2));
            if (country.isPresent()) {
                return GEOMETRY.createPoint(new Coordinate(country.get().labelLon(), country.get().labelLat()));
            }
        }
        return null;
    }

    private String manifest(FindingDraft draft) {
        ManifestService service = manifests.getIfAvailable();
        if (service == null) {
            return null;
        }
        List<String> inputs = new ArrayList<>(draft.seriesRefs());
        inputs.addAll(draft.members());
        try {
            return service.record(ManifestRequest.of(draft.detectorName(), draft.detectorVersion())
                    .config(draft.detectorParams()).registries("signal-families.json").asOf(draft.asOfSeq())
                    .inputs(inputs));
        } catch (RuntimeException e) {
            log.warn("Could not record the manifest of a {} finding: {}", draft.findingType(), e.toString());
            return null;
        }
    }

    private static String actor(FindingDraft draft) {
        return draft.detectorName() + ":" + draft.detectorVersion();
    }

    private static String clip(String title) {
        if (title == null) {
            return "Finding";
        }
        return title.length() > 500 ? title.substring(0, 500) : title;
    }

    private void publishAfterCommit(List<FindingLogEntity> rows, AnalyticFindingEntity created) {
        Runnable action = () -> {
            for (FindingLogEntity row : rows) {
                FindingEvent event = eventOf(row);
                if (event != null) {
                    bus.publish(event);
                }
            }
            if (created != null && FindingSeverities.SIGNIFICANT.equals(created.getSeverity())
                    && !created.isPrivateFinding()) {
                FindingNotifications n = notifications.getIfAvailable();
                if (n != null) {
                    try {
                        n.send(created);
                    } catch (RuntimeException e) {
                        log.warn("The finding.significant notification of {} failed: {}", created.getId(),
                                e.toString());
                    }
                }
            }
        };
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }

    private static String encode(String at, String id) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((at + "\u0000" + id).getBytes(StandardCharsets.UTF_8));
    }

    private static String[] decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String[] parts = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8)
                    .split("\u0000", -1);
            if (parts.length == 2) {
                Instant.parse(parts[0]);
                return parts;
            }
        } catch (RuntimeException e) {
            // fall through
        }
        throw ValidationException.forField("cursor", "is not a cursor of this list");
    }
}
