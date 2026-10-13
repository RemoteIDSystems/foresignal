package ai.foresignal.collect.socket;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link LineFramer} (prompt 069): LF and CRLF, lines split across reads, and whole-line drops. */
class LineFramerTest {

    private final List<String> lines = new ArrayList<>();
    private final AtomicInteger dropped = new AtomicInteger();
    private final LineFramer framer = new LineFramer(8, b -> lines.add(new String(b, StandardCharsets.US_ASCII)),
            dropped::incrementAndGet);

    private void feed(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.US_ASCII);
        framer.feed(bytes, 0, bytes.length);
    }

    @Test
    @DisplayName("LF and CRLF end lines; an empty line is delivered empty")
    void terminators() {
        feed("a\nbb\r\n\nccc\r\n");
        assertThat(lines).containsExactly("a", "bb", "", "ccc");
        assertThat(framer.pendingBytes()).isZero();
    }

    @Test
    @DisplayName("a line split across reads, even between CR and LF, is joined")
    void split() {
        feed("abc");
        feed("de\r");
        feed("\nx");
        assertThat(lines).containsExactly("abcde");
        assertThat(framer.pendingBytes()).isEqualTo(1);
    }

    @Test
    @DisplayName("a line of exactly the limit passes; a longer one is dropped once, through its terminator")
    void limit() {
        feed("12345678\r\n");
        feed("123456789");
        feed("0123\nok\n");
        assertThat(lines).containsExactly("12345678", "ok");
        assertThat(dropped.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("nine bytes then LF (the CR slack used by a payload byte) is dropped")
    void slackIsOnlyForCr() {
        feed("123456789\n");
        assertThat(lines).isEmpty();
        assertThat(dropped.get()).isEqualTo(1);
    }
}
