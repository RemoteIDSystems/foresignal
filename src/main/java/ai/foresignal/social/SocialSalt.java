package ai.foresignal.social;

import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import java.security.SecureRandom;
import java.time.Clock;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Holds the instance social salt (prompt 135): generated on first use (32 random bytes), stored encrypted in
 * {@code social_salt} like other secret material, never rotated, never exported.
 */
@Component
public class SocialSalt {

    private final SocialSaltRepository rows;
    private final CredentialCipher cipher;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();
    private volatile AuthorKeys keys;

    /** Creates the holder. */
    public SocialSalt(SocialSaltRepository rows, CredentialCipher cipher, Clock clock) {
        this.rows = rows;
        this.cipher = cipher;
        this.clock = clock;
    }

    /** The author keyer, generating the salt on first use. */
    @Transactional
    public synchronized AuthorKeys authorKeys() {
        AuthorKeys current = keys;
        if (current != null) {
            return current;
        }
        SocialSaltEntity row = rows.findById(SocialSaltEntity.INSTANCE).orElse(null);
        if (row == null) {
            byte[] value = new byte[32];
            random.nextBytes(value);
            Encrypted e = cipher.encrypt(value);
            row = rows.save(new SocialSaltEntity(e.ciphertext(), e.nonce(), clock.instant()));
        }
        current = new AuthorKeys(cipher.decrypt(new Encrypted(row.getCiphertext(), row.getNonce())));
        keys = current;
        return current;
    }
}
