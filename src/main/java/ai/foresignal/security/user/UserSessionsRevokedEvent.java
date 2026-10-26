package ai.foresignal.security.user;

import java.util.UUID;

/**
 * Every session of a user must end now (prompt R2-002): published synchronously, inside the transaction that changed
 * the account, when a password is set or the user is suspended or disabled. The session store (R2-003) listens and
 * revokes; until it exists nobody listens and there is nothing to revoke.
 *
 * @param userId the user
 * @param reason why
 */
public record UserSessionsRevokedEvent(UUID userId, Reason reason) {

    /** Why the sessions end. */
    public enum Reason {
        /** A password was set by activation, reset or an admin. */
        PASSWORD_CHANGED,
        /** The account was suspended. */
        SUSPENDED,
        /** The account was disabled. */
        DISABLED
    }
}
