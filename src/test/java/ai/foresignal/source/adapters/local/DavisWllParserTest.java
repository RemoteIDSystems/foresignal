package ai.foresignal.source.adapters.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.Point;

/**
 * {@link DavisWllParser} (prompt 069): the SI conversions, every {@code rain_size}, the indoor record only
 * with {@code includeIndoor}, coordinate rounding and format errors.
 */
class DavisWllParserTest {

    private static byte[] fixture() {
        return AdapterFixtures.bytes("local-receivers", "davis-current-conditions.json");
    }

    private static byte[] withRainSize(int size) {
        return AdapterFixtures.text("local-receivers", "davis-current-conditions.json")
                .replace("\"rain_size\": 2", "\"rain_size\": " + size).getBytes(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("°F, mph and inHg become °C, m/s and hPa; did and ts identify and time the record")
    void conversions() {
        NormalizedRecord r = DavisWllParser.parse(fixture(), 40.01499, -105.27001, false);
        assertThat(r.externalId()).isEqualTo("001D0A700002");
        assertThat(r.kind()).isEqualTo("weather-observation");
        assertThat(r.observedAt()).isEqualTo(Instant.ofEpochSecond(1791900030L));
        assertThat(r.eventTime()).isEqualTo(r.observedAt());
        assertThat(r.payload())
                .containsEntry("tempC", 17.1)
                .containsEntry("rh", 41.1)
                .containsEntry("dewPointC", 3.5)
                .containsEntry("windSpeedMs", 2.2)
                .containsEntry("windDirDeg", 270.0)
                .containsEntry("windGustMs", 5.4)
                .containsEntry("pressureHpa", 1016.2)
                .containsEntry("pressureTrendHpa", -1.0)
                .containsEntry("batteryLow", false);
    }

    @Test
    @DisplayName("rain_size 2 is 0.2 mm per count: 10 counts/h is 2 mm/h, 63 counts is 12.6 mm")
    void rainSize2() {
        NormalizedRecord r = DavisWllParser.parse(fixture(), 40, -105, false);
        assertThat(r.payload()).containsEntry("rainRateMmH", 2.0).containsEntry("rainDailyMm", 12.6);
    }

    @ParameterizedTest(name = "rain_size {0}: {1} mm/h, {2} mm")
    @CsvSource({"1, 2.54, 16.0", "3, 1.0, 6.3", "4, 0.25, 1.6"})
    @DisplayName("each rain_size converts counts to millimeters")
    void rainSizes(int size, double rate, double daily) {
        NormalizedRecord r = DavisWllParser.parse(withRainSize(size), 40, -105, false);
        assertThat(r.payload()).containsEntry("rainRateMmH", rate).containsEntry("rainDailyMm", daily);
    }

    @Test
    @DisplayName("an unknown rain_size leaves the rain fields out")
    void unknownRainSize() {
        assertThat(DavisWllParser.parse(withRainSize(0), 40, -105, false).payload())
                .doesNotContainKeys("rainRateMmH", "rainDailyMm");
        assertThat(DavisWllParser.millimetersPerCount(null)).isNull();
    }

    @Test
    @DisplayName("indoor values are left out by default and kept with includeIndoor")
    void indoor() {
        assertThat(DavisWllParser.parse(fixture(), 40, -105, false).payload())
                .doesNotContainKeys("indoorTempC", "indoorRh");
        assertThat(DavisWllParser.parse(fixture(), 40, -105, true).payload())
                .containsEntry("indoorTempC", 22.2).containsEntry("indoorRh", 35.5);
    }

    @Test
    @DisplayName("the location is rounded to 2 decimals (about 1 km)")
    void coarseLocation() {
        Point p = (Point) DavisWllParser.parse(fixture(), 40.01499, -105.27501, false).geometry();
        assertThat(p.getY()).isEqualTo(40.01);
        assertThat(p.getX()).isEqualTo(-105.28);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "nope", "[]", "{\"data\":null,\"error\":{\"code\":409,\"message\":\"busy\"}}",
            "{\"data\":{\"ts\":1,\"conditions\":[]},\"error\":null}", "{\"data\":{\"did\":\"x\",\"conditions\":[]}}"})
    @DisplayName("an error answer or a body without data, did, ts or conditions is a format error")
    void formatErrors(String body) {
        assertThatThrownBy(() -> DavisWllParser.parse(body.getBytes(StandardCharsets.UTF_8), 40, -105, false))
                .isInstanceOf(UpstreamFormatException.class);
    }
}
