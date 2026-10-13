package ai.foresignal.source.adapters.roads;

import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceAdapterFamily;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Registers the {@code wzdx-*} sources (prompt 074): one {@link WzdxAdapter} per feed of the vendored registry
 * snapshot ({@link WzdxRegistry}), every one disabled by default.
 *
 * <p>The snapshot is validated at startup ({@link WzdxRegistry#problems}); an invalid snapshot stops Foresignal.
 * The registry is never read at runtime and feeds are never caller-supplied.
 */
@Component
public class WzdxFamily implements SourceAdapterFamily {

    private final List<SourceAdapter> adapters;

    /**
     * Creates the family.
     *
     * @param baseUrlOverride the value of {@value RoadFetch#BASE_URL_PROPERTY}; blank in production
     * @param meters the registry of the skip counter, resolved lazily
     * @throws IllegalStateException when the snapshot is invalid
     */
    @Autowired
    public WzdxFamily(@Value("${" + RoadFetch.BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<MeterRegistry> meters) {
        this(baseUrlOverride, meters == null ? () -> null : (Supplier<MeterRegistry>) meters::getIfAvailable);
    }

    /**
     * Creates the family with a plain supplier (tests).
     *
     * @param baseUrlOverride the test base URL, or blank
     * @param meters supplies the meter registry, or null
     */
    public WzdxFamily(String baseUrlOverride, Supplier<MeterRegistry> meters) {
        List<WzdxFeed> feeds = WzdxRegistry.load();
        List<String> problems = WzdxRegistry.problems(feeds);
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Foresignal cannot start: the vendored WZDx registry snapshot is invalid: "
                    + problems);
        }
        List<SourceAdapter> built = new ArrayList<>();
        for (WzdxFeed feed : feeds) {
            built.add(new WzdxAdapter(baseUrlOverride == null || baseUrlOverride.isBlank() ? feed
                    : feed.rebased(baseUrlOverride), meters));
        }
        this.adapters = List.copyOf(built);
    }

    @Override
    public List<SourceAdapter> adapters() {
        return adapters;
    }
}
