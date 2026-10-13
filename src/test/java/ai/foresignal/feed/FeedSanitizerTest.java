package ai.foresignal.feed;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Markdown to feed HTML (prompt 058): the allowlist neutralizes scripts, handlers, unsafe links and raw HTML. */
class FeedSanitizerTest {

    private final FeedSanitizer sanitizer = new FeedSanitizer();

    @Test
    @DisplayName("allowed markdown survives: paragraphs, emphasis, code, lists, quotes, h3 and http links with rel")
    void allowed() {
        String html = sanitizer.html("""
                ### Heading

                Some **bold**, *em* and `code`.

                - one
                - two

                > quoted

                [link](https://example.com/a?b=1)
                """);
        assertThat(html).contains("<h3>Heading</h3>", "<strong>bold</strong>", "<em>em</em>", "<code>code</code>",
                "<ul>", "<li>one</li>", "<blockquote>");
        assertThat(html).contains("href=\"https://example.com/a?b")
                .containsPattern("<a [^>]*rel=\"[^\"]*nofollow[^\"]*\"")
                .containsPattern("<a [^>]*rel=\"[^\"]*noopener[^\"]*\"");
    }

    @Test
    @DisplayName("a <script> in the markdown is escaped text, never an element")
    void script() {
        String html = sanitizer.html("Hello <script>alert(1)</script> world");
        assertThat(html).doesNotContain("<script");
        assertThat(html).contains("&lt;script&gt;");
    }

    @Test
    @DisplayName("javascript: and data: links lose their href")
    void javascriptLinks() {
        String html = sanitizer.html("[x](javascript:alert(1)) and [y](data:text/html,hi)");
        assertThat(html).doesNotContain("javascript:").doesNotContain("data:text");
    }

    @Test
    @DisplayName("event-handler attributes and raw HTML elements never pass")
    void handlersAndRawHtml() {
        String html = sanitizer.html("<img src=x onerror=alert(1)>\n\n<a href=\"https://e.com\" onclick=\"x()\">a</a>"
                + "\n\n<div style=\"color:red\">d</div><iframe src=\"https://e.com\"></iframe>");
        assertThat(html).doesNotContain("<img").doesNotContain("<iframe").doesNotContain("<div")
                .doesNotContain("onerror=").doesNotContain("onclick=").doesNotContain("style=");
    }

    @Test
    @DisplayName("h1 and h2 keep their text but not the element; images are dropped")
    void outsideTheAllowlist() {
        String html = sanitizer.html("# Big\n\n## Second\n\n![alt](https://e.com/i.png)");
        assertThat(html).doesNotContain("<h1").doesNotContain("<h2").doesNotContain("<img").contains("Big", "Second");
    }

    @Test
    @DisplayName("plain text drops markup; null is empty; control characters are removed")
    void text() {
        assertThat(sanitizer.text("**Bold** and [link](https://e.com)")).contains("Bold").doesNotContain("**");
        assertThat(sanitizer.text(null)).isEmpty();
        assertThat(sanitizer.html(null)).isEmpty();
        assertThat(sanitizer.html("a\u0001b")).contains("ab");
    }
}
