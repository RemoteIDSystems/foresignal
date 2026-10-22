package ai.foresignal.social.correlation;

import ai.foresignal.analytics.finding.FindingDraft;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.observability.AbsenceCheck;
import ai.foresignal.social.SocialEvents;
import ai.foresignal.social.SocialObservability;
import ai.foresignal.social.SocialVocabulary;
import ai.foresignal.social.SocialVolumeStore;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.statistics.distribution.NormalDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * {@code AttentionGapDetector} 1.0.0 (prompt 137): significant physical events that drew far less public discussion
 * than similar past events.
 *
 * <p>Candidates are {@code significant} (or, by setting, {@code notable}) non-social findings, situation escalations
 * and critical alerts. A candidate's attention is the posts attributed to its region on the complete-stream platforms
 * in the {@code windowHours} (6) after it, normalized by the region's trailing 8-week baseline for those hours. The
 * expected distribution comes from past candidates of the same family and severity band in the trailing
 * {@code lookbackDays} (365): a log-normal fit (an intercept-only regression of {@code log(1 + attention)}) when at
 * least {@value #MIN_PAST} exist, else the empirical distribution of the region's past events of that family (the
 * region-family median as the expectation). When observed attention is below the {@code percentile} (10th) of the
 * expected distribution <em>and</em> 135 social observability is ok for the region and window (SOC-2), an
 * {@code attention-gap} finding is recorded with expected and observed volumes and the platforms covered; otherwise
 * nothing is recorded ({@code LOW_SOCIAL_COVERAGE} is logged).
 */
@Component
public class AttentionGapDetector {

    private static final Logger log = LoggerFactory.getLogger(AttentionGapDetector.class);

    /** The past events the regression needs. */
    public static final int MIN_PAST = 5;

    /** The past events the region-family fallback needs. */
    public static final int MIN_FALLBACK = 3;

    /** The past events read per candidate. */
    static final int PAST_LIMIT = 200;

    /** The candidates read per kind per pass. */
    static final int CANDIDATES = 200;

    /** The smallest log-scale spread used. */
    static final double MIN_SIGMA = 0.1;

    private static final NormalDistribution STANDARD = NormalDistribution.of(0, 1);

    /**
     * A physical event that may draw attention.
     *
     * @param ref its object ref ({@code finding:…}, {@code situation:…}, {@code alert:…})
     * @param title a title
     * @param familyKey what makes events comparable ({@code finding:{family}}, {@code situation:{family}},
     *     {@code alert-rule:{ruleId}})
     * @param band the severity band
     * @param regionCode its region
     * @param at the event time
     */
    public record Candidate(String ref, String title, String familyKey, String band, String regionCode, Instant at) {}

    /**
     * The attention one event drew.
     *
     * @param posts posts attributed to the region in the window
     * @param baseline the region's baseline for the window (posts)
     * @param platforms the complete-stream platforms counted
     */
    public record Attention(double posts, double baseline, List<String> platforms) {

        /** Posts over baseline, or null without a baseline. */
        public Double normalized() {
            return baseline > 0 ? posts / baseline : null;
        }
    }

    /**
     * The expected attention.
     *
     * @param expected the expected normalized attention
     * @param threshold the normalized attention at the percentile
     * @param method {@code regression} or {@code region-family-median}
     * @param n the past events used
     */
    public record Expected(double expected, double threshold, String method, int n) {}

    /** What social volume says about a region and window (overridable in tests). */
    public interface VolumeReader {

        /** The attention of a region over a window. */
        Attention attention(String regionCode, Instant from, Instant to);

        /** Social observability of a region over a window. */
        AbsenceCheck coverage(String regionCode, Instant from, Instant to);
    }

    private final JdbcTemplate jdbc;
    private final ObjectProvider<FindingService> findings;
    private final SocialSettingsService settings;
    private final SocialEvents events;
    private final SocialNotifications notifications;
    private final Clock clock;
    private final Set<String> evaluated = Collections.synchronizedSet(new LinkedHashSet<>());
    private VolumeReader reader;

    /** Creates the detector. */
    public AttentionGapDetector(JdbcTemplate jdbc, ObjectProvider<FindingService> findings,
            ObjectProvider<SocialVolumeStore> volume, ObjectProvider<SocialObservability> observability,
            SocialSettingsService settings, SocialEvents events, SocialNotifications notifications, Clock clock) {
        this.jdbc = jdbc;
        this.findings = findings;
        this.settings = settings;
        this.events = events;
        this.notifications = notifications;
        this.clock = clock;
        this.reader = new StoreReader(volume, observability);
    }

    /** Replaces the volume reader (tests). */
    public void setReader(VolumeReader reader) {
        this.reader = reader;
    }

    /** Forgets which candidates were evaluated (tests). */
    public void reset() {
        evaluated.clear();
    }

    // ------------------------------------------------------------------------- model

    /**
     * The expected attention. Pure.
     *
     * @param sameBand normalized attention of past events of the same family and band
     * @param regionFamily normalized attention of past events of the same family in the region
     * @param percentile the percentile (0–50) that marks a gap
     * @return the expectation, or null when there is too little history
     */
    public static Expected expected(List<Double> sameBand, List<Double> regionFamily, double percentile) {
        double q = Math.max(0.001, Math.min(0.5, percentile / 100.0));
        if (sameBand.size() >= MIN_PAST) {
            double mean = 0;
            for (double v : sameBand) {
                mean += Math.log1p(Math.max(0, v));
            }
            mean /= sameBand.size();
            double var = 0;
            for (double v : sameBand) {
                double d = Math.log1p(Math.max(0, v)) - mean;
                var += d * d;
            }
            double sigma = Math.max(MIN_SIGMA, Math.sqrt(var / Math.max(1, sameBand.size() - 1)));
            double threshold = Math.expm1(mean + STANDARD.inverseCumulativeProbability(q) * sigma);
            return new Expected(Math.expm1(mean), Math.max(0, threshold), "regression", sameBand.size());
        }
        if (regionFamily.size() >= MIN_FALLBACK) {
            List<Double> sorted = regionFamily.stream().map(v -> Math.max(0, v)).sorted().toList();
            return new Expected(LeadTimeStatistics.quantile(sorted, 0.5), LeadTimeStatistics.quantile(sorted, q),
                    "region-family-median", sorted.size());
        }
        return null;
    }

    /** True when observed normalized attention is a gap under an expectation. Pure. */
    public static boolean gap(double observed, Expected e) {
        return e != null && observed < e.threshold();
    }

    // ------------------------------------------------------------------------- pass

    /**
     * Examines the candidates whose window closed in the last two days.
     *
     * @return the findings recorded
     */
    public int run() {
        Instant now = clock.instant();
        SocialSettingsService.Gap g = settings.gap();
        Duration window = Duration.ofHours(g.windowHours());
        int n = 0;
        for (Candidate c : candidates(now.minus(Duration.ofDays(2)), now.minus(window), g.minSeverity())) {
            if (!evaluated.add(c.ref())) {
                continue;
            }
            if (evaluated.size() > 100_000) {
                evaluated.clear();
            }
            try {
                if (evaluate(c, g, now) != null) {
                    n++;
                }
            } catch (RuntimeException e) {
                log.warn("Attention-gap check of {} failed: {}", c.ref(), e.toString());
            }
        }
        return n;
    }

    /**
     * Evaluates one candidate.
     *
     * @return the finding id, or null when there is no gap, too little history or no adequate coverage
     */
    public String evaluate(Candidate c, SocialSettingsService.Gap g, Instant now) {
        Duration window = Duration.ofHours(g.windowHours());
        Instant to = c.at().plus(window);
        AbsenceCheck coverage = reader.coverage(c.regionCode(), c.at(), to);
        if (!coverage.ok()) {
            log.debug("Attention gap of {} not judged: {} ({})", c.ref(), SocialVocabulary.LOW_SOCIAL_COVERAGE,
                    coverage.reason());
            return null;
        }
        Attention observed = reader.attention(c.regionCode(), c.at(), to);
        if (observed.normalized() == null) {
            return null;
        }
        Instant historyFrom = c.at().minus(Duration.ofDays(g.lookbackDays()));
        List<Double> sameBand = new ArrayList<>();
        List<Double> regionFamily = new ArrayList<>();
        for (Candidate past : past(c, historyFrom, c.at().minus(window))) {
            Double v = reader.attention(past.regionCode(), past.at(), past.at().plus(window)).normalized();
            if (v == null) {
                continue;
            }
            if (past.band().equals(c.band())) {
                sameBand.add(v);
            }
            if (c.regionCode() != null && c.regionCode().equals(past.regionCode())) {
                regionFamily.add(v);
            }
        }
        Expected e = expected(sameBand, regionFamily, g.percentile());
        if (!gap(observed.normalized(), e)) {
            return null;
        }
        FindingService service = findings.getIfAvailable();
        if (service == null) {
            return null;
        }
        double expectedPosts = e.expected() * observed.baseline();
        FindingService.Outcome outcome = service.record(draft(c, observed, e, expectedPosts, g, now));
        if (outcome.change() == FindingService.Change.UNCHANGED) {
            return null;
        }
        String id = outcome.finding().getId();
        events.emit(CorrelationVocabulary.EVENT_GAP, gapEvent(id, c, expectedPosts, observed, now), false);
        notifications.sendGap(id, c.title(), c.ref(), expectedPosts, observed.posts(), observed.platforms());
        return id;
    }

    /** The finding draft. */
    public static FindingDraft draft(Candidate c, Attention observed, Expected e, double expectedPosts,
            SocialSettingsService.Gap g, Instant now) {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("ref", c.ref());
        stats.put("familyKey", c.familyKey());
        stats.put("severityBand", c.band());
        stats.put("regionCode", c.regionCode());
        stats.put("expected", expectedPosts);
        stats.put("observed", observed.posts());
        stats.put("expectedNormalized", e.expected());
        stats.put("observedNormalized", observed.normalized());
        stats.put("thresholdNormalized", e.threshold());
        stats.put("regionBaseline", observed.baseline());
        stats.put("method", e.method());
        stats.put("pastEvents", e.n());
        stats.put("platforms", observed.platforms());
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("percentile", g.percentile());
        params.put("lookbackDays", g.lookbackDays());
        params.put("windowHours", g.windowHours());
        params.put("minPast", MIN_PAST);
        List<Map<String, Object>> caveats = List.of(Map.of("code", "SOCIAL_IS_REPORTING", "text", "Attention measures"
                + " how much people discussed the event, not whether it happened or mattered; it is compared only where"
                + " social coverage of the region was adequate."));
        String explanation = String.format(Locale.ROOT, "In the %d h after %s, %.0f posts were attributed to %s on %s"
                + " (%.2f× the region's baseline), below the %.0fth percentile (%.2f×) of %d past %s events (%s);"
                + " expected about %.0f posts.", g.windowHours(), c.at(), observed.posts(), c.regionCode(),
                String.join(", ", observed.platforms()), observed.normalized(), g.percentile(), e.threshold(), e.n(),
                c.familyKey(), e.method(), expectedPosts);
        return new FindingDraft(CorrelationVocabulary.ATTENTION_GAP_FINDING, "Attention gap: " + c.title(), "notable",
                null, c.regionCode() == null ? List.of() : List.of(c.regionCode()), c.at(),
                c.at().plus(Duration.ofHours(g.windowHours())), now, List.of(), List.of(c.ref()), 0, stats, Map.of(),
                caveats, CorrelationVocabulary.GAP_DETECTOR, CorrelationVocabulary.GAP_DETECTOR_VERSION, params,
                explanation, false, "attention", List.of(), List.of(), c.ref());
    }

    /** The {@code AttentionGapEvent} body. */
    public static Map<String, Object> gapEvent(String findingId, Candidate c, double expected, Attention observed,
            Instant now) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("event", CorrelationVocabulary.EVENT_GAP);
        body.put("findingId", findingId);
        body.put("ref", c.ref());
        body.put("regionCodes", c.regionCode() == null ? List.of() : List.of(c.regionCode()));
        body.put("expected", expected);
        body.put("observed", observed.posts());
        body.put("platforms", observed.platforms());
        body.put("at", now.toString());
        return body;
    }

    // ------------------------------------------------------------------------- candidates

    /** The candidates of a time range. */
    List<Candidate> candidates(Instant from, Instant to, String minSeverity) {
        List<Candidate> out = new ArrayList<>();
        out.addAll(findingCandidates(from, to, null, null, null, minSeverity));
        out.addAll(situationCandidates(from, to, null, null, null));
        out.addAll(alertCandidates(from, to, null, null));
        return out;
    }

    /** The past events comparable to a candidate (same family key) between two times. */
    List<Candidate> past(Candidate c, Instant from, Instant to) {
        String key = c.familyKey();
        if (key.startsWith("finding:")) {
            return findingCandidates(from, to, key.substring(8), null, PAST_LIMIT, "notable");
        }
        if (key.startsWith("situation:")) {
            return situationCandidates(from, to, key.substring(10), null, PAST_LIMIT);
        }
        return alertCandidates(from, to, key.startsWith("alert-rule:") ? key.substring(11) : null, PAST_LIMIT);
    }

    List<Candidate> findingCandidates(Instant from, Instant to, String family, String band, Integer limit,
            String minSeverity) {
        String[] severities = "notable".equals(minSeverity) ? new String[] {"notable", "significant"}
                : new String[] {"significant"};
        List<Object> args = new ArrayList<>(List.of(severities, JdbcPhysicalEvidence.NON_PHYSICAL_FINDINGS.toArray(
                String[]::new), Timestamp.from(from), Timestamp.from(to)));
        String extra = "";
        if (family != null) {
            extra = " AND signal_family = ?";
            args.add(family);
        }
        args.add(limit == null ? CANDIDATES : limit);
        List<Candidate> out = new ArrayList<>();
        jdbc.query("SELECT id, title, signal_family, severity, region_codes, detected_at FROM analytic_finding WHERE"
                + " severity = ANY(?) AND NOT (finding_type = ANY(?)) AND signal_family IS NOT NULL AND status = 'open'"
                + " AND detected_at BETWEEN ? AND ?" + extra + " ORDER BY detected_at DESC, id LIMIT ?", rs -> {
                    String region = first(rs.getArray("region_codes"));
                    if (region != null) {
                        out.add(new Candidate("finding:" + rs.getString("id"), rs.getString("title"), "finding:"
                                + rs.getString("signal_family"), rs.getString("severity"), region,
                                rs.getTimestamp("detected_at").toInstant()));
                    }
                }, args.toArray());
        return out;
    }

    List<Candidate> situationCandidates(Instant from, Instant to, String family, String band, Integer limit) {
        List<Object> args = new ArrayList<>(List.of(Timestamp.from(from), Timestamp.from(to)));
        String extra = "";
        if (family != null) {
            extra = " AND ? = ANY(s.signal_families)";
            args.add(family);
        }
        args.add(limit == null ? CANDIDATES : limit);
        List<Candidate> out = new ArrayList<>();
        jdbc.query("SELECT s.id, s.title, s.signal_families, s.severity, s.region_codes, l.at FROM situation_log l"
                + " JOIN situation s ON s.id = l.situation_id WHERE l.event = 'situation.escalated' AND NOT s.private"
                + " AND l.at BETWEEN ? AND ?" + extra + " ORDER BY l.at DESC LIMIT ?", rs -> {
                    String region = first(rs.getArray("region_codes"));
                    String fam = family != null ? family : first(rs.getArray("signal_families"));
                    if (region != null && fam != null) {
                        out.add(new Candidate("situation:" + rs.getString("id"), rs.getString("title"), "situation:"
                                + fam, rs.getString("severity"), region, rs.getTimestamp("at").toInstant()));
                    }
                }, args.toArray());
        return out;
    }

    List<Candidate> alertCandidates(Instant from, Instant to, String ruleId, Integer limit) {
        List<Object> args = new ArrayList<>(List.of(Timestamp.from(from), Timestamp.from(to)));
        String extra = "";
        if (ruleId != null) {
            extra = " AND CAST(rule_id AS text) = ?";
            args.add(ruleId);
        }
        args.add(limit == null ? CANDIDATES : limit);
        List<Candidate> out = new ArrayList<>();
        jdbc.query("SELECT id, rule_id, kind, region, fired_at FROM alert WHERE severity = 'critical' AND NOT exercise"
                + " AND region IS NOT NULL AND fired_at BETWEEN ? AND ?" + extra + " ORDER BY fired_at DESC LIMIT ?",
                rs -> {
                    out.add(new Candidate("alert:" + rs.getLong("id"), "critical alert ("
                            + (rs.getString("kind") == null ? "alert" : rs.getString("kind")) + ")", "alert-rule:"
                            + rs.getString("rule_id"), "critical", rs.getString("region"),
                            rs.getTimestamp("fired_at").toInstant()));
                }, args.toArray());
        return out;
    }

    private static String first(java.sql.Array array) throws java.sql.SQLException {
        if (array == null) {
            return null;
        }
        Object[] values = (Object[]) array.getArray();
        return values.length == 0 || values[0] == null ? null : values[0].toString();
    }

    /** The volume reader over the 135 aggregate tables and social observability. */
    static final class StoreReader implements VolumeReader {

        private final ObjectProvider<SocialVolumeStore> volume;
        private final ObjectProvider<SocialObservability> observability;

        StoreReader(ObjectProvider<SocialVolumeStore> volume, ObjectProvider<SocialObservability> observability) {
            this.volume = volume;
            this.observability = observability;
        }

        @Override
        public Attention attention(String regionCode, Instant from, Instant to) {
            SocialVolumeStore store = volume.getIfAvailable();
            SocialObservability obs = observability.getIfAvailable();
            if (store == null || obs == null || regionCode == null) {
                return new Attention(0, 0, List.of());
            }
            double posts = 0;
            double baseline = 0;
            List<String> platforms = new ArrayList<>();
            double hours = Math.max(1, Duration.between(from, to).toMinutes() / 60.0);
            for (SocialObservability.Expectation e : obs.expected(regionCode, from, to)) {
                if (!e.complete() || e.baselinePostsPerHour() == null) {
                    continue;
                }
                platforms.add(e.platform());
                baseline += e.baselinePostsPerHour() * hours;
                for (SocialVolumeStore.CoverageRow r : store.coverage(e.platform(), regionCode, from, to)) {
                    posts += r.postsSeen();
                }
            }
            return new Attention(posts, baseline, platforms);
        }

        @Override
        public AbsenceCheck coverage(String regionCode, Instant from, Instant to) {
            SocialObservability obs = observability.getIfAvailable();
            return obs == null ? new AbsenceCheck(false, null, SocialVocabulary.LOW_SOCIAL_COVERAGE)
                    : obs.canSeeAbsence(regionCode, from, to);
        }
    }
}
