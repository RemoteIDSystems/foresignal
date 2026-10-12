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
 *
 * <p><b>Region placeholders (prompt 040),</b> from the record's region tags, each empty when unknown:
 * {@code {{near.name}}}, {@code {{near.km}}}, {@code {{near.mi}}}, {@code {{near.bearing}}} (16-point
 * compass, {@code NNE}), {@code {{near.text}}} ({@code 12 km NNE of Austin, TX}; in miles when the
 * extras say {@code imperial}, which a channel's {@code config.units: imperial} sets, or with the filter
 * {@code {{near.text|imperial}}}), {@code {{country}}} (ISO2), {@code {{country.name}}},
 * {@code {{admin1}}}, {@code {{admin1.name}}}, {@code {{county}}} and {@code {{sea}}}. Distances are
 * whole numbers.
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
     * @param region the record's region tags (prompt 040), or null
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
            Map<String, ?> payload,
            ai.foresignal.reference.RegionTags region) {

        /** A context without region tags (the shape before prompt 040). */
        public Context(String name, String source, String externalId, String kind, String title, String severity,
                Instant observedAt, Instant eventTime, Instant validFrom, Map<String, ?> payload) {
            this(name, source, externalId, kind, title, severity, observedAt, eventTime, validFrom, payload, null);
        }
    }

    /**
     * The notification extras of {@link #render(String, Context, Extras)}.
     *
     * @param zone the zone of {@code |local}; null means UTC
     * @param now the reference time of {@code |ago}; null means {@link Instant#now()}
     * @param vars extra variables by name (they win over the built-in placeholders)
     * @param escape applied to every substituted value (for example markdown escaping for a body),
     *     or null
     * @param imperial true to render {@code {{near.text}}} in miles (a channel's {@code units=imperial},
     *     prompt 040)
     */
    public record Extras(ZoneId zone, Instant now, Map<String, String> vars, UnaryOperator<String> escape,
            boolean imperial) {

        /** No extras: UTC, no extra variables, no escaping. */
        public static final Extras NONE = new Extras(ZoneOffset.UTC, null, Map.of(), null, false);

        /** Metric extras (the shape before prompt 040). */
        public Extras(ZoneId zone, Instant now, Map<String, String> vars, UnaryOperator<String> escape) {
            this(zone, now, vars, escape, false);
        }

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
        String region = region(key, filter, c.region(), x.imperial());
        if (region != null) {
            return region;
        }
        return value(key, c);
    }

    /** The region placeholders of prompt 040; null when {@code key} is not one of them. */
    static String region(String key, String filter, ai.foresignal.reference.RegionTags tags, boolean imperial) {
        if (!REGION_KEYS.contains(key)) {
            return null;
        }
        if (tags == null) {
            return "";
        }
        ai.foresignal.reference.RegionNear near = tags.near();
        return switch (key) {
            case "near.name" -> near == null ? "" : text(near.name());
            case "near.km" -> near == null ? "" : Long.toString(Math.round(near.distanceKm()));
            case "near.mi" -> near == null ? "" : Long.toString(Math.round(near.distanceMi()));
            case "near.bearing" -> near == null ? "" : near.compass();
            case "near.text" -> near == null ? "" : near.text(imperial || "imperial".equals(filter));
            case "country" -> text(tags.iso2());
            case "country.name" -> text(tags.countryName());
            case "admin1" -> text(tags.admin1());
            case "admin1.name" -> text(tags.admin1Name());
            case "county" -> text(tags.countyName() != null ? tags.countyName() : tags.admin2());
            case "sea" -> text(tags.sea());
            default -> "";
        };
    }

    /** The region placeholder names (prompt 040). */
    public static final java.util.Set<String> REGION_KEYS = java.util.Set.of("near.name", "near.km", "near.mi",
            "near.bearing", "near.text", "country", "country.name", "admin1", "admin1.name", "county", "sea");

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
