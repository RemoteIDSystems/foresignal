package ai.foresignal.source.adapters.cctv;

import ai.foresignal.config.ForesignalVersion;
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
 * Registers the {@code cctv-*} sources (prompt 072): one {@link CctvPackAdapter} per pack of the reviewed list
 * ({@link CctvPacks#REVIEWED}), every one disabled by default.
 *
 * <p>The list is validated at startup ({@link CctvPacks#problems}); an invalid list stops Foresignal rather than
 * registering a URL nobody reviewed. Pack lists are never caller-supplied.
 */
@Component
public class CctvCatalogFamily implements SourceAdapterFamily {

    /**
     * Test-only override moving every catalog URL under one base (scheme and authority), with the original host as
     * the first path segment ({@link CctvPack#rebased}).
     */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.cctv.base-url";

    private final List<SourceAdapter> adapters;

    /**
     * Creates the family.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank in production
     * @param version Foresignal's version, for the {@code Digitraffic-User} header
     * @param meters the registry of the adapters' counters, resolved lazily
     * @throws IllegalStateException when the reviewed list is invalid
     */
    public CctvCatalogFamily(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<ForesignalVersion> version, ObjectProvider<MeterRegistry> meters) {
        this(baseUrlOverride,
                () -> {
                    ForesignalVersion v = version == null ? null : version.getIfAvailable();
                    return v == null ? ForesignalVersion.FALLBACK : v.version();
                },
                meters == null ? () -> null : (Supplier<MeterRegistry>) meters::getIfAvailable);
    }

    /**
     * Creates the family with plain suppliers (tests).
     *
     * @param baseUrlOverride the test base URL, or blank
     * @param version supplies Foresignal's version
     * @param meters supplies the meter registry, or null
     */
    public CctvCatalogFamily(String baseUrlOverride, Supplier<String> version, Supplier<MeterRegistry> meters) {
        List<String> problems = CctvPacks.problems(CctvPacks.REVIEWED);
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Foresignal cannot start: the reviewed CCTV pack list is invalid: "
                    + problems);
        }
        String base = baseUrlOverride == null || baseUrlOverride.isBlank() ? null : baseUrlOverride.strip();
        List<SourceAdapter> built = new ArrayList<>();
        for (CctvPack pack : CctvPacks.REVIEWED) {
            built.add(new CctvPackAdapter(base == null ? pack : pack.rebased(base), version, meters));
        }
        this.adapters = List.copyOf(built);
    }

    @Override
    public List<SourceAdapter> adapters() {
        return adapters;
    }
}
