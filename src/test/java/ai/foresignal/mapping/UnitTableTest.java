package ai.foresignal.mapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.function.DoubleUnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** {@link Units}: every row of the built-in table, with known values (prompt 059). */
class UnitTableTest {

    static double convert(String from, String to, double value) {
        DoubleUnaryOperator op = Units.conversion(from, to).orElseThrow();
        return op.applyAsDouble(value);
    }

    @ParameterizedTest(name = "{1} {0} = {3} {2}")
    @CsvSource(delimiter = '|', value = {
        "[degF]|32|Cel|0",
        "[degF]|212|Cel|100",
        "[degF]|-40|Cel|-40",
        "[mi_i]/h|1|m/s|0.44704",
        "km/h|36|m/s|10",
        "[kn_i]|1|m/s|0.5144444444444445",
        "[in_i]|1|mm|25.4",
        "[in_i]/h|0.5|mm/h|12.7",
        "[in_i'Hg]|29.92|hPa|1013.207",
        "Pa|101325|hPa|1013.25",
        "[mi_i]|1|km|1.609344",
        "[ft_i]|1000|m|304.8",
    })
    @DisplayName("each conversion of the table gives the known value")
    void conversions(String from, double value, String to, double expected) {
        assertThat(convert(from, to, value)).isCloseTo(expected, within(1e-3));
    }

    @ParameterizedTest(name = "{0} is unchanged")
    @CsvSource({"W/m2", "%", "uSv/h", "/min", "Cel", "m/s", "hPa"})
    @DisplayName("a known unit converts to itself unchanged")
    void identity(String code) {
        assertThat(convert(code, code, 12.5)).isEqualTo(12.5);
    }

    @Test
    @DisplayName("a pair outside the table, or an unknown code, has no conversion")
    void unknownPairs() {
        assertThat(Units.conversion("Cel", "[degF]")).isEmpty();
        assertThat(Units.conversion("m/s", "km/h")).isEmpty();
        assertThat(Units.conversion("[degF]", "K")).isEmpty();
        assertThat(Units.conversion("furlong", "m")).isEmpty();
        assertThat(Units.conversion(null, "m")).isEmpty();
        assertThat(Units.known("K")).isFalse();
        assertThat(Units.known("[in_i'Hg]")).isTrue();
    }

    @Test
    @DisplayName("the table lists exactly the ten conversions of the prompt")
    void pairs() {
        assertThat(Units.pairs()).containsExactlyInAnyOrder("[degF]->Cel", "[mi_i]/h->m/s", "km/h->m/s", "[kn_i]->m/s",
                "[in_i]->mm", "[in_i]/h->mm/h", "[in_i'Hg]->hPa", "Pa->hPa", "[mi_i]->km", "[ft_i]->m");
        assertThat(Units.CODES).hasSize(21);
    }
}
