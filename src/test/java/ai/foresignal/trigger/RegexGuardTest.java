package ai.foresignal.trigger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** {@link RegexGuard} and {@link DeadlineCharSequence} (prompt 017). */
class RegexGuardTest {

    @ParameterizedTest
    @ValueSource(strings = {"^N[0-9]+$", "a|b", "(?:ab)+", "\\d{3}\\s\\w*", "a+?", "x{2,5}", "\\.", "(a)(b)", "[a\\]]"})
    @DisplayName("ordinary patterns compile")
    void accepted(String regex) {
        assertThat(RegexGuard.compile(regex).pattern()).isEqualTo(regex);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "(a)\\1", "\\k<x>", "(?=a)", "(?!a)", "(?<=a)b", "(?<!a)b", "(?<n>a)", "(?i)a", "(?>a)",
        "(a+)+", "(a*)*", "(a{2,})+", "((a+)b)*", "a*+", "a++", "a?+", "a{2}+", "\\bword", "\\p{L}",
        "[a[b]]", "[a&&b]", "(", ")", "[abc", "a{2", "\\", ""
    })
    @DisplayName("backreferences, lookaround, other (? constructs, nested and possessive quantifiers and Java-only syntax are refused")
    void refused(String regex) {
        assertThatThrownBy(() -> RegexGuard.compile(regex)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("at most 256 characters")
    void length() {
        assertThat(RegexGuard.compile("a".repeat(256))).isNotNull();
        assertThatThrownBy(() -> RegexGuard.compile("a".repeat(257))).hasMessageContaining("256");
    }

    @Test
    @DisplayName("a match over a DeadlineCharSequence stops once the deadline passes")
    void deadline() {
        DeadlineCharSequence text = new DeadlineCharSequence("x".repeat(10_000), 0);
        assertThatThrownBy(() -> {
            for (int i = 0; i < text.length(); i++) {
                text.charAt(i);
            }
        }).isInstanceOf(DeadlineCharSequence.Timeout.class);
        DeadlineCharSequence roomy = new DeadlineCharSequence("abc", 1_000_000_000L);
        assertThat(roomy.subSequence(1, 3).toString()).isEqualTo("bc");
        assertThat(roomy.length()).isEqualTo(3);
    }
}
