package ai.foresignal.chatops;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.ask.AskCaller;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.security.AccessPolicy;
import ai.foresignal.security.AccessRequirement;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import java.util.Set;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

/**
 * Turns a {@link ChatPrincipal} into the callers the services take and checks it against
 * {@link AccessPolicy} exactly like a token (prompt 061).
 */
@Component
public class ChatPrincipals {

    private final ReadCallerResolver readCallers;
    private final AppSettingService settings;
    private final AccessPolicy policy;

    /** Creates the component. */
    public ChatPrincipals(ReadCallerResolver readCallers, AppSettingService settings, AccessPolicy policy) {
        this.readCallers = readCallers;
        this.settings = settings;
        this.policy = policy;
    }

    /** The read-side caller: the principal's scope and private grant, no restriction. */
    public ReadCaller readCaller(ChatPrincipal principal) {
        SecuritySettings security =
                settings.getOrDefault(SettingKeys.SECURITY, SecuritySettings.class, SecuritySettings::defaults);
        return new ReadCaller(principal.scope(), security.exposeObserver(), security.openReads(), ReadRestriction.NONE,
                principal.privateAccess());
    }

    /** The private sources the principal may not see (the 039 gate, exactly as for a token). */
    public Set<String> excludedSources(ChatPrincipal principal) {
        return readCallers.excludedSources(readCaller(principal));
    }

    /** The principal as the MCP caller (023) that Ask, briefings and the tools take. */
    public McpCaller mcpCaller(ChatPrincipal principal) {
        ReadCaller read = readCaller(principal);
        return new McpCaller(principal.token(), read, readCallers.excludedSources(read), null);
    }

    /** The principal as an Ask caller. */
    public AskCaller askCaller(ChatPrincipal principal) {
        return new AskCaller(mcpCaller(principal));
    }

    /**
     * True when the principal may call {@code method} {@code path}: the {@link AccessPolicy} rule of the REST
     * operation a command mirrors, with the principal's scope standing in for a token's. Open rules pass, and
     * a chat principal is always authenticated, so {@code alwaysAuthenticated} rules pass on scope alone.
     */
    public boolean allows(ChatPrincipal principal, HttpMethod method, String path) {
        AccessRequirement requirement = policy.requirementFor(method, path);
        return switch (requirement) {
            case AccessRequirement.Open open -> true;
            case AccessRequirement.RequiresScope required -> principal.scope().includes(required.scope());
            case AccessRequirement.IngestKeyOrAdmin ingest -> false;
            case AccessRequirement.PeerKey peer -> false;
        };
    }
}
