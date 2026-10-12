package ai.foresignal.notify;

import java.nio.charset.StandardCharsets;
import java.text.BreakIterator;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

/**
 * Shortens text to a destination's limit (prompt 018), shared by every channel type.
 *
 * <p>The limit is counted in UTF-16 characters ({@link Unit#CHARS}, what {@link String#length()}
 * reports) or in UTF-8 bytes ({@link Unit#UTF8_BYTES}). Cuts fall only on grapheme boundaries
 * ({@link BreakIterator#getCharacterInstance()}), so an emoji with its modifiers or a letter with its
 * combining marks is never split. In {@link Mode#HTML} a cut never falls inside a tag or an entity,
 * and every tag still open at the cut is closed again. When the text is cut, "…" is appended, plus a
 * link to {@code url} when one is given and the space allows (an anchor in HTML, a markdown link in
 * {@link Mode#MARKDOWN}, the bare URL in plain text). The result, suffix included, never exceeds the
 * limit.
 */
public final class Truncator {

    /** The ellipsis appended to cut text. */
    public static final String ELLIPSIS = "…";

    /** How the limit is counted. */
    public enum Unit {
        /** UTF-16 code units. */
        CHARS,
        /** UTF-8 bytes. */
        UTF8_BYTES
    }

    /** The text's format, which decides safe cut points and the link suffix. */
    public enum Mode {
        /** Plain text. */
        PLAIN,
        /** Markdown (cut as plain text; the link suffix is a markdown link). */
        MARKDOWN,
        /** HTML: never cut inside a tag or entity; close open tags. */
        HTML
    }

    private Truncator() {}

    /**
     * {@code text} shortened to {@code limit}.
     *
     * @param text the text; null is returned as null
     * @param limit the largest size of the result
     * @param unit how the size is counted
     * @param mode the text's format
     * @param url a link appended after the ellipsis when space allows, or null
     */
    public static String truncate(String text, int limit, Unit unit, Mode mode, String url) {
        if (text == null || size(text, unit) <= limit) {
            return text;
        }
        if (limit <= 0) {
            return "";
        }
        String withLink = url == null || url.isBlank() ? null : suffix(mode, url);
        if (withLink != null) {
            String cut = cut(text, limit, unit, mode, withLink);
            if (cut != null) {
                return cut;
            }
        }
        String cut = cut(text, limit, unit, mode, ELLIPSIS);
        if (cut != null) {
            return cut;
        }
        return size(ELLIPSIS, unit) <= limit ? ELLIPSIS : "";
    }

    /** The size of {@code text} in {@code unit}. */
    public static int size(String text, Unit unit) {
        return unit == Unit.CHARS ? text.length() : text.getBytes(StandardCharsets.UTF_8).length;
    }

    private static String suffix(Mode mode, String url) {
        return switch (mode) {
            case HTML -> ELLIPSIS + " <a href=\"" + escapeAttribute(url) + "\">more</a>";
            case MARKDOWN -> ELLIPSIS + " [more](" + url + ")";
            case PLAIN -> ELLIPSIS + " " + url;
        };
    }

    /**
     * The longest prefix ending at a safe boundary such that prefix + closing tags + suffix fits, or
     * null when not even an empty prefix fits.
     */
    private static String cut(String text, int limit, Unit unit, Mode mode, String suffix) {
        List<Integer> boundaries = safeBoundaries(text, mode);
        int suffixSize = size(suffix, unit);
        // Prefix size grows with the boundary, so binary-search the last boundary whose bare prefix
        // plus the suffix fits, then step back while closing tags push it over.
        int lo = 0;
        int hi = boundaries.size() - 1;
        int last = -1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (size(text.substring(0, boundaries.get(mid)), unit) + suffixSize <= limit) {
                last = mid;
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        for (int i = last; i >= 0; i--) {
            String prefix = text.substring(0, boundaries.get(i));
            String closed = mode == Mode.HTML ? prefix.stripTrailing() + closingTags(prefix) : prefix.stripTrailing();
            String candidate = closed + suffix;
            if (size(candidate, unit) <= limit) {
                return candidate;
            }
        }
        return null;
    }

    /** Grapheme boundaries that are outside tags and entities (HTML), ascending, including 0. */
    static List<Integer> safeBoundaries(String text, Mode mode) {
        List<Integer> out = new ArrayList<>();
        boolean[] unsafe = mode == Mode.HTML ? unsafePositions(text) : null;
        BreakIterator graphemes = BreakIterator.getCharacterInstance(Locale.ROOT);
        graphemes.setText(text);
        for (int b = graphemes.first(); b != BreakIterator.DONE; b = graphemes.next()) {
            if (b == text.length()) {
                break;
            }
            if (unsafe == null || !unsafe[b]) {
                out.add(b);
            }
        }
        return out;
    }

    /** {@code unsafe[i]} is true when a cut before index {@code i} would fall inside a tag or entity. */
    private static boolean[] unsafePositions(String text) {
        boolean[] unsafe = new boolean[text.length() + 1];
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            int close = -1;
            if (c == '<') {
                close = text.indexOf('>', i);
            } else if (c == '&') {
                int semi = text.indexOf(';', i);
                if (semi > i && semi - i <= 10 && text.substring(i + 1, semi).matches("#?[A-Za-z0-9]+")) {
                    close = semi;
                }
            }
            if (close > i) {
                for (int k = i + 1; k <= close; k++) {
                    unsafe[k] = true;
                }
                i = close + 1;
            } else {
                i++;
            }
        }
        return unsafe;
    }

    /** The closing tags for every element still open at the end of {@code html}, innermost first. */
    static String closingTags(String html) {
        Deque<String> open = new ArrayDeque<>();
        int i = 0;
        while (true) {
            int start = html.indexOf('<', i);
            if (start < 0) {
                break;
            }
            int end = html.indexOf('>', start);
            if (end < 0) {
                break;
            }
            String inner = html.substring(start + 1, end).strip();
            i = end + 1;
            if (inner.isEmpty() || inner.endsWith("/") || inner.startsWith("!")) {
                continue;
            }
            if (inner.startsWith("/")) {
                String name = tagName(inner.substring(1));
                if (open.contains(name)) {
                    while (!open.isEmpty() && !open.pop().equals(name)) {
                        // pop until the matching element
                    }
                }
                continue;
            }
            String name = tagName(inner);
            if (!name.equals("br") && !name.equals("hr") && !name.equals("img")) {
                open.push(name);
            }
        }
        StringBuilder out = new StringBuilder();
        for (String name : open) {
            out.append("</").append(name).append('>');
        }
        return out.toString();
    }

    private static String tagName(String inner) {
        int end = 0;
        while (end < inner.length() && !Character.isWhitespace(inner.charAt(end)) && inner.charAt(end) != '/') {
            end++;
        }
        return inner.substring(0, end).toLowerCase(Locale.ROOT);
    }

    private static String escapeAttribute(String value) {
        return value.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
