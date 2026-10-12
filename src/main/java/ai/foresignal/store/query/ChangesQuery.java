package ai.foresignal.store.query;

import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.api.support.NearFilter;
import java.util.List;
import java.util.Set;
import ai.foresignal.security.restrict.ReadRestriction;

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
 * @param near radius filter on the version's geometry, or null (prompt 015)
 * @param prevBoxes also match the spatial filter on the previous version's geometry
 *     ({@code LEFT JOIN record_version p ON p.seq = v.prev_seq}) and select its box, so the SSE
 *     replay can flag entities that left the area ({@code leftFilter}, prompt 015)
 * @param restriction the caller's read restriction (prompt 035; never null)
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
        Set<String> excludedSources,
        NearFilter near,
        boolean prevBoxes,
        ReadRestriction restriction) {

    /** An unrestricted query with radius filter and previous boxes (the 015 shape). */
    public ChangesQuery(long after, List<String> sources, List<String> kinds, List<String> externalIds,
            BboxFilter bbox, boolean includePayload, int limit, FieldSelection fields, GeometryOptions geometry,
            GeometryEncoding encoding, Set<String> excludedSources, NearFilter near, boolean prevBoxes) {
        this(after, sources, kinds, externalIds, bbox, includePayload, limit, fields, geometry, encoding,
                excludedSources, near, prevBoxes, ReadRestriction.NONE);
    }

    /** The change-feed query of prompt 014: no radius filter, no previous boxes. */
    public ChangesQuery(
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
        this(after, sources, kinds, externalIds, bbox, includePayload, limit, fields, geometry, encoding,
                excludedSources, null, false);
    }

    /** Normalizes nulls. */
    public ChangesQuery {
        sources = sources == null ? List.of() : List.copyOf(sources);
        kinds = kinds == null ? List.of() : List.copyOf(kinds);
        externalIds = externalIds == null ? List.of() : List.copyOf(externalIds);
        fields = fields == null ? FieldSelection.ALL : fields;
        geometry = geometry == null ? GeometryOptions.DEFAULT : geometry;
        encoding = encoding == null ? GeometryEncoding.GEOJSON : encoding;
        excludedSources = excludedSources == null ? Set.of() : Set.copyOf(excludedSources);
        restriction = restriction == null ? ReadRestriction.NONE : restriction;
    }

    /** This query under {@code r}. */
    public ChangesQuery withRestriction(ReadRestriction r) {
        return new ChangesQuery(after, sources, kinds, externalIds, bbox, includePayload, limit, fields, geometry,
                encoding, excludedSources, near, prevBoxes, r);
    }
}
