package ai.foresignal.mapping;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.inbound.CustomSourceEntity;
import ai.foresignal.inbound.CustomSourceRepository;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The compiled mapping of each custom source (prompt 059), recompiled whenever the stored mapping text or the
 * source's kinds or time base change, so an edited mapping applies to the next push.
 */
@Component
public class MappingCache {

    private final CustomSourceRepository repository;
    private final MappingCompiler compiler;
    private final JsonMapper json;
    private final Map<String, Entry> cache = new ConcurrentHashMap<>();

    /** One cached compilation and what it was compiled from. */
    private record Entry(String key, CompiledMapping compiled) {}

    /** Creates the cache. */
    public MappingCache(CustomSourceRepository repository, MappingCompiler compiler, JsonMapper json) {
        this.repository = repository;
        this.compiler = compiler;
        this.json = json;
    }

    /**
     * The stored mapping of {@code descriptor}'s source, compiled strictly against it.
     *
     * @return the mapping, or empty when the source stores none
     * @throws MappingInvalidException when the stored mapping no longer compiles (for example after a kind was
     *     removed)
     */
    public Optional<CompiledMapping> stored(SourceDescriptor descriptor) {
        Optional<CustomSourceEntity> row = repository.findById(descriptor.id());
        String text = row.map(CustomSourceEntity::getMapping).orElse(null);
        if (text == null || text.isBlank()) {
            cache.remove(descriptor.id());
            return Optional.empty();
        }
        return Optional.of(compiled(descriptor.id(), text, null, descriptor));
    }

    /**
     * The stored mapping, else {@code fallback} (a preset's mapping), compiled against {@code descriptor}.
     *
     * @throws InvalidInputException 400 when there is neither
     */
    public CompiledMapping require(SourceDescriptor descriptor, JsonNode fallback) {
        Optional<CompiledMapping> stored = stored(descriptor);
        if (stored.isPresent()) {
            return stored.get();
        }
        if (fallback == null) {
            throw new InvalidInputException("The source " + descriptor.id() + " has no mapping; set its mapping"
                    + " member (PUT /v1/custom-sources/{id}).");
        }
        return compiled(descriptor.id() + "#preset", json.writeValueAsString(fallback), fallback, descriptor);
    }

    private CompiledMapping compiled(String cacheKey, String text, JsonNode node, SourceDescriptor descriptor) {
        String key = text + "|" + descriptor.kinds() + "|" + descriptor.observedAtMode();
        Entry entry = cache.get(cacheKey);
        if (entry != null && entry.key().equals(key)) {
            return entry.compiled();
        }
        JsonNode mapping = node != null ? node : json.readTree(text);
        CompiledMapping compiled = compiler.compile(mapping, MappingContext.of(descriptor, true));
        cache.put(cacheKey, new Entry(key, compiled));
        return compiled;
    }
}
