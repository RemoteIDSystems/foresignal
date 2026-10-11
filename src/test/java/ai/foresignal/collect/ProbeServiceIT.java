package ai.foresignal.collect;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.BudgetExceededProblem;
import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Observer;
import ai.foresignal.source.spi.PositionSource;
import ai.foresignal.source.spi.Quality;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.store.FetchTrigger;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.FakeAdapters;
import ai.foresignal.testsupport.adapters.FakeStreamingAdapter;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.collect.ControllablePollingAdapter;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The probe against a stubbed upstream: what it captures, what it redacts, and everything it must
 * leave alone.
 */
@IntegrationTest
@Import(CollectorTestSources.class)
class ProbeServiceIT extends WireMockSupport {

    private static final String POLL = CollectorTestSources.POLL;
    private static final String KEYED = CollectorTestSources.KEYED;
    private static final Instant T0 = Instant.parse("2026-04-01T12:00:00Z");
    private static final String SECRET = "probe-secret-9876543210";

    @Autowired private ProbeService probes;
    @Autowired private ControllablePollingAdapter collectPollAdapter;
    @Autowired private ControllablePollingAdapter collectKeyedAdapter;
    @Autowired private FakeStreamingAdapter fakeStreamingAdapter;
    @Autowired private SourceService sources;
    @Autowired private BackoffRegistry backoff;
    @Autowired private MutableClock clock;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        clock.setInstant(T0);
        collectPollAdapter.reset();
        collectKeyedAdapter.reset();
        fakeStreamingAdapter.reset();
        backoff.onSuccess(POLL);
        backoff.onSuccess(KEYED);
    }

    private static NormalizedRecord rich(String id) {
        return NormalizedRecord.builder(id, CollectorTestSources.KIND)
                .observedAt(T0.minusSeconds(10))
                .eventTime(T0.minusSeconds(60))
                .validFrom(T0.minusSeconds(30))
                .point(-79.4, 43.7)
                .put("mag", 4.2)
                .quality(new Quality(12.5, 0.9, PositionSource.REPORTED, Map.of("test.age", 3)))
                .observers(List.of(new Observer("rx-1", 1_090_000_000L, -71.5, 12.0, 0.8)))
                .build();
    }

    private void assertNothingWritten(String sourceId) {
        assertThat(jdbc.queryForObject("select count(*) from fetch_log", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from record_version", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from record_head", Integer.class)).isZero();
        List<Map<String, Object>> health =
                jdbc.queryForList("select * from source_health where source_id = ?", sourceId);
        if (!health.isEmpty()) {
            assertThat(health.get(0).get("last_attempt_at")).isNull();
            assertThat(health.get(0).get("last_success_at")).isNull();
            assertThat(health.get(0).get("last_error")).isNull();
            assertThat(health.get(0).get("consecutive_failures")).isEqualTo(0);
        }
    }

    @Test
    @DisplayName("captures the raw body, truncated at maxBytes")
    void capturesAndTruncates() {
        String body = "a".repeat(5000);
        stubFor(get(urlPathEqualTo("/probe"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/plain").withBody(body)));
        collectPollAdapter.then(ctx -> {
            ctx.http().get(URI.create(url("/probe")));
            return FetchResult.complete(List.of(rich("p1")));
        });

        ProbeResult result = probes.probe(POLL, 1024, null);

        assertThat(result.sourceId()).isEqualTo(POLL);
        assertThat(result.parseError()).isNull();
        assertThat(result.requests()).hasSize(1);
        ProbeResult.Request request = result.requests().get(0);
        assertThat(request.method()).isEqualTo("GET");
        assertThat(request.status()).isEqualTo(200);
        assertThat(request.bytes()).isEqualTo(5000);
        assertThat(request.truncated()).isTrue();
        assertThat(request.contentType()).startsWith("text/plain");
        assertThat(request.body()).startsWith("a".repeat(1024)).hasSize(1024 + RequestRecorder.TRUNCATION_MARKER.length());
        assertNothingWritten(POLL);
    }

    @Test
    @DisplayName("redacts credentials from the URL and the body")
    void redactsCredentials() {
        sources.setCredentials(KEYED, Map.of("apiKey", SECRET));
        stubFor(get(urlPathEqualTo("/keyed")).willReturn(aResponse().withStatus(200).withBody("echo " + SECRET)));
        collectKeyedAdapter.then(ctx -> {
            ctx.http().get(URI.create(url("/keyed?apiKey=" + ctx.credentials().get("apiKey"))));
            return FetchResult.complete(List.of());
        });

        ProbeResult result = probes.probe(KEYED, null, null);

        ProbeResult.Request request = result.requests().get(0);
        assertThat(request.url()).doesNotContain(SECRET);
        assertThat(request.body()).doesNotContain(SECRET);
        assertThat(request.truncated()).isFalse();
    }

    @Test
    @DisplayName("previews carry eventTime, validFrom, quality, observers and GeoJSON geometry")
    void previews() {
        collectPollAdapter.then(ctx -> FetchResult.complete(List.of(rich("p1"))));

        ProbeResult result = probes.probe(POLL, null, null);

        assertThat(result.records()).hasSize(1);
        NormalizedRecordPreview preview = result.records().get(0);
        assertThat(preview.externalId()).isEqualTo("p1");
        assertThat(preview.kind()).isEqualTo(CollectorTestSources.KIND);
        assertThat(preview.observedAt()).isEqualTo(T0.minusSeconds(10));
        assertThat(preview.eventTime()).isEqualTo(T0.minusSeconds(60));
        assertThat(preview.validFrom()).isEqualTo(T0.minusSeconds(30));
        assertThat(preview.quality().accuracyMeters()).isEqualTo(12.5);
        assertThat(preview.quality().positionSource()).isEqualTo(PositionSource.REPORTED);
        assertThat(preview.observers()).extracting(Observer::receiverId).containsExactly("rx-1");
        assertThat(preview.geometry()).containsEntry("type", "Point");
        assertThat(preview.geometry().get("coordinates")).isEqualTo(List.of(-79.4, 43.7));
        assertThat(preview.payload()).containsEntry("mag", 4.2);
        assertNothingWritten(POLL);
    }

    @Test
    @DisplayName("returns at most 100 previews")
    void capsPreviews() {
        List<NormalizedRecord> many = new ArrayList<>();
        for (int i = 0; i < 150; i++) {
            many.add(CollectorTestSources.record("r" + i, T0.minusSeconds(5)));
        }
        collectPollAdapter.then(ctx -> FetchResult.complete(many));

        assertThat(probes.probe(POLL, null, null).records()).hasSize(ProbeResult.MAX_RECORDS);
    }

    @Test
    @DisplayName("a parse failure fills parseError and still returns the exchanges")
    void parseError() {
        stubFor(get(urlPathEqualTo("/broken")).willReturn(aResponse().withStatus(200).withBody("{not json")));
        collectPollAdapter.then(ctx -> {
            String text = ctx.http().get(URI.create(url("/broken"))).bodyAsString();
            throw new UpstreamFormatException("Expected JSON", text);
        });

        ProbeResult result = probes.probe(POLL, null, null);

        assertThat(result.parseError()).contains("UpstreamFormatException").contains("Expected JSON");
        assertThat(result.requests()).hasSize(1);
        assertThat(result.requests().get(0).body()).isEqualTo("{not json");
        assertThat(result.records()).isEmpty();
        assertNothingWritten(POLL);
    }

    @Test
    @DisplayName("a budget refusal is 429")
    void budget() {
        collectPollAdapter.then(ctx -> {
            throw new BudgetExceededException(POLL, "spent");
        });

        assertThatThrownBy(() -> probes.probe(POLL, null, null)).isInstanceOf(BudgetExceededProblem.class);
        assertNothingWritten(POLL);
    }

    @Test
    @DisplayName("a stream is captured for the window and then closed")
    void streaming() {
        fakeStreamingAdapter.scriptOnStart(rich("s1"), rich("s2"), rich("s3"));

        ProbeResult result = probes.probe(FakeAdapters.STREAMING_ID, null, 1);

        assertThat(result.records()).extracting(NormalizedRecordPreview::externalId).containsExactly("s1", "s2", "s3");
        assertThat(result.parseError()).isNull();
        assertThat(fakeStreamingAdapter.allClosed()).isTrue();
        assertNothingWritten(FakeAdapters.STREAMING_ID);
    }

    @Test
    @DisplayName("a stream that fails to start fills parseError")
    void streamingStartFails() {
        fakeStreamingAdapter.failOnStart(new ai.foresignal.source.spi.AdapterException("refused"));

        ProbeResult result = probes.probe(FakeAdapters.STREAMING_ID, null, 1);

        assertThat(result.parseError()).contains("refused");
        assertThat(result.records()).isEmpty();
    }

    @Test
    @DisplayName("push sources have nothing to probe: 409 NOT_SUPPORTED_FOR_MODE")
    void push() {
        assertThatThrownBy(() -> probes.probe(FakeAdapters.PUSH_ID, null, null))
                .isInstanceOf(ConflictException.class)
                .satisfies(e -> assertThat(((ForesignalException) e).getCode())
                        .isEqualTo(ConflictException.NOT_SUPPORTED_FOR_MODE));
    }

    @Test
    @DisplayName("out-of-range arguments are 400")
    void validation() {
        assertThatThrownBy(() -> probes.probe(POLL, 10, null)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> probes.probe(POLL, 20_000_000, null)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> probes.probe(POLL, null, 0)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> probes.probe(POLL, null, 61)).isInstanceOf(ValidationException.class);
        assertThat(collectPollAdapter.fetchCount()).isZero();
    }

    @Test
    @DisplayName("FetchTrigger is untouched: a probe is not a fetch")
    void notAFetch() {
        collectPollAdapter.then(ctx -> FetchResult.complete(List.of(rich("p1"))));
        probes.probe(POLL, null, null);
        assertThat(jdbc.queryForObject(
                "select count(*) from fetch_log where trigger = ?", Integer.class, FetchTrigger.MANUAL.name())).isZero();
    }
}
