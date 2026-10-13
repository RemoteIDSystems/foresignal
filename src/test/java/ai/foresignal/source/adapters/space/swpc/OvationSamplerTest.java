package ai.foresignal.source.adapters.space.swpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PlacePoints;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link OvationSampler} on the trimmed OVATION capture of 2026-10-13 and on hand-built grids: longitude wrap, the
 * 300 km neighborhood, a place on the antimeridian; and the {@code swpc-aurora} record and descriptor.
 */
class OvationSamplerTest {

    static final Instant OBS = Instant.parse("2026-10-13T20:34:00Z");
    static final Instant FORECAST = Instant.parse("2026-10-13T21:21:00Z");

    static OvationSampler grid(int[][] cells) {
        byte[] b = new byte[OvationSampler.LONS * OvationSampler.LATS];
        for (int[] c : cells) {
            b[OvationSampler.index(c[0], c[1])] = (byte) c[2];
        }
        return OvationSampler.of(b, OBS, FORECAST);
    }

    @Test
    @DisplayName("the capture: times, cells by rounded longitude and latitude, a missing cell reads 0")
    void capture() {
        OvationSampler g = OvationSampler.parse(AdapterFixtures.bytes("swpc-aurora", "ovation_aurora_latest.json"));
        assertThat(g.observationTime()).isEqualTo(OBS);
        assertThat(g.forecastTime()).isEqualTo(FORECAST);
        assertThat(g.probability(10.0, 69.6)).isEqualTo(13);
        assertThat(g.maxWithin(10.0, 69.6, 300)).isEqualTo(14);
        assertThat(g.probability(-105.27, 64.84)).isEqualTo(3);
        assertThat(g.maxWithin(-105.27, 64.84, 300)).isEqualTo(6);
        assertThat(g.probability(-97.0, 32.0)).as("outside the trimmed band").isZero();
        assertThat(g.probability(-0.4, 60.0)).as("-0.4 rounds to longitude 0").isEqualTo(g.probability(0.2, 60.0));
    }

    @Test
    @DisplayName("longitude wrap: -1 is cell 359, 359.6 is cell 0, -180 and 180 are one cell")
    void lonWrap() {
        OvationSampler g = grid(new int[][] {{359, 60, 40}, {0, 61, 50}, {180, 65, 70}});
        assertThat(g.probability(-1.0, 60.0)).isEqualTo(40);
        assertThat(g.probability(359.6, 61.0)).isEqualTo(50);
        assertThat(g.probability(-180.0, 65.0)).isEqualTo(70);
        assertThat(g.probability(180.0, 65.0)).isEqualTo(70);
        assertThat(g.probability(0.0, 95.0)).as("latitude clamped").isZero();
    }

    @Test
    @DisplayName("300 km: a cell 2° of latitude away counts, 3° does not; across 0° longitude it counts")
    void neighborhood() {
        OvationSampler g = grid(new int[][] {{20, 62, 30}, {20, 63, 90}, {358, 60, 55}});
        assertThat(g.maxWithin(20.0, 60.0, 300)).as("222 km away; 333 km is out").isEqualTo(30);
        assertThat(g.maxWithin(20.0, 60.0, 400)).isEqualTo(90);
        assertThat(g.maxWithin(1.0, 60.0, 300)).as("3° of longitude at 60° N is about 167 km").isEqualTo(55);
        assertThat(g.maxWithin(1.0, 60.0, 100)).isZero();
    }

    @Test
    @DisplayName("a place on the antimeridian sees cells on both sides, and a pole place sees every longitude ring")
    void antimeridian() {
        OvationSampler g = grid(new int[][] {{178, 65, 20}, {182, 65, 35}, {0, 89, 60}});
        assertThat(g.probability(179.8, 65.0)).as("rounds to cell 180, which is empty").isZero();
        assertThat(g.maxWithin(179.8, 65.0, 300)).isEqualTo(35);
        assertThat(g.maxWithin(-179.6, 65.0, 300)).isEqualTo(35);
        assertThat(g.maxWithin(180.0, 89.5, 300)).isEqualTo(60);
    }

    @Test
    @DisplayName("format errors: no coordinates, no times")
    void formatErrors() {
        assertThatThrownBy(() -> OvationSampler.parse("{}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> OvationSampler.parse("{\"coordinates\": [[0, 60, 5]]}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        OvationSampler g = OvationSampler.parse(("{\"Observation Time\": \"2026-10-13T20:34:00Z\", \"Forecast Time\":"
                + " \"2026-10-13T21:21:00Z\", \"coordinates\": [[0, 60, 5], [\"x\", 1, 2], [1, 95, 9], [2, 61, 140]]}")
                .getBytes(StandardCharsets.UTF_8));
        assertThat(g.probability(0, 60)).isEqualTo(5);
        assertThat(g.probability(2, 61)).as("capped at 100").isEqualTo(100);
    }

    @Test
    @DisplayName("the record: place id, place point, name, probability, max within 300 km, forecast time; private source")
    void record() {
        OvationSampler g = grid(new int[][] {{256, 65, 12}, {255, 66, 25}});
        PlacePoints.Summary place = new PlacePoints.Summary("cabin", "Cabin", -104.2, 64.9, List.of(), 3);
        NormalizedRecord r = SwpcAuroraAdapter.record(place, g);
        assertThat(r.externalId()).isEqualTo("cabin");
        assertThat(r.kind()).isEqualTo("aurora-probability");
        assertThat(r.observedAt()).isEqualTo(OBS);
        assertThat(r.eventTime()).isEqualTo(FORECAST);
        assertThat(r.geometry().getCoordinate().x).isEqualTo(-104.2);
        assertThat(r.payload()).containsEntry("placeName", "Cabin").containsEntry("probability", 12)
                .containsEntry("maxWithin300Km", 25).containsEntry("forecastTime", FORECAST.toString());
        SwpcAuroraAdapter adapter = new SwpcAuroraAdapter(() -> null, "");
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        assertThat(adapter.descriptor().privateSource()).isTrue();
        FieldSpecContract.assertPayloadKeysDeclared(adapter.descriptor(), List.of(r));
        SnapshotContract.assertStableStaticHash(adapter.descriptor(), List.of(r, SwpcAuroraAdapter.record(place, g)));
        assertThat(SwpcAuroraAdapter.fingerprint(List.of(place))).isEqualTo("cabin@3\n");
    }
}
