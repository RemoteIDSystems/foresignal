package ai.foresignal.sink;

import ai.foresignal.place.PlaceService;
import ai.foresignal.watch.Identity;
import ai.foresignal.watch.IdentityResolver;
import ai.foresignal.watch.WatchlistSnapshot;
import java.util.Optional;
import java.util.UUID;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Resolves the {@code placeId} and {@code watchlistId} members of a {@link SinkFilter} (prompt 110, 036).
 *
 * <ul>
 *   <li><em>Place:</em> a record passes when its geometry intersects the place's area. A record without a
 *       geometry, or a place that no longer exists, does not pass (fail closed: an operator who scoped a sink to
 *       a place never gets the whole world because the place was deleted).
 *   <li><em>Watchlist:</em> a record passes when the list's identity (icao24, registration, callsign, mmsi, imo),
 *       read from the payload path the source declares in {@code entityKeys} and normalized as the list
 *       normalizes, is on the list; a {@code custom} list matches the record's external id. An {@code entity}
 *       list (098) is not expanded here and matches nothing; an unknown list matches nothing.
 * </ul>
 *
 * <p>Both lookups read in-memory snapshots (the place cache and {@link WatchlistSnapshot}), so the check is cheap
 * enough for every live update.
 */
@Component
public class SinkScope {

    /**
     * A resolver without place or watchlist services (tests): filters without {@code placeId}/{@code watchlistId}
     * pass, and a filter that names either matches nothing (fail closed).
     */
    public static final SinkScope NONE = new SinkScope(null, null, null);

    private final ObjectProvider<PlaceService> places;
    private final ObjectProvider<WatchlistSnapshot> watchlists;
    private final ObjectProvider<IdentityResolver> identities;

    /** Creates the scope resolver; every collaborator is optional. */
    public SinkScope(ObjectProvider<PlaceService> places, ObjectProvider<WatchlistSnapshot> watchlists,
            ObjectProvider<IdentityResolver> identities) {
        this.places = places;
        this.watchlists = watchlists;
        this.identities = identities;
    }

    /** True when {@code record} passes the filter's {@code placeId} and {@code watchlistId} (absent ones pass). */
    public boolean matches(SinkFilter filter, SinkRecord record) {
        if (filter.placeId() != null && !inPlace(filter.placeId(), record)) {
            return false;
        }
        return filter.watchlistId() == null || onWatchlist(filter.watchlistId(), record);
    }

    /** True when the record's geometry intersects the place's area. */
    boolean inPlace(String placeId, SinkRecord record) {
        if (record.geometry() == null || record.geometry().isEmpty()) {
            return false;
        }
        PlaceService service = places == null ? null : places.getIfAvailable();
        if (service == null) {
            return false;
        }
        Optional<Geometry> area = service.geometry(placeId);
        return area.isPresent() && area.get().intersects(record.geometry());
    }

    /** True when the record's identity is on the watchlist. */
    boolean onWatchlist(String watchlistId, SinkRecord record) {
        UUID id;
        try {
            id = UUID.fromString(watchlistId);
        } catch (IllegalArgumentException e) {
            return false;
        }
        WatchlistSnapshot snapshot = watchlists == null ? null : watchlists.getIfAvailable();
        if (snapshot == null) {
            return false;
        }
        Optional<WatchlistSnapshot.ListSnapshot> list = snapshot.list(id);
        if (list.isEmpty()) {
            return false;
        }
        Identity identity = list.get().identity();
        String value;
        if (identity == Identity.CUSTOM) {
            value = record.externalId();
        } else if (identity == Identity.ENTITY) {
            return false;
        } else {
            IdentityResolver resolver = identities == null ? null : identities.getIfAvailable();
            String path = resolver == null ? null : resolver.path(record.sourceId(), identity);
            value = path == null ? null : record.string(path);
        }
        String normalized = list.get().normalize().apply(value);
        return normalized != null && !normalized.isEmpty() && list.get().values().contains(normalized);
    }
}
