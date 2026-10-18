package ai.foresignal.notify;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A validated {@code ChannelInput} body (prompt 018).
 *
 * @param name the name
 * @param type the type id
 * @param config the non-secret settings
 * @param secrets the secret settings, or null when the body omitted {@code secrets} (keep existing)
 * @param events the subscribed events
 * @param ruleIds the rule filter, or null for every rule
 * @param minSeverity the minimum severity
 * @param titleTemplate the title template, or null
 * @param bodyTemplate the body template, or null
 * @param waitForEnrichmentSeconds the enrichment wait (0–60)
 * @param enabled whether the channel receives messages
 * @param timezone the IANA timezone, or null
 * @param quietHours the quiet hours, or null
 * @param groupWindowSeconds the grouping window
 * @param backlogCap the backlog cap
 * @param editInPlace the edit-in-place switch
 * @param deferWhileActiveSeconds presence deferral while a desktop is active (0–600, prompt 033)
 * @param acceptExercise whether the channel receives exercise alerts (prompt 115)
 * @param language the BCP 47 language of the channel's notifications (prompt 141), or null for the default
 */
public record ChannelInput(
        String name,
        String type,
        Map<String, Object> config,
        Map<String, String> secrets,
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
        boolean acceptExercise,
        String language) {

    /** An input without a language (the shape before prompt 141). */
    public ChannelInput(String name, String type, Map<String, Object> config, Map<String, String> secrets,
            List<String> events, List<UUID> ruleIds, String minSeverity, String titleTemplate, String bodyTemplate,
            int waitForEnrichmentSeconds, boolean enabled, String timezone, QuietHours quietHours,
            int groupWindowSeconds, int backlogCap, boolean editInPlace, int deferWhileActiveSeconds,
            boolean acceptExercise) {
        this(name, type, config, secrets, events, ruleIds, minSeverity, titleTemplate, bodyTemplate,
                waitForEnrichmentSeconds, enabled, timezone, quietHours, groupWindowSeconds, backlogCap, editInPlace,
                deferWhileActiveSeconds, acceptExercise, null);
    }

    /** An input without the exercise opt-in (the shape before prompt 115). */
    public ChannelInput(String name, String type, Map<String, Object> config, Map<String, String> secrets,
            List<String> events, List<UUID> ruleIds, String minSeverity, String titleTemplate, String bodyTemplate,
            int waitForEnrichmentSeconds, boolean enabled, String timezone, QuietHours quietHours,
            int groupWindowSeconds, int backlogCap, boolean editInPlace, int deferWhileActiveSeconds) {
        this(name, type, config, secrets, events, ruleIds, minSeverity, titleTemplate, bodyTemplate,
                waitForEnrichmentSeconds, enabled, timezone, quietHours, groupWindowSeconds, backlogCap, editInPlace,
                deferWhileActiveSeconds, false);
    }
}
