package ai.foresignal.source.kit.feed;

import ai.foresignal.source.kit.KitOverlay;
import ai.foresignal.source.kit.KitSpecLoader;
import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.spi.IngestPolicyValidator;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Checks feed-kit specs at startup (prompt 073), collecting every problem with its field path.
 *
 * <ul>
 *   <li><strong>Identity:</strong> {@code feed-} plus kebab-case (overlay specs {@code op-}), at most 64 characters,
 *       unique across every kit spec; a kebab-case kind;
 *   <li><strong>URL:</strong> https only, no user info, no credential-looking query parameter; overlay URLs also pass
 *       the outbound URL policy;
 *   <li><strong>Time base:</strong> {@code observedAt} is {@code updated}, {@code published} or {@code RECEIVED},
 *       one rule for the whole source (Core-Data-Model §2);
 *   <li><strong>Schedule:</strong> interval at least 5 min, {@code minInterval ≤ interval}, and the TTL leaves room
 *       for a keyframe at the interval and, for a WebSub source, at the 6 h safety-net poll;
 *   <li><strong>Limits:</strong> {@code maxItems} 1–1,000; a known {@code itemMapper}; a budget.
 * </ul>
 */
public final class FeedSpecValidator {

    /** A built-in feed spec id. */
    public static final Pattern ID_PATTERN = Pattern.compile("^(?:feed|status|news-feed)-[a-z0-9]+(-[a-z0-9]+)*$");

    /** An operator overlay spec id. */
    public static final Pattern OVERLAY_ID_PATTERN = Pattern.compile("^op-[a-z0-9]+(-[a-z0-9]+)*$");

    /** A kind. */
    static final Pattern KIND_PATTERN = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    /** The largest id. */
    public static final int MAX_ID_LENGTH = 64;

    /** The largest item cap. */
    public static final int MAX_ITEMS = 1000;

    /** The poll interval while a WebSub subscription is active. */
    public static final Duration WEBSUB_POLL = Duration.ofHours(6);

    private FeedSpecValidator() {}

    /**
     * Checks every loaded spec.
     *
     * @param specs the specs, built-in and overlay
     * @param takenIds ids already used elsewhere (the feature kit's), for uniqueness
     * @param overlay the overlay settings, for overlay URL checks
     * @return every problem, empty when all specs are valid
     */
    public static List<String> problems(List<KitSpecLoader.Loaded<FeedSourceSpec>> specs, Set<String> takenIds,
            KitOverlay overlay) {
        List<String> problems = new ArrayList<>();
        Set<String> seen = new HashSet<>(takenIds == null ? Set.of() : takenIds);
        for (KitSpecLoader.Loaded<FeedSourceSpec> loaded : specs) {
            FeedSourceSpec spec = loaded.spec();
            String where = loaded.where() + " " + spec.id() + ": ";
            problems(spec, loaded.overlay()).forEach(p -> problems.add(where + p));
            if (spec.id() != null && !seen.add(spec.id())) {
                problems.add(where + "id: '" + spec.id() + "' is declared twice");
            }
            if (loaded.overlay() && overlay != null) {
                List<String> urlProblems = new ArrayList<>();
                overlay.checkUrl("url", spec.url(), urlProblems);
                urlProblems.forEach(p -> problems.add(where + p));
            }
        }
        return problems;
    }

    /**
     * The problems of one spec, each starting with its field path.
     *
     * @param spec the spec
     * @param overlay true for an operator overlay spec
     */
    public static List<String> problems(FeedSourceSpec spec, boolean overlay) {
        List<String> p = new ArrayList<>();
        Pattern pattern = overlay ? OVERLAY_ID_PATTERN : ID_PATTERN;
        if (spec.id() == null || spec.id().isBlank()) {
            p.add("id: is required");
        } else {
            if (!pattern.matcher(spec.id()).matches()) {
                p.add("id: '" + spec.id() + "' must match " + pattern.pattern());
            }
            if (spec.id().length() > MAX_ID_LENGTH) {
                p.add("id: is longer than " + MAX_ID_LENGTH + " characters");
            }
        }
        if (spec.name() == null || spec.name().isBlank()) {
            p.add("name: is required");
        }
        if (!KIND_PATTERN.matcher(spec.effectiveKind()).matches()) {
            p.add("kind: must be kebab-case, but was '" + spec.kind() + "'");
        }
        p.addAll(KitUrls.problems("url", spec.url(), true));
        p.addAll(KitUrls.problems("homepage", spec.homepage(), true));
        p.addAll(KitUrls.problems("termsUrl", spec.termsUrl(), false));
        if (spec.license() == null || spec.license().isBlank()) {
            p.add("license: is required");
        }
        if (spec.attribution() == null || spec.attribution().isBlank()) {
            p.add("attribution: is required");
        }
        String observedAt = spec.effectiveObservedAt();
        if (!observedAt.equals(FeedSourceSpec.UPDATED) && !observedAt.equals(FeedSourceSpec.PUBLISHED)
                && !observedAt.equals(FeedSourceSpec.RECEIVED)) {
            p.add("observedAt: must be updated, published or RECEIVED (one rule per source, never a fallback), but"
                    + " was '" + observedAt + "'");
        }
        String anchor = spec.ttlAnchor();
        if (anchor != null && !anchor.equals("RECEIVED") && !anchor.equals("OBSERVED")) {
            p.add("ttlAnchor: must be RECEIVED or OBSERVED, but was '" + anchor + "'");
        }
        if ("OBSERVED".equals(anchor) && observedAt.equals(FeedSourceSpec.RECEIVED)) {
            p.add("ttlAnchor: OBSERVED needs observedAt updated or published");
        }
        if (spec.effectiveMaxItems() < 1 || spec.effectiveMaxItems() > MAX_ITEMS) {
            p.add("maxItems: must be between 1 and " + MAX_ITEMS);
        }
        if (spec.itemMapper() != null && FeedItemMappers.of(spec.itemMapper()) == null) {
            p.add("itemMapper: '" + spec.itemMapper() + "' is not a reviewed mapper");
        }
        boolean newsId = spec.id() != null && ai.foresignal.source.formats.news.NewsArticles.isNewsSource(spec.id());
        boolean newsMapper = NewsArticleItemMapper.NAME.equals(spec.itemMapper());
        if (newsId != newsMapper) {
            p.add("itemMapper: the news-feed-* (and op-news-feed-*) outlet feeds and only they use the '"
                    + NewsArticleItemMapper.NAME + "' mapper (prompt 133)");
        }
        if (newsMapper && spec.kind() != null && !ai.foresignal.source.formats.news.NewsArticles.KIND
                .equals(spec.kind())) {
            p.add("kind: an outlet feed writes news-article records, but was '" + spec.kind() + "'");
        }
        Duration interval = duration("interval", spec.interval(), FeedSourceSpec.DEFAULT_INTERVAL, p);
        Duration minInterval = duration("minInterval", spec.minInterval(), FeedSourceSpec.MIN_INTERVAL, p);
        Duration ttl = duration("recordTtl", spec.recordTtl(), FeedSourceSpec.DEFAULT_TTL, p);
        duration("retention", spec.retention(), FeedSourceSpec.DEFAULT_RETENTION, p);
        if (minInterval != null && minInterval.compareTo(FeedSourceSpec.MIN_INTERVAL) < 0) {
            p.add("minInterval: must be at least 5 min");
        }
        if (interval != null && minInterval != null && interval.compareTo(minInterval) < 0) {
            p.add("interval: " + interval + " is below minInterval " + minInterval);
        }
        if (ttl != null && interval != null) {
            keyframe("recordTtl", ttl, interval, p);
            if (spec.wantsWebSub()) {
                keyframe("recordTtl", ttl, WEBSUB_POLL, p);
            }
        }
        FeedSourceSpec.Budget budget = spec.budget();
        if (budget == null || budget.perMinute() == null || budget.perDay() == null) {
            p.add("budget: perMinute and perDay are required");
        } else if (budget.perMinute() < 1 || budget.perDay() < 1
                || (budget.mbPerDay() != null && budget.mbPerDay() < 1)) {
            p.add("budget: values must be positive");
        }
        if (spec.maxResponseBytes() != null
                && (spec.maxResponseBytes() < 1024 || spec.maxResponseBytes() > FeedParser.MAX_BYTES)) {
            p.add("maxResponseBytes: must be between 1 KB and " + FeedParser.MAX_BYTES + " bytes");
        }
        return p;
    }

    private static void keyframe(String path, Duration ttl, Duration poll, List<String> p) {
        Duration ceiling = ttl.minus(poll).minus(IngestPolicyValidator.KEYFRAME_TTL_MARGIN);
        if (ceiling.isZero() || ceiling.isNegative()) {
            p.add(path + ": " + ttl + " leaves no keyframe for a poll interval of " + poll
                    + "; recordTtl − interval − 30 s must be positive (Core-Data-Model §5.1)");
        }
    }

    private static Duration duration(String path, String text, Duration fallback, List<String> p) {
        try {
            Duration d = FeedSourceSpec.parse(text, fallback);
            if (d != null && (d.isZero() || d.isNegative())) {
                p.add(path + ": must be positive");
                return null;
            }
            return d;
        } catch (RuntimeException e) {
            p.add(path + ": '" + text + "' is not a duration (" + e.getMessage() + ")");
            return null;
        }
    }
}
