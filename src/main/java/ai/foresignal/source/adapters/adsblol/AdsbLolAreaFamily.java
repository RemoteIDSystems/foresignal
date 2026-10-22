package ai.foresignal.source.adapters.adsblol;

import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceAdapterFamily;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The {@code adsblol-area-1..3} slots (prompt 062): three ≤ 250 nm circles an operator can center anywhere,
 * each a separate source with its own settings, budget and health, all disabled by default.
 *
 * <p>The slot list is fixed in code, and the parameters are validated numbers, never URLs (the family rule
 * in {@link SourceAdapterFamily}). The worldwide coverage is {@code adsblol-world}, whose
 * {@code /v2/lat/lon/dist} path takes continent-sized radii; these slots are additions on top of it.
 */
@Component
public class AdsbLolAreaFamily implements SourceAdapterFamily {

    /** How many slots exist. */
    public static final int SLOTS = 3;

    private final List<SourceAdapter> adapters;

    /**
     * Creates the family.
     *
     * @param baseUrlOverride the value of {@code foresignal.test.adapters.adsblol.base-url}; blank means the
     *     production upstream
     */
    public AdsbLolAreaFamily(@Value("${" + AdsbLol.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        String baseUrl = AdsbLol.baseUrl(baseUrlOverride);
        List<SourceAdapter> built = new ArrayList<>(SLOTS);
        for (int slot = 1; slot <= SLOTS; slot++) {
            built.add(new AdsbLolAreaAdapter(slot, baseUrl));
        }
        this.adapters = List.copyOf(built);
    }

    @Override
    public List<SourceAdapter> adapters() {
        return adapters;
    }
}
