package ai.foresignal.source.adapters.nws;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link NwsGridpointsParser} against a real {@code /gridpoints/VEF/55,177} body captured 2026-10-12 (prompt 053):
 * interval expansion, accumulation splitting, unit conversion, the 24-hour summaries and hazards.
 */
class NwsGridpointsParserTest {

    static final String DIR = "nws-gridpoints";
    static final byte[] GRID = AdapterFixtures.bytes(DIR, "gridpoints-VEF-55-177.json");
    static final Instant NOW = Instant.parse("2026-10-12T20:10:00Z");

    private static Map<String, Object> step(NwsGridpointsParser.Forecast f, String time) {
        return f.series().stream().filter(s -> time.equals(s.get("time"))).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("/points gives the office, grid cell, gridpoint URL and time zone")
    void point() {
        NwsGridpointsParser.Point p = NwsGridpointsParser.parsePoint(AdapterFixtures.bytes(DIR, "points-VEF-55-177.json"));
        assertThat(p.gridId()).isEqualTo("VEF");
        assertThat(p.gridX()).isEqualTo(55);
        assertThat(p.gridY()).isEqualTo(177);
        assertThat(p.forecastGridData()).isEqualTo("https://api.weather.gov/gridpoints/VEF/55,177");
        assertThat(p.timeZone()).isEqualTo("America/Los_Angeles");
        assertThatThrownBy(() -> NwsGridpointsParser.parsePoint("{\"properties\":{}}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("72 hourly steps from the current hour; intervals spread over their hours, wind km/h -> m/s")
    void expansion() {
        NwsGridpointsParser.Forecast f = NwsGridpointsParser.parseForecast(GRID, NOW);
        assertThat(f.updateTime()).isEqualTo(Instant.parse("2026-10-12T19:46:56Z"));
        assertThat(f.wfo()).isEqualTo("VEF");
        assertThat(f.gridX()).isEqualTo(55);
        assertThat(f.gridY()).isEqualTo(177);
        assertThat(f.start()).isEqualTo(Instant.parse("2026-10-12T20:00:00Z"));
        assertThat(f.series()).hasSize(72);
        assertThat(f.series().get(0)).containsEntry("time", "2026-10-12T20:00:00Z");
        assertThat(f.series().get(71)).containsEntry("time", "2026-10-15T19:00:00Z");

        Map<String, Object> first = step(f, "2026-10-12T20:00:00Z");
        // temperature 5.555… degC; windSpeed 14.816 km/h (20:00/PT1H) = 4.1 m/s; windDirection 260 (from 20:00).
        assertThat(first).containsEntry("tempC", 5.6).containsEntry("windSpeedMs", 4.1).containsEntry("windFromDeg", 260)
                .containsEntry("popPct", 30).containsEntry("skyCoverPct", 75);
        assertThat((Double) first.get("gustMs")).isEqualTo(6.2);
        // 21:00/PT2H 11.112 km/h covers 21:00 and 22:00.
        assertThat(step(f, "2026-10-12T21:00:00Z")).containsEntry("windSpeedMs", 3.1);
        assertThat(step(f, "2026-10-12T22:00:00Z")).containsEntry("windSpeedMs", 3.1);
    }

    @Test
    @DisplayName("an accumulation is divided evenly across its interval's hours")
    void accumulationSplitting() {
        NwsGridpointsParser.Forecast f = NwsGridpointsParser.parseForecast(GRID, NOW);
        // 18:00/PT6H 0.508 mm -> 0.0847 mm per hour, of which 20:00..23:00 fall in the series.
        assertThat(step(f, "2026-10-12T20:00:00Z")).containsEntry("precipMm", 0.08);
        assertThat(step(f, "2026-10-12T23:00:00Z")).containsEntry("precipMm", 0.08);
        // 00:00/PT6H 1.016 mm -> 0.169 mm per hour.
        assertThat(step(f, "2026-10-13T00:00:00Z")).containsEntry("precipMm", 0.17);
        assertThat(step(f, "2026-10-13T05:00:00Z")).containsEntry("precipMm", 0.17);
        assertThat(step(f, "2026-10-13T06:00:00Z")).containsEntry("precipMm", 0.0);
    }

    @Test
    @DisplayName("24-hour summaries: precipitation chance, amount, temperature range and peak gust")
    void summaries() {
        NwsGridpointsParser.Forecast f = NwsGridpointsParser.parseForecast(GRID, NOW);
        assertThat(f.summary()).containsEntry("popMax24h", 60);
        // 4 hours x 0.0847 + 6 hours x 0.1693 = 1.355 mm.
        assertThat((Double) f.summary().get("qpf24hMm")).isEqualTo(1.4);
        assertThat((Double) f.summary().get("tMaxC24h")).isEqualTo(6.7);
        assertThat((Double) f.summary().get("tMinC24h")).isEqualTo(2.2);
        assertThat((Double) f.summary().get("gustMaxMs24h")).isCloseTo(19.5, within(0.05));
    }

    @Test
    @DisplayName("hazards: phenomenon.significance with the published interval; empty lists are no hazard")
    void hazards() {
        NwsGridpointsParser.Forecast f = NwsGridpointsParser.parseForecast(GRID, NOW);
        assertThat(f.hazards()).containsExactly(new NwsGridpointsParser.Hazard("FZ.W", "2026-10-13T07:00:00+00:00/PT10H"));
    }

    @Test
    @DisplayName("interval parsing: hours, days and minutes; malformed intervals are skipped")
    void intervals() {
        assertThat(NwsGridpointsParser.interval("2026-10-12T13:00:00+00:00/PT5H").hours()).isEqualTo(5);
        assertThat(NwsGridpointsParser.interval("2026-10-12T13:00:00+00:00/P1DT14H").duration())
                .isEqualTo(Duration.ofHours(38));
        assertThat(NwsGridpointsParser.interval("2026-10-12T13:00:00+00:00/PT30M").hours()).as("at least one hour")
                .isEqualTo(1);
        assertThat(NwsGridpointsParser.interval("2026-10-12T13:00:00+00:00")).isNull();
        assertThat(NwsGridpointsParser.interval("nonsense/PT1H")).isNull();
        assertThat(NwsGridpointsParser.interval(null)).isNull();
    }

    @Test
    @DisplayName("units: degF and m/s are converted or kept; unexpected units are a format error")
    void units() {
        assertThat(NwsGridpointsParser.convert(212, "wmoUnit:degF", NwsGridpointsParser.Unit.CELSIUS)).isEqualTo(100.0);
        assertThat(NwsGridpointsParser.convert(36, "wmoUnit:km_h-1", NwsGridpointsParser.Unit.SPEED)).isEqualTo(10.0);
        assertThat(NwsGridpointsParser.convert(7, "wmoUnit:m_s-1", NwsGridpointsParser.Unit.SPEED)).isEqualTo(7.0);
        assertThatThrownBy(() -> NwsGridpointsParser.convert(7, "wmoUnit:kn", NwsGridpointsParser.Unit.SPEED))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> NwsGridpointsParser.convert(7, "wmoUnit:in", NwsGridpointsParser.Unit.MILLIMETERS))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("a body without updateTime, a non-JSON body, and a forecast outside its valid times")
    void malformed() {
        assertThatThrownBy(() -> NwsGridpointsParser.parseForecast("not json".getBytes(StandardCharsets.UTF_8), NOW))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> NwsGridpointsParser.parseForecast(
                "{\"properties\":{\"gridId\":\"VEF\",\"gridX\":1,\"gridY\":2}}".getBytes(StandardCharsets.UTF_8), NOW))
                .isInstanceOf(UpstreamFormatException.class);
        NwsGridpointsParser.Forecast late = NwsGridpointsParser.parseForecast(GRID, Instant.parse("2026-11-30T00:00:00Z"));
        assertThat(late.series()).as("no layer covers that time").isEmpty();
        assertThat(late.summary()).isEmpty();
    }

    @Test
    @DisplayName("at most 10 hazards, ordered by start")
    void hazardCap() throws Exception {
        JsonMapper json = JsonMapper.builder().build();
        StringBuilder values = new StringBuilder();
        for (int i = 12; i >= 0; i--) {
            if (values.length() > 0) {
                values.append(',');
            }
            values.append("{\"validTime\":\"2026-10-").append(13 + i).append("T00:00:00+00:00/PT6H\",\"value\":[{")
                    .append("\"phenomenon\":\"WI\",\"significance\":\"Y\",\"event_number\":null}]}");
        }
        List<NwsGridpointsParser.Hazard> hazards = NwsGridpointsParser.hazards(json.readTree("{\"values\":[" + values + "]}"));
        assertThat(hazards).hasSize(NwsGridpointsParser.MAX_HAZARDS);
        assertThat(hazards.get(0).validTime()).startsWith("2026-10-13T00");
        assertThat(hazards).allSatisfy(h -> assertThat(h.type()).isEqualTo("WI.Y"));
    }
}
