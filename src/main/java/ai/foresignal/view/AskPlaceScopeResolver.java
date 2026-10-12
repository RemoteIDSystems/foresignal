package ai.foresignal.view;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.ask.AskScope;
import ai.foresignal.ask.AskScopeResolver;
import ai.foresignal.place.PlaceService;
import ai.foresignal.place.PlaceView;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.ReadScope;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Resolves Ask's {@code placeIds} and {@code viewId} (prompt 036; the hook of prompt 028). The scope
 * keeps the ids and gains the place <em>names</em> (what the model sees: names and, in record items,
 * km-rounded distances, never place coordinates); a view's sources and kinds become the scope's when
 * it names none. The places' areas and the view's predicate narrow the tool caller's restriction, so
 * every read tool and retrieval runs inside them ({@code RestrictionClamp}).
 */
@Component
public class AskPlaceScopeResolver implements AskScopeResolver {

    private final PlaceService places;
    private final SavedViewService views;
    private final RecordScopes scopes;

    /** Creates the resolver. */
    public AskPlaceScopeResolver(PlaceService places, SavedViewService views, RecordScopes scopes) {
        this.places = places;
        this.views = views;
        this.scopes = scopes;
    }

    @Override
    public AskScope resolve(AskScope scope) {
        List<FieldError> errors = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (String id : scope.placeIds()) {
            places.find(id).map(PlaceView::name).ifPresentOrElse(names::add,
                    () -> errors.add(new FieldError("scope.placeIds", "unknown place '" + id + "'")));
        }
        List<String> sources = scope.sources();
        List<String> kinds = scope.kinds();
        if (scope.viewId() != null) {
            var view = views.find(scope.viewId());
            if (view.isEmpty()) {
                errors.add(new FieldError("scope.viewId", "unknown view '" + scope.viewId() + "'"));
            } else {
                if (sources.isEmpty()) {
                    sources = view.get().getSources();
                }
                if (kinds.isEmpty()) {
                    kinds = view.get().getKinds();
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The question's scope is not valid.", errors);
        }
        return new AskScope(sources, kinds, scope.bbox(), scope.since(), scope.until(), scope.placeIds(),
                scope.viewId(), names);
    }

    @Override
    public ReadRestriction restriction(AskScope scope, ReadRestriction base) {
        try {
            return scopes.narrow(base, new ReadScope(scope.placeIds(), null, scope.viewId(), null), true);
        } catch (ForesignalException e) {
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, "The question's scope is not valid.",
                    List.of(new FieldError("scope", e.getMessage())));
        }
    }
}
