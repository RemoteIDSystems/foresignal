package ai.foresignal.source.adapters.gbfs;

import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceAdapterFamily;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Registers the {@code gbfs-*} sources (prompt 071): one {@code gbfs-{id}} per system of the reviewed list
 * ({@link GbfsSystems#REVIEWED}).
 *
 * <p>The list is validated at startup ({@link GbfsSystems#problems}); an invalid list stops Foresignal rather than
 * registering a URL nobody reviewed. There is no runtime discovery and no catalog of extra systems: every requested
 * URL is in the code.
 */
@Component
public class GbfsFamily implements SourceAdapterFamily {

    /**
     * Test-only override moving every GBFS URL under one base (scheme and authority), with the original host as the
     * first path segment ({@link GbfsSystem#rebased}).
     */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.gbfs.base-url";

    private final List<SourceAdapter> adapters;

    /**
     * Creates the family.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank in production
     * @param meters the registry of the adapters' counters, resolved lazily
     * @throws IllegalStateException when the reviewed list is invalid
     */
    public GbfsFamily(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<MeterRegistry> meters) {
        this(baseUrlOverride, meters == null ? () -> null : (Supplier<MeterRegistry>) meters::getIfAvailable);
    }

    /**
     * Creates the family with a plain supplier (tests).
     *
     * @param baseUrlOverride the test base URL, or blank
     * @param meters supplies the meter registry, or null
     */
    public GbfsFamily(String baseUrlOverride, Supplier<MeterRegistry> meters) {
        List<String> problems = GbfsSystems.problems(GbfsSystems.REVIEWED);
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Foresignal cannot start: the reviewed GBFS system list is invalid: "
                    + problems);
        }
        String base = baseUrlOverride == null || baseUrlOverride.isBlank() ? null : baseUrlOverride.strip();
        List<SourceAdapter> built = new ArrayList<>();
        for (GbfsSystem system : GbfsSystems.REVIEWED) {
            built.add(new GbfsAdapter(base == null ? system : system.rebased(base), meters));
        }
        this.adapters = List.copyOf(built);
    }

    @Override
    public List<SourceAdapter> adapters() {
        return adapters;
    }
}
