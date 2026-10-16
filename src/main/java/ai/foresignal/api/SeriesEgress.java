package ai.foresignal.api;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.visibility.Viewer;
import ai.foresignal.source.SourceRegistry;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.stereotype.Component;

/**
 * The egress decision of the series reads (prompt 093, EGR-2): {@code docs/Conventions.md} serves the raw values
 * of a {@code LOCAL_ONLY} source "only to callers allowed local egress", and {@code docs/OpenAPI.json}'s
 * {@code listSeries} says the same. Whether a caller is allowed is {@link EgressPolicy}'s decision, never this
 * class's: every registered source is put to
 * {@link EgressPolicy#decide(EgressPath, String, ai.foresignal.provenance.LicenceTerms, java.util.Map,
 * EgressPolicy.EgressContext)} on the read's path ({@link EgressPath#API_READ} for {@code /v1/series},
 * {@link EgressPath#MCP} for {@code get_series}) for the caller, and the sources it refuses join the caller's
 * excluded sources. The records reads serve the same caller on the same paths, so the two agree.
 */
@Component
public class SeriesEgress {

    private final EgressPolicy egress;
    private final SourceRegistry registry;

    /** Creates the decision. */
    public SeriesEgress(EgressPolicy egress, SourceRegistry registry) {
        this.egress = egress;
        this.registry = registry;
    }

    /**
     * The caller's series visibility: its excluded sources plus every registered source {@link EgressPolicy}
     * refuses to send it on {@code path}, under its restriction.
     *
     * @param caller the read caller
     * @param excluded the sources the caller may not read already (private and restriction filters)
     * @param restriction the caller's read restriction
     * @param path {@link EgressPath#API_READ} or {@link EgressPath#MCP}
     * @return the visibility to pass to {@link SeriesQueryService}
     */
    public SeriesQueryService.Visibility visibility(ReadCaller caller, Set<String> excluded,
            ReadRestriction restriction, EgressPath path) {
        return new SeriesQueryService.Visibility(excluded(caller, excluded, path), restriction);
    }

    /**
     * {@code excluded} plus every registered source {@link EgressPolicy} refuses to send {@code caller} on
     * {@code path} (a {@code LOCAL_ONLY} source for a caller not allowed local egress, among the policy's other
     * rules).
     *
     * @param caller the read caller, or null for an anonymous one
     * @param excluded the sources already excluded
     * @param path the read path
     * @return the combined set
     */
    public Set<String> excluded(ReadCaller caller, Set<String> excluded, EgressPath path) {
        Viewer viewer = caller == null ? Viewer.anonymous()
                : new Viewer(caller.scope(), caller.privateAccess(), TokenRestrictions.NONE);
        EgressPolicy.EgressContext context = EgressPolicy.EgressContext.of(viewer);
        Set<String> out = new TreeSet<>(excluded == null ? Set.of() : excluded);
        for (String sourceId : registry.ids()) {
            if (!out.contains(sourceId) && !egress.decide(path, sourceId, null, null, context).allowed()) {
                out.add(sourceId);
            }
        }
        return out;
    }
}
