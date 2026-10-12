package ai.foresignal.view;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.place.PlaceService;
import ai.foresignal.place.PlaceView;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.ReadScope;
import ai.foresignal.security.restrict.ReadScopes;
import ai.foresignal.security.restrict.RecordFilter;
import ai.foresignal.security.restrict.RecordFilters;
import ai.foresignal.trigger.IdentityOperator;
import ai.foresignal.trigger.Operator;
import ai.foresignal.trigger.OperatorRegistry;
import ai.foresignal.trigger.RecordPredicate;
import ai.foresignal.trigger.RuleCondition;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.MultiPolygon;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Turns {@code place=}, {@code view=} and {@code inWatchlist=} into a request filter (prompt 036): the
 * {@link ReadScopes} of every read path that accepts them (records, history, changes, tiles, the
 * streams and search), intersected with the other filters and with the caller's 035 restriction.
 *
 * <ul>
 *   <li>{@code place}: the union of the places' areas, buffered by {@code radiusKm} when given;
 *       intersected with {@code ST_Intersects} behind a {@code &&} prefilter.
 *   <li>{@code view}: the view's compiled predicate.
 *   <li>{@code inWatchlist}: an identity condition over every source that declares the list's
 *       identity (a {@code custom} list matches nothing here; use a view with a path).
 * </ul>
 *
 * <p>Places are never anonymous: {@code place} and {@code inWatchlist}, and a view that references a
 * place or a watchlist, answer {@code 401} to anonymous callers and feed keys.
 */
@Component
public class RecordScopes implements ReadScopes {

    private final PlaceService places;
    private final SavedViewService views;
    private final OperatorRegistry operators;

    /** Creates the resolver. */
    public RecordScopes(PlaceService places, SavedViewService views, OperatorRegistry operators) {
        this.places = places;
        this.views = views;
        this.operators = operators;
    }

    @Override
    public ReadRestriction narrow(ReadRestriction base, ReadScope scope, boolean tokenPresent) {
        if (scope == null || scope.isEmpty()) {
            return base;
        }
        RecordFilter filter = null;
        if (!scope.placeIds().isEmpty()) {
            requireToken(tokenPresent, "place");
            filter = and(filter, places(scope.placeIds(), scope.radiusKm()));
        }
        if (scope.view() != null) {
            SavedViewService.CompiledView view = views.compile(views.get(scope.view()));
            if (view.requiresToken()) {
                requireToken(tokenPresent, "a view that references a place or a watchlist");
            }
            filter = and(filter, view.filter());
        }
        if (scope.inWatchlist() != null) {
            requireToken(tokenPresent, "inWatchlist");
            filter = and(filter, watchlist(scope.inWatchlist()));
        }
        return base.withFilter(filter);
    }

    /** The filter of a place list. */
    public PredicateFilter places(List<String> ids, Double radiusKm) {
        List<PlaceView> resolved = places.resolve(ids);
        MultiPolygon area = places.union(ids, radiusKm == null ? 0 : radiusKm);
        String canonical = "place:" + resolved.stream().map(p -> p.id() + "@" + p.version())
                .collect(Collectors.joining(",")) + ":" + (radiusKm == null ? 0 : radiusKm);
        return new PredicateFilter(new RecordPredicate(Set.of(), Set.of(), List.of(), area), canonical);
    }

    /** The filter of a watchlist id. */
    public PredicateFilter watchlist(String id) {
        Operator op = operators.find("inWatchlist").orElseThrow(() -> unknownWatchlist(id));
        Object value;
        try {
            value = op.normalize(id);
        } catch (IllegalArgumentException e) {
            throw unknownWatchlist(id);
        }
        if (!(op instanceof IdentityOperator identity) || !identity.known(value)) {
            throw unknownWatchlist(id);
        }
        RuleCondition condition = new RuleCondition(null, op, value, identity.identityPaths(value, List.of()));
        return new PredicateFilter(RecordPredicate.of(List.of(condition)), "watchlist:" + value);
    }

    private static RecordFilter and(RecordFilter a, RecordFilter b) {
        return a == null ? b : RecordFilters.and(a, b);
    }

    private static void requireToken(boolean tokenPresent, String what) {
        if (!tokenPresent) {
            throw new ForesignalException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED,
                    "Filtering by " + what + " needs a token: places and watchlists are never anonymous.");
        }
    }

    private static ForesignalException unknownWatchlist(Object id) {
        return new ForesignalException(HttpStatus.NOT_FOUND, ErrorCodes.UNKNOWN_WATCHLIST,
                "No watchlist has the id '" + id + "'.");
    }

    /** The view id of a request, parsed as UUID when it is one (for logs). */
    static String describe(String idOrSlug) {
        try {
            return UUID.fromString(idOrSlug).toString();
        } catch (IllegalArgumentException e) {
            return idOrSlug;
        }
    }
}
