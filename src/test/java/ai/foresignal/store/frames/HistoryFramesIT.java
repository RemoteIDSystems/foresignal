package ai.foresignal.store.frames;

import static ai.foresignal.testsupport.timetravel.TimeTravelTestSources.MOVER;
import static ai.foresignal.testsupport.timetravel.TimeTravelTestSources.VEHICLE;
import static ai.foresignal.testsupport.timetravel.TimeTravelTestSources.ZONE;
import static ai.foresignal.testsupport.timetravel.TimeTravelTestSources.ZONE_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.BboxParser;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.query.AsOfQueryService;
import ai.foresignal.store.query.GeometryEncoding;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.timetravel.TimeTravelTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * History frames (prompt 037) against a real PostGIS, seeded only through {@link TestRecords}. The
 * central assertion is the invariant: applying frames {@code 1..i} to the keyframe equals the asOf
 * state at {@code t_i}, checked against {@link AsOfQueryService} at every frame end; plus
 * tombstone, expiry and {@code left} removals, the 400 limits and ndjson.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(TimeTravelTestSources.class)
class HistoryFramesIT {

    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");
    static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);
    static final String BBOX = "9.9,49.9,10.5,50.5";
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired HistoryFramesService frames;
    @Autowired AsOfQueryService asOf;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired MockMvc mvc;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
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

    private static NormalizedRecord mover(String id, double lon, double lat, Instant at) {
        return NormalizedRecord.builder(id, VEHICLE).observedAt(at).point(lon, lat).put("callsign", "CS-" + id)
                .put("alt", 1000).build();
    }

    /** The scenario: an entity leaving and re-entering the bbox, a tombstone, two expiries, a zone. */
    private void seed() {
        ingest(t(0), ZONE, NormalizedRecord.builder("z1", ZONE_KIND).observedAt(t(0))
                .geometry(GEOMETRY.createPolygon(new Coordinate[] {
                        new Coordinate(10, 50), new Coordinate(10.1, 50), new Coordinate(10.1, 50.1),
                        new Coordinate(10, 50.1), new Coordinate(10, 50)}))
                .put("level", 2).put("name", "Zone").build());
        ingest(t(0), MOVER, mover("m4", 10.1, 50.1, t(0)), mover("m1", 10.0, 50.0, t(0)));
        ingest(t(120), MOVER, mover("m1", 10.2, 50.0, t(120)), mover("m2", 10.3, 50.3, t(120)));
        ingest(t(240), MOVER, mover("m1", 11.0, 50.0, t(240)), mover("m4", 10.1, 50.11, t(240)),
                NormalizedRecord.tombstone("m2", VEHICLE, t(240)));
        ingest(t(300), MOVER, mover("m3", 10.4, 50.4, t(300)));
        ingest(t(360), MOVER, mover("m4", 10.12, 50.1, t(360)));
        ingest(t(480), MOVER, mover("m1", 10.25, 50.0, t(480)));
        clock.setInstant(t(1000));
    }

    private FramesQuery query(Instant from, Instant to, Duration step, BboxFilter bbox) {
        return new FramesQuery(List.of(MOVER, ZONE), List.of(), bbox, from, to, step, 240, false, Set.of(),
                ReadRestriction.NONE);
    }

    /** {@code id → version} of the asOf state at {@code at}, through the records asOf path. */
    private Map<String, Integer> asOfState(Instant at, BboxFilter bbox) {
        AsOfQueryService.AsOfSlice slice = asOf.list(new AsOfQueryService.AsOfQuery(at, List.of(MOVER, ZONE), List.of(),
                List.of(), bbox, null, null, 5000, FieldSelection.ALL, GeometryOptions.DEFAULT, GeometryEncoding.GEOJSON,
                false, Set.of()));
        Map<String, Integer> state = new TreeMap<>();
        for (RecordRow row : slice.rows()) {
            state.put(row.sourceId() + ":" + row.externalId(), row.version());
        }
        return state;
    }

    @Test
    @DisplayName("keyframe plus frames 1..i equals asOf(t_i) at every frame end, with and without a bbox")
    void keyframePlusDeltasEqualsAsOf() {
        seed();
        for (BboxFilter bbox : new BboxFilter[] {BboxParser.parse(BBOX), null}) {
            HistoryFrames result = frames.frames(query(t(60), t(900), Duration.ofSeconds(60), bbox));
            Map<String, Integer> state = new TreeMap<>();
            for (FrameItem item : result.keyframe()) {
                state.put(item.id(), item.version());
            }
            assertThat(state).as("keyframe").isEqualTo(asOfState(t(60), bbox));
            assertThat(result.frames()).hasSize(14);
            for (HistoryFrames.Frame frame : result.frames()) {
                for (HistoryFrames.Removal removal : frame.removals()) {
                    assertThat(state).containsKey(removal.id());
                    state.remove(removal.id());
                }
                for (FrameItem item : frame.upserts()) {
                    state.put(item.id(), item.version());
                }
                assertThat(state).as("state at " + frame.t()).isEqualTo(asOfState(frame.t(), bbox));
            }
        }
    }

    @Test
    @DisplayName("tombstone, expiry and left removals land in the frame where they happen")
    void removals() {
        seed();
        HistoryFrames result = frames.frames(query(t(60), t(900), Duration.ofSeconds(60), BboxParser.parse(BBOX)));
        assertThat(result.asOf()).isEqualTo(t(60));
        assertThat(result.keyframe()).extracting(FrameItem::id)
                .containsExactly(MOVER + ":m1", MOVER + ":m4", ZONE + ":z1");
        Map<Instant, HistoryFrames.Frame> byTime = new TreeMap<>();
        result.frames().forEach(f -> byTime.put(f.t(), f));

        assertThat(byTime.get(t(120)).upserts()).extracting(FrameItem::id).containsExactly(MOVER + ":m1", MOVER + ":m2");
        HistoryFrames.Frame at240 = byTime.get(t(240));
        assertThat(at240.removals()).containsExactly(new HistoryFrames.Removal(MOVER + ":m1", "left"),
                new HistoryFrames.Removal(MOVER + ":m2", "tombstone"));
        assertThat(at240.removals().getFirst().left()).isTrue();
        assertThat(at240.upserts()).extracting(FrameItem::id).containsExactly(MOVER + ":m4");
        assertThat(byTime.get(t(600)).removals()).containsExactly(new HistoryFrames.Removal(MOVER + ":m3", "expired"));
        assertThat(byTime.get(t(480)).upserts()).extracting(FrameItem::id).containsExactly(MOVER + ":m1");
        assertThat(byTime.get(t(660)).removals()).extracting(HistoryFrames.Removal::id).containsExactly(MOVER + ":m4");

        FrameItem m1 = result.keyframe().getFirst();
        assertThat(m1.kind()).isEqualTo(VEHICLE);
        assertThat(m1.title()).isEqualTo("CS-m1");
        assertThat(m1.lon()).isEqualTo(10.0);
        assertThat(m1.lat()).isEqualTo(50.0);
        assertThat(m1.geometry()).isNull();
        assertThat(JSON.readTree(m1.props()).get("alt").asInt()).isEqualTo(1000);
        assertThat(result.keyframe().get(2).lon()).as("the zone's point on surface").isBetween(10.0, 10.1);
        assertThat(result.watermarkSeq()).isPositive();
        assertThat(result.horizon()).isNotNull();
        assertThat(result.approximate()).isFalse();
    }

    @Test
    @DisplayName("geometry=full returns GeoJSON geometry instead of a centroid")
    void fullGeometry() {
        seed();
        HistoryFrames result = frames.frames(new FramesQuery(List.of(ZONE), List.of(), null, t(60), t(120),
                Duration.ofSeconds(60), 240, true, Set.of(), ReadRestriction.NONE));
        FrameItem zone = result.keyframe().getFirst();
        assertThat(zone.lon()).isNull();
        assertThat(JSON.readTree(zone.geometry()).get("type").asString()).isEqualTo("Polygon");
    }

    @Test
    @DisplayName("400 limits: step, frame count, filter, FRAMES_TOO_LARGE")
    void limits() {
        seed();
        assertThatThrownBy(() -> frames.frames(query(t(60), t(900), Duration.ofSeconds(5), null)))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> frames.frames(new FramesQuery(List.of(MOVER), List.of(), null, t(0), t(900),
                Duration.ofSeconds(10), 10, false, Set.of(), ReadRestriction.NONE)))
                .as("90 frames > maxFrames 10").isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> frames.frames(new FramesQuery(List.of(), List.of(), null, t(60), t(900),
                Duration.ofSeconds(60), 240, false, Set.of(), ReadRestriction.NONE)))
                .satisfies(e -> assertThat(((ValidationException) e).getCode()).isEqualTo(ErrorCodes.ASOF_FILTER_REQUIRED));
        assertThatThrownBy(() -> frames.frames(query(t(60), t(900), Duration.ofSeconds(60), null), 2, 1_000))
                .satisfies(e -> assertThat(((ValidationException) e).getCode()).isEqualTo(ErrorCodes.FRAMES_TOO_LARGE));
        assertThatThrownBy(() -> frames.frames(query(t(60), t(900), Duration.ofSeconds(60), null), 1_000, 3))
                .satisfies(e -> assertThat(((ValidationException) e).getCode()).isEqualTo(ErrorCodes.FRAMES_TOO_LARGE));
        assertThatThrownBy(() -> frames.frames(query(t(60), t(2000), Duration.ofSeconds(60), null)))
                .as("to in the future").isInstanceOf(ValidationException.class);
        assertThat(HistoryFramesService.frameTimes(t(0), t(150), Duration.ofSeconds(60)))
                .containsExactly(t(60), t(120), t(150));
    }

    @Test
    @DisplayName("over HTTP: JSON envelope, and ndjson with the keyframe line first")
    void http() throws Exception {
        seed();
        mvc.perform(get("/v1/history/frames").param("source", MOVER).param("bbox", BBOX)
                        .param("from", t(60).toString()).param("to", t(300).toString()).param("step", "1m"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.asOf").value(t(60).toString()))
                .andExpect(jsonPath("$.step").value("1m"))
                .andExpect(jsonPath("$.keyframe.length()").value(2))
                .andExpect(jsonPath("$.frames.length()").value(4))
                .andExpect(jsonPath("$.frames[2].removals[0].reason").value("left"))
                .andExpect(jsonPath("$.frames[2].removals[0].left").value(true));
        MvcResult ndjson = mvc.perform(get("/v1/history/frames").param("source", MOVER).param("bbox", BBOX)
                        .param("from", t(60).toString()).param("to", t(300).toString()).param("step", "60s")
                        .param("format", "ndjson"))
                .andExpect(status().isOk()).andReturn();
        String[] lines = ndjson.getResponse().getContentAsString().strip().split("\n");
        assertThat(lines).hasSize(5);
        JsonNode head = JSON.readTree(lines[0]);
        assertThat(head.has("keyframe")).isTrue();
        assertThat(head.has("frames")).isFalse();
        assertThat(JSON.readTree(lines[1]).get("t").asString()).isEqualTo(t(120).toString());
        mvc.perform(get("/v1/history/frames").param("from", t(60).toString()).param("to", t(300).toString())
                        .param("step", "60s"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ASOF_FILTER_REQUIRED"));
        mvc.perform(get("/v1/history/frames").param("source", "test-private").param("from", t(60).toString())
                        .param("to", t(300).toString()).param("step", "60s"))
                .andExpect(status().isNotFound());
    }
}
