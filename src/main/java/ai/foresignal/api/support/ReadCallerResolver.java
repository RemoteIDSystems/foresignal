package ai.foresignal.api.support;

import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenFilter;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.stereotype.Component;

/**
 * Resolves the {@link ReadCaller} of a request (the scope {@code TokenFilter} recorded, plus the
 * {@code security} setting) and the sources that caller may not read.
 *
 * <p>Private sources (descriptor {@code private=true}) are hidden from anonymous {@code openReads}
 * callers on the read endpoints of prompt 014. Prompt 039 completes the enforcement on every path
 * through {@code EgressPolicy}.
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

    /** The sources {@code caller} may not read: every private source for an anonymous caller. */
    public Set<String> excludedSources(ReadCaller caller) {
        if (!caller.isAnonymous()) {
            return Set.of();
        }
        Set<String> excluded = new TreeSet<>();
        for (SourceDescriptor descriptor : registry.descriptors()) {
            if (descriptor.privateSource()) {
                excluded.add(descriptor.id());
            }
        }
        return Set.copyOf(excluded);
    }
}
