package ai.foresignal.provenance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The client licence policy flags (prompt 109). */
class LicencePolicyTest {

    private final LicenceCatalog catalog = new LicenceCatalog();

    @Test
    @DisplayName("flags parse, combine and never loosen")
    void parseAndCombine() {
        LicencePolicy policy = LicencePolicy.parse("licencePolicy", "no-nc, no-sa");
        assertThat(policy.noNc()).isTrue();
        assertThat(policy.noSa()).isTrue();
        assertThat(policy.redistributable()).isFalse();
        LicencePolicy combined = policy.plus(LicencePolicy.parse("licencePolicy", "redistributable"));
        assertThat(combined.noNc()).isTrue();
        assertThat(combined.redistributable()).isTrue();
        assertThat(LicencePolicy.NONE.plus(policy)).isEqualTo(policy);
    }

    @Test
    @DisplayName("an unknown flag is a 400")
    void unknownFlag() {
        assertThatThrownBy(() -> LicencePolicy.parse("licencePolicy", "no-nc,whatever"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("no-nc excludes non-commercial, no-sa share-alike, redistributable LOCAL_ONLY")
    void excludes() {
        LicenceTerms nc = catalog.terms("CC-BY-NC-4.0");
        LicenceTerms sa = catalog.terms("ODbL-1.0");
        LicenceTerms localOnly = catalog.terms("local-only-operator");
        assertThat(new LicencePolicy(true, false, false).excludes(nc)).isTrue();
        assertThat(new LicencePolicy(true, false, false).excludes(sa)).isFalse();
        assertThat(new LicencePolicy(false, true, false).excludes(sa)).isTrue();
        assertThat(new LicencePolicy(false, false, true).excludes(localOnly)).isTrue();
        assertThat(new LicencePolicy(true, true, true).excludes(catalog.terms("CC0-1.0"))).isFalse();
    }
}
