package ai.foresignal.source.adapters.ourairports;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.source.formats.csv.CsvReader;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link AirportChart}: the VFR sectional symbol of each airport from the runway and frequency files. */
class AirportChartTest {

    private static final String HEADER = "le_heading_degT,he_heading_degT,le_latitude_deg,le_longitude_deg,"
            + "he_latitude_deg,he_longitude_deg";

    private static Map<String, AirportChart.Info> fixtures() {
        return AirportChart.index(AdapterFixtures.bytes(OurAirportsAdapter.ID, "runways.csv"),
                AdapterFixtures.bytes(OurAirportsAdapter.ID, "airport-frequencies.csv"));
    }

    @Test
    @DisplayName("the index: a TWR frequency makes an airport towered, and the longest open runway decides")
    void index() {
        Map<String, AirportChart.Info> chart = fixtures();

        AirportChart.Info jfk = chart.get("KJFK");
        assertThat(jfk.towered()).isTrue();
        assertThat(jfk.surface()).isEqualTo(AirportChart.SURFACE_HARD);
        assertThat(jfk.longestFt()).isEqualTo(14_511L);
        assertThat(jfk.longestHardFt()).isEqualTo(14_511L);
        assertThat(jfk.heading()).isEqualTo(31.0);

        assertThat(chart.get("EGLL").towered()).isTrue();
        assertThat(chart.get("EGLL").heading()).isEqualTo(89.6);

        // No TWR frequency; no heading at either end, so the bearing between the runway's two ends.
        AirportChart.Info cdg = chart.get("LFPG");
        assertThat(cdg.towered()).isFalse();
        assertThat(cdg.heading()).isCloseTo(86.4, within(1.0));

        // The 9,000 ft runway is closed, so the turf strip is the longest, and 180 folds to 0.
        AirportChart.Info small = chart.get("ZZZZ");
        assertThat(small.towered()).as("a CTAF is not a tower").isFalse();
        assertThat(small.surface()).isEqualTo(AirportChart.SURFACE_SOFT);
        assertThat(small.longestFt()).isEqualTo(2_100L);
        assertThat(small.longestHardFt()).isNull();
        assertThat(small.heading()).isEqualTo(0.0);

        assertThat(chart.get("KFFO").heading()).isNull();
        assertThat(chart).doesNotContainKey("XXCL");
    }

    @Test
    @DisplayName("the symbol: hard-surfaced runway length decides, with the chart's 1,500 and 8,069 ft limits")
    void symbol() {
        assertThat(AirportChart.symbol("large_airport", info(9_000L, 9_000L))).isEqualTo(AirportChart.MAJOR);
        assertThat(AirportChart.symbol("small_airport", info(8_070L, 8_070L))).isEqualTo(AirportChart.MAJOR);
        assertThat(AirportChart.symbol("medium_airport", info(8_069L, 8_069L))).isEqualTo(AirportChart.HARD);
        assertThat(AirportChart.symbol("small_airport", info(1_500L, 1_500L))).isEqualTo(AirportChart.HARD);
        assertThat(AirportChart.symbol("small_airport", info(1_499L, 1_499L))).isEqualTo(AirportChart.SOFT);
        assertThat(AirportChart.symbol("small_airport", info(5_000L, null))).as("turf only")
                .isEqualTo(AirportChart.SOFT);
        // A long turf strip with a shorter hard runway is drawn by the hard runway.
        assertThat(AirportChart.symbol("small_airport", info(6_000L, 3_000L))).isEqualTo(AirportChart.HARD);
    }

    @Test
    @DisplayName("the symbol: heliports, seaplane bases and closed airports by type, and the type when runways are unknown")
    void symbolByType() {
        assertThat(AirportChart.symbol("heliport", info(9_000L, 9_000L))).isEqualTo(AirportChart.HELIPORT);
        assertThat(AirportChart.symbol("seaplane_base", AirportChart.NONE)).isEqualTo(AirportChart.SEAPLANE);
        assertThat(AirportChart.symbol("closed", info(9_000L, 9_000L))).isEqualTo(AirportChart.CLOSED);
        assertThat(AirportChart.symbol("large_airport", AirportChart.NONE)).isEqualTo(AirportChart.MAJOR);
        assertThat(AirportChart.symbol("medium_airport", AirportChart.NONE)).isEqualTo(AirportChart.HARD);
        assertThat(AirportChart.symbol("small_airport", AirportChart.NONE)).isEqualTo(AirportChart.SOFT);
        assertThat(AirportChart.symbol(null, AirportChart.NONE)).isEqualTo(AirportChart.SOFT);
        assertThat(AirportChart.turnsWithRunway(AirportChart.HARD)).isTrue();
        assertThat(AirportChart.turnsWithRunway(AirportChart.HELIPORT)).isFalse();
    }

    @Test
    @DisplayName("surface codes: paved prefixes are hard, water is water, anything else (or nothing) is soft")
    void surface() {
        for (String hard : List.of("ASP", "asph-g", "CON", "CONC", "PEM", "BIT", "PAVED", "TARMAC", "MACADAM")) {
            assertThat(AirportChart.surface(hard)).as(hard).isEqualTo(AirportChart.SURFACE_HARD);
        }
        assertThat(AirportChart.surface("WATER")).isEqualTo(AirportChart.SURFACE_WATER);
        for (String soft : List.of("TURF", "GRS", "GRAVEL", "DIRT", "UNK")) {
            assertThat(AirportChart.surface(soft)).as(soft).isEqualTo(AirportChart.SURFACE_SOFT);
        }
        assertThat(AirportChart.surface(null)).isEqualTo(AirportChart.SURFACE_SOFT);
        assertThat(AirportChart.surface(" ")).isEqualTo(AirportChart.SURFACE_SOFT);
    }

    @Test
    @DisplayName("heading: the low end, else the high end less 180, else the bearing; folded into 0 to 180")
    void heading() {
        assertThat(heading("350,,,,,")).isEqualTo(170.0);
        assertThat(heading(",223,,,,")).isEqualTo(43.0);
        assertThat(heading(",,0,0,1,0")).isEqualTo(0.0);
        assertThat(heading(",,0,0,0,1")).isEqualTo(90.0);
        assertThat(heading(",,,,,")).isNull();
    }

    private static Double heading(String values) {
        Map<String, Integer> index = CsvReader.index(List.of(HEADER.split(",")));
        return AirportChart.heading(List.of(values.split(",", -1)), index);
    }

    private static AirportChart.Info info(Long longestFt, Long longestHardFt) {
        return new AirportChart.Info(false, longestHardFt == null ? AirportChart.SURFACE_SOFT
                : AirportChart.SURFACE_HARD, longestFt, longestHardFt, null);
    }
}
