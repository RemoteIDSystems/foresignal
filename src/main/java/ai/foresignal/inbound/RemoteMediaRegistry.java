package ai.foresignal.inbound;

import ai.foresignal.source.spi.RemoteMediaReceiver;
import ai.foresignal.source.spi.RemoteMediaReceivers;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * The receivers of pushed images, one per running source (prompt 054). A SatDump station registers
 * while its stream runs and unregisters when it stops; {@code POST /v1/ingest/{sourceId}/media} finds the
 * receiver here.
 */
@Component
public class RemoteMediaRegistry implements RemoteMediaReceivers {

    private final Map<String, RemoteMediaReceiver> receivers = new ConcurrentHashMap<>();

    @Override
    public void register(String sourceId, RemoteMediaReceiver receiver) {
        receivers.put(sourceId, receiver);
    }

    @Override
    public void unregister(String sourceId, RemoteMediaReceiver receiver) {
        receivers.remove(sourceId, receiver);
    }

    /** The receiver of a running source, if any. */
    public Optional<RemoteMediaReceiver> find(String sourceId) {
        return Optional.ofNullable(receivers.get(sourceId));
    }
}
