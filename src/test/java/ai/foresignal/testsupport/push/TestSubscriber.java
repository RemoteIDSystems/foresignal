package ai.foresignal.testsupport.push;

import ai.foresignal.notify.push.EcKeys;
import ai.foresignal.notify.push.WebPushCrypto;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * A browser stand-in (prompt 031): a P-256 key pair and auth secret generated at test time, the
 * {@code PushSubscriptionInput} JSON for an endpoint, and decryption of what Foresignal pushed.
 */
public final class TestSubscriber {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final KeyPair keys = EcKeys.generate();
    private final byte[] auth = new byte[16];
    private final String endpoint;

    /** A subscriber with fresh keys at {@code endpoint}. */
    public TestSubscriber(String endpoint) {
        new SecureRandom().nextBytes(auth);
        this.endpoint = endpoint;
    }

    /** The endpoint. */
    public String endpoint() {
        return endpoint;
    }

    /** {@code p256dh}, base64url. */
    public String p256dh() {
        return EcKeys.b64(EcKeys.encodePoint((ECPublicKey) keys.getPublic()));
    }

    /** {@code auth}, base64url. */
    public String auth() {
        return EcKeys.b64(auth);
    }

    /** A {@code PushSubscriptionInput} body with {@code extra} JSON members (or empty). */
    public String body(String extra) {
        return "{\"name\":\"Test\",\"subscription\":{\"endpoint\":\"" + endpoint + "\",\"keys\":{\"p256dh\":\"" + p256dh()
                + "\",\"auth\":\"" + auth() + "\"}}" + (extra == null || extra.isBlank() ? "" : "," + extra) + "}";
    }

    /** Decrypts an {@code aes128gcm} push body to its JSON. */
    public JsonNode decrypt(byte[] body) {
        byte[] plain = WebPushCrypto.decrypt(body, (ECPrivateKey) keys.getPrivate(),
                EcKeys.encodePoint((ECPublicKey) keys.getPublic()), auth);
        return JSON.readTree(new String(plain, StandardCharsets.UTF_8));
    }
}
