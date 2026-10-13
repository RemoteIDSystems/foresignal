package ai.foresignal.testsupport.weather;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.collect.BackoffRegistry;
import ai.foresignal.collect.BudgetGovernor;
import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.collect.UsageMeter;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.AreaOfInterest;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CountyGeometries;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.MutableClock;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.locationtech.jts.geom.Geometry;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Plumbing for the prompt 047 weather adapter tests: a real {@link UpstreamClient} (budget, backoff,
 * usage, User-Agent) pointed at WireMock, a hand-built {@link FetchContext}, an in-memory adapter
 * state, a settable {@link AreaOfInterest} and a stub {@link CountyGeometries}.
 */
public final class WeatherAdapterHarness {

    private WeatherAdapterHarness() {}

    /**
     * A real upstream client and its collaborators.
     *
     * @param client the client
     * @param backoff its backoff registry
     * @param usage its usage meter
     */
    public record Upstream(UpstreamClient client, BackoffRegistry backoff, UsageMeter usage) {}

    /**
     * A real upstream client whose budget and parameters come from a mocked source service.
     *
     * @param budget the effective budget
     * @param s3Endpoint the path-style S3 endpoint, or null for none
     */
    public static Upstream upstream(JdbcTemplate jdbc, JsonMapper jsonMapper, ForesignalProperties properties,
            InstallIdService install, MutableClock clock, Budget budget, String s3Endpoint) {
        SourceService sources = mock(SourceService.class);
        CredentialsAccess credentials = mock(CredentialsAccess.class);
        when(credentials.forSource(anyString())).thenReturn(Map.of());
        when(sources.effectiveBudget(anyString())).thenReturn(budget);
        when(sources.effectiveParams(anyString())).thenReturn(ValidatedParams.empty());
        UsageMeter usage = new UsageMeter(jdbc, clock);
        BackoffRegistry backoff = new BackoffRegistry(jdbc, clock);
        BudgetGovernor budgets = new BudgetGovernor(sources, usage, clock);
        UpstreamClient client = new UpstreamClient(budgets, backoff, usage, install, credentials, sources, jdbc, jsonMapper,
                properties, clock);
        if (s3Endpoint != null) {
            client.setS3Endpoint(s3Endpoint);
        }
        return new Upstream(client, backoff, usage);
    }

    /**
     * A {@link SourceHttp} that marks every GET response truncated, as the client does when a body hits
     * {@code maxResponseBytes}.
     */
    public static SourceHttp truncating(SourceHttp delegate) {
        return (SourceHttp) java.lang.reflect.Proxy.newProxyInstance(SourceHttp.class.getClassLoader(),
                new Class<?>[] {SourceHttp.class}, (proxy, method, args) -> {
                    Object result;
                    try {
                        result = method.invoke(delegate, args);
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        throw e.getCause();
                    }
                    if (result instanceof ai.foresignal.source.spi.UpstreamResponse r && r.status() == 200) {
                        byte[] half = java.util.Arrays.copyOf(r.body(), r.body().length / 2);
                        return new ai.foresignal.source.spi.UpstreamResponse(r.status(), r.headers(), half, r.contentType(),
                                true, false);
                    }
                    return result;
                });
    }

    /** In-memory adapter state. */
    public static final class MapState implements AdapterStateStore {

        private final Map<String, String> values = new ConcurrentHashMap<>();

        @Override
        public Optional<String> get(String key) {
            return Optional.ofNullable(values.get(key));
        }

        @Override
        public void put(String key, String value) {
            values.put(key, value);
        }

        @Override
        public void remove(String key) {
            values.remove(key);
        }

        /** Everything stored. */
        public Map<String, String> asMap() {
            return Map.copyOf(values);
        }
    }

    /** A fetch context. */
    public record Context(SourceDescriptor descriptor, SourceHttp http, ValidatedParams params, AdapterStateStore state,
            Clock clock) implements FetchContext {

        @Override
        public String sourceId() {
            return descriptor.id();
        }

        @Override
        public Map<String, String> credentials() {
            return Map.of();
        }

        @Override
        public Optional<Instant> lastSuccessAt() {
            return Optional.empty();
        }
    }

    /** An AOI whose envelope a test sets; every change bumps the version. */
    public static final class TestAoi implements AreaOfInterest {

        private volatile double[] envelope;
        private final AtomicLong version = new AtomicLong(1);
        private final java.util.Set<String> consumers = ConcurrentHashMap.newKeySet();

        /** An AOI with {@code envelope}. */
        public TestAoi(double... envelope) {
            this.envelope = envelope.clone();
        }

        /** Moves the AOI. */
        public void set(double... envelope) {
            this.envelope = envelope.clone();
            version.incrementAndGet();
        }

        @Override
        public double[] envelope() {
            return envelope.clone();
        }

        @Override
        public long version() {
            return version.get();
        }

        @Override
        public void registerConsumer(String sourceId) {
            consumers.add(sourceId);
        }

        /** The registered consumers. */
        public java.util.Set<String> consumers() {
            return java.util.Set.copyOf(consumers);
        }
    }

    /** Counties that always answer {@code geometry}. */
    public static CountyGeometries counties(Geometry geometry, java.util.List<Collection<String>> calls) {
        return new CountyGeometries() {
            @Override
            public boolean available() {
                return true;
            }

            @Override
            public Optional<Geometry> union(Collection<String> fips5) {
                calls.add(java.util.List.copyOf(fips5));
                return Optional.ofNullable(geometry);
            }
        };
    }
}
