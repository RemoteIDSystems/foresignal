package ai.foresignal.collect;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * The only path from Foresignal to an upstream.
 *
 * <p>Adapters never build an {@link HttpClient}; they are handed a {@link SourceHttp} already bound
 * to their source, so the budget, the backoff, the User-Agent, the byte cap, the metering and the
 * redaction apply whether or not the adapter's author remembered them. That is what makes Product
 * Charter G2 — "no source ever exceeds its configured budget" — a property of the code.
 *
 * <p>Two shared clients, because redirect policy is per-client in {@code java.net.http}: the
 * default one follows redirects, and a second does not, for ranged reads, S3 listings and
 * pinned-IP LAN requests where a redirect would defeat the point.
 */
@Component
public class UpstreamClient {

    private static final Logger log = LoggerFactory.getLogger(UpstreamClient.class);

    static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(20);

    static {
        allowHostHeaderForPinnedRequests();
    }

    /**
     * Allows the {@code Host} header to be set.
     *
     * <p>{@code java.net.http} refuses to set {@code Host} unless this property names it, and a
     * pinned-IP LAN request needs exactly that: connect to the validated address, but send the Host
     * the operator configured, so a receiver serving several names still answers correctly.
     *
     * <p><strong>The JDK reads this property once,</strong> when its internal HTTP utilities first
     * initialize — which the first {@code HttpClient} or {@code HttpRequest} anywhere in the JVM
     * triggers, not necessarily ours. Loading this class sets it, and
     * {@code ForesignalApplication.main} calls this method before Spring starts so that no library can
     * win the race. When something does win it anyway, {@code BoundSourceHttp} sends the request
     * without the header rather than failing the fetch: connecting to the pinned address is the
     * protection, and the Host is a courtesy to multi-name receivers.
     */
    public static void allowHostHeaderForPinnedRequests() {
        String existing = System.getProperty("jdk.httpclient.allowRestrictedHeaders", "");
        if (!existing.toLowerCase().contains("host")) {
            String merged = existing.isBlank() ? "host" : existing + ",host";
            System.setProperty("jdk.httpclient.allowRestrictedHeaders", merged);
        }
    }

    private final HttpClient followingRedirects;
    private final HttpClient noRedirects;

    private final BudgetGovernor budgets;
    private final BackoffRegistry backoff;
    private final UsageMeter usage;
    private final InstallIdService install;
    private final CredentialsAccess credentials;
    private final SourceService sources;
    private final JdbcTemplate jdbc;
    private final JsonMapper jsonMapper;
    private final ForesignalProperties properties;
    private final Clock clock;

    public UpstreamClient(
            BudgetGovernor budgets,
            BackoffRegistry backoff,
            UsageMeter usage,
            InstallIdService install,
            CredentialsAccess credentials,
            SourceService sources,
            JdbcTemplate jdbc,
            JsonMapper jsonMapper,
            ForesignalProperties properties,
            Clock clock) {
        this.budgets = budgets;
        this.backoff = backoff;
        this.usage = usage;
        this.install = install;
        this.credentials = credentials;
        this.sources = sources;
        this.jdbc = jdbc;
        this.jsonMapper = jsonMapper;
        this.properties = properties;
        this.clock = clock;

        this.followingRedirects = build(HttpClient.Redirect.NORMAL);
        this.noRedirects = build(HttpClient.Redirect.NEVER);
    }

    private static HttpClient build(HttpClient.Redirect redirect) {
        return HttpClient.newBuilder()
                // Preferred, not required: HttpClient negotiates down to 1.1 when an upstream does
                // not speak HTTP/2, which many of these feeds do not.
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(redirect)
                // One virtual thread per in-flight request: a fetch that is waiting on a slow
                // upstream must not hold a platform thread.
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .build();
    }

    /**
     * A {@link SourceHttp} bound to one source.
     *
     * @param recorder captures exchanges for a probe, or null in normal operation
     * @throws UnsupportedOperationException for a {@link Mode#DERIVED} source, which makes no
     *     upstream requests at all
     */
    public SourceHttp forSource(String sourceId, SourceDescriptor descriptor, RequestRecorder recorder) {
        if (descriptor.mode() == Mode.DERIVED) {
            throw new UnsupportedOperationException(
                    sourceId
                            + " is a DERIVED source: it is computed from data Foresignal already holds and"
                            + " makes no upstream requests, so it has no HTTP client.");
        }
        return new BoundSourceHttp(
                this,
                sourceId,
                descriptor,
                recorder,
                redactorFor(sourceId),
                stateStore(sourceId),
                pinnedHosts(sourceId, descriptor));
    }

    /**
     * A {@link SourceHttp} for an upstream user that is not a registered source (prompt 040: the reference
     * data loader), with the fixed {@code budget}. Everything else is as for an adapter: the budget, the
     * backoff, the User-Agent, the byte cap, the metering and the redaction.
     *
     * @param descriptor a descriptor naming the user (its id meters usage and backoff)
     * @param budget the request budget
     */
    public SourceHttp forSystem(SourceDescriptor descriptor, ai.foresignal.source.spi.Budget budget) {
        budgets.fixBudget(descriptor.id(), budget);
        return forSource(descriptor.id(), descriptor, null);
    }

    /** A {@link SourceHttp} with no probe recorder. */
    public SourceHttp forSource(String sourceId, SourceDescriptor descriptor) {
        return forSource(sourceId, descriptor, null);
    }

    /** The persistent adapter state for a source. */
    public PersistentAdapterStateStore stateStore(String sourceId) {
        return new PersistentAdapterStateStore(sourceId, jdbc, jsonMapper);
    }

    /**
     * A redactor loaded with this source's credential values.
     *
     * <p>Built from the real values rather than from parameter names, because an upstream is free to
     * call its key anything and a name-based guess would miss it.
     */
    UrlRedactor redactorFor(String sourceId) {
        try {
            Map<String, String> values = credentials.forSource(sourceId);
            return new UrlRedactor(values.values());
        } catch (RuntimeException e) {
            // Without the values nothing can be masked, so fail closed on logging rather than on
            // fetching: a trace with an unmasked key is worse than a trace with none.
            log.warn(
                    "Could not load {}'s credentials for redaction; traces will omit URLs", sourceId, e);
            return null;
        }
    }

    /**
     * The addresses this source's {@code LAN_URL} parameters resolved to, keyed by host.
     *
     * <p>Requests to one of these hosts connect to the pinned address rather than re-resolving the
     * name. Re-resolving is exactly the DNS rebinding hole the validation in prompt 004 exists to
     * close: a name that resolved to 192.168.1.50 when the operator saved it can resolve to
     * anything on the next lookup.
     */
    Map<String, InetAddress> pinnedHosts(String sourceId, SourceDescriptor descriptor) {
        boolean anyLanParams =
                descriptor.params().stream().anyMatch(spec -> spec.type() == ParamType.LAN_URL);
        if (!anyLanParams) {
            return Map.of();
        }
        try {
            var params = sources.effectiveParams(sourceId);
            Map<String, InetAddress> byHost = new LinkedHashMap<>();
            for (ParamSpec spec : descriptor.params()) {
                if (spec.type() != ParamType.LAN_URL) {
                    continue;
                }
                URI uri = params.lanUrl(spec.name());
                var pinned = params.pinnedAddress(spec.name());
                if (uri != null && uri.getHost() != null && pinned.isPresent()) {
                    byHost.put(uri.getHost().toLowerCase(), pinned.get());
                }
            }
            return Map.copyOf(byHost);
        } catch (RuntimeException e) {
            // Without a pinned address a LAN request would re-resolve the host, so refuse to build
            // the map rather than quietly dropping the protection.
            log.warn("Could not resolve the pinned LAN addresses for {}", sourceId, e);
            return Map.of();
        }
    }

    HttpClient client(boolean allowRedirects) {
        return allowRedirects ? followingRedirects : noRedirects;
    }

    BudgetGovernor budgets() {
        return budgets;
    }

    BackoffRegistry backoff() {
        return backoff;
    }

    UsageMeter usage() {
        return usage;
    }

    Clock clock() {
        return clock;
    }

    /** The User-Agent for {@code descriptor}: its override if it has one, else Foresignal's own. */
    String userAgent(SourceDescriptor descriptor) {
        String override = descriptor.userAgentOverride();
        return override == null || override.isBlank() ? install.userAgent() : override;
    }

    /**
     * The URL a public bucket listing is requested from, virtual-hosted style.
     *
     * <p>Package-private and overridable rather than inlined at the call site so a test can point a
     * listing at a local stub; production has exactly one implementation of it.
     */
    URI s3ListUri(String bucket, String query) {
        return URI.create("https://" + bucket + ".s3.amazonaws.com/" + query);
    }

    /** Where {@link SourceHttp#download} writes, per Weather-Imagery-Design §2.2. */
    java.nio.file.Path rasterTempDir() {
        return properties.dataDir().resolve("rasters").resolve("tmp");
    }

    /** Wraps a checked failure so callers see one adapter-facing exception type. */
    static AdapterException io(String message, Throwable cause) {
        return new UpstreamIOException(message, cause);
    }
}
