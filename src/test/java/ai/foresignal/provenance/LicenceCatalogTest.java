package ai.foresignal.provenance;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.SourceDescriptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The licence catalogue: built-in ids, aliases and the restrictive unknown fallback (prompt 109). */
class LicenceCatalogTest {

    private final LicenceCatalog catalog = new LicenceCatalog();

    @Test
    @DisplayName("every built-in id resolves")
    void builtIns() {
        for (String id : new String[] {"CC0-1.0", "CC-BY-4.0", "CC-BY-SA-4.0", "CC-BY-NC-4.0", "CC-BY-NC-SA-4.0",
                "ODbL-1.0", "PDDL-1.0", "OGL-UK-3.0", "NLOD-2.0", "DL-DE-BY-2.0", "us-gov-public-domain",
                "opensky-terms", "acled-terms", "aisstream-terms", "local-only-operator", "unknown"}) {
            assertThat(catalog.find(id)).as(id).isPresent();
        }
    }

    @Test
    @DisplayName("an unknown id resolves to the restrictive unknown terms")
    void unknown() {
        LicenceTerms terms = catalog.terms("no-such-licence");
        assertThat(terms.id()).isEqualTo("unknown");
        assertThat(terms.effectiveCommercial()).isEqualTo(LicenceTerms.CommercialUse.FORBIDDEN);
        assertThat(terms.effectiveRedistribution()).isEqualTo(LicenceTerms.Redistribution.LOCAL_ONLY);
    }

    @Test
    @DisplayName("aliases map a descriptor's free-text licence to a catalogue entry")
    void aliases() {
        assertThat(catalog.terms("US-PD").id()).isEqualTo("us-gov-public-domain");
        assertThat(catalog.terms("OpenSky Network terms (non-commercial research)").id()).isEqualTo("opensky-terms");
        assertThat(catalog.terms("ACLED Terms of Use: no redistribution of raw data").id()).isEqualTo("acled-terms");
        assertThat(catalog.terms("aisstream.io terms (unverified)").id()).isEqualTo("aisstream-terms");
        assertThat(catalog.terms("Operator's own data (no upstream license)").id()).isEqualTo("local-only-operator");
        assertThat(catalog.terms("ODbL-1.0").id()).isEqualTo("ODbL-1.0");
    }

    @Test
    @DisplayName("the binding cases resolve to their restrictive terms")
    void bindingCases() {
        assertThat(catalog.terms("OpenSky Network terms (non-commercial research)").effectiveRedistribution())
                .isEqualTo(LicenceTerms.Redistribution.FORBIDDEN);
        assertThat(catalog.terms("ACLED Terms of Use: no redistribution of raw data").effectiveCommercial())
                .isEqualTo(LicenceTerms.CommercialUse.FORBIDDEN);
        assertThat(catalog.terms("aisstream.io terms (unverified)").effectiveRedistribution())
                .isEqualTo(LicenceTerms.Redistribution.FORBIDDEN);
        LicenceTerms odbl = catalog.terms("ODbL-1.0");
        assertThat(odbl.shareAlike()).isTrue();
        assertThat(odbl.attributionRequired()).isTrue();
    }

    @Test
    @DisplayName("forDescriptor prefers explicit terms, then the licence alias")
    void forDescriptor() {
        SourceDescriptor usPd = SourceDescriptor.builder("usgs").licence("US-PD").build();
        assertThat(catalog.forDescriptor(usPd).id()).isEqualTo("us-gov-public-domain");
        LicenceTerms explicit = catalog.terms("CC-BY-4.0");
        SourceDescriptor custom = SourceDescriptor.builder("x").licence("something").licenceTerms(explicit).build();
        assertThat(catalog.forDescriptor(custom).id()).isEqualTo("CC-BY-4.0");
    }
}
