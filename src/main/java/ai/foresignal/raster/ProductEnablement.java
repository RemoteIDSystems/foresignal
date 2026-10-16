package ai.foresignal.raster;

import ai.foresignal.source.spi.ValidatedParams;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Per-product enablement for the extended weather products (prompt 106): each product lives behind a
 * {@code products} string-list parameter on its source, whose default comes from the memory profile
 * (small or large, Weather-Imagery-Design §2.6). An operator overrides it by listing product ids.
 *
 * <p>A product not in the list is not fetched or committed, so its frames, tokens and disk cost
 * disappear. The profile default keeps the heavy products (C02, GOES-18, GFS-Wave, SST, AIFS, mesoscale
 * sectors, NEXRAD L3, MRMS extras) off on a small host.
 */
public final class ProductEnablement {

    /** The parameter name every extended source uses. */
    public static final String PRODUCTS_PARAM = "products";

    private ProductEnablement() {}

    /**
     * The enabled product ids.
     *
     * @param params the source's params, or null
     * @param profileValue {@code foresignal.profile} ({@code small} or {@code large})
     * @param all every product id the source knows (unknown ids in the param are ignored)
     * @param lightDefault the ids on by default on the small profile
     * @param largeDefault the ids on by default on the large profile
     */
    public static Set<String> enabled(ValidatedParams params, String profileValue, Collection<String> all,
            Collection<String> lightDefault, Collection<String> largeDefault) {
        List<String> requested = params == null ? List.of() : params.stringList(PRODUCTS_PARAM);
        Set<String> out = new LinkedHashSet<>();
        if (requested != null && !requested.isEmpty()) {
            for (String id : requested) {
                if (id == null) {
                    continue;
                }
                String normalized = id.strip().toLowerCase(Locale.ROOT);
                if (all.contains(normalized)) {
                    out.add(normalized);
                }
            }
            return out;
        }
        RasterProfile profile = RasterProfile.parse(profileValue);
        out.addAll(profile == RasterProfile.LARGE ? largeDefault : lightDefault);
        out.retainAll(all);
        return out;
    }

    /** True when {@code productId} is enabled. */
    public static boolean on(Set<String> enabled, String productId) {
        return enabled.contains(productId);
    }
}
