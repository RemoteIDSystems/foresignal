package ai.foresignal.stream.sse;

import ai.foresignal.notify.NotificationMessage;
import java.util.Set;

/**
 * The filter of the {@code notifications} and {@code system} topics (prompt 018). The spec defines
 * no filter fields yet; anonymous callers never see notifications about private sources.
 *
 * @param excludedSources sources whose notifications are hidden from this caller
 */
public record NotificationFilter(Set<String> excludedSources) {

    /** Copies. */
    public NotificationFilter {
        excludedSources = excludedSources == null ? Set.of() : Set.copyOf(excludedSources);
    }

    /** True when the caller may see {@code message}. */
    public boolean matches(NotificationMessage message) {
        if (excludedSources.isEmpty() || message.data() == null) {
            return true;
        }
        Object source = message.data().get("sourceId");
        return source == null || !excludedSources.contains(String.valueOf(source));
    }
}
