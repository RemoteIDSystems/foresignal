package ai.foresignal.notify.push;

import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.MarkdownRenderer;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.Truncator;
import ai.foresignal.settings.ClientLinks;
import ai.foresignal.settings.ServerSettings;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Builds the {@link PushMessage} of a notification (prompt 031).
 *
 * <ul>
 *   <li>{@code id} is the persisted notification id (018), the alert id when there is none.
 *   <li>{@code body} is plain text: an alert's body cut to 1000 characters, a briefing's first 200
 *       characters, and for a system event only its code ({@code system.source_failing}).
 *   <li>Links: {@code api} is the message URL; the 023 {@code clientLinks} template of the entity
 *       ({@code alert} for alerts, {@code briefingRun} for briefings) fills {@code app} when it uses
 *       the {@code foresignal://} scheme and {@code web} when it is an {@code https://} link.
 *   <li>{@code tag} is {@code foresignal-alert-<alertId>} for alerts (so a resolve replaces the fired
 *       notification in the OS) and {@code foresignal-<event>-<id>} otherwise.
 *   <li>{@code topic} is {@link PushTopic} of {@code alert:<ruleId>:<source>:<externalId>},
 *       {@code briefing:<briefingId>} or {@code system:<code>}.
 * </ul>
 */
@Component
public class PushContentFactory {

    /** The longest alert body. */
    public static final int MAX_BODY = 1000;

    /** The length of a briefing excerpt. */
    public static final int BRIEFING_EXCERPT = 200;

    private final ChannelService channels;
    private final Clock clock;

    /** Creates the factory. */
    public PushContentFactory(ChannelService channels, Clock clock) {
        this.channels = channels;
        this.clock = clock;
    }

    /** The push content of {@code message} (as rendered for the Push channel). */
    public PushMessage of(NotificationMessage message) {
        ServerSettings server = channels.serverSettings();
        Map<String, Object> data = message.data() == null ? Map.of() : message.data();
        Long id = message.id() != null ? message.id() : message.alertId();
        String plain = message.bodyMarkdown() == null ? null
                : MarkdownRenderer.render(message.bodyMarkdown(), MarkdownRenderer.Format.PLAIN).strip();
        String body;
        String template = null;
        Map<String, String> values = Map.of();
        String topic;
        String tag;
        switch (message.event()) {
            case NotificationMessage.ALERT -> {
                body = cut(plain, MAX_BODY);
                if (message.alertId() != null) {
                    template = server.effectiveClientLinks().alert();
                    values = Map.of("id", Long.toString(message.alertId()));
                }
                topic = PushTopic.alert(message.ruleId() == null ? "" : message.ruleId().toString(),
                        text(data.get("sourceId")), text(data.get("externalId")));
                tag = message.alertId() != null ? "foresignal-alert-" + message.alertId() : "foresignal-alert-" + id;
            }
            case NotificationMessage.BRIEFING -> {
                body = cut(plain, BRIEFING_EXCERPT);
                String briefingId = text(data.get("briefingId"));
                String runId = message.briefingRunId() != null ? message.briefingRunId().toString() : text(data.get("id"));
                if (!briefingId.isEmpty() && !runId.isEmpty()) {
                    template = server.effectiveClientLinks().briefingRun();
                    values = Map.of("briefingId", briefingId, "id", runId);
                }
                topic = PushTopic.briefing(briefingId.isEmpty() ? runId : briefingId);
                tag = "foresignal-briefing-" + id;
            }
            default -> {
                body = message.subtype();
                topic = PushTopic.system(message.subtype());
                tag = "foresignal-" + message.event() + "-" + id;
            }
        }
        String app = null;
        String web = null;
        if (template != null) {
            String filled = ClientLinks.fill(template, values);
            String lower = filled.toLowerCase(Locale.ROOT);
            if (lower.startsWith("foresignal://")) {
                app = filled;
            } else if (lower.startsWith("https://")) {
                web = filled;
            }
        }
        Instant created = message.createdAt() == null ? clock.instant() : message.createdAt();
        return new PushMessage(message.event(), id, message.alertId(), message.ruleId(), message.severity(),
                message.title(), message.shortTitle(), body, message.url(), app, web, message.tags(), tag, topic, false,
                created.toEpochMilli(), null, null);
    }

    private static String cut(String text, int max) {
        return text == null ? null : Truncator.truncate(text, max, Truncator.Unit.CHARS, Truncator.Mode.PLAIN, null);
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
