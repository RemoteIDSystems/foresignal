package ai.foresignal.view;

import ai.foresignal.api.support.BboxParser;
import ai.foresignal.briefing.BriefingScope;
import ai.foresignal.briefing.ScopeResolver;
import ai.foresignal.place.PlaceService;
import ai.foresignal.place.PlaceView;
import ai.foresignal.security.restrict.RecordFilter;
import ai.foresignal.security.restrict.RecordFilters;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * The briefing {@link ScopeResolver} of prompt 036: the 029 members as given, plus {@code placeIds}
 * (the places' areas, as a request filter ANDed into the run's restriction, and their names for the
 * digest heading) and {@code viewId} (the view's predicate; its sources and kinds become the scope's
 * when the scope names none). A deleted place or view matches nothing (fail closed). The LLM sees
 * place names only, never coordinates.
 */
@Component
@Primary
public class PlaceBriefingScopeResolver implements ScopeResolver {

    private final PlaceService places;
    private final SavedViewService views;
    private final RecordScopes scopes;

    /** Creates the resolver. */
    public PlaceBriefingScopeResolver(PlaceService places, SavedViewService views, RecordScopes scopes) {
        this.places = places;
        this.views = views;
        this.scopes = scopes;
    }

    @Override
    public Resolved resolve(BriefingScope scope) {
        BriefingScope s = scope == null ? BriefingScope.all() : scope;
        List<String> sources = s.sources();
        List<String> kinds = s.kinds();
        RecordFilter filter = null;
        List<String> names = new ArrayList<>();
        if (!s.placeIds().isEmpty()) {
            List<String> known = new ArrayList<>();
            for (String id : s.placeIds()) {
                Optional<PlaceView> place = places.find(id);
                place.ifPresent(p -> {
                    known.add(p.id());
                    names.add(p.name());
                });
            }
            filter = known.size() == s.placeIds().size() ? scopes.places(known, null) : nothing("place:deleted");
        }
        if (s.viewId() != null) {
            Optional<SavedViewEntity> view = views.find(s.viewId());
            if (view.isPresent()) {
                SavedViewService.CompiledView compiled = views.compile(view.get());
                filter = filter == null ? compiled.filter() : RecordFilters.and(filter, compiled.filter());
                if (sources.isEmpty()) {
                    sources = view.get().getSources();
                }
                if (kinds.isEmpty()) {
                    kinds = view.get().getKinds();
                }
            } else {
                filter = nothing("view:deleted");
            }
        }
        return new Resolved(sources, kinds, BboxParser.parse(s.bbox()), new LinkedHashSet<>(s.ruleIds()),
                s.includeHealth(), filter, names, s.regionFilter());
    }

    /** A filter that matches nothing (an empty area), for a deleted place or view. */
    static PredicateFilter nothing(String canonical) {
        org.locationtech.jts.geom.Geometry empty = new org.locationtech.jts.geom.GeometryFactory(
                new org.locationtech.jts.geom.PrecisionModel(), 4326).createPolygon();
        return new PredicateFilter(new ai.foresignal.trigger.RecordPredicate(java.util.Set.of(), java.util.Set.of(),
                List.of(), empty), canonical);
    }
}
