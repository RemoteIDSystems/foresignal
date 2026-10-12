package ai.foresignal.security.restrict;

import java.util.List;

/**
 * The request parameters that narrow a read beyond its filters (prompt 036): {@code place=} (with
 * {@code radiusKm} as an extra buffer when the endpoint has no {@code near}), {@code view=} and
 * {@code inWatchlist=}. A {@link ReadScopes} turns them into a {@link RecordFilter}.
 *
 * @param placeIds place ids; empty for none
 * @param radiusKm extra buffer around the places in km, or null
 * @param view a saved view id or slug, or null
 * @param inWatchlist a watchlist id, or null
 */
public record ReadScope(List<String> placeIds, Double radiusKm, String view, String inWatchlist) {

    /** No narrowing. */
    public static final ReadScope NONE = new ReadScope(List.of(), null, null, null);

    /** Copies the ids; blank values become null. */
    public ReadScope {
        placeIds = placeIds == null ? List.of() : List.copyOf(placeIds);
        view = view == null || view.isBlank() ? null : view.strip();
        inWatchlist = inWatchlist == null || inWatchlist.isBlank() ? null : inWatchlist.strip();
    }

    /** True when nothing narrows the read. */
    public boolean isEmpty() {
        return placeIds.isEmpty() && view == null && inWatchlist == null;
    }
}
