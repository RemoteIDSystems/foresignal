package ai.foresignal.source.adapters.acars;

import ai.foresignal.source.codec.AcarsJsonCodec;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceAdapterFamily;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Registers the {@code local-acars-{n}} slots (prompt 086) on prompt 069's slot pattern: as many slots as
 * {@value #SLOTS_PROPERTY} (default {@value #DEFAULT_SLOTS}, at most {@value #MAX_SLOTS}), every one disabled by default.
 * The property and its bounds are the local receivers' own, repeated here because adapter packages do not import each
 * other.
 */
@Component
public class LocalAcarsFamily implements SourceAdapterFamily {

    /** The slot count property, shared with prompt 069's local receivers. */
    public static final String SLOTS_PROPERTY = "foresignal.local-receivers.slots";

    /** The slots when the property is unset. */
    public static final int DEFAULT_SLOTS = 2;

    /** The most slots. */
    public static final int MAX_SLOTS = 16;

    private final List<SourceAdapter> adapters;

    /**
     * Creates the family.
     *
     * @param slots the slot count, 1 to {@value #MAX_SLOTS}
     * @param codec the {@value AcarsJsonCodec#ID} codec
     * @throws IllegalStateException when {@code slots} is out of range
     */
    @Autowired
    public LocalAcarsFamily(@Value("${" + SLOTS_PROPERTY + ":" + DEFAULT_SLOTS + "}") int slots, AcarsJsonCodec codec) {
        if (slots < 1 || slots > MAX_SLOTS) {
            throw new IllegalStateException("Foresignal cannot start: " + SLOTS_PROPERTY + " must be 1 to " + MAX_SLOTS
                    + ", but was " + slots);
        }
        List<SourceAdapter> built = new ArrayList<>();
        for (int slot = 1; slot <= slots; slot++) {
            built.add(new LocalAcarsAdapter(slot, codec));
        }
        this.adapters = List.copyOf(built);
    }

    @Override
    public List<SourceAdapter> adapters() {
        return adapters;
    }
}
