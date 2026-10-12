package ai.foresignal.store.query;

import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import java.util.List;
import java.util.Set;

/**
 * A change-feed page: every version with {@code seq > after}, in seq order.
 *
 * @param after the last seq the client has
 * @param sources source filter; empty for all
 * @param kinds kind filter; empty for all
 * @param externalIds external id filter; empty for all
 * @param bbox spatial filter on the version's geometry, or null
 * @param includePayload load payloads (kinds in {@code inlinePayloadKinds} always carry theirs)
 * @param limit the most changes returned
 * @param fields the projection
 * @param geometry geometry output options
 * @param encoding geometry text encoding
 * @param excludedSources sources the caller may not see
 */
public record ChangesQuery(
        long after,
        List<String> sources,
        List<String> kinds,
        List<String> externalIds,
        BboxFilter bbox,
        boolean includePayload,
        int limit,
        FieldSelection fields,
        GeometryOptions geometry,
        GeometryEncoding encoding,
        Set<String> excludedSources) {

    /** Normalizes nulls. */
    public ChangesQuery {
        sources = sources == null ? List.of() : List.copyOf(sources);
        kinds = kinds == null ? List.of() : List.copyOf(kinds);
        externalIds = externalIds == null ? List.of() : List.copyOf(externalIds);
        fields = fields == null ? FieldSelection.ALL : fields;
        geometry = geometry == null ? GeometryOptions.DEFAULT : geometry;
        encoding = encoding == null ? GeometryEncoding.GEOJSON : encoding;
        excludedSources = excludedSources == null ? Set.of() : Set.copyOf(excludedSources);
    }
}
