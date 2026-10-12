package ai.foresignal.security;

import java.util.UUID;

/**
 * Published once a token's revocation has committed (prompt 023).
 *
 * <p>Listeners: the ticket store invalidates the token's tickets
 * ({@link ai.foresignal.security.ticket.TicketStore}), the SSE hub closes its open streams, and
 * prompt 031 deletes the Web Push subscriptions the token created (031 stores {@code token_id};
 * 032 does the same for push devices). A listener must not throw: the revocation has already
 * committed.
 *
 * @param tokenId the revoked token
 */
public record ApiTokenRevokedEvent(UUID tokenId) {}
