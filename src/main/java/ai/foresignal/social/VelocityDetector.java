package ai.foresignal.social;

import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.analytics.finding.FindingDraft;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.attention.AttentionTopicEntity;
import ai.foresignal.attention.AttentionTopicRepository;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * The {@code social-velocity} finding (prompt 135): when an hour closes, every scope (topic, place, entity) × platform
 * with volume in that hour is baselined with {@link SocialBaseline} 1.0.0 over its complete and paged collection
 * (languages summed; sampled paths never count, SOC-3). A finding is recorded when {@code velocityRatio ≥ minRatio}
 * (5) and at least {@code minUniqueAuthors} (20) distinct authors posted in the hour ({@code social.velocity});
 * severity comes from the robust z (≥ 8 significant, ≥ 4 notable, else info); one finding per scope and platform per
 * {@code dedupHours} (6). Each finding emits {@code social.velocity} on the {@code social} topic.
 */
@Component
public class VelocityDetector {

    private static final Logger log = LoggerFactory.getLogger(VelocityDetector.class);

    /** The robust z of a significant finding. */
    public static final double SIGNIFICANT_Z = 8;

    /** The robust z of a notable finding. */
    public static final double NOTABLE_Z = 4;

    /**
     * The thresholds in force.
     *
     * @param minRatio the velocity ratio
     * @param minUniqueAuthors the distinct authors in the hour
     * @param dedupHours the deduplication window per scope and platform
     */
    public record Thresholds(double minRatio, int minUniqueAuthors, int dedupHours) {

        /** The defaults. */
        public static final Thresholds DEFAULT = new Thresholds(SocialVocabulary.DEFAULT_VELOCITY_RATIO,
                SocialVocabulary.DEFAULT_VELOCITY_AUTHORS, SocialVocabulary.DEFAULT_VELOCITY_DEDUP_HOURS);
    }

    /**
     * A candidate before deduplication.
     *
     * @param scope the scope key
     * @param platform the network
     * @param point the baselined hour
     * @param uniqueAuthors distinct authors in the hour
     * @param severity the severity
     */
    public record Candidate(String scope, String platform, SocialBaseline.Point point, long uniqueAuthors,
            String severity) {}

    private final SocialVolumeStore store;
    private final ObjectProvider<FindingService> findings;
    private final ObjectProvider<AttentionTopicRepository> topics;
    private final SocialEvents events;
    private final AppSettingService settings;
    private final Clock clock;

    /** Creates the detector. */
    public VelocityDetector(SocialVolumeStore store, ObjectProvider<FindingService> findings,
            ObjectProvider<AttentionTopicRepository> topics, SocialEvents events, AppSettingService settings,
            Clock clock) {
        this.store = store;
        this.findings = findings;
        this.topics = topics;
        this.events = events;
        this.settings = settings;
        this.clock = clock;
    }

    /** The thresholds in force ({@code social.velocity}). */
    public Thresholds thresholds() {
        try {
            Map<?, ?> m = settings.get(SettingKeys.SOCIAL_VELOCITY, Map.class).orElse(null);
            if (m == null) {
                return Thresholds.DEFAULT;
            }
            return new Thresholds(number(m.get("minRatio"), Thresholds.DEFAULT.minRatio()),
                    (int) number(m.get("minUniqueAuthors"), Thresholds.DEFAULT.minUniqueAuthors()),
                    (int) number(m.get("dedupHours"), Thresholds.DEFAULT.dedupHours()));
        } catch (RuntimeException e) {
            return Thresholds.DEFAULT;
        }
    }

    private static double number(Object v, double fallback) {
        if (v instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return v == null ? fallback : Double.parseDouble(v.toString());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /** The severity of a robust z. */
    public static String severity(Double z) {
        if (z != null && z >= SIGNIFICANT_Z) {
            return "significant";
        }
        if (z != null && z >= NOTABLE_Z) {
            return "notable";
        }
        return "info";
    }

    /**
     * The candidates of one closed hour (pure apart from the store reads).
     *
     * @param hour the hour start
     * @param t the thresholds
     */
    public List<Candidate> candidates(Instant hour, Thresholds t) {
        List<Candidate> out = new ArrayList<>();
        Map<String, List<SocialVolumeStore.VolumeRow>> byScope = store.hour(hour);
        Instant from = hour.minus(Duration.ofDays(7L * SocialBaseline.WEEKS));
        Instant to = hour.plus(Duration.ofHours(1));
        Map<String, Map<Instant, Long>> denominatorCache = new LinkedHashMap<>();
        for (Map.Entry<String, List<SocialVolumeStore.VolumeRow>> e : byScope.entrySet()) {
            Set<String> platforms = new HashSet<>();
            for (SocialVolumeStore.VolumeRow r : e.getValue()) {
                if (SocialBaseline.eligible(r.collectionType())) {
                    platforms.add(r.platform());
                }
            }
            for (String platform : platforms) {
                long authors = 0;
                for (SocialVolumeStore.VolumeRow r : e.getValue()) {
                    if (r.platform().equals(platform) && SocialBaseline.eligible(r.collectionType())) {
                        authors += r.uniqueAuthors();
                    }
                }
                if (authors < t.minUniqueAuthors()) {
                    continue;
                }
                int colon = e.getKey().indexOf(':');
                List<SocialVolumeStore.VolumeRow> history = store.volume(e.getKey().substring(0, colon),
                        e.getKey().substring(colon + 1), platform, null, from, to);
                Map<Instant, Long> values = new TreeMap<>();
                Instant first = null;
                for (SocialVolumeStore.VolumeRow r : history) {
                    if (!SocialBaseline.eligible(r.collectionType())) {
                        continue;
                    }
                    values.merge(r.hour(), r.posts(), Long::sum);
                    first = first == null || r.hour().isBefore(first) ? r.hour() : first;
                }
                Map<Instant, Long> denominators = denominatorCache.computeIfAbsent(platform,
                        p -> store.denominators(p, null, from, to));
                Set<Instant> collected = null;
                if (!SocialBaseline.hasDenominators(denominators) && first != null) {
                    collected = new HashSet<>();
                    for (Instant h = first; !h.isAfter(hour); h = h.plus(Duration.ofHours(1))) {
                        collected.add(h);
                    }
                }
                SocialBaseline.Point p = SocialBaseline.at(hour, values, denominators, collected);
                if (p.velocityRatio() == null || p.velocityRatio() < t.minRatio()) {
                    continue;
                }
                out.add(new Candidate(e.getKey(), platform, p, authors, severity(p.zRobust())));
            }
        }
        return out;
    }

    /**
     * Runs the detector over one closed hour.
     *
     * @param hour the hour start
     * @return the findings recorded
     */
    public int run(Instant hour) {
        FindingService service = findings.getIfAvailable();
        if (service == null) {
            return 0;
        }
        Thresholds t = thresholds();
        Instant now = clock.instant();
        int n = 0;
        for (Candidate c : candidates(hour, t)) {
            String dedupScope = c.scope() + "/" + c.platform();
            if (recent(service, dedupScope, now, t.dedupHours())) {
                continue;
            }
            try {
                FindingService.Outcome outcome = service.record(draft(c, hour, now, t, dedupScope));
                if (outcome.change() != FindingService.Change.UNCHANGED) {
                    events.velocity(eventPayload(outcome.finding().getId(), c, now), false);
                    n++;
                }
            } catch (RuntimeException e) {
                log.warn("Could not record a social-velocity finding for {}: {}", dedupScope, e.toString());
            }
        }
        return n;
    }

    private static boolean recent(FindingService service, String dedupScope, Instant now, int hours) {
        String key = SocialVocabulary.VELOCITY_FINDING + "|" + dedupScope;
        for (AnalyticFindingEntity f : service.detectedBetween(SocialVocabulary.VELOCITY_FINDING,
                now.minus(Duration.ofHours(Math.max(1, hours))), now)) {
            if (key.equals(f.getDedupKey())) {
                return true;
            }
        }
        return false;
    }

    /** The finding of a candidate. */
    FindingDraft draft(Candidate c, Instant hour, Instant now, Thresholds t, String dedupScope) {
        int colon = c.scope().indexOf(':');
        String scopeType = c.scope().substring(0, colon);
        String scopeId = c.scope().substring(colon + 1);
        List<String> regions = List.of();
        String label = c.scope();
        if (SocialVocabulary.SCOPE_TOPIC.equals(scopeType)) {
            AttentionTopicRepository repository = topics.getIfAvailable();
            AttentionTopicEntity topic = repository == null ? null : repository.findById(scopeId).orElse(null);
            if (topic != null) {
                regions = topic.getRegionCodes();
                label = topic.getName();
            }
        }
        SocialBaseline.Point p = c.point();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("scope", Map.of("type", scopeType, "id", scopeId));
        stats.put("platform", c.platform());
        stats.put("hour", hour.toString());
        stats.put("observed", p.observed());
        stats.put("baseline", p.baseline());
        stats.put("mad", p.mad());
        stats.put("velocityRatio", p.velocityRatio());
        stats.put("zRobust", p.zRobust());
        stats.put("uniqueAuthors", c.uniqueAuthors());
        stats.put("samples", p.samples());
        stats.put("denominatorScaled", p.scaled());
        List<Map<String, Object>> caveats = new ArrayList<>();
        caveats.add(Map.of("code", "SOCIAL_IS_REPORTING", "text",
                "Social volume measures what people are saying, not what happened; it never corroborates an event."));
        if (!p.scaled()) {
            caveats.add(Map.of("code", "NO_DENOMINATOR", "text",
                    "This platform has no complete-stream denominator, so platform growth or outages are not removed."));
        }
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("minRatio", t.minRatio());
        params.put("minUniqueAuthors", t.minUniqueAuthors());
        params.put("dedupHours", t.dedupHours());
        params.put("weeks", SocialBaseline.WEEKS);
        params.put("minWeeks", SocialBaseline.MIN_WEEKS);
        String ref = "social:" + c.scope() + "/" + c.platform() + "/" + SocialVocabulary.POSTS;
        String title = String.format(Locale.ROOT, "Social velocity: %s on %s at %.1f× its baseline", label,
                c.platform(), p.velocityRatio());
        String explanation = String.format(Locale.ROOT, "In the hour from %s, %d posts by %d distinct authors"
                + " mentioned %s on %s, against a seasonal baseline of %.1f (%d same-hour samples over %d weeks%s).",
                hour, Math.round(p.observed()), c.uniqueAuthors(), label, c.platform(), p.baseline(), p.samples(),
                SocialBaseline.WEEKS, p.scaled() ? ", scaled by the platform denominator" : "");
        return new FindingDraft(SocialVocabulary.VELOCITY_FINDING, title, c.severity(), null, regions, hour,
                hour.plus(Duration.ofHours(1)), now, List.of(ref), List.of(), 0, stats, Map.of(), caveats,
                SocialBaseline.NAME, SocialBaseline.VERSION, params, explanation, false, "attention", List.of(),
                List.of(), dedupScope);
    }

    /** The {@code SocialVelocityEvent} payload (without {@code event}). */
    public static Map<String, Object> eventPayload(String findingId, Candidate c, Instant now) {
        int colon = c.scope().indexOf(':');
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("findingId", findingId);
        out.put("scope", Map.of("type", c.scope().substring(0, colon), "id", c.scope().substring(colon + 1)));
        out.put("platform", c.platform());
        out.put("language", null);
        out.put("velocityRatio", c.point().velocityRatio());
        out.put("uniqueAuthors", c.uniqueAuthors());
        out.put("zRobust", c.point().zRobust());
        out.put("severity", c.severity());
        out.put("detectedAt", now.toString());
        return out;
    }
}
