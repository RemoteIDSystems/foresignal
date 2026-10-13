package ai.foresignal.source.adapters.local;

import ai.foresignal.source.codec.CodecRegistry;
import ai.foresignal.source.codec.GpsdCodec;
import ai.foresignal.source.codec.Sbs1Codec;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceAdapterFamily;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Registers the local receiver slots (prompt 069): {@code foresignal.local-receivers.slots} (default 2, at
 * most 16) numbered sources of each family: {@code local-adsb-{n}}, {@code local-sbs-{n}},
 * {@code local-gpsd-{n}}, {@code station-readsb-{n}}, {@code wxstation-davis-{n}} and
 * {@code wxstation-ecowitt-{n}}.
 *
 * <p>The slot lists are code-reviewed (the family rule of {@link SourceAdapterFamily}); the operator supplies
 * only LAN parameters, never a host list. Every slot is disabled by default with {@code egress} LOCAL_ONLY.
 */
@Component
public class LocalReceiversFamily implements SourceAdapterFamily {

    private final List<SourceAdapter> adapters;

    /**
     * Creates the family.
     *
     * @param slots slots per family, 1 to {@value LocalReceivers#MAX_SLOTS}
     * @param codecs supplies the {@code sbs1} and {@code gpsd} codecs
     * @param meters the registry of the adapters' counters, resolved lazily
     * @throws IllegalStateException when {@code slots} is out of range
     */
    public LocalReceiversFamily(@Value("${" + LocalReceivers.SLOTS_PROPERTY + ":" + LocalReceivers.DEFAULT_SLOTS + "}") int slots,
            CodecRegistry codecs, ObjectProvider<MeterRegistry> meters) {
        if (slots < 1 || slots > LocalReceivers.MAX_SLOTS) {
            throw new IllegalStateException("Foresignal cannot start: " + LocalReceivers.SLOTS_PROPERTY + " must be 1 to "
                    + LocalReceivers.MAX_SLOTS + ", but was " + slots);
        }
        java.util.function.Supplier<MeterRegistry> registry = meters == null ? () -> null : meters::getIfAvailable;
        List<SourceAdapter> built = new ArrayList<>();
        for (int slot = 1; slot <= slots; slot++) {
            built.add(new LocalAdsbAdapter(slot));
            built.add(new LocalSbsAdapter(slot, codecs.require(Sbs1Codec.ID)));
            built.add(new LocalGpsdAdapter(slot, codecs.require(GpsdCodec.ID)));
            built.add(new StationReadsbAdapter(slot));
            built.add(new DavisWllAdapter(slot));
            built.add(new EcowittAdapter(slot, registry));
        }
        this.adapters = List.copyOf(built);
    }

    @Override
    public List<SourceAdapter> adapters() {
        return adapters;
    }
}
