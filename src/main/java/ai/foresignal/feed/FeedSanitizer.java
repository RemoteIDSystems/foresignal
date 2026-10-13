package ai.foresignal.feed;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.text.TextContentRenderer;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Component;

/**
 * Markdown to feed content (prompt 058): Atom {@code content type="html"} and JSON Feed
 * {@code content_html} hold the markdown rendered with commonmark-java and then sanitized by an OWASP
 * allowlist; {@code content_text} holds plain text.
 *
 * <p>Two layers keep upstream text inert. CommonMark escapes raw HTML instead of passing it through
 * ({@code escapeHtml}) and drops unsafe link targets ({@code sanitizeUrls}); the allowlist then keeps only
 * {@code p, br, strong, em, code, pre, ul, ol, li, blockquote, h3–h6} and {@code a[href]} with
 * {@code http}/{@code https} targets and {@code rel="nofollow noopener"}. No script, event handler,
 * {@code style}, image or {@code javascript:} link can survive; other elements keep their text only.
 */
@Component
public class FeedSanitizer {

    /** The allowlist. */
    static final PolicyFactory POLICY = new HtmlPolicyBuilder()
            .allowElements("p", "br", "strong", "em", "code", "pre", "ul", "ol", "li", "blockquote",
                    "h3", "h4", "h5", "h6", "a")
            .allowAttributes("href").onElements("a")
            .allowUrlProtocols("http", "https")
            .requireRelsOnLinks("nofollow", "noopener")
            .toFactory();

    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer html = HtmlRenderer.builder().escapeHtml(true).sanitizeUrls(true).build();
    private final TextContentRenderer text = TextContentRenderer.builder().build();

    /** {@code markdown} as allowlisted HTML; null becomes empty. */
    public String html(String markdown) {
        Node document = parser.parse(XmlText.clean(markdown == null ? "" : markdown));
        return POLICY.sanitize(html.render(document)).strip();
    }

    /** {@code markdown} as plain text; null becomes empty. */
    public String text(String markdown) {
        Node document = parser.parse(XmlText.clean(markdown == null ? "" : markdown));
        return text.render(document).strip();
    }
}
