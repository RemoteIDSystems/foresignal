package ai.foresignal.source.adapters.env.airq;

import ai.foresignal.source.adapters.local.LocalReceivers;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceAdapterFamily;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Registers the {@code airq-local-{n}} slots (prompt 078): one per local receiver slot, {@value
 * LocalReceivers#SLOTS_PROPERTY} (prompt 069; default {@value LocalReceivers#DEFAULT_SLOTS}, at most {@value
 * LocalReceivers#MAX_SLOTS}).
 *
 * <p>The slot list is code-defined (the family rule of {@link SourceAdapterFamily}); the operator supplies only LAN
 * parameters, never a host list. Every slot is disabled by default, private, with egress {@code LOCAL_ONLY}.
 */
@Component
public class AirqLocalFamily implements SourceAdapterFamily {

    private final List<SourceAdapter> adapters;

    /**
     * Creates the family.
     *
     * @param slots the number of slots, 1 to {@value LocalReceivers#MAX_SLOTS}
     * @throws IllegalStateException when {@code slots} is out of range
     */
    @Autowired
    public AirqLocalFamily(@Value("${" + LocalReceivers.SLOTS_PROPERTY + ":" + LocalReceivers.DEFAULT_SLOTS + "}")
            int slots) {
        if (slots < 1 || slots > LocalReceivers.MAX_SLOTS) {
            throw new IllegalStateException("Foresignal cannot start: " + LocalReceivers.SLOTS_PROPERTY + " must be 1"
                    + " to " + LocalReceivers.MAX_SLOTS + ", but was " + slots);
        }
        List<SourceAdapter> built = new ArrayList<>();
        for (int slot = 1; slot <= slots; slot++) {
            built.add(new AirqLocalAdapter(slot));
        }
        this.adapters = List.copyOf(built);
    }

    @Override
    public List<SourceAdapter> adapters() {
        return adapters;
    }
}
