package ai.foresignal.briefing;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * One delivery target of an edition (prompt NP-004, OpenAPI {@code DeliveryTarget} authored by NP-001): where a run goes
 * and in what shape.
 *
 * <ul>
 *   <li>{@value #CHANNEL}: a 018 notify channel ({@code channelId}) in format {@value #EMAIL} (the whole edition with
 *       its audio attached and a link to the site), {@value #PUSH} (a headline, a short summary and a link, never the
 *       whole body) or {@value #ALERT} (the shape of an alert notification: severity, title, summary, link).
 *   <li>{@value #WEBSITE}: the hosted edition site ({@code getEditionSite}, NP-005), format {@value #HTML}.
 *   <li>{@value #PODCAST}: the briefing podcast feed ({@code getBriefingPodcast}, 141), format {@value #AUDIO}.
 * </ul>
 *
 * @param kind {@value #CHANNEL}, {@value #WEBSITE} or {@value #PODCAST}
 * @param channelId the channel of a {@value #CHANNEL} target, null otherwise
 * @param format the payload shape
 * @param options per-kind options, passed on as they are (never null)
 */
public record DeliveryTarget(String kind, UUID channelId, String format, Map<String, Object> options) {

    /** A notify channel. */
    public static final String CHANNEL = "channel";

    /** The hosted edition site. */
    public static final String WEBSITE = "website";

    /** The podcast feed. */
    public static final String PODCAST = "podcast";

    /** The whole edition as an email. */
    public static final String EMAIL = "email";

    /** A headline and a link. */
    public static final String PUSH = "push";

    /** The alert-notification shape. */
    public static final String ALERT = "alert";

    /** The standalone page. */
    public static final String HTML = "html";

    /** The synthesized edition. */
    public static final String AUDIO = "audio";

    /** Every kind. */
    public static final Set<String> KINDS = Set.of(CHANNEL, WEBSITE, PODCAST);

    /** The formats a {@value #CHANNEL} target takes. */
    public static final Set<String> CHANNEL_FORMATS = Set.of(EMAIL, PUSH, ALERT);

    /** Requires the kind and format; copies the options. */
    public DeliveryTarget {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(format, "format");
        options = options == null ? Map.of() : Map.copyOf(options);
    }

    /** The stored and wire form. */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("kind", kind);
        out.put("channelId", channelId == null ? null : channelId.toString());
        out.put("format", format);
        out.put("options", new LinkedHashMap<>(options));
        return out;
    }

    /** The target stored as {@code map}, or null when it is unreadable. */
    @SuppressWarnings("unchecked")
    public static DeliveryTarget fromMap(Map<String, Object> map) {
        if (map == null || !(map.get("kind") instanceof String kind) || !(map.get("format") instanceof String format)) {
            return null;
        }
        UUID channel = null;
        if (map.get("channelId") instanceof String id) {
            try {
                channel = UUID.fromString(id);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        Map<String, Object> options = map.get("options") instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
        return new DeliveryTarget(kind, channel, format, options);
    }

    /** The format a {@code kind} other than {@value #CHANNEL} requires, or null for a channel. */
    public static String requiredFormat(String kind) {
        return switch (kind) {
            case WEBSITE -> HTML;
            case PODCAST -> AUDIO;
            default -> null;
        };
    }
}
