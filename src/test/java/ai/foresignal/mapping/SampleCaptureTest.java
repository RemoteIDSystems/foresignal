package ai.foresignal.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link SampleCapture} without a context (prompt 059): redaction, truncation and the ring of five. */
class SampleCaptureTest {

    static final Instant T = Instant.parse("2026-10-13T15:05:00Z");

    @Test
    @DisplayName("query and form credentials are masked by name, case-insensitively; JSON members too")
    void redaction() {
        SampleCapture.Sample form = SampleCapture.sample(T, "post", "ecowitt", "application/x-www-form-urlencoded", null,
                "ID=a&PASSWORD=eik_secret&key=k1", "PASSKEY=ABC&Password=p2&tempf=1".getBytes(StandardCharsets.UTF_8));
        assertThat(form.method()).isEqualTo("POST");
        assertThat(form.query()).isEqualTo("ID=a&PASSWORD=***&key=***");
        assertThat(form.body()).isEqualTo("PASSKEY=***&Password=***&tempf=1");
        SampleCapture.Sample json = SampleCapture.sample(T, "POST", null, "application/json", "gzip", null,
                "{\"Token\":\"t\\\"x\",\"auth\" : \"a\",\"keep\":\"v\"}".getBytes(StandardCharsets.UTF_8));
        assertThat(json.body()).isEqualTo("{\"Token\":\"***\",\"auth\" : \"***\",\"keep\":\"v\"}");
        assertThat(json.contentEncoding()).isEqualTo("gzip");
        assertThat(json.query()).isNull();
    }

    @Test
    @DisplayName("a long body is truncated and flagged; an empty body is null")
    void truncation() {
        byte[] big = "a".repeat(SampleCapture.MAX_BODY_CHARS + 10).getBytes(StandardCharsets.UTF_8);
        SampleCapture.Sample sample = SampleCapture.sample(T, "POST", "x", "text/plain", null, null, big);
        assertThat(sample.truncated()).isTrue();
        assertThat(sample.body()).hasSize(SampleCapture.MAX_BODY_CHARS);
        assertThat(sample.bodyBytes()).isEqualTo(big.length);
        assertThat(SampleCapture.sample(T, "GET", "gmc", null, null, "CPM=1", new byte[0]).body()).isNull();
    }

    @Test
    @DisplayName("five per source, newest first; clear forgets")
    void ring() {
        SampleCapture capture = new SampleCapture();
        for (int i = 0; i < 7; i++) {
            capture.capture("s", T.plusSeconds(i), "POST", "f", null, null, null, ("n=" + i).getBytes(StandardCharsets.UTF_8));
        }
        assertThat(capture.list("s")).hasSize(5).extracting(SampleCapture.Sample::body)
                .containsExactly("n=6", "n=5", "n=4", "n=3", "n=2");
        assertThat(capture.list("other")).isEmpty();
        capture.clear("s");
        assertThat(capture.list("s")).isEmpty();
        assertThat(capture.list("s")).isEmpty();
    }
}
