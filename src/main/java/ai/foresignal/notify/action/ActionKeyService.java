package ai.foresignal.notify.action;

import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * The HMAC key of action capabilities (prompt 033): 32 random bytes stored in {@code system_secret}
 * under {@value #KEY}, encrypted with {@link CredentialCipher} ({@code nonce ‖ ciphertext}, as the
 * VAPID key of 031). Generated on first use; two instances starting together agree because the
 * insert is {@code on conflict do nothing} and both read back what won. Cached after the first read.
 */
@Service
public class ActionKeyService {

    /** The {@code system_secret} key. */
    public static final String KEY = "action.hmac";

    static final int KEY_BYTES = 32;

    private static final int NONCE_LENGTH = 12;

    private final NamedParameterJdbcTemplate jdbc;
    private final CredentialCipher cipher;
    private final TransactionTemplate tx;
    private volatile byte[] key;

    /** Creates the service. */
    public ActionKeyService(NamedParameterJdbcTemplate jdbc, CredentialCipher cipher,
            PlatformTransactionManager transactions) {
        this.jdbc = jdbc;
        this.cipher = cipher;
        this.tx = new TransactionTemplate(transactions);
    }

    /** The key (a copy), generated and stored on first use. */
    public byte[] key() {
        byte[] current = key;
        if (current == null) {
            synchronized (this) {
                if (key == null) {
                    key = tx.execute(status -> loadOrCreate());
                }
                current = key;
            }
        }
        return current.clone();
    }

    /** Forgets the cached key (tests, after a table truncation). */
    public synchronized void forget() {
        key = null;
    }

    private byte[] loadOrCreate() {
        byte[] stored = load();
        if (stored != null) {
            return stored;
        }
        byte[] fresh = new byte[KEY_BYTES];
        new SecureRandom().nextBytes(fresh);
        Encrypted encrypted = cipher.encrypt(fresh);
        byte[] value = new byte[NONCE_LENGTH + encrypted.ciphertext().length];
        System.arraycopy(encrypted.nonce(), 0, value, 0, NONCE_LENGTH);
        System.arraycopy(encrypted.ciphertext(), 0, value, NONCE_LENGTH, encrypted.ciphertext().length);
        jdbc.update("insert into system_secret (key, value) values (:key, :value) on conflict (key) do nothing",
                new MapSqlParameterSource("key", KEY).addValue("value", value));
        byte[] out = load();
        if (out == null) {
            throw new IllegalStateException("The action HMAC key could not be stored");
        }
        return out;
    }

    private byte[] load() {
        List<byte[]> rows = jdbc.query("select value from system_secret where key = :key",
                new MapSqlParameterSource("key", KEY), (rs, n) -> rs.getBytes(1));
        if (rows.isEmpty() || rows.getFirst() == null || rows.getFirst().length <= NONCE_LENGTH) {
            return null;
        }
        byte[] value = rows.getFirst();
        byte[] nonce = Arrays.copyOfRange(value, 0, NONCE_LENGTH);
        byte[] ciphertext = Arrays.copyOfRange(value, NONCE_LENGTH, value.length);
        return cipher.decrypt(new Encrypted(ciphertext, nonce));
    }
}
