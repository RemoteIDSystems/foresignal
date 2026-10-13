package ai.foresignal.source.adapters.env.airq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FieldThreshold;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link AirqLocalParsers}, {@link AirqLocalAdapter} and {@link AirqLocalFamily}: each device type from its
 * documented sample, °F to °C, indoor inference and the operator's override, PurpleAir A/B disagreement, the slot
 * descriptor (private, LOCAL_ONLY, SAMPLED) and parameter checks.
 */
class AirqLocalParsersTest {

    static final SourceDescriptor DESCRIPTOR = new AirqLocalAdapter(1).descriptor();

    @Test
    @DisplayName("PurpleAir outdoor: channel means, 71 °F = 21.7 °C, its own location, no disagreement")
    void purpleAir() {
        AirqLocalParsers.Reading reading = AirqLocalParsers.purpleAir(AdapterFixtures.bytes("airq-local", "purpleair.json"));
        assertThat(reading.indoorHint()).isFalse();
        NormalizedRecord r = AirqLocalParsers.record(reading, "purpleair", "airq-local-1", null, null, null);
        assertThat(r.externalId()).isEqualTo("purpleair:84:f3:eb:7b:c8:ee");
        assertThat(r.kind()).isEqualTo("air-quality");
        assertThat(r.observedAt()).as("RECEIVED").isNull();
        assertThat(r.geometry().getCoordinate().x).isEqualTo(-105.2705);
        assertThat(r.geometry().getCoordinate().y).isEqualTo(40.015);
        assertThat(r.payload()).containsEntry("pm25", 3.65).containsEntry("pm1", 2.29).containsEntry("pm10", 4.11)
                .containsEntry("tempC", 21.7).containsEntry("humidityPct", 28.0).containsEntry("pressureHpa", 838.6)
                .containsEntry("deviceModel", "PurpleAir 2.0").containsEntry("channelDisagreement", false)
                .doesNotContainKeys("co2Ppm", "tvocIndex", "noxIndex");
        FieldSpecContract.assertPayloadKeysDeclared(DESCRIPTOR, List.of(r));
    }

    @Test
    @DisplayName("PurpleAir inside with channels 14.6 and 3.2 µg/m³ apart: indoor kind, disagreement flagged")
    void purpleAirDisagreement() {
        NormalizedRecord r = AirqLocalParsers.record(AirqLocalParsers.purpleAir(
                AdapterFixtures.bytes("airq-local", "purpleair-disagree.json")), "purpleair", "airq-local-1", null, null,
                null);
        assertThat(r.kind()).isEqualTo("indoor-air-quality");
        assertThat(r.payload()).containsEntry("channelDisagreement", true).containsEntry("pm25", 8.9)
                .containsEntry("tempC", 20.0);
        // The operator's override and location win over the device's.
        NormalizedRecord overridden = AirqLocalParsers.record(AirqLocalParsers.purpleAir(
                AdapterFixtures.bytes("airq-local", "purpleair-disagree.json")), "purpleair", "airq-local-1", false,
                40.1, -105.1);
        assertThat(overridden.kind()).isEqualTo("air-quality");
        assertThat(overridden.geometry().getCoordinate().y).isEqualTo(40.1);

        assertThat(AirqLocalParsers.disagree(14.6, 3.2)).isTrue();
        assertThat(AirqLocalParsers.disagree(10.0, 4.0)).as("6 apart, 86% of their mean").isTrue();
        assertThat(AirqLocalParsers.disagree(30.0, 24.0)).as("6 apart, 22%").isFalse();
        assertThat(AirqLocalParsers.disagree(3.0, 0.5)).as("2.5 apart").isFalse();
        assertThat(AirqLocalParsers.fahrenheitToCelsius(32)).isEqualTo(0.0);
        assertThat(AirqLocalParsers.fahrenheitToCelsius(71)).isEqualTo(21.7);
    }

    @Test
    @DisplayName("airrohr: SDS011 and BME280 values, pascals to hPa, the slot id as externalId, outdoor by default")
    void airrohr() {
        AirqLocalParsers.Reading reading = AirqLocalParsers.airrohr(AdapterFixtures.bytes("airq-local", "airrohr.json"));
        assertThat(reading.serial()).isNull();
        NormalizedRecord r = AirqLocalParsers.record(reading, "airrohr", "airq-local-2", null, 48.78, 9.18);
        assertThat(r.externalId()).isEqualTo("airrohr:airq-local-2");
        assertThat(r.kind()).isEqualTo("air-quality");
        assertThat(r.payload()).containsEntry("pm10", 12.4).containsEntry("pm25", 6.1).containsEntry("tempC", 18.5)
                .containsEntry("humidityPct", 64.21).containsEntry("pressureHpa", 1013.3)
                .containsEntry("deviceModel", "airrohr NRZ-2020-133").doesNotContainKey("channelDisagreement");
        FieldSpecContract.assertPayloadKeysDeclared(DESCRIPTOR, List.of(r));
        assertThatThrownBy(() -> AirqLocalParsers.record(reading, "airrohr", "airq-local-2", null, null, null))
                .isInstanceOf(UpstreamFormatException.class).hasMessageContaining("lat and lon");
    }

    @Test
    @DisplayName("AirGradient: I- model indoor, O- model outdoor, CO2 and the Sensirion indexes")
    void airGradient() {
        NormalizedRecord indoor = AirqLocalParsers.record(AirqLocalParsers.airGradient(
                AdapterFixtures.bytes("airq-local", "airgradient-indoor.json")), "airgradient", "airq-local-1", null,
                40.0, -105.0);
        assertThat(indoor.externalId()).isEqualTo("airgradient:ecda3b1eaaaf");
        assertThat(indoor.kind()).isEqualTo("indoor-air-quality");
        assertThat(indoor.payload()).containsEntry("pm1", 3.0).containsEntry("pm25", 7.0).containsEntry("pm10", 8.0)
                .containsEntry("co2Ppm", 447.0).containsEntry("tempC", 25.9).containsEntry("humidityPct", 43.0)
                .containsEntry("tvocIndex", 100.0).containsEntry("noxIndex", 1.0).containsEntry("deviceModel", "I-9PSL");
        NormalizedRecord outdoor = AirqLocalParsers.record(AirqLocalParsers.airGradient(
                AdapterFixtures.bytes("airq-local", "airgradient-outdoor.json")), "airgradient", "airq-local-1", null,
                40.0, -105.0);
        assertThat(outdoor.kind()).isEqualTo("air-quality");
        assertThat(outdoor.payload()).containsEntry("deviceModel", "O-1PST").doesNotContainKey("co2Ppm");
        FieldSpecContract.assertPayloadKeysDeclared(DESCRIPTOR, List.of(indoor, outdoor));
    }

    @Test
    @DisplayName("bodies that are not the device's JSON are format errors")
    void formatErrors() {
        assertThatThrownBy(() -> AirqLocalParsers.purpleAir("<html>".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> AirqLocalParsers.purpleAir("{\"SensorId\": \"x\"}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> AirqLocalParsers.airrohr("{\"software_version\": \"x\"}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> AirqLocalParsers.airGradient("[]".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("slot descriptor: private, LOCAL_ONLY and overridable, RECEIVED, SAMPLED 60 s / 8 min, thresholds")
    void descriptor() {
        AirqLocalAdapter adapter = new AirqLocalAdapter(3);
        SourceDescriptor d = adapter.descriptor();
        assertThat(d.id()).isEqualTo("airq-local-3");
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.privateSource()).isTrue();
        assertThat(d.egress()).isEqualTo(Egress.LOCAL_ONLY);
        assertThat(d.egressOverridable()).isTrue();
        assertThat(d.followRedirects()).isFalse();
        assertThat(d.observedAtMode()).isEqualTo(ObservedAtMode.RECEIVED);
        assertThat(d.recordTtl()).isEqualTo(Duration.ofMinutes(10));
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofSeconds(60));
        assertThat(d.minInterval()).isEqualTo(Duration.ofSeconds(30));
        assertThat(d.defaultIngestPolicy()).isInstanceOfSatisfying(IngestPolicy.Sampled.class, p -> {
            assertThat(p.minInterval()).isEqualTo(Duration.ofSeconds(60));
            assertThat(p.keyframe()).isEqualTo(Duration.ofMinutes(8));
            assertThat(p.thresholds()).isTrue();
        });
        assertThat(d.significantFields()).containsExactly("deviceModel", "channelDisagreement");
        assertThat(d.thresholds()).extracting(FieldThreshold::field)
                .containsExactly("pm25", "pm10", "co2Ppm", "tempC", "humidityPct");
        assertThat(d.thresholds()).extracting(FieldThreshold::delta).containsExactly(5.0, 10.0, 100.0, 1.0, 5.0);
    }

    @Test
    @DisplayName("family: one slot per local receiver slot; out-of-range counts refuse to start")
    void family() {
        assertThat(new AirqLocalFamily(4).adapters()).extracting(SourceAdapter::id)
                .containsExactly("airq-local-1", "airq-local-2", "airq-local-3", "airq-local-4");
        assertThatThrownBy(() -> new AirqLocalFamily(0)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new AirqLocalFamily(17)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("checkParams: address only, coordinates together and in range, required for airrohr and AirGradient")
    void checkParams() {
        AirqLocalAdapter adapter = new AirqLocalAdapter(1);
        assertThat(adapter.checkParams(params("purpleair", "http://192.168.1.60", null, null))).isEmpty();
        assertThat(adapter.checkParams(params("airgradient", "http://192.168.1.61", 40.0, -105.0))).isEmpty();
        assertThat(adapter.checkParams(params("airgradient", "http://192.168.1.61", null, null))).containsKey("lat");
        assertThat(adapter.checkParams(params("airrohr", "http://192.168.1.62", 40.0, null))).containsKey("lon");
        assertThat(adapter.checkParams(params("purpleair", "http://192.168.1.60/json?live=true", null, null)))
                .containsKey("url");
        assertThat(adapter.checkParams(params("purpleair", "http://192.168.1.60", 95.0, 0.0))).containsKey("lat");
        assertThat(AirqLocalAdapter.path("purpleair")).isEqualTo("/json?live=true");
        assertThat(AirqLocalAdapter.path("airrohr")).isEqualTo("/data.json");
        assertThat(AirqLocalAdapter.path("airgradient")).isEqualTo("/measures/current");
    }

    @Test
    @DisplayName("snapshot contract: the same reading polled twice keeps its static hash")
    void snapshot() {
        AirqLocalParsers.Reading reading = AirqLocalParsers.airGradient(
                AdapterFixtures.bytes("airq-local", "airgradient-indoor.json"));
        SnapshotContract.assertStableStaticHash(DESCRIPTOR, List.of(
                AirqLocalParsers.record(reading, "airgradient", "airq-local-1", null, 40.0, -105.0),
                AirqLocalParsers.record(reading, "airgradient", "airq-local-1", null, 40.0, -105.0)));
    }

    private static ValidatedParams params(String type, String url, Double lat, Double lon) {
        Map<String, Object> values = new HashMap<>();
        values.put("deviceType", type);
        values.put("url", URI.create(url));
        if (lat != null) {
            values.put("lat", lat);
        }
        if (lon != null) {
            values.put("lon", lon);
        }
        return ValidatedParams.of(values);
    }
}
