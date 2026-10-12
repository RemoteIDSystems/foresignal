package ai.foresignal.notify;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.Truncator.Mode;
import ai.foresignal.notify.Truncator.Unit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link Truncator} (prompt 018). */
class TruncatorTest {

    @Test
    @DisplayName("text within the limit is returned unchanged; null stays null")
    void untouched() {
        assertThat(Truncator.truncate("short", 10, Unit.CHARS, Mode.PLAIN, "https://x")).isEqualTo("short");
        assertThat(Truncator.truncate(null, 10, Unit.CHARS, Mode.PLAIN, null)).isNull();
    }

    @Test
    @DisplayName("cut text ends with an ellipsis and, when it fits, the link; the result never exceeds the limit")
    void ellipsisAndLink() {
        String text = "a".repeat(100);
        String cut = Truncator.truncate(text, 40, Unit.CHARS, Mode.PLAIN, "https://x.example");
        assertThat(cut).hasSizeLessThanOrEqualTo(40).endsWith("… https://x.example");
        String noRoom = Truncator.truncate(text, 10, Unit.CHARS, Mode.PLAIN, "https://a-very-long-link.example/path");
        assertThat(noRoom).hasSizeLessThanOrEqualTo(10).endsWith("…").doesNotContain("https");
        assertThat(Truncator.truncate(text, 20, Unit.CHARS, Mode.MARKDOWN, "u")).endsWith("… [more](u)");
    }

    @Test
    @DisplayName("graphemes are never split: emoji with modifiers and combining marks stay whole")
    void graphemes() {
        String family = "👩‍👩‍👧‍👦";
        String text = "ab" + family + family + family;
        String cut = Truncator.truncate(text, 2 + family.length() + 2, Unit.CHARS, Mode.PLAIN, null);
        assertThat(cut).isEqualTo("ab" + family + "…");
        String combining = "éééé";
        String cutCombining = Truncator.truncate(combining, 4, Unit.CHARS, Mode.PLAIN, null);
        assertThat(cutCombining).isEqualTo("é…");
    }

    @Test
    @DisplayName("bytes and characters differ: multibyte text is cut by its UTF-8 size")
    void bytesVersusChars() {
        String text = "éééééééééé";
        assertThat(Truncator.truncate(text, 10, Unit.CHARS, Mode.PLAIN, null)).isEqualTo(text);
        String byBytes = Truncator.truncate(text, 10, Unit.UTF8_BYTES, Mode.PLAIN, null);
        assertThat(Truncator.size(byBytes, Unit.UTF8_BYTES)).isLessThanOrEqualTo(10);
        assertThat(byBytes).isEqualTo("ééé…");
    }

    @Test
    @DisplayName("HTML is never cut inside a tag or entity, and open tags are closed")
    void htmlBoundaries() {
        String html = "<b>bold &amp; more text here</b>";
        for (int limit = 4; limit < html.length(); limit++) {
            String cut = Truncator.truncate(html, limit, Unit.CHARS, Mode.HTML, null);
            assertThat(cut.length()).as("limit " + limit).isLessThanOrEqualTo(limit);
            assertThat(cut).doesNotContain("&am…").doesNotContain("<b…").doesNotContain("</…");
            if (cut.contains("<b>")) {
                assertThat(cut).contains("</b>");
            }
        }
        assertThat(Truncator.truncate(html, 18, Unit.CHARS, Mode.HTML, null)).isEqualTo("<b>bold &amp;</b>…");
        assertThat(Truncator.truncate(html, 20, Unit.CHARS, Mode.HTML, null)).isEqualTo("<b>bold &amp; m</b>…");
    }

    @Test
    @DisplayName("an HTML link suffix is an anchor with an escaped URL")
    void htmlLink() {
        String cut = Truncator.truncate("x".repeat(200), 80, Unit.CHARS, Mode.HTML, "https://e.example/?a=1&b=2");
        assertThat(cut).endsWith("… <a href=\"https://e.example/?a=1&amp;b=2\">more</a>").hasSizeLessThanOrEqualTo(80);
    }

    @Test
    @DisplayName("closing tags ignore void and self-closing elements")
    void closingTags() {
        assertThat(Truncator.closingTags("<i>a<br>b<b>c</b><img src=x/>")).isEqualTo("</i>");
        assertThat(Truncator.closingTags("<a href=\"x\"><b>t")).isEqualTo("</b></a>");
    }
}
