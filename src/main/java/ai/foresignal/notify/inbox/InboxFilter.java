package ai.foresignal.notify.inbox;

import java.util.Objects;
import java.util.Set;

/**
 * The filter of one inbox stream (prompt 033): the device whose read state the items carry and the
 * sources the caller may not see.
 *
 * @param deviceKey the device
 * @param excludedSources sources whose notifications are hidden
 * @param restriction the caller's read restriction (prompt 035; never null)
 */
public record InboxFilter(String deviceKey, Set<String> excludedSources,
        ai.foresignal.security.restrict.ReadRestriction restriction) {

    /** Requires the device; copies the set. */
    public InboxFilter {
        Objects.requireNonNull(deviceKey, "deviceKey");
        excludedSources = excludedSources == null ? Set.of() : Set.copyOf(excludedSources);
        restriction = restriction == null ? ai.foresignal.security.restrict.ReadRestriction.NONE : restriction;
    }

    /** An unrestricted filter (the shape before prompt 035). */
    public InboxFilter(String deviceKey, Set<String> excludedSources) {
        this(deviceKey, excludedSources, null);
    }
}
