package ai.foresignal.source.adapters.passes;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.place.PlaceView;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.adapters.celestrak.CelestrakGpParser;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.space.Observer;
import ai.foresignal.space.OmmElements;
import ai.foresignal.space.PredictedPass;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The pure parts of {@link SatellitePassesGenerator} (prompt 064). */
class SatellitePassesGeneratorTest {

    private static final Instant NOW = Instant.parse("2026-10-13T06:00:00Z");

    private static PlaceView place(Map<String, Object> attributes) {
        return new PlaceView("home", "Home", "point", Geo.point(-97.7431, 30.2672), 10.0, null, null, "America/Chicago",
                "user", List.of(), List.of(), attributes, false, 1, NOW, NOW);
    }

    private static OmmElements iss() {
        return CelestrakGpParser.parseCsv(AdapterFixtures.bytes("celestrak-gp", "stations.csv"), "stations")
                .elements().get(0);
    }

    @Test
    @DisplayName("times round to the nearest 10 s; ids parse back to their place")
    void rounding() {
        assertThat(SatellitePassesGenerator.round(Instant.parse("2026-10-13T12:06:34.9Z")))
                .isEqualTo(Instant.parse("2026-10-13T12:06:30Z"));
        assertThat(SatellitePassesGenerator.round(Instant.parse("2026-10-13T12:06:35.0Z")))
                .isEqualTo(Instant.parse("2026-10-13T12:06:40Z"));
        assertThat(SatellitePassesGenerator.placeIdOf("place:home:25544:29840000")).isEqualTo("home");
        assertThat(SatellitePassesGenerator.placeIdOf("home:iss:1")).isNull();
    }

    @Test
    @DisplayName("the record: id, eventTime and validFrom = AOS, rounded payload, ground track, not yet imminent")
    void record() {
        PredictedPass pass = new PredictedPass(Instant.parse("2026-10-13T12:06:33.49Z"),
                Instant.parse("2026-10-13T12:09:51.25Z"), Instant.parse("2026-10-13T12:13:10.41Z"), 50.7795, 238.74,
                33.72, List.of(new double[] {-110.123, 20.0}, new double[] {-90.0, 35.4567}));
        NormalizedRecord r = SatellitePassesGenerator.record(place(Map.of()), "25544", iss(), pass, NOW);
        Instant aos = Instant.parse("2026-10-13T12:06:30Z");
        assertThat(r.externalId()).isEqualTo("place:home:25544:" + aos.getEpochSecond() / 60);
        assertThat(r.eventTime()).isEqualTo(aos);
        assertThat(r.validFrom()).isEqualTo(aos);
        assertThat(r.payload()).containsEntry("satellite", "ISS (ZARYA)").containsEntry("norad", 25544L)
                .containsEntry("place", "home").containsEntry("status", "predicted")
                .containsEntry("aos", "2026-10-13T12:06:30Z").containsEntry("tca", "2026-10-13T12:09:50Z")
                .containsEntry("los", "2026-10-13T12:13:10Z").containsEntry("maxElevationDeg", 50.8)
                .containsEntry("aosAzimuthDeg", 239.0).containsEntry("losAzimuthDeg", 34.0)
                .containsEntry("imminent", false);
        assertThat(r.geometry().getCoordinates()[0].x).isEqualTo(-110.12);
    }

    @Test
    @DisplayName("imminent from 15 minutes before AOS until LOS")
    void imminent() {
        Instant aos = NOW.plus(Duration.ofMinutes(20));
        Instant los = aos.plus(Duration.ofMinutes(8));
        assertThat(SatellitePassesGenerator.imminent(aos, los, NOW)).isFalse();
        assertThat(SatellitePassesGenerator.imminent(aos, los, NOW.plus(Duration.ofMinutes(5)))).isTrue();
        assertThat(SatellitePassesGenerator.imminent(aos, los, los)).isFalse();
    }

    @Test
    @DisplayName("the observer is the place point with its altitudeM attribute, else 0 m")
    void observer() {
        Observer o = SatellitePassesGenerator.observer(place(Map.of("altitudeM", 150)));
        assertThat(o.lat()).isEqualTo(30.2672);
        assertThat(o.lon()).isEqualTo(-97.7431);
        assertThat(o.altM()).isEqualTo(150.0);
        assertThat(SatellitePassesGenerator.observer(place(Map.of())).altM()).isZero();
    }

    @Test
    @DisplayName("parameter checks and a valid DERIVED descriptor without TTL, 14-day pass retention")
    void descriptor() {
        SatellitePassesGenerator g = new SatellitePassesGenerator();
        assertThat(g.checkParams(ValidatedParams.of(Map.of("norad", List.of("abc"))))).containsKey("norad");
        assertThat(g.checkParams(ValidatedParams.of(Map.of("minElevationDeg", 95.0)))).containsKey("minElevationDeg");
        assertThat(g.checkParams(ValidatedParams.of(Map.of("horizon", Duration.ofDays(11))))).containsKey("horizon");
        assertThat(g.checkParams(ValidatedParams.of(Map.of("maxPassesPerPlace", 0)))).containsKey("maxPassesPerPlace");
        assertThat(g.checkParams(ValidatedParams.of(Map.of()))).isEmpty();
        assertThat(g.descriptor().recordTtl()).isNull();
        assertThat(g.descriptor().kindRetention()).containsEntry("satellite-pass", Duration.ofDays(14));
        assertThat(DescriptorValidator.problems(g.descriptor(), g)).isEmpty();
        assertThat(g.inputs(ValidatedParams.of(Map.of())).matchesChange("celestrak-gp", "satellite")).isTrue();
    }
}
