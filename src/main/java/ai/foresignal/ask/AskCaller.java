package ai.foresignal.ask;

import ai.foresignal.mcp.McpCaller;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Who is asking: the token {@code TokenFilter} accepted (Ask is always authenticated, so normally
 * present), seen through the same {@link McpCaller} the MCP tools take, so Ask's tools apply exactly
 * the scope, restriction and private-source rules of MCP.
 *
 * @param mcp the caller as the tools see it
 */
public record AskCaller(McpCaller mcp) {

    /** Requires the caller. */
    public AskCaller {
        Objects.requireNonNull(mcp, "mcp");
    }

    /** The token, or null for an anonymous caller. */
    public AuthenticatedToken token() {
        return mcp.token();
    }

    /** The token id, or null. */
    public UUID tokenId() {
        return mcp.token() == null ? null : mcp.token().id();
    }

    /** The token's scope, or null when anonymous. */
    public Scope scope() {
        return mcp.scope();
    }

    /** True for an {@code admin} token, which sees every thread and action. */
    public boolean admin() {
        return scope() == Scope.ADMIN;
    }

    /** True when the caller may execute actions: {@code agent} scope or above. */
    public boolean mayAct() {
        return scope() != null && scope().includes(Scope.AGENT);
    }

    /** The actor recorded on writes ({@code admin} or {@code token:<name>}). */
    public String actor() {
        return mcp.actor();
    }

    /** The private sources the caller may not see. */
    public Set<String> excludedSources() {
        return mcp.excludedSources();
    }

    /**
     * True when the caller may see a thread or action owned by {@code ownerTokenId} (null for an
     * anonymous owner): its own, or any for an admin.
     */
    public boolean owns(UUID ownerTokenId) {
        return admin() || Objects.equals(ownerTokenId, tokenId());
    }

    /** The caller's read restriction (prompt 035): the tool loop and retrieval run inside it. */
    public ai.foresignal.security.restrict.ReadRestriction restriction() {
        return mcp.restriction();
    }

    /** The same caller with {@code excluded} sources hidden from the tools as well. */
    public McpCaller toolCaller(Set<String> excluded) {
        if (excluded.isEmpty()) {
            return mcp;
        }
        Set<String> all = new TreeSet<>(mcp.excludedSources());
        all.addAll(excluded);
        return new McpCaller(mcp.token(), mcp.readCaller(), all, mcp.remoteIp());
    }
}
