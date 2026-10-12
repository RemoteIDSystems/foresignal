package ai.foresignal.notify.inbox;

/**
 * A device's read state changed (prompt 033): its open inbox streams get a {@code badge} event so
 * other windows clear their badge.
 *
 * @param deviceKey the device
 * @param badge the new counts
 */
public record InboxBadgeChangedEvent(String deviceKey, InboxService.Badge badge) {}
