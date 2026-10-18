package ai.foresignal.briefing;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.api.error.FieldError;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** The {@code audio} option of a briefing (prompt 141): parsing, defaults, storage form and the run's outcome object. */
class BriefingAudioOptionTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    static BriefingInput.Audio parse(String json, List<FieldError> errors) {
        return BriefingValidator.audio(json == null ? null : JSON.readTree(json), errors);
    }

    @Test
    @DisplayName("null or absent is no audio; a voice alone defaults to mp3; opus is accepted")
    void valid() {
        List<FieldError> errors = new ArrayList<>();
        assertThat(parse(null, errors)).isNull();
        assertThat(parse("null", errors)).isNull();
        assertThat(parse("{\"voice\":\" alloy \"}", errors)).isEqualTo(new BriefingInput.Audio("alloy", "mp3"));
        assertThat(parse("{\"voice\":\"af_bella\",\"format\":\"opus\"}", errors))
                .isEqualTo(new BriefingInput.Audio("af_bella", "opus"));
        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("every problem is reported: not an object, missing or long voice, unknown format, unknown member")
    void invalid() {
        List<FieldError> errors = new ArrayList<>();
        assertThat(parse("\"alloy\"", errors)).isNull();
        assertThat(parse("{\"format\":\"wav\",\"speed\":2}", errors)).isNull();
        assertThat(parse("{\"voice\":\"" + "v".repeat(101) + "\"}", errors)).isNull();
        assertThat(errors).extracting(FieldError::field)
                .containsExactly("audio", "audio.speed", "audio.voice", "audio.format", "audio.voice");
    }

    @Test
    @DisplayName("stored as {voice, format} and read back; an unreadable stored value is no audio")
    void storage() {
        BriefingInput.Audio audio = new BriefingInput.Audio("alloy", null);
        assertThat(audio.format()).isEqualTo("mp3");
        assertThat(audio.toMap()).containsExactly(Map.entry("voice", "alloy"), Map.entry("format", "mp3"));
        assertThat(BriefingInput.Audio.fromMap(audio.toMap())).isEqualTo(audio);
        assertThat(BriefingInput.Audio.fromMap(Map.of("voice", ""))).isNull();
        assertThat(BriefingInput.Audio.fromMap(null)).isNull();
        assertThat(BriefingInput.Audio.fromMap(Map.of("voice", "v"))).isEqualTo(new BriefingInput.Audio("v", "mp3"));
    }

    @Test
    @DisplayName("the run's audio object starts with every member, nothing synthesized")
    void outcomeShape() {
        Map<String, Object> out = BriefingAudioService.result(BriefingAudioService.SKIPPED,
                new BriefingInput.Audio("alloy", "opus"));
        assertThat(out).containsEntry("status", "skipped").containsEntry("format", "opus").containsEntry("voice", "alloy")
                .containsEntry("mediaId", null).containsEntry("characters", null).containsEntry("truncated", false)
                .containsEntry("error", null);
    }
}
