package ai.foresignal.notify;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link OriginMarker} and {@link Redactor} (prompt 018). */
class OriginMarkerTest {

    @Test
    @DisplayName("an instance recognizes its own marker in every form, and not another instance's")
    void isOwn() {
        assertThat(OriginMarker.isOwn("abcd1234", "abcd1234", null, null)).isTrue();
        assertThat(OriginMarker.isOwn("abcd1234", null, "abcd1234", null)).isTrue();
        assertThat(OriginMarker.isOwn("abcd1234", null, null, List.of("x", "foresignal-origin-abcd1234"))).isTrue();
        assertThat(OriginMarker.isOwn("abcd1234", "zzzz9999", "zzzz9999", List.of("foresignal-origin-zzzz9999"))).isFalse();
        assertThat(OriginMarker.isOwn("abcd1234", null, null, null)).isFalse();
    }

    @Test
    @DisplayName("redaction masks every secret, longest first, and cuts to 500 characters")
    void redaction() {
        String url = "https://hooks.example.invalid/services/T0/B0/SECRETPART";
        assertThat(Redactor.redact("POST " + url + " failed", List.of(url, "SECRETPART"))).isEqualTo("POST *** failed");
        assertThat(Redactor.redact("bot123:TOKEN/sendMessage", List.of("123:TOKEN", "ab"))).isEqualTo("bot***/sendMessage");
        assertThat(Redactor.error("x".repeat(900), List.of())).hasSize(500);
        assertThat(Redactor.error(null, List.of("secret"))).isNull();
    }
}
