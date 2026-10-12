package ai.foresignal.place;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Percentage.withPercentage;

import ai.foresignal.collect.HealthRuleContext;
import ai.foresignal.collect.HealthSnapshot;
import ai.foresignal.collect.HealthVerdict;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.place.PlaceTestSources;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link PlacesAoiService} (prompt 036): the AOI is the union of the places, each buffered to at
 * least {@code max(radiusKm, 150 km)}, with its envelope; without places it is the default Texas
 * box and AOI consumers report a health warning; a place change recomputes it and publishes
 * {@link PlacesAoiChangedEvent}.
 */
@IntegrationTest
@Import(PlaceTestSources.class)
@RecordApplicationEvents
class PlacesAoiServiceTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired PlacesAoiService aoi;
    @Autowired PlaceService places;
    @Autowired SourceRegistry registry;
    @Autowired JdbcTemplate jdbc;
    @Autowired ApplicationEvents events;

    @BeforeEach
    void setUp() {
        aoi.resetState();
    }

    void point(String id, double lon, double lat, double radiusKm) {
        places.create(JSON.readTree("{\"id\":\"" + id + "\",\"name\":\"" + id + "\",\"shape\":\"point\",\"geometry\":"
                + "{\"type\":\"Point\",\"coordinates\":[" + lon + "," + lat + "]},\"radiusKm\":" + radiusKm + "}"));
    }

    double areaKm2(Geometry g) {
        return jdbc.queryForObject("SELECT ST_Area(ST_GeomFromWKB(?, 4326)::geography)", Double.class,
                new WKBWriter(2).write(g)) / 1e6;
    }

    @Test
    @DisplayName("no places: the default Texas box, isDefault, and a health warning for AOI consumers only")
    void empty() {
        PlacesAoiService.Aoi current = aoi.current();
        assertThat(current.isDefault()).isTrue();
        assertThat(current.placeCount()).isZero();
        assertThat(current.envelope()).containsExactly(-106.65, 25.84, -93.51, 36.50);
        assertThat(aoi.envelope()).containsExactly(-106.65, 25.84, -93.51, 36.50);

        aoi.registerConsumer(PlaceTestSources.EVENTS);
        HealthRuleContext consumer = new HealthRuleContext(registry.descriptor(PlaceTestSources.EVENTS),
                HealthSnapshot.empty(PlaceTestSources.EVENTS), Instant.now());
        Optional<HealthVerdict> verdict = aoi.evaluate(consumer);
        assertThat(verdict).hasValueSatisfying(v -> {
            assertThat(v.status()).isEqualTo(HealthStatus.DEGRADED);
            assertThat(v.reason()).contains("default area of interest");
        });
        HealthRuleContext other = new HealthRuleContext(registry.descriptor(PlaceTestSources.AREAS),
                HealthSnapshot.empty(PlaceTestSources.AREAS), Instant.now());
        assertThat(aoi.evaluate(other)).isEmpty();
    }

    @Test
    @DisplayName("a 20 km point is buffered to the 150 km floor; a 300 km point keeps its radius; the union and envelope follow")
    void unionAndFloor() {
        point("home", -97.74, 30.27, 20);
        PlacesAoiService.Aoi one = aoi.current();
        assertThat(one.isDefault()).isFalse();
        assertThat(one.placeCount()).isEqualTo(1);
        assertThat(one.bufferKm()).isEqualTo(150);
        assertThat(areaKm2(one.geometry())).isCloseTo(Math.PI * 150 * 150, withPercentage(1.5));
        double[] e = one.envelope();
        assertThat(e[0]).isLessThan(-97.74 - 1.3);
        assertThat(e[2]).isGreaterThan(-97.74 + 1.3);
        assertThat(e[1]).isCloseTo(30.27 - 1.35, org.assertj.core.data.Offset.offset(0.05));

        point("far", 10, 50, 300);
        PlacesAoiService.Aoi two = aoi.current();
        assertThat(two.placeCount()).isEqualTo(2);
        assertThat(two.version()).isGreaterThan(one.version());
        assertThat(areaKm2(two.geometry())).isCloseTo(Math.PI * 150 * 150 + Math.PI * 300 * 300, withPercentage(1.5));
        assertThat(two.envelope()[0]).isLessThan(-98);
        assertThat(two.envelope()[2]).isGreaterThan(13);
    }

    @Test
    @DisplayName("a place change recomputes the AOI and publishes PlacesAoiChangedEvent; the consumer warning clears")
    void recompute() {
        aoi.registerConsumer(PlaceTestSources.EVENTS);
        long before = aoi.version();
        point("home", -97.74, 30.27, 20);
        List<PlacesAoiChangedEvent> received = events.stream(PlacesAoiChangedEvent.class).toList();
        assertThat(received).isNotEmpty();
        assertThat(received.getLast().version()).isGreaterThan(before);
        assertThat(aoi.current().isDefault()).isFalse();
        HealthRuleContext consumer = new HealthRuleContext(registry.descriptor(PlaceTestSources.EVENTS),
                HealthSnapshot.empty(PlaceTestSources.EVENTS), Instant.now());
        assertThat(aoi.evaluate(consumer)).hasValueSatisfying(v -> assertThat(v.status()).isEqualTo(HealthStatus.HEALTHY));

        places.delete("home", false);
        assertThat(aoi.current().isDefault()).isTrue();
    }
}
