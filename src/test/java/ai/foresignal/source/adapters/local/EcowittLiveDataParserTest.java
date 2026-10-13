package ai.foresignal.source.adapters.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * {@link EcowittLiveDataParser} (prompt 069): known ids in metric and imperial units, units read from the
 * {@code unit} member or the value's suffix, unknown ids counted, indoor values only on request, and garbage
 * as a format error.
 */
class EcowittLiveDataParserTest {

    @Test
    @DisplayName("metric live data: every known id stored, ignored ids skipped, one unknown id counted")
    void metric() {
        EcowittLiveDataParser.Result r = EcowittLiveDataParser.parse(
                AdapterFixtures.bytes("local-receivers", "ecowitt-livedata.json"), "wxstation-ecowitt-1", 40.0, -105.0, false);
        assertThat(r.unknownIds()).isEqualTo(1);
        assertThat(r.record().externalId()).isEqualTo("wxstation-ecowitt-1");
        assertThat(r.record().observedAt()).as("RECEIVED: imputed by the framework").isNull();
        assertThat(r.record().payload())
                .containsEntry("tempC", 18.4).containsEntry("rh", 62.0).containsEntry("dewPointC", 11.1)
                .containsEntry("windSpeedMs", 2.3).containsEntry("windGustMs", 4.1).containsEntry("windDirDeg", 225.0)
                .containsEntry("solarWm2", 345.7).containsEntry("uvIndex", 3.0)
                .containsEntry("rainRateMmH", 1.2).containsEntry("rainDailyMm", 4.6)
                .containsEntry("pressureHpa", 1013.6)
                .doesNotContainKeys("indoorTempC", "indoorRh");
    }

    @Test
    @DisplayName("°F, mph, inHg and inches are converted; a Klux solar value is left out; piezoRain is read")
    void imperial() {
        EcowittLiveDataParser.Result r = EcowittLiveDataParser.parse(
                AdapterFixtures.bytes("local-receivers", "ecowitt-livedata-imperial.json"), "s", 40.0, -105.0, true);
        assertThat(r.unknownIds()).isEqualTo(1);
        assertThat(r.record().payload())
                .containsEntry("tempC", 18.4).containsEntry("dewPointC", 11.1)
                .containsEntry("windSpeedMs", 2.3).containsEntry("windGustMs", 4.1)
                .containsEntry("pressureHpa", 1013.5)
                .containsEntry("rainRateMmH", 1.3).containsEntry("rainDailyMm", 4.6)
                .containsEntry("indoorTempC", 21.9).containsEntry("indoorRh", 44.0)
                .doesNotContainKey("solarWm2");
    }

    @ParameterizedTest(name = "{0} {1} {2} -> {3}")
    @CsvSource({
        "windSpeedMs, 36 km/h, , 10.0", "windSpeedMs, 10 knots, , 5.1", "windSpeedMs, 10 ft/s, , 3.0",
        "pressureHpa, 760 mmHg, , 1013.2", "pressureHpa, 101.3 kPa, , 1013.0", "tempC, 50, F, 10.0",
        "tempC, 21.5, ℃, 21.5", "rh, 55%, , 55.0"
    })
    @DisplayName("units come from the unit member or the value suffix")
    void units(String key, String raw, String unit, double expected) {
        assertThat(EcowittLiveDataParser.convert(key, raw, unit)).isEqualTo(expected);
    }

    @Test
    @DisplayName("an unknown unit or a value that is not a number yields nothing")
    void unknownUnit() {
        assertThat(EcowittLiveDataParser.convert("windSpeedMs", "5 bft", null)).isNull();
        assertThat(EcowittLiveDataParser.convert("tempC", "--.-", "C")).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ecowitt-garbage.html"})
    @DisplayName("an HTML page is a format error")
    void garbageFixture(String fixture) {
        assertThatThrownBy(() -> EcowittLiveDataParser.parse(AdapterFixtures.bytes("local-receivers", fixture), "s", 0, 0,
                false)).isInstanceOf(UpstreamFormatException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "[]", "{}", "{\"status\":\"ok\"}"})
    @DisplayName("a body without any live-data list is a format error")
    void garbage(String body) {
        assertThatThrownBy(() -> EcowittLiveDataParser.parse(body.getBytes(StandardCharsets.UTF_8), "s", 0, 0, false))
                .isInstanceOf(UpstreamFormatException.class);
    }
}
