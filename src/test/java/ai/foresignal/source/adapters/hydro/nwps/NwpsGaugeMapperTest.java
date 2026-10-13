package ai.foresignal.source.adapters.hydro.nwps;

import static ai.foresignal.source.adapters.hydro.HydroTestSupport.fixture;
import static ai.foresignal.source.adapters.hydro.HydroTestSupport.node;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.adapters.env.EnvTestSupport;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.source.support.MappedRecord;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import tools.jackson.databind.JsonNode;

/**
 * {@link NwpsGaugeMapper} on the 2026-10-13 NWPS gauge lists (four Denver gauges; two Mississippi-basin gauges with a
 * current forecast and one out of service) and the South Platte at Denver detail: the {@code -999} and
 * {@code 0001-01-01T00:00:00Z} sentinels, the threshold join, {@code stageToMinor}, the state round trip, and the
 * descriptor of {@link NwpsGaugesAdapter}.
 */
class NwpsGaugeMapperTest {

    static NwpsGaugeMapper.Thresholds denver() {
        return NwpsGaugeMapper.thresholds(fixture("nwps-gauges", "detail-DNVC2.json"));
    }

    static List<MappedRecord> map(String file, Map<String, NwpsGaugeMapper.Thresholds> thresholds) {
        List<MappedRecord> out = new ArrayList<>();
        for (JsonNode g : NwpsGaugeMapper.gauges(fixture("nwps-gauges", file))) {
            out.add(NwpsGaugeMapper.gauge(g, thresholds.get(g.get("lid").asString())));
        }
        return out;
    }

    static List<NormalizedRecord> records(String file, Map<String, NwpsGaugeMapper.Thresholds> thresholds) {
        return map(file, thresholds).stream().filter(MappedRecord::accepted).map(MappedRecord::record).toList();
    }

    @Test
    @DisplayName("the detail: stages from flood.categories, -9999 flows ignored, blank usgsId absent, in service")
    void thresholds() {
        assertThat(denver()).isEqualTo(new NwpsGaugeMapper.Thresholds(9.5, 11.0, 13.0, 15.0, "ft", null, true));
        NwpsGaugeMapper.Thresholds undefined = NwpsGaugeMapper.thresholds(node("""
                {"usgsId": "06714000", "inService": {"enabled": false},
                 "flood": {"stageUnits": "ft", "categories": {"action": {"stage": -9999}, "minor": {"stage": -9999},
                   "moderate": {"stage": -9999}, "major": {"stage": -9999}}}}"""));
        assertThat(undefined).isEqualTo(new NwpsGaugeMapper.Thresholds(null, null, null, null, "ft", "06714000",
                false));
        assertThat(NwpsGaugeMapper.thresholds(null)).isEqualTo(NwpsGaugeMapper.Thresholds.NONE);
    }

    @Test
    @DisplayName("Denver: stage and flow sorted by unit, -999 flow absent, the sentinel time skipped, thresholds joined")
    void denverGauges() {
        List<MappedRecord> mapped = map("gauges-denver.json", Map.of("DNVC2", denver()));
        assertThat(mapped).hasSize(4);
        assertThat(mapped.stream().filter(m -> !m.accepted()).map(MappedRecord::reason))
                .containsExactly(MappedRecord.MISSING_OBSERVED_AT);
        List<NormalizedRecord> records = records("gauges-denver.json", Map.of("DNVC2", denver()));
        NormalizedRecord platte = EnvTestSupport.byId(records, "DNVC2");
        assertThat(platte.observedAt()).isEqualTo(Instant.parse("2026-10-13T20:45:00Z")).isEqualTo(platte.eventTime());
        assertThat(platte.payload()).containsEntry("name", "South Platte River at Denver")
                .containsEntry("rfc", "MBRFC").containsEntry("wfo", "BOU").containsEntry("state", "CO")
                .containsEntry("stage", 2.47).containsEntry("stageUnit", "ft").containsEntry("flow", 0.0552)
                .containsEntry("flowUnit", "kcfs").containsEntry("floodCategory", "no_flooding")
                .containsEntry("actionStage", 9.5).containsEntry("minorStage", 11.0)
                .containsEntry("moderateStage", 13.0).containsEntry("majorStage", 15.0)
                .containsEntry("stageToMinor", 8.53).containsEntry("inService", true)
                .doesNotContainKeys("usgsId", "forecastCategory", "forecastStage", "forecastValidTime");
        assertThat(EnvTestSupport.byId(records, "BBCC2").payload()).containsEntry("stage", 0.94)
                .doesNotContainKeys("flow", "flowUnit", "minorStage", "stageToMinor", "inService");
        assertThat(EnvTestSupport.byId(records, "CGCC2").payload()).containsEntry("floodCategory", "obs_not_current")
                .containsEntry("flow", 0.0).doesNotContainKeys("stage", "stageUnit");
        FieldSpecContract.assertPayloadKeysDeclared(new NwpsGaugesAdapter("", () -> null).descriptor(), records);
    }

    @Test
    @DisplayName("a current forecast adds its category, stage and time; out of service with sentinel times is skipped")
    void forecasts() {
        List<MappedRecord> mapped = map("gauges-forecast.json", Map.of());
        assertThat(mapped).hasSize(3);
        List<NormalizedRecord> records = records("gauges-forecast.json", Map.of());
        assertThat(records).extracting(NormalizedRecord::externalId).containsExactly("AKFM7", "ALNI2");
        assertThat(EnvTestSupport.byId(records, "ALNI2").payload()).containsEntry("forecastCategory", "no_flooding")
                .containsEntry("forecastStage", 11.1).containsEntry("forecastValidTime", "2026-10-14T00:00:00Z")
                .containsEntry("flow", 176.0);
        assertThat(EnvTestSupport.byId(records, "AKFM7").payload()).containsEntry("floodCategory", "low_threshold");
        FieldSpecContract.assertPayloadKeysDeclared(new NwpsGaugesAdapter("", () -> null).descriptor(), records);
    }

    @Test
    @DisplayName("stageToMinor is negative in flood and absent when the units differ")
    void stageToMinor() {
        JsonNode inFlood = node("""
                {"lid": "TEST1", "name": "Test", "latitude": 40.0, "longitude": -100.0,
                 "status": {"observed": {"primary": 12.34, "primaryUnit": "ft", "secondary": -999, "secondaryUnit": "",
                   "floodCategory": "minor", "validTime": "2026-10-13T21:00:00Z"},
                   "forecast": {"primary": -999, "floodCategory": "fcst_not_current",
                   "validTime": "0001-01-01T00:00:00Z"}}}""");
        NwpsGaugeMapper.Thresholds feet = new NwpsGaugeMapper.Thresholds(null, 11.0, null, null, "ft", null, null);
        assertThat(NwpsGaugeMapper.gauge(inFlood, feet).record().payload()).containsEntry("stageToMinor", -1.34);
        NwpsGaugeMapper.Thresholds meters = new NwpsGaugeMapper.Thresholds(null, 3.0, null, null, "m", null, null);
        assertThat(NwpsGaugeMapper.gauge(inFlood, meters).record().payload()).doesNotContainKey("stageToMinor");
        assertThat(NwpsGaugeMapper.gauge(node("{\"name\": \"x\"}"), null).reason()).isEqualTo(MappedRecord.MISSING_ID);
    }

    @Test
    @DisplayName("a list without a gauges array is a format error")
    void formatError() {
        assertThatThrownBy(() -> NwpsGaugeMapper.gauges(node("{\"items\": []}")))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("cached thresholds survive the state round trip with their read time; damaged state is null")
    void stateRoundTrip() {
        Instant at = Instant.parse("2026-10-13T22:00:00Z");
        NwpsGaugeMapper.Cached c = NwpsGaugeMapper.fromJson(NwpsGaugeMapper.toJson(denver(), at));
        assertThat(c).isEqualTo(new NwpsGaugeMapper.Cached(denver(), at));
        assertThat(NwpsGaugeMapper.fromJson(NwpsGaugeMapper.toJson(NwpsGaugeMapper.Thresholds.NONE, at)).thresholds())
                .isEqualTo(NwpsGaugeMapper.Thresholds.NONE);
        assertThat(NwpsGaugeMapper.fromJson("{")).isNull();
        assertThat(NwpsGaugeMapper.fromJson("{\"a\": 1}")).isNull();
    }

    @Test
    @DisplayName("checkParams, the list URL, the descriptor, and the snapshot contract across fifteen minutes")
    void descriptorAndSnapshot() {
        NwpsGaugesAdapter adapter = new NwpsGaugesAdapter("", () -> null);
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        assertThat(adapter.descriptor().tileProperties()).containsExactly("name", "floodCategory", "stage");
        assertThat(adapter.descriptor().significantFields()).isEmpty();
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("bbox", new Envelope(-106, -96, 35, 45))))).isEmpty();
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("bbox", new Envelope(-110, -96, 35, 45)))))
                .containsKey("bbox");
        assertThat(adapter.checkParams(ValidatedParams.empty())).containsKey("bbox");
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("bbox", new Envelope(-106, -105, 39, 40),
                "maxGauges", 301)))).containsKey("maxGauges");
        assertThat(adapter.listUri(new Envelope(-105.5, -104.5, 39.5, 40.2))).hasToString(
                "https://api.water.noaa.gov/nwps/v1/gauges?bbox.xmin=-105.5&bbox.ymin=39.5&bbox.xmax=-104.5"
                        + "&bbox.ymax=40.2&srid=EPSG_4326");
        NormalizedRecord r = EnvTestSupport.byId(records("gauges-denver.json", Map.of("DNVC2", denver())), "DNVC2");
        SnapshotContract.assertStableStaticHash(adapter.descriptor(),
                List.of(r, EnvTestSupport.later(r, Duration.ofMinutes(15))));
    }
}
