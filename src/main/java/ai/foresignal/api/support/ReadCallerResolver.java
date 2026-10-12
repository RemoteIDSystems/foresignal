package ai.foresignal.api.support;

import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.SourceVisibilityGate;
import ai.foresignal.security.TokenFilter;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Resolves the {@link ReadCaller} of a request (the scope {@code TokenFilter} recorded, plus the
 * {@code security} setting) and the sources that caller may not read.
 *
 * <p>Private sources (descriptor {@code private=true}) are visible only to {@code agent} and
 * {@code admin} tokens ({@link SourceVisibilityGate}, prompt 023); anonymous callers and
 * {@code read} tokens never see them. Prompt 039 completes the enforcement on every path through
 * {@code EgressPolicy}.
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
        return new ReadCaller(scope, security.exposeObserver(), security.openReads());
    }

    /**
     * The sources {@code caller} may not read: every private source unless the caller holds an
     * {@code agent} or {@code admin} token.
     */
    public Set<String> excludedSources(ReadCaller caller) {
        return SourceVisibilityGate.excludedSources(registry, caller.scope(), TokenRestrictions.NONE);
    }

    /** The private sources the caller of {@code request} may not read, with its restrictions. */
    public Set<String> excludedSources(HttpServletRequest request) {
        TokenRestrictions restrictions = CurrentToken.of(request).map(CurrentToken::restrictions)
                .orElse(TokenRestrictions.NONE);
        return SourceVisibilityGate.excludedSources(registry, resolve(request).scope(), restrictions);
    }
}
