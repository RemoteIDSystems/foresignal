package ai.foresignal.source.kit.feature;

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
 * Registers one {@link KitFeatureAdapter} per feature-kit spec (prompt 073): the reviewed
 * {@code kits/features/*.json} files plus, when enabled, the operator overlay. Every source ships disabled.
 *
 * <p>The specs are loaded and validated by {@link KitSpecs}; an invalid spec stops startup with every problem listed.
 */
@Component
public class FeatureKitFamily implements SourceAdapterFamily {

    private final List<SourceAdapter> adapters;

    /**
     * Creates the family.
     *
     * @param specs the validated kit specs
     * @param meters the registry of the adapters' counters, resolved lazily
     */
    @Autowired
    public FeatureKitFamily(KitSpecs specs, ObjectProvider<MeterRegistry> meters) {
        this(specs.features(), specs.testBaseUrl(),
                meters == null ? () -> null : (Supplier<MeterRegistry>) meters::getIfAvailable);
    }

    /**
     * Creates the family from specs (tests).
     *
     * @param specs the validated specs
     * @param baseUrl the test base URL, or blank
     * @param meters supplies the meter registry, or null
     */
    public FeatureKitFamily(List<FeatureSourceSpec> specs, String baseUrl, Supplier<MeterRegistry> meters) {
        List<SourceAdapter> built = new ArrayList<>();
        for (FeatureSourceSpec spec : specs) {
            built.add(new KitFeatureAdapter(KitFeatureAdapter.rebased(spec, baseUrl), meters));
        }
        this.adapters = List.copyOf(built);
    }

    @Override
    public List<SourceAdapter> adapters() {
        return adapters;
    }
}
