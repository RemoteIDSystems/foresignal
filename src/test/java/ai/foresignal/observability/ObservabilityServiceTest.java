package ai.foresignal.observability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.collect.HealthService;
import ai.foresignal.evidence.CanonicalRules;
import ai.foresignal.evidence.OriginatorRegistry;
import ai.foresignal.reference.RegionTagger;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.DeclaredObservability;
import ai.foresignal.trigger.window.CoverageGate;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Detection probabilities (prompt 116, OBS-1): combination across independence groups (product across groups,
 * max within one), the declared fallback with area and thresholds, and the absence rule: false on unhealthy
 * sources or a combined p below {@code absenceMinDetect}.
 */
class ObservabilityServiceTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final Instant NOW = Instant.parse("2026-03-01T12:00:00Z");

    private static OriginatorRegistry originators() throws Exception {
        try (InputStream in = new ClassPathResource(OriginatorRegistry.RESOURCE).getInputStream()) {
            return OriginatorRegistry.of(JSON.readValue(in, new TypeReference<LinkedHashMap<String, Object>>() {}), JSON);
        }
    }

    private static ObservabilityService.SourceEstimate estimate(String source, double p) {
        return new ObservabilityService.SourceEstimate(source, "seismic", null, null, null, NOW.minusSeconds(86_400),
                NOW.minusSeconds(3_600), p, Math.max(0, p - 0.1), Math.min(1, p + 0.1), 40, CaptureRecapture.METHOD,
                "moderate", NOW, null);
    }

    @Test
    @DisplayName("independent groups combine as 1 − Π(1 − p); sources of one group combine by max")
    void combination() throws Exception {
        OriginatorRegistry registry = originators();
        ObservabilityService.Detectability independent = ObservabilityService.combineEstimates("seismic", null, null,
                null, NOW, List.of(estimate("usgs-earthquakes", 0.6), estimate("emsc-seismic", 0.5)), registry);
        assertThat(independent.p()).isEqualTo(0.8);
        assertThat(independent.sources()).containsExactly("emsc-seismic", "usgs-earthquakes");
        assertThat(independent.methods()).containsExactly(CaptureRecapture.METHOD);
        // usgs-shakemap and usgs-earthquakes share the usgs-anss originator: one group, max not product.
        ObservabilityService.Detectability sameGroup = ObservabilityService.combineEstimates("seismic", null, null,
                null, NOW, List.of(estimate("usgs-earthquakes", 0.6), estimate("usgs-shakemap", 0.5)), registry);
        assertThat(sameGroup.p()).isEqualTo(0.6);
        ObservabilityService.Detectability none = ObservabilityService.combineEstimates("seismic", null, null, null,
                NOW, List.of(), registry);
        assertThat(none.p()).isEqualTo(0.0);
        assertThat(none.confidence()).isEqualTo("low");
    }

    @Test
    @DisplayName("declared: 1 inside the area and at or above the threshold that applies to the place, else 0")
    void declared() {
        DeclaredObservability.KindObservability usgs = new DeclaredObservability.KindObservability(
                DeclaredObservability.Area.GLOBAL, List.of(new DeclaredObservability.Threshold("mag", 2.5, "US"),
                        new DeclaredObservability.Threshold("mag", 4.5, "global")), null, null);
        assertThat(ObservabilityService.declaredP(usgs, Place.point(-100, 40), List.of("US"), 3.0)).isEqualTo(1.0);
        assertThat(ObservabilityService.declaredP(usgs, Place.point(30, 38), List.of("TR"), 3.0)).isEqualTo(0.0);
        assertThat(ObservabilityService.declaredP(usgs, Place.point(30, 38), List.of("TR"), 4.5)).isEqualTo(1.0);
        assertThat(ObservabilityService.declaredP(usgs, Place.point(30, 38), List.of("TR"), null)).isEqualTo(1.0);
        DeclaredObservability.KindObservability goes = new DeclaredObservability.KindObservability(
                new DeclaredObservability.Area("bbox", List.of(-180.0, -60.0, -10.0, 60.0), null), List.of(), null, null);
        assertThat(ObservabilityService.declaredP(goes, Place.point(-90, 30), List.of(), null)).isEqualTo(1.0);
        assertThat(ObservabilityService.declaredP(goes, Place.point(30, 30), List.of(), null)).isEqualTo(0.0);
        assertThat(ObservabilityService.declaredP(goes, Place.GLOBAL, List.of(), null)).isEqualTo(0.0);
        DeclaredObservability.KindObservability nws = new DeclaredObservability.KindObservability(
                new DeclaredObservability.Area("regions", null, List.of("US")), List.of(), null, null);
        assertThat(ObservabilityService.declaredP(nws, Place.region("US-CA"), List.of("US-CA", "US"), null))
                .isEqualTo(1.0);
        assertThat(ObservabilityService.declaredP(nws, Place.region("CA"), List.of("CA"), null)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("snapshot lookups prefer the cell, then the region, then everywhere; a track cell without rows is 0")
    void snapshotSpecificity() {
        ObservabilityService.SourceEstimate global = new ObservabilityService.SourceEstimate("s", "aircraft", null,
                null, null, NOW, NOW, 0.4, 0.3, 0.5, 10, CaptureRecapture.METHOD, "moderate", NOW, null);
        ObservabilityService.SourceEstimate region = new ObservabilityService.SourceEstimate("s", "aircraft", "US",
                null, null, NOW, NOW, 0.6, 0.5, 0.7, 10, CaptureRecapture.METHOD, "moderate", NOW, null);
        ObservabilityService.Snapshot snapshot = new ObservabilityService.Snapshot(List.of(global, region));
        assertThat(snapshot.best("s", null, List.of("US"), null).p()).isEqualTo(0.6);
        assertThat(snapshot.best("s", null, List.of("CA"), null).p()).isEqualTo(0.4);
        ObservabilityService.SourceEstimate cell = new ObservabilityService.SourceEstimate("t", "aircraft", null,
                "8428309ffffffff", null, NOW, NOW, 0.9, 0, 1, 10, PresenceProxy.METHOD, "low", NOW, null);
        ObservabilityService.Snapshot tracks = new ObservabilityService.Snapshot(List.of(cell));
        assertThat(tracks.best("t", "8428309ffffffff", List.of(), null).p()).isEqualTo(0.9);
        assertThat(tracks.best("t", "842830bffffffff", List.of(), null).p()).isEqualTo(0.0);
        assertThat(tracks.best("u", "842830bffffffff", List.of(), null)).isNull();
    }

    @SuppressWarnings("unchecked")
    private ObservabilityService service(double estimateP, CoverageGate.Gate gateResult) throws Exception {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.query(anyString(), any(RowMapper.class), any(), any(), any())).thenReturn(List.of(
                new ObservabilityService.SourceEstimate("usgs-earthquakes", "seismic", null, null, null,
                        NOW.minusSeconds(86_400 * 365L), NOW.minusSeconds(3_600), estimateP, estimateP, estimateP, 40,
                        CaptureRecapture.METHOD, "moderate", NOW, null)));
        CanonicalRules rules = new CanonicalRules(JSON);
        ObjectProvider<SourceRegistry> sources = mock(ObjectProvider.class);
        ObservabilityDeclarations declarations = new ObservabilityDeclarations(JSON, rules, sources);
        ObjectProvider<HealthService> health = mock(ObjectProvider.class);
        CoverageGate gate = mock(CoverageGate.class);
        when(gate.evaluate(any(), any(), any(), any(), any(), anyDouble())).thenReturn(gateResult);
        ObjectProvider<CoverageGate> gates = mock(ObjectProvider.class);
        when(gates.getIfAvailable()).thenReturn(gate);
        ObjectProvider<RegionTagger> regions = mock(ObjectProvider.class);
        AppSettingService settings = mock(AppSettingService.class);
        when(settings.get(eq(ai.foresignal.settings.SettingKeys.ABSENCE_MIN_DETECT), eq(Double.class)))
                .thenReturn(Optional.empty());
        return new ObservabilityService(jdbc, declarations, originators(), sources, health, gates, regions, settings,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("canSeeAbsence: healthy sources and p ≥ 0.8 is ok; low p is LOW_DETECTABILITY; unhealthy is the coverage reason")
    void canSeeAbsence() throws Exception {
        Instant from = NOW.minusSeconds(7_200);
        AbsenceCheck ok = service(0.9, CoverageGate.Gate.covered()).canSeeAbsence(Set.of("usgs-earthquakes"),
                "seismic", Place.GLOBAL, from, NOW, null);
        assertThat(ok.ok()).isTrue();
        assertThat(ok.p()).isEqualTo(0.9);
        AbsenceCheck low = service(0.3, CoverageGate.Gate.covered()).canSeeAbsence(Set.of("usgs-earthquakes"),
                "seismic", Place.GLOBAL, from, NOW, null);
        assertThat(low.ok()).isFalse();
        assertThat(low.reason()).isEqualTo(AbsenceCheck.LOW_DETECTABILITY);
        AbsenceCheck unhealthy = service(0.95, CoverageGate.Gate.unknown("source usgs-earthquakes is failing"))
                .canSeeAbsence(Set.of("usgs-earthquakes"), "seismic", Place.GLOBAL, from, NOW, null);
        assertThat(unhealthy.ok()).isFalse();
        assertThat(unhealthy.reason()).isEqualTo("source usgs-earthquakes is failing");
        AbsenceCheck nobody = service(0.95, CoverageGate.Gate.covered()).canSeeAbsence(Set.of(), "seismic",
                Place.GLOBAL, from, NOW, null);
        assertThat(nobody.ok()).isFalse();
    }

    @Test
    @DisplayName("the default absenceMinDetect is 0.8; tiles use coarser hexagons at small zooms")
    void defaults() throws Exception {
        assertThat(service(0.5, CoverageGate.Gate.covered()).absenceMinDetect()).isEqualTo(0.8);
        assertThat(ObservabilityService.resolutionForZoom(0)).isEqualTo(1);
        assertThat(ObservabilityService.resolutionForZoom(5)).isEqualTo(3);
        assertThat(ObservabilityService.resolutionForZoom(12)).isEqualTo(ObservabilityService.CELL_RESOLUTION);
        assertThat(ObservabilityService.cellsIn(new double[] {-1, -1, 1, 1}, 4, 10_000)).isNotEmpty();
    }

    @Test
    @DisplayName("bundled declarations: USGS thresholds, NWS regions, and the observability family of each kind")
    void bundledDeclarations() {
        CanonicalRules rules = new CanonicalRules(JSON);
        @SuppressWarnings("unchecked")
        ObjectProvider<SourceRegistry> sources = mock(ObjectProvider.class);
        ObservabilityDeclarations declarations = new ObservabilityDeclarations(JSON, rules, sources);
        assertThat(declarations.declared("usgs-earthquakes").kind("earthquake").thresholds()).hasSize(2);
        assertThat(declarations.declared("nws-alerts").kind("nws-alert").area().regionCodes()).containsExactly("US");
        assertThat(declarations.familyOfKind("earthquake")).isEqualTo("seismic");
        assertThat(declarations.familyOfKind("aircraft")).isEqualTo("aircraft");
        assertThat(declarations.familyOfKind("thing")).isEqualTo("thing");
        assertThat(declarations.kindsOf("seismic")).contains("earthquake", "disaster");
        assertThat(declarations.trackFamilies()).containsExactlyInAnyOrder("aircraft", "vessel");
        Map<String, Object> empty = Map.of();
        assertThat(new ObservabilityDeclarations(empty, rules, sources).declared("usgs-earthquakes")).isNull();
    }
}
