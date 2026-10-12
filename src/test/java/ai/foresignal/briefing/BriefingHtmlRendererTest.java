package ai.foresignal.briefing;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link BriefingHtmlRenderer} (prompt 029): tables, links with {@code rel="noopener nofollow"}, and
 * nothing active surviving from markdown or record text.
 */
class BriefingHtmlRendererTest {

    final BriefingHtmlRenderer renderer = new BriefingHtmlRenderer();

    @Test
    @DisplayName("headings, tables, emphasis and code survive; links get rel=noopener nofollow")
    void allowed() {
        String html = renderer.fragment("# Title\n\n| Kind | New |\n| --- | ---: |\n| quake | 3 |\n\n"
                + "**bold** *em* `code`\n\n- [record](https://fs.example/v1/records/a/b)\n");
        assertThat(html).contains("<h1>Title</h1>").contains("<table>").contains("<th>Kind</th>")
                .contains("<td align=\"right\">3</td>").contains("<strong>bold</strong>").contains("<em>em</em>")
                .contains("<code>code</code>").contains("<li>")
                .contains("href=\"https://fs.example/v1/records/a/b\"");
        assertThat(html).containsPattern("<a [^>]*rel=\"[^\"]*noopener[^\"]*\"")
                .containsPattern("<a [^>]*rel=\"[^\"]*nofollow[^\"]*\"");
    }

    @Test
    @DisplayName("raw HTML, event handlers, javascript: links, styles and iframes never survive")
    void stripped() {
        String html = renderer.fragment("<script>alert(1)</script>\n\n<img src=x onerror=alert(2)>\n\n"
                + "<iframe src=\"https://evil.example\"></iframe>\n\n[click](javascript:alert(3))\n\n"
                + "<p style=\"color:red\" onclick=\"x()\">styled</p>\n\nText <b onmouseover=\"y()\">inline</b>");
        assertThat(html).doesNotContain("<script").doesNotContain("<img").doesNotContain("<iframe")
                .doesNotContain("javascript:").doesNotContain("<b ").doesNotContain("<p style");
        assertThat(html).doesNotContainPattern("<[^>]*\\son[a-z]+\\s*=").doesNotContainPattern("<[^>]*\\sstyle\\s*=");
        assertThat(html).contains("&lt;script&gt;");
    }

    @Test
    @DisplayName("a page has a title, the inline light/dark stylesheet and an escaped footer")
    void page() {
        String page = renderer.page("Brief <b>", "# Hi", "run <1>");
        assertThat(page).startsWith("<!DOCTYPE html>").contains("<title>Brief &lt;b&gt;</title>")
                .contains("prefers-color-scheme:dark").contains("<h1>Hi</h1>").contains("<footer>run &lt;1&gt;</footer>")
                .doesNotContain("<script");
        assertThat(BriefingHtmlRenderer.CONTENT_SECURITY_POLICY)
                .isEqualTo("default-src 'none'; style-src 'unsafe-inline'; img-src 'self' data:");
    }
}
