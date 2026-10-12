package ai.foresignal.place;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.place.PlaceTestSources;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKBWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link PlaceGeometry} (prompt 036): point and route areas are geography buffers (their area on the
 * ellipsoid matches π r² and 2·b·L + π b² within 1%), polygons are fixed, the vertex cap holds, and
 * a polygon across the antimeridian is split into parts within ±180°. Needs PostGIS for the
 * geography buffers.
 */
@IntegrationTest
@Import(PlaceTestSources.class)
class PlaceGeometryTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final GeometryFactory WGS84 = new GeometryFactory(new PrecisionModel(), 4326);

    @Autowired PlaceGeometry geometry;
    @Autowired JdbcTemplate jdbc;

    static JsonNode json(String text) {
        return JSON.readTree(text.replace('\'', '"'));
    }

    double areaKm2(Geometry g) {
        Double m2 = jdbc.queryForObject("SELECT ST_Area(ST_GeomFromWKB(?, 4326)::geography)", Double.class,
                new WKBWriter(2).write(g));
        return m2 / 1e6;
    }

    double lengthKm(Geometry g) {
        Double m = jdbc.queryForObject("SELECT ST_Length(ST_GeomFromWKB(?, 4326)::geography)", Double.class,
                new WKBWriter(2).write(g));
        return m / 1000.0;
    }

    @Test
    @DisplayName("a point place's area is a geography disc: area ≈ π r² within 1%, at the equator and at 60°N")
    void pointBuffer() {
        for (double lat : new double[] {0, 60}) {
            Geometry point = geometry.read("point", json("{'type':'Point','coordinates':[10," + lat + "]}"));
            MultiPolygon area = geometry.area("point", point, 20, 0);
            assertThat(area.getSRID()).isEqualTo(4326);
            assertThat(area.isValid()).isTrue();
            assertThat(areaKm2(area)).as("lat " + lat).isCloseTo(Math.PI * 20 * 20, org.assertj.core.data.Percentage
                    .withPercentage(1));
        }
    }

    @Test
    @DisplayName("a route place's area is a geography corridor: ≈ 2·b·L + π b² within 1%")
    void routeBuffer() {
        Geometry line = geometry.read("route", json("{'type':'LineString','coordinates':[[-97.7,30.2],[-97.0,30.9]]}"));
        MultiPolygon area = geometry.area("route", line, 0, 5_000);
        double l = lengthKm(line);
        double b = 5;
        assertThat(areaKm2(area)).isCloseTo(2 * b * l + Math.PI * b * b,
                org.assertj.core.data.Percentage.withPercentage(1));
    }

    @Test
    @DisplayName("a self-intersecting polygon is fixed into a valid area")
    void polygonFixing() {
        Geometry bowtie = geometry.read("polygon", json("{'type':'Polygon','coordinates':[[[0,0],[2,2],[2,0],[0,2],[0,0]]]}"));
        assertThat(bowtie.isValid()).isFalse();
        MultiPolygon area = geometry.area("polygon", bowtie, 0, 0);
        assertThat(area.isValid()).isTrue();
        assertThat(area.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("shapes must match their geometry, and coordinates must lie on the globe")
    void shapeChecks() {
        assertThatThrownBy(() -> geometry.read("point", json("{'type':'LineString','coordinates':[[0,0],[1,1]]}")))
                .isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> geometry.read("route", json("{'type':'Point','coordinates':[0,0]}")))
                .isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> geometry.read("point", json("{'type':'Point','coordinates':[0,95]}")))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    @DisplayName("the vertex cap: more than 10,000 input vertices is refused; a larger derived area is simplified")
    void vertexCap() {
        StringBuilder ring = new StringBuilder("{'type':'Polygon','coordinates':[[");
        int n = PlaceGeometry.MAX_VERTICES + 10;
        for (int i = 0; i < n; i++) {
            double a = 2 * Math.PI * i / n;
            ring.append('[').append(Math.cos(a)).append(',').append(Math.sin(a)).append("],");
        }
        ring.append("[1,0]]]}");
        assertThatThrownBy(() -> geometry.read("polygon", json(ring.toString()))).isInstanceOf(InvalidInputException.class);

        List<Polygon> parts = new ArrayList<>();
        for (int p = 0; p < 3; p++) {
            Coordinate[] coordinates = new Coordinate[4_001];
            for (int i = 0; i < 4_000; i++) {
                double a = 2 * Math.PI * i / 4_000;
                coordinates[i] = new Coordinate(p * 3 + Math.cos(a), Math.sin(a));
            }
            coordinates[4_000] = coordinates[0];
            parts.add(WGS84.createPolygon(coordinates));
        }
        MultiPolygon big = WGS84.createMultiPolygon(parts.toArray(Polygon[]::new));
        assertThat(big.getNumPoints()).isGreaterThan(PlaceGeometry.MAX_VERTICES);
        MultiPolygon capped = PlaceGeometry.capVertices(big);
        assertThat(capped.getNumPoints()).isLessThanOrEqualTo(PlaceGeometry.MAX_VERTICES);
        assertThat(capped.getNumGeometries()).isEqualTo(3);
    }

    @Test
    @DisplayName("a polygon across the antimeridian is split into parts within ±180°")
    void antimeridian() {
        Geometry crossing = geometry.read("polygon",
                json("{'type':'Polygon','coordinates':[[[170,-10],[-170,-10],[-170,10],[170,10],[170,-10]]]}"));
        MultiPolygon area = geometry.area("polygon", crossing, 0, 0);
        assertThat(area.getNumGeometries()).isEqualTo(2);
        assertThat(area.getEnvelopeInternal().getMinX()).isGreaterThanOrEqualTo(-180);
        assertThat(area.getEnvelopeInternal().getMaxX()).isLessThanOrEqualTo(180);
        assertThat(area.covers(WGS84.createPoint(new Coordinate(179.5, 0)))).isTrue();
        assertThat(area.covers(WGS84.createPoint(new Coordinate(-179.5, 0)))).isTrue();
        assertThat(area.covers(WGS84.createPoint(new Coordinate(0, 0)))).isFalse();
    }
}
