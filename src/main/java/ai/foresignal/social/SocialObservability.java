package ai.foresignal.social;

import ai.foresignal.observability.AbsenceCheck;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceService;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Social observability (prompt 135, Intelligence-Design §12.7): the {@code social-attention} family of 116's
 * {@code ObservabilityService}, method {@code platform-volume}.
 *
 * <p>For a region and window, the expected post volume per complete-stream platform is the region's mean posts per
 * complete hour over the trailing 8 weeks ({@code social_coverage}); collection is complete when no unreplayed stream
 * gap overlaps the window and every closed hour in it was complete. {@link #canSeeAbsence} is {@code ok} only when at
 * least one complete-stream platform was healthy over the whole window <em>and</em> the region's baseline on such a
 * platform is at least {@code social.minRegionBaseline} posts per hour (20); otherwise the reason is
 * {@value SocialVocabulary#SOCIAL_STREAM_GAP} (no healthy stream) or {@value SocialVocabulary#LOW_SOCIAL_COVERAGE}.
 * "Nobody is talking about it" is therefore only said where Foresignal could have heard the conversation.
 */
@Component
public class SocialObservability {

    /** The baseline window. */
    public static final Duration BASELINE_WINDOW = Duration.ofDays(56);

    /**
     * One platform's expectation for a region and window.
     *
     * @param platformId the adapter
     * @param platform the network
     * @param enabled whether the adapter is collecting
     * @param complete whether collection was complete over the window
     * @param baselinePostsPerHour the trailing 8-week mean posts per complete hour, or null
     * @param expectedPosts the baseline times the window's hours, or null
     */
    public record Expectation(String platformId, String platform, boolean enabled, boolean complete,
            Double baselinePostsPerHour, Double expectedPosts) {}

    private final SocialVolumeStore store;
    private final StreamLedger ledger;
    private final ObjectProvider<SourceService> sources;
    private final AppSettingService settings;

    /** Creates the component. */
    public SocialObservability(SocialVolumeStore store, StreamLedger ledger, ObjectProvider<SourceService> sources,
            AppSettingService settings) {
        this.store = store;
        this.ledger = ledger;
        this.sources = sources;
        this.settings = settings;
    }

    /** {@code social.minRegionBaseline} in force. */
    public double minRegionBaseline() {
        try {
            Number n = settings.get(SettingKeys.SOCIAL_MIN_REGION_BASELINE, Number.class).orElse(null);
            return n == null ? SocialVocabulary.DEFAULT_MIN_REGION_BASELINE : n.doubleValue();
        } catch (RuntimeException e) {
            return SocialVocabulary.DEFAULT_MIN_REGION_BASELINE;
        }
    }

    /** The adapters with a complete stream path. */
    static List<SocialPlatformCatalog.Definition> streamPlatforms() {
        List<SocialPlatformCatalog.Definition> out = new ArrayList<>();
        for (SocialPlatformCatalog.Definition d : SocialPlatformCatalog.all()) {
            if (d.path("stream").filter(p -> SocialVocabulary.COMPLETE.equals(p.collectionType())).isPresent()) {
                out.add(d);
            }
        }
        return out;
    }

    private boolean enabled(String sourceId) {
        SourceService service = sources.getIfAvailable();
        if (service == null) {
            return false;
        }
        try {
            return service.view(sourceId).enabled();
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * The expected volume per complete-stream platform.
     *
     * @param regionCode the 040 region, or null for everywhere (the platform denominators)
     * @param from the window start
     * @param to the window end
     */
    public List<Expectation> expected(String regionCode, Instant from, Instant to) {
        List<Expectation> out = new ArrayList<>();
        double hours = Math.max(1, Duration.between(from, to).toMinutes() / 60.0);
        for (SocialPlatformCatalog.Definition d : streamPlatforms()) {
            boolean on = enabled(d.id());
            boolean complete = on && complete(d.id(), d.network(), from, to);
            Double baseline = baseline(d.network(), regionCode, from);
            out.add(new Expectation(d.id(), d.network(), on, complete, baseline,
                    baseline == null ? null : baseline * hours));
        }
        return out;
    }

    /** Whether a stream's collection was complete over a window. */
    boolean complete(String sourceId, String platform, Instant from, Instant to) {
        if (!ledger.complete(sourceId, from, to)) {
            return false;
        }
        for (Boolean hour : store.completeHours(platform, from, to).values()) {
            if (!hour) {
                return false;
            }
        }
        return true;
    }

    /** The trailing 8-week mean posts per complete hour of a region (or of the whole platform when null). */
    Double baseline(String platform, String regionCode, Instant before) {
        Instant start = before.minus(BASELINE_WINDOW);
        if (regionCode == null) {
            Map<Instant, Boolean> hours = store.completeHours(platform, start, before);
            long complete = hours.values().stream().filter(Boolean::booleanValue).count();
            if (complete == 0) {
                return null;
            }
            long total = 0;
            Map<Instant, Long> d = store.denominators(platform, null, start, before);
            for (Map.Entry<Instant, Boolean> e : hours.entrySet()) {
                if (e.getValue()) {
                    total += d.getOrDefault(e.getKey(), 0L);
                }
            }
            return total / (double) complete;
        }
        return store.regionMeanPerHour(platform, regionCode, start, before);
    }

    /**
     * {@code canSeeAbsence(social-attention, region, window)}.
     *
     * @param regionCode the 040 region, or null for everywhere
     * @param from the window start
     * @param to the window end
     * @return ok, or why not ({@value SocialVocabulary#SOCIAL_STREAM_GAP}, {@value
     *     SocialVocabulary#LOW_SOCIAL_COVERAGE})
     */
    public AbsenceCheck canSeeAbsence(String regionCode, Instant from, Instant to) {
        double min = minRegionBaseline();
        boolean anyHealthy = false;
        Double best = null;
        for (Expectation e : expected(regionCode, from, to)) {
            if (!e.complete()) {
                continue;
            }
            anyHealthy = true;
            if (e.baselinePostsPerHour() != null && (best == null || e.baselinePostsPerHour() > best)) {
                best = e.baselinePostsPerHour();
            }
        }
        if (!anyHealthy) {
            return new AbsenceCheck(false, null, SocialVocabulary.SOCIAL_STREAM_GAP);
        }
        double p = best == null || min <= 0 ? (best == null ? 0 : 1) : Math.min(1, best / min);
        if (best == null || best < min) {
            return new AbsenceCheck(false, p, SocialVocabulary.LOW_SOCIAL_COVERAGE);
        }
        return new AbsenceCheck(true, 1.0, null);
    }

    /** The view of an expectation list, for APIs. */
    public static List<Map<String, Object>> view(List<Expectation> expectations) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Expectation e : expectations) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("platformId", e.platformId());
            m.put("platform", e.platform());
            m.put("enabled", e.enabled());
            m.put("complete", e.complete());
            m.put("baselinePostsPerHour", e.baselinePostsPerHour());
            m.put("expectedPosts", e.expectedPosts());
            out.add(m);
        }
        return out;
    }
}
