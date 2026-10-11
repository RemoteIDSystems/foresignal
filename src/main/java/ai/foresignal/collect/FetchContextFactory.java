package ai.foresignal.collect;

import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.SourceDescriptor;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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

    /** Creates the factory. */
    public FetchContextFactory(
            UpstreamClient upstream,
            SourceService sources,
            CredentialsAccess credentials,
            UsageMeter usage,
            JdbcTemplate jdbc,
            Clock clock) {
        this.upstream = upstream;
        this.sources = sources;
        this.credentials = credentials;
        this.usage = usage;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /**
     * A context for one fetch.
     *
     * @param recorder captures exchanges when probing, or null
     */
    public DefaultFetchContext forFetch(SourceDescriptor descriptor, RequestRecorder recorder) {
        String id = descriptor.id();
        return new DefaultFetchContext(
                descriptor,
                upstream,
                recorder,
                () -> sources.effectiveParams(id),
                credentials,
                clock,
                () -> lastSuccessAt(id),
                upstream.stateStore(id));
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
        return new DefaultStreamContext(
                descriptor,
                upstream,
                recorder,
                () -> sources.effectiveParams(id),
                credentials,
                clock,
                () -> lastSuccessAt(id),
                upstream.stateStore(id),
                usage,
                callbacks);
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
