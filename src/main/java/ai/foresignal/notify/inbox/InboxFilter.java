package ai.foresignal.notify.inbox;

import java.util.Objects;
import java.util.Set;

/**
 * The filter of one inbox stream (prompt 033): the device whose read state the items carry and the
 * sources the caller may not see.
 *
 * @param deviceKey the device
 * @param excludedSources sources whose notifications are hidden
 */
public record InboxFilter(String deviceKey, Set<String> excludedSources) {

    /** Requires the device; copies the set. */
    public InboxFilter {
        Objects.requireNonNull(deviceKey, "deviceKey");
        excludedSources = excludedSources == null ? Set.of() : Set.copyOf(excludedSources);
    }
}
