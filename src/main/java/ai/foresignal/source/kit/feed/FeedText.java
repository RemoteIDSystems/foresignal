package ai.foresignal.source.kit.feed;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;

/**
 * Feed text as plain text (prompt 073). Item text is upstream-controlled, so it is never kept or rendered as HTML.
 *
 * <ul>
 *   <li>{@link #plain}: HTML removed with jsoup's {@link Safelist#none()} (line breaks and block ends become spaces
 *       first, so words never run together), entities decoded, whitespace collapsed, cut at a byte limit on a
 *       character boundary;
 *   <li>{@link #link}: an absolute {@code http}/{@code https} URL without user info, resolved against a base, or null.
 * </ul>
 */
public final class FeedText {

    /** The longest summary kept, in UTF-8 bytes. */
    public static final int MAX_SUMMARY_BYTES = 4096;

    /** The longest title kept, in UTF-8 bytes. */
    public static final int MAX_TITLE_BYTES = 1024;

    /** The longest short value (author, category, id) kept, in UTF-8 bytes. */
    public static final int MAX_SHORT_BYTES = 256;

    private static final Pattern BREAKS = Pattern.compile(
            "(?i)<\\s*(br|hr|/p|/div|/li|/h[1-6]|/tr|/td|/th|/blockquote|/pre|/dt|/dd)\\b[^>]*>");

    private static final Pattern SPACE = Pattern.compile("[\\s\\u00A0\\u200B]+");

    private static final Document.OutputSettings OUTPUT = new Document.OutputSettings().prettyPrint(false);

    private FeedText() {}

    /**
     * {@code html} as plain text, at most {@code maxBytes} UTF-8 bytes, or null when nothing is left.
     *
     * @param html HTML or plain text
     * @param maxBytes the byte limit
     */
    public static String plain(String html, int maxBytes) {
        if (html == null || html.isBlank()) {
            return null;
        }
        String spaced = BREAKS.matcher(html).replaceAll(" ");
        String cleaned = Jsoup.clean(spaced, "", Safelist.none(), OUTPUT);
        String text = SPACE.matcher(Parser.unescapeEntities(cleaned, false)).replaceAll(" ").strip();
        if (text.isEmpty()) {
            return null;
        }
        return cut(text, maxBytes);
    }

    /** {@code text} cut to at most {@code maxBytes} UTF-8 bytes, never inside a character. */
    public static String cut(String text, int maxBytes) {
        if (text == null) {
            return null;
        }
        if (text.getBytes(StandardCharsets.UTF_8).length <= maxBytes) {
            return text;
        }
        int bytes = 0;
        int end = 0;
        while (end < text.length()) {
            int cp = text.codePointAt(end);
            int size = cp < 0x80 ? 1 : cp < 0x800 ? 2 : cp < 0x10000 ? 3 : 4;
            if (bytes + size > maxBytes) {
                break;
            }
            bytes += size;
            end += Character.charCount(cp);
        }
        return text.substring(0, end).strip();
    }

    /**
     * {@code raw} as an absolute http(s) URL, resolved against {@code base}, or null.
     *
     * @param raw the link text
     * @param base the base URL, or null
     */
    public static String link(String raw, String base) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            URI uri = new URI(raw.strip());
            if (!uri.isAbsolute() && base != null) {
                uri = new URI(base.strip()).resolve(uri);
            }
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if (!uri.isAbsolute() || (!scheme.equals("http") && !scheme.equals("https")) || uri.getHost() == null
                    || uri.getRawUserInfo() != null) {
                return null;
            }
            String out = uri.toString();
            return out.length() > 2048 ? null : out;
        } catch (java.net.URISyntaxException | IllegalArgumentException e) {
            return null;
        }
    }
}
