package ai.foresignal.security;

import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.stereotype.Component;

/**
 * The private-source gate hook (prompt 023): a source whose descriptor has {@code private=true}
 * (prompt 008) is visible only to {@code agent} and {@code admin} tokens whose source restriction
 * (if any) includes it. Anonymous callers and {@code read} tokens never see it.
 *
 * <p>Applied now to source listings ({@code /v1/sources}) and wherever records, changes, streams,
 * alerts and tiles resolve their source filter: private sources are excluded, or {@code 404} when
 * named explicitly. Prompt 039 folds this into {@code EgressPolicy} and extends it to every path
 * (search, embeddings, LLM egress, exports).
 */
@Component
public class SourceVisibilityGate {

    private final SourceRegistry registry;

    /** Creates the gate. */
    public SourceVisibilityGate(SourceRegistry registry) {
        this.registry = registry;
    }

    /**
     * True when {@code sourceId} is visible to {@code token} (empty for an anonymous caller). An
     * unknown source is reported visible, so the caller's own "unknown source" handling applies.
     */
    public boolean visible(String sourceId, Optional<CurrentToken> token) {
        Optional<SourceDescriptor> descriptor = registry.find(sourceId).map(SourceRegistry.Registration::descriptor);
        if (descriptor.isEmpty() || !descriptor.get().privateSource()) {
            return true;
        }
        return token.map(t -> privateVisible(sourceId, t.scope(), t.restrictions())).orElse(false);
    }

    /** The private sources a caller of {@code scope} (null when anonymous) with {@code restrictions} may not see. */
    public Set<String> excludedSources(Scope scope, TokenRestrictions restrictions) {
        return excludedSources(registry, scope, restrictions);
    }

    /** {@link #excludedSources(Scope, TokenRestrictions)} over {@code registry}, for components without the gate. */
    public static Set<String> excludedSources(SourceRegistry registry, Scope scope, TokenRestrictions restrictions) {
        Set<String> excluded = new TreeSet<>();
        for (SourceDescriptor descriptor : registry.descriptors()) {
            if (descriptor.privateSource() && !privateVisible(descriptor.id(), scope, restrictions)) {
                excluded.add(descriptor.id());
            }
        }
        return Set.copyOf(excluded);
    }

    /** True when private source {@code sourceId} is visible to a caller of {@code scope} with {@code restrictions}. */
    public static boolean privateVisible(String sourceId, Scope scope, TokenRestrictions restrictions) {
        if (scope == null || !scope.includes(Scope.AGENT)) {
            return false;
        }
        TokenRestrictions r = TokenRestrictions.orNone(restrictions);
        return r.sources() == null || r.sources().contains(sourceId);
    }
}
