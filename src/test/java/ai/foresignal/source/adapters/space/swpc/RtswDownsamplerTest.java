package ai.foresignal.source.adapters.space.swpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
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
 * {@link RtswDownsampler} on the real summary files and the trimmed one-minute wind file of 2026-10-13: the active
 * row is chosen among three spacecraft, values are rounded so noise is not a version, and the large file is due at
 * most once per {@code densityInterval}.
 */
class RtswDownsamplerTest {

    static final Instant T = Instant.parse("2026-10-13T20:40:00Z");

    @Test
    @DisplayName("the active row: the newest SOLAR1 row, skipping newer inactive IMAP and ACE rows")
    void activeRow() {
        RtswDownsampler.Density d = RtswDownsampler.activeDensity(AdapterFixtures.bytes("swpc-rtsw", "rtsw_wind_1m.json"));
        assertThat(d.spacecraft()).isEqualTo("SOLAR1");
        assertThat(d.at()).isEqualTo(T);
        assertThat(d.densityCc()).isEqualTo(4.04);
        String none = "[{\"time_tag\": \"2026-10-13T20:42:03\", \"active\": false, \"source\": \"IMAP\","
                + " \"proton_density\": 1.97}, {\"time_tag\": \"2026-10-13T20:41:00\", \"active\": true,"
                + " \"source\": \"SOLAR1\", \"proton_density\": null}]";
        assertThat(RtswDownsampler.activeDensity(none.getBytes(StandardCharsets.UTF_8))).isNull();
        assertThatThrownBy(() -> RtswDownsampler.activeDensity("{}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("the summaries and the record: speed to 10 km/s, field to 0.5 nT, density to 0.1; newer time wins")
    void record() {
        RtswDownsampler.Speed speed = RtswDownsampler.speed(AdapterFixtures.bytes("swpc-rtsw", "solar-wind-speed.json"));
        RtswDownsampler.Field field = RtswDownsampler.field(AdapterFixtures.bytes("swpc-rtsw", "solar-wind-mag-field.json"));
        RtswDownsampler.Density density = RtswDownsampler.activeDensity(
                AdapterFixtures.bytes("swpc-rtsw", "rtsw_wind_1m.json"));
        assertThat(speed.speedKms()).isEqualTo(638.0);
        assertThat(speed.at()).isEqualTo(Instant.parse("2026-10-13T20:37:00Z"));
        assertThat(field.btNt()).isEqualTo(6.0);
        assertThat(field.bzNt()).isEqualTo(2.0);
        NormalizedRecord r = RtswDownsampler.record(speed, field, density);
        assertThat(r.externalId()).isEqualTo("l1");
        assertThat(r.kind()).isEqualTo("solar-wind");
        assertThat(r.observedAt()).as("the newer summary time").isEqualTo(T);
        assertThat(r.payload()).containsEntry("speedKms", 640L).containsEntry("btNt", 6.0).containsEntry("bzNt", 2.0)
                .containsEntry("densityCc", 4.0).containsEntry("spacecraft", "SOLAR1");
        FieldSpecContract.assertPayloadKeysDeclared(new SwpcRtswAdapter("").descriptor(), List.of(r));
        assertThat(RtswDownsampler.record(null, field, density)).isNull();
        assertThat(RtswDownsampler.record(speed, null, null).payload()).containsOnlyKeys("speedKms");
    }

    @Test
    @DisplayName("rounding: half-up to the step, never -0.0")
    void rounding() {
        assertThat(RtswDownsampler.roundSpeed(634.9)).isEqualTo(630L);
        assertThat(RtswDownsampler.roundSpeed(635.0)).isEqualTo(640L);
        assertThat(RtswDownsampler.roundField(-0.2)).isEqualTo(0.0);
        assertThat(RtswDownsampler.roundField(-0.26)).isEqualTo(-0.5);
        assertThat(RtswDownsampler.roundField(2.74)).isEqualTo(2.5);
        assertThat(RtswDownsampler.roundField(null)).isNull();
        assertThat(RtswDownsampler.roundDensity(4.04)).isEqualTo(4.0);
        assertThat(RtswDownsampler.roundDensity(4.05)).isEqualTo(4.1);
    }

    @Test
    @DisplayName("noise inside the rounding steps keeps the static hash")
    void snapshot() {
        RtswDownsampler.Density d = new RtswDownsampler.Density(4.04, "SOLAR1", T);
        SnapshotContract.assertStableStaticHash(new SwpcRtswAdapter("").descriptor(), List.of(
                RtswDownsampler.record(new RtswDownsampler.Speed(638.0, T), new RtswDownsampler.Field(6.1, 2.2, T), d),
                RtswDownsampler.record(new RtswDownsampler.Speed(641.0, T.plusSeconds(60)),
                        new RtswDownsampler.Field(5.9, 1.8, T.plusSeconds(60)),
                        new RtswDownsampler.Density(3.96, "SOLAR1", T))));
    }

    @Test
    @DisplayName("density cadence: due when never read or once the interval has passed; the parameter is at least 30 min")
    void cadence() {
        Duration hour = Duration.ofMinutes(60);
        assertThat(RtswDownsampler.densityDue(null, T, hour)).isTrue();
        assertThat(RtswDownsampler.densityDue(T, T.plus(Duration.ofMinutes(55)), hour)).isFalse();
        assertThat(RtswDownsampler.densityDue(T, T.plus(hour), hour)).isTrue();
        assertThat(SwpcRtswAdapter.densityInterval(ValidatedParams.empty())).isEqualTo(hour);
        assertThat(SwpcRtswAdapter.densityInterval(ValidatedParams.of(Map.of("densityInterval", Duration.ofMinutes(10)))))
                .isEqualTo(Duration.ofMinutes(30));
        assertThat(SwpcRtswAdapter.densityInterval(ValidatedParams.of(Map.of("densityInterval", Duration.ofHours(2)))))
                .isEqualTo(Duration.ofHours(2));
        SwpcRtswAdapter adapter = new SwpcRtswAdapter("");
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("densityInterval", Duration.ofMinutes(10)))))
                .containsKey("densityInterval");
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("densityInterval", Duration.ofMinutes(30))))).isEmpty();
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        assertThat(adapter.descriptor().defaultBudget().mbPerDay()).isEqualTo(80);
    }
}
