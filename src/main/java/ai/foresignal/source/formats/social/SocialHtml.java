package ai.foresignal.source.formats.social;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Plain text from the HTML some platforms send as post content (prompt 135; Mastodon's {@code content}): paragraphs
 * and line breaks become newlines, everything else is its text. Nothing is fetched and no markup survives.
 */
public final class SocialHtml {

    private SocialHtml() {}

    /** The plain text of an HTML fragment, or null for null. */
    public static String text(String html) {
        if (html == null) {
            return null;
        }
        Document doc = Jsoup.parseBodyFragment(html);
        for (Element br : doc.select("br")) {
            br.after("\\n");
        }
        for (Element p : doc.select("p")) {
            p.prepend("\\n\\n");
        }
        String text = doc.body().text().replace("\\n", "\n");
        return text.strip().replaceAll("\n{3,}", "\n\n");
    }
}
