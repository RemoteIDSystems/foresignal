package ai.foresignal.api.support;

import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenFilter;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RequestPrincipal;
import ai.foresignal.security.restrict.RestrictionClamp;
import ai.foresignal.security.visibility.PrivateFilter;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.stereotype.Component;

/**
 * Resolves the {@link ReadCaller} of a request (the scope {@code TokenFilter} recorded, plus the
 * {@code security} setting) and the sources that caller may not read.
 *
 * <p>Private sources ({@link SourceVisibility}: descriptor flag or operator tightening) are visible
 * only to {@code admin} tokens and to tokens, feed keys and tickets created with the private grant
 * ({@code privateAccess}, prompt 039); anonymous callers never see them. The excluded set this
 * resolver returns is the caller's {@link PrivateFilter} plus its restriction, and is what every
 * read path ({@code EgressPolicy}'s API, tile, SSE, search, MCP, stats and alert paths) passes to its
 * query builder.
 *
 * <p>From prompt 035 the caller also carries its effective {@link ReadRestriction} (from the
 * {@link RequestPrincipal} the filters bound): a restriction with {@code excludePrivate} hides every
 * private source, and sources outside its source member are added to the excluded set, so every
 * path that already honors excluded sources honors the source restriction too. Kind and area
 * restrictions are applied by {@link RestrictionClamp} predicates.
 */
@Component
public class ReadCallerResolver {

    private final AppSettingService settings;
    private final SourceRegistry registry;
    private final SourceVisibility visibility;
    private final org.springframework.beans.factory.ObjectProvider<ai.foresignal.security.restrict.ReadScopes> scopes;
    private final org.springframework.beans.factory.ObjectProvider<PrivateReadAuditor> auditor;

    /** Creates the resolver. */
    @org.springframework.beans.factory.annotation.Autowired
    public ReadCallerResolver(AppSettingService settings, SourceRegistry registry, SourceVisibility visibility,
            org.springframework.beans.factory.ObjectProvider<ai.foresignal.security.restrict.ReadScopes> scopes,
            org.springframework.beans.factory.ObjectProvider<PrivateReadAuditor> auditor) {
        this.settings = settings;
        this.registry = registry;
        this.visibility = visibility;
        this.scopes = scopes;
        this.auditor = auditor;
    }

    /** A resolver without request scopes or auditing (tests). */
    public ReadCallerResolver(AppSettingService settings, SourceRegistry registry, SourceVisibility visibility) {
        this(settings, registry, visibility, null, null);
    }

    /**
     * The caller of {@code request} with the request's {@code place=}, {@code view=} and
     * {@code inWatchlist=} parameters folded into its restriction (prompt 036). {@code radiusKm}
     * buffers the places only when the request has no {@code near}.
     */
    public ReadCaller resolveScoped(HttpServletRequest request) {
        return resolveScoped(request, scopeOf(request, ""));
    }

    /** {@link #resolveScoped(HttpServletRequest)} with an explicit scope. */
    public ReadCaller resolveScoped(HttpServletRequest request, ai.foresignal.security.restrict.ReadScope scope) {
        ReadCaller caller = resolve(request);
        if (scope == null || scope.isEmpty()) {
            return caller;
        }
        ai.foresignal.security.restrict.ReadScopes resolver = scopes == null ? null : scopes.getIfAvailable();
        if (resolver == null) {
            return caller;
        }
        ReadRestriction narrowed = resolver.narrow(caller.restriction(), scope, tokenPresent(request));
        return caller.withRestriction(narrowed);
    }

    /** True when the request carries a token (not anonymous and not a feed key). */
    public static boolean tokenPresent(HttpServletRequest request) {
        return RequestPrincipal.of(request).map(p -> p.kind() == RequestPrincipal.Kind.TOKEN).orElse(false);
    }

    /**
     * The narrowing parameters of {@code request} with {@code prefix} ({@code ""}, or
     * {@code "changes."} on the event stream).
     */
    public static ai.foresignal.security.restrict.ReadScope scopeOf(HttpServletRequest request, String prefix) {
        String[] places = request.getParameterValues(prefix + "place");
        java.util.List<String> ids = new java.util.ArrayList<>();
        if (places != null) {
            for (String value : places) {
                for (String part : value.split(",")) {
                    if (!part.isBlank()) {
                        ids.add(part.strip());
                    }
                }
            }
        }
        String near = request.getParameter(prefix + "near");
        String radius = request.getParameter(prefix + "radiusKm");
        Double radiusKm = null;
        if (!ids.isEmpty() && (near == null || near.isBlank()) && radius != null && !radius.isBlank()) {
            radiusKm = QueryParams.doubleInRange("radiusKm", radius, 0, NearParser.MAX_RADIUS_KM);
        }
        return new ai.foresignal.security.restrict.ReadScope(ids, radiusKm, request.getParameter(prefix + "view"),
                request.getParameter(prefix + "inWatchlist"));
    }

    /**
     * {@code radiusKm} for the {@code near} parser: absent when it buffers {@code place} instead
     * (a request with places and no {@code near}).
     */
    public static String radiusForNear(HttpServletRequest request, String near, String radiusKm) {
        if ((near == null || near.isBlank()) && !scopeOf(request, "").placeIds().isEmpty()) {
            return null;
        }
        return radiusKm;
    }

    /** The caller of {@code request}. */
    public ReadCaller resolve(HttpServletRequest request) {
        Object attribute = request.getAttribute(TokenFilter.SCOPE_ATTRIBUTE);
        Optional<RequestPrincipal> principal = RequestPrincipal.of(request);
        Scope scope = attribute instanceof Scope s ? s : null;
        boolean privateAccess = principal.map(RequestPrincipal::privateAccess).orElse(false);
        SecuritySettings security =
                settings.getOrDefault(SettingKeys.SECURITY, SecuritySettings.class, SecuritySettings::defaults);
        ReadCaller caller = new ReadCaller(scope, security.exposeObserver(), security.openReads(),
                RequestPrincipal.restrictionOf(request), privateAccess);
        PrivateReadAuditor audit = auditor == null ? null : auditor.getIfAvailable();
        if (audit != null) {
            audit.noteReader(request, caller, visibility.privateSourceIds());
        }
        return caller;
    }

    /** The effective read restriction of {@code request} ({@link ReadRestriction#NONE} when none). */
    public ReadRestriction restriction(HttpServletRequest request) {
        return RequestPrincipal.restrictionOf(request);
    }

    /**
     * The sources {@code caller} may not read: every private source unless the caller is admin or holds
     * the private grant, plus what its restriction hides.
     */
    public Set<String> excludedSources(ReadCaller caller) {
        return withRestriction(privateFilter(caller).hidden(), caller.restriction());
    }

    /** The private-source filter of {@code caller} (prompt 039; its restriction is not included). */
    public PrivateFilter privateFilter(ReadCaller caller) {
        return visibility.filterFor(caller.scope(), caller.privateAccess(), TokenRestrictions.NONE);
    }

    /**
     * The private sources {@code caller} may not read (the 023 gate plus {@code excludePrivate}),
     * without the restriction's source member. A request naming one of these is answered 404;
     * naming a source that is merely outside the restriction yields an empty result (prompt 035:
     * intersect, never union).
     */
    public Set<String> hiddenSources(ReadCaller caller) {
        ReadRestriction r = caller.restriction();
        return withRestriction(privateFilter(caller).hidden(),
                new ReadRestriction(null, null, null, null, null, r.excludePrivate(), null));
    }

    /** The private sources the caller of {@code request} may not read, with its restrictions. */
    public Set<String> excludedSources(HttpServletRequest request) {
        TokenRestrictions restrictions = CurrentToken.of(request).map(CurrentToken::restrictions)
                .orElse(TokenRestrictions.NONE);
        ReadCaller caller = resolve(request);
        return withRestriction(visibility.hiddenFor(caller.scope(), caller.privateAccess(), restrictions),
                RequestPrincipal.restrictionOf(request));
    }

    /**
     * {@code excluded} plus what {@code restriction} hides: every private source when it excludes
     * private sources, and every source outside its source member.
     */
    public Set<String> withRestriction(Set<String> excluded, ReadRestriction restriction) {
        return withRestriction(registry, visibility, excluded, restriction);
    }

    /** {@link #withRestriction(Set, ReadRestriction)} over {@code registry} and {@code visibility}. */
    public static Set<String> withRestriction(SourceRegistry registry, SourceVisibility visibility,
            Set<String> excluded, ReadRestriction restriction) {
        if (restriction == null || (!restriction.excludePrivate() && restriction.sources() == null)) {
            return excluded;
        }
        Set<String> out = new TreeSet<>(excluded);
        if (restriction.excludePrivate()) {
            out.addAll(visibility.privateSourceIds());
        }
        out.addAll(RestrictionClamp.excludedBy(restriction, registry.ids()));
        return Set.copyOf(out);
    }
}
