package ai.foresignal.source.kit.feature;

import java.util.Iterator;
import java.util.List;
import tools.jackson.databind.JsonNode;

/**
 * What one feature-kit client fetch produced (prompt 073): GeoJSON Feature trees, bounded by the spec's caps.
 *
 * @param features the features, at most {@code maxFeatures}
 * @param complete true when the whole layer arrived: the last page said so, and neither {@code maxPages} nor
 *     {@code maxFeatures} cut it short. Only a complete fetch may tombstone absent features
 * @param unchanged true when the layer's {@code lastEditDate} is the one of the last complete fetch, so nothing was
 *     queried
 * @param pages the pages requested
 * @param stopReason why paging stopped: {@code last-page}, {@code max-pages}, {@code max-features},
 *     {@code empty-page}, {@code no-pagination} or {@code unchanged}
 */
public record FeatureFetch(List<JsonNode> features, boolean complete, boolean unchanged, int pages, String stopReason)
        implements Iterable<JsonNode> {

    /** Copies the list. */
    public FeatureFetch {
        features = features == null ? List.of() : List.copyOf(features);
    }

    /** The layer did not change since the last complete fetch. */
    public static FeatureFetch unchangedLayer() {
        return new FeatureFetch(List.of(), false, true, 0, "unchanged");
    }

    @Override
    public Iterator<JsonNode> iterator() {
        return features.iterator();
    }

    /** The number of features. */
    public int size() {
        return features.size();
    }
}
