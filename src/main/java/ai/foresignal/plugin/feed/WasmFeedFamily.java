package ai.foresignal.plugin.feed;

import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.plugin.PluginParsers;
import ai.foresignal.plugin.PluginRuntime;
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
 * Registers one {@link WasmFeedAdapter} per {@code wasm-feed} spec (prompt 141). The specs are the operator's files
 * under {@code ${FORESIGNAL_DATA_DIR}/wasm-feeds/}, read once at startup by {@link WasmFeedSpecs}, never supplied by an
 * API caller (the rule of {@link SourceAdapterFamily}). Every source ships disabled.
 */
@Component
public class WasmFeedFamily implements SourceAdapterFamily {

    private final List<SourceAdapter> adapters;

    /**
     * Creates the family.
     *
     * @param specs the checked specs
     * @param runtime runs the plugins
     * @param policy the outbound URL policy, resolved on every fetch
     * @param meters the registry of the adapters' counters, resolved lazily
     */
    @Autowired
    public WasmFeedFamily(WasmFeedSpecs specs, PluginRuntime runtime, ObjectProvider<OutboundUrlPolicy> policy,
            ObjectProvider<MeterRegistry> meters) {
        this(specs.sources(), runtime, UrlGate.outbound(policy == null ? () -> null : policy::getIfAvailable),
                meters == null ? () -> null : (Supplier<MeterRegistry>) meters::getIfAvailable);
    }

    /**
     * Creates the family from loaded sources (tests).
     *
     * @param sources the loaded sources
     * @param parsers runs the plugins
     * @param gate checks URLs before every fetch
     * @param meters supplies the meter registry, or null
     */
    public WasmFeedFamily(List<WasmFeedSpecs.WasmFeedSource> sources, PluginParsers parsers, UrlGate gate,
            Supplier<MeterRegistry> meters) {
        List<SourceAdapter> built = new ArrayList<>();
        for (WasmFeedSpecs.WasmFeedSource source : sources) {
            built.add(new WasmFeedAdapter(source.spec(), source.descriptor(), parsers, gate, meters));
        }
        this.adapters = List.copyOf(built);
    }

    @Override
    public List<SourceAdapter> adapters() {
        return adapters;
    }
}
