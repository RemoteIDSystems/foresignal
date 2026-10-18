package ai.foresignal.briefing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link SpeechText} (prompt 141): the spoken rendition, chunking for MP3 and truncation for Opus. */
class SpeechTextTest {

    @Test
    @DisplayName("the rendition: title first, links read as their labels, URLs dropped, tables read as cells")
    void rendition() {
        String markdown = """
                # Morning

                ## Notable
                - [M5.5 Tokyo Bay](https://fs.example/v1/records/q/1) at 07:00
                - see https://fs.example/v1/alerts/3 for details

                | Kind | New | Changed |
                |---|---:|---:|
                | quake | 3 | 1 |



                **Quiet** night.""";

        String text = SpeechText.of("Morning — 2026-10-18", markdown);

        assertThat(text).startsWith("Morning — 2026-10-18.\n\n");
        assertThat(text).contains("M5.5 Tokyo Bay at 07:00").contains("Kind, New, Changed.").contains("quake, 3, 1.")
                .contains("Quiet night.");
        assertThat(text).doesNotContain("https://").doesNotContain("](").doesNotContain("|").doesNotContain("---")
                .doesNotContain("**").doesNotContain("\n\n\n");
    }

    @Test
    @DisplayName("an empty run is just its title; no title gives just the text")
    void emptyParts() {
        assertThat(SpeechText.of("Title", "")).isEqualTo("Title.");
        assertThat(SpeechText.of(null, "Body text")).isEqualTo("Body text");
        assertThat(SpeechText.of(" ", null)).isEmpty();
    }

    @Test
    @DisplayName("chunks: each at most the limit, cut at paragraph breaks first, nothing lost")
    void chunksAtParagraphs() {
        String para = "Sentence one is here. ".repeat(10).strip();
        String text = String.join("\n\n", List.of(para, para, para, para, para));

        List<String> chunks = SpeechText.chunks(text, 500);

        assertThat(chunks).allSatisfy(c -> assertThat(c.length()).isLessThanOrEqualTo(500));
        assertThat(chunks).allSatisfy(c -> assertThat(c).endsWith("here."));
        assertThat(String.join(" ", chunks).replaceAll("\\s+", " "))
                .isEqualTo(text.replaceAll("\\s+", " "));
    }

    @Test
    @DisplayName("chunks: a paragraph longer than the limit is cut after a sentence, then at a space, then hard")
    void chunksFallbacks() {
        String sentences = "Short sentence number one. ".repeat(40).strip();
        assertThat(SpeechText.chunks(sentences, 200)).allSatisfy(c -> {
            assertThat(c.length()).isLessThanOrEqualTo(200);
            assertThat(c).endsWith(".");
        });
        String words = "word ".repeat(100).strip();
        assertThat(SpeechText.chunks(words, 120)).allSatisfy(c -> {
            assertThat(c.length()).isLessThanOrEqualTo(120);
            assertThat(c).doesNotStartWith(" ").doesNotEndWith(" ").doesNotContain("wo rd");
        });
        String solid = "x".repeat(450);
        assertThat(SpeechText.chunks(solid, 200)).extracting(String::length).containsExactly(200, 200, 50);
        assertThat(SpeechText.chunks("", 200)).isEmpty();
        assertThat(SpeechText.chunks("short", 4000)).containsExactly("short");
        assertThatThrownBy(() -> SpeechText.chunks("x", 10)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("chunks never split a surrogate pair")
    void surrogates() {
        String emoji = "🌋".repeat(150);
        assertThat(SpeechText.chunks(emoji, 101)).allSatisfy(c -> {
            assertThat(Character.isLowSurrogate(c.charAt(0))).isFalse();
            assertThat(Character.isHighSurrogate(c.charAt(c.length() - 1))).isFalse();
        });
    }

    @Test
    @DisplayName("truncate: within the limit including the spoken note; text that fits is unchanged")
    void truncate() {
        String text = "A sentence that repeats. ".repeat(400);

        String cut = SpeechText.truncate(text, 4000);

        assertThat(cut.length()).isLessThanOrEqualTo(4000);
        assertThat(cut).endsWith(SpeechText.TRUNCATION_NOTE);
        assertThat(cut).contains("repeats.\n\n" + SpeechText.TRUNCATION_NOTE);
        assertThat(SpeechText.truncate("fits", 4000)).isEqualTo("fits");
        assertThat(SpeechText.exceeds(text, 4000)).isTrue();
        assertThat(SpeechText.exceeds("fits", 4000)).isFalse();
        assertThatThrownBy(() -> SpeechText.truncate(text, 60)).isInstanceOf(IllegalArgumentException.class);
        assertThat(SpeechText.withNote("Short.", 4000)).isEqualTo("Short.\n\n" + SpeechText.TRUNCATION_NOTE);
        assertThat(SpeechText.withNote(text, 1000)).hasSizeLessThanOrEqualTo(1000).endsWith(SpeechText.TRUNCATION_NOTE);
        assertThatThrownBy(() -> SpeechText.withNote(text, 60)).isInstanceOf(IllegalArgumentException.class);
    }
}
