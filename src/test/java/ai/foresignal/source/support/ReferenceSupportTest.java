package ai.foresignal.source.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import tools.jackson.databind.JsonNode;

/** {@link ReferenceSupport} and {@link ReferenceStreams} (prompt 094, 2026-10 review). */
class ReferenceSupportTest {

    @Test
    @DisplayName("the chunk emitter hands over chunks of at most 5,000 records and the remainder on flush")
    void chunkEmitter() {
        List<List<NormalizedRecord>> chunks = new ArrayList<>();
        ReferenceSupport.ChunkEmitter emitter = new ReferenceSupport.ChunkEmitter(chunks::add);
        for (int i = 0; i < 12_000; i++) {
            emitter.accept(NormalizedRecord.builder("r" + i, "test").build());
        }
        assertThat(chunks).hasSize(2);
        emitter.flush();
        emitter.flush();

        assertThat(chunks).extracting(List::size).containsExactly(5_000, 5_000, 2_000);
        assertThat(emitter.emitted()).isEqualTo(12_000);
    }

    @Test
    @DisplayName("a geodesic area away from the antimeridian is one polygon within [-180, 180]")
    void areaAwayFromAntimeridian() {
        Geometry area = ReferenceSupport.geodesicArea(4.4, 51.9, 10, 64);
        assertThat(area).isInstanceOf(Polygon.class);
        assertThat(area.getEnvelopeInternal().getMinX()).isGreaterThan(4.0);
        assertThat(area.getSRID()).isEqualTo(4326);
    }

    @Test
    @DisplayName("a geodesic area crossing the antimeridian is split into parts that stay within [-180, 180]")
    void areaAcrossAntimeridian() {
        Geometry area = ReferenceSupport.geodesicArea(179.99, -16.8, 10, 64);
        assertThat(area).isInstanceOf(MultiPolygon.class);
        Envelope env = area.getEnvelopeInternal();
        assertThat(env.getMinX()).isGreaterThanOrEqualTo(-180);
        assertThat(env.getMaxX()).isLessThanOrEqualTo(180);
        assertThat(area.getNumGeometries()).isEqualTo(2);
        assertThat(area.getSRID()).isEqualTo(4326);
    }

    @Test
    @DisplayName("a center longitude past 180 is normalized")
    void normalizesLongitude() {
        assertThat(ReferenceSupport.normalizeLongitude(190)).isEqualTo(-170);
        assertThat(ReferenceSupport.normalizeLongitude(-190)).isEqualTo(170);
        assertThat(ReferenceSupport.normalizeLongitude(12.5)).isEqualTo(12.5);
        Polygon circle = ReferenceSupport.geodesicCircle(190, 0, 1, 16);
        assertThat(circle.getCentroid().getX()).isCloseTo(-170, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    @DisplayName("the stream reads the elements of a nested array one at a time and skips other members")
    void streamsNestedArray() {
        String body = """
                {"head":{"vars":["a"]},"results":{"other":[1,2],"bindings":[{"a":1},7,{"a":2}]},"tail":{}}
                """;
        List<JsonNode> seen = new ArrayList<>();
        int count = ReferenceStreams.forEachElement(body.getBytes(StandardCharsets.UTF_8), "doc", seen::add,
                "results", "bindings");
        assertThat(count).isEqualTo(2);
        assertThat(seen).extracting(n -> n.get("a").asInt()).containsExactly(1, 2);
    }

    @Test
    @DisplayName("a document without the array, or one that does not parse, is a format change")
    void streamFailures() {
        assertThatThrownBy(() -> ReferenceStreams.forEachElement(
                        "{\"type\":\"FeatureCollection\"}".getBytes(StandardCharsets.UTF_8), "doc", n -> {}, "features"))
                .isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("features");
        assertThatThrownBy(() -> ReferenceStreams.forEachElement(
                        "{\"features\":[{\"a\":".getBytes(StandardCharsets.UTF_8), "doc", n -> {}, "features"))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> ReferenceStreams.forEachElement(
                        "[]".getBytes(StandardCharsets.UTF_8), "doc", n -> {}, "features"))
                .isInstanceOf(UpstreamFormatException.class);
    }
}
