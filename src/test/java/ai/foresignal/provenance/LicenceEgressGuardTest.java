package ai.foresignal.provenance;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.EgressPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The license egress guard table, including the UNKNOWN defaults (prompt 109). */
class LicenceEgressGuardTest {

    private final LicenceEgressGuard guard = new LicenceEgressGuard();
    private final LicenceCatalog catalog = new LicenceCatalog();

    private LicenceTerms terms(String id) {
        return catalog.terms(id);
    }

    @Test
    @DisplayName("ALLOWED redistribution leaves on every path")
    void allowed() {
        LicenceTerms cc = terms("CC-BY-4.0");
        for (EgressPath path : EgressPath.values()) {
            assertThat(guard.allows(path, cc)).as(path.name()).isTrue();
        }
    }

    @Test
    @DisplayName("LOCAL_ONLY leaves internally and on a LAN sink, but not off-box or public")
    void localOnly() {
        LicenceTerms local = terms("local-only-operator");
        assertThat(guard.allows(EgressPath.API_READ, local)).isTrue();
        assertThat(guard.allows(EgressPath.TILE, local)).isTrue();
        assertThat(guard.allows(EgressPath.FEED, local)).isTrue();
        assertThat(guard.allows(EgressPath.SINK_SERVE_LAN, local, false, true)).isTrue();
        assertThat(guard.allows(EgressPath.SINK_SERVE_LAN, local, false, false)).isFalse();
        assertThat(guard.allows(EgressPath.SINK_PUSH, local)).isFalse();
        assertThat(guard.allows(EgressPath.EXPORT, local)).isFalse();
        assertThat(guard.allows(EgressPath.EXPORT, local, true, false)).isTrue();
        assertThat(guard.allows(EgressPath.PUBLIC_SHARE, local)).isFalse();
        assertThat(guard.allows(EgressPath.FEDERATION, local)).isFalse();
    }

    @Test
    @DisplayName("FORBIDDEN is refused off-box and on public paths, but allowed for own use")
    void forbidden() {
        LicenceTerms forbidden = terms("acled-terms");
        assertThat(guard.allows(EgressPath.API_READ, forbidden)).isTrue();
        assertThat(guard.allows(EgressPath.SINK_SERVE_LAN, forbidden, false, true)).isFalse();
        assertThat(guard.allows(EgressPath.SINK_PUSH, forbidden)).isFalse();
        assertThat(guard.allows(EgressPath.EXPORT, forbidden)).isFalse();
        assertThat(guard.allows(EgressPath.PUBLIC_SHARE, forbidden)).isFalse();
        assertThat(guard.allows(EgressPath.FEDERATION, forbidden)).isFalse();
    }

    @Test
    @DisplayName("UNKNOWN defaults to LOCAL_ONLY")
    void unknownDefaults() {
        LicenceTerms unknown = terms("unknown");
        assertThat(guard.allows(EgressPath.API_READ, unknown)).isTrue();
        assertThat(guard.allows(EgressPath.SINK_PUSH, unknown)).isFalse();
        assertThat(guard.allows(EgressPath.PUBLIC_SHARE, unknown)).isFalse();
    }
}
