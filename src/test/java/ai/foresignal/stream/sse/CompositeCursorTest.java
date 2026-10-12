package ai.foresignal.stream.sse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Composite and single-seq resume cursors. */
class CompositeCursorTest {

    @Test
    @DisplayName("format and parse round-trip in order")
    void roundTrip() {
        Map<String, Long> cursors = new LinkedHashMap<>();
        cursors.put("c", 1234L);
        cursors.put("a", 56L);
        cursors.put("n", 7L);
        String id = CompositeCursor.format(cursors);
        assertThat(id).isEqualTo("c=1234;a=56;n=7");
        assertThat(CompositeCursor.parse("Last-Event-ID", id)).containsExactly(
                Map.entry("c", 1234L), Map.entry("a", 56L), Map.entry("n", 7L));
        assertThat(CompositeCursor.parse("Last-Event-ID", null)).isEmpty();
        assertThat(CompositeCursor.parse("Last-Event-ID", " ")).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"c", "c=", "=1", "c=1;", "C=1", "c=-1", "c=1;c=2", "c=99999999999999999999", "c=1,a=2"})
    @DisplayName("malformed composite ids are 400 INVALID_CURSOR")
    void malformed(String id) {
        assertThatThrownBy(() -> CompositeCursor.parse("lastEventId", id))
                .isInstanceOfSatisfying(ValidationException.class, e -> {
                    assertThat(e.getCode()).isEqualTo(ErrorCodes.INVALID_CURSOR);
                    assertThat(e.getErrors()).extracting(err -> err.field()).containsExactly("lastEventId");
                });
    }

    @Test
    @DisplayName("single seqs parse; anything else is 400 INVALID_CURSOR")
    void singleSeq() {
        assertThat(CompositeCursor.parseSeq("Last-Event-ID", "42")).isEqualTo(42L);
        assertThat(CompositeCursor.parseSeq("Last-Event-ID", null)).isNull();
        assertThatThrownBy(() -> CompositeCursor.parseSeq("Last-Event-ID", "c=1"))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ErrorCodes.INVALID_CURSOR));
        assertThatThrownBy(() -> CompositeCursor.parseSeq("Last-Event-ID", "-3"))
                .isInstanceOf(ValidationException.class);
    }
}
