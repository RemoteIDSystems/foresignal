package ai.foresignal.security.user;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;

/**
 * The user who owns the API token of a request (prompt R2-025): its principal is the user's {@code person} principal.
 * Held as a request attribute, so the audit attributes the request to the user and reads are narrowed by the user's
 * data restriction, as for the user's own session.
 *
 * @param userId the user
 * @param email the user's email
 */
public record TokenOwner(UUID userId, String email) {

    /** The request attribute holding it. */
    public static final String ATTRIBUTE = "ai.foresignal.tokenOwner";

    /**
     * The owner of a request's token, if a user owns it.
     *
     * @param request the request
     * @return the owner, or empty
     */
    public static Optional<TokenOwner> of(HttpServletRequest request) {
        Object value = request == null ? null : request.getAttribute(ATTRIBUTE);
        return value instanceof TokenOwner o ? Optional.of(o) : Optional.empty();
    }
}
