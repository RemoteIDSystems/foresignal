package ai.foresignal.source.adapters.gdelt;

import ai.foresignal.source.formats.news.NewsNames;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses the GDELT DOC 2.0 {@code mode=ArtList&format=json} answer (prompt 133).
 *
 * <p>Verified against a live capture on 2026-10-18: {@code {"articles": [{"url", "url_mobile", "title", "seendate"
 * ("20261018T013000Z"), "socialimage", "domain", "language" ("English"), "sourcecountry" ("Russia")}]}}. An answer
 * with no matches is {@code {}}. A plain-text answer (GDELT's error messages, its rate-limit notice) is not JSON and is
 * refused. Pure.
 */
public final class GdeltArtListParser {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final DateTimeFormatter SEEN = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    private GdeltArtListParser() {}

    /**
     * One ArtList entry.
     *
     * @param url the article URL
     * @param title the headline
     * @param seenAt when GDELT saw it
     * @param imageUrl the social image link, or null
     * @param domain GDELT's domain, or null
     * @param language the ISO 639 language code, or null when unknown
     * @param sourceCountry the ISO 3166-1 alpha-2 country, or null when unknown
     */
    public record Entry(String url, String title, Instant seenAt, String imageUrl, String domain, String language,
            String sourceCountry) {}

    /**
     * Parses one answer.
     *
     * @throws UpstreamFormatException when the body is not an ArtList JSON object
     */
    public static List<Entry> parse(byte[] body) throws UpstreamFormatException {
        String text = body == null ? "" : new String(body, StandardCharsets.UTF_8).strip();
        if (text.isEmpty()) {
            return List.of();
        }
        JsonNode root;
        try {
            root = JSON.readTree(text);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("gdelt-articles: GDELT answered with text, not JSON: "
                    + text.substring(0, Math.min(200, text.length())), "");
        }
        if (root == null || !root.isObject()) {
            throw new UpstreamFormatException("gdelt-articles: the ArtList answer is not a JSON object", "");
        }
        JsonNode articles = root.get("articles");
        if (articles == null || articles.isNull()) {
            return List.of();
        }
        if (!articles.isArray()) {
            throw new UpstreamFormatException("gdelt-articles: 'articles' is not an array", "");
        }
        List<Entry> out = new ArrayList<>();
        for (JsonNode a : articles) {
            String url = text(a, "url");
            String title = text(a, "title");
            Instant seen = seen(text(a, "seendate"));
            if (url == null || title == null || seen == null) {
                continue;
            }
            out.add(new Entry(url, title, seen, text(a, "socialimage"), text(a, "domain"),
                    NewsNames.languageCode(text(a, "language")), NewsNames.countryIso2(text(a, "sourcecountry"))));
        }
        return out;
    }

    /** GDELT's {@code seendate} ({@code yyyyMMddTHHmmssZ}, UTC), or null. */
    static Instant seen(String text) {
        if (text == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(text, SEEN).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) {
            return null;
        }
        String s = v.asString();
        return s == null || s.isBlank() ? null : s.strip();
    }
}
