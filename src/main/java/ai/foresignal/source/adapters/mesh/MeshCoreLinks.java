package ai.foresignal.source.adapters.mesh;

import ai.foresignal.source.spi.LanEndpoint;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * The MeshCore companion links by {@code host:port} (prompt 083): one {@link MeshCoreCompanionLink} per companion, so the
 * {@code meshcore-local} source and prompt 084's {@code meshcore} channel share the single TCP client a companion
 * accepts.
 */
@Component
public class MeshCoreLinks {

    private final Clock clock;
    private final Map<String, MeshCoreCompanionLink> links = new ConcurrentHashMap<>();

    /**
     * Creates the registry.
     *
     * @param clock the receive-time clock of every link
     */
    public MeshCoreLinks(Clock clock) {
        this.clock = clock;
    }

    /** The link of {@code endpoint}'s pinned address and port, created on first use. */
    public MeshCoreCompanionLink link(LanEndpoint endpoint) {
        return links.computeIfAbsent(key(endpoint), k -> new MeshCoreCompanionLink(endpoint, clock));
    }

    /** The key of an endpoint: its pinned address and port. */
    static String key(LanEndpoint endpoint) {
        return endpoint.address().getHostAddress() + ":" + endpoint.port();
    }

    /** The number of links (tests, diagnostics). */
    public int size() {
        return links.size();
    }
}
