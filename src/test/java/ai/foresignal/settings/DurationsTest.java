package ai.foresignal.settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.InvalidInputException;
import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class DurationsTest {

    // ---------- parse ----------

    @ParameterizedTest(name = "parse(\"{0}\") == {1}s")
    @CsvSource({
        // shorthand
        "30s, 30",
        "0s, 0",
        "15m, 900",
        "90m, 5400",
        "12h, 43200",
        "7d, 604800",
        "1w, 604800",
        "2w, 1209600",
        "3650d, 315360000",
        // ISO-8601
        "PT15M, 900",
        "P7D, 604800",
        "PT1H30M, 5400",
        "PT45S, 45",
        "PT0S, 0",
        "P1DT2H3M4S, 93784",
    })
    void parsesBothSpellings(String text, long expectedSeconds) {
        assertThat(Durations.parse(text)).isEqualTo(Duration.ofSeconds(expectedSeconds));
    }

    @ParameterizedTest
    @CsvSource({"7D, 604800", "15M, 900", "12H, 43200", "30S, 30", "1W, 604800"})
    @DisplayName("shorthand units are case-insensitive")
    void shorthandIsCaseInsensitive(String text, long expectedSeconds) {
        assertThat(Durations.parse(text)).isEqualTo(Duration.ofSeconds(expectedSeconds));
    }

    @ParameterizedTest
    @ValueSource(strings = {" 7d", "7d ", "  15m  ", " PT15M "})
    @DisplayName("surrounding whitespace is ignored")
    void trimsWhitespace(String text) {
        assertThat(Durations.parse(text)).isPositive();
    }

    @Test
    @DisplayName("whitespace between amount and unit is allowed")
    void allowsSpaceBeforeUnit() {
        assertThat(Durations.parse("7 d")).isEqualTo(Duration.ofDays(7));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "",
                "   ",
                "7",
                "d",
                "7x",
                "seven days",
                "7d5h",
                "1.5h",
                "P",
                "PT",
                "-7d",
                "7 days",
                "0x10s",
                "++7d",
            })
    @DisplayName("garbage is rejected with a helpful message")
    void rejectsGarbage(String text) {
        assertThatThrownBy(() -> Durations.parse(text))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> Durations.parse(null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("required");
    }

    @Test
    @DisplayName("the error message names both accepted spellings")
    void errorMessageExplainsBothSpellings() {
        assertThatThrownBy(() -> Durations.parse("nonsense"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("ISO-8601")
                .hasMessageContaining("shorthand");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-PT15M", "-P7D", "PT-15M"})
    @DisplayName("negative ISO durations are rejected")
    void rejectsNegativeIso(String text) {
        assertThatThrownBy(() -> Durations.parse(text))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("negative");
    }

    @Test
    void rejectsAmountsTooLargeToRepresent() {
        assertThatThrownBy(() -> Durations.parse("9223372036854775807d"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("too large");
    }

    // ---------- parsePositive ----------

    @Test
    void parsePositiveAcceptsPositive() {
        assertThat(Durations.parsePositive("15m")).isEqualTo(Duration.ofMinutes(15));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0s", "0d", "PT0S"})
    @DisplayName("parsePositive rejects zero")
    void parsePositiveRejectsZero(String text) {
        assertThatThrownBy(() -> Durations.parsePositive(text))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("greater than zero");
    }

    @Test
    @DisplayName("parse allows zero, parsePositive does not")
    void parseAndParsePositiveDifferOnZero() {
        assertThat(Durations.parse("0s")).isZero();
        assertThatThrownBy(() -> Durations.parsePositive("0s"))
                .isInstanceOf(InvalidInputException.class);
    }

    // ---------- format ----------

    @ParameterizedTest(name = "format({0}s) == \"{1}\"")
    @CsvSource({
        "0, 0s",
        "45, 45s",
        "59, 59s",
        "60, 1m",
        "5400, 90m",
        "900, 15m",
        "3600, 1h",
        "43200, 12h",
        "86400, 1d",
        "604800, 7d",
        "315360000, 3650d",
        "61, 61s",
        "3661, 3661s",
    })
    void formatsShortestExactShorthand(long seconds, String expected) {
        assertThat(Durations.format(Duration.ofSeconds(seconds))).isEqualTo(expected);
    }

    @Test
    @DisplayName("seven days formats as 7d, never 1w")
    void neverEmitsWeeks() {
        assertThat(Durations.format(Duration.ofDays(7))).isEqualTo("7d");
        assertThat(Durations.format(Duration.ofDays(14))).isEqualTo("14d");
    }

    @Test
    @DisplayName("sub-second precision falls back to ISO-8601 so it survives a round trip")
    void fallsBackToIsoForSubSecond() {
        Duration millis = Duration.ofMillis(1500);

        String formatted = Durations.format(millis);

        assertThat(formatted).isEqualTo("PT1.5S");
        assertThat(Duration.parse(formatted)).isEqualTo(millis);
    }

    @Test
    void formatRejectsNull() {
        assertThatThrownBy(() -> Durations.format(null))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void formatRejectsNegative() {
        assertThatThrownBy(() -> Durations.format(Duration.ofSeconds(-1)))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("negative");
    }

    // ---------- round trips ----------

    static Stream<Arguments> roundTripDurations() {
        return Stream.of(
                        Duration.ZERO,
                        Duration.ofSeconds(45),
                        Duration.ofSeconds(61),
                        Duration.ofMinutes(15),
                        Duration.ofMinutes(90),
                        Duration.ofHours(12),
                        Duration.ofDays(1),
                        Duration.ofDays(7),
                        Duration.ofDays(30),
                        Duration.ofDays(90),
                        Duration.ofDays(3650),
                        Duration.ofMillis(1500))
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("roundTripDurations")
    @DisplayName("format then parse returns the original duration")
    void roundTripsThroughFormat(Duration original) {
        assertThat(Durations.parse(Durations.format(original))).isEqualTo(original);
    }

    @ParameterizedTest
    @ValueSource(strings = {"30s", "15m", "90m", "12h", "7d", "3650d", "0s"})
    @DisplayName("parse then format returns the original text")
    void roundTripsThroughParse(String original) {
        assertThat(Durations.format(Durations.parse(original))).isEqualTo(original);
    }

    @Test
    @DisplayName("1w parses but normalizes to 7d, which is the canonical spelling")
    void weekNormalizesToDays() {
        assertThat(Durations.format(Durations.parse("1w"))).isEqualTo("7d");
    }
}
