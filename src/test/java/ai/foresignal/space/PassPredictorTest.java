package ai.foresignal.space;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.source.adapters.celestrak.CelestrakGpParser;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;

/**
 * {@link PassPredictor} against an independent reference table (Skyfield 1.55 / sgp4 2.27, see
 * {@code fixtures/space/README.md}): AOS and LOS within 5 s, maximum elevation within 0.2° (prompt 064).
 */
class PassPredictorTest {

    private static final Instant FROM = Instant.parse("2026-10-13T06:00:00Z");
    private static final Duration HORIZON = Duration.ofHours(48);

    /** One reference pass. */
    record Reference(String site, double lat, double lon, double alt, Instant aos, Instant tca, Instant los,
            double maxEl, double aosAz, double losAz) {}

    static List<Reference> references() {
        List<Reference> out = new ArrayList<>();
        String[] lines = AdapterFixtures.text("space", "iss-passes-skyfield.csv").split("\n");
        for (int i = 1; i < lines.length; i++) {
            String[] f = lines[i].strip().split(",");
            out.add(new Reference(f[0], Double.parseDouble(f[1]), Double.parseDouble(f[2]), Double.parseDouble(f[3]),
                    Instant.parse(f[4]), Instant.parse(f[5]), Instant.parse(f[6]), Double.parseDouble(f[7]),
                    Double.parseDouble(f[8]), Double.parseDouble(f[9])));
        }
        return out;
    }

    static OmmElements byNorad(long norad) {
        return CelestrakGpParser.parseCsv(AdapterFixtures.bytes("celestrak-gp", "stations.csv"), "stations")
                .elements().stream().filter(e -> e.norad() == norad).findFirst().orElseThrow();
    }

    private static double seconds(Instant a, Instant b) {
        return Math.abs(Duration.between(a, b).toMillis()) / 1000.0;
    }

    @Test
    @DisplayName("ISS passes over two sites match the reference table (AOS/LOS ±5 s, max elevation ±0.2°)")
    void referenceTable() {
        OmmElements iss = byNorad(25544);
        for (String site : List.of("austin", "sydney")) {
            List<Reference> expected = references().stream().filter(r -> r.site().equals(site)).toList();
            Reference first = expected.get(0);
            List<PredictedPass> passes = PassPredictor.predict(iss, new Observer(first.lat(), first.lon(), first.alt()),
                    FROM, HORIZON, 10);
            assertThat(passes).as(site).hasSameSizeAs(expected);
            for (int i = 0; i < expected.size(); i++) {
                Reference r = expected.get(i);
                PredictedPass p = passes.get(i);
                assertThat(seconds(p.aos(), r.aos())).as(site + " AOS " + r.aos()).isLessThanOrEqualTo(5);
                assertThat(seconds(p.los(), r.los())).as(site + " LOS " + r.los()).isLessThanOrEqualTo(5);
                assertThat(seconds(p.tca(), r.tca())).as(site + " TCA " + r.tca()).isLessThanOrEqualTo(5);
                assertThat(p.maxElevationDeg()).as(site + " max el").isCloseTo(r.maxEl(), within(0.2));
                assertThat(angleDiff(p.aosAzimuthDeg(), r.aosAz())).as(site + " AOS az").isLessThan(1.0);
                assertThat(angleDiff(p.losAzimuthDeg(), r.losAz())).as(site + " LOS az").isLessThan(1.0);
            }
        }
    }

    @Test
    @DisplayName("no pass is below the minimum elevation; a mask above the best pass finds none")
    void minimumElevation() {
        OmmElements iss = byNorad(25544);
        Observer austin = new Observer(30.2672, -97.7431, 150);
        List<PredictedPass> high = PassPredictor.predict(iss, austin, FROM, HORIZON, 40);
        assertThat(high).hasSize(4).allSatisfy(p -> assertThat(p.maxElevationDeg()).isGreaterThanOrEqualTo(40));
        assertThat(PassPredictor.predict(iss, austin, FROM, HORIZON, 79)).isEmpty();
    }

    @Test
    @DisplayName("a six-digit catalog number propagates through the TLE field constructor")
    void sixDigit() {
        OmmElements soyuz = byNorad(100057);
        assertThat(soyuz.tleCompatible()).isFalse();
        List<PredictedPass> passes = PassPredictor.predict(soyuz, new Observer(30.2672, -97.7431, 150), FROM,
                HORIZON, 10);
        // Docked at the ISS: the element set is the station's, so the passes are the station's.
        List<Reference> austin = references().stream().filter(r -> r.site().equals("austin")).toList();
        assertThat(passes).hasSameSizeAs(austin);
        assertThat(seconds(passes.get(0).aos(), austin.get(0).aos())).isLessThanOrEqualTo(5);
    }

    @Test
    @DisplayName("the ground track has at most 60 points from AOS to LOS and becomes a line geometry")
    void groundTrack() {
        PredictedPass p = PassPredictor.predict(byNorad(25544), new Observer(30.2672, -97.7431, 150), FROM, HORIZON, 10)
                .get(0);
        assertThat(p.groundTrack()).hasSizeBetween(2, PassPredictor.MAX_TRACK_POINTS);
        Geometry g = GroundTracks.geometry(p.groundTrack());
        assertThat(g.getGeometryType()).isIn("LineString", "MultiLineString");
        assertThat(g.getSRID()).isEqualTo(4326);
        double[] first = p.groundTrack().get(0);
        assertThat(first[1]).isBetween(-52.0, 52.0);
    }

    @Test
    @DisplayName("a track crossing ±180° is split into two lines meeting at the meridian")
    void antimeridian() {
        Geometry g = GroundTracks.geometry(List.of(new double[] {178, 10}, new double[] {179.5, 11},
                new double[] {-179.5, 12}, new double[] {-178, 13}));
        assertThat(g.getNumGeometries()).isEqualTo(2);
        assertThat(g.getGeometryN(0).getCoordinates()[g.getGeometryN(0).getNumPoints() - 1].x).isEqualTo(180);
        assertThat(g.getGeometryN(1).getCoordinates()[0].x).isEqualTo(-180);
        assertThat(GroundTracks.geometry(List.of(new double[] {1, 1}))).isNull();
    }

    @Test
    @DisplayName("the startup self-test passes on the bundled fixture and fails on a wrong expectation")
    void selfTest() {
        OrekitSelfTest selfTest = new OrekitSelfTest(tools.jackson.databind.json.JsonMapper.builder().build(), true);
        assertThat(selfTest.run().passed()).isTrue();
        assertThat(selfTest.result().detail()).contains("ISS pass AOS");
        Map<String, Object> wrong = new java.util.LinkedHashMap<>(Map.of(
                "elements", byNorad(25544).toPayload(),
                "observer", Map.of("lat", 30.2672, "lon", -97.7431, "altM", 150),
                "from", "2026-10-13T06:00:00Z", "horizonHours", 12, "minElevationDeg", 10,
                "expected", Map.of("aos", "2026-10-13T12:07:33Z", "los", "2026-10-13T12:13:10Z", "maxElevationDeg", 50.8)));
        assertThat(OrekitSelfTest.check(wrong).passed()).isFalse();
    }

    private static double angleDiff(double a, double b) {
        double d = Math.abs(a - b) % 360;
        return d > 180 ? 360 - d : d;
    }
}
