package ai.foresignal.security;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.store.SystemSecretEntity;
import ai.foresignal.store.SystemSecretRepository;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * The bootstrap admin token (Architecture §7.2 and §15, FR-30): the {@code api_token} row named
 * {@value ApiTokenEntity#BOOTSTRAP_NAME}, scope {@code admin}, {@code created_via = bootstrap},
 * owned by the principal {@code owner}.
 *
 * <p>Generated on first start, shown exactly once in the log, and stored only as a SHA-256 hash.
 * There is no way to recover it afterwards; an operator who loses it restarts with
 * {@code FORESIGNAL_RESET_ADMIN_TOKEN=true} to get a new one. {@link #rotate()} replaces it at run
 * time. What the operator sees is unchanged from prompts 003 and 019.
 *
 * <p><b>Migration (prompt 023):</b> before 023 the hash lived in
 * {@code system_secret['admin_token_hash']}. At startup, if that secret exists and no bootstrap row
 * does, the row is created with that hash (so the operator's token keeps working) and the secret is
 * deleted, in one transaction.
 *
 * <p>Runs as an {@link InitializingBean} so the token exists before the web server starts
 * accepting requests, which avoids a window where writes would be unauthenticated.
 */
@Service
public class AdminTokenService implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(AdminTokenService.class);

    /** The pre-023 key in {@code system_secret}, read once by the migration and then deleted. */
    public static final String ADMIN_TOKEN_HASH_KEY = "admin_token_hash";

    private final ApiTokenRepository tokens;
    private final SystemSecretRepository secrets;
    private final TokenHasher hasher;
    private final PrincipalService principals;
    private final TransactionTemplate tx;
    private final Clock clock;
    private final boolean resetRequested;

    /** Creates the service. */
    public AdminTokenService(ApiTokenRepository tokens, SystemSecretRepository secrets, TokenHasher hasher,
            PrincipalService principals, PlatformTransactionManager transactions, Clock clock,
            ForesignalProperties properties) {
        this.tokens = tokens;
        this.secrets = secrets;
        this.hasher = hasher;
        this.principals = principals;
        this.tx = new TransactionTemplate(transactions);
        this.clock = clock;
        this.resetRequested = properties.resetAdminToken();
    }

    @Override
    public void afterPropertiesSet() {
        ensureToken();
    }

    /**
     * Migrates a legacy hash, then generates and announces a token if there is none, or if a reset
     * was requested.
     */
    public void ensureToken() {
        boolean migrated = Boolean.TRUE.equals(tx.execute(status -> migrateLegacySecret()));
        Optional<ApiTokenEntity> stored = bootstrapRow();
        if (stored.isPresent() && !resetRequested) {
            log.info(migrated
                    ? "Moved the existing admin token into api_token (row 'bootstrap'); it keeps working."
                    : "Using the existing admin token. Set FORESIGNAL_RESET_ADMIN_TOKEN=true to replace it.");
            return;
        }
        String reason = stored.isPresent() ? "FORESIGNAL_RESET_ADMIN_TOKEN=true" : "first start";
        String token = storeNewToken();
        announce(token, reason);
    }

    /**
     * Replaces the token and returns the new one, which the caller must show the operator exactly
     * once. The previous token stops working immediately. Recreates the bootstrap row if it is
     * missing.
     *
     * <p>Exposed by {@code POST /v1/system/admin-token/rotate} (prompt 019).
     */
    public String rotate() {
        String token = storeNewToken();
        log.info("Admin token rotated. The previous token is no longer valid.");
        return token;
    }

    /** True when {@code presentedToken} is the current bootstrap admin token. */
    public boolean verify(String presentedToken) {
        if (presentedToken == null || presentedToken.isBlank()) {
            return false;
        }
        return bootstrapRow()
                .filter(row -> row.isActive(clock.instant()))
                .map(row -> hasher.matches(presentedToken, row.getTokenHash()))
                .orElse(false);
    }

    /** True once a bootstrap token exists. */
    public boolean isInitialized() {
        return bootstrapRow().isPresent();
    }

    /** The bootstrap row's id, if it exists. */
    public Optional<UUID> bootstrapTokenId() {
        return bootstrapRow().map(ApiTokenEntity::getId);
    }

    private Optional<ApiTokenEntity> bootstrapRow() {
        return tokens.findFirstByCreatedViaOrderByCreatedAtAsc(TokenCreatedVia.BOOTSTRAP.wireValue());
    }

    /**
     * Creates the bootstrap row from {@code system_secret['admin_token_hash']} when the secret exists
     * and no bootstrap row does, then deletes the secret. Runs in the caller's transaction.
     *
     * @return true when a hash was migrated
     */
    boolean migrateLegacySecret() {
        Optional<SystemSecretEntity> legacy = secrets.findById(ADMIN_TOKEN_HASH_KEY);
        if (legacy.isEmpty()) {
            return false;
        }
        boolean migrated = false;
        if (bootstrapRow().isEmpty()) {
            String hash = new String(legacy.get().getValue(), StandardCharsets.UTF_8);
            ApiTokenEntity row = new ApiTokenEntity(UUID.randomUUID(), ApiTokenEntity.BOOTSTRAP_NAME, Scope.ADMIN, hash,
                    // The plaintext is gone, so only the shared prefix is known.
                    TokenHasher.TOKEN_PREFIX, TokenCreatedVia.BOOTSTRAP, principals.owner().getId(), clock.instant());
            tokens.saveAndFlush(row);
            migrated = true;
        }
        secrets.delete(legacy.get());
        return migrated;
    }

    private String storeNewToken() {
        String token = hasher.generateToken();
        String hash = hasher.hash(token);
        tx.executeWithoutResult(status -> {
            Optional<ApiTokenEntity> existing = bootstrapRow();
            if (existing.isPresent()) {
                existing.get().replaceSecret(hash, TokenService.prefix(token));
                tokens.saveAndFlush(existing.get());
            } else {
                tokens.saveAndFlush(new ApiTokenEntity(UUID.randomUUID(), ApiTokenEntity.BOOTSTRAP_NAME, Scope.ADMIN,
                        hash, TokenService.prefix(token), TokenCreatedVia.BOOTSTRAP, principals.owner().getId(),
                        clock.instant()));
            }
        });
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
                  Foresignal (foresignal.ai), unrelated to foresignal.com
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
