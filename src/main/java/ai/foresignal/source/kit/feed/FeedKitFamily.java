package ai.foresignal.source.kit.feed;

import ai.foresignal.source.kit.KitSpecs;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceAdapterFamily;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Registers one {@link KitFeedAdapter} per feed-kit spec (prompt 073): the reviewed {@code kits/feeds/*.json} files
 * (the curated bulletin pack among them) plus, when enabled, the operator overlay. A kit source declares no credential
 * and no parameter, so every one starts enabled (prompt 145).
 *
 * <p>The WebSub coordinator is resolved lazily, because the WebSub service depends on the source registry this
 * family feeds.
 */
@Component
public class FeedKitFamily implements SourceAdapterFamily {

    private final List<SourceAdapter> adapters;
    private final List<FeedSourceSpec> specs;

    /**
     * Creates the family.
     *
     * @param specs the validated kit specs
     * @param websub the WebSub coordinator, resolved lazily
     * @param meters the registry of the adapters' counters, resolved lazily
     */
    @Autowired
    public FeedKitFamily(KitSpecs specs, ObjectProvider<WebSubCoordinator> websub,
            ObjectProvider<MeterRegistry> meters) {
        this(specs.feeds(), specs.testBaseUrl(),
                websub == null ? () -> WebSubCoordinator.DISABLED
                        : () -> websub.getIfAvailable(() -> WebSubCoordinator.DISABLED),
                meters == null ? () -> null : (Supplier<MeterRegistry>) meters::getIfAvailable);
    }

    /**
     * Creates the family from specs (tests).
     *
     * @param specs the validated specs
     * @param baseUrl the test base URL, or blank
     * @param websub supplies the WebSub coordinator
     * @param meters supplies the meter registry, or null
     */
    public FeedKitFamily(List<FeedSourceSpec> specs, String baseUrl, Supplier<WebSubCoordinator> websub,
            Supplier<MeterRegistry> meters) {
        List<SourceAdapter> built = new ArrayList<>();
        List<FeedSourceSpec> rebased = new ArrayList<>();
        for (FeedSourceSpec spec : specs) {
            FeedSourceSpec s = KitFeedAdapter.rebased(spec, baseUrl);
            rebased.add(s);
            built.add(new KitFeedAdapter(s, websub, meters));
        }
        this.adapters = List.copyOf(built);
        this.specs = List.copyOf(rebased);
    }

    @Override
    public List<SourceAdapter> adapters() {
        return adapters;
    }

    /** The spec of feed source {@code sourceId}, or null when it is not a feed-kit source. */
    public FeedSourceSpec spec(String sourceId) {
        for (FeedSourceSpec spec : specs) {
            if (spec.id().equals(sourceId)) {
                return spec;
            }
        }
        return null;
    }
}
