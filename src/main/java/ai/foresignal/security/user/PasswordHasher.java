package ai.foresignal.security.user;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Hashes and verifies passwords with argon2id (prompt R2-002, invariant RBAC-5).
 *
 * <p>The algorithm is Bouncy Castle's argon2 through Spring Security Crypto's {@link Argon2PasswordEncoder}: a
 * published algorithm from audited libraries, nothing hand-written. The encoded form is the standard PHC string
 * ({@code $argon2id$v=19$m=…,t=…,p=…$salt$hash}), so the parameters travel with each hash and can be raised later
 * without invalidating stored hashes. The parameters live only here.
 *
 * <p>Raw passwords are never logged, echoed or stored; {@code user.password_hash} holds only the encoded hash.
 */
@Component
public class PasswordHasher {

    /** Random salt per hash, in bytes. */
    public static final int SALT_LENGTH = 16;

    /** Output hash length, in bytes. */
    public static final int HASH_LENGTH = 32;

    /** Lanes. One keeps a login to one core. */
    public static final int PARALLELISM = 1;

    /** Memory cost in KiB: 16 MiB per hash in flight. */
    public static final int MEMORY_KIB = 1 << 14;

    /** Passes over memory. */
    public static final int ITERATIONS = 3;

    /** The PHC prefix of every hash this class writes. */
    public static final String ENCODED_PREFIX = "$argon2id$";

    private final Argon2PasswordEncoder encoder =
            new Argon2PasswordEncoder(SALT_LENGTH, HASH_LENGTH, PARALLELISM, MEMORY_KIB, ITERATIONS);

    /**
     * The argon2id hash of a password, with a fresh random salt.
     *
     * @param raw the password; never logged
     * @return the encoded PHC string
     */
    public String hash(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("password is null");
        }
        return encoder.encode(raw);
    }

    /**
     * True when a password matches an encoded hash. False for a null on either side, or a hash that is not an argon2
     * PHC string, so an account without a password never matches.
     *
     * @param raw the presented password
     * @param encoded the stored hash, or null
     * @return true on a match
     */
    public boolean matches(String raw, String encoded) {
        if (raw == null || encoded == null || !encoded.startsWith(ENCODED_PREFIX)) {
            return false;
        }
        return encoder.matches(raw, encoded);
    }
}
