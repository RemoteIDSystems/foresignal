package ai.foresignal.briefing;

import java.util.List;
import java.util.Map;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

/**
 * The HTML rendition of a briefing run (prompt 029): markdown converted with CommonMark and GFM tables,
 * then sanitized with an allowlist, inside a minimal page with a light/dark stylesheet.
 *
 * <p>Two layers keep record text inert. CommonMark escapes raw HTML in the markdown instead of passing
 * it through ({@code escapeHtml}) and drops unsafe link targets ({@code sanitizeUrls}); the OWASP
 * sanitizer then keeps only headings, paragraphs, lists, tables, links ({@code http}, {@code https},
 * {@code mailto}, with {@code rel="noopener nofollow"}), code, emphasis, quotes and rules, so no
 * script, event handler, {@code style} attribute or iframe can survive. The page itself carries only
 * an inline stylesheet, which the response's Content-Security-Policy
 * ({@value #CONTENT_SECURITY_POLICY}) allows; nothing else may load.
 *
 * <p>Foresignal's channel renderer (018, {@code MarkdownRenderer}) is not reused here: it has no tables
 * and produces fragments for chat formats, not documents.
 */
@Component
public class BriefingHtmlRenderer {

    /** The {@code Content-Security-Policy} of every HTML rendition. */
    public static final String CONTENT_SECURITY_POLICY = "default-src 'none'; style-src 'unsafe-inline'; img-src 'self' data:";

    /** The allowlist. */
    static final PolicyFactory POLICY = new HtmlPolicyBuilder()
            .allowElements("h1", "h2", "h3", "h4", "h5", "h6", "p", "br", "hr", "ul", "ol", "li", "blockquote",
                    "table", "thead", "tbody", "tr", "th", "td", "code", "pre", "em", "strong", "del", "a")
            .allowAttributes("href").onElements("a")
            .allowAttributes("align").matching(true, "left", "right", "center").onElements("th", "td")
            .allowUrlProtocols("http", "https", "mailto")
            .requireRelsOnLinks("noopener", "nofollow")
            .toFactory();

    static final String STYLE = """
            :root{color-scheme:light dark;--fg:#1d1f23;--bg:#ffffff;--muted:#5b6170;--rule:#d9dce3;--link:#0b5cad;--code:#f2f4f7}
            @media (prefers-color-scheme:dark){:root{--fg:#e6e8ec;--bg:#14161a;--muted:#a3a9b6;--rule:#2f333b;--link:#7ab7ff;--code:#1f2228}}
            *{box-sizing:border-box}
            body{margin:0;background:var(--bg);color:var(--fg);font:16px/1.55 system-ui,-apple-system,"Segoe UI",Roboto,sans-serif}
            main{max-width:52rem;margin:0 auto;padding:1.5rem 1rem 3rem}
            h1{font-size:1.6rem;margin:.2rem 0 .6rem}h2{font-size:1.25rem;margin:1.8rem 0 .6rem;border-bottom:1px solid var(--rule);padding-bottom:.25rem}h3{font-size:1.05rem;margin:1.2rem 0 .4rem}
            a{color:var(--link)}em{color:var(--muted)}
            table{border-collapse:collapse;width:100%;margin:.6rem 0;font-variant-numeric:tabular-nums;display:block;overflow-x:auto}
            th,td{border:1px solid var(--rule);padding:.3rem .55rem;text-align:left}th[align=right],td[align=right]{text-align:right}
            code,pre{background:var(--code);border-radius:4px;font:.9em ui-monospace,SFMono-Regular,Menlo,monospace}code{padding:.1em .3em}pre{padding:.75rem;overflow-x:auto}
            blockquote{margin:1rem 0;padding:.25rem 1rem;border-left:3px solid var(--rule);color:var(--muted)}
            footer{max-width:52rem;margin:0 auto;padding:0 1rem 2rem;color:var(--muted);font-size:.85rem}
            aside.sources{border-top:1px solid var(--rule);margin-top:2rem;padding-top:.5rem;font-size:.9rem}aside.sources ol{padding-left:1.25rem}aside.sources li{margin:.35rem 0}
            .badge{display:inline-block;border:1px solid var(--rule);border-radius:999px;padding:0 .5rem;margin-left:.35rem;font-size:.78rem;color:var(--muted);white-space:nowrap}
            .badge.strong{border-color:#2e7d32;color:#2e7d32}.badge.weak,.badge.insufficient{border-color:#b26a00;color:#b26a00}
            .why{display:block;color:var(--muted);font-size:.82rem}
            """;

    private final List<Extension> extensions = List.of(TablesExtension.create());
    private final Parser parser = Parser.builder().extensions(extensions).build();
    private final HtmlRenderer renderer =
            HtmlRenderer.builder().extensions(extensions).escapeHtml(true).sanitizeUrls(true).build();

    /** {@code markdown} as sanitized HTML (a fragment). */
    public String fragment(String markdown) {
        Node document = parser.parse(markdown == null ? "" : markdown);
        return POLICY.sanitize(renderer.render(document));
    }

    /**
     * A whole page.
     *
     * @param title the page title (escaped here)
     * @param markdown the run's markdown
     * @param footer a plain-text footer line (escaped here), or null
     */
    public String page(String title, String markdown, String footer) {
        return page(title, markdown, footer, "en");
    }

    /**
     * A whole page in {@code language} (NP-004: an edition in its output language).
     *
     * @param title the page title (escaped here)
     * @param markdown the run's markdown
     * @param footer a plain-text footer line (escaped here), or null
     * @param language the page's BCP 47 {@code lang}; {@code en} when null or not a tag
     */
    public String page(String title, String markdown, String footer, String language) {
        return page(title, markdown, footer, language, null);
    }

    /**
     * A whole page in {@code language} with the run's provenance and corroboration (NP-007): when any of
     * {@code citations} carries {@code provenance} or {@code corroboration}, a "Sources" aside lists each annotated
     * item with compact badges (corroboration level, independent sources, the Admiralty grade) and its "why included"
     * line. Without annotations the page is exactly {@link #page(String, String, String, String)}'s.
     *
     * @param citations the run's {@code AskCitation} maps, or null
     */
    public String page(String title, String markdown, String footer, String language,
            List<Map<String, Object>> citations) {
        String lang = ai.foresignal.notify.NotificationTranslator.canonicalTag(language);
        StringBuilder out = new StringBuilder(4096);
        out.append("<!DOCTYPE html>\n<html lang=\"").append(lang == null ? "en" : HtmlUtils.htmlEscape(lang))
                .append("\"><head><meta charset=\"utf-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
                .append("<meta name=\"color-scheme\" content=\"light dark\">")
                .append("<meta name=\"referrer\" content=\"no-referrer\">")
                .append("<title>").append(HtmlUtils.htmlEscape(title == null ? "Briefing" : title)).append("</title>")
                .append("<style>").append(STYLE).append("</style></head><body><main>")
                .append(fragment(markdown))
                .append(sources(citations))
                .append("</main>");
        if (footer != null && !footer.isBlank()) {
            out.append("<footer>").append(HtmlUtils.htmlEscape(footer)).append("</footer>");
        }
        out.append("</body></html>\n");
        return out.toString();
    }

    /** The "Sources" aside of the annotated citations (NP-007), or empty when none is annotated. */
    static String sources(List<Map<String, Object>> citations) {
        if (citations == null) {
            return "";
        }
        StringBuilder items = new StringBuilder();
        for (Map<String, Object> citation : citations) {
            Object provenance = citation.get("provenance");
            Object corroboration = citation.get("corroboration");
            if (!(provenance instanceof Map<?, ?>) && !(corroboration instanceof Map<?, ?>)) {
                continue;
            }
            items.append("<li>").append(HtmlUtils.htmlEscape(String.valueOf(citation.getOrDefault("title", ""))));
            if (corroboration instanceof Map<?, ?> c) {
                String quality = String.valueOf(c.get("evidenceQuality"));
                items.append("<span class=\"badge ").append(HtmlUtils.htmlEscape(quality)).append("\">")
                        .append(HtmlUtils.htmlEscape(quality)).append("</span>");
                if (c.get("independentSources") instanceof Number n) {
                    items.append("<span class=\"badge\">").append(n.intValue())
                            .append(n.intValue() == 1 ? " source" : " sources").append("</span>");
                }
                if (c.get("reliability") instanceof String r && c.get("credibility") instanceof Number g) {
                    items.append("<span class=\"badge\">").append(HtmlUtils.htmlEscape(r)).append(g.intValue())
                            .append("</span>");
                }
            }
            if (provenance instanceof Map<?, ?> p && p.get("reason") instanceof String reason) {
                items.append("<span class=\"why\">Why included: ").append(HtmlUtils.htmlEscape(reason))
                        .append("</span>");
            }
            items.append("</li>");
        }
        return items.isEmpty() ? "" : "<aside class=\"sources\"><h2>Sources</h2><ol>" + items + "</ol></aside>";
    }
}
