package ai.foresignal.inbound.signature;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.inbound.format.IngestRequest;
import ai.foresignal.notify.HmacSigner;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/** The webhook signature verifiers (prompt 024). */
class SignatureVerifierTest {

    private static final Instant NOW = Instant.parse("2026-10-12T08:00:00Z");
    private static final byte[] BODY = "{\"hello\":\"world\"}".getBytes(StandardCharsets.UTF_8);
    private static final String SECRET = "s3cret";

    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final SignatureVerifiers verifiers = new SignatureVerifiers(List.of(new NoneVerifier(),
            new HmacSha256Verifier(), new StandardWebhooksVerifier(clock), new ForesignalSignatureVerifier(clock)));

    private static IngestRequest request(HttpHeaders headers) {
        return new IngestRequest(BODY, MediaType.APPLICATION_JSON, headers, Map.of(), NOW);
    }

    private static byte[] hmac(byte[] key, String message) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("hmac-sha256: valid, wrong, missing, and a custom header")
    void hmacSha256() throws Exception {
        String good = "sha256=" + HexFormat.of().formatHex(hmac(SECRET.getBytes(StandardCharsets.UTF_8), new String(BODY, StandardCharsets.UTF_8)));
        VerifierConfig config = new VerifierConfig("hmac-sha256", null, false);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Signature-256", good);
        assertThat(verifiers.verify(config, SECRET, request(headers))).isEqualTo(Verification.VALID);
        assertThat(verifiers.verify(config, "other", request(headers))).isEqualTo(Verification.INVALID);
        assertThat(verifiers.verify(config, SECRET, request(new HttpHeaders()))).isEqualTo(Verification.MISSING);

        HttpHeaders custom = new HttpHeaders();
        custom.add("X-Hub-Signature-256", good);
        assertThat(verifiers.verify(new VerifierConfig("hmac-sha256", "X-Hub-Signature-256", false), SECRET, request(custom)))
                .isEqualTo(Verification.VALID);
    }

    @Test
    @DisplayName("standard-webhooks: valid once, replayed id refused, expired timestamp and bad signature refused")
    void standardWebhooks() throws Exception {
        byte[] key = "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8);
        String secret = "whsec_" + Base64.getEncoder().encodeToString(key);
        VerifierConfig config = new VerifierConfig("standard-webhooks", null, false);

        HttpHeaders valid = swHeaders("msg_1", NOW.getEpochSecond(), key);
        assertThat(verifiers.verify(config, secret, request(valid))).isEqualTo(Verification.VALID);
        assertThat(verifiers.verify(config, secret, request(valid))).as("replay").isEqualTo(Verification.INVALID);

        HttpHeaders expired = swHeaders("msg_2", NOW.minusSeconds(301).getEpochSecond(), key);
        assertThat(verifiers.verify(config, secret, request(expired))).isEqualTo(Verification.INVALID);

        HttpHeaders wrong = swHeaders("msg_3", NOW.getEpochSecond(), "another-key-entirely-0000000000".getBytes(StandardCharsets.UTF_8));
        assertThat(verifiers.verify(config, secret, request(wrong))).isEqualTo(Verification.INVALID);

        assertThat(verifiers.verify(config, secret, request(new HttpHeaders()))).isEqualTo(Verification.MISSING);
    }

    private static HttpHeaders swHeaders(String id, long timestamp, byte[] key) throws Exception {
        String signature = Base64.getEncoder().encodeToString(
                hmac(key, id + "." + timestamp + "." + new String(BODY, StandardCharsets.UTF_8)));
        HttpHeaders headers = new HttpHeaders();
        headers.add("webhook-id", id);
        headers.add("webhook-timestamp", Long.toString(timestamp));
        headers.add("webhook-signature", "v1,bogus v1," + signature);
        return headers;
    }

    @Test
    @DisplayName("foresignal: accepts exactly what the webhook channel signs; stale or wrong signatures fail")
    void foresignal() {
        VerifierConfig config = new VerifierConfig("foresignal", null, false);
        String ts = Long.toString(NOW.getEpochSecond());

        HttpHeaders valid = new HttpHeaders();
        valid.add("X-Foresignal-Timestamp", ts);
        valid.add(HmacSigner.HEADER, HmacSigner.sign(SECRET, ts, BODY));
        assertThat(verifiers.verify(config, SECRET, request(valid))).isEqualTo(Verification.VALID);
        assertThat(verifiers.verify(config, "nope", request(valid))).isEqualTo(Verification.INVALID);

        String old = Long.toString(NOW.minusSeconds(600).getEpochSecond());
        HttpHeaders stale = new HttpHeaders();
        stale.add("X-Foresignal-Timestamp", old);
        stale.add(HmacSigner.HEADER, HmacSigner.sign(SECRET, old, BODY));
        assertThat(verifiers.verify(config, SECRET, request(stale))).isEqualTo(Verification.INVALID);
        assertThat(verifiers.verify(config, SECRET, request(new HttpHeaders()))).isEqualTo(Verification.MISSING);
    }

    @Test
    @DisplayName("none and a null config are VALID, an unknown scheme is INVALID; active() and schemes()")
    void registry() {
        assertThat(verifiers.verify(null, null, request(new HttpHeaders()))).isEqualTo(Verification.VALID);
        assertThat(verifiers.verify(new VerifierConfig("none", null, true), null, request(new HttpHeaders())))
                .isEqualTo(Verification.VALID);
        assertThat(verifiers.verify(new VerifierConfig("rot13", null, false), SECRET, request(new HttpHeaders())))
                .isEqualTo(Verification.INVALID);
        assertThat(new VerifierConfig("none", null, false).active()).isFalse();
        assertThat(new VerifierConfig("hmac-sha256", null, true).active()).isTrue();
        assertThat(verifiers.schemes()).containsExactly("foresignal", "hmac-sha256", "none", "standard-webhooks");
        assertThat(verifiers.supports("foresignal")).isTrue();
    }
}
