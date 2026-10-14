package ai.foresignal.source.adapters.stac;

import ai.foresignal.source.spi.PlacePoints;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceAdapterFamily;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Registers the {@code stac-*} sources (prompt 096): one {@link EarthSearchAdapter} per feed of the
 * reviewed list ({@link StacSources#REVIEWED}). The list is validated at startup
 * ({@link StacSources#problems}); an invalid list stops Foresignal rather than registering a collection
 * nobody reviewed. Feed lists are never caller-supplied.
 */
@Component
public class EarthSearchFamily implements SourceAdapterFamily {

    /**
     * Test-only override moving every Earth Search URL to one base (scheme and authority); all feeds share
     * the same host, so no path prefix is added.
     */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.stac.base-url";

    private final List<SourceAdapter> adapters;

    /**
     * Creates the family.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank in production
     * @param places resolves a place id to a point, resolved lazily
     * @throws IllegalStateException when the reviewed list is invalid
     */
    @Autowired
    public EarthSearchFamily(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<PlacePoints> places) {
        this(baseUrlOverride, places == null ? () -> null : (Supplier<PlacePoints>) places::getIfAvailable);
    }

    /**
     * Creates the family with a plain supplier (tests).
     *
     * @param baseUrlOverride the test base URL, or blank
     * @param places supplies the place lookup, or null
     */
    public EarthSearchFamily(String baseUrlOverride, Supplier<PlacePoints> places) {
        List<String> problems = StacSources.problems(StacSources.REVIEWED);
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Foresignal cannot start: the reviewed Earth Search feed list is"
                    + " invalid: " + problems);
        }
        String base = baseUrlOverride == null || baseUrlOverride.isBlank() ? null : baseUrlOverride.strip();
        List<SourceAdapter> built = new ArrayList<>();
        for (StacSource source : StacSources.REVIEWED) {
            built.add(new EarthSearchAdapter(source, base, places));
        }
        this.adapters = List.copyOf(built);
    }

    @Override
    public List<SourceAdapter> adapters() {
        return adapters;
    }
}
