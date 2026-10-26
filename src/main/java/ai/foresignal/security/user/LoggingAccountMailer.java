package ai.foresignal.security.user;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * The {@link AccountMailer} until the transactional email sender of R2-007 exists: it logs that an email would have
 * been sent, naming the user id and, for a link, the token's 8-character prefix, never the token itself. It sends
 * nothing, so user management returns each one-time link to the admin (R2-005).
 */
@Component
public class LoggingAccountMailer implements AccountMailer {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingAccountMailer.class);

    @Override
    public boolean sendPasswordReset(UserEntity user, IssuedUserToken token) {
        LOG.info("No account email sender is configured: a password reset email for user {} (token {}…) was not sent",
                user.getId(), token.token().getTokenPrefix());
        return false;
    }

    @Override
    public boolean sendWelcome(UserEntity user, List<String> roleNames, IssuedUserToken token) {
        LOG.info("No account email sender is configured: a welcome email for user {} (token {}…) was not sent",
                user.getId(), token.token().getTokenPrefix());
        return false;
    }

    @Override
    public void sendRoleChanged(UserEntity user, List<String> roleNames) {
        LOG.info("No account email sender is configured: a role-changed email for user {} was not sent", user.getId());
    }

    @Override
    public void sendSuspended(UserEntity user) {
        LOG.info("No account email sender is configured: a suspended email for user {} was not sent", user.getId());
    }
}
