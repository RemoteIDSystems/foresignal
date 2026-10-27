package ai.foresignal.security.user;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.security.TokenHasher;
import ai.foresignal.security.audit.AuditRecord;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.security.mfa.MfaService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sign-in and login sessions (prompt R2-003, invariants RBAC-2, RBAC-5, RBAC-7).
 *
 * <p>A session bearer is {@value #PREFIX} followed by 32 random bytes, base64url, unpadded, returned once by
 * {@link #login}; only its SHA-256 hex ({@link TokenHasher}) and first eight characters are stored. It lives
 * {@link #ABSOLUTE_LIFETIME} at most and ends after {@link #IDLE_TIMEOUT} without use; each accepted request moves the
 * idle expiry forward, written at most once per {@link #TOUCH_INTERVAL}. The user's status and roles are read again on
 * every request, so suspension and role changes apply to the next request.
 *
 * <p>Every sign-in failure (unknown email, wrong password, an account that is not {@code active}) is the same
 * {@code 401 INVALID_CREDENTIALS}, takes the same argon2id work (an unknown email is checked against a dummy hash), and
 * is audited with the reason in the audit detail only.
 *
 * <p>{@link UserSessionsRevokedEvent} (password set, suspension, disabling) revokes all of a user's sessions in the
 * transaction that changed the account.
 *
 * <p>Prompt R2-024: a user with MFA active gets no session from the password alone. Without a code the sign-in answers
 * {@code mfaRequired} and issues nothing; with a wrong one it is the same {@code 401 INVALID_CREDENTIALS}. A session
 * of a user whose role requires MFA but who has not enrolled is marked {@link AuthenticatedSession#mfaEnrollmentRequired()}
 * on every request, and the token filter confines it to enrollment.
 */
@Service
public class SessionService {

    /** The prefix of a session bearer. */
    public static final String PREFIX = "fss_";

    /** How long a session lives at most: 12 hours. */
    public static final Duration ABSOLUTE_LIFETIME = Duration.ofHours(12);

    /** How long a session lives without use: 2 hours. */
    public static final Duration IDLE_TIMEOUT = Duration.ofHours(2);

    /** The idle expiry and {@code last_seen_at} are written at most this often per session. */
    public static final Duration TOUCH_INTERVAL = Duration.ofMinutes(1);

    static final int ENTROPY_BYTES = 32;

    private final UserSessionRepository sessions;
    private final UserRepository users;
    private final PermissionResolver permissions;
    private final PasswordHasher passwords;
    private final TokenHasher hasher;
    private final AuditService audit;
    private final MfaService mfa;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();
    private volatile String dummyHash;

    /**
     * Creates the service.
     *
     * @param sessions the session store
     * @param users the user store
     * @param permissions effective-permission resolution
     * @param passwords argon2id verification
     * @param hasher SHA-256 of bearers
     * @param audit the audit log
     * @param mfa the second factor (R2-024)
     * @param clock the clock
     */
    public SessionService(UserSessionRepository sessions, UserRepository users, PermissionResolver permissions,
            PasswordHasher passwords, TokenHasher hasher, AuditService audit, MfaService mfa, Clock clock) {
        this.sessions = sessions;
        this.users = users;
        this.permissions = permissions;
        this.passwords = passwords;
        this.hasher = hasher;
        this.audit = audit;
        this.mfa = mfa;
        this.clock = clock;
    }

    /**
     * A successful sign-in, or a correct password still waiting for its MFA code.
     *
     * @param token the session bearer, returned once; null while {@code mfaRequired}
     * @param session the stored session; null while {@code mfaRequired}
     * @param user the user
     * @param permissions the user's effective permissions now
     * @param mfaRequired true when MFA is active and no code was given: no session was issued (R2-024)
     */
    public record Login(String token, UserSessionEntity session, UserEntity user, EffectivePermissions permissions,
            boolean mfaRequired) {

        @Override
        public String toString() {
            return "Login[session=" + (session == null ? null : session.getId()) + ", user=" + user.getId()
                    + ", mfaRequired=" + mfaRequired + "]";
        }
    }

    /**
     * Signs a user in with email and password.
     *
     * @param email the email as entered
     * @param password the password; never logged
     * @param mfaCode a TOTP or recovery code when MFA is active, or null (R2-024); never logged
     * @param userAgent the client's {@code User-Agent}, or null
     * @param ip the client address, or null
     * @return the new session, or, for a user with MFA active and no code, {@code mfaRequired} with no session
     * @throws ForesignalException {@code 401 INVALID_CREDENTIALS} for every failure, a wrong MFA code included
     */
    @Transactional
    public Login login(String email, String password, String mfaCode, String userAgent, String ip) {
        Instant now = clock.instant();
        UserEntity user = email == null ? null : users.findByEmail(UserEntity.normalizeEmail(email)).orElse(null);
        String stored = user == null || user.getPasswordHash() == null ? dummyHash() : user.getPasswordHash();
        boolean matches = password != null && passwords.matches(password, stored)
                && user != null && user.getPasswordHash() != null;
        if (!matches || !UserLifecycleService.canAuthenticate(user)) {
            String reason = user == null ? "unknown_email" : !matches ? "wrong_password"
                    : "status_" + user.getStatus().wireValue();
            auditAuth(now, "login", user, null, 401, ip, Map.of("event", "login_failed", "reason", reason));
            throw invalidCredentials();
        }
        if (user.isMfaEnabled()) {
            if (mfaCode == null || mfaCode.isBlank()) {
                auditAuth(now, "login", user, null, 200, ip, Map.of("event", "login_mfa_required"));
                return new Login(null, null, user, EffectivePermissions.NONE, true);
            }
            if (!mfa.verifyLogin(user, mfaCode)) {
                auditAuth(now, "login", user, null, 401, ip, Map.of("event", "login_failed",
                        "reason", "wrong_mfa_code"));
                throw invalidCredentials();
            }
        }
        String token = newBearer();
        UserSessionEntity session = sessions.save(new UserSessionEntity(UUID.randomUUID(), user.getId(),
                hasher.hash(token), token.substring(0, 8), now, now.plus(ABSOLUTE_LIFETIME), now.plus(IDLE_TIMEOUT),
                userAgent, ip));
        user.recordLogin(now);
        users.save(user);
        auditAuth(now, "login", user, session.getId(), 200, ip, user.isMfaEnabled()
                ? Map.of("event", "login", "mfa", true) : Map.of("event", "login"));
        return new Login(token, session, user, permissions.forUser(user.getId()), false);
    }

    /**
     * A user by id.
     *
     * @param userId the user
     * @return the user, or empty
     */
    @Transactional(readOnly = true)
    public Optional<UserEntity> user(UUID userId) {
        return users.findById(userId);
    }

    /**
     * Resolves a session bearer for one request: live session, active user, permissions read now. Moves the idle
     * expiry at most once per {@link #TOUCH_INTERVAL}.
     *
     * @param bearer the presented bearer
     * @return the session, or empty when it is not a live session of an active user
     */
    @Transactional
    public Optional<AuthenticatedSession> authenticate(String bearer) {
        if (bearer == null || !bearer.startsWith(PREFIX)) {
            return Optional.empty();
        }
        String hash = hasher.hash(bearer);
        UserSessionEntity session = sessions.findByTokenHash(hash).orElse(null);
        Instant now = clock.instant();
        if (session == null || !MessageDigest.isEqual(hash.getBytes(StandardCharsets.UTF_8),
                session.getTokenHash().getBytes(StandardCharsets.UTF_8)) || !session.isLive(now)) {
            return Optional.empty();
        }
        UserEntity user = users.findById(session.getUserId()).orElse(null);
        if (!UserLifecycleService.canAuthenticate(user)) {
            return Optional.empty();
        }
        if (session.getLastSeenAt() == null || !now.isBefore(session.getLastSeenAt().plus(TOUCH_INTERVAL))) {
            session.touch(now, now.plus(IDLE_TIMEOUT));
            sessions.save(session);
        }
        return Optional.of(new AuthenticatedSession(session.getId(), user.getId(), user.getEmail(),
                user.getPrincipalId(), session.getTokenPrefix(), session.getExpiresAt(), session.getIdleExpiresAt(),
                permissions.forUser(user.getId()), mfa.enrollmentPending(user)));
    }

    /**
     * Ends the calling session ({@code logout}).
     *
     * @param current the calling session
     * @param ip the client address, for the audit
     */
    @Transactional
    public void logout(AuthenticatedSession current, String ip) {
        Instant now = clock.instant();
        sessions.findById(current.sessionId()).ifPresent(s -> {
            s.revoke(now);
            sessions.save(s);
        });
        auditAuth(now, "logout", users.findById(current.userId()).orElse(null), current.sessionId(), 204, ip,
                Map.of("event", "logout"));
    }

    /**
     * A page of the caller's live sessions, by id ({@code listSessions}).
     *
     * @param current the calling session, marked {@code current: true}; null when the caller is not a session
     * @param userId the user
     * @param cursor the previous page's {@code nextCursor}, or null
     * @param limit the page size
     * @return the {@code SessionPage} view
     */
    @Transactional(readOnly = true)
    public Map<String, Object> list(UUID current, UUID userId, String cursor, int limit) {
        UUID after;
        try {
            after = cursor == null || cursor.isBlank() ? null : UUID.fromString(cursor);
        } catch (IllegalArgumentException e) {
            throw ai.foresignal.api.error.ValidationException.forField("cursor", "is not a cursor from this list");
        }
        List<UserSessionEntity> page = sessions.findLive(userId, clock.instant(), after,
                PageRequest.of(0, limit + 1));
        boolean more = page.size() > limit;
        List<UserSessionEntity> items = more ? page.subList(0, limit) : page;
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items.stream().map(s -> view(s, current)).toList());
        out.put("nextCursor", more ? items.get(items.size() - 1).getId().toString() : null);
        return out;
    }

    /**
     * Revokes one of the user's own sessions ({@code revokeSession}).
     *
     * @param userId the calling user
     * @param sessionId the session to revoke
     * @param ip the client address, for the audit
     * @throws NotFoundException {@code 404 UNKNOWN_SESSION} when the session does not exist or is another user's
     */
    @Transactional
    public void revoke(UUID userId, UUID sessionId, String ip) {
        UserSessionEntity session = sessions.findById(sessionId).filter(s -> s.getUserId().equals(userId))
                .orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_SESSION, "No such session."));
        Instant now = clock.instant();
        session.revoke(now);
        sessions.save(session);
        auditAuth(now, "revokeSession", users.findById(userId).orElse(null), sessionId, 204, ip,
                Map.of("event", "session_revoked"));
    }

    /**
     * Revokes the user's sessions ({@code revokeAllSessions}).
     *
     * @param userId the calling user
     * @param keep the session to keep (the calling one), or null to revoke every session
     * @param ip the client address, for the audit
     * @return how many were revoked
     */
    @Transactional
    public int revokeAll(UUID userId, UUID keep, String ip) {
        Instant now = clock.instant();
        int revoked = sessions.revokeAll(userId, keep, now);
        auditAuth(now, "revokeAllSessions", users.findById(userId).orElse(null), keep, 204, ip,
                Map.of("event", "sessions_revoked", "count", revoked, "keptCurrent", keep != null));
        return revoked;
    }

    /**
     * The session-revocation hook of R2-002: every session of the user ends, in the transaction that changed it.
     *
     * @param event the event
     */
    @EventListener
    @Transactional
    public void onSessionsRevoked(UserSessionsRevokedEvent event) {
        sessions.revokeAll(event.userId(), null, clock.instant());
    }

    /**
     * Audits an account event that happened outside a token-authenticated write (a sign-in, a reset) or that the
     * token filter does not see in full.
     *
     * @param at when
     * @param operationId the operation
     * @param user the user, or null when no account matched
     * @param sessionId the session, or null
     * @param status the HTTP status answered
     * @param ip the client address
     * @param detail what happened; never a password or token
     */
    public void auditAuth(Instant at, String operationId, UserEntity user, UUID sessionId, int status, String ip,
            Map<String, Object> detail) {
        // R2-013: an account event is the user's own: actor kind user, with the user's id and email. A sign-in for an
        // unknown email still has actor kind user, with no user.
        audit.record(new AuditRecord(at, AuditRecord.TOKEN, sessionId,
                user == null ? null : AuthenticatedSession.NAME_PREFIX + user.getEmail(),
                user == null ? null : user.getPrincipalId(), null, "POST", null, operationId, status, "users",
                user == null ? null : user.getId().toString(), ip, new LinkedHashMap<>(detail))
                .asUser(user == null ? null : user.getId(), user == null ? null : user.getEmail()));
    }

    /**
     * Audits an account event now ({@link #auditAuth(Instant, String, UserEntity, UUID, int, String, Map)} at the
     * current time, with no session): activation and password resets (prompt R2-013).
     *
     * @param operationId the operation
     * @param user the user, or null when no account matched
     * @param status the HTTP status answered
     * @param ip the client address
     * @param detail what happened; never a password or token
     */
    public void auditAuth(String operationId, UserEntity user, int status, String ip, Map<String, Object> detail) {
        auditAuth(clock.instant(), operationId, user, null, status, ip, detail);
    }

    /** The {@code Session} view. */
    static Map<String, Object> view(UserSessionEntity s, UUID current) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", s.getId().toString());
        out.put("tokenPrefix", s.getTokenPrefix());
        out.put("createdAt", s.getCreatedAt());
        out.put("lastSeenAt", s.getLastSeenAt());
        out.put("expiresAt", s.getExpiresAt());
        out.put("idleExpiresAt", s.getIdleExpiresAt());
        out.put("userAgent", s.getUserAgent());
        out.put("ipAddress", s.getIpAddress());
        out.put("current", s.getId().equals(current));
        return out;
    }

    private String newBearer() {
        byte[] entropy = new byte[ENTROPY_BYTES];
        random.nextBytes(entropy);
        return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(entropy);
    }

    /** A hash of a random password, so an unknown email costs the same argon2id work as a known one. */
    private String dummyHash() {
        String h = dummyHash;
        if (h == null) {
            byte[] b = new byte[16];
            random.nextBytes(b);
            h = passwords.hash(Base64.getEncoder().encodeToString(b));
            dummyHash = h;
        }
        return h;
    }

    private static ForesignalException invalidCredentials() {
        return new ForesignalException(HttpStatus.UNAUTHORIZED, ErrorCodes.INVALID_CREDENTIALS,
                "The email or password is not correct.");
    }
}
