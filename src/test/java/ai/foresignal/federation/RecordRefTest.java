package ai.foresignal.federation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link RecordRef} (prompt 113): the one place that formats and parses {@code urn:foresignal:…} references, with
 * RFC 3986 percent-encoding of the external id, and the local external id of a federated record (hashed above 256
 * characters).
 */
class RecordRefTest {

    @Test
    @DisplayName("format and parse round trip")
    void roundTrip() {
        RecordRef ref = new RecordRef("6f1c2a", "usgs-earthquakes", "us7000n7tc");
        assertThat(ref.format()).isEqualTo("urn:foresignal:6f1c2a:usgs-earthquakes:us7000n7tc");
        assertThat(ref).hasToString("urn:foresignal:6f1c2a:usgs-earthquakes:us7000n7tc");
        assertThat(RecordRef.parse(ref.format())).isEqualTo(ref);
        assertThat(RecordRef.format("6f1c2a", "usgs-earthquakes", "us7000n7tc")).isEqualTo(ref.format());
    }

    @Test
    @DisplayName("the external id is percent-encoded outside RFC 3986's unreserved set, and decodes back")
    void encoding() {
        String external = "a b/c:d%é?#";
        RecordRef ref = new RecordRef("inst", "src", external);
        assertThat(ref.format()).isEqualTo("urn:foresignal:inst:src:a%20b%2Fc%3Ad%25%C3%A9%3F%23");
        assertThat(RecordRef.parse(ref.format()).externalId()).isEqualTo(external);
        assertThat(RecordRef.encode("Az09-._~")).isEqualTo("Az09-._~");
        assertThat(RecordRef.decode("%c3%a9")).isEqualTo("é");
        String emoji = "😀";
        assertThat(RecordRef.decode(RecordRef.encode(emoji))).isEqualTo(emoji);
        // A colon in the external id never confuses the parser: only the first two separate the parts.
        assertThat(RecordRef.parse("urn:foresignal:i:s:x%3Ay").externalId()).isEqualTo("x:y");
    }

    @Test
    @DisplayName("malformed references and parts are refused")
    void malformed() {
        assertThatThrownBy(() -> RecordRef.parse(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RecordRef.parse("urn:other:i:s:e")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RecordRef.parse("urn:foresignal:i:s:")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RecordRef.parse("urn:foresignal:i::e")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RecordRef.parse("urn:foresignal:only")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RecordRef.decode("%zz")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RecordRef.decode("%4")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RecordRef.decode("%C3")).as("not UTF-8").isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RecordRef("a:b", "s", "e")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RecordRef("a", "s:t", "e")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RecordRef(" ", "s", "e")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("the local external id is {source}/{external}, hashed with sha256: above 256 characters")
    void localExternalId() {
        assertThat(RecordRef.localExternalId("usgs-earthquakes", "us7000n7tc"))
                .isEqualTo("usgs-earthquakes/us7000n7tc");
        String fits = "x".repeat(256 - "src/".length());
        assertThat(RecordRef.localExternalId("src", fits)).isEqualTo("src/" + fits).hasSize(256);
        String tooLong = "x".repeat(300);
        String hashed = RecordRef.localExternalId("src", tooLong);
        assertThat(hashed).startsWith(RecordRef.HASH_PREFIX).hasSize(RecordRef.HASH_PREFIX.length() + 64)
                .matches("sha256:[0-9a-f]{64}");
        assertThat(RecordRef.localExternalId("src", tooLong)).as("deterministic").isEqualTo(hashed);
        assertThat(RecordRef.localExternalId("src", tooLong + "y")).isNotEqualTo(hashed);
        assertThat(RecordRef.hashed(hashed)).isTrue();
        assertThat(RecordRef.hashed("src/a")).isFalse();
        assertThat(hashed.substring(7)).isEqualTo(RecordRef.sha256Hex("src/" + tooLong));
    }

    @Test
    @DisplayName("the compact message id is the first 8 bytes of the SHA-256 of the formatted reference")
    void messageId() {
        RecordRef ref = new RecordRef("inst", "alert", "42");
        byte[] id = ref.messageId();
        assertThat(id).hasSize(8);
        assertThat(id).isEqualTo(java.util.Arrays.copyOf(RecordRef.sha256(ref.format()), 8));
        assertThat(RecordRef.messageId(ref.format())).isEqualTo(id);
        assertThat(new RecordRef("inst", "alert", "43").messageId()).isNotEqualTo(id);
        assertThat(ref.format().getBytes(StandardCharsets.US_ASCII)).isNotEmpty();
    }
}
