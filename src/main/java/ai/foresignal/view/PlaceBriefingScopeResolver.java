package ai.foresignal.view;

import ai.foresignal.api.support.BboxParser;
import ai.foresignal.briefing.BriefingScope;
import ai.foresignal.briefing.ScopeResolver;
import ai.foresignal.place.PlaceService;
import ai.foresignal.place.PlaceView;
import ai.foresignal.security.restrict.RecordFilter;
import ai.foresignal.security.restrict.RecordFilters;
import ai.foresignal.source.group.SourceGroupLookup;
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
 *
 * <p>Prompt SG-002 adds {@code sourceGroupIds}: each group's member sources are unioned into the scope's sources (the
 * group bounds the scope), resolved by id through {@link SourceGroupLookup}. A group that is unknown, deleted or
 * resolves to no members makes the scope match nothing (fail closed), exactly like a deleted place or view, so a
 * dangling group never widens the scope to every source.
 */
@Component
@Primary
public class PlaceBriefingScopeResolver implements ScopeResolver {

    private final PlaceService places;
    private final SavedViewService views;
    private final RecordScopes scopes;
    private final SourceGroupLookup sourceGroups;

    /** Creates the resolver. */
    public PlaceBriefingScopeResolver(PlaceService places, SavedViewService views, RecordScopes scopes,
            SourceGroupLookup sourceGroups) {
        this.places = places;
        this.views = views;
        this.scopes = scopes;
        this.sourceGroups = sourceGroups;
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
        if (!s.sourceGroupIds().isEmpty()) {
            LinkedHashSet<String> members = new LinkedHashSet<>();
            boolean resolved = true;
            for (String groupId : s.sourceGroupIds()) {
                Optional<List<String>> group = sourceGroups.memberSources(groupId);
                if (group.isEmpty()) {
                    resolved = false;
                    break;
                }
                members.addAll(group.get());
            }
            if (!resolved || members.isEmpty()) {
                // Fail closed: a dangling or empty group matches nothing, it never widens the scope to every source.
                filter = filter == null ? nothing("group:deleted") : RecordFilters.and(filter, nothing("group:deleted"));
            } else {
                LinkedHashSet<String> union = new LinkedHashSet<>(sources);
                union.addAll(members);
                sources = new ArrayList<>(union);
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
