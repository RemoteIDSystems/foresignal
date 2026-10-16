package ai.foresignal.provenance;

import ai.foresignal.derive.DerivedGenerator;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceRegistryChangedEvent;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * The license terms of a registered source (prompt 109, task 1): the descriptor's explicit
 * {@code licenceTerms}, else the catalogue entry its free-text {@code licence} resolves to, and for a
 * {@code DERIVED} source the <strong>most restrictive</strong> terms of its inputs
 * ({@link LicenceCatalog#mostRestrictive}). The inputs are the descriptor's {@code derivedFrom} when declared,
 * else the source ids the generator subscribes to ({@link DerivedGenerator#inputs}); a derived source with no
 * resolvable inputs keeps its own free-text resolution, which for {@code "derived"} is the restrictive
 * {@code unknown} (fail closed).
 *
 * <p>Results are cached per source and dropped whenever the registry changes. Every provenance and egress
 * path ({@code EgressPolicy}, {@link LicenceResolver}, {@link AttributionService}) asks this class rather than
 * {@link LicenceCatalog#forDescriptor} so a derived source is never treated as less restricted than its inputs.
 */
@Component
public class SourceLicences {

    /** The deepest chain of derived-from-derived sources followed before giving up (fail closed). */
    static final int MAX_DEPTH = 4;

    private final SourceRegistry registry;
    private final LicenceCatalog catalog;
    private final ConcurrentHashMap<String, LicenceTerms> cache = new ConcurrentHashMap<>();

    /** Creates the resolver. */
    public SourceLicences(SourceRegistry registry, LicenceCatalog catalog) {
        this.registry = registry;
        this.catalog = catalog;
    }

    /** The catalogue this resolver reads. */
    public LicenceCatalog catalog() {
        return catalog;
    }

    /** Drops the cache when a source is registered, redefined or removed. */
    @EventListener
    public void onRegistryChanged(SourceRegistryChangedEvent event) {
        cache.clear();
    }

    /**
     * The terms of {@code sourceId}; {@code unknown} for an unregistered source.
     *
     * @param sourceId the source id, or null
     */
    public LicenceTerms termsOf(String sourceId) {
        if (sourceId == null || !registry.contains(sourceId)) {
            return catalog.terms(LicenceCatalog.UNKNOWN);
        }
        LicenceTerms cached = cache.get(sourceId);
        if (cached != null) {
            return cached;
        }
        LicenceTerms terms = resolve(sourceId, new HashSet<>(), 0);
        cache.put(sourceId, terms);
        return terms;
    }

    /** The terms of {@code descriptor}, following {@code derivedFrom} for a DERIVED source. */
    public LicenceTerms termsOf(SourceDescriptor descriptor) {
        if (descriptor == null) {
            return catalog.terms(LicenceCatalog.UNKNOWN);
        }
        if (registry.contains(descriptor.id()) && registry.descriptor(descriptor.id()) == descriptor) {
            return termsOf(descriptor.id());
        }
        return resolve(descriptor, new HashSet<>(), 0);
    }

    /**
     * The input source ids a DERIVED source's terms are inherited from: {@code derivedFrom} when declared,
     * else the generator's subscribed sources. Empty for any other source.
     */
    public List<String> inputsOf(String sourceId) {
        if (sourceId == null || !registry.contains(sourceId)) {
            return List.of();
        }
        SourceDescriptor descriptor = registry.descriptor(sourceId);
        if (descriptor == null || descriptor.mode() != Mode.DERIVED) {
            return List.of();
        }
        if (!descriptor.derivedFrom().isEmpty()) {
            return descriptor.derivedFrom();
        }
        return registry.find(sourceId)
                .flatMap(SourceRegistry.Registration::adapterIfPresent)
                .filter(DerivedGenerator.class::isInstance)
                .map(adapter -> generatorInputs((DerivedGenerator) adapter, sourceId))
                .orElse(List.of());
    }

    private static List<String> generatorInputs(DerivedGenerator generator, String self) {
        try {
            Set<String> sources = new LinkedHashSet<>(generator.inputs(ValidatedParams.empty()).sources());
            sources.remove(self);
            return List.copyOf(sources);
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    private LicenceTerms resolve(String sourceId, Set<String> seen, int depth) {
        SourceDescriptor descriptor = registry.descriptor(sourceId);
        if (descriptor == null) {
            return catalog.terms(LicenceCatalog.UNKNOWN);
        }
        return resolve(descriptor, seen, depth);
    }

    private LicenceTerms resolve(SourceDescriptor descriptor, Set<String> seen, int depth) {
        LicenceTerms own = catalog.forDescriptor(descriptor);
        if (descriptor.mode() != Mode.DERIVED || descriptor.licenceTerms() != null) {
            return own;
        }
        if (!seen.add(descriptor.id()) || depth >= MAX_DEPTH) {
            return catalog.terms(LicenceCatalog.UNKNOWN);
        }
        List<String> inputs = registry.contains(descriptor.id()) ? inputsOf(descriptor.id()) : descriptor.derivedFrom();
        if (inputs.isEmpty()) {
            return own;
        }
        Collection<LicenceTerms> terms = new ArrayList<>(inputs.size());
        for (String input : inputs) {
            terms.add(registry.contains(input) ? resolve(input, new HashSet<>(seen), depth + 1)
                    : catalog.terms(LicenceCatalog.UNKNOWN));
        }
        return catalog.mostRestrictive(terms);
    }
}
