package ai.foresignal.source.adapters.radio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** {@link Maidenhead} (prompt 082): 4-, 6- and 8-character locators, case, malformed input and the lenient prefix. */
class MaidenheadTest {

    @Test
    @DisplayName("FN31: a 2 x 1 degree square west -74, south 41, center (-73, 41.5)")
    void square() {
        Maidenhead.Cell cell = Maidenhead.decode("FN31");

        assertThat(cell.west()).isEqualTo(-74.0);
        assertThat(cell.south()).isEqualTo(41.0);
        assertThat(cell.centerLon()).isEqualTo(-73.0);
        assertThat(cell.centerLat()).isEqualTo(41.5);
        assertThat(cell.east()).isEqualTo(-72.0);
        assertThat(cell.north()).isEqualTo(42.0);
    }

    @Test
    @DisplayName("FN31pr (any case): a 5' x 2.5' subsquare centered at (-72.708333, 41.729167)")
    void subsquare() {
        Maidenhead.Cell cell = Maidenhead.decode("fn31PR");

        assertThat(cell.locator()).isEqualTo("FN31pr");
        assertThat(cell.centerLon()).isCloseTo(-72.708333, within(1e-6));
        assertThat(cell.centerLat()).isCloseTo(41.729167, within(1e-6));
        assertThat(cell.widthDeg()).isCloseTo(2.0 / 24, within(1e-12));
        assertThat(cell.halfDiagonalMeters()).isCloseTo(4162.0, within(1.0));
    }

    @Test
    @DisplayName("FN31pr21: the extended square is a tenth of the subsquare")
    void extended() {
        Maidenhead.Cell cell = Maidenhead.decode("FN31pr21");

        assertThat(cell.widthDeg()).isCloseTo(2.0 / 240, within(1e-12));
        assertThat(cell.west()).isCloseTo(-72.75 + 2 * (2.0 / 240), within(1e-9));
        assertThat(cell.south()).isCloseTo(41.0 + 17.0 / 24 + 1.0 / 240, within(1e-9));
    }

    @Test
    @DisplayName("the corners of the grid: AA00 and RR99xx99")
    void corners() {
        assertThat(Maidenhead.decode("AA00").west()).isEqualTo(-180.0);
        assertThat(Maidenhead.decode("AA00").south()).isEqualTo(-90.0);
        assertThat(Maidenhead.decode("RR99xx99").east()).isCloseTo(180.0, within(1e-9));
        assertThat(Maidenhead.decode("RR99xx99").north()).isCloseTo(90.0, within(1e-9));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "F", "FN3", "FN31p", "SN31", "FNA1", "FN31py", "FN31pr2", "FN31prA1", "FN31pr21x", "12AB"})
    @DisplayName("malformed locators are null")
    void malformed(String locator) {
        assertThat(Maidenhead.decode(locator)).isNull();
    }

    @Test
    @DisplayName("the lenient form reads PSK Reporter's 10-character locators to 8 characters, and 4 when only that is valid")
    void lenient() {
        assertThat(Maidenhead.decodeLenient("CM88qg60fk").locator()).isEqualTo("CM88qg60");
        assertThat(Maidenhead.decodeLenient("FN20UP").locator()).isEqualTo("FN20up");
        assertThat(Maidenhead.decodeLenient("FN31zz").locator()).isEqualTo("FN31");
        assertThat(Maidenhead.decodeLenient("ZZ99")).isNull();
        assertThat(Maidenhead.decodeLenient(null)).isNull();
        assertThat(Maidenhead.decode(null)).isNull();
    }
}
