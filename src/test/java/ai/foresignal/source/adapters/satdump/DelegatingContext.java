package ai.foresignal.source.adapters.satdump;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.InboxAccess;
import ai.foresignal.source.spi.MediaSink;
import ai.foresignal.source.spi.RasterSink;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/** A {@link StreamContext} that forwards everything to another, for tests that override one method. */
class DelegatingContext implements StreamContext {

    private final StreamContext delegate;

    /** Wraps {@code delegate}. */
    DelegatingContext(StreamContext delegate) {
        this.delegate = delegate;
    }

    @Override
    public String sourceId() {
        return delegate.sourceId();
    }

    @Override
    public SourceDescriptor descriptor() {
        return delegate.descriptor();
    }

    @Override
    public SourceHttp http() {
        return delegate.http();
    }

    @Override
    public ValidatedParams params() {
        return delegate.params();
    }

    @Override
    public Map<String, String> credentials() {
        return delegate.credentials();
    }

    @Override
    public Clock clock() {
        return delegate.clock();
    }

    @Override
    public Optional<Instant> lastSuccessAt() {
        return delegate.lastSuccessAt();
    }

    @Override
    public AdapterStateStore state() {
        return delegate.state();
    }

    @Override
    public RasterSink rasters() {
        return delegate.rasters();
    }

    @Override
    public MediaSink media() {
        return delegate.media();
    }

    @Override
    public InboxAccess inbox() {
        return delegate.inbox();
    }

    @Override
    public void chargeRequest(String label) throws AdapterException {
        delegate.chargeRequest(label);
    }

    @Override
    public void recordReceivedBytes(long bytes) {
        delegate.recordReceivedBytes(bytes);
    }

    @Override
    public void connected() {
        delegate.connected();
    }

    @Override
    public void disconnected(Throwable error) {
        delegate.disconnected(error);
    }
}
