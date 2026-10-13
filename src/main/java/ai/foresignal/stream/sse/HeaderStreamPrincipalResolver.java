package ai.foresignal.stream.sse;

import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RequestPrincipal;

import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.TokenFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * The {@link StreamPrincipalResolver}: the token {@link TokenFilter} accepted for the request,
 * whether it came as {@code Authorization: Bearer} or as a stream ticket ({@code ?ticket=}, prompt
 * 023), keyed on the token's id ({@code token:<uuid>}). A ticket therefore resolves to the principal
 * of the token that minted it, so per-token stream limits, stream ownership and revocation apply to
 * ticket streams too. A request without an accepted token is anonymous and keyed on its remote
 * address.
 *
 * <p>Prompt 015 suggested a separate {@code @Primary} resolver for tickets; resolving both through
 * {@link CurrentToken} makes that unnecessary.
 */
@Component
public class HeaderStreamPrincipalResolver implements StreamPrincipalResolver {

    @Override
    public StreamPrincipal resolve(HttpServletRequest request) {
        ReadRestriction restriction = RequestPrincipal.restrictionOf(request);
        Optional<RequestPrincipal> principal = RequestPrincipal.of(request);
        if (principal.isPresent() && principal.get().isPathKey()) {
            // A feed key (prompt 035) or tile key (prompt 050) is its own principal, keyed on its id
            // (feed:<id>, tilekey:<id>), with the read scope.
            return new StreamPrincipal(principal.get().clientKey(), Scope.READ, request.getRemoteAddr(),
                    restriction, principal.get().privateAccess());
        }
        Optional<CurrentToken> current = CurrentToken.of(request);
        if (current.isEmpty()) {
            return StreamPrincipal.anonymous(request.getRemoteAddr()).withRestriction(restriction);
        }
        // The 039 private grant travels with the principal, so the hub filters private sources per client.
        return StreamPrincipal.forToken(current.get().id(), current.get().scope(), request.getRemoteAddr())
                .withRestriction(restriction).withPrivateAccess(current.get().privateAccess());
    }
}
