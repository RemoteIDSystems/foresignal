package ai.elaro.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenHasherTest {

    private final TokenHasher hasher = new TokenHasher();

    @Test
    @DisplayName("a generated token is elr_ plus unpadded base64url of 32 bytes")
    void generatesWellFormedToken() {
        String token = hasher.generateToken();

        assertThat(token).startsWith(TokenHasher.TOKEN_PREFIX);
        String body = token.substring(TokenHasher.TOKEN_PREFIX.length());
        // 32 bytes base64url, unpadded, is 43 characters.
        assertThat(body).hasSize(43).doesNotContain("=").matches("[A-Za-z0-9_-]+");
    }

    @Test
    void generatesDistinctTokens() {
        Set<String> tokens = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            tokens.add(hasher.generateToken());
        }

        assertThat(tokens).hasSize(200);
    }

    @Test
    @DisplayName("the hash is 64 lowercase hex characters")
    void hashesToHex() {
        assertThat(hasher.hash("elr_example")).hasSize(64).matches("[0-9a-f]{64}");
    }

    @Test
    void hashIsStable() {
        assertThat(hasher.hash("elr_example")).isEqualTo(hasher.hash("elr_example"));
    }

    @Test
    void differentTokensHashDifferently() {
        assertThat(hasher.hash("elr_a")).isNotEqualTo(hasher.hash("elr_b"));
    }

    @Test
    @DisplayName("the hash is the well-known SHA-256 of the input")
    void matchesKnownSha256() {
        // SHA-256("abc"), so a regression in the algorithm choice is caught.
        assertThat(hasher.hash("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @Test
    void matchesTheCorrectToken() {
        String token = hasher.generateToken();

        assertThat(hasher.matches(token, hasher.hash(token))).isTrue();
    }

    @Test
    void rejectsTheWrongToken() {
        assertThat(hasher.matches("elr_wrong", hasher.hash("elr_right"))).isFalse();
    }

    @Test
    @DisplayName("a token differing in one character is rejected")
    void rejectsNearMiss() {
        String token = hasher.generateToken();
        String almost = token.substring(0, token.length() - 1) + (token.endsWith("A") ? "B" : "A");

        assertThat(hasher.matches(almost, hasher.hash(token))).isFalse();
    }

    @Test
    void rejectsNulls() {
        assertThat(hasher.matches(null, hasher.hash("elr_x"))).isFalse();
        assertThat(hasher.matches("elr_x", null)).isFalse();
        assertThat(hasher.matches(null, null)).isFalse();
    }

    @Test
    void rejectsEmptyToken() {
        assertThat(hasher.matches("", hasher.hash("elr_x"))).isFalse();
    }
}
