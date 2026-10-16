package ai.foresignal.api;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.export.ExportJobService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RequestPrincipal;
import ai.foresignal.security.visibility.Viewer;
import jakarta.servlet.http.HttpServletRequest;

/** Builds the {@link ExportJobService.Caller} of an export, snapshot or pack request (prompt 111). */
final class ExportCallers {

    private ExportCallers() {}

    /** The caller of {@code request}: its token id, admin flag, egress viewer and read restriction. */
    static ExportJobService.Caller of(HttpServletRequest request, ReadCallerResolver callers) {
        ReadCaller caller = callers.resolve(request);
        RequestPrincipal principal = RequestPrincipal.of(request).orElse(null);
        boolean admin = caller.scope() != null && caller.scope().includes(Scope.ADMIN);
        return new ExportJobService.Caller(tokenId(request), admin, Viewer.of(principal), caller.restriction());
    }

    /** The token id of {@code request}, or null. */
    static String tokenId(HttpServletRequest request) {
        return RequestPrincipal.of(request).map(p -> p.id() == null ? null : p.id().toString()).orElse(null);
    }

    /** True when the request carries an admin token. */
    static boolean admin(HttpServletRequest request, ReadCallerResolver callers) {
        ReadCaller caller = callers.resolve(request);
        return caller.scope() != null && caller.scope().includes(Scope.ADMIN);
    }
}
