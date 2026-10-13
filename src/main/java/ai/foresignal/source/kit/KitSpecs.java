package ai.foresignal.source.kit;

import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.source.kit.feature.FeatureSourceSpec;
import ai.foresignal.source.kit.feature.FeatureSpecValidator;
import ai.foresignal.source.kit.feed.FeedSourceSpec;
import ai.foresignal.source.kit.feed.FeedSpecValidator;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Loads and validates every kit spec once at startup (prompt 073): {@code classpath:kits/features/*.json} and
 * {@code classpath:kits/feeds/*.json}, plus the operator overlay when {@value KitOverlay#ENABLED_PROPERTY} is true.
 *
 * <p>Both kits are validated together, so an id is unique across them, and any problem in either stops startup
 * with every problem listed ({@link IllegalStateException}). The source registry adds the cross-family check: a kit
 * id that collides with another family's source fails startup there.
 */
@Component
public class KitSpecs {

    private static final Logger log = LoggerFactory.getLogger(KitSpecs.class);

    /** The classpath folder of feature specs. */
    public static final String FEATURES = "kits/features";

    /** The classpath folder of feed specs. */
    public static final String FEEDS = "kits/feeds";

    private final List<FeatureSourceSpec> features;
    private final List<FeedSourceSpec> feeds;
    private final String testBaseUrl;

    /**
     * Loads the specs.
     *
     * @param overlayEnabled the value of {@value KitOverlay#ENABLED_PROPERTY}
     * @param dataDir Foresignal's data directory
     * @param policy the outbound URL policy, for overlay URLs
     * @param testBaseUrl the value of {@value KitUrls#BASE_URL_PROPERTY}; blank in production
     * @throws IllegalStateException when any spec is invalid
     */
    public KitSpecs(@Value("${" + KitOverlay.ENABLED_PROPERTY + ":false}") boolean overlayEnabled,
            @Value("${foresignal.data-dir:./data}") String dataDir, ObjectProvider<OutboundUrlPolicy> policy,
            @Value("${" + KitUrls.BASE_URL_PROPERTY + ":}") String testBaseUrl) {
        this(new KitOverlay(overlayEnabled, dataDir == null || dataDir.isBlank() ? null : Path.of(dataDir),
                policy == null ? () -> null : policy::getIfAvailable), testBaseUrl);
    }

    /**
     * Loads the specs with explicit overlay settings (tests).
     *
     * @param overlay the overlay settings
     * @param testBaseUrl the test base URL, or blank
     * @throws IllegalStateException when any spec is invalid
     */
    public KitSpecs(KitOverlay overlay, String testBaseUrl) {
        List<String> problems = new ArrayList<>();
        List<KitSpecLoader.Loaded<FeatureSourceSpec>> loadedFeatures =
                new ArrayList<>(KitSpecLoader.classpath(FEATURES, FeatureSourceSpec.class, problems));
        List<KitSpecLoader.Loaded<FeedSourceSpec>> loadedFeeds =
                new ArrayList<>(KitSpecLoader.classpath(FEEDS, FeedSourceSpec.class, problems));
        Path featureOverlay = overlay.directory("features");
        Path feedOverlay = overlay.directory("feeds");
        if (featureOverlay != null) {
            loadedFeatures.addAll(KitSpecLoader.directory(featureOverlay, FeatureSourceSpec.class, problems));
        }
        if (feedOverlay != null) {
            loadedFeeds.addAll(KitSpecLoader.directory(feedOverlay, FeedSourceSpec.class, problems));
        }
        problems.addAll(FeatureSpecValidator.problems(loadedFeatures, Set.of(), overlay));
        Set<String> featureIds = new HashSet<>();
        loadedFeatures.forEach(l -> {
            if (l.spec().id() != null) {
                featureIds.add(l.spec().id());
            }
        });
        problems.addAll(FeedSpecValidator.problems(loadedFeeds, featureIds, overlay));
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Foresignal cannot start: " + problems.size() + " kit spec problem(s).\n  - "
                    + String.join("\n  - ", problems));
        }
        this.features = loadedFeatures.stream().map(KitSpecLoader.Loaded::spec).toList();
        this.feeds = loadedFeeds.stream().map(KitSpecLoader.Loaded::spec).toList();
        this.testBaseUrl = testBaseUrl == null ? "" : testBaseUrl.strip();
        log.info("Loaded {} feature-kit and {} feed-kit spec(s){}", features.size(), feeds.size(),
                overlay.enabled() ? " (operator overlay enabled)" : "");
    }

    /** The validated feature specs, in load order. */
    public List<FeatureSourceSpec> features() {
        return features;
    }

    /** The validated feed specs, in load order. */
    public List<FeedSourceSpec> feeds() {
        return feeds;
    }

    /** The test base URL, or blank in production. */
    public String testBaseUrl() {
        return testBaseUrl;
    }
}
