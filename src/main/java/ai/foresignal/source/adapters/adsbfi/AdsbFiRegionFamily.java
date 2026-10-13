package ai.foresignal.source.adapters.adsbfi;

import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceAdapterFamily;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The {@code adsbfi-region-1..4} slots (prompt 080): four circles of at most 250 nm an operator can center
 * anywhere, each a separate source with its own settings, budget and health, all disabled by default and all on
 * the one paced host {@code opendata.adsb.fi}.
 *
 * <p>The slot list is fixed in code, and the parameters are validated numbers, never URLs (the family rule in
 * {@link SourceAdapterFamily}).
 */
@Component
public class AdsbFiRegionFamily implements SourceAdapterFamily {

    /** How many slots exist. */
    public static final int SLOTS = 4;

    private final List<SourceAdapter> adapters;

    /**
     * Creates the family.
     *
     * @param originOverride the value of {@code foresignal.test.adapters.adsbfi.base-url}; blank means the
     *     production upstream
     */
    @Autowired
    public AdsbFiRegionFamily(@Value("${" + AdsbFi.BASE_URL_PROPERTY + ":}") String originOverride) {
        String origin = AdsbFi.origin(originOverride);
        List<SourceAdapter> built = new ArrayList<>(SLOTS);
        for (int slot = 1; slot <= SLOTS; slot++) {
            built.add(new AdsbFiRegionAdapter(slot, origin));
        }
        this.adapters = List.copyOf(built);
    }

    @Override
    public List<SourceAdapter> adapters() {
        return adapters;
    }
}
