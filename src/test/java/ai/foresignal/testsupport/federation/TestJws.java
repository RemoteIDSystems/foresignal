package ai.foresignal.testsupport.federation;

import ai.foresignal.provenance.Jwk;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Ed25519 keys and detached JWS (RFC 7515 Appendix F, RFC 7797 {@code b64:false}) for federation tests (prompt 113):
 * a remote instance's or an edge's signature, built exactly as 109's {@code JwsSigner} builds one.
 */
public final class TestJws {

    private static final Base64.Encoder URL = Base64.getUrlEncoder().withoutPadding();

    private TestJws() {}

    /** A fresh Ed25519 key pair. */
    public static KeyPair keyPair() {
        try {
            return KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** The raw 32-byte point of an Ed25519 public key. */
    public static byte[] raw(PublicKey key) {
        byte[] encoded = key.getEncoded();
        return Arrays.copyOfRange(encoded, encoded.length - 32, encoded.length);
    }

    /** The RFC 7638 thumbprint ({@code kid}) of an Ed25519 public key. */
    public static String kid(PublicKey key) {
        return Jwk.thumbprint(raw(key));
    }

    /** The base64url raw point, as the {@code Foresignal-Key} header carries it. */
    public static String keyHeader(PublicKey key) {
        return URL.encodeToString(raw(key));
    }

    /** A JWKS document holding {@code key}. */
    public static Map<String, Object> jwks(PublicKey key) {
        return Map.of("keys", List.of(Jwk.of(kid(key), raw(key))));
    }

    /** A detached JWS over {@code payload}, signed by {@code pair}. */
    public static String sign(byte[] payload, KeyPair pair) {
        return sign(payload, pair.getPrivate(), kid(pair.getPublic()));
    }

    /** A detached JWS over {@code payload} with {@code kid} in the header. */
    public static String sign(byte[] payload, PrivateKey key, String kid) {
        String header = "{\"alg\":\"Ed25519\",\"kid\":\"" + kid + "\",\"typ\":\"application/json\",\"b64\":false,"
                + "\"crit\":[\"b64\"]}";
        String headerB64 = URL.encodeToString(header.getBytes(StandardCharsets.US_ASCII));
        byte[] prefix = (headerB64 + ".").getBytes(StandardCharsets.US_ASCII);
        byte[] input = new byte[prefix.length + payload.length];
        System.arraycopy(prefix, 0, input, 0, prefix.length);
        System.arraycopy(payload, 0, input, prefix.length, payload.length);
        try {
            Signature signature = Signature.getInstance("Ed25519");
            signature.initSign(key);
            signature.update(input);
            return headerB64 + ".." + URL.encodeToString(signature.sign());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
