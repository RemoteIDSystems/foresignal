package ai.foresignal.source.adapters.hydro.coops;

import static ai.foresignal.source.adapters.hydro.HydroTestSupport.fixture;
import static ai.foresignal.source.adapters.hydro.HydroTestSupport.node;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.adapters.env.EnvTestSupport;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link CoopsMapper} on the 2026-10-13 CO-OPS answers for The Battery (8518750) and San Francisco (9414290): string
 * numbers, the high/low selection after now, the station-datum to MLLW conversion of the flood levels, the flood
 * category, CO-OPS error objects, the station list without Great Lakes stations, and the descriptor and URLs of
 * {@link NoaaCoopsTidesAdapter} (every data URL carries {@code application=Foresignal}).
 */
class CoopsMapperTest {

    static final Instant NOW = Instant.parse("2026-10-13T22:15:00Z");

    static byte[] bytes(String name) {
        return AdapterFixtures.bytes("noaa-coops-tides", name);
    }

    static CoopsMapper.FloodLevels battery() {
        return CoopsMapper.floodLevels(fixture("noaa-coops-tides", "floodlevels-8518750.json"),
                fixture("noaa-coops-tides", "datums-8518750.json"));
    }

    static NormalizedRecord record() {
        return CoopsMapper.record("8518750", CoopsMapper.waterLevel(bytes("water_level-8518750.json")),
                CoopsMapper.predictions(bytes("predictions-8518750.json")), battery(), NOW);
    }

    @Test
    @DisplayName("water level: text numbers, GMT time, the preliminary flag")
    void waterLevel() {
        CoopsMapper.WaterLevel level = CoopsMapper.waterLevel(bytes("water_level-8518750.json"));
        assertThat(level).isEqualTo(new CoopsMapper.WaterLevel("8518750", "The Battery", 40.7006, -74.0142,
                Instant.parse("2026-10-13T22:06:00Z"), 0.59, 0.046, "preliminary"));
        assertThat(CoopsMapper.waterLevel(bytes("water_level-9414290.json")).levelM()).isEqualTo(1.765);
    }

    @Test
    @DisplayName("CO-OPS error objects (unknown station, no MLLW) and an empty data array are format errors")
    void errors() {
        assertThatThrownBy(() -> CoopsMapper.waterLevel(bytes("error-invalid-station.json")))
                .isInstanceOf(UpstreamFormatException.class).hasMessageContaining("not a valid station");
        assertThatThrownBy(() -> CoopsMapper.waterLevel(bytes("error-no-mllw-9063020.json")))
                .isInstanceOf(UpstreamFormatException.class).hasMessageContaining("no MLLW");
        assertThatThrownBy(() -> CoopsMapper.waterLevel("{\"data\": []}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> CoopsMapper.predictions("{\"x\": 1}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("hilo: eight extremes sorted; the next high and low after 22:15 are tomorrow's 02:56 and 09:01")
    void hilo() {
        List<CoopsMapper.Tide> tides = CoopsMapper.predictions(bytes("predictions-8518750.json"));
        assertThat(tides).hasSize(8);
        assertThat(CoopsMapper.next(tides, NOW, true))
                .isEqualTo(new CoopsMapper.Tide(Instant.parse("2026-10-14T02:56:00Z"), 1.363, true));
        assertThat(CoopsMapper.next(tides, NOW, false))
                .isEqualTo(new CoopsMapper.Tide(Instant.parse("2026-10-14T09:01:00Z"), 0.228, false));
        assertThat(CoopsMapper.next(tides, Instant.parse("2026-10-13T12:00:00Z"), true).at())
                .isEqualTo(Instant.parse("2026-10-13T14:30:00Z"));
        assertThat(CoopsMapper.next(tides, Instant.parse("2026-10-15T00:00:00Z"), true)).isNull();
        assertThat(CoopsMapper.tidesFromJson(CoopsMapper.tidesToJson(tides))).isEqualTo(tides);
        assertThat(CoopsMapper.tidesFromJson("[{")).isEmpty();
    }

    @Test
    @DisplayName("flood levels are converted from station datum feet to MLLW meters; NWS is the fallback")
    void datumConversion() {
        // The Battery: (10.19 − 3.29) ft = 2.103 m minor; action 10.29 ft → 2.134 m.
        assertThat(battery()).isEqualTo(new CoopsMapper.FloodLevels(2.134, 2.103, 2.387, 2.774, "NOS"));
        // San Francisco: MLLW 5.98 ft, NOS minor 13.69 ft → 2.350 m; no action level.
        assertThat(CoopsMapper.floodLevels(fixture("noaa-coops-tides", "floodlevels-9414290.json"),
                fixture("noaa-coops-tides", "datums-9414290.json")))
                .isEqualTo(new CoopsMapper.FloodLevels(null, 2.35, 2.633, 3.021, "NOS"));
        CoopsMapper.FloodLevels nws = CoopsMapper.floodLevels(node("""
                {"nos_minor": null, "nws_minor": 10.0, "nws_moderate": 11.0, "nws_major": null, "action": null}"""),
                node("{\"units\": \"meters\", \"datums\": [{\"name\": \"MLLW\", \"value\": 1.0}]}"));
        assertThat(nws).isEqualTo(new CoopsMapper.FloodLevels(null, 9.0, 10.0, null, "NWS"));
        assertThat(CoopsMapper.floodLevels(fixture("noaa-coops-tides", "floodlevels-8518750.json"),
                node("{\"units\": \"feet\", \"datums\": [{\"name\": \"GL_LWD\", \"value\": 569.23}]}")))
                .isEqualTo(CoopsMapper.FloodLevels.NONE);
        assertThat(CoopsMapper.levelsFromJson(CoopsMapper.levelsToJson(battery()))).isEqualTo(battery());
        assertThat(CoopsMapper.levelsFromJson("nope")).isNull();
    }

    @Test
    @DisplayName("flood category: none, action, minor, moderate, major; absent without a minor level")
    void category() {
        CoopsMapper.FloodLevels levels = new CoopsMapper.FloodLevels(1.8, 2.0, 2.4, 2.8, "NOS");
        assertThat(CoopsMapper.category(0.59, levels)).isEqualTo("none");
        assertThat(CoopsMapper.category(1.9, levels)).isEqualTo("action");
        assertThat(CoopsMapper.category(2.0, levels)).isEqualTo("minor");
        assertThat(CoopsMapper.category(2.5, levels)).isEqualTo("moderate");
        assertThat(CoopsMapper.category(3.1, levels)).isEqualTo("major");
        assertThat(CoopsMapper.category(3.1, CoopsMapper.FloodLevels.NONE)).isNull();
        assertThat(CoopsMapper.category(null, levels)).isNull();
    }

    @Test
    @DisplayName("the record: MLLW meters, the next extremes, the category and thresholds")
    void recordPayload() {
        NormalizedRecord r = record();
        assertThat(r.kind()).isEqualTo("tide-gauge");
        assertThat(r.observedAt()).isEqualTo(Instant.parse("2026-10-13T22:06:00Z")).isEqualTo(r.eventTime());
        assertThat(r.payload()).containsEntry("name", "The Battery").containsEntry("waterLevelM", 0.59)
                .containsEntry("datum", "MLLW").containsEntry("sigmaM", 0.046).containsEntry("quality", "preliminary")
                .containsEntry("nextHighTime", "2026-10-14T02:56:00Z").containsEntry("nextHighM", 1.363)
                .containsEntry("nextLowTime", "2026-10-14T09:01:00Z").containsEntry("nextLowM", 0.228)
                .containsEntry("floodCategory", "none").containsEntry("minorFloodM", 2.103)
                .containsEntry("moderateFloodM", 2.387).containsEntry("majorFloodM", 2.774);
        FieldSpecContract.assertPayloadKeysDeclared(new NoaaCoopsTidesAdapter("").descriptor(), List.of(r));
        NormalizedRecord bare = CoopsMapper.record("9414290", CoopsMapper.waterLevel(bytes("water_level-9414290.json")),
                List.of(), null, NOW);
        assertThat(bare.payload()).doesNotContainKeys("nextHighTime", "floodCategory", "minorFloodM");
        assertThat(CoopsMapper.record("1", new CoopsMapper.WaterLevel("1", "x", null, null, NOW, 1.0, null, null),
                List.of(), null, NOW)).isNull();
    }

    @Test
    @DisplayName("the station list keeps tidal and coastal stations and drops the Great Lakes")
    void stations() {
        assertThat(CoopsMapper.stationIds(fixture("noaa-coops-tides", "stations-waterlevels.json")))
                .containsExactly("8518750", "9414290");
        assertThatThrownBy(() -> CoopsMapper.stationIds(node("{}"))).isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("checkParams, the URLs with application=Foresignal, the descriptor, and the snapshot contract")
    void adapter() {
        NoaaCoopsTidesAdapter adapter = new NoaaCoopsTidesAdapter("");
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        assertThat(adapter.descriptor().recordTtl()).isEqualTo(Duration.ofHours(1));
        assertThat(adapter.checkParams(ValidatedParams.empty())).containsKey("stations");
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("stations", List.of("8518750"))))).isEmpty();
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("stations", List.of("TheBattery")))))
                .containsKey("stations");
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("stations",
                java.util.stream.IntStream.range(0, 21).mapToObj(i -> String.valueOf(8500000 + i)).toList()))))
                .containsKey("stations");
        assertThat(adapter.waterLevelUri("8518750")).hasToString("https://api.tidesandcurrents.noaa.gov/api/prod/"
                + "datagetter?product=water_level&station=8518750&date=latest&datum=MLLW&units=metric&time_zone=gmt"
                + "&application=Foresignal&format=json");
        assertThat(adapter.predictionsUri("8518750", LocalDate.parse("2026-10-13")).toString())
                .contains("product=predictions&interval=hilo&begin_date=20261013&end_date=20261014")
                .contains("application=Foresignal");
        assertThat(adapter.floodLevelsUri("8518750")).hasToString(
                "https://api.tidesandcurrents.noaa.gov/mdapi/prod/webapi/stations/8518750/floodlevels.json");
        assertThat(adapter.datumsUri("8518750")).hasToString(
                "https://api.tidesandcurrents.noaa.gov/mdapi/prod/webapi/stations/8518750/datums.json");
        NormalizedRecord r = record();
        SnapshotContract.assertStableStaticHash(adapter.descriptor(),
                List.of(r, EnvTestSupport.later(r, Duration.ofMinutes(6))));
    }
}
