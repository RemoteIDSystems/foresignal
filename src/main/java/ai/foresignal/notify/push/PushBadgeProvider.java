package ai.foresignal.notify.push;

import java.util.Optional;

/**
 * Supplies the app badge of one push target at send time (prompt 033): the unread count of the
 * device behind a native target, so {@code aps.badge} matches {@code GET /v1/notifications/badge} on
 * that device. Declared as a bean by the inbox; the dispatch worker asks every provider.
 */
public interface PushBadgeProvider {

    /** The badge of {@code target}, or empty when this provider does not know it. */
    Optional<Integer> badgeFor(PushTarget target);
}
