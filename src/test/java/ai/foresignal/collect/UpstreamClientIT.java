package ai.foresignal.collect;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.DownloadResult;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.S3Listing;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.FakePollingAdapter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The whole request path against a stubbed upstream.
 *
 * <p>Every guarantee this prompt makes is only worth asserting end to end: that a refused request
 * really sends nothing (not that a method returned false), that a 429 really sets a backoff, that a
 * credential really does not reach a stored URL. WireMock's own view of what arrived is the
 * evidence, which is why the budget and backoff tests assert on
 * {@code verify(0, getRequestedFor(...))} rather than on Foresignal's own counters.
 *
 * <p>The collaborators are built here rather than autowired so the test owns the clock and the
 * budget: a per-minute limit of 60 would make "refuses past the budget" a 61-request test.
 * {@link UsageMeter}, {@link BackoffRegistry} and the adapter state store are the real ones,
 * against the real database, because their persistence is part of what is being tested.
 */
@IntegrationTest
class UpstreamClientIT extends WireMockSupport {

    private static final String SOURCE = "upstream-test";
    private static final Instant START = Instant.parse("2026-03-01T12:00:00Z");

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;

    private MutableClock clock;
    private SourceService sources;
    private CredentialsAccess credentials;
    private UsageMeter usage;
    private BackoffRegistry backoff;
    private BudgetGovernor budgets;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(START);
        sources = mock(SourceService.class);
        credentials = mock(CredentialsAccess.class);
        when(credentials.forSource(anyString())).thenReturn(Map.of());
        when(sources.effectiveBudget(anyString())).thenReturn(Budget.of(10, 100));
        when(sources.effectiveParams(anyString())).thenReturn(ValidatedParams.empty());

        usage = new UsageMeter(jdbc, clock);
        backoff = new BackoffRegistry(jdbc, clock);
        budgets = new BudgetGovernor(sources, usage, clock);
    }

    // ---------- harness ----------

    private UpstreamClient client() {
        return new UpstreamClient(
                budgets, backoff, usage, install, credentials, sources, jdbc, jsonMapper, properties, clock) {
            @Override
            URI s3ListUri(String bucket, String query) {
                // Points the virtual-hosted bucket URL at the stub; production has one form of this.
                return URI.create(baseUrl() + "/" + bucket + "/" + query);
            }
        };
    }

    private SourceHttp http() {
        return http(descriptor().build(), null);
    }

    private SourceHttp http(SourceDescriptor descriptor, RequestRecorder recorder) {
        return client().forSource(SOURCE, descriptor, recorder);
    }

    private static SourceDescriptor.Builder descriptor() {
        return FakePollingAdapter.defaultDescriptor(SOURCE);
    }

    private static URI uri(String path) {
        return URI.create(url(path));
    }

    private static byte[] gzip(String text) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
                gzip.write(text.getBytes(StandardCharsets.UTF_8));
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    // ---------- budget ----------

    @Nested
    @DisplayName("budget")
    class BudgetEnforcement {

        @Test
        @DisplayName("a refused request never reaches the upstream")
        void refusedRequestsAreNeverSent() {
            when(sources.effectiveBudget(SOURCE)).thenReturn(Budget.of(1, 1));
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/feed"))
                    .willReturn(aResponse().withStatus(200).withBody("{}")));

            // Spend the whole allowance without sending anything, so the next call is refused.
            assertThat(budgets.tryConsume(SOURCE)).isTrue();

            assertThatThrownBy(() -> http().get(uri("/feed")))
                    .isInstanceOf(BudgetExceededException.class)
                    .hasMessageContaining("was not sent");

            WIREMOCK.verify(0, getRequestedFor(urlEqualTo("/feed")));
        }

        @Test
        @DisplayName("a refusal is metered as refused, not as a request")
        void refusalIsMetered() {
            when(sources.effectiveBudget(SOURCE)).thenReturn(Budget.of(1, 1));
            assertThat(budgets.tryConsume(SOURCE)).isTrue();

            assertThatThrownBy(() -> http().get(uri("/feed")))
                    .isInstanceOf(BudgetExceededException.class);

            UsageTotalsWindow totals = usage.lastHour(SOURCE);
            assertThat(totals.refused()).isEqualTo(1);
            assertThat(totals.requests()).isZero();
        }

        @Test
        @DisplayName("the exception names the source, so a log line says which budget ran out")
        void refusalNamesTheSource() {
            when(sources.effectiveBudget(SOURCE)).thenReturn(Budget.of(1, 1));
            budgets.tryConsume(SOURCE);

            assertThatThrownBy(() -> http().get(uri("/feed")))
                    .isInstanceOf(BudgetExceededException.class)
                    .hasMessageContaining(SOURCE)
                    .extracting(e -> ((BudgetExceededException) e).getSourceId())
                    .isEqualTo(SOURCE);
        }
    }

    // ---------- backoff ----------

    @Nested
    @DisplayName("backoff")
    class Backoff {

        @Test
        @DisplayName("a source in backoff sends nothing and spends no budget")
        void backoffBlocksWithoutConsumingBudget() {
            backoff.onThrottled(SOURCE, java.util.Optional.of(Duration.ofMinutes(5)));
            long before = budgets.status(SOURCE).minuteRemaining();

            assertThatThrownBy(() -> http().get(uri("/feed")))
                    .isInstanceOf(InBackoffException.class)
                    .hasMessageContaining("in backoff until");

            WIREMOCK.verify(0, getRequestedFor(urlEqualTo("/feed")));
            assertThat(budgets.status(SOURCE).minuteRemaining())
                    .as("a request we were never going to send must not cost a token")
                    .isEqualTo(before);
        }

        @Test
        @DisplayName("429 with Retry-After sets the backoff the upstream asked for")
        void throttleHonoursRetryAfter() {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/feed"))
                    .willReturn(aResponse().withStatus(429).withHeader("Retry-After", "120").withBody("slow down")));

            assertThatThrownBy(() -> http().get(uri("/feed")))
                    .isInstanceOf(UpstreamHttpException.class)
                    .hasMessageContaining("429")
                    .hasMessageContaining("slow down");

            assertThat(backoff.isInBackoff(SOURCE)).isTrue();
            assertThat(backoff.backoffUntil(SOURCE)).isEqualTo(START.plus(Duration.ofMinutes(2)));
        }

        @Test
        @DisplayName("OpenSky's own retry header is honoured too")
        void throttleHonoursOpenSkyHeader() {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/feed"))
                    .willReturn(aResponse()
                            .withStatus(429)
                            .withHeader("X-Rate-Limit-Retry-After-Seconds", "45")
                            .withBody("")));

            assertThatThrownBy(() -> http().get(uri("/feed"))).isInstanceOf(UpstreamHttpException.class);

            assertThat(backoff.backoffUntil(SOURCE)).isEqualTo(START.plusSeconds(45));
        }

        @Test
        @DisplayName("a Retry-After under the floor is clamped, not obeyed literally")
        void retryAfterIsClamped() {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/feed"))
                    .willReturn(aResponse().withStatus(429).withHeader("Retry-After", "1").withBody("")));

            assertThatThrownBy(() -> http().get(uri("/feed"))).isInstanceOf(UpstreamHttpException.class);

            assertThat(backoff.backoffUntil(SOURCE)).isEqualTo(START.plus(BackoffRegistry.MIN_RETRY_AFTER));
        }

        @Test
        @DisplayName("503 with no header backs off exponentially from 30 seconds")
        void serviceUnavailableBacksOff() {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/feed"))
                    .willReturn(aResponse().withStatus(503).withBody("maintenance")));

            assertThatThrownBy(() -> http().get(uri("/feed")))
                    .isInstanceOf(UpstreamHttpException.class)
                    .extracting(e -> ((UpstreamHttpException) e).isThrottle())
                    .isEqualTo(true);

            assertThat(backoff.backoffUntil(SOURCE)).isEqualTo(START.plus(BackoffRegistry.BASE_BACKOFF));
        }

        @Test
        @DisplayName("404 is an error but not a reason to back off: the next fetch is still due")
        void notFoundDoesNotBackOff() {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/feed"))
                    .willReturn(aResponse().withStatus(404).withBody("no such feed")));

            assertThatThrownBy(() -> http().get(uri("/feed")))
                    .isInstanceOf(UpstreamHttpException.class)
                    .hasMessageContaining("no such feed");

            assertThat(backoff.isInBackoff(SOURCE)).isFalse();
            assertThat(backoff.backoffUntil(SOURCE)).isNull();
        }

        @Test
        @DisplayName("a success clears the backoff, so the next throttle starts at 30s again")
        void successResetsTheExponent() throws Exception {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/throttled"))
                    .willReturn(aResponse().withStatus(503)));
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/ok"))
                    .willReturn(aResponse().withStatus(200).withBody("fine")));

            assertThatThrownBy(() -> http().get(uri("/throttled"))).isInstanceOf(UpstreamHttpException.class);
            assertThat(backoff.consecutiveThrottles(SOURCE)).isEqualTo(1);

            clock.advance(Duration.ofMinutes(1));
            http().get(uri("/ok"));

            assertThat(backoff.consecutiveThrottles(SOURCE)).isZero();
            assertThat(backoff.isInBackoff(SOURCE)).isFalse();

            assertThatThrownBy(() -> http().get(uri("/throttled"))).isInstanceOf(UpstreamHttpException.class);
            assertThat(backoff.backoffUntil(SOURCE))
                    .as("the exponent restarted rather than continuing to double")
                    .isEqualTo(clock.instant().plus(BackoffRegistry.BASE_BACKOFF));
        }
    }

    // ---------- headers ----------

    @Nested
    @DisplayName("identification")
    class Identification {

        @Test
        @DisplayName("every request identifies the install, which is what lets an upstream contact us")
        void sendsTheForesignalUserAgent() throws Exception {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/feed"))
                    .willReturn(aResponse().withStatus(200).withBody("{}")));

            http().get(uri("/feed"));

            WIREMOCK.verify(getRequestedFor(urlEqualTo("/feed"))
                    .withHeader("User-Agent", equalTo(install.userAgent()))
                    .withHeader("User-Agent", matching("Foresignal/.+ \\(\\+https://foresignal\\.ai; install=[A-Za-z0-9]+\\)"))
                    .withHeader("Accept-Encoding", matching(".*gzip.*")));
        }

        @Test
        @DisplayName("an adapter cannot swap in its own User-Agent")
        void callersCannotOverrideTheUserAgent() throws Exception {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/feed"))
                    .willReturn(aResponse().withStatus(200).withBody("{}")));

            http().get(uri("/feed"), Map.of("User-Agent", "Mozilla/5.0", "X-Trace", "kept"));

            WIREMOCK.verify(getRequestedFor(urlEqualTo("/feed"))
                    .withHeader("User-Agent", equalTo(install.userAgent()))
                    .withHeader("X-Trace", equalTo("kept")));
        }

        @Test
        @DisplayName("a descriptor may demand a specific format, for an upstream that requires one")
        void descriptorOverrideIsUsed() throws Exception {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/feed"))
                    .willReturn(aResponse().withStatus(200).withBody("{}")));

            SourceDescriptor overriding = descriptor().userAgentOverride("contact@example.org").build();
            http(overriding, null).get(uri("/feed"));

            WIREMOCK.verify(getRequestedFor(urlEqualTo("/feed"))
                    .withHeader("User-Agent", equalTo("contact@example.org")));
        }
    }

    // ---------- bodies ----------

    @Nested
    @DisplayName("bodies")
    class Bodies {

        @Test
        @DisplayName("a gzipped body is decoded, because HttpClient does not do it for us")
        void decodesGzip() throws Exception {
            String payload = "{\"records\":[1,2,3]}";
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/gz"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Encoding", "gzip")
                            .withHeader("Content-Type", "application/json")
                            .withBody(gzip(payload))));

            UpstreamResponse response = http().get(uri("/gz"));

            assertThat(response.bodyAsString()).isEqualTo(payload);
            assertThat(response.truncated()).isFalse();
        }

        @Test
        @DisplayName("a body past the cap is truncated rather than filling the heap")
        void truncatesAtTheCap() throws Exception {
            String payload = "x".repeat(5_000);
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/big"))
                    .willReturn(aResponse().withStatus(200).withBody(payload)));

            UpstreamResponse response = http(descriptor().maxResponseBytes(100).build(), null).get(uri("/big"));

            assertThat(response.body()).hasSize(100);
            assertThat(response.truncated()).isTrue();
        }

        @Test
        @DisplayName("a body exactly at the cap is complete, not truncated")
        void exactlyAtTheCapIsComplete() throws Exception {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/exact"))
                    .willReturn(aResponse().withStatus(200).withBody("x".repeat(100))));

            UpstreamResponse response = http(descriptor().maxResponseBytes(100).build(), null).get(uri("/exact"));

            assertThat(response.body()).hasSize(100);
            assertThat(response.truncated()).isFalse();
        }

        @Test
        @DisplayName("a POST sends its body and content type")
        void postSendsTheBody() throws Exception {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.post(urlEqualTo("/submit"))
                    .willReturn(aResponse().withStatus(200).withBody("accepted")));

            UpstreamResponse response =
                    http().post(uri("/submit"), "application/json", "{\"q\":1}".getBytes(StandardCharsets.UTF_8), Map.of());

            assertThat(response.bodyAsString()).isEqualTo("accepted");
            WIREMOCK.verify(postRequestedFor(urlEqualTo("/submit"))
                    .withHeader("Content-Type", equalTo("application/json"))
                    .withRequestBody(equalTo("{\"q\":1}")));
        }

        @Test
        @DisplayName("a connection that cannot be made is an IO failure, not an HTTP one")
        void unreachableUpstreamIsAnIoFailure() {
            // Port 1 is reserved and unused, so this fails to connect rather than returning a status.
            assertThatThrownBy(() -> http().get(URI.create("http://127.0.0.1:1/nothing")))
                    .isInstanceOf(UpstreamIOException.class);

            assertThat(usage.lastHour(SOURCE).errors()).isEqualTo(1);
        }
    }

    // ---------- conditional GET ----------

    @Nested
    @DisplayName("conditional GET")
    class ConditionalGet {

        @Test
        @DisplayName("the first request is unconditional and the validators are stored")
        void storesValidators() throws Exception {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/cond"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("ETag", "\"v1\"")
                            .withHeader("Last-Modified", "Wed, 01 Mar 2026 11:00:00 GMT")
                            .withBody("first")));

            UpstreamResponse response = http().getConditional(uri("/cond"), "feed");

            assertThat(response.status()).isEqualTo(200);
            assertThat(response.notModified()).isFalse();
            WIREMOCK.verify(getRequestedFor(urlEqualTo("/cond"))
                    .withHeader("If-None-Match", absent())
                    .withHeader("If-Modified-Since", absent()));

            PersistentAdapterStateStore state = client().stateStore(SOURCE);
            assertThat(state.get("etag:feed")).contains("\"v1\"");
            assertThat(state.get("lastmod:feed")).contains("Wed, 01 Mar 2026 11:00:00 GMT");
        }

        @Test
        @DisplayName("the next request sends the validators and a 304 comes back as notModified")
        void sendsValidatorsAndHandles304() throws Exception {
            client().stateStore(SOURCE).put("etag:feed", "\"v1\"");
            client().stateStore(SOURCE).put("lastmod:feed", "Wed, 01 Mar 2026 11:00:00 GMT");

            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/cond"))
                    .withHeader("If-None-Match", equalTo("\"v1\""))
                    .withHeader("If-Modified-Since", equalTo("Wed, 01 Mar 2026 11:00:00 GMT"))
                    .willReturn(aResponse().withStatus(304)));

            UpstreamResponse response = http().getConditional(uri("/cond"), "feed");

            assertThat(response.notModified()).isTrue();
            assertThat(response.status()).isEqualTo(304);
            assertThat(response.body()).isEmpty();
        }

        @Test
        @DisplayName("a 304 still costs budget and is metered as a success: it cost a round trip")
        void notModifiedIsMeteredAsASuccess() throws Exception {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/cond"))
                    .willReturn(aResponse().withStatus(304)));

            long before = budgets.status(SOURCE).minuteRemaining();
            http().getConditional(uri("/cond"), "feed");

            assertThat(budgets.status(SOURCE).minuteRemaining()).isEqualTo(before - 1);
            UsageTotalsWindow totals = usage.lastHour(SOURCE);
            assertThat(totals.requests()).isEqualTo(1);
            assertThat(totals.errors()).isZero();
            assertThat(totals.bytes()).isZero();
        }

        @Test
        @DisplayName("a 304 leaves the stored validators alone, so the next request still sends them")
        void notModifiedKeepsTheStoredValidators() throws Exception {
            client().stateStore(SOURCE).put("etag:feed", "\"v1\"");
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/cond"))
                    .willReturn(aResponse().withStatus(304)));

            http().getConditional(uri("/cond"), "feed");

            assertThat(client().stateStore(SOURCE).get("etag:feed")).contains("\"v1\"");
        }
    }

    // ---------- recording and redaction ----------

    @Nested
    @DisplayName("recording")
    class Recording {

        private static final String SECRET = "sk-live-9f8e7d6c5b4a";

        @Test
        @DisplayName("a stored credential never reaches a recorded URL")
        void redactsTheUrl() throws Exception {
            when(credentials.forSource(SOURCE)).thenReturn(Map.of("apiKey", SECRET));
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/feed"))
                    .willReturn(aResponse().withStatus(200).withBody("{}")));

            RequestRecorder recorder = new RequestRecorder();
            http(descriptor().build(), recorder).get(uri("/feed?key=" + SECRET));

            assertThat(recorder.exchanges()).hasSize(1);
            assertThat(recorder.exchanges().get(0).redactedUrl())
                    .contains("key=" + UrlRedactor.MASK)
                    .doesNotContain(SECRET);
        }

        @Test
        @DisplayName("Authorization is redacted whole, even when Foresignal never stored its value")
        void redactsAuthorization() throws Exception {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/feed"))
                    .willReturn(aResponse().withStatus(200).withBody("{}")));

            RequestRecorder recorder = new RequestRecorder();
            http(descriptor().build(), recorder)
                    .get(uri("/feed"), Map.of("Authorization", "Bearer never-stored-anywhere"));

            Map<String, List<String>> headers = recorder.exchanges().get(0).requestHeaders();
            assertThat(headers.get("Authorization")).containsExactly(UrlRedactor.MASK);
        }

        @Test
        @DisplayName("a credential sent in a custom header is masked in the capture")
        void redactsCredentialHeaders() throws Exception {
            when(credentials.forSource(SOURCE)).thenReturn(Map.of("apiKey", SECRET));
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/feed"))
                    .willReturn(aResponse().withStatus(200).withHeader("X-Echo-Key", SECRET).withBody(SECRET)));

            RequestRecorder recorder = new RequestRecorder();
            http(descriptor().build(), recorder).get(uri("/feed"), Map.of("X-Api-Key", SECRET));

            RecordedExchange exchange = recorder.exchanges().get(0);
            assertThat(exchange.requestHeaders().get("X-Api-Key")).containsExactly(UrlRedactor.MASK);
            assertThat(exchange.responseHeaders().get("x-echo-key")).containsExactly(UrlRedactor.MASK);
            assertThat(exchange.body()).isEqualTo(UrlRedactor.MASK);
        }

        @Test
        @DisplayName("a fetch trace carries the redacted URL, a hash and the byte count, never a body")
        void tracesCarryHashesNotBodies() throws Exception {
            when(credentials.forSource(SOURCE)).thenReturn(Map.of("apiKey", SECRET));
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/feed"))
                    .willReturn(aResponse().withStatus(200).withHeader("ETag", "\"abc\"").withBody("hello")));

            SourceHttp bound = http();
            bound.get(uri("/feed?key=" + SECRET));

            List<FetchTrace> traces = ((BoundSourceHttp) bound).traces();
            assertThat(traces).hasSize(1);
            FetchTrace trace = traces.get(0);
            assertThat(trace.redactedUrl()).doesNotContain(SECRET);
            assertThat(trace.status()).isEqualTo(200);
            assertThat(trace.etag()).isEqualTo("\"abc\"");
            assertThat(trace.bytes()).isEqualTo(5);
            // SHA-256 of "hello".
            assertThat(trace.bodySha256())
                    .isEqualTo("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824");
            assertThat(trace.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("a request that never completed is traced as a failure with no status")
        void tracesFailures() {
            SourceHttp bound = http();
            assertThatThrownBy(() -> bound.get(URI.create("http://127.0.0.1:1/nothing")))
                    .isInstanceOf(UpstreamIOException.class);

            List<FetchTrace> traces = ((BoundSourceHttp) bound).traces();
            assertThat(traces).hasSize(1);
            assertThat(traces.get(0).status()).isZero();
            assertThat(traces.get(0).failure()).isNotNull();
            assertThat(traces.get(0).isSuccess()).isFalse();
        }
    }

    // ---------- metering ----------

    @Nested
    @DisplayName("metering")
    class Metering {

        @Test
        @DisplayName("requests and bytes are counted per source")
        void countsRequestsAndBytes() throws Exception {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/feed"))
                    .willReturn(aResponse().withStatus(200).withBody("0123456789")));

            SourceHttp bound = http();
            bound.get(uri("/feed"));
            bound.get(uri("/feed"));

            UsageTotalsWindow totals = usage.lastHour(SOURCE);
            assertThat(totals.requests()).isEqualTo(2);
            assertThat(totals.bytes()).isEqualTo(20);
            assertThat(totals.errors()).isZero();
            assertThat(usage.requestsToday(SOURCE)).isEqualTo(2);
        }

        @Test
        @DisplayName("an error status counts as a request and an error")
        void countsErrors() {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/feed"))
                    .willReturn(aResponse().withStatus(500).withBody("boom")));

            assertThatThrownBy(() -> http().get(uri("/feed"))).isInstanceOf(UpstreamHttpException.class);

            UsageTotalsWindow totals = usage.lastHour(SOURCE);
            assertThat(totals.requests()).isEqualTo(1);
            assertThat(totals.errors()).isEqualTo(1);
        }
    }

    // ---------- ranged reads ----------

    @Nested
    @DisplayName("getRange")
    class RangedReads {

        @Test
        @DisplayName("206 returns the slice that was asked for")
        void returnsThePartialBody() throws Exception {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/grib"))
                    .withHeader("Range", equalTo("bytes=100-109"))
                    .willReturn(aResponse()
                            .withStatus(206)
                            .withHeader("Content-Range", "bytes 100-109/50000")
                            .withBody("0123456789")));

            UpstreamResponse response = http().getRange(uri("/grib"), 100, 109, 1_000);

            assertThat(response.status()).isEqualTo(206);
            assertThat(response.bodyAsString()).isEqualTo("0123456789");
        }

        @Test
        @DisplayName("200 for a non-zero start is rejected: the upstream ignored the Range")
        void rejectsAFullBodyForANonZeroStart() {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/grib"))
                    .willReturn(aResponse().withStatus(200).withBody("the whole object, all of it")));

            assertThatThrownBy(() -> http().getRange(uri("/grib"), 5, 9, 1_000))
                    .isInstanceOf(UpstreamHttpException.class)
                    .hasMessageContaining("only 206");
        }

        @Test
        @DisplayName("200 is accepted when the range starts at zero")
        void acceptsAFullBodyFromZero() throws Exception {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/grib"))
                    .willReturn(aResponse().withStatus(200).withBody("0123456789")));

            UpstreamResponse response = http().getRange(uri("/grib"), 0, 9, 1_000);

            assertThat(response.status()).isEqualTo(200);
        }

        @Test
        @DisplayName("a body shorter than the range is rejected rather than parsed")
        void rejectsAShortBody() {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/grib"))
                    .willReturn(aResponse().withStatus(206).withBody("012")));

            assertThatThrownBy(() -> http().getRange(uri("/grib"), 0, 9, 1_000))
                    .isInstanceOf(AdapterException.class)
                    .hasMessageContaining("3 bytes for a 10-byte range");
        }

        @Test
        @DisplayName("an inverted or negative range is a caller bug, caught before any request")
        void rejectsAnImpossibleRange() {
            assertThatThrownBy(() -> http().getRange(uri("/grib"), 10, 5, 1_000))
                    .isInstanceOf(AdapterException.class)
                    .hasMessageContaining("Invalid byte range");
            assertThatThrownBy(() -> http().getRange(uri("/grib"), -1, 5, 1_000))
                    .isInstanceOf(AdapterException.class);
            WIREMOCK.verify(0, getRequestedFor(urlEqualTo("/grib")));
        }

        @Test
        @DisplayName("a redirect is not followed: a redirect usually means the Range was dropped")
        void doesNotFollowRedirects() {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/grib"))
                    .willReturn(aResponse().withStatus(302).withHeader("Location", url("/elsewhere"))));

            assertThatThrownBy(() -> http().getRange(uri("/grib"), 0, 9, 1_000))
                    .isInstanceOf(UpstreamHttpException.class);

            WIREMOCK.verify(0, getRequestedFor(urlEqualTo("/elsewhere")));
        }

        @Test
        @DisplayName("the per-read cap overrides the descriptor's")
        void perReadCapApplies() throws Exception {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/grib"))
                    .willReturn(aResponse().withStatus(206).withBody("x".repeat(500))));

            UpstreamResponse response = http().getRange(uri("/grib"), 0, 499, 50);

            assertThat(response.body()).hasSize(50);
            assertThat(response.truncated()).isTrue();
        }
    }

    // ---------- downloads ----------

    @Nested
    @DisplayName("download")
    class Downloads {

        @TempDir Path tempDir;

        @Test
        @DisplayName("the body goes to a file, with its size and hash")
        void streamsToAFile() throws Exception {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/raster"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "image/png")
                            .withBody("hello")));

            Path target = tempDir.resolve("frame.png");
            DownloadResult result = http().download(uri("/raster"), target, 1_000);

            assertThat(Files.readString(target)).isEqualTo("hello");
            assertThat(result.bytes()).isEqualTo(5);
            assertThat(result.contentType()).isEqualTo("image/png");
            assertThat(result.sha256())
                    .isEqualTo("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824");
            assertThat(usage.lastHour(SOURCE).bytes()).isEqualTo(5);
        }

        @Test
        @DisplayName("the cap stops the write, so an unexpectedly huge object cannot fill the disk")
        void stopsAtTheCap() throws Exception {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/raster"))
                    .willReturn(aResponse().withStatus(200).withBody("y".repeat(10_000))));

            Path target = tempDir.resolve("capped.bin");
            DownloadResult result = http().download(uri("/raster"), target, 128);

            assertThat(result.bytes()).isEqualTo(128);
            assertThat(Files.size(target)).isEqualTo(128);
        }

        @Test
        @DisplayName("a download consumes budget like any other request")
        void consumesBudget() throws Exception {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/raster"))
                    .willReturn(aResponse().withStatus(200).withBody("ok")));

            long before = budgets.status(SOURCE).minuteRemaining();
            http().download(uri("/raster"), tempDir.resolve("one.bin"), 1_000);

            assertThat(budgets.status(SOURCE).minuteRemaining()).isEqualTo(before - 1);
        }

        @Test
        @DisplayName("an error status throws and writes no file")
        void errorStatusThrows() {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/raster"))
                    .willReturn(aResponse().withStatus(500)));

            Path target = tempDir.resolve("never.bin");
            assertThatThrownBy(() -> http().download(uri("/raster"), target, 1_000))
                    .isInstanceOf(UpstreamHttpException.class);

            assertThat(Files.exists(target)).isFalse();
        }

        @Test
        @DisplayName("a 503 on a download backs the source off, like any other throttle")
        void throttledDownloadBacksOff() {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/raster"))
                    .willReturn(aResponse().withStatus(503)));

            assertThatThrownBy(() -> http().download(uri("/raster"), tempDir.resolve("no.bin"), 1_000))
                    .isInstanceOf(UpstreamHttpException.class);

            assertThat(backoff.isInBackoff(SOURCE)).isTrue();
        }
    }

    // ---------- S3 listings ----------

    @Nested
    @DisplayName("listS3")
    class S3Listings {

        private static final String BUCKET = "noaa-test-pds";

        private static String page(boolean truncated, String... keys) {
            StringBuilder xml = new StringBuilder(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                            + "<ListBucketResult xmlns=\"http://s3.amazonaws.com/doc/2006-03-01/\">"
                            + "<Name>"
                            + BUCKET
                            + "</Name><IsTruncated>"
                            + truncated
                            + "</IsTruncated>");
            for (String key : keys) {
                xml.append("<Contents><Key>")
                        .append(key)
                        .append("</Key><Size>1024</Size><ETag>\"deadbeef\"</ETag>")
                        .append("<LastModified>2026-03-01T10:00:00.000Z</LastModified></Contents>");
            }
            return xml.append("</ListBucketResult>").toString();
        }

        @Test
        @DisplayName("a page is parsed into keys, sizes and ETags")
        void parsesAPage() throws Exception {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/" + BUCKET + "/"))
                    .willReturn(aResponse().withStatus(200).withBody(page(false, "a/1.grib2", "a/2.grib2"))));

            S3Listing listing = http().listS3(BUCKET, "a/", null);

            assertThat(listing.objects()).extracting("key").containsExactly("a/1.grib2", "a/2.grib2");
            assertThat(listing.objects().get(0).size()).isEqualTo(1024);
            assertThat(listing.objects().get(0).etag()).isEqualTo("deadbeef");
            assertThat(listing.objects().get(0).lastModified()).isEqualTo(Instant.parse("2026-03-01T10:00:00Z"));
            assertThat(listing.truncated()).isFalse();
            assertThat(listing.nextStartAfter()).isNull();

            WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/" + BUCKET + "/"))
                    .withQueryParam("list-type", equalTo("2"))
                    .withQueryParam("prefix", equalTo("a/")));
        }

        @Test
        @DisplayName("a truncated page names the key to continue after, and the next call sends it")
        void pagesWithStartAfter() throws Exception {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/" + BUCKET + "/"))
                    .withQueryParam("start-after", absent())
                    .willReturn(aResponse().withStatus(200).withBody(page(true, "a/1.grib2", "a/2.grib2"))));
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/" + BUCKET + "/"))
                    .withQueryParam("start-after", equalTo("a/2.grib2"))
                    .willReturn(aResponse().withStatus(200).withBody(page(false, "a/3.grib2"))));

            S3Listing first = http().listS3(BUCKET, "a/", null);
            assertThat(first.truncated()).isTrue();
            assertThat(first.nextStartAfter()).isEqualTo("a/2.grib2");

            S3Listing second = http().listS3(BUCKET, "a/", first.nextStartAfter());
            assertThat(second.objects()).extracting("key").containsExactly("a/3.grib2");
            assertThat(second.truncated()).isFalse();
        }

        @Test
        @DisplayName("an empty listing is empty, not an error: a prefix with no objects yet is normal")
        void emptyListing() throws Exception {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/" + BUCKET + "/"))
                    .willReturn(aResponse().withStatus(200).withBody(page(false))));

            assertThat(http().listS3(BUCKET, null, null).isEmpty()).isTrue();
        }

        @Test
        @DisplayName("unparseable XML is a format error naming the source, not a crash")
        void rejectsBadXml() {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/" + BUCKET + "/"))
                    .willReturn(aResponse().withStatus(200).withBody("<ListBucketResult><unclosed>")));

            assertThatThrownBy(() -> http().listS3(BUCKET, null, null))
                    .isInstanceOf(UpstreamFormatException.class);
        }
    }

    // ---------- LAN pinning ----------

    @Nested
    @DisplayName("LAN_URL pinning")
    class LanPinning {

        private static final String PARAM = "receiverUrl";

        private SourceDescriptor lanDescriptor() {
            return descriptor()
                    .mode(Mode.POLLING)
                    .param(ParamSpec.required(PARAM, ParamType.LAN_URL, "The receiver's URL"))
                    .build();
        }

        private SourceHttp pinnedHttp(String host) throws Exception {
            URI configured = URI.create("http://" + host + ":" + port() + "/data");
            when(sources.effectiveParams(SOURCE))
                    .thenReturn(new ValidatedParams(
                            Map.of(PARAM, configured),
                            Map.of(PARAM, InetAddress.getByName("127.0.0.1"))));
            return http(lanDescriptor(), null);
        }

        @Test
        @DisplayName("the request goes to the pinned address, and the configured Host is preserved")
        void connectsToThePinnedAddress() throws Exception {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/data"))
                    .willReturn(aResponse().withStatus(200).withBody("{}")));

            String host = "receiver.local";
            SourceHttp bound = pinnedHttp(host);
            UpstreamResponse response = bound.get(URI.create("http://" + host + ":" + port() + "/data"));

            assertThat(response.status()).isEqualTo(200);
            // Reaching the stub at all proves the pinned address was used: receiver.local does not
            // resolve here, so a re-resolved host would have failed to connect.
            WIREMOCK.verify(getRequestedFor(urlEqualTo("/data"))
                    .withHeader("Host", equalTo(host + ":" + port())));
        }

        @Test
        @DisplayName("a pinned request follows no redirect, which would re-resolve the host")
        void doesNotFollowRedirects() throws Exception {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/data"))
                    .willReturn(aResponse().withStatus(302).withHeader("Location", url("/elsewhere"))));

            SourceHttp bound = pinnedHttp("receiver.local");
            UpstreamResponse response = bound.get(URI.create("http://receiver.local:" + port() + "/data"));

            assertThat(response.status()).isEqualTo(302);
            WIREMOCK.verify(0, getRequestedFor(urlEqualTo("/elsewhere")));
        }

        @Test
        @DisplayName("an ordinary host is not pinned, so other requests behave normally")
        void leavesOtherHostsAlone() throws Exception {
            WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/data"))
                    .willReturn(aResponse().withStatus(200).withBody("{}")));

            SourceHttp bound = pinnedHttp("receiver.local");
            assertThat(bound.get(uri("/data")).status()).isEqualTo(200);

            WIREMOCK.verify(getRequestedFor(urlEqualTo("/data"))
                    .withHeader("Host", equalTo("localhost:" + port())));
        }
    }

    // ---------- DERIVED sources ----------

    @Test
    @DisplayName("a DERIVED source has no HTTP client at all: it makes no upstream requests")
    void derivedSourcesGetNoClient() {
        SourceDescriptor derived = SourceDescriptor.builder("derived-test")
                .name("Derived")
                .kind("derived-record")
                .mode(Mode.DERIVED)
                .build();

        assertThatThrownBy(() -> client().forSource("derived-test", derived))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("DERIVED");
    }
}
