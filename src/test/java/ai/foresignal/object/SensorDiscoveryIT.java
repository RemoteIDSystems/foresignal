package ai.foresignal.object;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxParser;
import ai.foresignal.observability.ObservabilityService;
import ai.foresignal.security.Scope;
import ai.foresignal.source.adapters.stac.StacSources;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.space.CelestrakElementsService;
import ai.foresignal.space.OmmElements;
import ai.foresignal.store.SourceSettingsEntity;
import ai.foresignal.store.SourceSettingsRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.JsonNode;

/** {@link SensorDiscoveryService}: what can observe an area in a window (prompt 129). */
@IntegrationTest
@Import(AskTestSources.class)
class SensorDiscoveryIT extends ObjectItSupport {

    @Autowired SensorDiscoveryService sensors;
    @Autowired SourceSettingsRepository sourceSettings;
    @MockitoBean ObservabilityService observability;
    @MockitoBean CelestrakElementsService elements;

    static final String AREA = "42,12,46,16";

    private void enable(String sourceId) {
        SourceSettingsEntity row = sourceSettings.findById(sourceId).orElseGet(() -> new SourceSettingsEntity(sourceId));
        row.setEnabled(true);
        sourceSettings.save(row);
    }

    private SensorDiscoveryService.Request request(Set<String> kinds) {
        return new SensorDiscoveryService.Request(BboxParser.parse(AREA), null, clock.instant(),
                clock.instant().plus(Duration.ofDays(1)), kinds, 200);
    }

    private static ObservabilityService.SourceEstimate estimate(String sourceId, double p) {
        Instant at = Instant.parse("2026-10-01T00:00:00Z");
        return new ObservabilityService.SourceEstimate(sourceId, "seismic", "YE", null, null, at, at, p, p - 0.1,
                p + 0.1, 40, "presence-proxy", "medium", at, null);
    }

    @Test
    @DisplayName("sources are listed by their 116 detection probability for the area (above 0.2, enabled only)")
    void sourcesByObservability() {
        enable(AskTestSources.QUAKES);
        enable(AskTestSources.NOTICED);
        when(observability.pDetect(anyString(), any(), any(), any())).thenReturn(new ObservabilityService.Detectability(
                "seismic", "YE", null, null, clock.instant(), 0.8, 0.7, 0.9, List.of("presence-proxy"),
                List.of(AskTestSources.QUAKES), "medium", List.of(estimate(AskTestSources.QUAKES, 0.8),
                        estimate(AskTestSources.NOTICED, 0.1), estimate(AskTestSources.SECRET, 0.9))));
        JsonNode items = JSON.valueToTree(sensors.discover(request(Set.of("source")),
                objectCaller("p-reader", Scope.READ, false)).get("items"));
        assertThat(items).hasSize(1);
        JsonNode s = items.get(0);
        assertThat(s.get("ref").asString()).isEqualTo("sensor:source:" + AskTestSources.QUAKES);
        assertThat(s.get("sensorKind").asString()).isEqualTo("source");
        assertThat(s.get("pDetect").asDouble()).isEqualTo(0.8);
        assertThat(s.get("method").asString()).isEqualTo("presence-proxy");
        assertThat(s.get("families")).isNotEmpty();
    }

    @Test
    @DisplayName("local receivers are listed when their last position is within range")
    void receiversByRange() {
        enable("local-adsb-1");
        enable("local-adsb-2");
        records.ingest("local-adsb-1", NormalizedRecord.builder("a1b2c3", "aircraft").observedAt(clock.instant())
                .point(44.0, 13.5).build());
        records.ingest("local-adsb-2", NormalizedRecord.builder("d4e5f6", "aircraft").observedAt(clock.instant())
                .point(10.0, 50.0).build());
        JsonNode items = JSON.valueToTree(sensors.discover(request(Set.of("receiver")),
                objectCaller("p-admin", Scope.ADMIN, true)).get("items"));
        assertThat(items.findValuesAsString("ref")).containsExactly("sensor:receiver:local-adsb-1");
        assertThat(items.get(0).get("rangeKm").asDouble()).isEqualTo(SensorDiscoveryService.RECEIVER_RANGE_KM);
    }

    @Test
    @DisplayName("curated satellites with predicted passes over the area are listed with their capabilities")
    void predictedPasses() {
        OmmElements sentinel2a = new OmmElements("SENTINEL-2A", "2015-028A", clock.instant(), 14.30824323, 0.0001066,
                98.5633, 3.5871, 92.4346, 267.6959, 0, "U", 40697, 999, 59122, 0.00019021, 0.00000455, 0);
        when(elements.byNorad(anyLong())).thenReturn(Optional.empty());
        when(elements.byNorad(eq(40697L))).thenReturn(Optional.of(sentinel2a));
        JsonNode items = JSON.valueToTree(sensors.discover(request(Set.of("satellite")),
                objectCaller("p-reader", Scope.READ, false)).get("items"));
        assertThat(items).hasSize(1);
        JsonNode sat = items.get(0);
        assertThat(sat.get("ref").asString()).isEqualTo("sensor:satellite:40697");
        assertThat(sat.get("capabilities").get("operator").asString()).contains("Copernicus");
        assertThat(sat.get("capabilities").get("resolutionM").asDouble()).isEqualTo(10.0);
        assertThat(sat.get("passes")).isNotEmpty();
        for (JsonNode pass : sat.get("passes")) {
            Instant aos = Instant.parse(pass.get("aos").asString());
            assertThat(aos).isBetween(clock.instant(), clock.instant().plus(Duration.ofDays(1)));
            assertThat(pass.get("maxElevationDeg").asDouble()).isGreaterThanOrEqualTo(10.0);
        }
    }

    @Test
    @DisplayName("STAC scenes intersecting the area in the window are listed with footprint and links")
    void stacScenes() {
        Instant acquired = clock.instant().plus(Duration.ofHours(2));
        records.ingest(StacSources.SENTINEL2, NormalizedRecord.builder("S2A_38PKB_20261012", "satellite-scene")
                .observedAt(acquired).put("platform", "sentinel-2a").put("thumbnailUrl", "https://example.test/t.png")
                .geometry(GEOMETRY.createPolygon(new Coordinate[] {new Coordinate(43, 13), new Coordinate(44, 13),
                    new Coordinate(44, 14), new Coordinate(43, 14), new Coordinate(43, 13)})).build());
        records.ingest(StacSources.SENTINEL2, NormalizedRecord.builder("S2A_far", "satellite-scene")
                .observedAt(acquired).point(5.0, 50.0).build());
        Map<String, Object> out = sensors.discover(request(Set.of("scene")), objectCaller("p-reader", Scope.READ,
                false));
        JsonNode items = JSON.valueToTree(out.get("items"));
        assertThat(items).hasSize(1);
        assertThat(items.get(0).get("sensorKind").asString()).isEqualTo("scene");
        assertThat(items.get(0).get("footprint").get("type").asString()).isEqualTo("Polygon");
        assertThat(items.get(0).get("links").get("thumbnail").asString()).isEqualTo("https://example.test/t.png");
        assertThat(out.get("note")).isEqualTo(SensorDiscoveryService.NOTE);
    }

    @Test
    @DisplayName("the area and window are validated")
    void validation() {
        ObjectCaller reader = objectCaller("p-reader", Scope.READ, false);
        assertThatThrownBy(() -> sensors.discover(new SensorDiscoveryService.Request(null, null, null, null, Set.of(),
                10), reader)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> sensors.discover(new SensorDiscoveryService.Request(BboxParser.parse(AREA), null,
                clock.instant(), clock.instant().plus(Duration.ofDays(8)), Set.of(), 10), reader))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> sensors.discover(new SensorDiscoveryService.Request(null, "home", null, null,
                Set.of(), 10), objectCaller(null, null, false)))
                .isInstanceOf(ai.foresignal.api.error.ForesignalException.class);
    }
}
