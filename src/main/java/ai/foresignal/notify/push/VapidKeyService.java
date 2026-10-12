package ai.foresignal.notify.push;

import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Foresignal's own VAPID key pair (prompt 031, RFC 8292), so no third-party push account is needed.
 *
 * <p>On first use a P-256 key pair is generated and stored in {@code system_secret}:
 * {@value #PUBLIC_KEY} holds the uncompressed public point as base64url text (87 characters) and
 * {@value #PRIVATE_KEY} the PKCS#8 private key encrypted with {@link CredentialCipher}
 * ({@code nonce ‖ ciphertext}). Two instances starting together cannot store different pairs: the
 * inserts use {@code on conflict do nothing} and both read back what won. The pair is cached after
 * the first read. The public key is what {@code GET /v1/push/vapid-public-key} serves, what a
 * browser passes as {@code applicationServerKey} and what a Flutter app passes to UnifiedPush
 * {@code registerApp(vapid: …)}.
 */
@Service
public class VapidKeyService {

    /** The {@code system_secret} key of the public point. */
    public static final String PUBLIC_KEY = "push.vapid.public";

    /** The {@code system_secret} key of the encrypted private key. */
    public static final String PRIVATE_KEY = "push.vapid.private";

    private static final int NONCE_LENGTH = 12;

    /**
     * The loaded pair.
     *
     * @param publicKey the public key
     * @param privateKey the private key
     * @param publicB64 the public point, base64url
     */
    public record Keys(ECPublicKey publicKey, ECPrivateKey privateKey, String publicB64) {}

    private final NamedParameterJdbcTemplate jdbc;
    private final CredentialCipher cipher;
    private final TransactionTemplate tx;
    private volatile Keys keys;

    /** Creates the service. */
    public VapidKeyService(NamedParameterJdbcTemplate jdbc, CredentialCipher cipher,
            PlatformTransactionManager transactions) {
        this.jdbc = jdbc;
        this.cipher = cipher;
        this.tx = new TransactionTemplate(transactions);
    }

    /** The key pair, generated and stored on first use. */
    public Keys keys() {
        Keys current = keys;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            if (keys == null) {
                keys = tx.execute(status -> loadOrCreate());
            }
            return keys;
        }
    }

    /** The public key as base64url (87 characters). */
    public String publicKey() {
        return keys().publicB64();
    }

    /** Forgets the cached pair, so the next use reads the stored one again (tests). */
    public synchronized void forget() {
        keys = null;
    }

    private Keys loadOrCreate() {
        Keys stored = load();
        if (stored != null) {
            return stored;
        }
        KeyPair pair = EcKeys.generate();
        String publicB64 = EcKeys.b64(EcKeys.encodePoint((ECPublicKey) pair.getPublic()));
        Encrypted encrypted = cipher.encrypt(pair.getPrivate().getEncoded());
        byte[] value = new byte[NONCE_LENGTH + encrypted.ciphertext().length];
        System.arraycopy(encrypted.nonce(), 0, value, 0, NONCE_LENGTH);
        System.arraycopy(encrypted.ciphertext(), 0, value, NONCE_LENGTH, encrypted.ciphertext().length);
        boolean partial = read(PUBLIC_KEY) != null || read(PRIVATE_KEY) != null;
        if (partial) {
            // One half without the other cannot be used: replace both.
            jdbc.update("delete from system_secret where key in (:keys)",
                    new MapSqlParameterSource("keys", List.of(PUBLIC_KEY, PRIVATE_KEY)));
        }
        insert(PUBLIC_KEY, publicB64.getBytes(StandardCharsets.US_ASCII));
        insert(PRIVATE_KEY, value);
        Keys out = load();
        if (out == null) {
            throw new IllegalStateException("The VAPID key pair could not be stored");
        }
        return out;
    }

    private Keys load() {
        byte[] publicValue = read(PUBLIC_KEY);
        byte[] privateValue = read(PRIVATE_KEY);
        if (publicValue == null || privateValue == null || privateValue.length <= NONCE_LENGTH) {
            return null;
        }
        String publicB64 = new String(publicValue, StandardCharsets.US_ASCII);
        byte[] nonce = Arrays.copyOfRange(privateValue, 0, NONCE_LENGTH);
        byte[] ciphertext = Arrays.copyOfRange(privateValue, NONCE_LENGTH, privateValue.length);
        ECPrivateKey privateKey = EcKeys.privateFromPkcs8(cipher.decrypt(new Encrypted(ciphertext, nonce)));
        return new Keys(EcKeys.decodePoint(EcKeys.unb64(publicB64)), privateKey, publicB64);
    }

    private byte[] read(String key) {
        List<byte[]> rows = jdbc.query("select value from system_secret where key = :key",
                new MapSqlParameterSource("key", key), (rs, n) -> rs.getBytes(1));
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private void insert(String key, byte[] value) {
        jdbc.update("insert into system_secret (key, value) values (:key, :value) on conflict (key) do nothing",
                new MapSqlParameterSource("key", key).addValue("value", value));
    }
}
