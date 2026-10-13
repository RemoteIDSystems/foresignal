package ai.foresignal.collect;

import ai.foresignal.source.spi.AdapterStateStore;

import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.SourceDescriptor;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Builds the contexts adapters are given: {@link DefaultFetchContext} for fetches and polling
 * probes, {@link DefaultStreamContext} for streams and streaming probes.
 */
@Component
public class FetchContextFactory {

    private final UpstreamClient upstream;
    private final SourceService sources;
    private final CredentialsAccess credentials;
    private final UsageMeter usage;
    private final JdbcTemplate jdbc;
    private final Clock clock;
    private final ObjectProvider<RasterSinkFactory> rasters;
    private final ObjectProvider<MediaSinkFactory> media;

    /** Creates the factory without a raster pipeline ({@code ctx.rasters()} throws). */
    public FetchContextFactory(
            UpstreamClient upstream,
            SourceService sources,
            CredentialsAccess credentials,
            UsageMeter usage,
            JdbcTemplate jdbc,
            Clock clock) {
        this(upstream, sources, credentials, usage, jdbc, clock, null, null);
    }

    /**
     * Creates the factory without a media store ({@code ctx.media()} throws).
     *
     * @param rasters the raster pipeline behind {@code ctx.rasters()} (prompt 042), resolved lazily;
     *     null for none
     */
    public FetchContextFactory(
            UpstreamClient upstream,
            SourceService sources,
            CredentialsAccess credentials,
            UsageMeter usage,
            JdbcTemplate jdbc,
            Clock clock,
            ObjectProvider<RasterSinkFactory> rasters) {
        this(upstream, sources, credentials, usage, jdbc, clock, rasters, null);
    }

    /**
     * Creates the factory.
     *
     * @param rasters the raster pipeline behind {@code ctx.rasters()} (prompt 042), resolved lazily;
     *     null for none
     * @param media the media store behind {@code ctx.media()} (prompt 043), resolved lazily; null for
     *     none
     */
    @org.springframework.beans.factory.annotation.Autowired
    public FetchContextFactory(
            UpstreamClient upstream,
            SourceService sources,
            CredentialsAccess credentials,
            UsageMeter usage,
            JdbcTemplate jdbc,
            Clock clock,
            ObjectProvider<RasterSinkFactory> rasters,
            ObjectProvider<MediaSinkFactory> media) {
        this.upstream = upstream;
        this.sources = sources;
        this.credentials = credentials;
        this.usage = usage;
        this.jdbc = jdbc;
        this.clock = clock;
        this.rasters = rasters;
        this.media = media;
    }

    /**
     * Wires {@code ctx.rasters()} to the pipeline and {@code ctx.media()} to the media store, probing
     * when the context records a probe.
     */
    private <T extends DefaultFetchContext> T withRasters(T ctx, SourceDescriptor descriptor, boolean probing) {
        if (rasters != null) {
            ctx.rasterSink(() -> {
                RasterSinkFactory factory = rasters.getIfAvailable();
                if (factory == null) {
                    return FetchContextDefaults.noRasters(descriptor.id());
                }
                return factory.forSource(descriptor, probing);
            });
        }
        return withMedia(ctx, descriptor, probing);
    }

    /** Wires {@code ctx.media()} to the media store, probing when the context records a probe. */
    private <T extends DefaultFetchContext> T withMedia(T ctx, SourceDescriptor descriptor, boolean probing) {
        if (media != null) {
            ctx.mediaSink(() -> {
                MediaSinkFactory factory = media.getIfAvailable();
                if (factory == null) {
                    return FetchContextDefaults.noMedia(descriptor.id());
                }
                return factory.forSource(descriptor, probing);
            });
        }
        return ctx;
    }

    /**
     * A context for one fetch.
     *
     * @param recorder captures exchanges when probing, or null
     */
    public DefaultFetchContext forFetch(SourceDescriptor descriptor, RequestRecorder recorder) {
        String id = descriptor.id();
        return withRasters(new DefaultFetchContext(
                descriptor,
                upstream,
                recorder,
                () -> sources.effectiveParams(id),
                credentials,
                clock,
                () -> lastSuccessAt(id),
                stateFor(id, recorder)), descriptor, recorder != null);
    }

    /**
     * A context for one stream connection.
     *
     * @param recorder captures exchanges when probing, or null
     * @param callbacks the supervisor's connection callbacks, or null for none
     */
    public DefaultStreamContext forStream(
            SourceDescriptor descriptor, RequestRecorder recorder, DefaultStreamContext.Callbacks callbacks) {
        String id = descriptor.id();
        return withRasters(new DefaultStreamContext(
                descriptor,
                upstream,
                recorder,
                () -> sources.effectiveParams(id),
                credentials,
                clock,
                () -> lastSuccessAt(id),
                stateFor(id, recorder),
                usage,
                callbacks), descriptor, recorder != null);
    }

    /**
     * The adapter state for one fetch or stream: the persistent store, or, while probing, an overlay
     * that keeps the probe's writes in memory (prompt 047).
     */
    AdapterStateStore stateFor(String sourceId, RequestRecorder recorder) {
        AdapterStateStore persistent = upstream.stateStore(sourceId);
        return recorder == null ? persistent : new ProbeStateOverlay(persistent);
    }

    /** {@code source_health.last_success_at}, or empty when the source has never succeeded. */
    Optional<Instant> lastSuccessAt(String sourceId) {
        List<Timestamp> rows =
                jdbc.query(
                        "select last_success_at from source_health where source_id = ?",
                        (rs, row) -> rs.getTimestamp(1),
                        sourceId);
        return rows.isEmpty() || rows.get(0) == null ? Optional.empty() : Optional.of(rows.get(0).toInstant());
    }
}
