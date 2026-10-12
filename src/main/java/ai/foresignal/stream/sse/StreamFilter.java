package ai.foresignal.stream.sse;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A client's whole filter, immutable: the subscribed topics, each topic's own filter object (of the
 * type its {@link EventTopic#filterType()} names) and a version that starts at 1 and grows with
 * every applied PATCH.
 *
 * <p>An {@link SseClient} holds it in an {@code AtomicReference} and swaps it whole.
 *
 * @param topics the subscribed topics, in subscription order
 * @param filters the filter of each subscribed topic
 * @param version the filter version ({@code filterVersion} on the wire)
 */
public record StreamFilter(List<String> topics, Map<String, Object> filters, int version) {

    /** Copies and checks that every topic has a filter. */
    public StreamFilter {
        topics = List.copyOf(topics);
        filters = Map.copyOf(filters);
        for (String topic : topics) {
            Objects.requireNonNull(filters.get(topic), () -> "No filter for topic " + topic);
        }
        if (version < 1) {
            throw new IllegalArgumentException("filter version must be at least 1, got " + version);
        }
    }

    /** A version-1 filter subscribed to {@code changes} only. */
    public static StreamFilter changesOnly(ChangeFilter changes) {
        return new StreamFilter(List.of(ChangesTopic.NAME), Map.of(ChangesTopic.NAME, changes), 1);
    }

    /** True when {@code topic} is subscribed. */
    public boolean subscribes(String topic) {
        return topics.contains(topic);
    }

    /** The filter of {@code topic}, or null when it is not subscribed. */
    public Object filterFor(String topic) {
        return subscribes(topic) ? filters.get(topic) : null;
    }

    /** The {@code changes} filter, or null when {@code changes} is not subscribed. */
    public ChangeFilter changes() {
        return filterFor(ChangesTopic.NAME) instanceof ChangeFilter f ? f : null;
    }

    /** The next version with {@code newTopics} and {@code newFilters}. */
    public StreamFilter next(List<String> newTopics, Map<String, Object> newFilters) {
        Map<String, Object> kept = new LinkedHashMap<>();
        for (String topic : newTopics) {
            kept.put(topic, newFilters.get(topic));
        }
        return new StreamFilter(newTopics, kept, version + 1);
    }
}
