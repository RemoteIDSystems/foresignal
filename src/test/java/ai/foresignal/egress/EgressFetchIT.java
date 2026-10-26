package ai.foresignal.egress;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.collect.BackoffRegistry;
import ai.foresignal.collect.BudgetGovernor;
import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.collect.UsageMeter;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.adapters.FakePollingAdapter;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.net.InetAddress;
import java.net.URI;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.json.JsonMapper;

/**
 * Egress routing in the collector (prompt R2-018), end to end through {@link UpstreamClient}.
 *
 * <p>Two stub servers stand in for two HTTP proxies, each answering for the upstream itself. The upstream's host,
 * {@value #UPSTREAM_HOST}, does not resolve here, so a request that reaches a proxy stub can only have been sent
 * through that proxy, and a request sent direct would fail to connect. The stubs' own logs are the evidence.
 *
 * <p>Asserted: {@code direct} keeps today's path; a pull leaves through the selected route and round-robin moves the
 * next pull to the next route; a 429 with {@code Retry-After} through one route holds the source off that route only,
 * not the source (so the next pull leaves through the other route at once) and not other sources; a pinned LAN host is
 * never routed, so its DNS pinning holds; the host pacer is keyed per route; with rotation on and no usable route the
 * pull fails and nothing is sent direct.
 */
@IntegrationTest
class EgressFetchIT extends EgressItSupport {

    static final String UPSTREAM_HOST = "upstream.egress.invalid";
    private static final String SOURCE = "egress-fetch-test";
    private static final Instant START = Instant.parse("2026-03-01T12:00:00Z");

    @RegisterExtension
    static final WireMockExtension PROXY_A = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().gzipDisabled(true)).build();

    @RegisterExtension
    static final WireMockExtension PROXY_B = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().gzipDisabled(true)).build();

    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;
    @Autowired SourceRegistry registry;
    @Autowired SourceService sourceService;

    private MutableClock clock;
    private SourceService sources;
    private BackoffRegistry backoff;
    private BudgetGovernor budgets;
    private UsageMeter usage;
    private CredentialsAccess credentials;
    private EgressRoute a;
    private EgressRoute b;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(START);
        sources = mock(SourceService.class);
        credentials = mock(CredentialsAccess.class);
        when(credentials.forSource(anyString())).thenReturn(Map.of());
        when(sources.effectiveBudget(anyString())).thenReturn(Budget.of(100, 1000));
        when(sources.effectiveParams(anyString())).thenReturn(ValidatedParams.empty());
        usage = new UsageMeter(jdbc, clock);
        backoff = new BackoffRegistry(jdbc, clock);
        budgets = new BudgetGovernor(sources, usage, clock);

        a = httpRoute("a-proxy", PROXY_A.getPort());
        b = httpRoute("b-proxy", PROXY_B.getPort());
        PROXY_A.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/data"))
                .willReturn(aResponse().withStatus(200).withBody("via A")));
        PROXY_B.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/data"))
                .willReturn(aResponse().withStatus(200).withBody("via B")));
    }

    // ---------- harness ----------

    private UpstreamClient client() {
        UpstreamClient client = new UpstreamClient(
                budgets, backoff, usage, install, credentials, sources, jdbc, jsonMapper, properties, clock);
        client.setEgressRouter(router);
        return client;
    }

    /** A new pull of {@code sourceId}: a fresh {@code SourceHttp}, as each scheduled fetch gets. */
    private SourceHttp pull(String sourceId) {
        return client().forSource(sourceId, FakePollingAdapter.defaultDescriptor(sourceId).build(), null);
    }

    private static URI upstream() {
        return URI.create("http://" + UPSTREAM_HOST + "/data");
    }

    private static int count(WireMockExtension server) {
        return server.findAll(getRequestedFor(urlPathEqualTo("/data"))).size();
    }

    // ---------- tests ----------

    @Test
    @DisplayName("direct (the default) sends from the server's own address, ignoring the pool")
    void directIsTodaysPath() throws Exception {
        WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/data"))
                .willReturn(aResponse().withStatus(200).withBody("direct")));

        assertThat(pull(SOURCE).get(URI.create(url("/data"))).bodyAsString()).isEqualTo("direct");

        assertThat(count(PROXY_A) + count(PROXY_B)).isZero();
    }

    @Test
    @DisplayName("a routed pull leaves through the selected proxy, and round-robin moves the next pull on")
    void pullUsesTheSelectedProxy() throws Exception {
        rotation(EgressSettings.ROUND_ROBIN);

        SourceHttp first = pull(SOURCE);
        assertThat(first.get(upstream()).bodyAsString()).isEqualTo("via A");
        assertThat(first.get(upstream()).bodyAsString())
                .as("one pull keeps one route").isEqualTo("via A");
        assertThat(pull(SOURCE).get(upstream()).bodyAsString()).isEqualTo("via B");

        PROXY_A.verify(2, getRequestedFor(urlPathEqualTo("/data")).withHeader("Host", equalTo(UPSTREAM_HOST)));
        PROXY_B.verify(1, getRequestedFor(urlPathEqualTo("/data")).withHeader("Host", equalTo(UPSTREAM_HOST)));
    }

    @Test
    @DisplayName("Retry-After through one route holds the source off that route only; the other route carries on")
    void retryAfterIsPerEgress() throws Exception {
        rotation(EgressSettings.ROTATE_ON_RATE_LIMIT);
        PROXY_A.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/data"))
                .willReturn(aResponse().withStatus(429).withHeader("Retry-After", "120")));

        assertThatThrownBy(() -> pull(SOURCE).get(upstream()))
                .isInstanceOf(UpstreamHttpException.class);

        assertThat(egressBackoff.until(SOURCE, a.id())).as("held off route A").isNotNull();
        assertThat(egressBackoff.until(SOURCE, b.id())).as("not held off route B").isNull();
        assertThat(backoff.isInBackoff(SOURCE)).as("the source itself is not in backoff").isFalse();
        assertThat(egressBackoff.until(SOURCE + "-other", a.id()))
                .as("another source is not held off route A").isNull();

        assertThat(pull(SOURCE).get(upstream()).bodyAsString())
                .as("the next pull moves to the route that did not throttle").isEqualTo("via B");
        assertThat(count(PROXY_A)).isEqualTo(1);
    }

    @Test
    @DisplayName("a 429 without Retry-After through a route also holds only that route")
    void bare429IsPerEgress() throws Exception {
        rotation(EgressSettings.ROTATE_ON_RATE_LIMIT);
        PROXY_A.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/data"))
                .willReturn(aResponse().withStatus(429)));

        assertThatThrownBy(() -> pull(SOURCE).get(upstream())).isInstanceOf(UpstreamHttpException.class);

        assertThat(egressBackoff.until(SOURCE, a.id())).isNotNull();
        assertThat(backoff.isInBackoff(SOURCE)).isFalse();
        assertThat(pull(SOURCE).get(upstream()).bodyAsString()).isEqualTo("via B");
    }

    @Test
    @DisplayName("when every route has throttled the source, the pull waits: it is not sent direct")
    void everyRouteThrottledWaits() throws Exception {
        rotation(EgressSettings.ROUND_ROBIN);
        egressBackoff.onThrottled(SOURCE, a.id(), java.util.Optional.of(java.time.Duration.ofMinutes(5)));
        egressBackoff.onThrottled(SOURCE, b.id(), java.util.Optional.of(java.time.Duration.ofMinutes(5)));

        assertThatThrownBy(() -> pull(SOURCE).get(upstream())).isInstanceOf(AdapterException.class);

        assertThat(count(PROXY_A) + count(PROXY_B)).isZero();
    }

    @Test
    @DisplayName("a pinned LAN host is never routed, so it still connects to the pinned address")
    void pinnedHostIsNotRouted() throws Exception {
        rotation(EgressSettings.ROUND_ROBIN);
        WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/data"))
                .willReturn(aResponse().withStatus(200).withBody("receiver")));
        String host = "receiver.local";
        URI configured = URI.create("http://" + host + ":" + port() + "/data");
        when(sources.effectiveParams(SOURCE)).thenReturn(new ValidatedParams(
                Map.of("receiverUrl", configured), Map.of("receiverUrl", InetAddress.getByName("127.0.0.1"))));
        SourceDescriptor lan = FakePollingAdapter.defaultDescriptor(SOURCE)
                .mode(Mode.POLLING)
                .param(ParamSpec.required("receiverUrl", ParamType.LAN_URL, "The receiver's URL"))
                .build();

        assertThat(client().forSource(SOURCE, lan, null).get(configured).bodyAsString()).isEqualTo("receiver");

        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/data")).withHeader("Host", equalTo(host + ":" + port())));
        assertThat(count(PROXY_A) + count(PROXY_B)).isZero();
    }

    @Test
    @DisplayName("with rotation on and no usable route the pull fails, and nothing is sent direct")
    void failsClosedWithNoUsableRoute() throws Exception {
        routes.replace(a.id(), json("{'name':'a-proxy','type':'http','enabled':false,'url':'http://127.0.0.1:"
                + PROXY_A.getPort() + "'}"));
        routes.replace(b.id(), json("{'name':'b-proxy','type':'http','enabled':false,'url':'http://127.0.0.1:"
                + PROXY_B.getPort() + "'}"));
        rotation(EgressSettings.RANDOM);

        assertThatThrownBy(() -> pull(SOURCE).get(URI.create(url("/data")))).isInstanceOf(AdapterException.class);

        assertThat(WIREMOCK.findAll(getRequestedFor(urlPathEqualTo("/data")))).isEmpty();
        assertThat(count(PROXY_A) + count(PROXY_B)).isZero();
    }

    @Test
    @DisplayName("a pinned source leaves through its route whatever the rotation would pick")
    void pinWins() throws Exception {
        rotation(EgressSettings.ROUND_ROBIN);
        // A registered source, because the assignment lives on its settings row; the fixture clears the pin.
        String pinned = registry.ids().stream().sorted().findFirst().orElseThrow();
        sourceService.setEgressAssignment(pinned, b.id(), false);
        router.invalidate();

        assertThat(pull(pinned).get(upstream()).bodyAsString()).isEqualTo("via B");
        assertThat(pull(pinned).get(upstream()).bodyAsString()).isEqualTo("via B");
        assertThat(count(PROXY_A)).isZero();
    }

    @Test
    @DisplayName("the host pacer's key is the host direct, and the host and route when routed")
    void pacingIsPerEgress() {
        EgressRouter.Routing viaA = router.routing(a);
        EgressRouter.Routing viaB = router.routing(b);

        assertThat(EgressRouter.pacingKey(UPSTREAM_HOST, null)).isEqualTo(UPSTREAM_HOST);
        assertThat(EgressRouter.pacingKey(UPSTREAM_HOST, viaA))
                .startsWith(UPSTREAM_HOST)
                .isNotEqualTo(EgressRouter.pacingKey(UPSTREAM_HOST, viaB))
                .isNotEqualTo(UPSTREAM_HOST);
    }
}
