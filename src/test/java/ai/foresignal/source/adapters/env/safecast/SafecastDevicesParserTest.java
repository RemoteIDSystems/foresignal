package ai.foresignal.source.adapters.env.safecast;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.adapters.env.EnvTestSupport;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
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
 * {@link SafecastDevicesParser} on the 2026-10-13 {@code tt.safecast.org/devices} capture trimmed to 19 entries:
 * the radiation filter, the dose factors, the repeated device, unreadable times and devices without a location.
 */
class SafecastDevicesParserTest {

    static EnvSources.Parsed parsed() {
        return SafecastDevicesParser.parse(AdapterFixtures.bytes("safecast-radiation", "devices.json"),
                EnvTestSupport.NOW, SafecastDevicesParser.DEFAULT_MAX_AGE);
    }

    @Test
    @DisplayName("19 entries: 12 radiation devices; Airnotes without a tube, bad times, no location and old captures skipped")
    void filter() {
        EnvSources.Parsed p = parsed();
        assertThat(p.records()).hasSize(12).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo("radiation");
            assertThat(r.geometry()).isNotNull();
            assertThat(r.observedAt()).isEqualTo(r.eventTime());
            assertThat(r.payload()).containsKey("cpm");
        });
        assertThat(p.skipped()).containsEntry(SafecastDevicesParser.NO_TUBE, 3)
                .containsEntry(MappedRecord.MISSING_OBSERVED_AT, 2).containsEntry(EnvSources.MISSING_LOCATION, 1)
                .containsEntry(EnvSources.TOO_OLD, 1);
        assertThat(p.records()).extracting(NormalizedRecord::externalId)
                .doesNotContain("ngeigie:1", "geigiecast:61099", "safecast:1759788014", "note:dev:863740067197266");
        // The device listed twice: the 2026-10-13 capture is kept, the 2026-10-03 one is too old.
        assertThat(EnvTestSupport.byId(p.records(), "note:dev:863740067201399").observedAt())
                .isEqualTo(Instant.parse("2026-10-13T21:25:43Z"));
        FieldSpecContract.assertPayloadKeysDeclared(new SafecastRadiationAdapter("", () -> null).descriptor(),
                p.records());
    }

    @Test
    @DisplayName("dose factors: 7318u CPM / 334, else 7128ec CPM / 108; none for a 7318c or 712u tube alone")
    void doseFactors() {
        EnvSources.Parsed p = parsed();
        NormalizedRecord zen = EnvTestSupport.byId(p.records(), "geigiecast-zen:65025");
        assertThat(zen.payload()).containsEntry("cpm", Map.of("lnd_7318u", 34.0)).containsEntry("doseRateUsvH", 0.1018)
                .containsEntry("deviceClass", "geigiecast-zen");
        assertThat(zen.observedAt()).isEqualTo(Instant.parse("2026-10-13T21:31:33Z"));
        assertThat(zen.geometry().getCoordinate().y).isEqualTo(47.65443);
        NormalizedRecord pointcast = EnvTestSupport.byId(p.records(), "pointcast:10022");
        assertThat(pointcast.payload()).containsEntry("cpm", Map.of("lnd_7128ec", 14.0, "lnd_7318u", 26.0))
                .containsEntry("doseRateUsvH", 0.0778);
        NormalizedRecord radnote = EnvTestSupport.byId(p.records(), "note:dev:860264050368886");
        assertThat(radnote.payload()).containsEntry("cpm", Map.of("lnd_7318c", 41.40625))
                .containsEntry("locationName", "Chervona Sloboda").containsEntry("country", "UA")
                .doesNotContainKey("doseRateUsvH");
        assertThat(EnvTestSupport.byId(p.records(), "note:dev:864475044186905").payload())
                .doesNotContainKey("doseRateUsvH");

        assertThat(SafecastDevicesParser.doseRate(Map.of("lnd_7128ec", 108.0))).isEqualTo(1.0);
        assertThat(SafecastDevicesParser.doseRate(Map.of("lnd_7318u", 334.0, "lnd_7128ec", 10.0))).isEqualTo(1.0);
        assertThat(SafecastDevicesParser.doseRate(Map.of("lnd_712u", 10.0))).isNull();
    }

    @Test
    @DisplayName("maxAge: with 1 h, captures before 20:35 are skipped")
    void maxAge() {
        EnvSources.Parsed p = SafecastDevicesParser.parse(AdapterFixtures.bytes("safecast-radiation", "devices.json"),
                EnvTestSupport.NOW, Duration.ofHours(1));
        assertThat(p.records()).allSatisfy(r -> assertThat(r.observedAt())
                .isAfterOrEqualTo(EnvTestSupport.NOW.minus(Duration.ofHours(1))));
        assertThat(p.records()).hasSize(12);
    }

    @Test
    @DisplayName("a body that is not a JSON array is a format error")
    void formatErrors() {
        assertThatThrownBy(() -> SafecastDevicesParser.parse("{}".getBytes(StandardCharsets.UTF_8), EnvTestSupport.NOW,
                Duration.ofHours(48))).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> SafecastDevicesParser.parse("<html>".getBytes(StandardCharsets.UTF_8),
                EnvTestSupport.NOW, Duration.ofHours(48))).isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("descriptor valid; snapshot contract: the same counts 30 minutes later keep their static hash")
    void descriptorAndSnapshot() {
        SafecastRadiationAdapter adapter = new SafecastRadiationAdapter("", () -> null);
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        assertThat(adapter.descriptor().recordTtl()).isEqualTo(Duration.ofHours(6));
        assertThat(adapter.descriptor().defaultBudget().mbPerDay()).isEqualTo(100);
        assertThat(adapter.descriptor().licence()).isEqualTo("CC0 1.0");
        NormalizedRecord r = EnvTestSupport.byId(parsed().records(), "pointcast:10036");
        SnapshotContract.assertStableStaticHash(adapter.descriptor(),
                List.of(r, EnvTestSupport.later(r, Duration.ofMinutes(30))));
    }
}
