package ai.foresignal.source.spi;

/**
 * Where running sources register their {@link RemoteMediaReceiver} (prompt 054); implemented by
 * {@code inbound.RemoteMediaRegistry}, which {@code POST /v1/ingest/{sourceId}/media} consults.
 *
 * <p>Adapters depend only on SPI types (Architecture §3), so a SatDump station reaches the endpoint
 * through this interface rather than the endpoint reaching into the adapter.
 */
public interface RemoteMediaReceivers {

    /** Registers {@code receiver} for {@code sourceId}, replacing any earlier one. */
    void register(String sourceId, RemoteMediaReceiver receiver);

    /** Removes {@code receiver} for {@code sourceId} when it is still the registered one. */
    void unregister(String sourceId, RemoteMediaReceiver receiver);
}
