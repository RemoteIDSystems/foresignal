package ai.foresignal.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.provenance.AttributionService;
import ai.foresignal.provenance.LicenceCatalog;
import ai.foresignal.provenance.LicenceEgressGuard;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.visibility.Viewer;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The export egress and license decision and the license sidecar (prompt 111), with the real 109
 * catalog and guard and a stand-in {@link EgressPolicy}.
 */
class ExportLicenceServiceTest {

    static final SourceDescriptor OPEN = descriptor("open-src", "CC BY 4.0");
    static final SourceDescriptor FORBIDDEN = descriptor("opensky-like", "OpenSky Network terms (non-commercial research)");
    static final SourceDescriptor LOCAL = descriptor("my-station", "operator-owned");
    static final SourceDescriptor SECRET = descriptor("secret-src", "CC BY 4.0");

    EgressPolicy egress;
    SourceRegistry registry;
    ExportLicenceService service;

    static SourceDescriptor descriptor(String id, String license) {
        return SourceDescriptor.builder(id).name(id).kinds("thing").mode(Mode.PUSH).historyTier(HistoryTier.STANDARD)
                .defaultBudget(Budget.of(60, 1000)).licence(license).attribution("Data: " + id).build();
    }

    @BeforeEach
    void setUp() {
        egress = mock(EgressPolicy.class);
        registry = mock(SourceRegistry.class);
        List<SourceDescriptor> all = List.of(OPEN, FORBIDDEN, LOCAL, SECRET);
        when(registry.descriptors()).thenReturn(all);
        for (SourceDescriptor d : all) {
            when(registry.contains(d.id())).thenReturn(true);
            when(registry.descriptor(d.id())).thenReturn(d);
        }
        when(egress.isPrivate(anyString())).thenReturn(false);
        when(egress.isPrivate("secret-src")).thenReturn(true);
        when(egress.allows(eq(EgressPath.EXPORT), anyString(), any())).thenReturn(true);
        when(egress.allows(eq(EgressPath.EXPORT), eq("secret-src"), any()))
                .thenAnswer(inv -> inv.getArgument(2) instanceof Viewer v && v.canSeePrivate());
        LicenceCatalog catalog = new LicenceCatalog();
        service = new ExportLicenceService(catalog, new LicenceEgressGuard(), egress, registry,
                new AttributionService(registry, catalog));
    }

    @Test
    @DisplayName("FORBIDDEN and LOCAL_ONLY sources are excluded with their reasons; private needs admin and includePrivate")
    void exclusions() {
        ExportLicenceService.Decision agent = service.decide(List.of(OPEN, FORBIDDEN, LOCAL, SECRET),
                new Viewer(ai.foresignal.security.Scope.AGENT, false, null), false, false, false);
        assertThat(agent.allowed()).containsExactly("open-src");
        assertThat(agent.personalUseSources()).isEmpty();
        assertThat(agent.excluded()).extracting(m -> m.get("sourceId") + ":" + m.get("reason"))
                .containsExactly("opensky-like:licence-forbidden", "my-station:licence-local-only");
        ExportLicenceService.Decision adminWithout = service.decide(List.of(SECRET), Viewer.admin(), true, false, false);
        assertThat(adminWithout.allowed()).isEmpty();
        assertThat(adminWithout.excluded()).extracting(m -> m.get("reason")).containsExactly("private");
        ExportLicenceService.Decision adminWith = service.decide(List.of(SECRET), Viewer.admin(), true, true, false);
        assertThat(adminWith.allowed()).containsExactly("secret-src");
    }

    @Test
    @DisplayName("personalUse includes FORBIDDEN and LOCAL_ONLY sources and the summary says so")
    void personalUse() {
        ExportLicenceService.Decision decision = service.decide(List.of(OPEN, FORBIDDEN, LOCAL), Viewer.admin(), true,
                false, true);
        assertThat(decision.allowed()).containsExactly("open-src", "opensky-like", "my-station");
        assertThat(decision.personalUseSources()).containsExactly("opensky-like", "my-station");
        Map<String, Object> summary = service.summary(decision, Map.of("open-src", 3L, "opensky-like", 2L));
        assertThat(summary.get("personalUseOnly")).isEqualTo(true);
        assertThat(summary.get("personalUseSources")).isEqualTo(List.of("opensky-like", "my-station"));
        assertThat(ExportManifests.attributionText(summary)).contains(ExportLicenceService.PERSONAL_USE_NOTICE)
                .contains("opensky-like, my-station").contains("Data: open-src");
    }

    @Test
    @DisplayName("the summary groups sources and rows per license with full terms and attributions")
    void summary() {
        ExportLicenceService.Decision decision = new ExportLicenceService.Decision(List.of("open-src", "secret-src"),
                List.of(), List.of());
        Map<String, Object> summary = service.summary(decision, Map.of("open-src", 5L, "secret-src", 7L));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> licenses = (List<Map<String, Object>>) summary.get("licences");
        assertThat(licenses).hasSize(1);
        assertThat(licenses.getFirst().get("sources")).isEqualTo(List.of("open-src", "secret-src"));
        assertThat(licenses.getFirst().get("rows")).isEqualTo(12L);
        @SuppressWarnings("unchecked")
        Map<String, Object> terms = (Map<String, Object>) licenses.getFirst().get("terms");
        assertThat(terms.get("id")).isEqualTo("CC-BY-4.0");
        assertThat(terms.get("redistribution")).isEqualTo("ALLOWED");
        assertThat(summary.get("personalUseOnly")).isEqualTo(false);
        assertThat(ExportLicenceService.attributionLines(summary)).isNotEmpty();
        assertThat(ExportLicenceService.firstLicenseName(summary)).isNotBlank();
        assertThat(ExportLicenceService.firstTermsUrl(Map.of())).isNull();
        assertThat(ExportManifests.attributionText(summary)).doesNotContain(ExportLicenceService.PERSONAL_USE_NOTICE);
        Map<String, Object> archive = service.archiveSummary(FORBIDDEN, 4);
        assertThat(archive.get("personalUseOnly")).isEqualTo(false);
    }
}
