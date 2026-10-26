package ai.foresignal.security.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * The {@link AccountMailer} until the transactional email sender of R2-007 exists: it logs that an email would have
 * been sent, naming the user id and the token's 8-character prefix, never the token itself. An admin can then issue a
 * fresh link for the user through user management (R2-005).
 */
@Component
public class LoggingAccountMailer implements AccountMailer {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingAccountMailer.class);

    @Override
    public void sendPasswordReset(UserEntity user, IssuedUserToken token) {
        LOG.info("No account email sender is configured: a password reset email for user {} (token {}…) was not sent",
                user.getId(), token.token().getTokenPrefix());
    }
}
