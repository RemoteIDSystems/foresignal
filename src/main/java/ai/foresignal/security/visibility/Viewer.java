package ai.foresignal.security.visibility;

import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.restrict.RequestPrincipal;
import java.util.Optional;

/**
 * Who a record would be shown to, as far as private-source visibility cares (prompt 039): the caller's
 * scope, its private grant and its stored source restriction. {@code EgressPolicy} decides every path
 * from this.
 *
 * @param scope the scope, or null for an anonymous caller
 * @param privateAccess the 039 private grant ({@code privateAccess}) of the token or feed key
 * @param restrictions the token's stored restrictions ({@link TokenRestrictions#NONE} when none)
 */
public record Viewer(Scope scope, boolean privateAccess, TokenRestrictions restrictions) {

    /** An anonymous viewer never has the grant; null restrictions become none. */
    public Viewer {
        restrictions = TokenRestrictions.orNone(restrictions);
        if (scope == null) {
            privateAccess = false;
        }
    }

    /** An anonymous caller (open reads, system summaries shown to anyone). */
    public static Viewer anonymous() {
        return new Viewer(null, false, TokenRestrictions.NONE);
    }

    /** The operator: sees everything. */
    public static Viewer admin() {
        return new Viewer(Scope.ADMIN, false, TokenRestrictions.NONE);
    }

    /** The viewer behind {@code token} (empty: anonymous). */
    public static Viewer of(Optional<CurrentToken> token) {
        return token.map(t -> new Viewer(t.scope(), t.privateAccess(), t.restrictions())).orElseGet(Viewer::anonymous);
    }

    /** The viewer behind a request principal (a token or a feed key; anonymous when null). */
    public static Viewer of(RequestPrincipal principal) {
        if (principal == null || principal.isAnonymous()) {
            return anonymous();
        }
        return new Viewer(principal.scope(), principal.privateAccess(), TokenRestrictions.NONE);
    }

    /** True when this viewer may see private sources ({@link SourceVisibility#canSeePrivate(Scope, boolean)}). */
    public boolean canSeePrivate() {
        return SourceVisibility.canSeePrivate(scope, privateAccess);
    }

    /** True for an admin viewer. */
    public boolean isAdmin() {
        return scope != null && scope.includes(Scope.ADMIN);
    }
}
