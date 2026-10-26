package ai.foresignal.security.user;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Issues and consumes account activation and password reset tokens (prompt R2-002, invariant RBAC-5).
 *
 * <p>A token is {@value #ACTIVATION_PREFIX} (activation) or {@value #RESET_PREFIX} (reset) followed by 32 random
 * bytes, base64url, unpadded: the same shape and entropy as an API token ({@code ai.foresignal.security.TokenHasher}),
 * with a prefix per kind so a leaked string says what it is. Only its SHA-256 hex and first
 * {@value UserTokenEntity#PREFIX_LENGTH} characters are stored; the plaintext is returned once.
 *
 * <p><strong>Single-use and single-active.</strong> Consuming sets {@code consumed_at}, so a replay fails. Issuing a
 * token deletes every unconsumed token of the same kind for the user, so an earlier link stops working the moment a
 * new one is requested.
 *
 * <p><strong>No enumeration.</strong> The lookup is by hash (an index probe on a value the caller cannot choose),
 * followed by a constant-time comparison; every failure (unknown, other kind, expired, used) is the same
 * {@code 400 USER_TOKEN_INVALID}. Callers on the request side (R2-003's reset request) must answer identically whether
 * or not an email matched: {@link #requestReset} returns an empty optional for both "no such user" and "user may not
 * reset", and the caller sends nothing then, but responds the same way.
 */
@Service
public class UserTokenService {

    /** The prefix of an activation token. */
    public static final String ACTIVATION_PREFIX = "fsv_";

    /** The prefix of a password reset token. */
    public static final String RESET_PREFIX = "fsw_";

    /** How long an activation link works by default: 72 hours. */
    public static final Duration ACTIVATION_TTL = Duration.ofHours(72);

    /** How long a reset link works by default: 1 hour. */
    public static final Duration RESET_TTL = Duration.ofHours(1);

    static final int ENTROPY_BYTES = 32;

    private final UserTokenRepository tokens;
    private final UserRepository users;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    /**
     * Creates the service.
     *
     * @param tokens the token store
     * @param users the user store
     * @param clock the clock
     */
    public UserTokenService(UserTokenRepository tokens, UserRepository users, Clock clock) {
        this.tokens = tokens;
        this.users = users;
        this.clock = clock;
    }

    /**
     * Issues an activation token for an invited user, replacing any earlier unconsumed one.
     *
     * @param user the user
     * @param createdBy the admin issuing it, or null
     * @param ttl how long it works; null for {@link #ACTIVATION_TTL}
     * @return the plaintext (once) and the stored row
     */
    @Transactional
    public IssuedUserToken issueActivation(UserEntity user, UUID createdBy, Duration ttl) {
        return issue(user, UserTokenKind.ACTIVATION, createdBy, ttl == null ? ACTIVATION_TTL : ttl);
    }

    /**
     * Issues a reset token, replacing any earlier unconsumed one.
     *
     * @param user the user
     * @param ttl how long it works; null for {@link #RESET_TTL}
     * @return the plaintext (once) and the stored row
     */
    @Transactional
    public IssuedUserToken issueReset(UserEntity user, Duration ttl) {
        return issue(user, UserTokenKind.RESET, null, ttl == null ? RESET_TTL : ttl);
    }

    /**
     * The request side of a password reset, safe against enumeration: a token is issued only for an active user with
     * that email; every other case (no such user, invited, suspended, disabled) returns empty, and the caller must
     * respond identically either way.
     *
     * @param email the email as entered
     * @return the issued token for an active user, otherwise empty
     */
    @Transactional
    public Optional<IssuedUserToken> requestReset(String email) {
        return users.findByEmail(UserEntity.normalizeEmail(email)).filter(UserLifecycleService::canAuthenticate)
                .map(user -> issueReset(user, null));
    }

    /**
     * Consumes a token: marks it used and returns its user.
     *
     * @param plaintext the presented token
     * @param kind the kind the caller expects
     * @return the user the token acts on
     * @throws ValidationException {@code 400 USER_TOKEN_INVALID} when the token is unknown, of the other kind,
     *     expired or already consumed (deliberately not saying which)
     */
    @Transactional
    public UserEntity consume(String plaintext, UserTokenKind kind) {
        Instant now = clock.instant();
        if (plaintext == null || plaintext.isEmpty()) {
            throw invalid();
        }
        String hash = sha256(plaintext);
        UserTokenEntity token = tokens.findByTokenHash(hash).orElse(null);
        boolean hashMatches = token != null && MessageDigest.isEqual(hash.getBytes(StandardCharsets.UTF_8),
                token.getTokenHash().getBytes(StandardCharsets.UTF_8));
        if (!hashMatches || token.getKind() != kind || !token.isUsable(now)) {
            throw invalid();
        }
        token.consume(now);
        tokens.save(token);
        return users.findById(token.getUserId()).orElseThrow(UserTokenService::invalid);
    }

    /**
     * The lowercase hex SHA-256 of a token, which is what is stored.
     *
     * @param plaintext the token
     * @return 64 hex characters
     */
    public static String sha256(String plaintext) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(plaintext.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private IssuedUserToken issue(UserEntity user, UserTokenKind kind, UUID createdBy, Duration ttl) {
        if (ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("token lifetime must be positive");
        }
        List<UserTokenEntity> earlier = tokens.findByUserIdAndKind(user.getId(), kind.wireValue());
        tokens.deleteAll(earlier.stream().filter(t -> t.getConsumedAt() == null).toList());
        tokens.flush();
        byte[] entropy = new byte[ENTROPY_BYTES];
        random.nextBytes(entropy);
        String plaintext = (kind == UserTokenKind.ACTIVATION ? ACTIVATION_PREFIX : RESET_PREFIX)
                + Base64.getUrlEncoder().withoutPadding().encodeToString(entropy);
        Instant now = clock.instant();
        UserTokenEntity row = tokens.save(new UserTokenEntity(UUID.randomUUID(), user.getId(), kind,
                sha256(plaintext), plaintext.substring(0, UserTokenEntity.PREFIX_LENGTH), createdBy, now,
                now.plus(ttl)));
        return new IssuedUserToken(plaintext, row);
    }

    private static ValidationException invalid() {
        return new ValidationException(ErrorCodes.USER_TOKEN_INVALID,
                "The link is not valid. It may have expired or been used already.", List.of());
    }
}
