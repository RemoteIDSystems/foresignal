package ai.foresignal.security.restrict;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.store.query.SqlParams;
import ai.foresignal.stream.Bbox;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/** {@link RestrictionClamp}: intersect, never union (prompt 035). */
class RestrictionClampTest {

    private static final GeometryFactory WGS84 = new GeometryFactory(new PrecisionModel(), 4326);

    private static final ReadRestriction QUAKES_IN_BOX = new ReadRestriction(List.of("usgs"), List.of("earthquake"),
            List.of(0.0, 0.0, 10.0, 10.0), null, null, true, null);

    @Test
    @DisplayName("an absent request filter becomes the restriction")
    void emptyRequestBecomesRestriction() {
        assertThat(RestrictionClamp.clampSources(List.of(), QUAKES_IN_BOX)).containsExactly("usgs");
        assertThat(RestrictionClamp.clampKinds(null, QUAKES_IN_BOX)).containsExactly("earthquake");
        assertThat(RestrictionClamp.clampSources(List.of(), ReadRestriction.NONE)).isNull();
    }

    @Test
    @DisplayName("an overlapping request filter is intersected with the restriction")
    void intersects() {
        assertThat(RestrictionClamp.clampSources(List.of("usgs", "noaa"), QUAKES_IN_BOX)).containsExactly("usgs");
        assertThat(RestrictionClamp.clampSources(List.of("noaa"), ReadRestriction.NONE)).containsExactly("noaa");
    }

    @Test
    @DisplayName("a disjoint request filter yields nothing")
    void disjointIsEmpty() {
        assertThat(RestrictionClamp.clampSources(List.of("noaa"), QUAKES_IN_BOX)).isEmpty();
        assertThat(RestrictionClamp.disjoint(List.of("noaa"), List.of(), QUAKES_IN_BOX)).isTrue();
        assertThat(RestrictionClamp.disjoint(List.of(), List.of("flight"), QUAKES_IN_BOX)).isTrue();
        assertThat(RestrictionClamp.disjoint(List.of("usgs"), List.of("earthquake"), QUAKES_IN_BOX)).isFalse();
        assertThat(RestrictionClamp.disjoint(List.of("x"), List.of("y"), ReadRestriction.NONE)).isFalse();
    }

    @Test
    @DisplayName("in-memory test: source, kind and geometry must all pass; no geometry fails a spatial restriction")
    void testsInMemory() {
        var inside = WGS84.createPoint(new Coordinate(5, 5));
        var outside = WGS84.createPoint(new Coordinate(50, 5));
        assertThat(RestrictionClamp.test(QUAKES_IN_BOX, "usgs", "earthquake", inside)).isTrue();
        assertThat(RestrictionClamp.test(QUAKES_IN_BOX, "usgs", "earthquake", outside)).isFalse();
        assertThat(RestrictionClamp.test(QUAKES_IN_BOX, "noaa", "earthquake", inside)).isFalse();
        assertThat(RestrictionClamp.test(QUAKES_IN_BOX, "usgs", "flight", inside)).isFalse();
        assertThat(RestrictionClamp.test(QUAKES_IN_BOX, "usgs", "earthquake", (org.locationtech.jts.geom.Geometry) null))
                .isFalse();
        assertThat(RestrictionClamp.test(ReadRestriction.NONE, "any", null, (org.locationtech.jts.geom.Geometry) null))
                .isTrue();
        assertThat(RestrictionClamp.test(null, "any", null, (org.locationtech.jts.geom.Geometry) null)).isTrue();
    }

    @Test
    @DisplayName("box test: a point box is exact, a larger box passes when it touches the region")
    void testsBoxes() {
        assertThat(RestrictionClamp.test(QUAKES_IN_BOX, "usgs", "earthquake", new Bbox(5, 5, 5, 5))).isTrue();
        assertThat(RestrictionClamp.test(QUAKES_IN_BOX, "usgs", "earthquake", new Bbox(11, 11, 11, 11))).isFalse();
        assertThat(RestrictionClamp.test(QUAKES_IN_BOX, "usgs", "earthquake", new Bbox(9, 9, 20, 20))).isTrue();
        assertThat(RestrictionClamp.test(QUAKES_IN_BOX, "usgs", "earthquake", new Bbox(9, 20, 9, 30))).isFalse();
        assertThat(RestrictionClamp.test(QUAKES_IN_BOX, "usgs", "earthquake", (Bbox) null)).isFalse();
    }

    @Test
    @DisplayName("the SQL predicate ANDs source, kind and the region with an && prefilter, all bound")
    void sqlPredicate() {
        SqlParams p = new SqlParams();
        String sql = RestrictionClamp.sqlPredicate("v", QUAKES_IN_BOX, p).orElseThrow();
        assertThat(sql).contains("v.source_id = ANY(").contains("v.kind = ANY(").contains("v.geom && ST_MakeEnvelope(")
                .contains("ST_Intersects(v.geom, ST_GeomFromWKB(").doesNotContain("usgs").doesNotContain("earthquake");
        assertThat(p.source().getParameterNames()).isNotEmpty();
        assertThat(RestrictionClamp.sqlPredicate("v", ReadRestriction.NONE, new SqlParams())).isEmpty();
    }

    @Test
    @DisplayName("a spatial restriction without a geometry column fails closed")
    void noGeometryColumn() {
        String sql = RestrictionClamp.sqlPredicate("a.source_id", "a.kind", null, QUAKES_IN_BOX, new SqlParams())
                .orElseThrow();
        assertThat(sql).contains("FALSE");
    }

    @Test
    @DisplayName("named parameters for builders without SqlParams")
    void namedParameters() {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = RestrictionClamp.sqlPredicate("a.source_id", "a.kind", RestrictionClamp.ALERT_GEOMETRY_SQL,
                QUAKES_IN_BOX, params, "r").orElseThrow();
        assertThat(sql).contains(":r0").contains("record_snapshot");
        assertThat(params.hasValue("r0")).isTrue();
    }

    @Test
    @DisplayName("alert snapshots and GeoJSON text carry the geometry for in-memory tests")
    void snapshots() {
        String inside = "{\"geometry\":{\"type\":\"Point\",\"coordinates\":[5,5]}}";
        String outside = "{\"geometry\":{\"type\":\"Point\",\"coordinates\":[50,5]}}";
        assertThat(RestrictionClamp.testSnapshot(QUAKES_IN_BOX, "usgs", "earthquake", inside)).isTrue();
        assertThat(RestrictionClamp.testSnapshot(QUAKES_IN_BOX, "usgs", "earthquake", outside)).isFalse();
        assertThat(RestrictionClamp.testSnapshot(QUAKES_IN_BOX, "usgs", "earthquake", "{}")).isFalse();
        assertThat(RestrictionClamp.testSnapshot(QUAKES_IN_BOX, "usgs", "earthquake", "not json")).isFalse();
        assertThat(RestrictionClamp.testGeoJson(QUAKES_IN_BOX, "usgs", "earthquake",
                "{\"type\":\"Point\",\"coordinates\":[5,5]}")).isTrue();
        assertThat(RestrictionClamp.testGeoJson(QUAKES_IN_BOX, "usgs", "earthquake", null)).isFalse();
    }

    @Test
    @DisplayName("sources outside the restriction are reported as excluded")
    void excludedBy() {
        assertThat(RestrictionClamp.excludedBy(QUAKES_IN_BOX, List.of("usgs", "noaa", "gdacs")))
                .containsExactlyInAnyOrder("noaa", "gdacs");
        assertThat(RestrictionClamp.excludedBy(ReadRestriction.NONE, List.of("usgs"))).isEmpty();
    }

    @Test
    @DisplayName("an area restriction uses the polygon, not only its envelope")
    void areaPolygon() {
        // A triangle whose envelope contains (8, 1) while the triangle itself does not.
        ReadRestriction triangle = new ReadRestriction(null, null, null, ReadRestriction.area(Map.of("type", "Polygon",
                "coordinates", List.of(List.of(List.of(0, 0), List.of(10, 10), List.of(0, 10), List.of(0, 0))))),
                null, true, null);
        assertThat(RestrictionClamp.test(triangle, "s", "k", WGS84.createPoint(new Coordinate(2, 8)))).isTrue();
        assertThat(RestrictionClamp.test(triangle, "s", "k", WGS84.createPoint(new Coordinate(8, 1)))).isFalse();
    }
}
