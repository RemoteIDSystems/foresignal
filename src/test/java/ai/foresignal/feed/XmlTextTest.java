package ai.foresignal.feed;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** XML 1.0 cleaning and HTML escaping shared by Atom and KML (prompt 058). */
class XmlTextTest {

    @Test
    @DisplayName("characters XML 1.0 forbids are dropped; tab, newline, carriage return and pairs survive")
    void clean() {
        assertThat(XmlText.clean("a\u0000b\u0008c\u000Bd￾e￿")).isEqualTo("abcde");
        assertThat(XmlText.clean("x\ty\nz\r")).isEqualTo("x\ty\nz\r");
        assertThat(XmlText.clean("😀")).isEqualTo("😀");
        assertThat(XmlText.clean("lone\uD83D")).isEqualTo("lone");
        assertThat(XmlText.clean("\uDE00lone")).isEqualTo("lone");
        assertThat(XmlText.clean(null)).isNull();
        String plain = "unchanged";
        assertThat(XmlText.clean(plain)).isSameAs(plain);
    }

    @Test
    @DisplayName("html escapes <, >, & and quotes after cleaning; null is empty")
    void html() {
        assertThat(XmlText.html("<b>\"A&B\"</b>\u0001")).isEqualTo("&lt;b&gt;&quot;A&amp;B&quot;&lt;/b&gt;");
        assertThat(XmlText.html(null)).isEmpty();
    }
}
