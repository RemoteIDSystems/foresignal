package ai.foresignal.security.user;

import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.ReadRestriction;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * What a user-owned API token may do (prompt R2-025): never more than its owner may now.
 *
 * <p>A token whose principal is a user's {@code person} principal is the user's. On every request it is checked
 * against the owner: an owner who may not sign in (suspended, disabled) makes it fail like a revoked token; its scope is
 * lowered to {@link #maxScope} of the owner's current permissions, and its private grant dropped unless the owner's
 * sessions have one; its reads are narrowed by the owner's data restriction. Role and restriction changes therefore
 * reach existing tokens on their next request, as they reach sessions.
 */
@Component
public class TokenOwnership {

    private final UserRepository users;
    private final PermissionResolver permissions;
    private final UserDataRestrictionService restrictions;

    /**
     * Creates the component.
     *
     * @param users the user store
     * @param permissions effective-permission resolution
     * @param restrictions the per-user data restrictions
     */
    public TokenOwnership(UserRepository users, PermissionResolver permissions,
            UserDataRestrictionService restrictions) {
        this.users = users;
        this.permissions = permissions;
        this.restrictions = restrictions;
    }

    /**
     * The widest scope a token of a user holding {@code permissions} may carry: {@code admin} only with every permission
     * in the catalog (an admin token passes every permission check, so anything less would let the token do more than
     * its owner); otherwise {@code agent} when the owner's sessions are {@code agent} or {@code admin}, else
     * {@code read}.
     *
     * @param permissions the owner's effective permissions
     * @return the widest scope
     */
    public static Scope maxScope(EffectivePermissions permissions) {
        boolean all = Arrays.stream(Permissions.values()).allMatch(permissions::has);
        if (all) {
            return Scope.ADMIN;
        }
        return permissions.scope() == Scope.READ ? Scope.READ : Scope.AGENT;
    }

    /**
     * Whether a user holding {@code permissions} may give a token the private grant: when their own sessions see
     * private sources (an {@code admin}-derived session).
     *
     * @param permissions the owner's effective permissions
     * @return true when allowed
     */
    public static boolean mayGrantPrivate(EffectivePermissions permissions) {
        return permissions.scope() == Scope.ADMIN;
    }

    /** A token after its owner check: the clamped token and its owner, or the token as it was and no owner. */
    public record Checked(AuthenticatedToken token, TokenOwner owner) {
    }

    /**
     * Checks a token against its owner.
     *
     * @param token the authenticated token
     * @return the token clamped to its owner with the owner, the token unchanged with no owner when no user owns it, or
     *     empty when its owner may not sign in
     */
    @Transactional(readOnly = true)
    public Optional<Checked> check(AuthenticatedToken token) {
        if (token.bootstrap() || token.principalId() == null) {
            return Optional.of(new Checked(token, null));
        }
        UserEntity user = users.findByPrincipalId(token.principalId()).orElse(null);
        if (user == null) {
            return Optional.of(new Checked(token, null));
        }
        if (!UserLifecycleService.canAuthenticate(user)) {
            return Optional.empty();
        }
        EffectivePermissions held = permissions.forUser(user.getId());
        Scope max = maxScope(held);
        Scope scope = max.includes(token.scope()) ? token.scope() : max;
        boolean privateAccess = token.privateAccess() && mayGrantPrivate(held);
        AuthenticatedToken clamped = scope == token.scope() && privateAccess == token.privateAccess() ? token
                : new AuthenticatedToken(token.id(), token.name(), scope, token.restrictions(), token.device(),
                        token.principalId(), false, privateAccess, token.licencePolicy());
        return Optional.of(new Checked(clamped, new TokenOwner(user.getId(), user.getEmail())));
    }

    /**
     * A token's read restriction narrowed by its owner's data restriction.
     *
     * @param restriction the token's restriction
     * @param owner the owner
     * @return the narrowed restriction
     */
    public ReadRestriction narrow(ReadRestriction restriction, TokenOwner owner) {
        return restrictions.narrow(restriction, owner.userId());
    }
}
