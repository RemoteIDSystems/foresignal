package ai.foresignal.security.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link PasswordHasher}: argon2id with the documented parameters (prompt R2-002). */
class PasswordHasherTest {

    private final PasswordHasher hasher = new PasswordHasher();

    @Test
    @DisplayName("a hash verifies its password and rejects another")
    void roundTrip() {
        String hash = hasher.hash("correct horse battery staple");
        assertThat(hasher.matches("correct horse battery staple", hash)).isTrue();
        assertThat(hasher.matches("correct horse battery stapler", hash)).isFalse();
    }

    @Test
    @DisplayName("the encoded hash is argon2id with m=16384, t=3, p=1 and never contains the password")
    void encodedForm() {
        String hash = hasher.hash("a long enough password");
        assertThat(hash).startsWith(PasswordHasher.ENCODED_PREFIX).startsWith("$argon2id$v=19$m=16384,t=3,p=1$")
                .doesNotContain("a long enough password");
    }

    @Test
    @DisplayName("each hash gets its own salt")
    void salted() {
        assertThat(hasher.hash("same password here")).isNotEqualTo(hasher.hash("same password here"));
    }

    @Test
    @DisplayName("null passwords and hashes, and non-argon2 hashes, never match")
    void nulls() {
        String hash = hasher.hash("some password 1");
        assertThat(hasher.matches(null, hash)).isFalse();
        assertThat(hasher.matches("some password 1", null)).isFalse();
        assertThat(hasher.matches("some password 1", "$2a$10$abcdefghijklmnopqrstuv")).isFalse();
        assertThatThrownBy(() -> hasher.hash(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
