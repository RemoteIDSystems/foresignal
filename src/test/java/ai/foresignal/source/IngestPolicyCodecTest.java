package ai.foresignal.source;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.settings.DurationShorthandModule;
import ai.foresignal.source.spi.IngestPolicy;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** The one ingest-policy JSON shape shared by the API and {@code source_settings.ingest_policy}. */
class IngestPolicyCodecTest {

    private final IngestPolicyCodec codec =
            new IngestPolicyCodec(JsonMapper.builder().addModule(new DurationShorthandModule()).build());

    @Test
    @DisplayName("writes the discriminated shape in shorthand")
    void writes() {
        String json = codec.toJson(new IngestPolicy.Sampled(
                Duration.ofSeconds(60), Duration.ofSeconds(240), 1000.0, true, Duration.ofSeconds(30)));

        assertThat(json).contains("\"mode\":\"SAMPLED\"", "\"minInterval\":\"1m\"", "\"keyframe\":\"4m\"",
                "\"edgeMinInterval\":\"30s\"", "\"thresholds\":true");
    }

    @Test
    @DisplayName("a missing keyframe is written as none and read back as none")
    void keyframeNone() {
        String json = codec.toJson(IngestPolicy.all());

        assertThat(json).contains("\"keyframe\":\"none\"");
        assertThat(codec.fromJson(json)).isEqualTo(IngestPolicy.all());
        assertThat(codec.fromJson("{\"mode\":\"LATEST\",\"keyframe\":\"NONE\"}"))
                .isEqualTo(new IngestPolicy.Latest(null, null));
    }

    @Test
    @DisplayName("rows stored before 013 with a null keyframe still read")
    void legacyNull() {
        assertThat(codec.fromJson("{\"mode\":\"ALL\",\"keyframe\":null}")).isEqualTo(IngestPolicy.all());
        assertThat(codec.fromJson("{\"mode\":\"ALL\"}")).isEqualTo(IngestPolicy.all());
    }

    @Test
    @DisplayName("round-trips every mode")
    void roundTrip() {
        for (IngestPolicy policy : new IngestPolicy[] {
                IngestPolicy.all(Duration.ofMinutes(4)),
                new IngestPolicy.Sampled(Duration.ofSeconds(10), null, null, false, Duration.ofSeconds(5)),
                new IngestPolicy.Latest(Duration.ofMinutes(20), Duration.ofSeconds(15))}) {
            assertThat(codec.fromJson(codec.toJson(policy))).isEqualTo(policy);
            assertThat(codec.fromTree(codec.toTree(policy))).isEqualTo(policy);
        }
    }

    @Test
    @DisplayName("null and blank mean no policy; malformed JSON is a clear 400")
    void edges() {
        assertThat(codec.toJson(null)).isNull();
        assertThat(codec.toTree(null)).isNull();
        assertThat(codec.fromJson(null)).isNull();
        assertThat(codec.fromJson(" ")).isNull();
        assertThat(codec.fromTree(null)).isNull();
        assertThatThrownBy(() -> codec.fromJson("{not json")).isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> codec.fromJson("[1]")).isInstanceOf(InvalidInputException.class);
        assertThatThrownBy(() -> codec.fromJson("{\"mode\":\"NEVER\"}")).isInstanceOf(InvalidInputException.class);
    }
}
