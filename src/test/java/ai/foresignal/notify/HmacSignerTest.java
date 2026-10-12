package ai.foresignal.notify;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link HmacSigner} (prompt 018) against published HMAC-SHA256 vectors. */
class HmacSignerTest {

    @Test
    @DisplayName("RFC 4231 test case 2")
    void rfc4231() {
        assertThat(HmacSigner.hex("Jefe", "what do ya want for nothing?"))
                .isEqualTo("5bdcc146bf60754e6a042426089575c75a003f089d2739839dec58b964ec3843");
    }

    @Test
    @DisplayName("the well-known 'quick brown fox' vector")
    void quickBrownFox() {
        assertThat(HmacSigner.hex("key", "The quick brown fox jumps over the lazy dog"))
                .isEqualTo("f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8");
    }

    @Test
    @DisplayName("sign covers \"<timestamp>.<body>\"")
    void signsTimestampDotBody() {
        String body = "{\"a\":1}";
        assertThat(HmacSigner.sign("key", "1777636800", body.getBytes(StandardCharsets.UTF_8)))
                .isEqualTo("sha256=" + HmacSigner.hex("key", "1777636800." + body));
    }
}
