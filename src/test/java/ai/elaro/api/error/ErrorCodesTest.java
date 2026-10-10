package ai.elaro.api.error;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ErrorCodesTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
        "NOT_FOUND, https://elaro.ai/problems/not-found",
        "VALIDATION_FAILED, https://elaro.ai/problems/validation-failed",
        "BUDGET_EXCEEDED, https://elaro.ai/problems/budget-exceeded",
        "CREDENTIALS_REQUIRED, https://elaro.ai/problems/credentials-required",
        "UNAUTHORIZED, https://elaro.ai/problems/unauthorized",
        "INTERNAL_ERROR, https://elaro.ai/problems/internal-error",
    })
    void buildsTypeUris(String code, String expected) {
        assertThat(ErrorCodes.typeUri(code)).hasToString(expected);
    }

    @Test
    @DisplayName("underscores become hyphens and case is lowered")
    void kebabCases() {
        assertThat(ErrorCodes.toKebabCase("ALREADY_RUNNING")).isEqualTo("already-running");
        assertThat(ErrorCodes.toKebabCase("IN_BACKOFF")).isEqualTo("in-backoff");
        assertThat(ErrorCodes.toKebabCase("SIMPLE")).isEqualTo("simple");
    }

    @Test
    void handlesNullAndEmpty() {
        assertThat(ErrorCodes.toKebabCase(null)).isEmpty();
        assertThat(ErrorCodes.toKebabCase("")).isEmpty();
    }

    @Test
    @DisplayName("the type URI is absolute, as RFC 9457 expects")
    void typeUriIsAbsolute() {
        assertThat(ErrorCodes.typeUri(ErrorCodes.NOT_FOUND).isAbsolute()).isTrue();
    }
}
