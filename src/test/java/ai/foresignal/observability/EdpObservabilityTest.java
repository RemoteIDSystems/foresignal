package ai.foresignal.observability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.collect.HealthService;
import ai.foresignal.evidence.CanonicalRules;
import ai.foresignal.evidence.OriginatorRegistry;
import ai.foresignal.reference.RegionTagger;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.trigger.window.CoverageGate;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Declared coverage of bound EDP streams in the observability model (prompt 138): a stream's manifest coverage is a
 * low-confidence {@code declared} estimate of its provider for the bound family, inside its footprint only, and the
 * absence rule ({@code canSeeAbsence}) holds only when that estimate reaches {@code absenceMinDetect}.
 */
class EdpObservabilityTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final Instant NOW = Instant.parse("2026-10-18T06:00:00Z");
    private static final String SOURCE = "ext-test-lab";
    private static final String FAMILY = "maritime-incident";

    /** The Hormuz box of the EDP §2 example. */
    private static Geometry box() {
        GeometryFactory f = new GeometryFactory();
        return f.createPolygon(new Coordinate[] {new Coordinate(54.5, 25.0), new Coordinate(58.0, 25.0),
                new Coordinate(58.0, 27.5), new Coordinate(54.5, 27.5), new Coordinate(54.5, 25.0)});
    }

    /** An external coverage provider with one bound source of {@link #FAMILY}. */
    private static ExternalCoverage coverage(double pDetect) {
        return new ExternalCoverage() {
            @Override
            public Set<String> kindsOf(String family) {
                return FAMILY.equals(family) ? Set.of("maritime-incident-report") : Set.of();
            }

            @Override
            public Declared coverage(String sourceId, String family) {
                return SOURCE.equals(sourceId) && FAMILY.equals(family) ? new Declared(box(), pDetect) : null;
            }
        };
    }

    /** The service with no empirical estimates, covered sources and the given external coverage. */
    @SuppressWarnings("unchecked")
    private static ObservabilityService service(double pDetect) {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        CanonicalRules rules = new CanonicalRules(JSON);
        ObjectProvider<SourceRegistry> sources = mock(ObjectProvider.class);
        ObservabilityDeclarations declarations = new ObservabilityDeclarations(JSON, rules, sources);
        ObjectProvider<ExternalCoverage> external = mock(ObjectProvider.class);
        when(external.getIfAvailable()).thenReturn(coverage(pDetect));
        declarations.setExternal(external);
        ObjectProvider<HealthService> health = mock(ObjectProvider.class);
        CoverageGate gate = mock(CoverageGate.class);
        when(gate.evaluate(any(), any(), any(), any(), any(), anyDouble())).thenReturn(CoverageGate.Gate.covered());
        ObjectProvider<CoverageGate> gates = mock(ObjectProvider.class);
        when(gates.getIfAvailable()).thenReturn(gate);
        ObjectProvider<RegionTagger> regions = mock(ObjectProvider.class);
        AppSettingService settings = mock(AppSettingService.class);
        when(settings.get(eq(ai.foresignal.settings.SettingKeys.ABSENCE_MIN_DETECT), eq(Double.class)))
                .thenReturn(Optional.empty());
        OriginatorRegistry originators = OriginatorRegistry.of(Map.of(), JSON);
        return new ObservabilityService(jdbc, declarations, originators, sources, health, gates, regions, settings,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("Declared.p: pDetect inside the footprint, 0 outside, pDetect everywhere without one, clamped")
    void declaredProbability() {
        ExternalCoverage.Declared d = new ExternalCoverage.Declared(box(), 0.6);
        assertThat(d.p(Place.point(56.4, 26.5))).isEqualTo(0.6);
        assertThat(d.p(Place.point(10, 10))).isEqualTo(0.0);
        assertThat(d.p(Place.region("IR"))).isEqualTo(0.0);
        assertThat(new ExternalCoverage.Declared(null, 0.7).p(Place.region("IR"))).isEqualTo(0.7);
        assertThat(new ExternalCoverage.Declared(null, 1.5).pDetect()).isEqualTo(1.0);
        assertThat(new ExternalCoverage.Declared(null, -1).pDetect()).isEqualTo(0.0);
        assertThat(new ExternalCoverage.Declared(null, Double.NaN).pDetect()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("a bound stream's coverage is a low-confidence declared estimate, inside its footprint only")
    void declaredEstimate() {
        ObservabilityService service = service(0.6);
        ObservabilityService.Detectability inside = service.pDetect(Set.of(SOURCE), FAMILY, Place.point(56.4, 26.5),
                NOW, null);
        assertThat(inside.p()).isEqualTo(0.6);
        assertThat(inside.methods()).containsExactly(ObservabilityService.DECLARED);
        assertThat(inside.confidence()).isEqualTo("low");
        ObservabilityService.Detectability outside = service.pDetect(Set.of(SOURCE), FAMILY, Place.point(10, 10),
                NOW, null);
        assertThat(outside.p()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("canSeeAbsence: 0.6 declared is LOW_DETECTABILITY at the default 0.8; 0.9 inside is ok")
    void absence() {
        Instant from = NOW.minusSeconds(7_200);
        AbsenceCheck low = service(0.6).canSeeAbsence(List.of(SOURCE), FAMILY, Place.point(56.4, 26.5), from, NOW,
                null);
        assertThat(low.ok()).isFalse();
        assertThat(low.reason()).isEqualTo(AbsenceCheck.LOW_DETECTABILITY);
        AbsenceCheck ok = service(0.9).canSeeAbsence(List.of(SOURCE), FAMILY, Place.point(56.4, 26.5), from, NOW,
                null);
        assertThat(ok.ok()).isTrue();
        AbsenceCheck outside = service(0.9).canSeeAbsence(List.of(SOURCE), FAMILY, Place.point(10, 10), from, NOW,
                null);
        assertThat(outside.ok()).isFalse();
    }

    @Test
    @DisplayName("kindsOf adds the kinds external bindings link into a family")
    @SuppressWarnings("unchecked")
    void kinds() {
        ObjectProvider<SourceRegistry> sources = mock(ObjectProvider.class);
        ObservabilityDeclarations declarations = new ObservabilityDeclarations(JSON, new CanonicalRules(JSON),
                sources);
        assertThat(declarations.kindsOf(FAMILY)).containsExactly(FAMILY);
        ObjectProvider<ExternalCoverage> external = mock(ObjectProvider.class);
        when(external.getIfAvailable()).thenReturn(coverage(0.6));
        declarations.setExternal(external);
        assertThat(declarations.kindsOf(FAMILY)).containsExactly("maritime-incident-report");
        assertThat(declarations.kindsOf("seismic")).contains("earthquake").doesNotContain("maritime-incident-report");
    }
}
