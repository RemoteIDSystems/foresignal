package ai.foresignal.api.support;

import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.SourceVisibilityGate;
import ai.foresignal.security.TokenFilter;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RequestPrincipal;
import ai.foresignal.security.restrict.RestrictionClamp;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.stereotype.Component;

/**
 * Resolves the {@link ReadCaller} of a request (the scope {@code TokenFilter} recorded, plus the
 * {@code security} setting) and the sources that caller may not read.
 *
 * <p>Private sources (descriptor {@code private=true}) are visible only to {@code agent} and
 * {@code admin} tokens ({@link SourceVisibilityGate}, prompt 023); anonymous callers and
 * {@code read} tokens never see them. Prompt 039 completes the enforcement on every path through
 * {@code EgressPolicy}.
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

    /** Creates the resolver. */
    public ReadCallerResolver(AppSettingService settings, SourceRegistry registry) {
        this.settings = settings;
        this.registry = registry;
    }

    /** The caller of {@code request}. */
    public ReadCaller resolve(HttpServletRequest request) {
        Object attribute = request.getAttribute(TokenFilter.SCOPE_ATTRIBUTE);
        Scope scope = attribute instanceof Scope s ? s : null;
        SecuritySettings security =
                settings.getOrDefault(SettingKeys.SECURITY, SecuritySettings.class, SecuritySettings::defaults);
        return new ReadCaller(scope, security.exposeObserver(), security.openReads(),
                RequestPrincipal.restrictionOf(request));
    }

    /** The effective read restriction of {@code request} ({@link ReadRestriction#NONE} when none). */
    public ReadRestriction restriction(HttpServletRequest request) {
        return RequestPrincipal.restrictionOf(request);
    }

    /**
     * The sources {@code caller} may not read: every private source unless the caller holds an
     * {@code agent} or {@code admin} token.
     */
    public Set<String> excludedSources(ReadCaller caller) {
        return withRestriction(SourceVisibilityGate.excludedSources(registry, caller.scope(), TokenRestrictions.NONE),
                caller.restriction());
    }

    /**
     * The private sources {@code caller} may not read (the 023 gate plus {@code excludePrivate}),
     * without the restriction's source member. A request naming one of these is answered 404;
     * naming a source that is merely outside the restriction yields an empty result (prompt 035:
     * intersect, never union).
     */
    public Set<String> hiddenSources(ReadCaller caller) {
        ReadRestriction r = caller.restriction();
        return withRestriction(SourceVisibilityGate.excludedSources(registry, caller.scope(), TokenRestrictions.NONE),
                new ReadRestriction(null, null, null, null, null, r.excludePrivate(), null));
    }

    /** The private sources the caller of {@code request} may not read, with its restrictions. */
    public Set<String> excludedSources(HttpServletRequest request) {
        TokenRestrictions restrictions = CurrentToken.of(request).map(CurrentToken::restrictions)
                .orElse(TokenRestrictions.NONE);
        return withRestriction(SourceVisibilityGate.excludedSources(registry, resolve(request).scope(), restrictions),
                RequestPrincipal.restrictionOf(request));
    }

    /**
     * {@code excluded} plus what {@code restriction} hides: every private source when it excludes
     * private sources, and every source outside its source member.
     */
    public Set<String> withRestriction(Set<String> excluded, ReadRestriction restriction) {
        return withRestriction(registry, excluded, restriction);
    }

    /** {@link #withRestriction(Set, ReadRestriction)} over {@code registry}. */
    public static Set<String> withRestriction(SourceRegistry registry, Set<String> excluded,
            ReadRestriction restriction) {
        if (restriction == null || (!restriction.excludePrivate() && restriction.sources() == null)) {
            return excluded;
        }
        Set<String> out = new TreeSet<>(excluded);
        if (restriction.excludePrivate()) {
            registry.descriptors().stream().filter(d -> d.privateSource()).forEach(d -> out.add(d.id()));
        }
        out.addAll(RestrictionClamp.excludedBy(restriction, registry.ids()));
        return Set.copyOf(out);
    }
}
