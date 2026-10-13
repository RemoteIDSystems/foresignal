package ai.foresignal.source.kit.feed;

import ai.foresignal.source.adapters.fire.InciwebItemMapper;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The reviewed {@link FeedItemMapper}s a feed spec may name (prompt 073); the list is code, never configuration.
 *
 * <ul>
 *   <li>{@code inciweb} ({@link InciwebItemMapper}, prompt 075): InciWeb's structured item descriptions.
 * </ul>
 */
public final class FeedItemMappers {

    /** Every reviewed mapper. */
    public static final List<FeedItemMapper> REVIEWED = List.of(new InciwebItemMapper());

    private static final Map<String, FeedItemMapper> BY_NAME = REVIEWED.stream()
            .collect(Collectors.toUnmodifiableMap(FeedItemMapper::name, Function.identity()));

    private FeedItemMappers() {}

    /** The mapper named {@code name}, or null. */
    public static FeedItemMapper of(String name) {
        return name == null ? null : BY_NAME.get(name);
    }
}
