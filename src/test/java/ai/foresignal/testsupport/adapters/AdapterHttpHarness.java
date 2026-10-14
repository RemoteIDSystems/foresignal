package ai.foresignal.testsupport.adapters;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.collect.BackoffRegistry;
import ai.foresignal.collect.BudgetGovernor;
import ai.foresignal.collect.DefaultFetchContext;
import ai.foresignal.collect.RequestRecorder;
import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.collect.UsageMeter;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.MutableClock;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * A real {@link UpstreamClient} (budget, backoff, usage, User-Agent, redaction) around a {@link MutableClock}, for
 * adapter tests against WireMock that need no scheduler (prompt 064; the setup of {@code OpenSkyAdapterTest}).
 */
public final class AdapterHttpHarness {

    /** The clock. */
    public final MutableClock clock;

    /** The backoff registry the client uses. */
    public final BackoffRegistry backoff;

    /** The client. */
    public final UpstreamClient client;

    /** The usage meter the client records into. */
    public final UsageMeter usage;

    /** The credentials the context returns; tests put values here. */
    public final Map<String, String> credentials = new HashMap<>();

    private final CredentialsAccess credentialsAccess;

    /**
     * Builds the harness.
     *
     * @param budget the budget every source gets
     */
    public AdapterHttpHarness(JdbcTemplate jdbc, JsonMapper json, ForesignalProperties properties,
            InstallIdService install, MutableClock clock, Budget budget) {
        this.clock = clock;
        SourceService sources = mock(SourceService.class);
        credentialsAccess = mock(CredentialsAccess.class);
        when(credentialsAccess.forSource(anyString())).thenAnswer(i -> Map.copyOf(credentials));
        when(sources.effectiveBudget(anyString())).thenReturn(budget);
        when(sources.effectiveParams(anyString())).thenReturn(ValidatedParams.empty());
        usage = new UsageMeter(jdbc, clock);
        backoff = new BackoffRegistry(jdbc, clock);
        client = new UpstreamClient(new BudgetGovernor(sources, usage, clock), backoff, usage, install,
                credentialsAccess, sources, jdbc, json, properties, clock);
    }

    /** Clears backoff and the given adapter-state keys of {@code sourceId}. */
    public void reset(String sourceId, String... stateKeys) {
        backoff.onSuccess(sourceId);
        for (String key : stateKeys) {
            client.stateStore(sourceId).remove(key);
        }
    }

    /** A fetch context for {@code adapter} with {@code params}. */
    public DefaultFetchContext context(SourceAdapter adapter, Map<String, Object> params) {
        return context(adapter, params, null);
    }

    /** A fetch context for {@code adapter} with {@code params}, recording exchanges into {@code recorder}. */
    public DefaultFetchContext context(SourceAdapter adapter, Map<String, Object> params, RequestRecorder recorder) {
        return context(adapter, params, recorder, null);
    }

    /**
     * A fetch context whose {@code lastSuccessAt()} is {@code lastSuccess} (prompt 065: the FIRMS high-water mark
     * advances only after a successful run).
     *
     * @param lastSuccess the source's last success, or null for none
     */
    public DefaultFetchContext context(SourceAdapter adapter, Map<String, Object> params, RequestRecorder recorder,
            java.time.Instant lastSuccess) {
        return new DefaultFetchContext(adapter.descriptor(), client, recorder, () -> ValidatedParams.of(params),
                credentialsAccess, clock, () -> Optional.ofNullable(lastSuccess), client.stateStore(adapter.id()));
    }

    /**
     * A fetch context whose chunk sink is {@code chunkSink} (prompt 094: a chunked reference adapter), so
     * {@code ctx.emitChunk} hands each chunk to the test instead of to the ingest writer.
     */
    public DefaultFetchContext context(SourceAdapter adapter, Map<String, Object> params, RequestRecorder recorder,
            java.time.Instant lastSuccess,
            java.util.function.Consumer<java.util.List<ai.foresignal.source.spi.NormalizedRecord>> chunkSink) {
        DefaultFetchContext ctx = context(adapter, params, recorder, lastSuccess);
        ctx.chunkSink(chunkSink);
        return ctx;
    }
}
