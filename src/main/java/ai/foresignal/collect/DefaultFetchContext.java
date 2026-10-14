package ai.foresignal.collect;

import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.InboxAccess;
import ai.foresignal.source.spi.MediaSink;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.RasterSink;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The {@link FetchContext} the collector hands an adapter for one fetch or probe.
 *
 * <p>One instance per fetch, never shared: its {@link #http()} is a fresh source-bound
 * {@link UpstreamClient} binding whose request traces belong to this fetch alone, which is what
 * lets the {@link FetchExecutor} build the fetch's {@code fetch_log} row from exactly the requests
 * it made. When probing, the binding also carries a {@link RequestRecorder}.
 *
 * <p>Parameters, credentials and the last success time are read lazily and at most once, so an
 * adapter that never asks for its credentials never causes them to be decrypted.
 *
 * <p>A {@link Mode#DERIVED} source gets a context whose {@link #http()} throws: it makes no upstream
 * requests at all (prompt 004).
 */
public class DefaultFetchContext implements FetchContext {

    private final String sourceId;
    private final SourceDescriptor descriptor;
    private final UpstreamClient upstream;
    private final RequestRecorder recorder;
    private final Supplier<ValidatedParams> params;
    private final CredentialsAccess credentials;
    private final Clock clock;
    private final Supplier<Optional<Instant>> lastSuccessAt;
    private final AdapterStateStore state;

    private Supplier<RasterSink> rasterSink;
    private Supplier<MediaSink> mediaSink;
    private Supplier<InboxAccess> inboxSupplier;
    private Consumer<java.util.List<ai.foresignal.source.spi.NormalizedRecord>> chunkSink;
    private MediaSink media;
    private SourceHttp http;
    private RasterSink rasters;
    private ValidatedParams resolvedParams;
    private Map<String, String> resolvedCredentials;
    private Optional<Instant> resolvedLastSuccess;

    /**
     * Creates a context.
     *
     * @param recorder captures exchanges for a probe, or null in normal operation
     * @param params supplies the validated effective parameters; called at most once
     * @param lastSuccessAt supplies the last success time; called at most once
     */
    public DefaultFetchContext(
            SourceDescriptor descriptor,
            UpstreamClient upstream,
            RequestRecorder recorder,
            Supplier<ValidatedParams> params,
            CredentialsAccess credentials,
            Clock clock,
            Supplier<Optional<Instant>> lastSuccessAt,
            AdapterStateStore state) {
        this.sourceId = descriptor.id();
        this.descriptor = descriptor;
        this.upstream = upstream;
        this.recorder = recorder;
        this.params = params;
        this.credentials = credentials;
        this.clock = clock;
        this.lastSuccessAt = lastSuccessAt;
        this.state = state;
    }

    @Override
    public String sourceId() {
        return sourceId;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /**
     * The source-bound HTTP client, created on first use.
     *
     * @throws UnsupportedOperationException for a {@link Mode#DERIVED} source
     */
    @Override
    public synchronized SourceHttp http() {
        if (descriptor.mode() == Mode.DERIVED) {
            throw new UnsupportedOperationException(
                    sourceId + " is a DERIVED source and makes no upstream requests, so it has no HTTP client.");
        }
        if (http == null) {
            http = upstream.forSource(sourceId, descriptor, recorder);
        }
        return http;
    }

    @Override
    public synchronized ValidatedParams params() {
        if (resolvedParams == null) {
            resolvedParams = params.get();
        }
        return resolvedParams;
    }

    @Override
    public synchronized Map<String, String> credentials() {
        if (resolvedCredentials == null) {
            resolvedCredentials = Map.copyOf(credentials.forSource(sourceId));
        }
        return resolvedCredentials;
    }

    @Override
    public Clock clock() {
        return clock;
    }

    @Override
    public synchronized Optional<Instant> lastSuccessAt() {
        if (resolvedLastSuccess == null) {
            Optional<Instant> value = lastSuccessAt.get();
            resolvedLastSuccess = value == null ? Optional.empty() : value;
        }
        return resolvedLastSuccess;
    }

    @Override
    public AdapterStateStore state() {
        return state;
    }

    /**
     * Sets where {@link #rasters()} comes from (prompt 042); the factory calls it when a raster
     * pipeline is installed.
     */
    public void rasterSink(Supplier<RasterSink> supplier) {
        this.rasterSink = supplier;
    }

    /**
     * The raster pipeline for this fetch, created on first use; probing when this context records a
     * probe.
     *
     * @throws UnsupportedOperationException when no raster pipeline is installed
     */
    @Override
    public synchronized RasterSink rasters() {
        if (rasters == null) {
            if (rasterSink == null) {
                return FetchContextDefaults.noRasters(sourceId);
            }
            rasters = rasterSink.get();
        }
        return rasters;
    }

    /**
     * Sets where {@link #media()} comes from (prompt 043); the factory calls it when a media store is
     * installed.
     */
    public void mediaSink(Supplier<MediaSink> supplier) {
        this.mediaSink = supplier;
    }

    /**
     * The media store for this fetch, created on first use; probing when this context records a
     * probe.
     *
     * @throws UnsupportedOperationException when no media store is installed
     */
    @Override
    public synchronized MediaSink media() {
        if (media == null) {
            if (mediaSink == null) {
                return FetchContextDefaults.noMedia(sourceId);
            }
            media = mediaSink.get();
        }
        return media;
    }

    /**
     * Sets where {@link #inbox()} comes from (prompt 049); the factory calls it when an inbox is
     * installed.
     */
    public void inboxAccess(Supplier<InboxAccess> supplier) {
        this.inboxSupplier = supplier;
    }

    /**
     * The inbox (prompt 049).
     *
     * @throws UnsupportedOperationException when no inbox is installed
     */
    @Override
    public InboxAccess inbox() {
        InboxAccess access = inboxSupplier == null ? null : inboxSupplier.get();
        if (access == null) {
            throw new UnsupportedOperationException("No inbox is available to " + sourceId + ".");
        }
        return access;
    }

    /**
     * Installs the chunk sink (prompt 094); the executor sets it before calling a reference adapter's
     * {@code fetch}, so {@link #emitChunk} hands each chunk to the ingest writer with back-pressure.
     */
    public void chunkSink(Consumer<java.util.List<ai.foresignal.source.spi.NormalizedRecord>> sink) {
        this.chunkSink = sink;
    }

    /** {@inheritDoc} */
    @Override
    public void emitChunk(List<ai.foresignal.source.spi.NormalizedRecord> records) {
        if (chunkSink == null) {
            throw new UnsupportedOperationException("No chunk sink is installed for " + sourceId + ".");
        }
        if (records != null && !records.isEmpty()) {
            chunkSink.accept(records);
        }
    }

    /** The probe recorder, or null. */
    public RequestRecorder recorder() {
        return recorder;
    }

    /**
     * The traces of every request this context's client made, oldest first; empty when the adapter
     * never used {@link #http()}.
     */
    public synchronized List<FetchTrace> traces() {
        return http instanceof BoundSourceHttp bound ? bound.traces() : List.of();
    }
}
