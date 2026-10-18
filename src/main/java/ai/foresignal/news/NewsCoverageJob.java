package ai.foresignal.news;

import ai.foresignal.analytics.finding.FindingDraft;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.attention.AttentionTopicEntity;
import ai.foresignal.attention.AttentionTopicRepository;
import ai.foresignal.evidence.Originator;
import ai.foresignal.evidence.OriginatorRegistry;
import ai.foresignal.media.outlet.MediaVocabulary;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The periodic news-coverage work (prompt 133), every 10 minutes ({@code foresignal.analytics.scheduled=false} turns it
 * off; tests call {@link #runOnce}):
 *
 * <ol>
 *   <li><strong>Corroboration</strong> (116): a story active within {@value #CORROBORATION_DAYS} days is
 *       {@code corroborated} when a non-news canonical event shares a region with it within
 *       {@value #CORROBORATION_SLACK_HOURS} hours of its articles and has an observation from a sensor-network or
 *       agency originator. Coverage never decides it.
 *   <li><strong>Situations:</strong> each active story records the situations it is a member of.
 *   <li><strong>Daily series:</strong> evidence groups and articles per UTC day and bucket ({@code left},
 *       {@code center}, {@code right}, {@code unrated}, {@code state}) for every enabled attention topic and every
 *       situation with news members, for the last {@value #RECOMPUTE_DAYS} days ({@code news_coverage_daily}, read as
 *       {@code rollup:news/{topicId|situationId}/{bucket}}).
 *   <li><strong>Findings</strong> (119): {@code coverage-blindspot} once per story with a blindspot;
 *       {@code coverage-divergence} when one bucket of a topic's last complete day rises at least
 *       {@value #DIVERGENCE_SD} standard deviations above its {@value #BASELINE_DAYS}-day baseline (at least
 *       {@value #MIN_BASELINE_DAYS} days with data, at least {@value #MIN_GROUPS} groups that day) while the other
 *       buckets do not.
 *   <li><strong>Retention:</strong> article rows older than 90 days (the articles' {@code bulk-event} class) are
 *       deleted; stories and daily series stay.
 * </ol>
 */
@Component
public class NewsCoverageJob {

    private static final Logger log = LoggerFactory.getLogger(NewsCoverageJob.class);

    /** How long after its last article a story's corroboration is still checked, days. */
    public static final int CORROBORATION_DAYS = 3;

    /** The time slack between a story and a corroborating event, hours. */
    public static final int CORROBORATION_SLACK_HOURS = 24;

    /** The days of daily series recomputed per run. */
    public static final int RECOMPUTE_DAYS = 3;

    /** The baseline of the divergence detector, days. */
    public static final int BASELINE_DAYS = 28;

    /** The days with data a divergence baseline needs. */
    public static final int MIN_BASELINE_DAYS = 14;

    /** The rise that makes a divergence, standard deviations. */
    public static final double DIVERGENCE_SD = 3.0;

    /** The least evidence groups of the rising bucket that day. */
    public static final int MIN_GROUPS = 3;

    /** The floor of the baseline standard deviation. */
    static final double SD_FLOOR = 1.0;

    /** Originator classes whose observations corroborate news. */
    static final Set<String> CORROBORATING = Set.of("sensor-network", "agency");

    /** The daily buckets. */
    public static final List<String> DAILY_BUCKETS = List.of(MediaVocabulary.LEFT, MediaVocabulary.CENTER,
            MediaVocabulary.RIGHT, MediaVocabulary.UNRATED, "state");

    /** The divergence detector name. */
    public static final String DIVERGENCE_DETECTOR = "coverage-divergence";

    /** The divergence detector version. */
    public static final String DIVERGENCE_VERSION = "1.0.0";

    private final NewsStoryStore store;
    private final NewsCoverageService coverage;
    private final JdbcTemplate jdbc;
    private final ObjectProvider<OriginatorRegistry> originators;
    private final ObjectProvider<FindingService> findings;
    private final ObjectProvider<AttentionTopicRepository> topics;
    private final Clock clock;
    private final boolean scheduled;

    /** Creates the job. */
    public NewsCoverageJob(NewsStoryStore store, NewsCoverageService coverage, JdbcTemplate jdbc,
            ObjectProvider<OriginatorRegistry> originators, ObjectProvider<FindingService> findings,
            ObjectProvider<AttentionTopicRepository> topics, Clock clock,
            @Value("${foresignal.analytics.scheduled:true}") boolean scheduled) {
        this.store = store;
        this.coverage = coverage;
        this.jdbc = jdbc;
        this.originators = originators;
        this.findings = findings;
        this.topics = topics;
        this.clock = clock;
        this.scheduled = scheduled;
    }

    /** The scheduled pass. */
    @Scheduled(fixedDelayString = "10m", initialDelayString = "6m")
    public void tick() {
        if (scheduled) {
            runOnce();
        }
    }

    /** Runs every step once. */
    public synchronized void runOnce() {
        Instant now = clock.instant();
        step("corroboration", () -> corroborate(now));
        step("situations", () -> situations(now));
        step("daily series", () -> daily(now));
        step("blindspot findings", () -> blindspots(now));
        step("divergence findings", () -> divergences(now));
        step("article retention", () -> store.deleteArticlesBefore(now.minus(Duration.ofDays(90))));
    }

    private static void step(String name, Runnable r) {
        try {
            r.run();
        } catch (RuntimeException e) {
            log.warn("News coverage step {} failed: {}", name, e.toString());
        }
    }

    // ------------------------------------------------------------------------- corroboration

    /** Recomputes the corroboration of the active stories; returns how many changed. */
    public int corroborate(Instant now) {
        int changed = 0;
        for (NewsStoryStore.Story s : store.activeSince(now.minus(Duration.ofDays(CORROBORATION_DAYS)), 5_000)) {
            List<String> by = corroboratingEvents(s);
            if (store.updateCorroboration(s.id(), !by.isEmpty(), by, now)) {
                coverage.refreshStory(s.id());
                changed++;
            }
        }
        return changed;
    }

    /** The non-news canonical events that corroborate a story (same region, same window, a sensor or agency). */
    List<String> corroboratingEvents(NewsStoryStore.Story s) {
        if (s.regionCodes().isEmpty() || s.firstSeenAt() == null || s.lastSeenAt() == null) {
            return List.of();
        }
        Duration slack = Duration.ofHours(CORROBORATION_SLACK_HOURS);
        Map<String, Set<String>> originatorsOf = new TreeMap<>();
        jdbc.query("SELECT c.id, l.originator_id FROM canonical_event c JOIN observation_link l ON l.canonical_event_id"
                        + " = c.id WHERE c.family <> ? AND c.status = 'open' AND c.region_codes && ? AND c.event_time"
                        + " BETWEEN ? AND ? ORDER BY c.id LIMIT 2000", rs -> {
                    originatorsOf.computeIfAbsent(rs.getString(1), k -> new LinkedHashSet<>()).add(rs.getString(2));
                }, NewsStoryResolver.FAMILY, s.regionCodes().toArray(String[]::new),
                Timestamp.from(s.firstSeenAt().minus(slack)), Timestamp.from(s.lastSeenAt().plus(slack)));
        OriginatorRegistry registry = originators.getIfAvailable();
        List<String> out = new ArrayList<>();
        for (Map.Entry<String, Set<String>> e : originatorsOf.entrySet()) {
            for (String id : e.getValue()) {
                String cls = registry == null || id == null ? null
                        : registry.originator(id).map(Originator::originatorClass).orElse(null);
                if (cls != null && CORROBORATING.contains(cls)) {
                    out.add(e.getKey());
                    break;
                }
            }
            if (out.size() >= 20) {
                break;
            }
        }
        return out;
    }

    // ------------------------------------------------------------------------- situations

    /** Records the situations of the stories active within a week. */
    public void situations(Instant now) {
        for (NewsStoryStore.Story s : store.activeSince(now.minus(Duration.ofDays(7)), 5_000)) {
            store.updateSituations(s.id(), coverage.situationsOfStory(s.id()), now);
        }
    }

    // ------------------------------------------------------------------------- daily series

    /** Recomputes the daily coverage rows of the last {@value #RECOMPUTE_DAYS} days. */
    public void daily(Instant now) {
        Instant today = now.truncatedTo(ChronoUnit.DAYS);
        AttentionTopicRepository repository = topics.getIfAvailable();
        List<String> topicIds = new ArrayList<>();
        if (repository != null) {
            for (AttentionTopicEntity t : repository.findByEnabledOrderByIdAsc(true)) {
                topicIds.add(t.getId());
            }
        }
        List<String> situationIds = jdbc.queryForList("SELECT DISTINCT situation_id FROM situation_member WHERE kind = ?"
                + " AND member_type = 'record' ORDER BY situation_id", String.class, NewsStoryRecordPublisher.KIND);
        for (int d = RECOMPUTE_DAYS - 1; d >= 0; d--) {
            Instant day = today.minus(Duration.ofDays(d));
            Instant end = day.plus(Duration.ofDays(1));
            for (String topicId : topicIds) {
                writeDay("topic", topicId, day, articlesOfTopic(topicId, day, end), now);
            }
            for (String situationId : situationIds) {
                writeDay("situation", situationId, day, articlesOfSituation(situationId, day, end), now);
            }
        }
    }

    private List<NewsStoryStore.Article> articlesOfTopic(String topicId, Instant from, Instant to) {
        List<String> stories = jdbc.queryForList("SELECT DISTINCT story_id FROM news_article WHERE ? = ANY(topic_ids)"
                + " AND seen_at >= ? AND seen_at < ? AND story_id IS NOT NULL", String.class, topicId,
                Timestamp.from(from), Timestamp.from(to));
        List<NewsStoryStore.Article> out = new ArrayList<>();
        for (List<NewsStoryStore.Article> list : store.articles(stories).values()) {
            for (NewsStoryStore.Article a : list) {
                if (a.topicIds().contains(topicId) && !a.seenAt().isBefore(from) && a.seenAt().isBefore(to)) {
                    out.add(a);
                }
            }
        }
        return out;
    }

    private List<NewsStoryStore.Article> articlesOfSituation(String situationId, Instant from, Instant to) {
        List<NewsStoryStore.Article> out = new ArrayList<>();
        for (List<NewsStoryStore.Article> list : store.articles(coverage.storiesOfSituation(situationId)).values()) {
            for (NewsStoryStore.Article a : list) {
                if (!a.seenAt().isBefore(from) && a.seenAt().isBefore(to)) {
                    out.add(a);
                }
            }
        }
        return out;
    }

    private void writeDay(String scopeType, String scopeId, Instant day, List<NewsStoryStore.Article> articles,
            Instant now) {
        CoverageAnalyzer.Result r = coverage.analyzeArticles(scopeType, articles, false, null);
        @SuppressWarnings("unchecked")
        Map<String, Integer> groups = (Map<String, Integer>) r.stats().get("evidenceGroups");
        @SuppressWarnings("unchecked")
        Map<String, Integer> counts = (Map<String, Integer>) r.stats().get("articles");
        for (String bucket : DAILY_BUCKETS) {
            int g;
            int a;
            if ("state".equals(bucket)) {
                g = (int) Math.round(r.stateAffiliatedShare() * r.groups());
                a = g;
            } else {
                g = groups.getOrDefault(bucket, 0);
                a = counts.getOrDefault(bucket, 0);
            }
            jdbc.update("INSERT INTO news_coverage_daily (id, scope_type, scope_id, day, bucket, evidence_groups,"
                            + " articles, computed_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO UPDATE SET"
                            + " evidence_groups = EXCLUDED.evidence_groups, articles = EXCLUDED.articles,"
                            + " computed_at = EXCLUDED.computed_at", dailyId(scopeType, scopeId, day, bucket), scopeType,
                    scopeId, Timestamp.from(day), bucket, g, a, Timestamp.from(now));
        }
    }

    /** The id of a daily row. */
    static String dailyId(String scopeType, String scopeId, Instant day, String bucket) {
        return scopeType + "|" + scopeId + "|" + day.toString().substring(0, 10) + "|" + bucket;
    }

    // ------------------------------------------------------------------------- findings

    /** Records a {@code coverage-blindspot} finding for every story with a blindspot and none yet. */
    public int blindspots(Instant now) {
        FindingService service = findings.getIfAvailable();
        if (service == null) {
            return 0;
        }
        List<String> ids = jdbc.queryForList("SELECT id FROM news_story WHERE blindspot IS NOT NULL AND"
                + " blindspot_finding_id IS NULL ORDER BY last_seen_at DESC LIMIT 200", String.class);
        int n = 0;
        for (NewsStoryStore.Story s : store.stories(ids).values()) {
            Long asOf = jdbc.queryForObject("SELECT max(seq) FROM news_article WHERE story_id = ?", Long.class, s.id());
            List<String> members = new ArrayList<>();
            members.add("canonical-event:" + s.id());
            for (NewsStoryStore.Article a : store.articles(s.id())) {
                if (members.size() > 20) {
                    break;
                }
                members.add("record:" + a.sourceId() + "/" + a.externalId() + "@" + a.seq());
            }
            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("storyId", s.id());
            stats.put("side", s.blindspot());
            stats.put("nRated", s.nRated());
            stats.put("coverageBalance", s.coverage().get("coverageBalance"));
            stats.put("evidenceGroups", s.evidenceGroupCount());
            List<Map<String, Object>> caveats = List.of(Map.of("code", "COVERAGE_NOT_TRUTH", "text",
                    "A blindspot describes how a story is covered; it never raises or lowers the truth of a claim."));
            String side = s.blindspot();
            String title = "Coverage blindspot: \"" + s.title() + "\" is covered mostly by " + side + "-rated outlets";
            FindingDraft draft = new FindingDraft("coverage-blindspot", title, "notable", null, s.regionCodes(),
                    s.firstSeenAt(), s.lastSeenAt(), now, List.of(), members, asOf == null ? 0 : asOf, stats,
                    Map.of(), caveats, CoverageAnalyzer.NAME, CoverageAnalyzer.VERSION,
                    paramsOf(CoverageAnalyzer.detector()), "At least " + Math.round(CoverageAnalyzer.BLINDSPOT_SHARE
                            * 100) + "% of the " + s.nRated() + " rated evidence groups covering this story come from "
                            + side + "-rated outlets.", false, "attention", List.of(), List.of(), "story:" + s.id());
            FindingService.Outcome outcome = service.record(draft);
            store.setBlindspotFinding(s.id(), outcome.finding().getId());
            n++;
        }
        return n;
    }

    /** Runs the divergence detector over every enabled topic's last complete day; returns the findings recorded. */
    public int divergences(Instant now) {
        FindingService service = findings.getIfAvailable();
        AttentionTopicRepository repository = topics.getIfAvailable();
        if (service == null || repository == null) {
            return 0;
        }
        Instant day = now.truncatedTo(ChronoUnit.DAYS).minus(Duration.ofDays(1));
        int n = 0;
        for (AttentionTopicEntity t : repository.findByEnabledOrderByIdAsc(true)) {
            Map<String, Map<Instant, Integer>> series = new LinkedHashMap<>();
            jdbc.query("SELECT bucket, day, evidence_groups FROM news_coverage_daily WHERE scope_type = 'topic' AND"
                    + " scope_id = ? AND day >= ? AND day <= ?", rs -> {
                        series.computeIfAbsent(rs.getString(1), k -> new TreeMap<>()).put(
                                rs.getTimestamp(2).toInstant(), rs.getInt(3));
                    }, t.getId(), Timestamp.from(day.minus(Duration.ofDays(BASELINE_DAYS))), Timestamp.from(day));
            Divergence d = detect(series, day);
            if (d == null) {
                continue;
            }
            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("topicId", t.getId());
            stats.put("bucket", d.bucket());
            stats.put("value", d.value());
            stats.put("baselineMean", d.mean());
            stats.put("baselineSd", d.sd());
            stats.put("z", d.z());
            stats.put("otherZ", d.otherZ());
            String ref = "rollup:news/" + t.getId() + "/" + d.bucket();
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("baselineDays", BASELINE_DAYS);
            params.put("minBaselineDays", MIN_BASELINE_DAYS);
            params.put("sd", DIVERGENCE_SD);
            params.put("minGroups", MIN_GROUPS);
            List<Map<String, Object>> caveats = List.of(Map.of("code", "COVERAGE_NOT_TRUTH", "text",
                    "A coverage divergence describes reporting volume by outlet lean; it is not evidence of an event."));
            FindingDraft draft = new FindingDraft("coverage-divergence", "Coverage divergence: " + d.bucket()
                    + "-rated coverage of " + t.getName() + " rose " + String.format(java.util.Locale.ROOT, "%.1f",
                    d.z()) + " SD above its baseline", "notable", null, t.getRegionCodes(), day,
                    day.plus(Duration.ofDays(1)), now, List.of(ref), List.of(), 0, stats, Map.of(), caveats,
                    DIVERGENCE_DETECTOR, DIVERGENCE_VERSION, params, "On " + day.toString().substring(0, 10) + " the "
                    + d.bucket() + " bucket of topic " + t.getId() + " had " + d.value() + " evidence groups against a "
                    + BASELINE_DAYS + "-day baseline of " + String.format(java.util.Locale.ROOT, "%.1f", d.mean())
                    + "; the other buckets stayed within " + DIVERGENCE_SD + " SD.", false, "attention", List.of(),
                    List.of(), "topic:" + t.getId());
            service.record(draft);
            n++;
        }
        return n;
    }

    /**
     * A divergence.
     *
     * @param bucket the rising bucket
     * @param value its evidence groups on the day
     * @param mean its baseline mean
     * @param sd its baseline standard deviation (floored)
     * @param z its rise in standard deviations
     * @param otherZ the largest rise of the other buckets
     */
    record Divergence(String bucket, int value, double mean, double sd, double z, double otherZ) {}

    /**
     * The divergence of a topic's buckets on {@code day} against the preceding baseline, or null. Pure.
     *
     * @param series bucket → day → evidence groups
     */
    static Divergence detect(Map<String, Map<Instant, Integer>> series, Instant day) {
        Map<String, double[]> stats = new LinkedHashMap<>();
        for (String bucket : List.of(MediaVocabulary.LEFT, MediaVocabulary.CENTER, MediaVocabulary.RIGHT)) {
            Map<Instant, Integer> s = series.getOrDefault(bucket, Map.of());
            List<Integer> baseline = new ArrayList<>();
            for (Map.Entry<Instant, Integer> e : s.entrySet()) {
                if (e.getKey().isBefore(day)) {
                    baseline.add(e.getValue());
                }
            }
            if (baseline.size() < MIN_BASELINE_DAYS) {
                return null;
            }
            double mean = baseline.stream().mapToInt(Integer::intValue).average().orElse(0);
            double var = 0;
            for (int v : baseline) {
                var += (v - mean) * (v - mean);
            }
            double sd = Math.max(SD_FLOOR, Math.sqrt(var / Math.max(1, baseline.size() - 1)));
            int value = s.getOrDefault(day, 0);
            stats.put(bucket, new double[] {value, mean, sd, (value - mean) / sd});
        }
        String rising = null;
        for (Map.Entry<String, double[]> e : stats.entrySet()) {
            if (e.getValue()[3] >= DIVERGENCE_SD && e.getValue()[0] >= MIN_GROUPS) {
                if (rising != null) {
                    return null;
                }
                rising = e.getKey();
            }
        }
        if (rising == null) {
            return null;
        }
        double other = Double.NEGATIVE_INFINITY;
        for (Map.Entry<String, double[]> e : stats.entrySet()) {
            if (!e.getKey().equals(rising)) {
                other = Math.max(other, e.getValue()[3]);
            }
        }
        if (other >= DIVERGENCE_SD) {
            return null;
        }
        double[] r = stats.get(rising);
        return new Divergence(rising, (int) r[0], round(r[1]), round(r[2]), round(r[3]), round(other));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> paramsOf(Map<String, Object> detector) {
        return detector.get("params") instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
    }

    private static double round(double v) {
        return Math.round(v * 1000) / 1000.0;
    }
}
