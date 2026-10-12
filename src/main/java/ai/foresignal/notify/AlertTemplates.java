package ai.foresignal.notify;

import ai.foresignal.trigger.MessageTemplate;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * Title and body of an alert notification from a channel's templates or the defaults (prompt 018).
 *
 * <p>Templates use {@link MessageTemplate}'s placeholders plus {@code {{severity}}},
 * {@code {{summary}}} (the enrichment summary, empty until prompt 030), {@code {{url}}},
 * {@code {{ruleName}}} and {@code {{message}}} (the alert's rendered message), and the
 * {@code |local} and {@code |ago} filters in the channel's timezone. Values substituted into the
 * body are markdown-escaped, so a payload cannot inject formatting. The context comes from the
 * notification's {@code data} (the alert JSON the router stores), so a channel's templates can be
 * applied at send time.
 */
public final class AlertTemplates {

    /** The default title. */
    public static final String DEFAULT_TITLE = "[{{severity}}] {{name}}";

    /**
     * The default body: the alert message, a record summary and the enrichment summary. The link to
     * the alert travels as the message's {@code url} and {@code links}, which every type renders.
     */
    public static final String DEFAULT_BODY =
            "{{message}}\n\n{{source}}/{{externalId}} · {{kind}} · observed {{observedAt|local}}\n\n{{summary}}";

    private AlertTemplates() {}

    /**
     * The rendered title and body.
     *
     * @param title the title
     * @param body the markdown body
     */
    public record Text(String title, String body) {}

    /**
     * Renders the templates (defaults when null or blank) for an alert.
     *
     * @param titleTemplate the channel's title template, or null
     * @param bodyTemplate the channel's body template, or null
     * @param data the alert JSON of the notification
     * @param url the alert link
     * @param zone the channel's timezone
     * @param now the reference time of {@code |ago}
     */
    public static Text render(String titleTemplate, String bodyTemplate, Map<String, Object> data, String url,
            ZoneId zone, Instant now) {
        Map<String, Object> d = data == null ? Map.of() : data;
        MessageTemplate.Context context = new MessageTemplate.Context(
                text(d.get("ruleName")), text(d.get("sourceId")), text(d.get("externalId")), text(d.get("kind")),
                text(d.get("recordTitle")), text(d.get("severity")), instant(d.get("observedAt")),
                instant(d.get("eventTime")), instant(d.get("validFrom")), payload(d));
        Map<String, String> vars = new HashMap<>();
        vars.put("severity", text(d.get("severity")));
        vars.put("summary", text(d.get("summary")));
        vars.put("url", url == null ? "" : url);
        vars.put("ruleName", text(d.get("ruleName")));
        vars.put("message", text(d.get("message")));
        String title = MessageTemplate.render(blankToNull(titleTemplate, DEFAULT_TITLE), context,
                new MessageTemplate.Extras(zone, now, vars, null));
        String body = MessageTemplate.render(blankToNull(bodyTemplate, DEFAULT_BODY), context,
                new MessageTemplate.Extras(zone, now, vars, MarkdownRenderer::escapeMarkdown));
        return new Text(title.replace('\n', ' ').strip(), tidy(body));
    }

    /** Collapses runs of blank lines and trims. */
    static String tidy(String body) {
        return body.replaceAll("[ \\t]+\\n", "\n").replaceAll("\\n{3,}", "\n\n").strip();
    }

    private static String blankToNull(String template, String fallback) {
        return template == null || template.isBlank() ? fallback : template;
    }

    private static String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Instant instant(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(String.valueOf(value));
        } catch (RuntimeException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, ?> payload(Map<String, Object> d) {
        if (d.get("record") instanceof Map<?, ?> record && record.get("payload") instanceof Map<?, ?> payload) {
            return (Map<String, ?>) payload;
        }
        return null;
    }
}
