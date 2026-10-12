package ai.foresignal.watch;

import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Maps a (source, identity) pair to the payload path that holds the identity (prompt 036), from the
 * descriptor's {@code entityKeys} (prompt 008: entity key type → top-level payload field).
 * Identity-bearing adapters must declare them (icao24, registration, callsign, mmsi, imo); a source
 * that does not declare an identity never matches a watchlist of it.
 */
@Component
public class IdentityResolver {

    private final SourceRegistry registry;

    /** Creates the resolver. */
    public IdentityResolver(SourceRegistry registry) {
        this.registry = registry;
    }

    /** The payload path of {@code identity} in {@code sourceId}'s records, or null when undeclared. */
    public String path(String sourceId, Identity identity) {
        if (identity == Identity.CUSTOM) {
            return null;
        }
        return registry.find(sourceId).map(r -> r.descriptor().entityKeys().get(identity.wire())).orElse(null);
    }

    /**
     * The paths of {@code identity} for {@code sources} (every registered source when empty); sources
     * without the identity are left out.
     */
    public Map<String, String> paths(Identity identity, Collection<String> sources) {
        Map<String, String> out = new LinkedHashMap<>();
        for (String id : ids(sources)) {
            String path = path(id, identity);
            if (path != null) {
                out.put(id, path);
            }
        }
        return out;
    }

    /** The sources among {@code sources} (every registered source when empty) without {@code identity}. */
    public List<String> without(Identity identity, Collection<String> sources) {
        List<String> out = new ArrayList<>();
        for (String id : ids(sources)) {
            if (path(id, identity) == null) {
                out.add(id);
            }
        }
        return out;
    }

    private Collection<String> ids(Collection<String> sources) {
        if (sources != null && !sources.isEmpty()) {
            return sources;
        }
        return registry.descriptors().stream().map(SourceDescriptor::id).sorted().toList();
    }
}
