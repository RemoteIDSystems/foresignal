package ai.foresignal.security;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.store.SystemSecretEntity;
import ai.foresignal.store.SystemSecretRepository;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The bootstrap admin token (Architecture §7.2, FR-30).
 *
 * <p>Generated on first start, shown exactly once in the log, and stored only as a SHA-256 hash.
 * There is no way to recover it afterwards; an operator who loses it restarts with
 * {@code FORESIGNAL_RESET_ADMIN_TOKEN=true} to get a new one.
 *
 * <p>Runs as an {@link InitializingBean} so the token exists before the web server starts
 * accepting requests, which avoids a window where writes would be unauthenticated.
 *
 * <p>Prompt 023 moves this token into the {@code api_token} table as the {@code bootstrap} row;
 * {@link TokenHasher} already holds the reusable part.
 */
@Service
public class AdminTokenService implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(AdminTokenService.class);

    /** Key in {@code system_secret}. */
    public static final String ADMIN_TOKEN_HASH_KEY = "admin_token_hash";

    private final SystemSecretRepository secrets;
    private final TokenHasher hasher;
    private final boolean resetRequested;

    /** The stored hash, cached so verifying a request does not hit the database. */
    private volatile String tokenHash;

    public AdminTokenService(
            SystemSecretRepository secrets, TokenHasher hasher, ForesignalProperties properties) {
        this.secrets = secrets;
        this.hasher = hasher;
        this.resetRequested = properties.resetAdminToken();
    }

    @Override
    public void afterPropertiesSet() {
        ensureToken();
    }

    /** Generates and announces a token if there is none, or if a reset was requested. */
    @Transactional
    public void ensureToken() {
        Optional<SystemSecretEntity> stored = secrets.findById(ADMIN_TOKEN_HASH_KEY);
        if (stored.isPresent() && !resetRequested) {
            tokenHash = new String(stored.get().getValue(), StandardCharsets.UTF_8);
            log.info("Using the existing admin token. Set FORESIGNAL_RESET_ADMIN_TOKEN=true to replace it.");
            return;
        }

        String reason = stored.isPresent() ? "FORESIGNAL_RESET_ADMIN_TOKEN=true" : "first start";
        String token = storeNewToken();
        announce(token, reason);
    }

    /**
     * Replaces the token and returns the new one, which the caller must show the operator exactly
     * once. The previous token stops working immediately.
     *
     * <p>Exposed by {@code POST /v1/system/admin-token/rotate} (prompt 019).
     */
    @Transactional
    public String rotate() {
        String token = storeNewToken();
        log.info("Admin token rotated. The previous token is no longer valid.");
        return token;
    }

    /** True when {@code presentedToken} is the current admin token. */
    public boolean verify(String presentedToken) {
        String expected = tokenHash;
        if (expected == null || presentedToken == null || presentedToken.isBlank()) {
            return false;
        }
        return hasher.matches(presentedToken, expected);
    }

    /** True once a token exists. False only before {@link #ensureToken()} has run. */
    public boolean isInitialized() {
        return tokenHash != null;
    }

    private String storeNewToken() {
        String token = hasher.generateToken();
        String hash = hasher.hash(token);
        secrets.save(
                new SystemSecretEntity(ADMIN_TOKEN_HASH_KEY, hash.getBytes(StandardCharsets.UTF_8)));
        tokenHash = hash;
        return token;
    }

    /**
     * Prints the token once, in a block that is hard to miss and hard to confuse with ordinary log
     * output. This is the only time the plaintext token is ever emitted.
     */
    private void announce(String token, String reason) {
        String line = "=".repeat(78);
        log.warn(
                """

                {}
                  FORESIGNAL ADMIN TOKEN - shown once, store it securely
                  Generated because: {}

                    {}

                  Use it as:  Authorization: Bearer <token>
                  Only its SHA-256 hash is stored, so this cannot be shown again.
                  Lost it? Restart with FORESIGNAL_RESET_ADMIN_TOKEN=true for a new one.
                {}
                """,
                line,
                reason,
                token,
                line);
    }
}
