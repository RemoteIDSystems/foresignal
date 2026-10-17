package ai.foresignal.collab;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.Tlp;
import ai.foresignal.security.TlpTarget;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The TLP 2.0 rules of {@link EgressPolicy#tlp} (prompt 130, COL-2): every label against every share target, and the
 * strictest-label inheritance of derived products.
 */
class TlpPolicyTest {

    static boolean allowed(Tlp tlp, TlpTarget target) {
        return EgressPolicy.tlp(tlp, target).allowed();
    }

    @Test
    @DisplayName("RED reaches only named principals here and named recipients elsewhere")
    void red() {
        assertThat(allowed(Tlp.RED, TlpTarget.principal(false))).isTrue();
        assertThat(allowed(Tlp.RED, TlpTarget.group(true))).isFalse();
        assertThat(allowed(Tlp.RED, TlpTarget.instance(true))).isFalse();
        assertThat(allowed(Tlp.RED, TlpTarget.publicLink())).isFalse();
        assertThat(allowed(Tlp.RED, TlpTarget.federatedPeer())).isFalse();
        assertThat(allowed(Tlp.RED, TlpTarget.remoteRecipient(false))).isTrue();
        assertThat(EgressPolicy.tlp(Tlp.RED, TlpTarget.instance(true)).rule()).contains("RED");
    }

    @Test
    @DisplayName("AMBER+STRICT stays within the owner's organization and named recipients")
    void amberStrict() {
        assertThat(allowed(Tlp.AMBER_STRICT, TlpTarget.principal(false))).isTrue();
        assertThat(allowed(Tlp.AMBER_STRICT, TlpTarget.group(true))).isTrue();
        assertThat(allowed(Tlp.AMBER_STRICT, TlpTarget.group(false))).isFalse();
        assertThat(allowed(Tlp.AMBER_STRICT, TlpTarget.instance(true))).isTrue();
        assertThat(allowed(Tlp.AMBER_STRICT, TlpTarget.instance(false))).isFalse();
        assertThat(allowed(Tlp.AMBER_STRICT, TlpTarget.publicLink())).isFalse();
        assertThat(allowed(Tlp.AMBER_STRICT, TlpTarget.federatedPeer())).isFalse();
        assertThat(allowed(Tlp.AMBER_STRICT, TlpTarget.remoteRecipient(true))).isTrue();
        assertThat(allowed(Tlp.AMBER_STRICT, TlpTarget.remoteRecipient(false))).isFalse();
    }

    @Test
    @DisplayName("AMBER adds named recipients' organizations on other instances")
    void amber() {
        assertThat(allowed(Tlp.AMBER, TlpTarget.principal(false))).isTrue();
        assertThat(allowed(Tlp.AMBER, TlpTarget.group(true))).isTrue();
        assertThat(allowed(Tlp.AMBER, TlpTarget.group(false))).isFalse();
        assertThat(allowed(Tlp.AMBER, TlpTarget.instance(true))).isTrue();
        assertThat(allowed(Tlp.AMBER, TlpTarget.instance(false))).isFalse();
        assertThat(allowed(Tlp.AMBER, TlpTarget.publicLink())).isFalse();
        assertThat(allowed(Tlp.AMBER, TlpTarget.federatedPeer())).isFalse();
        assertThat(allowed(Tlp.AMBER, TlpTarget.remoteRecipient(false))).isTrue();
        assertThat(allowed(Tlp.AMBER, new TlpTarget(TlpTarget.Kind.REMOTE_RECIPIENT, true, false))).isFalse();
        assertThat(allowed(null, TlpTarget.instance(false))).isFalse();
    }

    @Test
    @DisplayName("GREEN reaches the instance and peers but no public link; CLEAR reaches everything")
    void greenAndClear() {
        for (TlpTarget t : List.of(TlpTarget.principal(false), TlpTarget.group(false), TlpTarget.instance(false),
                TlpTarget.federatedPeer(), TlpTarget.remoteRecipient(false))) {
            assertThat(allowed(Tlp.GREEN, t)).as("GREEN to " + t).isTrue();
            assertThat(allowed(Tlp.CLEAR, t)).as("CLEAR to " + t).isTrue();
        }
        assertThat(allowed(Tlp.GREEN, TlpTarget.publicLink())).isFalse();
        assertThat(allowed(Tlp.CLEAR, TlpTarget.publicLink())).isTrue();
    }

    @Test
    @DisplayName("derived products inherit the strictest label of their inputs; labels parse from the wire")
    void inheritance() {
        assertThat(Tlp.strictest(List.of(Tlp.GREEN, Tlp.RED, Tlp.AMBER), Tlp.CLEAR)).isEqualTo(Tlp.RED);
        assertThat(Tlp.strictest(List.of(), Tlp.AMBER)).isEqualTo(Tlp.AMBER);
        assertThat(Tlp.stricter(Tlp.AMBER, Tlp.AMBER_STRICT)).isEqualTo(Tlp.AMBER_STRICT);
        assertThat(Tlp.stricter(null, Tlp.GREEN)).isEqualTo(Tlp.GREEN);
        assertThat(Tlp.RED.stricterThan(Tlp.AMBER)).isTrue();
        assertThat(Tlp.CLEAR.stricterThan(Tlp.GREEN)).isFalse();
        assertThat(Tlp.fromWire("amber+strict")).isEqualTo(Tlp.AMBER_STRICT);
        assertThat(Tlp.fromWire("AMBER_STRICT")).isEqualTo(Tlp.AMBER_STRICT);
        assertThat(Tlp.AMBER_STRICT.wireValue()).isEqualTo("AMBER+STRICT");
        assertThat(Tlp.parse("tlp", null, Tlp.GREEN)).isEqualTo(Tlp.GREEN);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> Tlp.parse("tlp", "PURPLE", null))
                .isInstanceOf(ai.foresignal.api.error.ValidationException.class);
    }
}
