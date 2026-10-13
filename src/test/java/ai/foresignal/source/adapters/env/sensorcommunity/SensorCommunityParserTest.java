package ai.foresignal.source.adapters.env.sensorcommunity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.adapters.env.EnvTestSupport;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.source.support.MappedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link SensorCommunityParser} and {@link SensorCommunityArea} on 27 readings of 11 sensors from the 2026-10-13
 * Berlin box: the newest reading per sensor, indoor handling, pascals to hPa, sensors with nothing to store, and the
 * {@code area} validation.
 */
class SensorCommunityParserTest {

    static EnvSources.Parsed parsed(boolean indoor) {
        return SensorCommunityParser.parse(AdapterFixtures.bytes("sensor-community", "filter-box.json"), indoor,
                EnvTestSupport.NOW, SensorCommunityParser.DEFAULT_MAX_AGE);
    }

    @Test
    @DisplayName("outdoor only: 7 sensors, the newest reading each; indoor, noise and radiation sensors skipped")
    void newestPerSensor() {
        EnvSources.Parsed p = parsed(false);
        assertThat(p.records()).extracting(NormalizedRecord::externalId)
                .containsExactly("1377", "1847", "25211", "3355", "5356", "6438", "829");
        assertThat(p.records()).allSatisfy(r -> assertThat(r.kind()).isEqualTo("air-quality"));
        assertThat(p.skipped()).containsEntry(SensorCommunityParser.INDOOR, 4).containsEntry(EnvSources.NO_VALUES, 4)
                .containsEntry(MappedRecord.DUPLICATE_ID, 12);
        NormalizedRecord sds = EnvTestSupport.byId(p.records(), "1847");
        assertThat(sds.observedAt()).isEqualTo(Instant.parse("2026-10-13T21:35:52Z"));
        assertThat(sds.eventTime()).isEqualTo(sds.observedAt());
        assertThat(sds.payload()).containsEntry("sensorType", "SDS011").containsEntry("pm10", 3.0)
                .containsEntry("pm25", 1.53).containsEntry("exactLocation", false);
        assertThat(sds.geometry().getCoordinate().x).isEqualTo(13.356);
        assertThat(EnvTestSupport.byId(p.records(), "25211").payload()).containsEntry("exactLocation", true)
                .containsEntry("pm25", 1.23).doesNotContainKey("pm1");
        FieldSpecContract.assertPayloadKeysDeclared(new SensorCommunityAdapter("", () -> null).descriptor(),
                p.records());
    }

    @Test
    @DisplayName("BME280: pressure in pascals becomes hPa; the faulty sensors' values are kept as published")
    void pressure() {
        EnvSources.Parsed p = parsed(false);
        assertThat(EnvTestSupport.byId(p.records(), "829").payload()).containsEntry("tempC", 14.37)
                .containsEntry("humidityPct", 90.69).containsEntry("pressureHpa", 1623.7);
        assertThat(EnvTestSupport.byId(p.records(), "5356").payload()).containsEntry("tempC", -146.22)
                .containsEntry("pressureHpa", 651.0);
        assertThat(SensorCommunityParser.hectopascals(101325.0)).isEqualTo(1013.3);
        assertThat(SensorCommunityParser.hectopascals(1013.25)).isEqualTo(1013.3);
    }

    @Test
    @DisplayName("includeIndoor: the two indoor sensors become indoor-air-quality records")
    void indoor() {
        EnvSources.Parsed p = parsed(true);
        assertThat(p.records()).hasSize(9);
        assertThat(p.records()).filteredOn(r -> r.kind().equals("indoor-air-quality"))
                .extracting(NormalizedRecord::externalId).containsExactly("24729", "24730");
        assertThat(EnvTestSupport.byId(p.records(), "24730").payload()).containsEntry("tempC", 25.8)
                .containsEntry("humidityPct", 37.8);
        FieldSpecContract.assertPayloadKeysDeclared(new SensorCommunityAdapter("", () -> null).descriptor(),
                p.records());
    }

    @Test
    @DisplayName("readings without a sensor id, a timestamp or a location are skipped; a non-array is a format error")
    void skipsAndErrors() {
        String body = "[{\"timestamp\": \"2026-10-13 21:30:00\", \"location\": {\"latitude\": \"52.5\", \"longitude\":"
                + " \"13.4\"}, \"sensor\": {}, \"sensordatavalues\": [{\"value_type\": \"P1\", \"value\": \"1\"}]},"
                + "{\"timestamp\": \"yesterday\", \"location\": {\"latitude\": \"52.5\", \"longitude\": \"13.4\"},"
                + " \"sensor\": {\"id\": 1}, \"sensordatavalues\": [{\"value_type\": \"P1\", \"value\": \"1\"}]},"
                + "{\"timestamp\": \"2026-10-13 21:30:00\", \"location\": {\"latitude\": \"\", \"longitude\": \"\"},"
                + " \"sensor\": {\"id\": 2}, \"sensordatavalues\": [{\"value_type\": \"P1\", \"value\": \"1\"}]}]";
        EnvSources.Parsed p = SensorCommunityParser.parse(body.getBytes(StandardCharsets.UTF_8), false,
                EnvTestSupport.NOW, Duration.ofHours(6));
        assertThat(p.records()).isEmpty();
        assertThat(p.skipped()).containsEntry(MappedRecord.MISSING_ID, 1)
                .containsEntry(MappedRecord.MISSING_OBSERVED_AT, 1).containsEntry(EnvSources.MISSING_LOCATION, 1);
        assertThatThrownBy(() -> SensorCommunityParser.parse("{}".getBytes(StandardCharsets.UTF_8), false,
                EnvTestSupport.NOW, Duration.ofHours(6))).isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("area: box up to 10 x 10 degrees, area up to 500 km, one country code; anything else refused")
    void area() {
        assertThat(SensorCommunityArea.check("box=52.3,13.0,52.7,13.8")).isNull();
        assertThat(SensorCommunityArea.check("box=52.7,13.8,52.3,13.0")).as("corners in either order").isNull();
        assertThat(SensorCommunityArea.check("box=40,0,51,5")).contains("10");
        assertThat(SensorCommunityArea.check("box=40,0,40,5")).contains("area");
        assertThat(SensorCommunityArea.check("box=95,0,96,1")).contains("latitudes");
        assertThat(SensorCommunityArea.check("area=52.52,13.405,10")).isNull();
        assertThat(SensorCommunityArea.check("area=52.52,13.405,0")).contains("radius");
        assertThat(SensorCommunityArea.check("area=52.52,13.405,501")).contains("radius");
        assertThat(SensorCommunityArea.check("country=de")).isNull();
        assertThat(SensorCommunityArea.check("country=DEU")).isNotNull();
        assertThat(SensorCommunityArea.check("type=SDS011")).isNotNull();
        assertThat(SensorCommunityArea.check(null)).contains("required");
        assertThat(SensorCommunityArea.path(" Box = 52.3, 13.0, 52.7, 13.8 ")).isEqualTo("box=52.3,13.0,52.7,13.8");
        assertThat(SensorCommunityArea.path("country=de")).isEqualTo("country=DE");
        assertThatThrownBy(() -> SensorCommunityArea.path("bbox=1,2,3,4")).isInstanceOf(IllegalArgumentException.class);

        SensorCommunityAdapter adapter = new SensorCommunityAdapter("", () -> null);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("area", "area=52.52,13.405,10")))).isEmpty();
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("area", "box=0,0,20,20")))).containsKey("area");
        assertThat(adapter.checkParams(ValidatedParams.empty())).containsKey("area");
        assertThat(adapter.filterUri("box=52.3,13.0,52.7,13.8"))
                .hasToString("https://data.sensor.community/airrohr/v1/filter/box=52.3,13.0,52.7,13.8");
    }

    @Test
    @DisplayName("descriptor valid, area required, both kinds; snapshot contract across five minutes")
    void descriptorAndSnapshot() {
        SensorCommunityAdapter adapter = new SensorCommunityAdapter("", () -> null);
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        assertThat(adapter.descriptor().kinds()).containsExactlyInAnyOrder("air-quality", "indoor-air-quality");
        assertThat(adapter.descriptor().params()).filteredOn(s -> s.name().equals("area")).singleElement()
                .satisfies(s -> assertThat(s.required()).isTrue());
        assertThat(adapter.descriptor().recordTtl()).isEqualTo(Duration.ofHours(1));
        NormalizedRecord r = EnvTestSupport.byId(parsed(false).records(), "829");
        SnapshotContract.assertStableStaticHash(adapter.descriptor(),
                List.of(r, EnvTestSupport.later(r, Duration.ofMinutes(5))));
    }
}
