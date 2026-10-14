package ai.foresignal.source.adapters.humanitarian;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * Turns upstream HTML into bounded plain text (prompt 090) with jsoup's allowlist cleaner (no tags kept,
 * entities decoded), collapsing runs of whitespace. Free text from these sources is untrusted for LLM use.
 */
public final class HtmlText {

    private HtmlText() {}

    /**
     * The plain text of {@code html}, capped at {@code maxChars} characters (a cut value ends in {@code …}).
     *
     * @return the text, or null when the input is null or blank
     */
    public static String strip(String html, int maxChars) {
        if (html == null || html.isBlank()) {
            return null;
        }
        String text = Jsoup.clean(html, "", Safelist.none())
                .replace('\u00a0', ' ')
                .replaceAll("\\s+", " ")
                .strip();
        if (text.isEmpty()) {
            return null;
        }
        if (maxChars > 0 && text.length() > maxChars) {
            int cut = Math.max(0, maxChars - 1);
            text = text.substring(0, cut) + "…";
        }
        return text;
    }
}
