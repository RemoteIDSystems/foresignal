package ai.foresignal.api.support;

import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.SpecOperationIndex;
import ai.foresignal.security.audit.AuditRecord;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.security.restrict.RequestPrincipal;
import ai.foresignal.security.visibility.SourceVisibility;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

/**
 * Audits private-data reads by non-admin principals (prompt 039, §2 "Audit"): a {@code read} or
 * {@code agent} token, a ticket of one, or a feed key that holds the private grant and reads a data path
 * while private sources exist. One {@code audit_event} per request (target type
 * {@value AuditRecord#TARGET_PRIVATE_READ}); admin reads are not audited, and callers without the grant
 * never see private data, so they are not audited either.
 *
 * <p>Called by {@link ReadCallerResolver#resolve} on every read path; the request attribute
 * {@link #MARKER} keeps it to one entry per request. MCP tool calls are audited by the MCP server itself.
 */
@Component
public class PrivateReadAuditor {

    /** Set on a request once its private read was audited. */
    public static final String MARKER = "ai.foresignal.privateReadAudited";

    private final AuditService audit;
    private final SpecOperationIndex spec;
    private final Clock clock;

    /** Creates the auditor. */
    public PrivateReadAuditor(AuditService audit, SpecOperationIndex spec, Clock clock) {
        this.audit = audit;
        this.spec = spec;
        this.clock = clock;
    }

    /**
     * Records the read of {@code request} by {@code caller} when it can reach private data: the caller is
     * not admin, holds the private grant, and {@code privateSources} is not empty.
     */
    public void noteReader(HttpServletRequest request, ReadCaller caller, Set<String> privateSources) {
        if (request == null || caller == null || privateSources.isEmpty() || !caller.privateAccess()) {
            return;
        }
        if (caller.scope() == null || caller.scope().includes(Scope.ADMIN)) {
            return;
        }
        if (request.getAttribute(MARKER) != null) {
            return;
        }
        request.setAttribute(MARKER, Boolean.TRUE);
        Optional<CurrentToken> token = CurrentToken.of(request);
        Optional<RequestPrincipal> principal = RequestPrincipal.of(request);
        String path = normalize(request.getRequestURI());
        HttpMethod method;
        try {
            method = HttpMethod.valueOf(request.getMethod());
        } catch (IllegalArgumentException e) {
            method = HttpMethod.GET;
        }
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("privateSources", privateSources.size());
        principal.ifPresent(p -> detail.put("principalKind", p.kind().name().toLowerCase(java.util.Locale.ROOT)));
        token.filter(CurrentToken::viaTicket).ifPresent(t -> detail.put("viaTicket", true));
        audit.record(new AuditRecord(
                clock.instant(),
                AuditRecord.TOKEN,
                token.map(CurrentToken::id).orElse(principal.map(RequestPrincipal::id).orElse(null)),
                token.map(CurrentToken::name).orElse(principal.map(RequestPrincipal::name).orElse(null)),
                token.map(CurrentToken::principalId).orElse(null),
                caller.scope().wireValue(),
                method.name(),
                path,
                spec.operationId(method, path),
                null,
                AuditRecord.TARGET_PRIVATE_READ,
                null,
                request.getRemoteAddr(),
                detail));
    }

    /** True when {@code caller} could see private data at all ({@link SourceVisibility#canSeePrivate}). */
    public static boolean canReachPrivate(ReadCaller caller) {
        return SourceVisibility.canSeePrivate(caller.scope(), caller.privateAccess());
    }

    /** The request path without a trailing slash or duplicate slashes (as the access policy sees it). */
    static String normalize(String uri) {
        String path = uri == null ? "/" : uri.replaceAll("/{2,}", "/");
        return path.length() > 1 && path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }
}
