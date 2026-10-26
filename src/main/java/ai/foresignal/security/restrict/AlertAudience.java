package ai.foresignal.security.restrict;

/**
 * Which alerts a caller may see by their owner (prompt R2-008): a {@link ReadRestriction} member that is never stored.
 *
 * <p>An alert carries the owner of its rule when it fired: a user's principal, or null for a shared rule. A caller
 * with an audience sees the shared alerts plus, when {@link #principalId()} is set, the alerts it owns. A caller
 * without one (an API token, or a session holding {@code users.manage}) sees every alert.
 *
 * @param principalId the signed-in user's principal, or null for a caller that sees shared alerts only (anonymous
 *     callers, feed and tile keys)
 */
public record AlertAudience(String principalId) {

    /** Shared alerts only: anonymous callers, feed keys and tile keys. */
    public static final AlertAudience SHARED_ONLY = new AlertAudience(null);

    /**
     * The audience of a signed-in user without {@code users.manage}: their own alerts and the shared ones.
     *
     * @param principalId the user's principal
     * @return the audience
     */
    public static AlertAudience own(String principalId) {
        if (principalId == null) {
            throw new IllegalArgumentException("principalId");
        }
        return new AlertAudience(principalId);
    }

    /**
     * True when an alert or rule owned by {@code owner} is visible to this audience.
     *
     * @param owner the owner's principal, or null for a shared alert or rule
     * @return true when visible
     */
    public boolean allows(String owner) {
        return owner == null || owner.equals(principalId);
    }

    /**
     * A stable text form, for cache keys and cursor fingerprints.
     *
     * @return {@code *shared} or the principal
     */
    public String canonical() {
        return principalId == null ? "*shared" : principalId;
    }
}
