package ai.foresignal.store.tiles;

import static ai.foresignal.testsupport.timetravel.TimeTravelTestSources.MOVER;
import static ai.foresignal.testsupport.timetravel.TimeTravelTestSources.MOVER_PRIVATE;
import static ai.foresignal.testsupport.timetravel.TimeTravelTestSources.VEHICLE;
import static ai.foresignal.testsupport.timetravel.TimeTravelTestSources.ZONE;
import static ai.foresignal.testsupport.timetravel.TimeTravelTestSources.ZONE_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.security.SourceVisibilityGate;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.history.AsOfPlan;
import ai.foresignal.store.history.AsOfResolver;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.tiles.MvtDecoder;
import ai.foresignal.testsupport.tiles.MvtDecoder.Feature;
import ai.foresignal.testsupport.tiles.TileMath;
import ai.foresignal.testsupport.timetravel.TimeTravelTestSources;
import ai.foresignal.testsupport.timetravel.TimeTravelTestSources.StubHistoryFidelity;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * asOf tiles (prompt 037) against a real PostGIS, seeded only through {@link TestRecords}: a
 * synthetic TRACK mover crossing from tile A into tile B, a long-valid STANDARD polygon, tombstones,
 * TTL expiry, private sources, and the HTTP contract (headers, the immutable rule, byte-identical
 * repeats, the canonical redirect).
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(TimeTravelTestSources.class)
class AsOfTileQueryIT {

    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");
    static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);

    /** Tile A (Toronto) and tile B, its eastern neighbor at z10. */
    static final double LON_A = -79.38;
    static final double LAT = 43.65;
    static final TileCoordinates A = TileMath.tileFor(LON_A, LAT, 10);
    static final double LON_B = LON_A + 0.36;
    static final TileCoordinates B = TileMath.tileFor(LON_B, LAT, 10);

    @Autowired AsOfTileQueryService asOfTiles;
    @Autowired AsOfResolver resolver;
    @Autowired TileLayerResolver layers;
    @Autowired SourceRegistry registry;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired StubHistoryFidelity fidelity;
    @Autowired MockMvc mvc;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        fidelity.reset();
    }

    @AfterEach
    void tearDown() {
        fidelity.reset();
    }

    private void ingest(Instant at, String source, NormalizedRecord... batch) {
        if (clock.instant().isBefore(at)) {
            clock.setInstant(at);
        }
        IngestOutcome outcome = records.ingestAt(source, at, batch);
        assertThat(outcome.rejections()).isEmpty();
    }

    private static Instant t(long seconds) {
        return T.plusSeconds(seconds);
    }

    private static NormalizedRecord mover(String id, double lon, Instant at) {
        return NormalizedRecord.builder(id, VEHICLE).observedAt(at).point(lon, LAT).put("callsign", "CS-" + id)
                .put("alt", 1200).put("gs", 210.5).build();
    }

    private Set<String> anonymousExcluded() {
        return SourceVisibilityGate.excludedSources(registry, null, TokenRestrictions.NONE);
    }

    private List<Feature> features(String layer, TileCoordinates tile, Instant at, Set<String> excluded) {
        TileLayer resolved = layers.resolve(layer, excluded);
        AsOfPlan plan = resolver.plan(at, resolved.sourceIds(), excluded);
        return MvtDecoder.features(asOfTiles.raw(resolved, tile, 50_000, plan, ReadRestriction.NONE).mvt());
    }

    private List<String> ids(String layer, TileCoordinates tile, Instant at) {
        return features(layer, tile, at, Set.of()).stream().map(f -> (String) f.get("externalId")).sorted().toList();
    }

    private void seedMover() {
        ingest(t(0), MOVER, mover("m1", LON_A, t(0)));
        ingest(t(60), MOVER, mover("m1", LON_B, t(60)));
        clock.setInstant(t(1000));
    }

    @Test
    @DisplayName("the mover is in tile A at T1 and only in tile B at T2; it is never drawn at its old position")
    void moverAcrossTiles() {
        assertThat(B.x()).isEqualTo(A.x() + 1);
        assertThat(B.y()).isEqualTo(A.y());
        seedMover();

        assertThat(ids(VEHICLE, A, t(30))).containsExactly("m1");
        assertThat(ids(VEHICLE, B, t(30))).isEmpty();
        assertThat(ids(VEHICLE, A, t(90))).as("left tile A: not drawn at its last in-tile position").isEmpty();
        assertThat(ids(VEHICLE, B, t(90))).containsExactly("m1");
        assertThat(ids(VEHICLE, A, t(0).minusMillis(1))).as("before the first ingest").isEmpty();

        Feature f = features(MOVER, B, t(90), Set.of()).getFirst();
        assertThat(f.get("source")).isEqualTo(MOVER);
        assertThat(f.get("kind")).isEqualTo(VEHICLE);
        assertThat(f.get("title")).isEqualTo("CS-m1");
        assertThat(((Number) f.get("version")).longValue()).isEqualTo(2);
        assertThat(((Number) f.get("observedAt")).longValue()).isEqualTo(t(60).getEpochSecond());
        assertThat(((Number) f.get("alt")).intValue()).isEqualTo(1200);
        assertThat(f.properties()).doesNotContainKey("live");
    }

    @Test
    @DisplayName("gone after a tombstone and after TTL expiry")
    void tombstoneAndExpiry() {
        ingest(t(0), MOVER, mover("m1", LON_A, t(0)), mover("m2", LON_A + 0.01, t(0)));
        ingest(t(120), MOVER, NormalizedRecord.tombstone("m1", VEHICLE, t(120)));
        clock.setInstant(t(2000));

        assertThat(ids(VEHICLE, A, t(60))).containsExactly("m1", "m2");
        assertThat(ids(VEHICLE, A, t(150))).as("m1 tombstoned").containsExactly("m2");
        // TTL 5 min on ingested_at: m2 was ingested at t(0).
        assertThat(ids(VEHICLE, A, t(299))).containsExactly("m2");
        assertThat(ids(VEHICLE, A, t(301))).isEmpty();
    }

    @Test
    @DisplayName("the STANDARD long-valid polygon is present long after (L = 0)")
    void standardPolygon() {
        ingest(t(0), ZONE, NormalizedRecord.builder("z1", ZONE_KIND).observedAt(t(0))
                .geometry(GEOMETRY.createPolygon(new Coordinate[] {
                        new Coordinate(LON_A - 0.01, LAT - 0.01), new Coordinate(LON_A + 0.01, LAT - 0.01),
                        new Coordinate(LON_A + 0.01, LAT + 0.01), new Coordinate(LON_A - 0.01, LAT + 0.01),
                        new Coordinate(LON_A - 0.01, LAT - 0.01)}))
                .put("level", 3).put("name", "Zone one").build());
        clock.setInstant(t(86_400));
        List<Feature> features = features(ZONE, A, t(80_000), Set.of());
        assertThat(features).hasSize(1);
        assertThat(features.getFirst().type()).isEqualTo(MvtDecoder.GeomType.POLYGON);
        assertThat(features.getFirst().get("title")).isEqualTo("Zone one");
        assertThat(((Number) features.getFirst().get("level")).intValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("a private source is excluded for callers that fail the private gate")
    void privateSourceExcluded() {
        ingest(t(0), MOVER, mover("m1", LON_A, t(0)));
        ingest(t(0), MOVER_PRIVATE, mover("secret", LON_A + 0.01, t(0)));
        clock.setInstant(t(600));

        List<String> anonymous = features(VEHICLE, A, t(30), anonymousExcluded()).stream()
                .map(f -> (String) f.get("externalId")).toList();
        assertThat(anonymous).containsExactly("m1");
        assertThat(ids(VEHICLE, A, t(30))).as("an agent passes the gate").containsExactly("m1", "secret");
    }

    @Test
    @DisplayName("aggregated asOf tiles count the entities known then")
    void aggregated() {
        seedMover();
        TileLayer layer = layers.resolve(VEHICLE, Set.of());
        TileCoordinates low = TileMath.tileFor(LON_A, LAT, 4);
        AsOfPlan plan = resolver.plan(t(90), layer.sourceIds(), Set.of());
        List<Feature> cells = MvtDecoder.features(
                asOfTiles.aggregated(layer, low, TileShape.GRID, plan, ReadRestriction.NONE).mvt());
        assertThat(cells).hasSize(1);
        assertThat(((Number) cells.getFirst().get("count")).intValue()).isEqualTo(1);
    }

    // ------------------------------------------------------------------ HTTP

    private String url(String layer, TileCoordinates tile) {
        return "/v1/tiles/" + layer + "/" + tile.z() + "/" + tile.x() + "/" + tile.y() + ".mvt";
    }

    @Test
    @DisplayName("a closed past instant is byte-identical on repeat and immutable, with the asOf headers")
    void immutableAndRepeatable() throws Exception {
        seedMover();
        MvcResult first = mvc.perform(get(url(VEHICLE, B)).param("asOf", t(90).toString()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000, immutable"))
                .andExpect(header().string("Foresignal-As-Of", t(90).toString()))
                .andExpect(header().exists("Foresignal-As-Of-Seq"))
                .andExpect(header().string("Foresignal-Approximate", "false"))
                .andReturn();
        MvcResult second = mvc.perform(get(url(VEHICLE, B)).param("asOf", t(90).toString()))
                .andExpect(status().isOk()).andReturn();
        assertThat(second.getResponse().getContentAsByteArray()).isEqualTo(first.getResponse().getContentAsByteArray());
        assertThat(MvtDecoder.features(first.getResponse().getContentAsByteArray())).hasSize(1);
        String etag = first.getResponse().getHeader(HttpHeaders.ETAG);
        assertThat(etag).startsWith("\"");
        mvc.perform(get(url(VEHICLE, B)).param("asOf", t(90).toString()).header(HttpHeaders.IF_NONE_MATCH, etag))
                .andExpect(status().isNotModified());
    }

    @Test
    @DisplayName("within the last 2 minutes, or pending downsampling, the tile is not immutable")
    void notImmutable() throws Exception {
        seedMover();
        mvc.perform(get(url(VEHICLE, B)).param("asOf", t(1000 - 60).toString()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "public, max-age=300"));

        fidelity.setFinalBefore(t(10));
        mvc.perform(get(url(VEHICLE, B)).param("asOf", t(90).toString()))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "public, max-age=300"));

        fidelity.setDownsampled(true);
        mvc.perform(get(url(VEHICLE, B)).param("asOf", t(90).toString()))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000, immutable"))
                .andExpect(header().string("Foresignal-Approximate", "true"));
    }

    @Test
    @DisplayName("other spellings of asOf are 301 to the canonical whole-second form; a future asOf is 400")
    void canonicalRedirect() throws Exception {
        seedMover();
        mvc.perform(get(url(VEHICLE, B) + "?asOf=2026-03-01T00:01:30.500Z&agg=none"))
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string(HttpHeaders.LOCATION,
                        url(VEHICLE, B) + "?asOf=2026-03-01T00:01:30Z&agg=none"));
        mvc.perform(get(url(VEHICLE, B)).param("asOf", "2026-03-01T01:01:30+01:00"))
                .andExpect(status().isMovedPermanently());
        mvc.perform(get(url(VEHICLE, B)).param("asOf", t(5000).toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ASOF_INVALID"));
    }

    @Test
    @DisplayName("TileJSON with asOf bakes the canonical instant into a concrete template")
    void tileJson() throws Exception {
        seedMover();
        mvc.perform(get("/v1/tiles/" + VEHICLE + ".json").param("asOf", t(90).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tiles[0]").value(org.hamcrest.Matchers.endsWith(
                        "/v1/tiles/" + VEHICLE + "/{z}/{x}/{y}.mvt?asOf=" + t(90))))
                .andExpect(jsonPath("$['foresignal:asOf']").value(t(90).toString()))
                .andExpect(jsonPath("$['foresignal:properties']").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.hasItem("live"))));
        mvc.perform(get("/v1/tiles.json").param("asOf", t(90).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.layers[0].tilejson").value(org.hamcrest.Matchers.endsWith("?asOf=" + t(90))));
        mvc.perform(get("/v1/tiles/" + VEHICLE + ".json").param("asOf", "2026-03-01T00:01:30.1Z"))
                .andExpect(status().isMovedPermanently());
    }
}
