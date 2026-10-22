package ai.foresignal.source.formats.readsb;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;

/** {@link AircraftRegion}: Shadowbroker's two fixed circle sets, their path text and envelopes. */
class AircraftRegionTest {

    @Test
    @DisplayName("the six worldwide circles: 2,000 nm each, 2,500 nm for Africa")
    void world() {
        assertThat(AircraftRegion.WORLD).extracting(AircraftRegion::name).containsExactly("North America", "Europe",
                "Asia", "Australia", "Africa", "South America");
        assertThat(AircraftRegion.WORLD).extracting(AircraftRegion::radiusNm)
                .containsExactly(2_000, 2_000, 2_000, 2_000, 2_500, 2_000);
        assertThat(AircraftRegion.WORLD.getFirst()).isEqualTo(new AircraftRegion("North America", 39.8, -98.5, 2_000));
    }

    @Test
    @DisplayName("the eight blind spots: 250 nm each")
    void blindSpots() {
        assertThat(AircraftRegion.BLIND_SPOTS).hasSize(8).extracting(AircraftRegion::radiusNm).containsOnly(250);
        assertThat(AircraftRegion.BLIND_SPOTS).extracting(AircraftRegion::name).containsExactly("Yekaterinburg",
                "Novosibirsk", "Krasnoyarsk", "Vladivostok", "Urumqi", "Chengdu", "Lagos-Accra", "Addis Ababa");
    }

    @Test
    @DisplayName("path text has no exponent and no trailing zeros")
    void text() {
        AircraftRegion africa = AircraftRegion.WORLD.get(4);
        assertThat(africa.latText()).isEqualTo("0");
        assertThat(africa.lonText()).isEqualTo("20");
        assertThat(AircraftRegion.WORLD.getFirst().lonText()).isEqualTo("-98.5");
    }

    @Test
    @DisplayName("envelopes: a circle's bounding box, the whole longitude range only at a pole or the antimeridian")
    void envelope() {
        Envelope small = AircraftRegion.BLIND_SPOTS.getFirst().envelope();
        assertThat(small.getMinY()).isCloseTo(56.8 - 250 / 60.0, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(small.getMinX()).isGreaterThan(-180.0);
        // 2,000 nm is 33.3 degrees of latitude; at 39.8 N that is 43.4 degrees of longitude.
        Envelope northAmerica = AircraftRegion.WORLD.getFirst().envelope();
        assertThat(northAmerica.getMinX()).isCloseTo(-141.9, org.assertj.core.data.Offset.offset(0.1));
        Envelope world = AircraftRegion.envelopeOf(AircraftRegion.WORLD);
        assertThat(world.getMinX()).isEqualTo(northAmerica.getMinX());
        assertThat(world.getMaxX()).isCloseTo(169.8, org.assertj.core.data.Offset.offset(0.1));
        assertThat(world.getMinY()).isCloseTo(-58.3, org.assertj.core.data.Offset.offset(0.1));
        assertThat(new AircraftRegion("pole", 80.0, 0.0, 2_000).envelope().getMinX()).isEqualTo(-180.0);
        assertThat(new AircraftRegion("dateline", 0.0, 179.0, 250).envelope().getMaxX()).isEqualTo(180.0);
    }

    @RepeatedTest(20)
    @DisplayName("the jitter is uniform between zero and its bound")
    void jitter() {
        assertThat(RegionPoller.randomUpTo(Duration.ofMillis(400))).isBetween(Duration.ZERO, Duration.ofMillis(400));
        assertThat(RegionPoller.randomUpTo(Duration.ZERO)).isEqualTo(Duration.ZERO);
    }
}
