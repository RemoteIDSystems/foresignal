package ai.foresignal.store.tiles;

import static ai.foresignal.testsupport.tiles.TileTestSources.FIRE_A;
import static ai.foresignal.testsupport.tiles.TileTestSources.FIRE_B;
import static ai.foresignal.testsupport.tiles.TileTestSources.FIRE_KIND;
import static ai.foresignal.testsupport.tiles.TileTestSources.FIRE_PRIVATE;
import static ai.foresignal.testsupport.tiles.TileTestSources.PLANE;
import static ai.foresignal.testsupport.tiles.TileTestSources.PLANE_KIND;
import static ai.foresignal.testsupport.tiles.TileTestSources.QUAKE;
import static ai.foresignal.testsupport.tiles.TileTestSources.QUAKE_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.tiles.MvtDecoder;
import ai.foresignal.testsupport.tiles.MvtDecoder.Feature;
import ai.foresignal.testsupport.tiles.MvtDecoder.GeomType;
import ai.foresignal.testsupport.tiles.TileMath;
import ai.foresignal.testsupport.tiles.TileTestSources;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import tools.jackson.databind.json.JsonMapper;

/**
 * Aggregated tiles: cell counts and kinds below {@code aggBelowZoom}, one tile per record on a
 * shared edge, {@code agg=none}, {@code agg=grid} at a high zoom, {@code maxRank}, and private
 * sources left out of counts for anonymous callers.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(TileTestSources.class)
class AggregatedTilesIT {

    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");
    static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);
    static final double LON = -79.38;
    static final double LAT = 43.65;

    @Autowired TileQueryService tiles;
    @Autowired TileLayerResolver resolver;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired MockMvc mvc;
    @Autowired JsonMapper json;

    @BeforeEach
    void setClock() {
        clock.setInstant(T);
    }

    private void ingest(String source, NormalizedRecord... batch) {
        records.ingestAt(source, T, batch);
    }

    private static NormalizedRecord.Builder rec(String id, String kind) {
        return NormalizedRecord.builder(id, kind).observedAt(T);
    }

    private List<Feature> render(String layer, Set<String> excluded, TileCoordinates tile, AggMode agg) {
        return MvtDecoder.features(tiles.render(resolver.resolve(layer, excluded), tile, agg).mvt());
    }

    private static long count(Feature cell) {
        return ((Number) cell.get("count")).longValue();
    }

    private static long total(List<Feature> cells) {
        return cells.stream().mapToLong(AggregatedTilesIT::count).sum();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> kinds(Feature cell) {
        return json.readValue((String) cell.get("kinds"), Map.class);
    }

    @Test
    @DisplayName("below aggBelowZoom, auto renders hex cells with count, kinds, latestObservedAt and agg")
    void hexBelowAggBelowZoom() {
        ingest(PLANE,
                rec("p1", PLANE_KIND).point(LON, LAT).put("callsign", "A").build(),
                rec("p2", PLANE_KIND).point(LON + 0.0001, LAT).put("callsign", "B").build(),
                rec("p3", PLANE_KIND).point(LON, LAT + 0.0001).put("callsign", "C").build());
        TileCoordinates tile = TileMath.tileFor(LON, LAT, 3);
        TileResult result = tiles.render(resolver.resolve(PLANE, Set.of()), tile, AggMode.AUTO);
        assertThat(result.shape()).isEqualTo(TileShape.HEX);
        assertThat(result.truncated()).isFalse();
        MvtDecoder.Layer layer = MvtDecoder.singleLayer(result.mvt());
        assertThat(layer.name()).isEqualTo(PLANE);
        assertThat(layer.features()).hasSize(1);
        Feature cell = layer.features().getFirst();
        assertThat(cell.type()).isEqualTo(GeomType.POLYGON);
        assertThat(count(cell)).isEqualTo(3);
        assertThat(kinds(cell)).containsExactly(Map.entry(PLANE_KIND, 3));
        assertThat(cell.get("agg")).isEqualTo("hex");
        assertThat(((Number) cell.get("latestObservedAt")).longValue()).isEqualTo(T.getEpochSecond());
        assertThat(cell.properties()).doesNotContainKeys("maxRank", "source", "externalId");
    }

    @Test
    @DisplayName("at and above aggBelowZoom, auto renders raw features; agg=none forces raw below it")
    void rawAboveAndNone() {
        ingest(PLANE, rec("p1", PLANE_KIND).point(LON, LAT).put("callsign", "A").build());
        TileLayer layer = resolver.resolve(PLANE, Set.of());
        assertThat(tiles.render(layer, TileMath.tileFor(LON, LAT, 6), AggMode.AUTO).shape()).isEqualTo(TileShape.RAW);
        List<Feature> raw = render(PLANE, Set.of(), TileMath.tileFor(LON, LAT, 3), AggMode.NONE);
        assertThat(raw).hasSize(1);
        assertThat(raw.getFirst().get("externalId")).isEqualTo("p1");
        assertThat(raw.getFirst().properties()).doesNotContainKey("count");
    }

    @Test
    @DisplayName("agg=grid at a high zoom on a layer that never aggregates; a polygon counts once")
    void gridAtHighZoom() {
        ingest(QUAKE,
                rec("q1", QUAKE_KIND).point(LON, LAT).put("mag", 4.0).build(),
                rec("q2", QUAKE_KIND).point(LON, LAT).put("mag", 5.5).build(),
                rec("big", QUAKE_KIND).geometry(GEOMETRY.createPolygon(new Coordinate[] {
                        new Coordinate(LON - 0.2, LAT - 0.2), new Coordinate(LON + 0.2, LAT - 0.2),
                        new Coordinate(LON + 0.2, LAT + 0.2), new Coordinate(LON - 0.2, LAT + 0.2),
                        new Coordinate(LON - 0.2, LAT - 0.2)})).put("mag", 2.0).build());
        TileCoordinates tile = TileMath.tileFor(LON, LAT, 9);
        TileResult result = tiles.render(resolver.resolve(QUAKE, Set.of()), tile, AggMode.GRID);
        assertThat(result.shape()).isEqualTo(TileShape.GRID);
        List<Feature> cells = MvtDecoder.features(result.mvt());
        assertThat(cells).allSatisfy(c -> assertThat(c.get("agg")).isEqualTo("grid"));
        assertThat(total(cells)).isEqualTo(3);
        // Every source of the layer declares the numeric rank field mag.
        Feature point = cells.stream().filter(c -> count(c) >= 2).findFirst().orElseThrow();
        assertThat(((Number) point.get("maxRank")).doubleValue()).isEqualTo(5.5);
    }

    @Test
    @DisplayName("a point on a shared tile edge is counted in exactly one of the two neighboring tiles")
    void sharedEdgeCountedOnce() {
        ingest(FIRE_A, rec("edge", FIRE_KIND).point(0, 10).put("frp", 1.0).build());
        TileCoordinates west = new TileCoordinates(1, 0, 0);
        TileCoordinates east = new TileCoordinates(1, 1, 0);
        for (AggMode agg : List.of(AggMode.GRID, AggMode.HEX)) {
            long westCount = total(render(FIRE_A, Set.of(), west, agg));
            long eastCount = total(render(FIRE_A, Set.of(), east, agg));
            assertThat(westCount + eastCount).as("agg=%s", agg).isEqualTo(1);
        }
        // The same on a horizontal edge: the equator between rows 0 and 1.
        ingest(FIRE_B, rec("equator", FIRE_KIND).point(45, 0).build());
        long north = total(render(FIRE_B, Set.of(), new TileCoordinates(1, 1, 0), AggMode.GRID));
        long south = total(render(FIRE_B, Set.of(), new TileCoordinates(1, 1, 1), AggMode.GRID));
        assertThat(north + south).isEqualTo(1);
    }

    @Test
    @DisplayName("private sources are left out of counts for anonymous callers, and counted with a token")
    void privateExcludedFromCounts() {
        ingest(FIRE_A,
                rec("a1", FIRE_KIND).point(LON, LAT).put("frp", 10.0).build(),
                rec("a2", FIRE_KIND).point(LON, LAT).put("frp", 20.0).build());
        ingest(FIRE_B, rec("b1", FIRE_KIND).point(LON, LAT).put("frp", 99.0).build());
        ingest(FIRE_PRIVATE,
                rec("s1", FIRE_KIND).point(LON, LAT).put("frp", 500.0).build(),
                rec("s2", FIRE_KIND).point(LON, LAT).put("frp", 600.0).build());
        TileCoordinates tile = TileMath.tileFor(LON, LAT, 3);

        List<Feature> anonymous = render(FIRE_KIND, Set.of(FIRE_PRIVATE), tile, AggMode.AUTO);
        assertThat(total(anonymous)).isEqualTo(3);
        assertThat(kinds(anonymous.getFirst())).containsExactly(Map.entry(FIRE_KIND, 3));
        // tile-fire-b declares no rank field, so the kind layer carries no maxRank.
        assertThat(anonymous.getFirst().properties()).doesNotContainKey("maxRank");

        assertThat(total(render(FIRE_KIND, Set.of(), tile, AggMode.HEX))).isEqualTo(5);

        List<Feature> sourceLayer = render(FIRE_A, Set.of(FIRE_PRIVATE), tile, AggMode.HEX);
        assertThat(((Number) sourceLayer.getFirst().get("maxRank")).doubleValue()).isEqualTo(20.0);
    }

    @Test
    @DisplayName("through the API: anonymous aggregated counts skip private sources, with the aggregate cache header")
    void privateExcludedThroughApi() throws Exception {
        ingest(FIRE_A, rec("a1", FIRE_KIND).point(LON, LAT).put("frp", 10.0).build());
        ingest(FIRE_PRIVATE, rec("s1", FIRE_KIND).point(LON, LAT).put("frp", 500.0).build());
        TileCoordinates tile = TileMath.tileFor(LON, LAT, 3);
        byte[] body = mvc.perform(get("/v1/tiles/" + FIRE_KIND + "/" + tile.z() + "/" + tile.x() + "/" + tile.y() + ".mvt"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "public, max-age=60"))
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(total(MvtDecoder.features(body))).isEqualTo(1);
    }
}
