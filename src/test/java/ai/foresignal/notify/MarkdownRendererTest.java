package ai.foresignal.notify;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.MarkdownRenderer.Format;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link MarkdownRenderer} (prompt 018). */
class MarkdownRendererTest {

    static final String MD = "## Head\n**b** *i* _u_ ~~s~~ `c<d>` [l](https://x.example/a_b)\n- item & <x>\n```\nraw *x*\n```";

    @Test
    @DisplayName("plain text drops markers and spells links out")
    void plain() {
        assertThat(MarkdownRenderer.render(MD, Format.PLAIN))
                .isEqualTo("Head\nb i u s c<d> l (https://x.example/a_b)\n• item & <x>\nraw *x*");
    }

    @Test
    @DisplayName("Slack mrkdwn escapes & < > and uses <url|label> links")
    void slack() {
        assertThat(MarkdownRenderer.render(MD, Format.SLACK)).isEqualTo(
                "*Head*\n*b* _i_ _u_ ~s~ `c&lt;d&gt;` <https://x.example/a_b|l>\n• item &amp; &lt;x&gt;\n```\nraw *x*\n```");
    }

    @Test
    @DisplayName("Telegram HTML uses only allowed tags and escapes entities")
    void telegram() {
        assertThat(MarkdownRenderer.render(MD, Format.TELEGRAM_HTML)).isEqualTo(
                "<b>Head</b>\n<b>b</b> <i>i</i> <i>u</i> <s>s</s> <code>c&lt;d&gt;</code> "
                        + "<a href=\"https://x.example/a_b\">l</a>\n• item &amp; &lt;x&gt;\n<pre>raw *x*</pre>");
    }

    @Test
    @DisplayName("Matrix HTML uses headings, lists and del")
    void matrix() {
        assertThat(MarkdownRenderer.render(MD, Format.MATRIX_HTML)).isEqualTo(
                "<h4>Head</h4><strong>b</strong> <em>i</em> <em>u</em> <del>s</del> <code>c&lt;d&gt;</code> "
                        + "<a href=\"https://x.example/a_b\">l</a><br><ul><li>item &amp; &lt;x&gt;</li></ul>"
                        + "<pre><code>raw *x*</code></pre>");
    }

    @Test
    @DisplayName("Discord and Zulip backslash-escape markdown in text")
    void discordAndZulip() {
        assertThat(MarkdownRenderer.render("a_b *x* [y]", Format.DISCORD)).isEqualTo("a\\_b *x* \\[y\\]");
        assertThat(MarkdownRenderer.render("### T\n- a", Format.ZULIP)).isEqualTo("### T\n- a");
    }

    @Test
    @DisplayName("Adaptive Card keeps bold, italic, links and bullets; drops strike")
    void adaptive() {
        assertThat(MarkdownRenderer.render("**b** _i_ ~~s~~ [l](u)\n- x", Format.ADAPTIVE_CARD))
                .isEqualTo("**b** _i_ s [l](u)\n- x");
    }

    @Test
    @DisplayName("email and Google Chat HTML")
    void emailAndGoogleChat() {
        assertThat(MarkdownRenderer.render("~~s~~ & <x>", Format.EMAIL_HTML)).isEqualTo("<s>s</s> &amp; &lt;x&gt;");
        assertThat(MarkdownRenderer.render("**b**\n- x", Format.GOOGLE_CHAT)).isEqualTo("<b>b</b><br>• x");
    }

    @Test
    @DisplayName("snake_case, lone markers, escapes and emoji stay text")
    void edgeCases() {
        assertThat(MarkdownRenderer.render("snake_case_name 2*3 \\*lit\\* 🔥 * x", Format.TELEGRAM_HTML))
                .isEqualTo("snake_case_name 2*3 *lit* 🔥 * x");
        assertThat(MarkdownRenderer.render(null, Format.PLAIN)).isEmpty();
        assertThat(MarkdownRenderer.escapeMarkdown("a*b_c")).isEqualTo("a\\*b\\_c");
        assertThat(MarkdownRenderer.render(MarkdownRenderer.escapeMarkdown("*not bold*"), Format.PLAIN)).isEqualTo("*not bold*");
    }
}
