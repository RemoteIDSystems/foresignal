package ai.foresignal.trigger;

import ai.foresignal.ingest.JsonValues;
import java.time.Instant;
import java.util.Map;

/**
 * Renders an alert message (prompt 017).
 *
 * <p>Placeholders are {@code {{name}}}, {@code {{source}}}, {@code {{externalId}}}, {@code {{kind}}},
 * {@code {{title}}}, {@code {{severity}}}, {@code {{observedAt}}}, {@code {{eventTime}}},
 * {@code {{time}}} (the effective time, {@code eventTime} else {@code observedAt}),
 * {@code {{validFrom}}} and {@code {{payload.<dot.path>}}}; whitespace inside the braces is ignored.
 * Anything unknown or missing renders as nothing. There are no expressions, and a substituted value
 * is never scanned again, so a payload cannot inject placeholders. Times render as ISO-8601 UTC,
 * numbers canonically, objects and arrays as nothing. The result is cut to 2,000 characters. Prompt
 * 018 adds timezones and filters on top.
 */
public final class MessageTemplate {

    /** The template of a rule without one. */
    public static final String DEFAULT = "{{name}}: {{source}}/{{externalId}}";

    /** The longest template accepted on a rule. */
    public static final int MAX_TEMPLATE = 1_000;

    /** The longest rendered message. */
    public static final int MAX_OUTPUT = 2_000;

    private static final String PAYLOAD_PREFIX = "payload.";

    private MessageTemplate() {}

    /**
     * What a template can refer to.
     *
     * @param name the rule name
     * @param source the source id
     * @param externalId the entity
     * @param kind the kind
     * @param title the record title, or null
     * @param severity the severity wire value
     * @param observedAt when the version was observed
     * @param eventTime the event time, or null
     * @param validFrom the start of validity, or null
     * @param payload the version's payload, or null
     */
    public record Context(
            String name,
            String source,
            String externalId,
            String kind,
            String title,
            String severity,
            Instant observedAt,
            Instant eventTime,
            Instant validFrom,
            Map<String, ?> payload) {}

    /** Renders {@code template} (the default when null or blank) against {@code context}. */
    public static String render(String template, Context context) {
        String source = template == null || template.isBlank() ? DEFAULT : template;
        StringBuilder out = new StringBuilder(Math.min(source.length() + 64, MAX_OUTPUT + 16));
        int i = 0;
        while (i < source.length() && out.length() <= MAX_OUTPUT) {
            int open = source.indexOf("{{", i);
            if (open < 0) {
                out.append(source, i, source.length());
                break;
            }
            int close = source.indexOf("}}", open + 2);
            if (close < 0) {
                out.append(source, i, source.length());
                break;
            }
            out.append(source, i, open);
            out.append(value(source.substring(open + 2, close).strip(), context));
            i = close + 2;
        }
        return out.length() > MAX_OUTPUT ? out.substring(0, MAX_OUTPUT) : out.toString();
    }

    private static String value(String key, Context c) {
        if (key.startsWith(PAYLOAD_PREFIX)) {
            String path = key.substring(PAYLOAD_PREFIX.length());
            if (path.isEmpty() || c.payload() == null) {
                return "";
            }
            String text = JsonValues.text(JsonValues.at(c.payload(), path));
            return text == null ? "" : text;
        }
        return switch (key) {
            case "name" -> text(c.name());
            case "source" -> text(c.source());
            case "externalId" -> text(c.externalId());
            case "kind" -> text(c.kind());
            case "title" -> text(c.title());
            case "severity" -> text(c.severity());
            case "observedAt" -> time(c.observedAt());
            case "eventTime" -> time(c.eventTime());
            case "time" -> time(c.eventTime() != null ? c.eventTime() : c.observedAt());
            case "validFrom" -> time(c.validFrom());
            default -> "";
        };
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }

    private static String time(Instant value) {
        return value == null ? "" : value.toString();
    }
}
