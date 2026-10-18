package ai.foresignal.czml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.store.track.TrackResult;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link CzmlService} (prompt 141): grouping history rows into entities, skipping tombstones and rows without a
 * position, the entity cap, non-point geometries, tracks, the kindDisplay color lookup and the current title.
 */
class CzmlServiceTest {

    static final Instant T0 = Instant.parse("2026-03-01T00:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();

    SourceRegistry registry;
    RecordQueryService queries;
    CzmlService service;

    @BeforeEach
    void setUp() {
        registry = mock(SourceRegistry.class);
        queries = mock(RecordQueryService.class);
        SourceDescriptor own = descriptor("src-a", "Attribution A")
                .kindDisplay("vehicle", new KindDisplay(null, null, "#112233", null)).build();
        SourceDescriptor other = descriptor("src-b", "Attribution B")
                .kindDisplay("quake", new KindDisplay(null, null, "#AA0000", null)).build();
        when(registry.contains("src-a")).thenReturn(true);
        when(registry.contains("src-b")).thenReturn(true);
        when(registry.descriptor("src-a")).thenReturn(own);
        when(registry.descriptor("src-b")).thenReturn(other);
        when(registry.descriptors()).thenReturn(List.of(other, own));
        service = new CzmlService(registry, queries, JSON);
    }

    private static SourceDescriptor.Builder descriptor(String id, String attribution) {
        return SourceDescriptor.builder(id).name(id).attribution(attribution).kind("vehicle").mode(Mode.PUSH)
                .defaultBudget(Budget.of(60, 10_000));
    }

    static RecordRow row(String source, String id, String kind, Instant at, String geometry, boolean tombstone) {
        return new RecordRow(source, id, kind, 1, 1L, at, at, null, null, null, tombstone, null, geometry,
                "{\"name\":\"N-" + id + "\"}", null, null, null, 0, null, null, at, at, null, null, null, null, null,
                null);
    }

    static String point(double lon, double lat) {
        return "{\"type\":\"Point\",\"coordinates\":[" + lon + "," + lat + "]}";
    }

    @Test
    @DisplayName("rows group by id in first-appearance order with time-sorted samples; tombstones and no-geometry rows skip")
    void groupsRows() {
        List<RecordRow> rows = List.of(
                row("src-a", "m1", "vehicle", T0.plusSeconds(60), point(10.1, 50.1), false),
                row("src-b", "q1", "quake", T0.plusSeconds(10), point(20, 40), false),
                row("src-a", "m1", "vehicle", T0, point(10, 50), false),
                row("src-a", "m1", "vehicle", T0.plusSeconds(90), null, true),
                row("src-a", "m2", "vehicle", T0, null, false));
        CzmlService.Entities entities = service.fromRows(rows, r -> "Title " + r.externalId());
        assertThat(entities.truncated()).isFalse();
        assertThat(entities.entities()).extracting(CzmlWriter.Entity::id).containsExactly("src-a:m1", "src-b:q1");
        CzmlWriter.Entity m1 = entities.entities().getFirst();
        assertThat(m1.title()).isEqualTo("Title m1");
        assertThat(m1.color()).isEqualTo("#112233");
        assertThat(m1.samples()).extracting(CzmlWriter.Sample::time).containsExactly(T0, T0.plusSeconds(60));
        assertThat(entities.entities().get(1).color()).isEqualTo("#AA0000");
    }

    @Test
    @DisplayName("more than 5,000 entities: the first 5,000 are kept and truncated is true")
    void capsEntities() {
        List<RecordRow> rows = new ArrayList<>();
        for (int i = 0; i <= CzmlWriter.MAX_ENTITIES; i++) {
            rows.add(row("src-a", "e" + i, "vehicle", T0, point(1, 1), false));
        }
        CzmlService.Entities entities = service.fromRows(rows, r -> null);
        assertThat(entities.entities()).hasSize(CzmlWriter.MAX_ENTITIES);
        assertThat(entities.truncated()).isTrue();
    }

    @Test
    @DisplayName("positions: a point with height, a polygon's bbox center, a geometry collection, unreadable text")
    void positions() {
        assertThat(service.position("{\"type\":\"Point\",\"coordinates\":[1.5,2.5,300]}"))
                .containsExactly(1.5, 2.5, 300.0);
        assertThat(service.position("{\"type\":\"Polygon\",\"coordinates\":[[[0,0],[10,0],[10,20],[0,20],[0,0]]]}"))
                .containsExactly(5.0, 10.0, 0.0);
        assertThat(service.position("{\"type\":\"GeometryCollection\",\"geometries\":[{\"type\":\"Point\","
                + "\"coordinates\":[-10,-10]},{\"type\":\"LineString\",\"coordinates\":[[10,10],[20,30]]}]}"))
                .containsExactly(5.0, 10.0, 0.0);
        assertThat(service.position("not json")).isNull();
        assertThat(service.position("{\"type\":\"Point\"}")).isNull();
    }

    @Test
    @DisplayName("a track becomes one entity; repeated instants of one-point segments collapse")
    void track() {
        TrackResult track = new TrackResult("src-a", "m1", "vehicle", T0, T0.plusSeconds(600),
                List.of(List.of(new double[] {10, 50}, new double[] {10.1, 50.1}),
                        List.of(new double[] {11, 51}, new double[] {11, 51})),
                List.of(List.of(T0.toEpochMilli(), T0.plusSeconds(60).toEpochMilli()),
                        List.of(T0.plusSeconds(300).toEpochMilli(), T0.plusSeconds(300).toEpochMilli())),
                Map.of(), false, 4, 4, 5.0, 2, false, false, false, 9L, true, true);
        CzmlWriter.Entity entity = service.fromTrack(track, "CS-1");
        assertThat(entity.id()).isEqualTo("src-a:m1");
        assertThat(entity.title()).isEqualTo("CS-1");
        assertThat(entity.samples()).hasSize(3);
        List<Map<String, Object>> packets = service.trackDocument(track, null, null);
        assertThat(packets).hasSize(2);
        assertThat(packets.getFirst().get("description")).isEqualTo("Data: Attribution A");
        assertThat(((Map<?, ?>) packets.getFirst().get("clock")).get("interval"))
                .isEqualTo("2026-03-01T00:00:00Z/2026-03-01T00:10:00Z");
        assertThat(new String(service.bytes(packets), java.nio.charset.StandardCharsets.UTF_8))
                .startsWith("[{\"id\":\"document\"");
    }

    @Test
    @DisplayName("colors: own source first, then any source by id, else null; attributions joined")
    void colorsAndAttribution() {
        assertThat(service.colorOf("src-a", "vehicle")).isEqualTo("#112233");
        assertThat(service.colorOf("src-a", "quake")).isEqualTo("#AA0000");
        assertThat(service.colorOf("unknown", "quake")).isEqualTo("#AA0000");
        assertThat(service.colorOf("src-a", "nothing")).isNull();
        assertThat(service.colorOf("src-a", null)).isNull();
        assertThat(service.attributionOf(new java.util.TreeSet<>(Set.of("src-a", "src-b", "unknown"))))
                .isEqualTo("Attribution A · Attribution B");
        assertThat(service.attributionOf(Set.of("unknown"))).isNull();
    }

    @Test
    @DisplayName("currentTitle reads the visible current record, null when there is none")
    void currentTitle() {
        RecordRow current = new RecordRow("src-a", "m1", "vehicle", 3, 7L, T0, T0, null, null, null, false, "CS-1",
                point(1, 1), "{}", null, null, null, 0, null, null, T0, T0, null, null, null, null, null, null);
        when(queries.currentByKeys(anyList(), anySet(), any(ReadRestriction.class)))
                .thenReturn(Map.of("src-a:m1", current));
        assertThat(service.currentTitle("src-a", "m1", Set.of(), null)).isEqualTo("CS-1");
        assertThat(service.currentTitle("src-a", "m2", Set.of(), ReadRestriction.NONE)).isNull();
    }
}
