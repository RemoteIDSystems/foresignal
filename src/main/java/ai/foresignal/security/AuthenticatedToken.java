package ai.foresignal.security;

import java.util.Objects;
import java.util.UUID;

/**
 * A token that {@link TokenService#authenticate} accepted (prompt 023): who it is, what it may do
 * and on whose behalf. Never carries the token's value or hash.
 *
 * @param id the token id
 * @param name the token's name
 * @param scope its scope
 * @param restrictions its restrictions ({@link TokenRestrictions#NONE} when none)
 * @param device its reported device, or null
 * @param principalId the owning principal
 * @param bootstrap true for the bootstrap admin token
 * @param privateAccess the 039 private grant: a {@code read} or {@code agent} token that may see private
 *     sources (admin tokens always may)
 */
public record AuthenticatedToken(
        UUID id,
        String name,
        Scope scope,
        TokenRestrictions restrictions,
        DeviceInfo device,
        String principalId,
        boolean bootstrap,
        boolean privateAccess) {

    /** Requires the id, name and scope; null restrictions become {@link TokenRestrictions#NONE}. */
    public AuthenticatedToken {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(scope, "scope");
        restrictions = TokenRestrictions.orNone(restrictions);
    }

    /** A token without the private grant (the shape before prompt 039). */
    public AuthenticatedToken(UUID id, String name, Scope scope, TokenRestrictions restrictions, DeviceInfo device,
            String principalId, boolean bootstrap) {
        this(id, name, scope, restrictions, device, principalId, bootstrap, false);
    }

    /** The view of a stored row. */
    public static AuthenticatedToken of(ApiTokenEntity entity) {
        return new AuthenticatedToken(entity.getId(), entity.getName(), entity.getScope(), entity.getRestrictions(),
                entity.getDevice(), entity.getPrincipalId(), entity.isBootstrap(), entity.isPrivateAccess());
    }
}
