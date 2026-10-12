package ai.foresignal.collect;

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

    /** Creates the factory without a raster pipeline ({@code ctx.rasters()} throws). */
    public FetchContextFactory(
            UpstreamClient upstream,
            SourceService sources,
            CredentialsAccess credentials,
            UsageMeter usage,
            JdbcTemplate jdbc,
            Clock clock) {
        this(upstream, sources, credentials, usage, jdbc, clock, null);
    }

    /**
     * Creates the factory.
     *
     * @param rasters the raster pipeline behind {@code ctx.rasters()} (prompt 042), resolved lazily;
     *     null for none
     */
    @org.springframework.beans.factory.annotation.Autowired
    public FetchContextFactory(
            UpstreamClient upstream,
            SourceService sources,
            CredentialsAccess credentials,
            UsageMeter usage,
            JdbcTemplate jdbc,
            Clock clock,
            ObjectProvider<RasterSinkFactory> rasters) {
        this.upstream = upstream;
        this.sources = sources;
        this.credentials = credentials;
        this.usage = usage;
        this.jdbc = jdbc;
        this.clock = clock;
        this.rasters = rasters;
    }

    /** Wires {@code ctx.rasters()} to the pipeline, probing when the context records a probe. */
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
                upstream.stateStore(id)), descriptor, recorder != null);
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
                upstream.stateStore(id),
                usage,
                callbacks), descriptor, recorder != null);
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
