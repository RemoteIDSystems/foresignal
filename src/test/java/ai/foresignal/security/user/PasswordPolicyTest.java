package ai.foresignal.security.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link PasswordPolicy} (prompt R2-002): at least 12 code points, not blank, at most 1,024. */
class PasswordPolicyTest {

    private final PasswordPolicy policy = new PasswordPolicy();

    @Test
    @DisplayName("short, blank, whitespace-only and null passwords are refused with PASSWORD_POLICY on the field")
    void refuses() {
        for (String bad : new String[] {null, "", "            ", "elevenchars"}) {
            assertThatThrownBy(() -> policy.validate(bad, "password")).isInstanceOfSatisfying(ValidationException.class,
                    e -> {
                        assertThat(e.getCode()).isEqualTo(ErrorCodes.PASSWORD_POLICY);
                        assertThat(e.getErrors()).singleElement().satisfies(f -> {
                            assertThat(f.field()).isEqualTo("password");
                            if (bad != null && !bad.isBlank()) {
                                assertThat(f.message()).doesNotContain(bad);
                            }
                        });
                    });
        }
    }

    @Test
    @DisplayName("12 characters pass, with no composition rules")
    void accepts() {
        assertThatCode(() -> policy.validate("twelve chars", "password")).doesNotThrowAnyException();
        assertThat(policy.problem("aaaaaaaaaaaa")).isNull();
    }

    @Test
    @DisplayName("length counts code points, and over 1,024 is refused")
    void codePoints() {
        String elevenEmoji = "🔒".repeat(11);
        assertThat(policy.problem(elevenEmoji)).contains("at least 12");
        assertThat(policy.problem(elevenEmoji + "x")).isNull();
        assertThat(policy.problem("a".repeat(PasswordPolicy.MAX_LENGTH))).isNull();
        assertThat(policy.problem("a".repeat(PasswordPolicy.MAX_LENGTH + 1))).contains("at most");
    }
}
