package ai.foresignal.provenance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link SourceLicences} and the post-review catalogue additions (prompt 109): a DERIVED source inherits the most
 * restrictive terms of its {@code derivedFrom} inputs (merged when no single input dominates), cycles and unknown
 * inputs fail closed, the descriptor licence strings the review found unresolved now resolve, the MLAT downgrade
 * keeps attribution and the operator's opening lifts only {@code LOCAL_ONLY}.
 */
class SourceLicencesTest {

    final LicenceCatalog catalog = new LicenceCatalog();
    final Map<String, SourceDescriptor> all = new HashMap<>();
    SourceLicences licences;

    static SourceDescriptor plain(String id, String licence) {
        return SourceDescriptor.builder(id).name(id).kind("k").mode(Mode.PUSH).licence(licence).build();
    }

    static SourceDescriptor derived(String id, String... inputs) {
        return SourceDescriptor.builder(id).name(id).kind("k").mode(Mode.DERIVED).licence("derived (see inputs)")
                .derivedFrom(inputs).build();
    }

    @BeforeEach
    void setUp() {
        SourceRegistry registry = mock(SourceRegistry.class);
        when(registry.contains(any())).thenAnswer(i -> all.containsKey(i.<String>getArgument(0)));
        when(registry.descriptor(any())).thenAnswer(i -> all.get(i.<String>getArgument(0)));
        when(registry.find(any())).thenAnswer(i -> Optional.ofNullable(all.get(i.<String>getArgument(0)))
                .map(d -> new SourceRegistry.Registration(d, null, false)));
        licences = new SourceLicences(registry, catalog);
        for (SourceDescriptor d : List.of(plain("by", "CC BY 4.0"), plain("nc", "CC BY-NC 4.0"),
                plain("sa", "ODbL 1.0"), plain("acled", "ACLED Terms of Use: no redistribution of raw data"),
                derived("d-by-acled", "by", "acled"), derived("d-nc-sa", "nc", "sa"), derived("d-loop", "d-loop2"),
                derived("d-loop2", "d-loop"), derived("d-missing", "nowhere"), derived("d-none"))) {
            all.put(d.id(), d);
        }
    }

    @Test
    @DisplayName("derivedFrom: the strictest input wins; no dominant input merges the strictest flags")
    void mostRestrictive() {
        assertThat(licences.termsOf("d-by-acled").id()).isEqualTo("acled-terms");
        LicenceTerms merged = licences.termsOf("d-nc-sa");
        assertThat(merged.id()).isEqualTo("derived");
        assertThat(merged.shareAlike()).isTrue();
        assertThat(merged.effectiveCommercial()).isEqualTo(LicenceTerms.CommercialUse.FORBIDDEN);
        assertThat(merged.attributionRequired()).isTrue();
        assertThat(licences.inputsOf("d-nc-sa")).containsExactly("nc", "sa");
        assertThat(licences.inputsOf("by")).isEmpty();
    }

    @Test
    @DisplayName("cycles, unregistered inputs and no inputs fail closed to unknown")
    void failClosed() {
        assertThat(licences.termsOf("d-loop").id()).isEqualTo(LicenceCatalog.UNKNOWN);
        assertThat(licences.termsOf("d-missing").id()).isEqualTo(LicenceCatalog.UNKNOWN);
        assertThat(licences.termsOf("d-none").id()).isEqualTo(LicenceCatalog.UNKNOWN);
        assertThat(licences.termsOf("not-registered").id()).isEqualTo(LicenceCatalog.UNKNOWN);
    }

    @Test
    @DisplayName("descriptor strings the review found unresolved now resolve to the right terms")
    void reviewedStrings() {
        assertThat(catalog.terms("EMSC data (CC BY 4.0)").id()).isEqualTo("CC-BY-4.0");
        assertThat(catalog.terms("OGL v3 (Open Government Licence)").id()).isEqualTo("OGL-UK-3.0");
        assertThat(catalog.terms("CC BY-NC-SA 3.0 IGO (WHO)").id()).isEqualTo("CC-BY-NC-SA-3.0-IGO");
        assertThat(catalog.terms("ODC-By 1.0").id()).isEqualTo("ODC-By-1.0");
        assertThat(catalog.terms("Open Government Licence – Canada").id()).isEqualTo("OGL-Canada-2.0");
        assertThat(catalog.terms("unspecified — personal use, do not redistribute (gpsjam.org)").redistribution())
                .isEqualTo(LicenceTerms.Redistribution.FORBIDDEN);
        assertThat(catalog.terms("PSK Reporter terms (non-commercial, attribution)").commercialUse())
                .isEqualTo(LicenceTerms.CommercialUse.FORBIDDEN);
        assertThat(catalog.terms("unspecified (OpenMHz)").effectiveRedistribution())
                .isEqualTo(LicenceTerms.Redistribution.LOCAL_ONLY);
        assertThat(catalog.terms("GDELT: unrestricted use and redistribution with citation and a link")
                .redistribution()).isEqualTo(LicenceTerms.Redistribution.ALLOWED);
    }

    @Test
    @DisplayName("the MLAT downgrade keeps id and attribution; the operator opening lifts only LOCAL_ONLY")
    void termTransforms() {
        LicenceTerms odbl = catalog.terms("ODbL-1.0");
        LicenceTerms mlat = odbl.downgradedToLocalOnly();
        assertThat(mlat.id()).isEqualTo("ODbL-1.0");
        assertThat(mlat.attributionRequired()).isTrue();
        assertThat(mlat.attributionText()).isEqualTo(odbl.attributionText());
        assertThat(mlat.redistribution()).isEqualTo(LicenceTerms.Redistribution.LOCAL_ONLY);
        LicenceTerms forbidden = catalog.terms("acled-terms");
        assertThat(forbidden.downgradedToLocalOnly()).isSameAs(forbidden);
        assertThat(forbidden.openedByOperator()).isSameAs(forbidden);
        assertThat(catalog.terms("local-only-operator").openedByOperator().redistribution())
                .isEqualTo(LicenceTerms.Redistribution.ALLOWED);
        assertThat(forbidden.restrictiveness()).isGreaterThan(odbl.restrictiveness());
    }
}
