package ai.foresignal.intel;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * {@link IntelEventMapper}: the shapes the contract gives {@code IntelligenceEvent.inference.geometry} (a GeoJSON
 * geometry) and {@code IntelEventMember.id} (a string).
 */
class IntelEventMapperTest {

    private final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

    @Test
    @DisplayName("inference.geometry is GeoJSON, not WKT; the sections carry the members the contract requires")
    @SuppressWarnings("unchecked")
    void geometryIsGeoJson() {
        IntelEventMapper mapper = new IntelEventMapper();
        IntelligenceEventEntity event = new IntelligenceEventEntity("01M5GVP0SAWH6QVQY0Y2VT85QC",
                "infrastructure-disruption", "Outage", Instant.parse("2026-10-21T20:35:00Z"));
        event.setGeom(factory.createPolygon(new Coordinate[] {new Coordinate(-82.5, 8.4), new Coordinate(-82.4, 8.4),
            new Coordinate(-82.4, 8.5), new Coordinate(-82.5, 8.4)}));
        Map<String, Object> inference = (Map<String, Object>) mapper.full(event, List.of(), List.of()).get("inference");
        assertThat(inference.get("geometry")).isInstanceOf(Map.class);
        assertThat(((Map<String, Object>) inference.get("geometry")).get("type")).isEqualTo("Polygon");

        Map<String, Object> full = mapper.full(event, List.of(), List.of());
        assertThat(full).containsKey("createdAt").containsEntry("context", Map.of());
        assertThat(inference).containsKeys("confidence", "confidenceLabel", "confidenceMethod", "calibrated",
                "evidenceQuality", "startTime", "explanation", "rawConfidence", "centroid", "regionTags");
        Map<String, Object> speculation = (Map<String, Object>) full.get("speculation");
        assertThat(speculation).containsEntry("label", "speculation").containsEntry("alternatives", List.of());
        assertThat((Map<String, Object>) full.get("evidence")).containsEntry("signalFamilies", List.of());

        event.setGeom(null);
        inference = (Map<String, Object>) mapper.full(event, List.of(), List.of()).get("inference");
        assertThat(inference).containsEntry("geometry", null);
    }
}
