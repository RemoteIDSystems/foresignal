package ai.foresignal.store.tiles;

import static ai.foresignal.testsupport.tiles.TileTestSources.EXPIRING;
import static ai.foresignal.testsupport.tiles.TileTestSources.EXPIRING_KIND;
import static ai.foresignal.testsupport.tiles.TileTestSources.FIRE_A;
import static ai.foresignal.testsupport.tiles.TileTestSources.FIRE_B;
import static ai.foresignal.testsupport.tiles.TileTestSources.FIRE_KIND;
import static ai.foresignal.testsupport.tiles.TileTestSources.PLANE;
import static ai.foresignal.testsupport.tiles.TileTestSources.PLANE_KIND;
import static ai.foresignal.testsupport.tiles.TileTestSources.QUAKE;
import static ai.foresignal.testsupport.tiles.TileTestSources.QUAKE_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.RecordHeadId;
import ai.foresignal.store.RecordHeadRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.tiles.MvtDecoder;
import ai.foresignal.testsupport.tiles.MvtDecoder.Feature;
import ai.foresignal.testsupport.tiles.MvtDecoder.GeomType;
import ai.foresignal.testsupport.tiles.TileMath;
import ai.foresignal.testsupport.tiles.TileTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

/**
 * Raw vector tiles against a real PostGIS, seeded only through {@link TestRecords}: geometry and
 * time from {@code record_live}, typed properties, live positions before any new version, the
 * merged payload, expiry, tombstones, the cap and empty tiles.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(TileTestSources.class)
class TileQueryServiceIT {

    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");
    static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);

    /** Toronto, the point most tests look at. */
    static final double LON = -79.38;
    static final double LAT = 43.65;
    static final TileCoordinates TILE = TileMath.tileFor(LON, LAT, 10);

    @Autowired TileQueryService tiles;
    @Autowired TileLayerResolver resolver;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired RecordHeadRepository heads;

    @BeforeEach
    void setClock() {
        clock.setInstant(T);
    }

    private void ingest(Instant at, String source, NormalizedRecord... batch) {
        if (clock.instant().isBefore(at)) {
            clock.setInstant(at);
        }
        IngestOutcome outcome = records.ingestAt(source, at, batch);
        assertThat(outcome.rejections()).isEmpty();
    }

    private static NormalizedRecord.Builder rec(String id, String kind, Instant observedAt) {
        return NormalizedRecord.builder(id, kind).observedAt(observedAt);
    }

    private List<Feature> features(String layer, TileCoordinates tile) {
        return MvtDecoder.features(tiles.raw(resolver.resolve(layer, Set.of()), tile, 50_000).mvt());
    }

    private static Map<String, Feature> byId(List<Feature> features) {
        return features.stream().collect(Collectors.toMap(f -> (String) f.get("externalId"), f -> f));
    }

    @Test
    @DisplayName("points and polygons in a known tile, with base and tile properties in their native types")
    void pointsPolygonsAndTypes() {
        ingest(T, QUAKE,
                rec("q1", QUAKE_KIND, T).point(LON, LAT).put("mag", 4.5).put("place", "Toronto").put("depth", 10.0)
                        .build(),
                rec("zone", QUAKE_KIND, T).geometry(GEOMETRY.createPolygon(new Coordinate[] {
                        new Coordinate(LON - 0.01, LAT - 0.01), new Coordinate(LON + 0.01, LAT - 0.01),
                        new Coordinate(LON + 0.01, LAT + 0.01), new Coordinate(LON - 0.01, LAT + 0.01),
                        new Coordinate(LON - 0.01, LAT - 0.01)})).put("mag", 3.0).put("place", "Zone").build(),
                rec("far", QUAKE_KIND, T).point(10, 50).put("mag", 2.0).build());

        MvtDecoder.Layer layer = MvtDecoder.singleLayer(
                tiles.raw(resolver.resolve(QUAKE, Set.of()), TILE, 50_000).mvt());
        assertThat(layer.name()).isEqualTo(QUAKE);
        assertThat(layer.extent()).isEqualTo(4096);
        Map<String, Feature> features = byId(layer.features());
        assertThat(features).containsOnlyKeys("q1", "zone");

        Feature q1 = features.get("q1");
        assertThat(q1.type()).isEqualTo(GeomType.POINT);
        int[] expected = TileMath.pixel(LON, LAT, TILE);
        assertThat(q1.firstPoint()[0]).isCloseTo(expected[0], within(2));
        assertThat(q1.firstPoint()[1]).isCloseTo(expected[1], within(2));
        assertThat(q1.get("source")).isEqualTo(QUAKE);
        assertThat(q1.get("kind")).isEqualTo(QUAKE_KIND);
        assertThat(q1.get("title")).isInstanceOf(String.class).isEqualTo("Toronto");
        assertThat(q1.get("mag")).isInstanceOf(Number.class);
        assertThat(((Number) q1.get("mag")).doubleValue()).isEqualTo(4.5);
        assertThat(q1.get("place")).isEqualTo("Toronto");
        assertThat(q1.get("live")).isEqualTo(false);
        assertThat(((Number) q1.get("version")).longValue()).isEqualTo(1);
        assertThat(((Number) q1.get("observedAt")).longValue()).isEqualTo(T.getEpochSecond());
        assertThat(q1.properties()).doesNotContainKeys("depth", "bad-name");

        assertThat(features.get("zone").type()).isEqualTo(GeomType.POLYGON);
        assertThat(features.get("zone").get("title")).isEqualTo("Zone");
    }

    @Test
    @DisplayName("a live position appears before any new version: a LIVE observation moves an entity into the tile")
    void livePositionBeforeVersion() {
        ingest(T, PLANE, rec("p1", PLANE_KIND, T).point(10, 50).put("callsign", "ABC123").put("track", 90).build());
        assertThat(features(PLANE, TILE)).isEmpty();

        clock.setInstant(T.plusSeconds(10));
        IngestOutcome outcome = records.ingestAt(PLANE, T.plusSeconds(10),
                rec("p1", PLANE_KIND, T.plusSeconds(10)).point(LON, LAT).put("callsign", "ABC123").put("track", 180)
                        .build());
        assertThat(outcome.live()).isEqualTo(1);
        assertThat(outcome.persisted()).isZero();
        assertThat(heads.findById(new RecordHeadId(PLANE, "p1")).orElseThrow().getVersion()).isEqualTo(1);

        List<Feature> features = features(PLANE, TILE);
        assertThat(features).hasSize(1);
        Feature plane = features.getFirst();
        int[] expected = TileMath.pixel(LON, LAT, TILE);
        assertThat(plane.firstPoint()[0]).isCloseTo(expected[0], within(2));
        assertThat(plane.firstPoint()[1]).isCloseTo(expected[1], within(2));
        assertThat(plane.get("live")).isEqualTo(true);
        assertThat(((Number) plane.get("version")).longValue()).isEqualTo(1);
        assertThat(((Number) plane.get("observedAt")).longValue()).isEqualTo(T.plusSeconds(10).getEpochSecond());
        // The merged payload carries the kinematic key the LIVE update changed.
        assertThat(((Number) plane.get("track")).longValue()).isEqualTo(180);
    }

    @Test
    @DisplayName("expired records are left out; the expiry follows the application clock")
    void expiredExcluded() {
        ingest(T, EXPIRING, rec("e1", EXPIRING_KIND, T).point(LON, LAT).build());
        assertThat(features(EXPIRING, TILE)).hasSize(1);
        clock.setInstant(T.plus(Duration.ofMinutes(11)));
        assertThat(features(EXPIRING, TILE)).isEmpty();
    }

    @Test
    @DisplayName("tombstoned records are left out")
    void tombstonesExcluded() {
        ingest(T, QUAKE,
                rec("gone", QUAKE_KIND, T).point(LON, LAT).put("mag", 1.0).build(),
                rec("kept", QUAKE_KIND, T).point(LON, LAT).put("mag", 1.5).build());
        ingest(T.plusSeconds(5), QUAKE, rec("gone", QUAKE_KIND, T.plusSeconds(5)).tombstone(true).build());
        assertThat(byId(features(QUAKE, TILE))).containsOnlyKeys("kept");
    }

    @Test
    @DisplayName("the cap keeps the newest features and reports truncation")
    void cap() {
        for (int i = 0; i < 5; i++) {
            Instant at = T.plusSeconds(i);
            ingest(at, QUAKE, rec("c" + i, QUAKE_KIND, at).point(LON + i * 0.001, LAT).put("mag", 1.0 + i).build());
        }
        TileLayer layer = resolver.resolve(QUAKE, Set.of());
        TileResult capped = tiles.raw(layer, TILE, 3);
        assertThat(capped.truncated()).isTrue();
        assertThat(byId(MvtDecoder.features(capped.mvt()))).containsOnlyKeys("c2", "c3", "c4");
        TileResult exact = tiles.raw(layer, TILE, 5);
        assertThat(exact.truncated()).isFalse();
        assertThat(MvtDecoder.features(exact.mvt())).hasSize(5);
        TileResult viaRender = tiles.render(layer, TILE, AggMode.AUTO);
        assertThat(viaRender.truncated()).isFalse();
        assertThat(viaRender.shape()).isEqualTo(TileShape.RAW);
    }

    @Test
    @DisplayName("an empty tile has no features and an empty body")
    void emptyTile() {
        ingest(T, QUAKE, rec("q1", QUAKE_KIND, T).point(10, 50).put("mag", 1.0).build());
        TileResult result = tiles.raw(resolver.resolve(QUAKE, Set.of()), TILE, 50_000);
        assertThat(result.truncated()).isFalse();
        assertThat(result.mvt()).isEmpty();
    }

    @Test
    @DisplayName("a kind layer shows every visible source of the kind")
    void kindLayer() {
        ingest(T, FIRE_A, rec("a1", FIRE_KIND, T).point(LON, LAT).put("frp", 12.5).build());
        ingest(T, FIRE_B, rec("b1", FIRE_KIND, T).point(LON, LAT).put("frp", 3.0).put("confidence", "high").build());
        Map<String, Feature> features = byId(features(FIRE_KIND, TILE));
        assertThat(features).containsOnlyKeys("a1", "b1");
        assertThat(features.get("a1").get("source")).isEqualTo(FIRE_A);
        assertThat(features.get("b1").get("confidence")).isEqualTo("high");
    }
}
