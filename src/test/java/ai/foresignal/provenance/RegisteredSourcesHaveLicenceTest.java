package ai.foresignal.provenance;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.EgressPath;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.test.IntegrationTest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Prompt 109, task 1: every registered non-DERIVED source resolves to catalogue terms other than
 * {@code unknown}, and the binding cases hold (OpenSky, ACLED and aisstream {@code FORBIDDEN}; adsb.lol
 * {@code ODbL-1.0}; local receivers {@code LOCAL_ONLY}). Walks {@link SourceRegistry#all()} in the application
 * context, so a new adapter whose free-text licence the catalogue cannot read fails here.
 */
@IntegrationTest
class RegisteredSourcesHaveLicenceTest {

    @Autowired
    SourceRegistry registry;

    @Autowired
    SourceLicences licences;

    @Autowired
    EgressPolicy egress;

    @Test
    @DisplayName("every registered non-DERIVED source resolves to known licence terms")
    void everySourceResolves() {
        List<String> unresolved = new ArrayList<>();
        for (SourceRegistry.Registration registration : registry.all()) {
            SourceDescriptor descriptor = registration.descriptor();
            if (descriptor.mode() == Mode.DERIVED) {
                continue;
            }
            LicenceTerms terms = licences.termsOf(descriptor.id());
            if (terms == null || LicenceCatalog.UNKNOWN.equals(terms.id())) {
                unresolved.add(descriptor.id() + " <- " + descriptor.licence());
            }
        }
        assertThat(unresolved).as("sources whose licence resolves to 'unknown'").isEmpty();
    }

    @Test
    @DisplayName("DERIVED sources inherit terms no less restrictive than any of their resolvable inputs")
    void derivedInheritsTheStrictest() {
        for (SourceRegistry.Registration registration : registry.all()) {
            SourceDescriptor descriptor = registration.descriptor();
            if (descriptor.mode() != Mode.DERIVED) {
                continue;
            }
            LicenceTerms own = licences.termsOf(descriptor.id());
            for (String input : licences.inputsOf(descriptor.id())) {
                if (registry.contains(input)) {
                    assertThat(own.restrictiveness()).as(descriptor.id() + " vs " + input)
                            .isGreaterThanOrEqualTo(licences.termsOf(input).restrictiveness());
                }
            }
        }
    }

    @Test
    @DisplayName("binding cases: OpenSky/ACLED/aisstream FORBIDDEN and denied on sinks, shares and federation")
    void bindingCases() {
        for (String id : List.of("opensky-states", "acled", "aisstream")) {
            if (!registry.contains(id)) {
                continue;
            }
            LicenceTerms terms = licences.termsOf(id);
            assertThat(terms.redistribution()).as(id).isEqualTo(LicenceTerms.Redistribution.FORBIDDEN);
            for (EgressPath path : List.of(EgressPath.SINK, EgressPath.SINK_SERVE_LAN, EgressPath.SINK_PUSH,
                    EgressPath.PUBLIC_SHARE, EgressPath.FEDERATION)) {
                assertThat(egress.decide(path, id, null, null, EgressPolicy.EgressContext.sink(false, true))
                        .allowed()).as(id + " on " + path).isFalse();
            }
        }
        assertThat(licences.termsOf("opensky-states").commercialUse())
                .isEqualTo(LicenceTerms.CommercialUse.FORBIDDEN);
        if (registry.contains("acled")) {
            assertThat(licences.termsOf("acled").commercialUse()).isEqualTo(LicenceTerms.CommercialUse.FORBIDDEN);
        }
        registry.ids().stream().filter(id -> id.startsWith("adsblol-"))
                .forEach(id -> assertThat(licences.termsOf(id).id()).as(id).isEqualTo("ODbL-1.0"));
        registry.ids().stream().filter(id -> id.startsWith("local-adsb-") || id.startsWith("local-ais-"))
                .forEach(id -> assertThat(licences.termsOf(id).effectiveRedistribution()).as(id)
                        .isEqualTo(LicenceTerms.Redistribution.LOCAL_ONLY));
        if (registry.contains("submarine-cables")) {
            assertThat(licences.termsOf("submarine-cables").id()).isEqualTo("CC-BY-NC-SA-4.0");
        }
    }
}
