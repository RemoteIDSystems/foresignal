package ai.foresignal.ask;

import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.BboxParser;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The scope of a question ({@code AskScope}): default filters injected into every read tool call,
 * which the model may narrow but never widen ({@link ScopeInjector}).
 *
 * @param sources source ids; empty for every source the caller may see
 * @param kinds record kinds; empty for every kind
 * @param bbox the area as {@code minLon,minLat,maxLon,maxLat}, or null
 * @param since the earliest time, or null
 * @param until the latest time (exclusive), or null
 * @param placeIds places (prompt 036), resolved to areas by an {@link AskScopeResolver}
 * @param viewId a saved view (prompt 036), resolved by an {@link AskScopeResolver}, or null
 */
public record AskScope(List<String> sources, List<String> kinds, String bbox, Instant since, Instant until,
        List<String> placeIds, String viewId) {

    /** No scope at all. */
    public static final AskScope NONE = new AskScope(List.of(), List.of(), null, null, null, List.of(), null);

    /** Copies the lists. */
    public AskScope {
        sources = sources == null ? List.of() : List.copyOf(sources);
        kinds = kinds == null ? List.of() : List.copyOf(kinds);
        placeIds = placeIds == null ? List.of() : List.copyOf(placeIds);
        bbox = bbox == null || bbox.isBlank() ? null : bbox.strip();
    }

    /** The parsed area, or null. */
    public BboxFilter bboxFilter() {
        return BboxParser.parse(bbox);
    }

    /** True when nothing is restricted. */
    public boolean isEmpty() {
        return sources.isEmpty() && kinds.isEmpty() && bbox == null && since == null && until == null
                && placeIds.isEmpty() && viewId == null;
    }

    /** The scope as JSON members, for the prompt and the stored message; empty members left out. */
    public Map<String, Object> describe() {
        Map<String, Object> out = new LinkedHashMap<>();
        if (!sources.isEmpty()) {
            out.put("sources", sources);
        }
        if (!kinds.isEmpty()) {
            out.put("kinds", kinds);
        }
        if (bbox != null) {
            out.put("bbox", bbox);
        }
        if (since != null) {
            out.put("since", since.toString());
        }
        if (until != null) {
            out.put("until", until.toString());
        }
        return out;
    }
}
