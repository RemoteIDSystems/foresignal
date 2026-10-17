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
 * @param deferWhileActiveSeconds presence deferral in seconds (prompt 033), 0 for none
 * @param createdAt when created
 * @param updatedAt when last changed
 * @param acceptExercise whether the channel receives exercise alerts (prompt 115)
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
        int deferWhileActiveSeconds,
        Instant createdAt,
        Instant updatedAt,
        boolean acceptExercise) {

    /** A channel that does not take exercise alerts (the shape before prompt 115). */
    public NotificationChannel(UUID id, String name, String type, Map<String, Object> config, List<String> secretFields,
            List<String> events, List<UUID> ruleIds, String minSeverity, String titleTemplate, String bodyTemplate,
            int waitForEnrichmentSeconds, boolean enabled, String timezone, QuietHours quietHours,
            int groupWindowSeconds, int backlogCap, boolean editInPlace, int deferWhileActiveSeconds,
            Instant createdAt, Instant updatedAt) {
        this(id, name, type, config, secretFields, events, ruleIds, minSeverity, titleTemplate, bodyTemplate,
                waitForEnrichmentSeconds, enabled, timezone, quietHours, groupWindowSeconds, backlogCap, editInPlace,
                deferWhileActiveSeconds, createdAt, updatedAt, false);
    }

    /** The view of {@code e} with the given set secret names. */
    public static NotificationChannel of(NotificationChannelEntity e, List<String> secretFields) {
        return new NotificationChannel(e.getId(), e.getName(), e.getType(), e.getConfig(),
                secretFields == null ? List.of() : List.copyOf(secretFields), List.copyOf(e.getEvents()),
                e.getRuleIds() == null ? null : List.copyOf(e.getRuleIds()), e.getMinSeverity(), e.getTitleTemplate(),
                e.getBodyTemplate(), e.getWaitForEnrichmentSeconds(), e.isEnabled(), e.getTimezone(),
                QuietHours.fromMap(e.getQuietHours()), e.getGroupWindowSeconds(), e.getBacklogCap(), e.isEditInPlace(),
                e.getDeferWhileActiveSeconds(), e.getCreatedAt(), e.getUpdatedAt(), e.isAcceptExercise());
    }

    /**
     * True when the channel takes {@code message} by its own settings: enabled, subscribed to the
     * event, the severity at or above its minimum (and, for a {@code meshtastic} or {@code meshcore} channel, at or
     * above its {@code config.minSeverity}, prompt 084), and for alerts the rule in its rule filter.
     */
    public boolean accepts(NotificationMessage message) {
        if (!enabled || !events.contains(message.event())) {
            return false;
        }
        if (Severity.fromWire(message.severity()).compareTo(Severity.fromWire(minSeverity)) < 0) {
            return false;
        }
        String meshMinimum = ai.foresignal.notify.mesh.MeshChannels.minSeverity(type, config);
        if (meshMinimum != null && Severity.fromWire(message.severity()).compareTo(Severity.fromWire(meshMinimum)) < 0) {
            // Prompt 084: a mesh channel's own config.minSeverity (default warning) narrows it further.
            return false;
        }
        if (NotificationMessage.ALERT.equals(message.event()) && ruleIds != null) {
            return message.ruleId() != null && ruleIds.contains(message.ruleId());
        }
        return true;
    }
}
