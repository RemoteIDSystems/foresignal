package ai.foresignal.social.narrative;

import ai.foresignal.analytics.finding.FindingDraft;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.analytics.math.Seeds;
import ai.foresignal.social.SocialEvents;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * The daily coordination pass (prompt 136): {@link CoordinationDetector} 1.0.0 over every narrative active in the
 * last 24 hours, Benjamini–Hochberg across them, the {@code coordination} block written to each narrative, and a
 * {@code social-coordination} finding (119) with {@code coordination.detected} where at least two indicators are
 * significant. The finding says "coordinated-amplification indicators present" and never names or labels an account.
 */
@Component
public class CoordinationRunner {

    private static final Logger log = LoggerFactory.getLogger(CoordinationRunner.class);

    /** The window examined. */
    static final Duration WINDOW = Duration.ofHours(24);

    /** The baseline window of new-account shares. */
    static final Duration BASELINE_WINDOW = Duration.ofDays(30);

    private final NarrativeStore store;
    private final ObjectProvider<FindingService> findings;
    private final SocialEvents events;
    private final Clock clock;

    /** Creates the runner. */
    public CoordinationRunner(NarrativeStore store, ObjectProvider<FindingService> findings, SocialEvents events,
            Clock clock) {
        this.store = store;
        this.findings = findings;
        this.events = events;
        this.clock = clock;
    }

    /** Runs the pass for the day ending now; the findings recorded. */
    public int run() {
        Instant end = clock.instant();
        Instant start = end.minus(WINDOW);
        List<NarrativeStore.Narrative> narratives = store.activeSince(start, 5_000);
        List<CoordinationDetector.Indicators> day = new ArrayList<>();
        for (NarrativeStore.Narrative n : narratives) {
            List<NarrativeStore.Member> members = store.membersBetween(n.id(), start, end);
            if (members.isEmpty()) {
                continue;
            }
            List<CoordinationDetector.Post> posts = new ArrayList<>();
            Map<String, Integer> byPlatform = new HashMap<>();
            for (NarrativeStore.Member m : members) {
                posts.add(new CoordinationDetector.Post(m.platform() + ":" + m.authorKey(), m.createdAt(),
                        m.signature(), m.accountCreatedAt(), m.repostOf()));
                byPlatform.merge(m.platform(), 1, Integer::sum);
            }
            String platform = byPlatform.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                    .orElse(null);
            Double baseline = newAccountBaseline(platform, n.topicIds(), end);
            day.add(CoordinationDetector.compute(n.id(), posts, start, end, baseline,
                    Seeds.of(n.id(), start.truncatedTo(ChronoUnit.DAYS).toString())));
        }
        int recorded = 0;
        for (CoordinationDetector.Indicators ind : CoordinationDetector.adjust(day)) {
            String findingId = null;
            if (ind.detected()) {
                findingId = record(ind, end);
                if (findingId != null) {
                    recorded++;
                }
            }
            store.updateCoordination(ind.narrativeId(), ind.toMap(findingId, end), findingId, end);
        }
        return recorded;
    }

    /**
     * The platform's share of new accounts among authors of posts on the narrative's topics over the last 30 days, or
     * null when the platform gives no creation dates.
     */
    Double newAccountBaseline(String platform, List<String> topics, Instant now) {
        if (platform == null) {
            return null;
        }
        String topicFilter = topics.isEmpty() ? "" : " AND topic_ids && ?";
        List<Object> args = new ArrayList<>(List.of(platform, Timestamp.from(now.minus(BASELINE_WINDOW))));
        if (!topics.isEmpty()) {
            args.add(NarrativeStore.array(topics));
        }
        return store.jdbc().query("SELECT count(DISTINCT author_key), count(DISTINCT author_key) FILTER (WHERE"
                + " created_at - account_created_at < interval '" + CoordinationDetector.NEW_ACCOUNT_DAYS + " days')"
                + " FROM social_narrative_member WHERE platform = ? AND created_at >= ? AND account_created_at IS NOT"
                + " NULL" + topicFilter, rs -> {
                    if (!rs.next() || rs.getLong(1) < CoordinationDetector.MIN_AUTHORS) {
                        return null;
                    }
                    double share = (double) rs.getLong(2) / rs.getLong(1);
                    return Math.max(0.001, Math.min(0.999, share));
                }, args.toArray());
    }

    private String record(CoordinationDetector.Indicators ind, Instant now) {
        FindingService service = findings.getIfAvailable();
        NarrativeStore.Narrative n = store.narrative(ind.narrativeId()).orElse(null);
        if (service == null || n == null) {
            return null;
        }
        try {
            FindingService.Outcome outcome = service.record(draft(ind, n, now));
            String id = outcome.finding().getId();
            if (outcome.change() != FindingService.Change.UNCHANGED) {
                events.emit(NarrativeVocabulary.EVENT_COORDINATION, body(id, ind, now), false);
            }
            return id;
        } catch (RuntimeException e) {
            log.warn("Could not record a social-coordination finding for {}: {}", ind.narrativeId(), e.toString());
            return null;
        }
    }

    /** The finding of a detection. */
    static FindingDraft draft(CoordinationDetector.Indicators ind, NarrativeStore.Narrative n, Instant now) {
        Map<String, Object> stats = new LinkedHashMap<>();
        for (Map.Entry<String, CoordinationDetector.Indicator> e : ind.byName().entrySet()) {
            stats.put(e.getKey(), e.getValue().toMap());
        }
        stats.put("significant", ind.significant());
        stats.put("narrativeId", n.id());
        List<Map<String, Object>> caveats = List.of(Map.of("code", "INDICATORS_NOT_LABELS", "text",
                "These are statistics on the narrative against null models; they do not identify any account as"
                        + " coordinated, automated or state-directed, and organic events can produce similar patterns."));
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("alpha", CoordinationDetector.ALPHA);
        params.put("minSignificant", CoordinationDetector.MIN_SIGNIFICANT);
        params.put("windowHours", WINDOW.toHours());
        String title = NarrativeStore.clip("Coordinated-amplification indicators present: \"" + NarrativeStore.clip(
                n.title(), 80) + "\"", 200);
        String explanation = "In the 24 hours to " + now + ", " + String.join(", ", ind.significant())
                + " exceeded their null models after Benjamini-Hochberg adjustment across the day's narratives ("
                + NarrativeVocabulary.COORDINATION_STATEMENT + ").";
        return new FindingDraft(NarrativeVocabulary.COORDINATION_FINDING, title, "notable", null, n.regionCodes(),
                ind.windowStart(), ind.windowEnd(), now, List.of(), List.of("narrative:" + n.id()), 0, stats,
                Map.of(), caveats, NarrativeVocabulary.COORDINATION, NarrativeVocabulary.COORDINATION_VERSION, params,
                explanation, false, "attention", List.of(), List.of(), n.id() + "/"
                        + ind.windowEnd().truncatedTo(ChronoUnit.DAYS));
    }

    /** The {@code CoordinationDetectedEvent} body. */
    public static Map<String, Object> body(String findingId, CoordinationDetector.Indicators ind, Instant at) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("event", NarrativeVocabulary.EVENT_COORDINATION);
        body.put("findingId", findingId);
        body.put("narrativeId", ind.narrativeId());
        body.put("significantIndicators", ind.significant());
        body.put("at", at.toString());
        return body;
    }
}
