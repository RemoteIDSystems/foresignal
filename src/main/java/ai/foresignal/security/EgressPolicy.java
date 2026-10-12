package ai.foresignal.security;

import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.RecordRow;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import org.springframework.stereotype.Component;

/**
 * The one component that decides whether a record may leave Foresignal on a given path
 * ({@code docs/Conventions.md}, "Shared components"). Prompts 026 (LLM context), 039 (private
 * sources everywhere), 062 ({@code LOCAL_ONLY} egress) and 109 (licence policy) each extend this
 * class; no parallel policy class may exist.
 *
 * <p><strong>LLM-context path (prompt 026).</strong> Every caller that puts record content into a
 * prompt passes the records through {@link #filter(Collection, LlmDestination)} (or the generic
 * {@link #filter(Collection, Function, LlmDestination)}) first: Ask (028), briefings (029),
 * enrichment (030) and search (027). A record from a source whose descriptor has {@code private=true}
 * (prompt 008's flag) is dropped unless the destination is local: an {@code ollama} provider, or one
 * flagged {@code options.local=true}. A record whose source is not registered is dropped too, since
 * its flag cannot be read (fail closed). Prompt 039 hardens this path (the {@code allowPrivateLocal}
 * setting, EGR-1).
 *
 * <p><strong>Search and embedding path (prompt 027).</strong> {@link #allowsEmbedding(String)} decides
 * which sources' records may be embedded and so become searchable semantically: a private source's
 * records never are (Gap-Analysis §1 item 7), and neither are records of an unregistered source.
 * Search results are further limited to the sources the caller may see.
 *
 * <p><strong>Ask (prompt 028).</strong> {@link #llmExcludedSources(LlmDestination)} names the sources
 * whose records Ask's tools must not read for a non-local provider; every tool result is filtered
 * again with {@link #allowsLlmContext} before it reaches the provider.
 */
@Component
public class EgressPolicy {

    /**
     * An LLM provider as an egress destination.
     *
     * @param providerId the provider id
     * @param type the provider type wire value ({@code anthropic}, {@code openai-compatible}, {@code ollama})
     * @param local true when prompts stay on this machine or network (Ollama, or {@code options.local})
     */
    public record LlmDestination(String providerId, String type, boolean local) {

        /** Validates the members. */
        public LlmDestination {
            Objects.requireNonNull(providerId, "providerId");
            Objects.requireNonNull(type, "type");
        }
    }

    private final SourceRegistry registry;

    /** Creates the policy. */
    public EgressPolicy(SourceRegistry registry) {
        this.registry = registry;
    }

    /**
     * The records of {@code records} that may be sent to {@code destination} as LLM context, in their
     * original order.
     */
    public List<RecordRow> filter(Collection<RecordRow> records, LlmDestination destination) {
        return filter(records, RecordRow::sourceId, destination);
    }

    /**
     * The items of {@code records} that may be sent to {@code destination} as LLM context, in their
     * original order; {@code sourceIdOf} names each item's source.
     */
    public <T> List<T> filter(Collection<T> records, Function<? super T, String> sourceIdOf,
            LlmDestination destination) {
        Objects.requireNonNull(destination, "destination");
        List<T> out = new ArrayList<>(records.size());
        for (T record : records) {
            if (allowsLlmContext(sourceIdOf.apply(record), destination)) {
                out.add(record);
            }
        }
        return out;
    }

    /** True when records of {@code sourceId} may be sent to {@code destination} as LLM context. */
    public boolean allowsLlmContext(String sourceId, LlmDestination destination) {
        if (sourceId == null) {
            return false;
        }
        Optional<SourceDescriptor> descriptor = registry.find(sourceId).map(SourceRegistry.Registration::descriptor);
        if (descriptor.isEmpty()) {
            return false;
        }
        return !descriptor.get().privateSource() || destination.local();
    }

    /**
     * The registered sources whose records may not be sent to {@code destination} as LLM context:
     * every private source unless the destination is local (prompt 028). Ask adds these to the
     * sources its tools exclude, so withheld records are never even read for the model; tool output
     * is then checked item by item with {@link #allowsLlmContext}.
     */
    public Set<String> llmExcludedSources(LlmDestination destination) {
        Objects.requireNonNull(destination, "destination");
        if (destination.local()) {
            return Set.of();
        }
        Set<String> out = new TreeSet<>();
        for (SourceDescriptor descriptor : registry.descriptors()) {
            if (descriptor.privateSource()) {
                out.add(descriptor.id());
            }
        }
        return Set.copyOf(out);
    }

    /**
     * True when records of {@code sourceId} may be embedded into {@code embedding_document} (the
     * search path, prompt 027): the source is registered and not private. Whether its descriptor
     * declares {@code embedFields} and its {@code embed} setting is on is decided by the embedding
     * worker on top of this.
     */
    public boolean allowsEmbedding(String sourceId) {
        if (sourceId == null) {
            return false;
        }
        return registry.find(sourceId)
                .map(SourceRegistry.Registration::descriptor)
                .map(descriptor -> !descriptor.privateSource())
                .orElse(false);
    }
}
