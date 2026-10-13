package ai.foresignal.source.kit.feed;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The reviewed {@link FeedItemMapper}s a feed spec may name (prompt 073). Empty until a source needs one (prompt 075
 * adds InciWeb's); the list is code, never configuration.
 */
public final class FeedItemMappers {

    /** Every reviewed mapper. */
    public static final List<FeedItemMapper> REVIEWED = List.of();

    private static final Map<String, FeedItemMapper> BY_NAME = REVIEWED.stream()
            .collect(Collectors.toUnmodifiableMap(FeedItemMapper::name, Function.identity()));

    private FeedItemMappers() {}

    /** The mapper named {@code name}, or null. */
    public static FeedItemMapper of(String name) {
        return name == null ? null : BY_NAME.get(name);
    }
}
