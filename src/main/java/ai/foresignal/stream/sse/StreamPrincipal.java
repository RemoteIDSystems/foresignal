package ai.foresignal.stream.sse;

import ai.foresignal.security.Scope;
import java.util.Objects;

/**
 * Who opened a stream, resolved once at connect by a {@link StreamPrincipalResolver}.
 *
 * <p>Per-principal stream limits and stream ownership (who may PATCH a filter) key on {@link #id},
 * never on a raw token, so prompt 023's reconnect-aware stream tickets can resolve to the same
 * principal as the token that issued them.
 *
 * @param id the stable principal id: {@code token:<hash prefix>} for a token, {@code anon:<address>}
 *     for an anonymous caller
 * @param scope the token's scope, or null for an anonymous caller
 * @param remoteAddress the caller's remote address
 */
public record StreamPrincipal(String id, Scope scope, String remoteAddress) {

    /** Requires an id. */
    public StreamPrincipal {
        Objects.requireNonNull(id, "id");
    }

    /** An anonymous caller from {@code remoteAddress}. */
    public static StreamPrincipal anonymous(String remoteAddress) {
        String address = remoteAddress == null ? "unknown" : remoteAddress;
        return new StreamPrincipal("anon:" + address, null, address);
    }

    /** True when no token was presented. */
    public boolean anonymous() {
        return scope == null;
    }

    /**
     * True when the principal may use something that needs {@code required}. An anonymous caller
     * (admitted under {@code security.openReads}) counts as {@code read}.
     */
    public boolean allows(Scope required) {
        if (scope == null) {
            return required == Scope.READ;
        }
        return scope.includes(required);
    }
}
