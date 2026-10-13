package ai.foresignal.source.adapters.env;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.source.support.MappedRecord;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;

/** {@link EnvSources}: the newest-per-entity collector, {@code maxAge}, UCUM codes, boxes and packed state. */
class EnvSourcesTest {

    static MappedRecord at(String id, String time) {
        Instant t = Instant.parse(time);
        return MappedRecord.of(NormalizedRecord.builder(id, "air-quality").observedAt(t).eventTime(t).point(1, 1)
                .put("v", 1.0).build());
    }

    @Test
    @DisplayName("collector: newest per entity wins in any order, repeats and old entries are counted")
    void collector() {
        EnvSources.Collector c = new EnvSources.Collector(Instant.parse("2026-10-13T21:00:00Z"), Duration.ofHours(6));
        c.add(at("a", "2026-10-13T20:00:00Z"));
        c.add(at("a", "2026-10-13T20:30:00Z"));
        c.add(at("a", "2026-10-13T20:10:00Z"));
        c.add(at("b", "2026-10-13T10:00:00Z"));
        c.add(MappedRecord.skipped(MappedRecord.MISSING_ID));
        c.skip("custom");
        EnvSources.Parsed p = c.result();
        assertThat(p.records()).singleElement().satisfies(r -> {
            assertThat(r.externalId()).isEqualTo("a");
            assertThat(r.observedAt()).isEqualTo(Instant.parse("2026-10-13T20:30:00Z"));
        });
        assertThat(p.skipped()).containsEntry(MappedRecord.DUPLICATE_ID, 2).containsEntry(EnvSources.TOO_OLD, 1)
                .containsEntry(MappedRecord.MISSING_ID, 1).containsEntry("custom", 1);
        EnvSources.Collector keepAll = new EnvSources.Collector(null, null);
        keepAll.add(at("b", "2000-01-01T00:00:00Z"));
        assertThat(keepAll.result().records()).hasSize(1);
    }

    @Test
    @DisplayName("maxAge: the parameter when set and positive, else the fallback")
    void maxAge() {
        assertThat(EnvSources.maxAge(ValidatedParams.empty(), Duration.ofHours(6))).isEqualTo(Duration.ofHours(6));
        assertThat(EnvSources.maxAge(null, Duration.ofHours(6))).isEqualTo(Duration.ofHours(6));
        assertThat(EnvSources.maxAge(ValidatedParams.of(Map.of("maxAge", Duration.ofHours(2))), Duration.ofHours(6)))
                .isEqualTo(Duration.ofHours(2));
        assertThat(EnvSources.maxAge(ValidatedParams.of(Map.of("maxAge", Duration.ZERO)), Duration.ofHours(6)))
                .isEqualTo(Duration.ofHours(6));
        assertThat(EnvSources.maxAgeParam("6h").defaultValue()).isEqualTo("6h");
    }

    @Test
    @DisplayName("helpers: UCUM codes, points, boxes, rounding and packed state")
    void helpers() {
        assertThat(EnvSources.ucum("µg/m³")).isEqualTo("ug/m3");
        assertThat(EnvSources.ucum("UG/M3")).isEqualTo("ug/m3");
        assertThat(EnvSources.ucum("ppm")).isEqualTo("[ppm]");
        assertThat(EnvSources.ucum("PPB")).isEqualTo("[ppb]");
        assertThat(EnvSources.ucum("c")).isEqualTo("c");
        assertThat(EnvSources.ucum(" ")).isNull();
        assertThat(EnvSources.validPoint(0.0, -3.051)).isTrue();
        assertThat(EnvSources.validPoint(0.0, 0.0)).isFalse();
        assertThat(EnvSources.validPoint(91.0, 0.0)).isFalse();
        assertThat(EnvSources.validPoint(null, 1.0)).isFalse();
        assertThat(EnvSources.inBox(new Envelope(13, 14, 52, 53), 13.4, 52.5)).isTrue();
        assertThat(EnvSources.inBox(new Envelope(13, 14, 52, 53), 15, 52.5)).isFalse();
        assertThat(EnvSources.inBox(null, 15, 52.5)).isTrue();
        assertThat(EnvSources.round(1.005, 2)).isEqualTo(1.01);
        assertThat(EnvSources.round(-0.0001, 2)).isEqualTo(0.0);
        assertThat(EnvSources.round(Double.NaN, 2)).isNull();
        String big = "x".repeat(40_000);
        String packed = EnvSources.pack(big);
        assertThat(packed).startsWith(EnvSources.GZIP_PREFIX).hasSizeLessThan(big.length());
        assertThat(EnvSources.unpack(packed)).isEqualTo(big);
        assertThat(EnvSources.pack("small")).isEqualTo("small");
        assertThat(EnvSources.unpack("small")).isEqualTo("small");
    }
}
