package ai.foresignal.source.adapters.roads;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Builds the slim, allowlisted payload of a {@code road-event} record (prompt 074), the same for every format.
 *
 * <p>Keys, all optional, nulls omitted (adapter rule 8):
 * <ul>
 *   <li>{@code category}: one of {@link #CATEGORIES}; {@code subtype}: the upstream code in lower-kebab case;
 *   <li>{@code severity}: one of {@link #SEVERITIES};
 *   <li>{@code headline} (at most {@value #MAX_HEADLINE} characters), {@code description} (plain text, at most
 *       {@value #MAX_DESCRIPTION_BYTES} UTF-8 bytes), {@code roads} (at most {@value #MAX_ROADS} distinct names or
 *       numbers);
 *   <li>{@code direction}: one of {@link #DIRECTIONS}; {@code status}: {@code active} or {@code planned};
 *       {@code isClosure}: true when the road (not just a lane) is closed;
 *   <li>{@code lanesAffected} (text), {@code speedLimitKph} (a temporary limit, km/h);
 *   <li>{@code startsAt}, {@code endsAt}: ISO-8601 instants (the descriptor's {@code startField} and
 *       {@code endField});
 *   <li>{@code format}: one of {@link #FORMATS}; {@code url}: the upstream's own page or API URL of the event;
 *       {@code language}: the language of the text, an ISO 639-1 code.
 * </ul>
 *
 * <p>Text is cut on a character boundary; values outside the closed vocabularies are refused with an
 * {@link IllegalArgumentException}, because a parser bug must not invent a new category.
 */
public final class RoadEventPayload {

    /** The categories every format maps onto. */
    public static final List<String> CATEGORIES =
            List.of("roadwork", "incident", "closure", "restriction", "weather", "event", "other");

    /** The severities every format maps onto. */
    public static final List<String> SEVERITIES = List.of("unknown", "minor", "moderate", "major", "severe");

    /** The directions every format maps onto. */
    public static final List<String> DIRECTIONS = List.of("both", "northbound", "southbound", "eastbound",
            "westbound", "inbound", "outbound", "unknown");

    /** The statuses of a current event. */
    public static final List<String> STATUSES = List.of("active", "planned");

    /** The source formats. */
    public static final List<String> FORMATS = List.of("open511", "wzdx", "digitraffic", "datex3");

    /** The longest headline, in characters. */
    public static final int MAX_HEADLINE = 200;

    /** The longest description, in UTF-8 bytes. */
    public static final int MAX_DESCRIPTION_BYTES = 4096;

    /** The most roads kept. */
    public static final int MAX_ROADS = 10;

    /** The longest road name or other short text value, in characters. */
    public static final int MAX_SHORT_TEXT = 200;

    private String category;
    private String subtype;
    private String severity;
    private String headline;
    private String description;
    private final Set<String> roads = new LinkedHashSet<>();
    private String direction;
    private String status;
    private Boolean isClosure;
    private String lanesAffected;
    private Double speedLimitKph;
    private Instant startsAt;
    private Instant endsAt;
    private String format;
    private String url;
    private String language;

    /**
     * Starts a payload of {@code format}.
     *
     * @param format one of {@link #FORMATS}
     */
    public RoadEventPayload(String format) {
        this.format = oneOf("format", format, FORMATS);
    }

    /** Sets the category, one of {@link #CATEGORIES}. */
    public RoadEventPayload category(String category) {
        this.category = category == null ? null : oneOf("category", category, CATEGORIES);
        return this;
    }

    /** Sets the subtype from an upstream code, converted to lower-kebab case. */
    public RoadEventPayload subtype(String code) {
        this.subtype = kebab(code);
        return this;
    }

    /** Sets the severity, one of {@link #SEVERITIES}. */
    public RoadEventPayload severity(String severity) {
        this.severity = severity == null ? null : oneOf("severity", severity, SEVERITIES);
        return this;
    }

    /** Sets the headline, whitespace collapsed and cut to {@value #MAX_HEADLINE} characters. */
    public RoadEventPayload headline(String headline) {
        this.headline = truncateChars(clean(headline), MAX_HEADLINE);
        return this;
    }

    /** Sets the description, whitespace collapsed and cut to {@value #MAX_DESCRIPTION_BYTES} UTF-8 bytes. */
    public RoadEventPayload description(String description) {
        this.description = truncateUtf8(clean(description), MAX_DESCRIPTION_BYTES);
        return this;
    }

    /** Adds a road name or number; blanks and duplicates are ignored, and at most {@value #MAX_ROADS} are kept. */
    public RoadEventPayload road(String road) {
        String value = truncateChars(clean(road), MAX_SHORT_TEXT);
        if (value != null && roads.size() < MAX_ROADS) {
            roads.add(value);
        }
        return this;
    }

    /** Adds every road of {@code values}. */
    public RoadEventPayload roads(Collection<String> values) {
        if (values != null) {
            values.forEach(this::road);
        }
        return this;
    }

    /** Sets the direction, one of {@link #DIRECTIONS}. */
    public RoadEventPayload direction(String direction) {
        this.direction = direction == null ? null : oneOf("direction", direction, DIRECTIONS);
        return this;
    }

    /** Sets the status, one of {@link #STATUSES}. */
    public RoadEventPayload status(String status) {
        this.status = status == null ? null : oneOf("status", status, STATUSES);
        return this;
    }

    /**
     * Sets the status from the schedule: {@code planned} when the event starts after {@code now}, otherwise
     * {@code active}.
     *
     * @param start the scheduled start, or null (active)
     * @param now the fetch time
     */
    public RoadEventPayload statusFrom(Instant start, Instant now) {
        return status(start != null && now != null && start.isAfter(now) ? "planned" : "active");
    }

    /** Sets whether the road itself is closed. */
    public RoadEventPayload isClosure(boolean closure) {
        this.isClosure = closure;
        return this;
    }

    /** Sets the lanes affected, as text. */
    public RoadEventPayload lanesAffected(String text) {
        this.lanesAffected = truncateChars(clean(text), MAX_SHORT_TEXT);
        return this;
    }

    /** Sets the temporary speed limit in km/h; a missing, non-positive or non-finite value is omitted. */
    public RoadEventPayload speedLimitKph(Double kph) {
        this.speedLimitKph = kph != null && Double.isFinite(kph) && kph > 0 ? kph : null;
        return this;
    }

    /** Sets the scheduled start. */
    public RoadEventPayload startsAt(Instant startsAt) {
        this.startsAt = startsAt;
        return this;
    }

    /** Sets the scheduled end. */
    public RoadEventPayload endsAt(Instant endsAt) {
        this.endsAt = endsAt;
        return this;
    }

    /** Sets the upstream URL of the event; only absolute http(s) URLs without user info are kept. */
    public RoadEventPayload url(String url) {
        String value = clean(url);
        this.url = value != null && value.length() <= 2048 && value.matches("(?i)^https?://[^\\s/@]+(/\\S*)?$")
                ? value : null;
        return this;
    }

    /** Sets the language of the text, lowercased (for example {@code en}, {@code fi}, {@code nl}). */
    public RoadEventPayload language(String language) {
        String value = clean(language);
        this.language = value == null ? null : value.toLowerCase(Locale.ROOT);
        return this;
    }

    /** The category set so far, or null. */
    public String category() {
        return category;
    }

    /** The start set so far, or null. */
    public Instant startsAt() {
        return startsAt;
    }

    /** The headline set so far, or null. */
    public String headline() {
        return headline;
    }

    /** The payload map in key order, nulls omitted. */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        put(out, "category", category);
        put(out, "subtype", subtype);
        put(out, "severity", severity);
        put(out, "headline", headline);
        put(out, "description", description);
        if (!roads.isEmpty()) {
            out.put("roads", List.copyOf(roads));
        }
        put(out, "direction", direction);
        put(out, "status", status);
        put(out, "isClosure", isClosure);
        put(out, "lanesAffected", lanesAffected);
        put(out, "speedLimitKph", speedLimitKph);
        put(out, "startsAt", startsAt == null ? null : startsAt.toString());
        put(out, "endsAt", endsAt == null ? null : endsAt.toString());
        put(out, "format", format);
        put(out, "url", url);
        put(out, "language", language);
        return out;
    }

    private static void put(Map<String, Object> out, String key, Object value) {
        if (value != null) {
            out.put(key, value);
        }
    }

    // ---------------------------------------------------------------- text helpers

    /**
     * An upstream code in lower-kebab case: {@code ROAD_CONSTRUCTION} → {@code road-construction},
     * {@code carriagewayClosures} → {@code carriageway-closures}, {@code accident report} → {@code accident-report}.
     *
     * @return the code, or null when blank
     */
    public static String kebab(String code) {
        String value = clean(code);
        if (value == null) {
            return null;
        }
        String split = value.replaceAll("([a-z0-9])([A-Z])", "$1-$2");
        String out = split.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
        return out.isEmpty() ? null : truncateChars(out, MAX_SHORT_TEXT);
    }

    /**
     * A camel-case or snake-case code as words: {@code carriagewayClosures} → {@code Carriageway closures}.
     *
     * @return the words with the first letter capitalized, or null when blank
     */
    public static String words(String code) {
        String kebab = kebab(code);
        if (kebab == null) {
            return null;
        }
        String text = kebab.replace('-', ' ');
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    /** {@code text} with whitespace runs collapsed to one space and stripped; null when blank. */
    public static String clean(String text) {
        if (text == null) {
            return null;
        }
        String out = text.replaceAll("[\\s\\u00A0]+", " ").strip();
        return out.isEmpty() ? null : out;
    }

    /**
     * The first sentence of {@code text} (up to the first {@code ". "}), for a headline when the upstream gives
     * none; the whole text when it has no sentence end.
     *
     * @return the sentence, or null when blank
     */
    public static String firstSentence(String text) {
        String value = clean(text);
        if (value == null) {
            return null;
        }
        int end = value.indexOf(". ");
        return end > 0 ? value.substring(0, end + 1) : value;
    }

    /** {@code text} cut to at most {@code max} characters, never inside a surrogate pair. */
    public static String truncateChars(String text, int max) {
        if (text == null || text.length() <= max) {
            return text;
        }
        int end = max;
        if (Character.isHighSurrogate(text.charAt(end - 1))) {
            end--;
        }
        return text.substring(0, end).strip();
    }

    /** {@code text} cut to at most {@code maxBytes} UTF-8 bytes, on a character boundary. */
    public static String truncateUtf8(String text, int maxBytes) {
        if (text == null) {
            return null;
        }
        int bytes = 0;
        int i = 0;
        while (i < text.length()) {
            int cp = text.codePointAt(i);
            int size = cp < 0x80 ? 1 : cp < 0x800 ? 2 : cp < 0x10000 ? 3 : 4;
            if (bytes + size > maxBytes) {
                return text.substring(0, i).strip();
            }
            bytes += size;
            i += Character.charCount(cp);
        }
        return text;
    }

    /**
     * Joins the non-blank parts with a space, skipping a part already contained in the text so far.
     *
     * @return the joined text, or null when every part is blank
     */
    public static String join(List<String> parts) {
        List<String> kept = new ArrayList<>();
        String sofar = "";
        for (String part : parts) {
            String value = clean(part);
            if (value != null && !sofar.contains(value)) {
                kept.add(value);
                sofar = String.join(" ", kept);
            }
        }
        return kept.isEmpty() ? null : sofar;
    }

    private static String oneOf(String field, String value, List<String> allowed) {
        if (!allowed.contains(value)) {
            throw new IllegalArgumentException("road-event " + field + " '" + value + "' is not one of " + allowed);
        }
        return value;
    }
}
