package ai.foresignal.security.user;

/**
 * Sends the account emails that carry a one-time link (prompt R2-003; the templated SMTP sender is R2-007). The
 * plaintext token is passed only so it can be put in the link; an implementation never logs it.
 */
public interface AccountMailer {

    /**
     * Sends a password reset link.
     *
     * @param user the user, who is active
     * @param token the freshly issued reset token
     */
    void sendPasswordReset(UserEntity user, IssuedUserToken token);
}
