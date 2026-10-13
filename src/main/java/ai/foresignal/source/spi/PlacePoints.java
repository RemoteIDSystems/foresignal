package ai.foresignal.source.spi;

import java.util.List;
import java.util.Optional;

/**
 * Resolves a place id to a point, for an adapter parameter that may name a place instead of
 * coordinates (prompt 049: a SatDump station's {@code location}).
 *
 * <p>Adapters depend only on SPI types (Architecture §3), so places reach them through this interface;
 * {@code ai.foresignal.place.PlaceService} implements it.
 */
public interface PlacePoints {

    /**
     * The place's representative point as {@code {lon, lat}} in WGS84 degrees (the point itself, or
     * the interior point of a polygon or route), or empty when there is no such place.
     */
    Optional<double[]> point(String placeId);

    /**
     * Every place as an adapter may see it (prompt 053, {@code nws-gridpoints}), ordered by id. The default
     * is none, for test doubles that resolve single points only.
     */
    default List<Summary> summaries() {
        return List.of();
    }

    /**
     * One place for an adapter: what a per-place upstream request needs, nothing more.
     *
     * @param id the place id
     * @param name the display name
     * @param lon the representative point's longitude, WGS84 degrees
     * @param lat the representative point's latitude
     * @param products the place's {@code products} list (for example {@code nws})
     * @param version the place's version, which changes on every edit
     */
    record Summary(String id, String name, double lon, double lat, List<String> products, int version) {

        /** Copies the products. */
        public Summary {
            products = products == null ? List.of() : List.copyOf(products);
        }
    }
}
