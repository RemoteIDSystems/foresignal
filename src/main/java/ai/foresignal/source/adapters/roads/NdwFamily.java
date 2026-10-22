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
 * Registers {@code ndw-situations} and {@code ndw-srti} (prompt 074), one {@link NdwSituationsAdapter} per feed of
 * {@link NdwSituationsAdapter#FEEDS}; each starts enabled unless it needs a credential or a parameter (the seeding
 * rule of prompt 145).
 */
@Component
public class NdwFamily implements SourceAdapterFamily {

    private final List<SourceAdapter> adapters;

    /**
     * Creates the family.
     *
     * @param baseUrlOverride the value of {@value RoadFetch#BASE_URL_PROPERTY}; blank in production
     * @param meters the registry of the skip counter, resolved lazily
     */
    @Autowired
    public NdwFamily(@Value("${" + RoadFetch.BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<MeterRegistry> meters) {
        this(baseUrlOverride, meters == null ? () -> null : (Supplier<MeterRegistry>) meters::getIfAvailable);
    }

    /**
     * Creates the family with a plain supplier (tests).
     *
     * @param baseUrlOverride the test base URL, or blank
     * @param meters supplies the meter registry, or null
     */
    public NdwFamily(String baseUrlOverride, Supplier<MeterRegistry> meters) {
        List<SourceAdapter> built = new ArrayList<>();
        for (NdwSituationsAdapter.Feed feed : NdwSituationsAdapter.FEEDS) {
            built.add(new NdwSituationsAdapter(baseUrlOverride == null || baseUrlOverride.isBlank() ? feed
                    : feed.rebased(baseUrlOverride), meters));
        }
        this.adapters = List.copyOf(built);
    }

    @Override
    public List<SourceAdapter> adapters() {
        return adapters;
    }
}
