package ai.foresignal.security.pairing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Pairing code text (prompt 023). */
class PairingCodesTest {

    @Test
    @DisplayName("codes are XXXX-XXXX in Crockford base32 and practically unique")
    void generate() {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            String code = PairingCodes.generate();
            assertThat(code).matches("^[0-9A-HJKMNP-TV-Z]{4}-[0-9A-HJKMNP-TV-Z]{4}$");
            seen.add(code);
        }
        assertThat(seen).hasSizeGreaterThan(990);
    }

    @Test
    @DisplayName("input is case-insensitive, the dash optional, I/L read as 1 and O as 0")
    void normalize() {
        assertThat(PairingCodes.normalize("abcd-efgh")).isEqualTo("ABCDEFGH");
        assertThat(PairingCodes.normalize(" ABCDEFGH ")).isEqualTo("ABCDEFGH");
        assertThat(PairingCodes.normalize("IL0O-1234")).isEqualTo("11001234");
        assertThat(PairingCodes.normalize("ABCD-EFG")).isNull();
        assertThat(PairingCodes.normalize("ABCD-EFGU")).isNull();
        assertThat(PairingCodes.normalize("ABCD_EFGH")).isNull();
        assertThat(PairingCodes.normalize(null)).isNull();
    }
}
