package ai.foresignal.source.adapters.seismic;

import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceAdapterFamily;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Registers one {@link FdsnEventAdapter} per reviewed FDSN event service (prompt 089): {@code fdsn-geofon} and
 * {@code fdsn-ingv}, from {@link FdsnServices#RESOURCE}. Every source ships disabled.
 */
@Component
public class FdsnEventFamily implements SourceAdapterFamily {

    /** Test-only override moving every service URL under one base (scheme and authority), keeping the path. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.fdsn.base-url";

    private final List<SourceAdapter> adapters;

    /** Creates the family. */
    @Autowired
    public FdsnEventFamily(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this(baseUrlOverride, FdsnServices.load());
    }

    /** Creates the family from an explicit list (tests). */
    public FdsnEventFamily(String baseUrlOverride, List<FdsnService> services) {
        List<String> problems = FdsnServices.problems(services);
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Foresignal cannot start: the reviewed FDSN service list is invalid: "
                    + problems);
        }
        String base = baseUrlOverride == null || baseUrlOverride.isBlank() ? null : baseUrlOverride.strip();
        List<SourceAdapter> built = new ArrayList<>();
        for (FdsnService service : services) {
            built.add(new FdsnEventAdapter(base == null ? service : service.rebased(base)));
        }
        this.adapters = List.copyOf(built);
    }

    @Override
    public List<SourceAdapter> adapters() {
        return adapters;
    }
}
