package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The trace and recorder shapes, including what they must never carry. */
class FetchTraceTest {

    private static final Instant STARTED = Instant.parse("2026-03-01T12:00:00Z");

    @Test
    @DisplayName("a trace carries the redacted URL and a body hash, never the body")
    void carriesHashNotBody() {
        var trace =
                new FetchTrace(
                        "usgs-earthquakes",
                        "GET",
                        "https://earthquake.usgs.gov/feed?key=***",
                        200,
                        "\"abc\"",
                        4096,
                        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                        false,
                        false,
                        Duration.ofMillis(250),
                        STARTED,
                        null);

        assertThat(trace.redactedUrl()).contains("***").doesNotContain("secret");
        assertThat(trace.bodySha256()).hasSize(64);
        assertThat(trace.isSuccess()).isTrue();
        // FetchTrace has no body component at all; this is structural, not a convention.
        assertThat(FetchTrace.class.getRecordComponents())
                .extracting(java.lang.reflect.RecordComponent::getName)
                .doesNotContain("body");
    }

    @Test
    @DisplayName("a 4xx or 5xx trace is not a success")
    void statusDecidesSuccess() {
        assertThat(trace(200).isSuccess()).isTrue();
        assertThat(trace(304).isSuccess()).isTrue();
        assertThat(trace(404).isSuccess()).isFalse();
        assertThat(trace(503).isSuccess()).isFalse();
        assertThat(trace(0).isSuccess()).isFalse();
    }

    @Test
    @DisplayName("a request that was never sent records why, with no status")
    void notSentRecordsTheReason() {
        var trace =
                FetchTrace.notSent(
                        "adsblol-mil", "GET", "https://api.adsb.lol/v2/mil", STARTED, "BudgetExceededException");

        assertThat(trace.status()).isZero();
        assertThat(trace.bytes()).isZero();
        assertThat(trace.bodySha256()).isNull();
        assertThat(trace.failure()).isEqualTo("BudgetExceededException");
        assertThat(trace.duration()).isZero();
    }

    @Test
    @DisplayName("the recorder caps each body")
    void recorderCapsBodies() {
        var recorder = new RequestRecorder(10, 5);

        assertThat(recorder.capBody("x".repeat(50))).isEqualTo("x".repeat(10) + "… (truncated)");
        assertThat(recorder.capBody("short")).isEqualTo("short");
        assertThat(recorder.capBody(null)).isNull();
        assertThat(recorder.bodyCap()).isEqualTo(10);
    }

    @Test
    @DisplayName("the recorder stops at its exchange limit, so a probe cannot grow without bound")
    void recorderCapsExchangeCount() {
        var recorder = new RequestRecorder(1024, 2);

        recorder.record(exchange());
        recorder.record(exchange());
        assertThat(recorder.isFull()).isTrue();

        recorder.record(exchange());

        assertThat(recorder.exchanges()).hasSize(2);
    }

    @Test
    void recorderDefaultsAreSane() {
        var recorder = new RequestRecorder();

        assertThat(recorder.bodyCap()).isEqualTo(RequestRecorder.DEFAULT_BODY_CAP);
        assertThat(recorder.isFull()).isFalse();
        assertThat(recorder.exchanges()).isEmpty();
    }

    @Test
    @DisplayName("a recorded exchange defensively copies its header maps")
    void exchangeCopiesHeaders() {
        var mutable = new java.util.HashMap<String, List<String>>();
        mutable.put("accept", List.of("application/json"));

        var exchange =
                new RecordedExchange(
                        "GET", "https://x", mutable, 200, Map.of(), null, 0, false, null, Duration.ZERO, null);
        mutable.put("authorization", List.of("Bearer leaked"));

        assertThat(exchange.requestHeaders()).containsOnlyKeys("accept");
    }

    private static FetchTrace trace(int status) {
        return new FetchTrace(
                "s", "GET", "https://x", status, null, 0, null, false, false, Duration.ZERO, STARTED, null);
    }

    private static RecordedExchange exchange() {
        return new RecordedExchange(
                "GET", "https://x", Map.of(), 200, Map.of(), null, 0, false, null, Duration.ZERO, null);
    }
}
