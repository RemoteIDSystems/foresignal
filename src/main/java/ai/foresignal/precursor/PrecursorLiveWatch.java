package ai.foresignal.precursor;

import ai.foresignal.analytics.AnalyticsSupport;
import ai.foresignal.analytics.family.SignalFamilyRegistry;
import ai.foresignal.analytics.finding.FindingCaveats;
import ai.foresignal.analytics.finding.FindingDraft;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.analytics.finding.FindingSeverities;
import ai.foresignal.analytics.series.SeriesData;
import ai.foresignal.analytics.series.SeriesFrequency;
import ai.foresignal.analytics.series.SeriesReader;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.evidence.EvidenceService;
import ai.foresignal.evidence.RecordRef;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.intel.IntelCalibrationService;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.store.query.SeqClockQuery;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The precursor live watch (prompt 120 §5): when an occurrence matches the antecedent of a {@code validated} event
 * precursor, or a series antecedent crosses its standardized threshold (2.0) in the expected direction, a
 * {@code precursor-active} finding is recorded with the precursor id, the expected consequent window and the probability
 * {@code (hits + 1) / (n + 2)} shrunk toward the base rate by {@code n / (n + 20)}. Prompt 122 subscribes to these
 * findings to open forecast questions.
 *
 * <p><b>Cursors.</b> New occurrences are read in commit order from the typed logs behind the change and alert streams:
 * the intel event log ({@code created}), the situation log ({@code situation.activated}), the finding log
 * ({@code created} change points and unexplained moves), alerts and anomaly observations, each by its own id cursor
 * ({@code precursor.cursor.streams}). The durable seq cursor {@code precursor.cursor} records the change-stream position
 * each poll has passed and joins retention's consumer clamp (RET-2). On first start the watch begins at the heads,
 * never re-scanning history. A restart from an older cursor writes nothing twice: a re-found occurrence produces an
 * identical finding, which {@link FindingService#record} leaves unchanged.
 *
 * <p>Private occurrences never trigger a finding; alerts are collapsed to their 116 canonical events.
 */
@Service
public class PrecursorLiveWatch implements IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(PrecursorLiveWatch.class);

    /** The detector name of {@code precursor-active} findings. */
    public static final String DETECTOR = "precursor-watch";

    /** The detector version. */
    public static final String VERSION = "1.0.0";

    /** Rows read per stream per poll page. */
    static final int PAGE = 1_000;

    /** The buckets of history the series check reads. */
    static final int SERIES_BUCKETS = 120;

    private static final List<String> SEVERITY_ORDER = List.of("info", "notable", "warning", "significant",
            "critical");

    private final PrecursorRepository precursors;
    private final FindingService findings;
    private final SignalFamilyRegistry families;
    private final SeriesReader reader;
    private final AppSettingService settings;
    private final SeqClockQuery seqClock;
    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectProvider<IntelVisibility> visibility;
    private final ObjectProvider<EvidenceService> evidence;
    private final ObjectProvider<IntelCalibrationService> calibration;
    private final JsonMapper json;
    private final Clock clock;
    private final ReentrantLock lock = new ReentrantLock();
    private volatile Cursors cursors;

    /** Creates the watch. */
    public PrecursorLiveWatch(PrecursorRepository precursors, FindingService findings, SignalFamilyRegistry families,
            SeriesReader reader, AppSettingService settings, SeqClockQuery seqClock, NamedParameterJdbcTemplate jdbc,
            ObjectProvider<IntelVisibility> visibility, ObjectProvider<EvidenceService> evidence,
            ObjectProvider<IntelCalibrationService> calibration, JsonMapper json, Clock clock) {
        this.precursors = precursors;
        this.findings = findings;
        this.families = families;
        this.reader = reader;
        this.settings = settings;
        this.seqClock = seqClock;
        this.jdbc = jdbc;
        this.visibility = visibility;
        this.evidence = evidence;
        this.calibration = calibration;
        this.json = json;
        this.clock = clock;
    }

    /**
     * The watch position.
     *
     * @param seq the change-stream seq passed (the durable consumer-clamp cursor)
     * @param intel the last intel event log id read
     * @param situations the last situation log id read
     * @param findings the last finding log id read
     * @param alerts the last alert id read
     * @param anomalies the last anomaly observation id read
     */
    public record Cursors(long seq, long intel, long situations, long findings, long alerts, long anomalies) {

        /** The stream cursors as stored. */
        Map<String, Object> streams() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("intel", intel);
            out.put("situations", situations);
            out.put("findings", findings);
            out.put("alerts", alerts);
            out.put("anomalies", anomalies);
            return out;
        }
    }

    /**
     * What a poll did.
     *
     * @param occurrences matching antecedent occurrences found
     * @param findings findings written (new or superseding; identical re-finds are not counted)
     * @param cursors the position afterwards
     */
    public record PollResult(int occurrences, int findings, Cursors cursors) {}

    /**
     * A new occurrence on one of the streams.
     *
     * @param keys the selector keys it matches ({@code intelEventType:x}, {@code alertRuleTag:t}, …)
     * @param at when it happened
     * @param ref its object reference
     * @param regions its 040 regions
     * @param severity its severity, or null
     * @param identity the key that makes two occurrences the same (a canonical event for alerts), for dedup
     */
    record LiveOccurrence(Set<String> keys, Instant at, String ref, List<String> regions, String severity,
            String identity) {}

    /** The durable seq cursor (0 before the first poll). */
    public long cursor() {
        return cursors().seq();
    }

    /** The current position. */
    public Cursors cursors() {
        Cursors value = cursors;
        if (value == null) {
            value = load();
            cursors = value;
        }
        return value;
    }

    @Override
    public void resetState() {
        cursors = load();
    }

    /** Moves the position (tests restart the watch from an older cursor). */
    public void rewind(Cursors position) {
        store(position);
    }

    /** Reads every stream up to its head, writes the findings of matching occurrences and advances the cursors. */
    public PollResult poll() {
        lock.lock();
        try {
            Instant now = clock.instant();
            long seqHead = seqClock.seqAt(jdbc, now);
            Cursors from = cursors();
            if (from.seq() == 0) {
                Cursors heads = new Cursors(Math.max(1, seqHead), head("intelligence_event_log"), head("situation_log"),
                        head("analytic_finding_log"), head("alert"), head("anomaly_observation"));
                store(heads);
                return new PollResult(0, 0, heads);
            }
            List<PrecursorEntity> validated = precursors.findByKindAndStatusOrderByIdAsc(Candidate.EVENT,
                    PrecursorRules.VALIDATED);
            long[] next = {from.intel(), from.situations(), from.findings(), from.alerts(), from.anomalies()};
            List<LiveOccurrence> found = new ArrayList<>();
            next[0] = readIntel(from.intel(), found);
            next[1] = readSituations(from.situations(), found);
            next[2] = readFindings(from.findings(), found);
            next[3] = readAlerts(from.alerts(), found);
            next[4] = readAnomalies(from.anomalies(), found);
            int matches = 0;
            int written = 0;
            for (LiveOccurrence o : found) {
                for (PrecursorEntity p : validated) {
                    Map<String, Object> selector = Candidate.selector(p.getAntecedent());
                    if (selector == null || !matches(selector, o)) {
                        continue;
                    }
                    matches++;
                    try {
                        if (record(p, o, now, seqHead)) {
                            written++;
                        }
                    } catch (RuntimeException e) {
                        log.warn("precursor-active finding of {} for {} failed: {}", p.getId(), o.ref(), e.toString());
                    }
                }
            }
            Cursors after = new Cursors(Math.max(from.seq(), seqHead), next[0], next[1], next[2], next[3], next[4]);
            store(after);
            return new PollResult(matches, written, after);
        } finally {
            lock.unlock();
        }
    }

    /** True when an occurrence matches a selector's form, regions and minimum severity. */
    static boolean matches(Map<String, Object> selector, LiveOccurrence o) {
        String key = selectorKey(selector);
        if (key == null || !o.keys().contains(key)) {
            return false;
        }
        Set<String> regions = new LinkedHashSet<>();
        if (selector.get("regionCodes") instanceof List<?> list) {
            for (Object r : list) {
                regions.add(String.valueOf(r));
            }
        }
        if (!regions.isEmpty() && !PrecursorOccurrences.inRegions(o.regions(), regions)) {
            return false;
        }
        Object min = selector.get("minSeverity");
        if (min != null) {
            int need = SEVERITY_ORDER.indexOf(min.toString());
            return o.severity() != null && SEVERITY_ORDER.indexOf(o.severity()) >= need;
        }
        return true;
    }

    /** The key an occurrence must carry to match a selector, or null for forms the watch cannot see live. */
    static String selectorKey(Map<String, Object> selector) {
        if (selector.get("situationActivated") != null) {
            Object theme = selector.get("situationActivated") instanceof Map<?, ?> m ? m.get("theme") : null;
            return "situationActivated:" + (theme == null ? "*" : theme);
        }
        if (selector.get("timestamps") != null) {
            return null;
        }
        String key = PrecursorOccurrences.familyKey(selector);
        return "unknown".equals(key) ? null : key;
    }

    /** Records the finding of one match; true when a finding was written (not an identical re-find). */
    boolean record(PrecursorEntity p, LiveOccurrence o, Instant now, long asOfSeq) {
        Instant expectedFrom = o.at().plusSeconds(p.getLagMinSeconds());
        Instant expectedTo = o.at().plusSeconds(p.getLagMaxSeconds());
        List<String> regions = o.regions().isEmpty() ? p.getRegionCodes() : o.regions();
        return write(p, o.ref(), o.identity(), o.at(), expectedFrom, expectedTo, regions, now, asOfSeq, null);
    }

    private boolean write(PrecursorEntity p, String ref, String identity, Instant at, Instant expectedFrom,
            Instant expectedTo, List<String> regions, Instant now, long asOfSeq, Double z) {
        double base = p.getBaseRate() == null ? Double.NaN : p.getBaseRate();
        double raw = PrecursorRules.probability(p.getHits(), p.getNAntecedent(), base);
        // Prompt 159/151: report the SCOPE_PRECURSOR-calibrated probability where a fitted mapping exists (its scope
        // has at least CORP_MIN resolved precursor outcomes), otherwise the raw shrink-to-base-rate probability
        // unchanged, exactly as intel-event confidence is remapped. The raw value is kept in stats.rawProbability so
        // the calibration layer keeps training on raw scores (PrecursorOutcomeResolver reads it), never on an
        // already-calibrated probability.
        IntelCalibrationService cal = calibration.getIfAvailable();
        Double mapped = cal == null ? null : cal.calibrate(IntelCalibrationService.SCOPE_PRECURSOR, raw).orElse(null);
        boolean isCalibrated = mapped != null;
        double probability = isCalibrated ? mapped : raw;
        String consequent = PrecursorViews.label(p.getConsequent());
        String antecedent = PrecursorViews.label(p.getAntecedent());
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("precursorId", p.getId());
        stats.put("expectedFrom", expectedFrom.toString());
        stats.put("expectedTo", expectedTo.toString());
        stats.put("probability", AnalyticsSupport.round(probability));
        stats.put("rawProbability", AnalyticsSupport.round(raw));
        stats.put("calibrated", isCalibrated);
        stats.put("hits", p.getHits());
        stats.put("n", p.getNAntecedent());
        stats.put("hitRate", p.getHitRate());
        stats.put("baseRate", p.getBaseRate());
        stats.put("lift", p.getLift());
        stats.put("shrinkWeight", AnalyticsSupport.round(PrecursorRules.shrinkWeight(p.getNAntecedent())));
        stats.put("antecedentRef", ref);
        stats.put("antecedentAt", at.toString());
        if (z != null) {
            stats.put("z", AnalyticsSupport.round(z));
        }
        Map<String, Object> baseRate = new LinkedHashMap<>();
        baseRate.put("value", p.getBaseRate());
        baseRate.put("unit", "probability");
        baseRate.put("method", "precursor-base-rate");
        baseRate.put("n", p.getNAntecedent());
        baseRate.put("text", isCalibrated
                ? "By chance alone (circular-shift baseline) the consequent followed in about " + PrecursorText.pct(base)
                        + " of comparable windows; the precursor's hit rate shrunk toward that rate by sample size is "
                        + PrecursorText.pct(raw) + ", recalibrated against resolved precursor outcomes to the stated "
                        + PrecursorText.pct(probability) + "."
                : "By chance alone (circular-shift baseline) the consequent followed in about " + PrecursorText.pct(base)
                        + " of comparable windows; the stated probability " + PrecursorText.pct(probability)
                        + " is the precursor's hit rate shrunk toward that rate by sample size.");
        List<Map<String, Object>> caveats = new ArrayList<>();
        caveats.add(FindingCaveats.notGroundTruth());
        caveats.add(FindingCaveats.of(FindingCaveats.MULTIPLE_TESTS, "Found among " + p.getBatchSize()
                + " tests of one mining batch with false-discovery control, then validated on later data."));
        if (p.isOperatorValidated()) {
            caveats.add(FindingCaveats.of("OPERATOR_VALIDATED", "An operator pinned this precursor as validated."));
        }
        for (Map<String, Object> c : p.getCaveats()) {
            Object code = c.get("code");
            if (code != null && List.of(FindingCaveats.LOW_DETECTABILITY, FindingCaveats.DAILY_RESOLUTION_ONLY,
                    "CO_OCCURRENCE", "CO_MOVEMENT").contains(code.toString())) {
                caveats.add(c);
            }
        }
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("precursorId", p.getId());
        params.put("shrinkPrior", PrecursorRules.SHRINK_PRIOR);
        if (z != null) {
            params.put("threshold", threshold(p));
        }
        String title = "Precursor active: " + antecedent + "; " + consequent + " expected by "
                + expectedTo.toString().substring(0, 10);
        String explanation = (p.getDescription() == null ? "" : p.getDescription() + " ") + "It occurred at " + at
                + "; " + consequent + " is expected between " + expectedFrom + " and " + expectedTo + " with probability "
                + PrecursorText.pct(probability) + " (" + PrecursorText.ASSOCIATION_NOTE + ").";
        List<String> members = new ArrayList<>();
        if (ref != null) {
            members.add(ref);
        }
        members.add("precursor:" + p.getId());
        FindingDraft draft = new FindingDraft("precursor-active", title, FindingSeverities.NOTABLE, null, regions,
                expectedFrom, expectedTo, now, Candidate.SERIES.equals(p.getKind())
                        ? seriesRefs(p) : List.of(), members, asOfSeq, stats, baseRate, caveats, DETECTOR, VERSION,
                params, explanation, false, consequentFamily(p), List.of(), List.of(),
                p.getId() + "|" + (identity == null ? ref : identity));
        FindingService.Outcome outcome = findings.record(draft);
        return outcome.change() != FindingService.Change.UNCHANGED;
    }

    private static List<String> seriesRefs(PrecursorEntity p) {
        List<String> out = new ArrayList<>();
        for (Map<String, Object> side : List.of(p.getAntecedent(), p.getConsequent())) {
            String ref = Candidate.seriesRef(side);
            if (ref != null) {
                out.add(ref);
            }
        }
        return out;
    }

    private String consequentFamily(PrecursorEntity p) {
        Object family = p.getConsequent().get("family");
        return family == null ? null : families.canonical(family.toString()).orElse(null);
    }

    private static double threshold(PrecursorEntity p) {
        Object t = p.getAntecedent().get("threshold");
        return t instanceof Number n && n.doubleValue() > 0 ? n.doubleValue() : PrecursorRules.LIVE_THRESHOLD;
    }

    // ------------------------------------------------------------------------- series antecedents

    /**
     * Checks every validated series precursor: when the antecedent's latest bucket, standardized against its own
     * baseline as of {@code now} (only points known by then, INT-2), is at or beyond the threshold, a
     * {@code precursor-active} finding is recorded for the bucket at the best lag.
     *
     * @param now the decision time
     * @return findings written
     */
    public int scanSeries(Instant now) {
        int written = 0;
        long asOfSeq = seqClock.seqAt(jdbc, now);
        for (PrecursorEntity p : precursors.findByKindAndStatusOrderByIdAsc(Candidate.SERIES,
                PrecursorRules.VALIDATED)) {
            try {
                if (checkSeries(p, now, asOfSeq)) {
                    written++;
                }
            } catch (RuntimeException e) {
                log.warn("Series check of precursor {} failed: {}", p.getId(), e.toString());
            }
        }
        return written;
    }

    private boolean checkSeries(PrecursorEntity p, Instant now, long asOfSeq) {
        String ref = Candidate.seriesRef(p.getAntecedent());
        if (ref == null || p.getBestLagSeconds() == null) {
            return false;
        }
        SeriesRef parsed = SeriesRef.parse(ref);
        SeriesFrequency frequency = reader.meta(parsed).frequency();
        Object stored = p.getDetector() != null && p.getDetector().get("params") instanceof Map<?, ?> params
                ? params.get("frequency") : null;
        SeriesFrequency target = "week".equals(stored) || frequency == SeriesFrequency.WEEK ? SeriesFrequency.WEEK
                : SeriesFrequency.DAY;
        Duration width = target == SeriesFrequency.WEEK ? Duration.ofDays(7) : Duration.ofDays(1);
        Instant end = target.floor(now);
        SeriesData data = reader.read(parsed, end.minus(width.multipliedBy(SERIES_BUCKETS)), end, now);
        if (data.frequency() != target) {
            data = data.resample(target);
        }
        SeriesData.Grid grid = data.grid().knownBy(now);
        int last = grid.size() - 1;
        while (last >= 0 && grid.gap()[last]) {
            last--;
        }
        if (last < 0) {
            return false;
        }
        double[] residuals = Prewhitening.residuals(grid.values(), data.counts(), target == SeriesFrequency.DAY ? 7 : 1);
        double[] z = Prewhitening.standardize(residuals);
        if (!Double.isFinite(z[last]) || Math.abs(z[last]) < threshold(p)) {
            return false;
        }
        Instant at = grid.starts()[last];
        Instant expected = at.plusSeconds(p.getBestLagSeconds());
        return write(p, "series-point:" + ref + "@" + at, ref + "@" + at, at, expected, expected.plus(width),
                p.getRegionCodes(), now, asOfSeq, z[last]);
    }

    // ------------------------------------------------------------------------- streams

    private long readIntel(long after, List<LiveOccurrence> out) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT l.id, l.action, e.id AS event_id, e.type,"
                + " e.severity, e.start_time, e.region_tags::text AS tags, e.private FROM intelligence_event_log l LEFT"
                + " JOIN intelligence_event e ON e.id = l.event_id WHERE l.id > :after ORDER BY l.id LIMIT :limit",
                new MapSqlParameterSource("after", after).addValue("limit", PAGE));
        for (Map<String, Object> row : rows) {
            if (!"created".equals(row.get("action")) || row.get("event_id") == null
                    || Boolean.TRUE.equals(row.get("private"))) {
                continue;
            }
            out.add(new LiveOccurrence(Set.of("intelEventType:" + row.get("type")), instant(row.get("start_time")),
                    "intel-event:" + row.get("event_id"), tags(String.valueOf(row.get("tags"))),
                    (String) row.get("severity"), "intel-event:" + row.get("event_id")));
        }
        return lastId(rows, after);
    }

    private long readSituations(long after, List<LiveOccurrence> out) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT l.id, l.event, l.at, s.id AS situation_id, s.theme,"
                + " s.severity, array_to_string(s.region_codes, ',') AS regions, s.private FROM situation_log l LEFT"
                + " JOIN situation s ON s.id = l.situation_id WHERE l.id > :after ORDER BY l.id LIMIT :limit",
                new MapSqlParameterSource("after", after).addValue("limit", PAGE));
        for (Map<String, Object> row : rows) {
            if (!"situation.activated".equals(row.get("event")) || row.get("situation_id") == null
                    || Boolean.TRUE.equals(row.get("private"))) {
                continue;
            }
            out.add(new LiveOccurrence(Set.of("situationActivated:*", "situationActivated:" + row.get("theme")),
                    instant(row.get("at")), "situation:" + row.get("situation_id"), csv((String) row.get("regions")),
                    (String) row.get("severity"), "situation:" + row.get("situation_id") + "@" + row.get("id")));
        }
        return lastId(rows, after);
    }

    private long readFindings(long after, List<LiveOccurrence> out) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT l.id, l.action, f.id AS finding_id, f.finding_type,"
                + " f.signal_family, f.severity, coalesce(f.window_start, f.detected_at) AS at,"
                + " array_to_string(f.region_codes, ',') AS regions, f.private FROM analytic_finding_log l LEFT JOIN"
                + " analytic_finding f ON f.id = l.finding_id WHERE l.id > :after ORDER BY l.id LIMIT :limit",
                new MapSqlParameterSource("after", after).addValue("limit", PAGE));
        for (Map<String, Object> row : rows) {
            Object type = row.get("finding_type");
            if (!"created".equals(row.get("action")) || row.get("finding_id") == null
                    || Boolean.TRUE.equals(row.get("private")) || row.get("signal_family") == null
                    || !("change-point".equals(type) || "unexplained-move".equals(type))) {
                continue;
            }
            String family = families.canonical(row.get("signal_family").toString())
                    .orElse(row.get("signal_family").toString());
            String form = "change-point".equals(type) ? "changePoint" : "unexplainedMove";
            out.add(new LiveOccurrence(Set.of(form + ":" + family), instant(row.get("at")),
                    "finding:" + row.get("finding_id"), csv((String) row.get("regions")), (String) row.get("severity"),
                    "finding:" + row.get("finding_id")));
        }
        return lastId(rows, after);
    }

    /** The id of the last row read, or {@code after} when none. */
    private static long lastId(List<Map<String, Object>> rows, long after) {
        return rows.isEmpty() ? after : ((Number) rows.get(rows.size() - 1).get("id")).longValue();
    }

    private long readAlerts(long after, List<LiveOccurrence> out) {
        List<LiveOccurrence> page = new ArrayList<>();
        List<RecordRef> refs = new ArrayList<>();
        long[] last = {after};
        jdbc.query("SELECT id, rule_id::text AS rule, tags::text AS tags, region ->> 'iso2' AS iso2, region ->>"
                + " 'admin1' AS admin1, severity, fired_at, source_id, external_id, suppressed, exercise FROM alert"
                + " WHERE id > :after ORDER BY id LIMIT :limit", new MapSqlParameterSource("after", after)
                        .addValue("limit", PAGE), (ResultSet rs) -> {
                    last[0] = rs.getLong("id");
                    if (rs.getBoolean("suppressed") || rs.getBoolean("exercise") || privateSource(rs.getString(
                            "source_id"))) {
                        return;
                    }
                    Set<String> keys = new LinkedHashSet<>();
                    keys.add("alertRuleId:" + rs.getString("rule"));
                    for (String tag : strings(rs.getString("tags"))) {
                        keys.add("alertRuleTag:" + tag);
                    }
                    List<String> regions = new ArrayList<>();
                    if (rs.getString("iso2") != null) {
                        regions.add(rs.getString("iso2"));
                    }
                    if (rs.getString("admin1") != null) {
                        regions.add(rs.getString("admin1"));
                    }
                    String source = rs.getString("source_id");
                    String external = rs.getString("external_id");
                    refs.add(new RecordRef(source, external, null));
                    page.add(new LiveOccurrence(keys, timestamp(rs, "fired_at"), "alert:" + rs.getLong("id"), regions,
                            rs.getString("severity"), EvidenceService.recordKey(source, external)));
                });
        Map<String, EvidenceService.GroupKey> canonical = Map.of();
        EvidenceService service = evidence.getIfAvailable();
        if (service != null && !refs.isEmpty()) {
            try {
                canonical = service.groupKeys(refs);
            } catch (RuntimeException e) {
                canonical = Map.of();
            }
        }
        for (LiveOccurrence o : page) {
            EvidenceService.GroupKey key = canonical.get(o.identity());
            String identity = key != null && key.canonicalEventId() != null ? "canonical:" + key.canonicalEventId()
                    : o.ref();
            out.add(new LiveOccurrence(o.keys(), o.at(), o.ref(), o.regions(), o.severity(), identity));
        }
        return last[0];
    }

    private long readAnomalies(long after, List<LiveOccurrence> out) {
        long[] last = {after};
        jdbc.query("SELECT o.id, o.rule_id::text AS rule, o.classification, o.group_key, o.bucket_start FROM"
                + " anomaly_observation o WHERE o.id > :after ORDER BY o.id LIMIT :limit",
                new MapSqlParameterSource("after", after).addValue("limit", PAGE), (ResultSet rs) -> {
                    last[0] = rs.getLong("id");
                    String classification = rs.getString("classification");
                    if (!"activity-up".equals(classification) && !"activity-down".equals(classification)) {
                        return;
                    }
                    String rule = rs.getString("rule");
                    String[] region = PrecursorOccurrences.regionOfCode(rs.getString("group_key"));
                    List<String> regions = new ArrayList<>();
                    if (region[0] != null) {
                        regions.add(region[0]);
                    }
                    if (region[1] != null) {
                        regions.add(region[1]);
                    }
                    out.add(new LiveOccurrence(Set.of("anomaly:" + rule + ":" + classification, "anomaly:" + rule
                            + ":*"), timestamp(rs, "bucket_start"), "anomaly:" + rs.getLong("id"), regions, null,
                            "anomaly:" + rule + ":" + rs.getString("group_key") + "@" + rs.getTimestamp(
                                    "bucket_start").toInstant()));
                });
        return last[0];
    }

    private long head(String table) {
        Long max = jdbc.queryForObject("SELECT max(id) FROM " + table, new MapSqlParameterSource(), Long.class);
        return max == null ? 0 : max;
    }

    private boolean privateSource(String sourceId) {
        IntelVisibility v = visibility.getIfAvailable();
        return v != null && sourceId != null && v.isPrivateSource(sourceId);
    }

    private List<String> tags(String text) {
        List<String> out = new ArrayList<>();
        for (String code : strings(text)) {
            if (code.length() >= 2 && !"ZZ".equals(code)) {
                out.add(code);
                if (code.contains("-") && !out.contains(code.substring(0, 2))) {
                    out.add(code.substring(0, 2));
                }
            }
        }
        return out;
    }

    private List<String> strings(String text) {
        List<String> out = new ArrayList<>();
        if (text == null || text.isBlank() || "null".equals(text)) {
            return out;
        }
        try {
            for (JsonNode node : json.readTree(text)) {
                if (node.isString()) {
                    out.add(node.asString());
                }
            }
        } catch (RuntimeException e) {
            return out;
        }
        return out;
    }

    private static List<String> csv(String text) {
        return text == null || text.isBlank() ? List.of() : List.of(text.split(","));
    }

    private static Instant instant(Object value) {
        if (value instanceof java.sql.Timestamp ts) {
            return ts.toInstant();
        }
        if (value instanceof java.time.OffsetDateTime odt) {
            return odt.toInstant();
        }
        if (value instanceof Instant i) {
            return i;
        }
        return Instant.parse(String.valueOf(value));
    }

    private static Instant timestamp(ResultSet rs, String column) throws SQLException {
        return rs.getTimestamp(column).toInstant();
    }

    private Cursors load() {
        long seq;
        try {
            seq = settings.get(SettingKeys.PRECURSOR_CURSOR, Long.class).orElse(0L);
        } catch (RuntimeException e) {
            seq = 0;
        }
        Map<?, ?> streams;
        try {
            streams = settings.get(SettingKeys.PRECURSOR_STREAM_CURSORS, Map.class).orElse(Map.of());
        } catch (RuntimeException e) {
            streams = Map.of();
        }
        return new Cursors(seq, number(streams.get("intel")), number(streams.get("situations")),
                number(streams.get("findings")), number(streams.get("alerts")), number(streams.get("anomalies")));
    }

    private static long number(Object value) {
        return value instanceof Number n ? n.longValue() : 0L;
    }

    private void store(Cursors position) {
        settings.put(SettingKeys.PRECURSOR_STREAM_CURSORS, position.streams());
        settings.put(SettingKeys.PRECURSOR_CURSOR, position.seq());
        cursors = position;
    }
}
