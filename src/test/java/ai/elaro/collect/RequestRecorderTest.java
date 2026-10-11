package ai.elaro.collect;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The probe capture buffer and its caps. */
class RequestRecorderTest {

    @Test
    @DisplayName("exchanges are kept in order")
    void keepsOrder() {
        RequestRecorder recorder = new RequestRecorder();

        recorder.record(exchange("/one"));
        recorder.record(exchange("/two"));

        assertThat(recorder.exchanges()).extracting(RecordedExchange::redactedUrl).containsExactly("/one", "/two");
    }

    @Test
    @DisplayName("a body longer than the cap is truncated and says so")
    void capsBodies() {
        RequestRecorder recorder = new RequestRecorder(10, 5);

        assertThat(recorder.capBody("0123456789")).isEqualTo("0123456789");
        assertThat(recorder.capBody("0123456789abc")).isEqualTo("0123456789… (truncated)");
        assertThat(recorder.bodyCap()).isEqualTo(10);
    }

    @Test
    void aNullBodyStaysNull() {
        assertThat(new RequestRecorder().capBody(null)).isNull();
    }

    @Test
    @DisplayName("a probe cannot grow without bound: past the limit, exchanges are dropped")
    void stopsRecordingWhenFull() {
        RequestRecorder recorder = new RequestRecorder(1024, 2);

        recorder.record(exchange("/one"));
        recorder.record(exchange("/two"));
        recorder.record(exchange("/three"));

        assertThat(recorder.isFull()).isTrue();
        assertThat(recorder.exchanges()).hasSize(2);
        assertThat(recorder.exchanges()).extracting(RecordedExchange::redactedUrl).doesNotContain("/three");
    }

    @Test
    @DisplayName("the defaults are the documented ones")
    void defaults() {
        RequestRecorder recorder = new RequestRecorder();

        assertThat(recorder.bodyCap()).isEqualTo(RequestRecorder.DEFAULT_BODY_CAP);
        assertThat(recorder.isFull()).isFalse();
    }

    @Test
    @DisplayName("header maps are defensively copied, so a caller cannot mutate a capture")
    void copiesHeaders() {
        RecordedExchange exchange =
                new RecordedExchange(
                        "GET", "/x", null, 200, null, "application/json", 3, false, "{}", Duration.ZERO, null);

        assertThat(exchange.requestHeaders()).isEmpty();
        assertThat(exchange.responseHeaders()).isEmpty();
    }

    private static RecordedExchange exchange(String url) {
        return new RecordedExchange(
                "GET",
                url,
                Map.of("Accept", List.of("application/json")),
                200,
                Map.of("content-type", List.of("application/json")),
                "application/json",
                2,
                false,
                "{}",
                Duration.ofMillis(5),
                null);
    }
}
