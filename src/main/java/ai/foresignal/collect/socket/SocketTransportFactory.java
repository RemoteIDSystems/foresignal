package ai.foresignal.collect.socket;

import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.source.spi.SocketTransport;
import ai.foresignal.source.spi.StreamContext;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Builds the {@link SocketTransport} of each stream context (prompt 069); {@code FetchContextFactory} wires
 * it behind {@link StreamContext#sockets()}.
 */
@Component
public class SocketTransportFactory {

    private final ObjectProvider<MeterRegistry> meters;
    private final ObjectProvider<OutboundUrlPolicy> policy;

    /**
     * Creates the factory.
     *
     * @param meters the registry of the {@code foresignal.socket.*} counters, resolved lazily
     * @param policy the outbound policy re-checking TCP destinations, resolved lazily
     */
    public SocketTransportFactory(ObjectProvider<MeterRegistry> meters, ObjectProvider<OutboundUrlPolicy> policy) {
        this.meters = meters;
        this.policy = policy;
    }

    /** The transport of {@code ctx}: budgeted and metered through it. */
    public SocketTransport forContext(StreamContext ctx) {
        MeterRegistry registry = meters == null ? null : meters.getIfAvailable();
        OutboundUrlPolicy outbound = policy == null ? null : policy.getIfAvailable();
        return new DefaultSocketTransport(ctx, new SocketMetrics(registry, ctx.sourceId()), outbound);
    }
}
