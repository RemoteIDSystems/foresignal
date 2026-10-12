package ai.foresignal.security;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import jakarta.annotation.PreDestroy;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;

/**
 * Scoped API tokens (Architecture §15, prompt 023): creation, listing, revocation and
 * authentication.
 *
 * <ul>
 *   <li><b>Create</b> returns the plaintext once ({@code fsr_} + base64url of 32 random bytes); only
 *       its SHA-256 and its first eight characters are stored.
 *   <li><b>Authenticate</b> looks the presented token up by hash and confirms it with a
 *       constant-time comparison; revoked and expired tokens are refused. {@code last_used_at} is
 *       written at most once a minute per token, off the request thread.
 *   <li><b>Revoke</b> refuses the bootstrap token ({@code 409 BOOTSTRAP_TOKEN}) and publishes
 *       {@link ApiTokenRevokedEvent} after the revocation commits.
 * </ul>
 */
@Service
public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    /** The least time between two {@code last_used_at} writes for one token. */
    public static final Duration LAST_USED_INTERVAL = Duration.ofMinutes(1);

    /** Longest token name. */
    public static final int MAX_NAME = 200;

    static final Set<String> INPUT_MEMBERS = Set.of(
            "name", "scope", "expiresAt", "restrictions", "principalId", "privateAccess", "licencePolicy");

    /**
     * What to create.
     *
     * @param name the token's name
     * @param scope its scope
     * @param expiresAt its expiry, or null
     * @param restrictions its restrictions (validated by the caller)
     * @param device device metadata, or null
     * @param createdVia how it is created
     * @param principalId the owning principal, or null for a new one named after the token
     * @param privateAccess the 039 private grant
     */
    public record TokenSpec(String name, Scope scope, Instant expiresAt, TokenRestrictions restrictions,
            DeviceInfo device, TokenCreatedVia createdVia, String principalId, boolean privateAccess) {

        /** A spec without the private grant (the shape before prompt 039). */
        public TokenSpec(String name, Scope scope, Instant expiresAt, TokenRestrictions restrictions,
                DeviceInfo device, TokenCreatedVia createdVia, String principalId) {
            this(name, scope, expiresAt, restrictions, device, createdVia, principalId, false);
        }
    }

    /**
     * A created token.
     *
     * @param entity the stored row
     * @param plaintext the token value, to be shown exactly once
     * @param principalHandle the owning principal's handle
     */
    public record CreatedToken(ApiTokenEntity entity, String plaintext, String principalHandle) {}

    private final ApiTokenRepository tokens;
    private final TokenHasher hasher;
    private final PrincipalService principals;
    private final RestrictionValidator restrictions;
    private final ApplicationEventPublisher events;
    private final TransactionTemplate tx;
    private final Clock clock;
    private final Map<UUID, Instant> lastUseWritten = new ConcurrentHashMap<>();
    private final ExecutorService lastUseWriter = Executors.newSingleThreadExecutor(
            Thread.ofVirtual().name("token-last-used").factory());

    /** Creates the service. */
    public TokenService(ApiTokenRepository tokens, TokenHasher hasher, PrincipalService principals,
            RestrictionValidator restrictions, ApplicationEventPublisher events,
            PlatformTransactionManager transactions, Clock clock) {
        this.tokens = tokens;
        this.hasher = hasher;
        this.principals = principals;
        this.restrictions = restrictions;
        this.events = events;
        this.tx = new TransactionTemplate(transactions);
        this.clock = clock;
    }

    /** Stops the {@code last_used_at} writer. */
    @PreDestroy
    public void stop() {
        lastUseWriter.shutdown();
    }

    // ------------------------------------------------------------------------- create

    /**
     * Parses and validates an {@code ApiTokenInput} body, then creates the token.
     *
     * @throws ValidationException listing every problem
     */
    @Transactional
    public CreatedToken create(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be an ApiTokenInput object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String member : body.propertyNames()) {
            if (!INPUT_MEMBERS.contains(member)) {
                errors.add(new FieldError(member, "is not an ApiTokenInput member"));
            }
        }
        String name = name(body.get("name"), "name", errors);
        Scope scope = scope(body.get("scope"), "scope", errors, false);
        Instant expiresAt = expiresAt(body.get("expiresAt"), errors);
        TokenRestrictions parsed = restrictions.parse(body.get("restrictions"), scope == null ? Scope.READ : scope,
                "restrictions", errors);
        String principalId = null;
        JsonNode principal = body.get("principalId");
        if (principal != null && !principal.isNull()) {
            if (!principal.isString() || principal.asString().isBlank()) {
                errors.add(new FieldError("principalId", "must be a principal id or handle"));
            } else {
                try {
                    principalId = principals.resolve(principal.asString()).getId();
                } catch (ValidationException e) {
                    errors.addAll(e.getErrors());
                }
            }
        }
        laterMembers(body, errors);
        boolean privateAccess = privateAccess(body);
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, "The token input is not valid.", errors);
        }
        if (scope == Scope.ADMIN && !parsed.isEmpty()) {
            // An admin token can mint an unrestricted token anyway, so a restriction would be theater (035).
            throw new ValidationException(ErrorCodes.RESTRICTION_NOT_ALLOWED,
                    "Restrictions cannot be set on an admin token.",
                    List.of(new FieldError("restrictions", "is not allowed on an admin token")));
        }
        return create(new TokenSpec(name, scope, expiresAt, parsed, null, TokenCreatedVia.API, principalId,
                privateAccess && scope != Scope.ADMIN));
    }

    /** Creates a token from an already validated {@code spec}. */
    @Transactional
    public CreatedToken create(TokenSpec spec) {
        PrincipalEntity principal = spec.principalId() == null
                ? principals.createFor(spec.name(), PrincipalKind.AGENT)
                : principals.find(spec.principalId()).orElseThrow(
                        () -> ValidationException.forField("principalId", "unknown principal " + spec.principalId()));
        String plaintext = hasher.generateToken();
        Instant now = clock.instant();
        ApiTokenEntity entity = new ApiTokenEntity(UUID.randomUUID(), spec.name(), spec.scope(), hasher.hash(plaintext),
                prefix(plaintext), spec.createdVia(), principal.getId(), now);
        entity.setExpiresAt(spec.expiresAt());
        entity.setRestrictions(spec.restrictions());
        entity.setPrivateAccess(spec.privateAccess());
        if (spec.device() != null) {
            entity.setDevice(spec.device(), now);
        }
        tokens.saveAndFlush(entity);
        return new CreatedToken(entity, plaintext, principal.getHandle());
    }

    // ------------------------------------------------------------------------- read

    /** Every token, newest first. */
    @Transactional(readOnly = true)
    public List<ApiTokenEntity> list() {
        return tokens.findAllByOrderByCreatedAtDesc().stream().filter(t -> !t.isFeedKey()).toList();
    }

    /**
     * The token {@code id}.
     *
     * @throws NotFoundException when there is none
     */
    @Transactional(readOnly = true)
    public ApiTokenEntity get(UUID id) {
        return tokens.findById(id).filter(t -> !t.isFeedKey()).orElseThrow(() -> NotFoundException.of("Token", id));
    }

    /**
     * Token {@code id} if it is still active (neither revoked nor expired). Tickets re-check their
     * parent token with this on every use.
     */
    @Transactional(readOnly = true)
    public Optional<AuthenticatedToken> current(UUID id) {
        return tokens.findById(id).filter(t -> !t.isFeedKey() && t.isActive(clock.instant()))
                .map(AuthenticatedToken::of);
    }

    /** The handle of principal {@code principalId}, or null. */
    @Transactional(readOnly = true)
    public String principalHandle(String principalId) {
        return principals.find(principalId).map(PrincipalEntity::getHandle).orElse(null);
    }

    // ------------------------------------------------------------------------- revoke

    /**
     * Revokes token {@code id}. Revoking an already revoked token changes nothing but still
     * announces the revocation, so a listener that missed it catches up.
     *
     * @throws NotFoundException when there is no such token
     * @throws ConflictException {@code BOOTSTRAP_TOKEN} for the bootstrap admin token
     */
    @Transactional
    public void revoke(UUID id) {
        ApiTokenEntity entity = tokens.findById(id).filter(t -> !t.isFeedKey())
                .orElseThrow(() -> NotFoundException.of("Token", id));
        if (entity.isBootstrap()) {
            throw new ConflictException(ErrorCodes.BOOTSTRAP_TOKEN,
                    "The bootstrap admin token cannot be revoked. Rotate it with POST /v1/system/admin-token/rotate.");
        }
        if (entity.getRevokedAt() == null) {
            entity.setRevokedAt(clock.instant());
            tokens.saveAndFlush(entity);
        }
        lastUseWritten.remove(id);
        publishAfterCommit(new ApiTokenRevokedEvent(id));
    }

    // ------------------------------------------------------------------------- authenticate

    /**
     * The token {@code presented} is, when it is a current token: looked up by its SHA-256 and
     * confirmed with a constant-time comparison. Revoked and expired tokens are refused. A
     * successful authentication records the use ({@link #recordUse}).
     *
     * @param presented the token from the request, never logged
     * @return the token, or empty
     */
    public Optional<AuthenticatedToken> authenticate(String presented) {
        if (presented == null || presented.isBlank() || !presented.startsWith(TokenHasher.TOKEN_PREFIX)) {
            return Optional.empty();
        }
        Optional<ApiTokenEntity> found = tokens.findByTokenHash(hasher.hash(presented));
        if (found.isEmpty()) {
            return Optional.empty();
        }
        ApiTokenEntity entity = found.get();
        if (!hasher.matches(presented, entity.getTokenHash()) || entity.isFeedKey()
                || !entity.isActive(clock.instant())) {
            return Optional.empty();
        }
        recordUse(entity.getId());
        return Optional.of(AuthenticatedToken.of(entity));
    }

    /**
     * Records a use of token {@code id}: {@code last_used_at} is written asynchronously, at most once
     * per {@link #LAST_USED_INTERVAL} per token.
     *
     * @return true when a write was scheduled
     */
    public boolean recordUse(UUID id) {
        Instant now = clock.instant();
        boolean[] due = {false};
        lastUseWritten.compute(id, (key, last) -> {
            if (last == null || !now.isBefore(last.plus(LAST_USED_INTERVAL))) {
                due[0] = true;
                return now;
            }
            return last;
        });
        if (!due[0]) {
            return false;
        }
        try {
            lastUseWriter.execute(() -> {
                try {
                    tx.executeWithoutResult(status -> tokens.touch(id, now));
                } catch (RuntimeException e) {
                    log.debug("Could not record the use of token {}: {}", id, e.toString());
                }
            });
            return true;
        } catch (RejectedExecutionException e) {
            return false;
        }
    }

    // ------------------------------------------------------------------------- device

    /**
     * Stores the device metadata token {@code id} reports for itself and stamps {@code reportedAt}.
     *
     * @return the updated row
     */
    @Transactional
    public ApiTokenEntity updateDevice(UUID id, JsonNode body) {
        List<FieldError> errors = new ArrayList<>();
        DeviceInfo device = parseDevice(body, "", errors);
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, "The device metadata is not valid.", errors);
        }
        ApiTokenEntity entity = get(id);
        entity.setDevice(device, clock.instant());
        return tokens.saveAndFlush(entity);
    }

    // ------------------------------------------------------------------------- parsing helpers

    /**
     * Parses a {@code DeviceInfo} object; {@code prefix} is prepended to field names
     * ({@code "device."} inside a pairing claim).
     */
    public static DeviceInfo parseDevice(JsonNode node, String prefix, List<FieldError> errors) {
        String field = prefix.isEmpty() ? "device" : prefix.substring(0, prefix.length() - 1);
        if (node == null || !node.isObject()) {
            errors.add(new FieldError(field, "must be a DeviceInfo object"));
            return null;
        }
        for (String member : node.propertyNames()) {
            if (!Set.of("deviceName", "platform", "appVersion", "reportedAt").contains(member)) {
                errors.add(new FieldError(prefix + member, "is not a DeviceInfo member"));
            }
        }
        JsonNode name = node.get("deviceName");
        String deviceName = null;
        if (name == null || !name.isString() || name.asString().isBlank()) {
            errors.add(new FieldError(prefix + "deviceName", "is required"));
        } else if (name.asString().strip().length() > DeviceInfo.MAX_NAME) {
            errors.add(new FieldError(prefix + "deviceName", "must be at most " + DeviceInfo.MAX_NAME + " characters"));
        } else {
            deviceName = name.asString().strip();
        }
        JsonNode platformNode = node.get("platform");
        String platform = null;
        if (platformNode == null || !platformNode.isString()
                || !DeviceInfo.PLATFORMS.contains(platformNode.asString())) {
            errors.add(new FieldError(prefix + "platform", "must be one of " + String.join(", ", DeviceInfo.PLATFORMS)));
        } else {
            platform = platformNode.asString();
        }
        JsonNode versionNode = node.get("appVersion");
        String appVersion = null;
        if (versionNode != null && !versionNode.isNull()) {
            if (!versionNode.isString() || versionNode.asString().length() > DeviceInfo.MAX_APP_VERSION) {
                errors.add(new FieldError(prefix + "appVersion",
                        "must be a string of at most " + DeviceInfo.MAX_APP_VERSION + " characters"));
            } else {
                appVersion = versionNode.asString();
            }
        }
        return new DeviceInfo(deviceName, platform, appVersion, null);
    }

    /** A token or principal name: a string of 1–200 characters. */
    static String name(JsonNode node, String field, List<FieldError> errors) {
        if (node == null || !node.isString() || node.asString().isBlank()) {
            errors.add(new FieldError(field, "is required"));
            return null;
        }
        String value = node.asString().strip();
        if (value.length() > MAX_NAME) {
            errors.add(new FieldError(field, "must be at most " + MAX_NAME + " characters"));
            return null;
        }
        return value;
    }

    /** A scope; {@code admin} is refused when {@code pairable} (pairing codes). */
    static Scope scope(JsonNode node, String field, List<FieldError> errors, boolean pairable) {
        if (node == null || !node.isString()) {
            errors.add(new FieldError(field, pairable ? "must be read or agent" : "must be read, agent or admin"));
            return null;
        }
        try {
            Scope scope = Scope.fromWireValue(node.asString());
            if (pairable && scope == Scope.ADMIN) {
                errors.add(new FieldError(field, "admin is never pairable; use read or agent"));
                return null;
            }
            return scope;
        } catch (IllegalArgumentException e) {
            errors.add(new FieldError(field, e.getMessage()));
            return null;
        }
    }

    /** The {@code privateAccess} member (prompt 039); false when absent or not a boolean. */
    static boolean privateAccess(JsonNode body) {
        JsonNode node = body.get("privateAccess");
        return node != null && node.isBoolean() && node.asBoolean();
    }

    /**
     * Checks {@code privateAccess} (a boolean, prompt 039) and rejects members later prompts own
     * ({@code licencePolicy} 109) when set.
     */
    static void laterMembers(JsonNode body, List<FieldError> errors) {
        JsonNode privateAccess = body.get("privateAccess");
        if (privateAccess != null && !privateAccess.isNull() && !privateAccess.isBoolean()) {
            errors.add(new FieldError("privateAccess", "must be a boolean"));
        }
        JsonNode licence = body.get("licencePolicy");
        if (licence != null && !licence.isNull() && !(licence.isArray() && licence.isEmpty())) {
            errors.add(new FieldError("licencePolicy", "is not supported by this version yet"));
        }
    }

    private Instant expiresAt(JsonNode node, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isString()) {
            errors.add(new FieldError("expiresAt", "must be an RFC 3339 date-time or null"));
            return null;
        }
        try {
            Instant value = java.time.OffsetDateTime.parse(node.asString()).toInstant();
            if (!value.isAfter(clock.instant())) {
                errors.add(new FieldError("expiresAt", "must be in the future"));
                return null;
            }
            return value;
        } catch (DateTimeParseException e) {
            errors.add(new FieldError("expiresAt", "must be an RFC 3339 date-time such as 2027-01-01T00:00:00Z"));
            return null;
        }
    }

    /** The displayed prefix of {@code plaintext}. */
    static String prefix(String plaintext) {
        return plaintext.substring(0, Math.min(ApiTokenEntity.PREFIX_LENGTH, plaintext.length()));
    }

    private void publishAfterCommit(Object event) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            events.publishEvent(event);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    events.publishEvent(event);
                } catch (RuntimeException e) {
                    // The revocation has committed; a failing listener must not turn it into an error.
                    log.warn("A listener failed on {}: {}", event, e.toString());
                }
            }
        });
    }
}
