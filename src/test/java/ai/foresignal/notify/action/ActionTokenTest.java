package ai.foresignal.notify.action;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The capability token format and its MAC (prompt 033). */
class ActionTokenTest {

    static final byte[] KEY = "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.US_ASCII);
    static final byte[] OTHER = "fedcba9876543210fedcba9876543210".getBytes(StandardCharsets.US_ASCII);

    @Test
    @DisplayName("format: eac_<22>.<11>, 38 characters, base64url, fits Telegram's 64-byte callback_data with ea:")
    void format() {
        String token = ActionToken.mint(KEY);
        assertThat(token).matches("^eac_[A-Za-z0-9_-]{22}\\.[A-Za-z0-9_-]{11}$");
        assertThat(token).hasSize(ActionToken.LENGTH).hasSize(38);
        assertThat(("ea:" + token).getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(64);
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            assertThat(seen.add(ActionToken.mint(KEY))).isTrue();
        }
    }

    @Test
    @DisplayName("a minted token verifies; the same random bytes always give the same token")
    void verifies() {
        byte[] random = new byte[16];
        random[0] = 7;
        String token = ActionToken.format(KEY, random);
        assertThat(ActionToken.verify(KEY, token)).isTrue();
        assertThat(ActionToken.format(KEY, random)).isEqualTo(token);
        assertThat(ActionToken.verify(KEY, ActionToken.mint(KEY))).isTrue();
    }

    @Test
    @DisplayName("the MAC rejects tampering, another key, truncation, padding and garbage before any lookup")
    void rejectsTampering() {
        String token = ActionToken.mint(KEY);
        assertThat(ActionToken.verify(OTHER, token)).isFalse();
        for (int i = ActionToken.PREFIX.length(); i < token.length(); i++) {
            if (token.charAt(i) == '.') {
                continue;
            }
            char replacement = token.charAt(i) == 'A' ? 'B' : 'A';
            String tampered = token.substring(0, i) + replacement + token.substring(i + 1);
            assertThat(ActionToken.verify(KEY, tampered)).as("position " + i).isFalse();
        }
        assertThat(ActionToken.verify(KEY, token.substring(0, token.length() - 1))).isFalse();
        assertThat(ActionToken.verify(KEY, token + "=")).isFalse();
        assertThat(ActionToken.verify(KEY, token.replace("eac_", "eak_"))).isFalse();
        assertThat(ActionToken.verify(KEY, token.replace('.', '-'))).isFalse();
        assertThat(ActionToken.verify(KEY, null)).isFalse();
        assertThat(ActionToken.verify(KEY, "")).isFalse();
        assertThat(ActionToken.verify(KEY, "eac_" + "!".repeat(22) + "." + "!".repeat(11))).isFalse();
    }

    @Test
    @DisplayName("non-canonical base64 spellings of a valid token are refused, so a capability has one spelling")
    void canonical() {
        byte[] random = new byte[16];
        String token = ActionToken.format(KEY, random);
        // The last character of a 16-byte value carries 2 spare bits; flipping them keeps the bytes.
        char last = token.charAt(ActionToken.PREFIX.length() + 21);
        char alias = (char) (last + 1);
        String aliased = token.substring(0, ActionToken.PREFIX.length() + 21) + alias
                + token.substring(ActionToken.PREFIX.length() + 22);
        assertThat(ActionToken.verify(KEY, aliased)).isFalse();
    }

    @Test
    @DisplayName("the stored hash is the hex SHA-256 of the token, never the token")
    void hash() throws Exception {
        String token = ActionToken.mint(KEY);
        String hash = ActionToken.hash(token);
        assertThat(hash).hasSize(64).matches("^[0-9a-f]+$").doesNotContain(token);
        byte[] expected = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
        assertThat(hash).isEqualTo(java.util.HexFormat.of().formatHex(expected));
    }

    @Test
    @DisplayName("the MAC comparison is constant time (MessageDigest.isEqual over equal-length arrays)")
    void constantTime() throws Exception {
        // The comparison delegates to MessageDigest.isEqual, which the JDK documents as constant time
        // for equal lengths; the source is checked so a refactor to Arrays.equals or String.equals fails.
        String source = java.nio.file.Files.readString(java.nio.file.Path.of(
                "src/main/java/ai/foresignal/notify/action/ActionToken.java"));
        assertThat(source).contains("MessageDigest.isEqual(expected, parts.get()[1])");
        assertThat(source).doesNotContain("Arrays.equals(expected");
    }

    @Test
    @DisplayName("tokens are redacted in paths")
    void redaction() {
        String token = ActionToken.mint(KEY);
        assertThat(ActionPaths.redact("/v1/actions/" + token)).isEqualTo("/v1/actions/eac_…");
        assertThat(ActionPaths.redact("GET /v1/actions/" + token + "?x=1 200")).isEqualTo("GET /v1/actions/eac_…?x=1 200");
        assertThat(ActionPaths.redact("/v1/alerts/1")).isEqualTo("/v1/alerts/1");
        assertThat(ActionPaths.redact(null)).isNull();
    }
}
