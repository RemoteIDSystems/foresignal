package ai.foresignal.stream.sse;

import ai.foresignal.api.support.QueryParams;
import java.util.List;
import java.util.Map;

/**
 * One topic's query parameters on connect, with the topic prefix removed: on
 * {@code /v1/events/stream} {@code changes.bbox} is {@code bbox} here; on
 * {@code /v1/changes/stream} the parameters arrive unprefixed. {@link #field} gives the name as the
 * client sent it, for error messages.
 *
 * @param prefix the prefix to put back in error fields ({@code "changes."}, or {@code ""})
 * @param values the parameter values by unprefixed name
 */
public record TopicParams(String prefix, Map<String, List<String>> values) {

    /** Copies the map. */
    public TopicParams {
        prefix = prefix == null ? "" : prefix;
        values = Map.copyOf(values);
    }

    /** Every value of {@code name}, comma-split, trimmed and deduplicated (at most 100). */
    public List<String> multi(String name) {
        return QueryParams.multi(field(name), values.get(name));
    }

    /** The single value of {@code name}, or null; the last one wins when repeated. */
    public String single(String name) {
        List<String> list = values.get(name);
        return list == null || list.isEmpty() ? null : list.getLast();
    }

    /** {@code name} as the client sent it. */
    public String field(String name) {
        return prefix + name;
    }
}
