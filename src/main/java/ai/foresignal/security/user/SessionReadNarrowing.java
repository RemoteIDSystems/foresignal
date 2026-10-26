package ai.foresignal.security.user;

import ai.foresignal.security.restrict.ReadRestriction;

/**
 * Narrows a signed-in user's read restriction by something the user chose (prompt R2-010: their source view). The
 * token filter applies every implementation to a session's restriction before binding it, so the read paths need not
 * know about users.
 */
public interface SessionReadNarrowing {

    /**
     * The restriction narrowed for {@code session}.
     *
     * @param restriction the restriction so far
     * @param session the session
     * @return the narrowed restriction, or {@code restriction} itself when nothing narrows it
     */
    ReadRestriction narrow(ReadRestriction restriction, AuthenticatedSession session);
}
