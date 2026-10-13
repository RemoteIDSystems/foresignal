package ai.foresignal.source.adapters.space.swpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link SwpcProductsParser} on the real scales, Kp and X-ray files of 2026-10-13, and the {@code swpc-kp} and
 * {@code swpc-xray} descriptors.
 */
class SwpcProductsParserTest {

    static byte[] bytes(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("scales: current R, S, G with their words and the day 1-3 probabilities; observedAt from today's stamp")
    void scales() {
        NormalizedRecord r = SwpcProductsParser.scales(AdapterFixtures.bytes("swpc-alerts", "noaa-scales.json"));
        assertThat(r.externalId()).isEqualTo("current");
        assertThat(r.kind()).isEqualTo("space-weather-scales");
        assertThat(r.observedAt()).isEqualTo(Instant.parse("2026-10-13T20:46:00Z"));
        assertThat(r.payload()).containsEntry("r", 0).containsEntry("s", 0).containsEntry("g", 0)
                .containsEntry("gText", "none").containsEntry("day1RMinorProb", 10).containsEntry("day1RMajorProb", 1)
                .containsEntry("day1SProb", 5).containsEntry("day1GScale", 1).containsEntry("day2GScale", 0)
                .containsEntry("day3SProb", 1);
        FieldSpecContract.assertPayloadKeysDeclared(new SwpcAlertsAdapter("").descriptor(), List.of(r));
        assertThatThrownBy(() -> SwpcProductsParser.scales(bytes("{\"1\": {}}")))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> SwpcProductsParser.scales(bytes("{\"0\": {\"DateStamp\": \"x\"}}")))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("Kp: the newest one-minute estimate with its thirds text; the newest official row ends its period")
    void kp() {
        NormalizedRecord estimated = SwpcProductsParser.kpEstimated(
                AdapterFixtures.bytes("swpc-kp", "planetary_k_index_1m.json"));
        assertThat(estimated.externalId()).isEqualTo("kp-estimated");
        assertThat(estimated.observedAt()).isEqualTo(Instant.parse("2026-10-13T20:44:00Z"));
        assertThat(estimated.eventTime()).isEqualTo(estimated.observedAt());
        assertThat(estimated.payload()).containsEntry("kp", 3.0).containsEntry("kpText", "3Z")
                .doesNotContainKeys("aRunning", "stationCount");

        NormalizedRecord official = SwpcProductsParser.kpOfficial(
                AdapterFixtures.bytes("swpc-kp", "noaa-planetary-k-index.json"));
        assertThat(official.externalId()).isEqualTo("kp-official");
        assertThat(official.eventTime()).isEqualTo(Instant.parse("2026-10-13T15:00:00Z"));
        assertThat(official.observedAt()).isEqualTo(Instant.parse("2026-10-13T18:00:00Z"));
        assertThat(official.payload()).containsEntry("kp", 2.33).containsEntry("kpText", "2P")
                .containsEntry("aRunning", 9L).containsEntry("stationCount", 8L);
        FieldSpecContract.assertPayloadKeysDeclared(new SwpcKpAdapter("").descriptor(), List.of(estimated, official));

        assertThat(SwpcProductsParser.kpText(0.0)).isEqualTo("0Z");
        assertThat(SwpcProductsParser.kpText(0.33)).isEqualTo("0P");
        assertThat(SwpcProductsParser.kpText(2.67)).isEqualTo("3M");
        assertThat(SwpcProductsParser.kpText(6.67)).isEqualTo("7M");
        assertThat(SwpcProductsParser.kpText(9.0)).isEqualTo("9Z");
        assertThat(SwpcProductsParser.kpText(null)).isNull();
        assertThat(SwpcProductsParser.kpEstimated(bytes("[]"))).isNull();
        assertThatThrownBy(() -> SwpcProductsParser.kpOfficial(bytes("{}"))).isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("Kp: the same estimate read again keeps the static hash")
    void kpSnapshot() {
        byte[] body = AdapterFixtures.bytes("swpc-kp", "planetary_k_index_1m.json");
        SnapshotContract.assertStableStaticHash(new SwpcKpAdapter("").descriptor(),
                List.of(SwpcProductsParser.kpEstimated(body), SwpcProductsParser.kpEstimated(body)));
    }

    @Test
    @DisplayName("X-ray: one flare keyed begin_time:satellite and the current class with a 4-decimal ratio")
    void xray() {
        List<NormalizedRecord> records = SwpcProductsParser.xray(
                AdapterFixtures.bytes("swpc-xray", "xray-flares-latest.json"));
        assertThat(records).hasSize(2);
        NormalizedRecord flare = records.get(0);
        assertThat(flare.externalId()).isEqualTo("2026-10-13T16:08:00Z:18");
        assertThat(flare.kind()).isEqualTo("solar-flare");
        assertThat(flare.eventTime()).isEqualTo(Instant.parse("2026-10-13T16:08:00Z"));
        assertThat(flare.observedAt()).isEqualTo(Instant.parse("2026-10-13T20:44:00Z"));
        assertThat(flare.payload()).containsEntry("beginClass", "B7.9").containsEntry("maxClass", "C4.2")
                .containsEntry("currentClass", "B6.4").containsEntry("maxTime", "2026-10-13T16:32:00Z")
                .containsEntry("endTime", "2026-10-13T16:43:00Z").containsEntry("satellite", 18L);
        NormalizedRecord current = records.get(1);
        assertThat(current.externalId()).isEqualTo("current");
        assertThat(current.kind()).isEqualTo("xray-current");
        assertThat(current.payload()).containsEntry("currentClass", "B6.4").containsEntry("currentRatio", 0.0072)
                .hasSize(2);
        FieldSpecContract.assertPayloadKeysDeclared(new SwpcXrayAdapter("").descriptor(), records);
        assertThat(SwpcProductsParser.xray(bytes("[]"))).isEmpty();
    }

    @Test
    @DisplayName("the swpc-kp and swpc-xray descriptors validate; xray has no TTL, flares keep the event window")
    void descriptors() {
        var kp = new SwpcKpAdapter("");
        assertThat(DescriptorValidator.problems(kp.descriptor(), kp)).isEmpty();
        assertThat(kp.descriptor().recordTtl()).isEqualTo(Duration.ofHours(3));
        assertThat(kp.descriptor().ttlAnchor()).isEqualTo(ai.foresignal.source.spi.TtlAnchor.OBSERVED);
        var xray = new SwpcXrayAdapter("");
        assertThat(DescriptorValidator.problems(xray.descriptor(), xray)).isEmpty();
        assertThat(xray.descriptor().recordTtl()).isNull();
        assertThat(xray.descriptor().kindRetention()).containsEntry("solar-flare", Duration.ofDays(1825))
                .containsEntry("xray-current", Duration.ofDays(365));
    }
}
