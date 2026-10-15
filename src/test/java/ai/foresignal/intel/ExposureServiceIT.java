package ai.foresignal.intel;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.place.PlaceService;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

/**
 * The ExposureService (prompt 101): facility counts inside a polygon, places inside, and an
 * {@code not-installed} population section without a pack.
 */
@WebIntegrationTest
@Import(TestIngestSources.class)
class ExposureServiceIT {

    @Autowired ExposureService exposure;
    @Autowired PlaceService places;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired JsonMapper json;

    @Test
    @DisplayName("facilities and places inside a polygon are counted; population is not-installed")
    void exposure() {
        clock.setInstant(Instant.parse("2026-03-01T12:00:00Z"));
        Instant now = clock.instant();
        var place = places.create(json.readTree("{\"name\":\"Home\",\"shape\":\"point\","
                + "\"geometry\":{\"type\":\"Point\",\"coordinates\":[-95.0,32.0]},\"radiusKm\":20}"));
        records.ingestAt("wri-power-plants", now,
                NormalizedRecord.builder("pp1", "power-plant").observedAt(now).point(-95.01, 32.01)
                        .put("name", "Plant").build());

        var area = Geo.point(-95.0, 32.0).buffer(0.5);
        Map<String, Object> result = exposure.exposure(area, true);
        @SuppressWarnings("unchecked")
        Map<String, Object> facilities = (Map<String, Object>) result.get("facilities");
        @SuppressWarnings("unchecked")
        Map<String, Object> counts = (Map<String, Object>) facilities.get("counts");
        assertThat(((Number) counts.get("power-plant")).intValue()).isGreaterThanOrEqualTo(1);
        @SuppressWarnings("unchecked")
        Map<String, Object> placesResult = (Map<String, Object>) result.get("places");
        assertThat(((Number) placesResult.get("count")).intValue()).isGreaterThanOrEqualTo(1);
        @SuppressWarnings("unchecked")
        Map<String, Object> population = (Map<String, Object>) result.get("population");
        assertThat(population.get("status")).isEqualTo("not-installed");
        assertThat(place.id()).isNotBlank();
    }
}
