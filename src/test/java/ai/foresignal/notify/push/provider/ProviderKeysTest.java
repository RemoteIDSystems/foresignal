package ai.foresignal.notify.push.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.testsupport.push.NativeTestKeys;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.ECGenParameterSpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Key parsing of uploaded provider secrets (prompt 032); keys are generated at test time. */
class ProviderKeysTest {

    @Test
    @DisplayName("an EC P-256 PKCS#8 PEM is an APNs key; the normalized PEM parses to the same key")
    void apnsKey() {
        KeyPair pair = NativeTestKeys.ec();
        String pem = NativeTestKeys.pem(pair);
        assertThat(ProviderKeys.apnsKey(pem).getS()).isEqualTo(((java.security.interfaces.ECPrivateKey) pair.getPrivate()).getS());
        String messy = "\n  " + pem.replace("\n", "\r\n") + "  ";
        assertThat(ProviderKeys.apnsKey(ProviderKeys.normalize(messy)).getS())
                .isEqualTo(((java.security.interfaces.ECPrivateKey) pair.getPrivate()).getS());
    }

    @Test
    @DisplayName("other curves, RSA keys, garbage and empty input are refused without echoing the key")
    void apnsRefusals() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp384r1"));
        String p384 = NativeTestKeys.pem(generator.generateKeyPair());
        assertThatThrownBy(() -> ProviderKeys.apnsKey(p384)).hasMessageContaining("P-256");
        String rsa = NativeTestKeys.pem(NativeTestKeys.rsa());
        assertThatThrownBy(() -> ProviderKeys.apnsKey(rsa)).isInstanceOf(IllegalArgumentException.class)
                .message().doesNotContain(rsa.substring(30, 60));
        assertThatThrownBy(() -> ProviderKeys.apnsKey("")).hasMessage("is empty");
        assertThatThrownBy(() -> ProviderKeys.apnsKey("hello")).hasMessageContaining("PEM");
        assertThatThrownBy(() -> ProviderKeys.apnsKey("-----BEGIN PRIVATE KEY-----\n%%%\n-----END PRIVATE KEY-----"))
                .hasMessageContaining("base64");
    }

    @Test
    @DisplayName("a 2048-bit RSA PKCS#8 PEM is a service-account key; EC keys are refused")
    void rsaKey() {
        assertThat(ProviderKeys.rsaKey(NativeTestKeys.pem(NativeTestKeys.rsa())).getModulus().bitLength()).isEqualTo(2048);
        assertThatThrownBy(() -> ProviderKeys.rsaKey(NativeTestKeys.pem(NativeTestKeys.ec())))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
