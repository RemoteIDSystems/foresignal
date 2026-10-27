package ai.foresignal.security.user;

import java.util.List;

/**
 * Sends the account emails (prompt R2-003; user management adds the welcome, role-changed and suspended notices in
 * R2-005; the templated SMTP sender is R2-007). The plaintext token is passed only so it can be put in the link; an
 * implementation never logs it.
 *
 * <p>The link-carrying methods return whether the email actually left: when it did not (no sender configured, or the
 * caller asked for no email), user management hands the one-time token to the admin instead, so an account never
 * depends on SMTP being up.
 */
public interface AccountMailer {

    /**
     * Sends a password reset link.
     *
     * @param user the user, who is active
     * @param token the freshly issued reset token
     * @return true when the email was sent
     */
    boolean sendPasswordReset(UserEntity user, IssuedUserToken token);

    /**
     * Sends the welcome (account added) email with the activation link.
     *
     * @param user the invited user
     * @param roleNames the names of the roles assigned
     * @param token the freshly issued activation token
     * @return true when the email was sent
     */
    boolean sendWelcome(UserEntity user, List<String> roleNames, IssuedUserToken token);

    /**
     * Tells a user their roles changed.
     *
     * @param user the user
     * @param roleNames the names of the roles now held
     */
    void sendRoleChanged(UserEntity user, List<String> roleNames);

    /**
     * Tells a user their account was suspended.
     *
     * @param user the user
     */
    void sendSuspended(UserEntity user);

    /**
     * Tells a user that multi-factor authentication was turned on or off on their account (prompt R2-024), so a change
     * they did not make is noticed. Does nothing by default.
     *
     * @param user the user
     * @param enabled true when MFA was turned on, false when it was turned off
     * @param byAdmin true when an administrator reset it
     */
    default void sendMfaChanged(UserEntity user, boolean enabled, boolean byAdmin) {
    }
}
