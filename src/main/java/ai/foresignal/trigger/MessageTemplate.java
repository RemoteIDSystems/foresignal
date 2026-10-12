package ai.foresignal.trigger;

import ai.foresignal.ingest.JsonValues;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * Renders an alert message (prompt 017).
 *
 * <p>Placeholders are {@code {{name}}}, {@code {{source}}}, {@code {{externalId}}}, {@code {{kind}}},
 * {@code {{title}}}, {@code {{severity}}}, {@code {{observedAt}}}, {@code {{eventTime}}},
 * {@code {{time}}} (the effective time, {@code eventTime} else {@code observedAt}),
 * {@code {{validFrom}}} and {@code {{payload.<dot.path>}}}; whitespace inside the braces is ignored.
 * Anything unknown or missing renders as nothing. There are no expressions, and a substituted value
 * is never scanned again, so a payload cannot inject placeholders. Times render as ISO-8601 UTC,
 * numbers canonically, objects and arrays as nothing. The result is cut to 2,000 characters.
 *
 * <p><b>Filters and extra variables (prompt 018).</b> {@link #render(String, Context, Extras)} adds
 * the notification variables ({@code {{summary}}}, {@code {{url}}}, {@code {{ruleName}}},
 * {@code {{message}}}, any others the caller passes, which also override the built-ins) and two
 * filters on the time placeholders: {@code {{observedAt|local}}} formats in the extras' zone with the
 * fixed pattern {@value #LOCAL_PATTERN} (US English zone abbreviations, for example {@code CDT}), and
 * {@code {{observedAt|ago}}} renders the age relative to the extras' {@code now}, for example
 * {@code 3 min ago}. Only {@code java.time} is used; there is no ICU dependency. An unknown filter
 * renders the value unfiltered.
 */
public final class MessageTemplate {

    /** The template of a rule without one. */
    public static final String DEFAULT = "{{name}}: {{source}}/{{externalId}}";

    /** The longest template accepted on a rule. */
    public static final int MAX_TEMPLATE = 1_000;

    /** The longest rendered message. */
    public static final int MAX_OUTPUT = 2_000;

    private static final String PAYLOAD_PREFIX = "payload.";

    /** The pattern of the {@code |local} filter. */
    public static final String LOCAL_PATTERN = "yyyy-MM-dd HH:mm zzz";

    private static final DateTimeFormatter LOCAL = DateTimeFormatter.ofPattern(LOCAL_PATTERN, Locale.US);

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

    /**
     * The notification extras of {@link #render(String, Context, Extras)}.
     *
     * @param zone the zone of {@code |local}; null means UTC
     * @param now the reference time of {@code |ago}; null means {@link Instant#now()}
     * @param vars extra variables by name (they win over the built-in placeholders)
     * @param escape applied to every substituted value (for example markdown escaping for a body),
     *     or null
     */
    public record Extras(ZoneId zone, Instant now, Map<String, String> vars, UnaryOperator<String> escape) {

        /** No extras: UTC, no extra variables, no escaping. */
        public static final Extras NONE = new Extras(ZoneOffset.UTC, null, Map.of(), null);

        /**
         * Copies the variables. A null value is kept (it renders as an empty string, see
         * {@code value}); {@code Map.copyOf} would reject it, which failed every notification whose
         * alert had no summary (prompt 022).
         */
        public Extras {
            vars = vars == null ? Map.of() : Collections.unmodifiableMap(new HashMap<>(vars));
        }
    }

    /** Renders {@code template} (the default when null or blank) against {@code context}. */
    public static String render(String template, Context context) {
        return render(template, context, Extras.NONE);
    }

    /**
     * Renders {@code template} (the default when null or blank) against {@code context}, with the
     * notification variables and filters of {@code extras}.
     */
    public static String render(String template, Context context, Extras extras) {
        Extras x = extras == null ? Extras.NONE : extras;
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
            String value = value(source.substring(open + 2, close).strip(), context, x);
            out.append(x.escape() == null ? value : x.escape().apply(value));
            i = close + 2;
        }
        return out.length() > MAX_OUTPUT ? out.substring(0, MAX_OUTPUT) : out.toString();
    }

    private static String value(String expression, Context c, Extras x) {
        int bar = expression.indexOf('|');
        String key = bar < 0 ? expression : expression.substring(0, bar).strip();
        String filter = bar < 0 ? null : expression.substring(bar + 1).strip();
        if (x.vars().containsKey(key)) {
            String var = x.vars().get(key);
            return var == null ? "" : var;
        }
        Instant instant = switch (key) {
            case "observedAt" -> c.observedAt();
            case "eventTime" -> c.eventTime();
            case "time" -> c.eventTime() != null ? c.eventTime() : c.observedAt();
            case "validFrom" -> c.validFrom();
            default -> null;
        };
        if (instant != null && filter != null) {
            return switch (filter) {
                case "local" -> LOCAL.format(instant.atZone(x.zone() == null ? ZoneOffset.UTC : x.zone()));
                case "ago" -> ago(instant, x.now() == null ? Instant.now() : x.now());
                default -> instant.toString();
            };
        }
        return value(key, c);
    }

    /** {@code "3 min ago"}, {@code "in 2 h"}, {@code "just now"}: whole units, rounded down. */
    static String ago(Instant at, Instant now) {
        Duration d = Duration.between(at, now);
        boolean future = d.isNegative();
        long s = Math.abs(d.getSeconds());
        String amount;
        if (s < 60) {
            if (s < 5) {
                return "just now";
            }
            amount = s + " s";
        } else if (s < 3_600) {
            amount = (s / 60) + " min";
        } else if (s < 86_400) {
            amount = (s / 3_600) + " h";
        } else {
            amount = (s / 86_400) + " d";
        }
        return future ? "in " + amount : amount + " ago";
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
