package ai.foresignal.store.tiles;

import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.SqlParams;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A resolved vector tile layer: a source id or a kind, the sources it covers for this caller, and
 * what its features carry.
 *
 * @param id the layer name as requested
 * @param type whether {@code id} is a source id or a kind
 * @param name a display name: the source's name, or the kind
 * @param description a one-line description
 * @param sources the sources whose records the layer shows, sorted by id; private sources are
 *     already removed for callers that may not see them
 * @param tileProperties the union of the sources' valid {@code tileProperties}, in first-seen order,
 *     without the {@link #BASE_PROPERTIES}
 * @param aggBelowZoom the lowest non-null {@code aggBelowZoom} of the sources; null when none
 *     declares one (never aggregate under {@code agg=auto})
 * @param rankFields source id to its numeric {@code rankField}; empty unless every source declares
 *     one, which is when aggregate cells carry {@code maxRank}
 */
public record TileLayer(
        String id,
        TileLayerType type,
        String name,
        String description,
        List<SourceDescriptor> sources,
        List<String> tileProperties,
        Integer aggBelowZoom,
        Map<String, String> rankFields) {

    /** Properties every raw feature carries, in this order. */
    public static final List<String> BASE_PROPERTIES =
            List.of("source", "externalId", "kind", "title", "observedAt", "version", "live");

    /** Copies the collections. */
    public TileLayer {
        sources = List.copyOf(sources);
        tileProperties = List.copyOf(tileProperties);
        rankFields = Map.copyOf(rankFields);
    }

    /** The base properties followed by the tile properties: {@code foresignal:properties}. */
    public List<String> properties() {
        List<String> all = new ArrayList<>(BASE_PROPERTIES);
        all.addAll(tileProperties);
        return List.copyOf(all);
    }

    /** The ids of {@link #sources}. */
    public List<String> sourceIds() {
        return sources.stream().map(SourceDescriptor::id).toList();
    }

    /** True when aggregate cells carry {@code maxRank}. */
    public boolean rankable() {
        return !rankFields.isEmpty();
    }

    /** The distinct non-blank descriptor attributions, joined with {@code " · "}; null when none. */
    public String attribution() {
        Set<String> parts = new LinkedHashSet<>();
        for (SourceDescriptor source : sources) {
            if (source.attribution() != null && !source.attribution().isBlank()) {
                parts.add(source.attribution().strip());
            }
        }
        return parts.isEmpty() ? null : String.join(" · ", parts);
    }

    /**
     * The layer filter as one SQL fragment over {@code record_head h}, with every value bound in
     * {@code params}. A source layer is {@code h.source_id = :id}; a kind layer is
     * {@code h.kind = :kind AND h.source_id = ANY(:visibleSources)}, so records of sources the
     * caller may not see never reach a feature or a count. Prompt 035 (restricted tokens) ANDs its
     * own fragment next to this one.
     */
    public String filterSql(SqlParams params) {
        if (type == TileLayerType.SOURCE) {
            return "h.source_id = " + params.text(id);
        }
        return "(h.kind = " + params.text(id) + " AND h.source_id = ANY(" + params.textArray(sourceIds()) + "))";
    }
}
