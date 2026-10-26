package ai.foresignal.security.user;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The account status machine and password setting (prompt R2-002).
 *
 * <p>Legal transitions: {@code invited → active} (activation), {@code active ↔ suspended}, and any status
 * {@code → disabled}, which is terminal for sign-in; the row is kept for audit and ownership. Anything else is
 * {@code 409 USER_STATUS_CONFLICT}. Only an {@code active} user may authenticate ({@link #canAuthenticate}).
 *
 * <p>Suspending, disabling and setting a password publish {@link UserSessionsRevokedEvent}, the hook through which
 * the session store (R2-003) ends every session of the user.
 */
@Service
public class UserLifecycleService {

    /** For each status, the statuses it may move to. */
    static final Map<UserStatus, Set<UserStatus>> TRANSITIONS = Map.of(
            UserStatus.INVITED, Set.of(UserStatus.ACTIVE, UserStatus.DISABLED),
            UserStatus.ACTIVE, Set.of(UserStatus.SUSPENDED, UserStatus.DISABLED),
            UserStatus.SUSPENDED, Set.of(UserStatus.ACTIVE, UserStatus.DISABLED),
            UserStatus.DISABLED, Set.of());

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final PasswordPolicy policy;
    private final ApplicationEventPublisher events;
    private final UserTokenService userTokens;
    private final Clock clock;

    /**
     * Creates the service.
     *
     * @param users the user store
     * @param hasher argon2id hashing
     * @param policy the password policy
     * @param events where the session-revocation hook is published
     * @param userTokens activation and reset tokens
     * @param clock the clock
     */
    public UserLifecycleService(UserRepository users, PasswordHasher hasher, PasswordPolicy policy,
            ApplicationEventPublisher events, UserTokenService userTokens, Clock clock) {
        this.users = users;
        this.hasher = hasher;
        this.policy = policy;
        this.events = events;
        this.userTokens = userTokens;
        this.clock = clock;
    }

    /**
     * Activation: checks the password against the policy first (so a refused password does not use up the link),
     * consumes the activation token, sets the first password, and moves {@code invited → active}. One transaction.
     *
     * @param token the activation token from the link
     * @param password the first password
     * @return the activated user
     * @throws ValidationException {@code 400 PASSWORD_POLICY} or {@code USER_TOKEN_INVALID}
     */
    @Transactional
    public UserEntity activateWithToken(String token, String password) {
        policy.validate(password, "password");
        UserEntity user = userTokens.consume(token, UserTokenKind.ACTIVATION);
        if (user.getStatus() != UserStatus.INVITED) {
            throw conflict(user, UserStatus.ACTIVE);
        }
        setPassword(user, password, "password");
        activate(user);
        return user;
    }

    /**
     * Password reset: checks the policy, consumes the reset token, and sets the new password, which ends every
     * session. A user who is no longer active cannot complete a reset; the answer is the same as for a bad link.
     *
     * @param token the reset token from the link
     * @param password the new password
     * @return the user
     * @throws ValidationException {@code 400 PASSWORD_POLICY} or {@code USER_TOKEN_INVALID}
     */
    @Transactional
    public UserEntity resetWithToken(String token, String password) {
        policy.validate(password, "password");
        UserEntity user = userTokens.consume(token, UserTokenKind.RESET);
        if (!canAuthenticate(user)) {
            throw new ValidationException(ErrorCodes.USER_TOKEN_INVALID,
                    "The link is not valid. It may have expired or been used already.", List.of());
        }
        setPassword(user, password, "password");
        return user;
    }

    /**
     * True when the status machine allows a move.
     *
     * @param from the current status
     * @param to the wanted status
     * @return true when legal
     */
    public static boolean isLegal(UserStatus from, UserStatus to) {
        return TRANSITIONS.get(from).contains(to);
    }

    /**
     * True when the user may authenticate: only an {@code active} user.
     *
     * @param user the user
     * @return true when active
     */
    public static boolean canAuthenticate(UserEntity user) {
        return user != null && user.getStatus() == UserStatus.ACTIVE;
    }

    /**
     * Moves an invited user to active. Called by activation once its token is consumed and the password set.
     *
     * @param user the user
     * @throws ConflictException when the user is not invited
     */
    @Transactional
    public void activate(UserEntity user) {
        if (user.getStatus() != UserStatus.INVITED) {
            throw conflict(user, UserStatus.ACTIVE);
        }
        move(user, UserStatus.ACTIVE);
    }

    /**
     * Suspends an active user: sign-in is refused and every session ends; configuration is kept.
     *
     * @param user the user
     * @throws ConflictException unless the user is active
     */
    @Transactional
    public void suspend(UserEntity user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw conflict(user, UserStatus.SUSPENDED);
        }
        move(user, UserStatus.SUSPENDED);
        events.publishEvent(new UserSessionsRevokedEvent(user.getId(), UserSessionsRevokedEvent.Reason.SUSPENDED));
    }

    /**
     * Restores a suspended user to active.
     *
     * @param user the user
     * @throws ConflictException unless the user is suspended
     */
    @Transactional
    public void unsuspend(UserEntity user) {
        if (user.getStatus() != UserStatus.SUSPENDED) {
            throw conflict(user, UserStatus.ACTIVE);
        }
        move(user, UserStatus.ACTIVE);
    }

    /**
     * Disables a user for good: sign-in is refused and every session ends; the row is kept.
     *
     * @param user the user
     * @throws ConflictException when the user is already disabled
     */
    @Transactional
    public void disable(UserEntity user) {
        if (!isLegal(user.getStatus(), UserStatus.DISABLED)) {
            throw conflict(user, UserStatus.DISABLED);
        }
        move(user, UserStatus.DISABLED);
        events.publishEvent(new UserSessionsRevokedEvent(user.getId(), UserSessionsRevokedEvent.Reason.DISABLED));
    }

    /**
     * Sets a user's password after checking the policy, and ends every session of the user. Does not change the
     * status; activation calls {@link #activate} as well.
     *
     * @param user the user
     * @param raw the new password; never logged
     * @param field the request field to name if the policy refuses it
     * @throws ValidationException {@code 400 PASSWORD_POLICY}
     */
    @Transactional
    public void setPassword(UserEntity user, String raw, String field) {
        policy.validate(raw, field);
        user.setPasswordHash(hasher.hash(raw), clock.instant());
        users.save(user);
        events.publishEvent(new UserSessionsRevokedEvent(user.getId(),
                UserSessionsRevokedEvent.Reason.PASSWORD_CHANGED));
    }

    /**
     * True when a user may authenticate and the password matches. Checks the password even for a user who may not
     * authenticate, so the time taken does not reveal the status.
     *
     * @param user the user, or null when no account matched
     * @param raw the presented password
     * @return true only for an active user with a matching password
     */
    public boolean verify(UserEntity user, String raw) {
        boolean matches = hasher.matches(raw, user == null ? null : user.getPasswordHash());
        return matches && canAuthenticate(user);
    }

    private void move(UserEntity user, UserStatus to) {
        Instant now = clock.instant();
        user.setStatus(to, now);
        users.save(user);
    }

    private static ConflictException conflict(UserEntity user, UserStatus to) {
        return new ConflictException(ErrorCodes.USER_STATUS_CONFLICT,
                "A " + user.getStatus().wireValue() + " account cannot become " + to.wireValue() + ".");
    }
}
