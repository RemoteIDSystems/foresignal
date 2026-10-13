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
 * Registers the {@code open511-*} sources (prompt 074): one {@link Open511Adapter} per server of the reviewed list
 * ({@link Open511Server#REVIEWED}), every one disabled by default.
 *
 * <p>The list is validated at startup; an invalid list stops Foresignal. Servers are never caller-supplied.
 */
@Component
public class Open511Family implements SourceAdapterFamily {

    private final List<SourceAdapter> adapters;

    /**
     * Creates the family.
     *
     * @param baseUrlOverride the value of {@value RoadFetch#BASE_URL_PROPERTY}; blank in production
     * @param meters the registry of the skip counter, resolved lazily
     * @throws IllegalStateException when the reviewed list is invalid
     */
    @Autowired
    public Open511Family(@Value("${" + RoadFetch.BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<MeterRegistry> meters) {
        this(baseUrlOverride, meters == null ? () -> null : (Supplier<MeterRegistry>) meters::getIfAvailable);
    }

    /**
     * Creates the family with a plain supplier (tests).
     *
     * @param baseUrlOverride the test base URL, or blank
     * @param meters supplies the meter registry, or null
     */
    public Open511Family(String baseUrlOverride, Supplier<MeterRegistry> meters) {
        List<String> problems = Open511Server.problems(Open511Server.REVIEWED);
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Foresignal cannot start: the reviewed Open511 server list is invalid: "
                    + problems);
        }
        List<SourceAdapter> built = new ArrayList<>();
        for (Open511Server server : Open511Server.REVIEWED) {
            built.add(new Open511Adapter(baseUrlOverride == null || baseUrlOverride.isBlank() ? server
                    : server.rebased(baseUrlOverride), meters));
        }
        this.adapters = List.copyOf(built);
    }

    @Override
    public List<SourceAdapter> adapters() {
        return adapters;
    }
}
