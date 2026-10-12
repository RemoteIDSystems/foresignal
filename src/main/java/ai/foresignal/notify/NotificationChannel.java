package ai.foresignal.notify;

import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.trigger.Severity;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A channel as routing and the API see it: everything but the secret values (prompt 018).
 *
 * <p>{@code NotificationService.publish} filters with a {@code Predicate<NotificationChannel>}, so
 * briefings (029) and system events can narrow the channels further.
 *
 * @param id the id
 * @param name the name
 * @param type the type id
 * @param config the non-secret settings
 * @param secretFields the names of the secret fields that are set
 * @param events the subscribed events
 * @param ruleIds the rule filter, or null for every rule
 * @param minSeverity the minimum severity wire value
 * @param titleTemplate the title template, or null
 * @param bodyTemplate the body template, or null
 * @param waitForEnrichmentSeconds the enrichment wait
 * @param enabled whether it receives messages
 * @param timezone the configured IANA timezone, or null
 * @param quietHours the quiet hours, or null
 * @param groupWindowSeconds the grouping window, 0 for none
 * @param backlogCap the backlog cap
 * @param editInPlace whether updates edit the original where possible
 * @param createdAt when created
 * @param updatedAt when last changed
 */
public record NotificationChannel(
        UUID id,
        String name,
        String type,
        Map<String, Object> config,
        List<String> secretFields,
        List<String> events,
        List<UUID> ruleIds,
        String minSeverity,
        String titleTemplate,
        String bodyTemplate,
        int waitForEnrichmentSeconds,
        boolean enabled,
        String timezone,
        QuietHours quietHours,
        int groupWindowSeconds,
        int backlogCap,
        boolean editInPlace,
        Instant createdAt,
        Instant updatedAt) {

    /** The view of {@code e} with the given set secret names. */
    public static NotificationChannel of(NotificationChannelEntity e, List<String> secretFields) {
        return new NotificationChannel(e.getId(), e.getName(), e.getType(), e.getConfig(),
                secretFields == null ? List.of() : List.copyOf(secretFields), List.copyOf(e.getEvents()),
                e.getRuleIds() == null ? null : List.copyOf(e.getRuleIds()), e.getMinSeverity(), e.getTitleTemplate(),
                e.getBodyTemplate(), e.getWaitForEnrichmentSeconds(), e.isEnabled(), e.getTimezone(),
                QuietHours.fromMap(e.getQuietHours()), e.getGroupWindowSeconds(), e.getBacklogCap(), e.isEditInPlace(),
                e.getCreatedAt(), e.getUpdatedAt());
    }

    /**
     * True when the channel takes {@code message} by its own settings: enabled, subscribed to the
     * event, the severity at or above its minimum, and for alerts the rule in its rule filter.
     */
    public boolean accepts(NotificationMessage message) {
        if (!enabled || !events.contains(message.event())) {
            return false;
        }
        if (Severity.fromWire(message.severity()).compareTo(Severity.fromWire(minSeverity)) < 0) {
            return false;
        }
        if (NotificationMessage.ALERT.equals(message.event()) && ruleIds != null) {
            return message.ruleId() != null && ruleIds.contains(message.ruleId());
        }
        return true;
    }
}
