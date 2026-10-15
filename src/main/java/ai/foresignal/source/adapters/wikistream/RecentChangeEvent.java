package ai.foresignal.source.adapters.wikistream;

import ai.foresignal.source.spi.UpstreamFormatException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Set;
import java.util.regex.Pattern;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * One Wikimedia EventStreams {@code recentchange} event (prompt 095).
 *
 * <p>A pure parser over one SSE {@code data:} payload. Only the fields the surge detector needs are read;
 * {@code user}, {@code comment} and {@code parsedcomment} are read only to hash the editor (never stored) and
 * are otherwise dropped, because they are contributor text (untrusted, and often personal).
 *
 * @param dt the event time ({@code meta.dt})
 * @param id the upstream event id ({@code meta.id})
 * @param domain the wikimedia domain ({@code meta.domain})
 * @param wiki the wiki database name (for example {@code enwiki})
 * @param type {@code edit}, {@code new} or another change type
 * @param namespace the page namespace
 * @param title the page title
 * @param titleUrl the canonical page URL
 * @param serverUrl the wiki's server URL, for the geotag query
 * @param bot whether the change was made by a bot
 * @param user the editor name (hashed, never stored)
 */
public record RecentChangeEvent(Instant dt, String id, String domain, String wiki, String type, int namespace,
        String title, String titleUrl, String serverUrl, boolean bot, String user) {

    /** The change types a surge is built from. */
    public static final Set<String> EDIT_TYPES = Set.of("edit", "new");

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final Pattern CONTROL = Pattern.compile("\\p{Cntrl}");

    /**
     * Parses one SSE data payload.
     *
     * @param data the JSON text after {@code data:}
     * @return the event, or empty when it lacks a time or title (unusable)
     * @throws UpstreamFormatException when the payload is not valid JSON or not an object
     */
    public static java.util.Optional<RecentChangeEvent> parse(String data) {
        if (data == null || data.isBlank()) {
            return java.util.Optional.empty();
        }
        JsonNode root;
        try {
            root = JSON.readTree(data);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("recentchange event is not valid JSON: " + e.getOriginalMessage(),
                    snippet(data), e);
        }
        if (root == null || !root.isObject()) {
            throw new UpstreamFormatException("recentchange event is not a JSON object", snippet(data));
        }
        JsonNode meta = root.get("meta");
        String dtText = meta != null && meta.isObject() ? text(meta.get("dt")) : null;
        Instant dt = instant(dtText);
        String title = text(root.get("title"));
        if (dt == null || title == null) {
            return java.util.Optional.empty();
        }
        // The canary arrives as a top-level domain "canary"; the normal event carries meta.domain.
        String metaDomain = meta != null && meta.isObject() ? text(meta.get("domain")) : null;
        String topDomain = text(root.get("domain"));
        return java.util.Optional.of(new RecentChangeEvent(
                dt,
                meta != null && meta.isObject() ? text(meta.get("id")) : null,
                topDomain != null ? topDomain : metaDomain,
                text(root.get("wiki")),
                text(root.get("type")),
                integral(root.get("namespace")),
                title,
                text(root.get("title_url")),
                text(root.get("server_url")),
                truthy(root.get("bot")),
                text(root.get("user"))));
    }

    /** True when this event is kept: not the canary, an edit or new page, main namespace, not a bot, a watched wiki. */
    public boolean kept(Set<String> wikis) {
        if (domain != null && domain.equalsIgnoreCase("canary")) {
            return false;
        }
        if (type == null || !EDIT_TYPES.contains(type)) {
            return false;
        }
        if (namespace != 0 || bot) {
            return false;
        }
        return wiki != null && wikis.contains(wiki);
    }

    /** True when this event created a new page. */
    public boolean isNew() {
        return "new".equals(type);
    }

    private static Instant instant(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static int integral(JsonNode node) {
        if (node == null || !node.isNumber()) {
            return 0;
        }
        return node.asInt();
    }

    private static boolean truthy(JsonNode node) {
        if (node == null || node.isNull()) {
            return false;
        }
        return node.isBoolean() ? node.asBoolean() : "true".equalsIgnoreCase(text(node));
    }

    private static String text(JsonNode node) {
        if (node == null || node.isNull() || node.isContainer()) {
            return null;
        }
        String raw = node.asString();
        String clean = CONTROL.matcher(raw).replaceAll("").strip();
        return clean.isEmpty() ? null : clean;
    }

    private static String snippet(String data) {
        return data.length() <= UpstreamFormatException.MAX_SNIPPET_LENGTH
                ? data
                : data.substring(0, UpstreamFormatException.MAX_SNIPPET_LENGTH) + "… (truncated)";
    }
}
