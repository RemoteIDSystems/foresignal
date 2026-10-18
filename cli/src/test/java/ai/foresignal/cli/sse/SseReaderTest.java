package ai.foresignal.cli.sse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/** The {@code text/event-stream} parser. */
class SseReaderTest {

    private static SseReader reader(String body) {
        return new SseReader(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void parsesNamedEventsWithIdsAndMultiLineData() throws IOException {
        try (SseReader r = reader("id: 7\nevent: change\ndata: {\"a\":1,\ndata: \"b\":2}\n\n")) {
            SseEvent e = r.next();
            assertEquals("change", e.event());
            assertEquals("7", e.id());
            assertEquals("{\"a\":1,\n\"b\":2}", e.data());
            assertNull(r.next());
        }
    }

    @Test
    void anEventWithoutAnIdReportsNoneEvenAfterAnEventWithOne() throws IOException {
        try (SseReader r = reader("id: 1\nevent: change\ndata: {}\n\nevent: live\ndata: {}\n\n")) {
            assertEquals("1", r.next().id());
            SseEvent live = r.next();
            assertEquals("live", live.event());
            assertNull(live.id(), "the resume position must not move on an id-less event");
        }
    }

    @Test
    void commentsAndEmptyBlocksAreSkippedAndUnnamedEventsAreMessages() throws IOException {
        try (SseReader r = reader(": ping\n\n\n\ndata:x\n\nfoo: bar\ndata: y\n\n")) {
            SseEvent first = r.next();
            assertEquals("message", first.event());
            assertEquals("x", first.data());
            assertEquals("y", r.next().data());
        }
    }

    @Test
    void retryIsRememberedAndReportedWithLaterEvents() throws IOException {
        try (SseReader r = reader("retry: 3000\n\nevent: heartbeat\ndata: {}\n\nretry: abc\nevent: x\ndata: 1\n\n")) {
            SseEvent hb = r.next();
            assertEquals(3000L, hb.retry());
            assertEquals(3000L, r.next().retry(), "an invalid retry is ignored");
            assertEquals(3000L, r.retry());
        }
    }

    @Test
    void handlesCrLfAndAFieldWithoutAColon() throws IOException {
        try (SseReader r = reader("event: a\r\ndata\r\n\r\n")) {
            SseEvent e = r.next();
            assertEquals("a", e.event());
            assertEquals("", e.data());
        }
    }

    @Test
    void anUnterminatedFinalEventIsDropped() throws IOException {
        try (SseReader r = reader("event: change\ndata: {}")) {
            assertNull(r.next());
        }
    }
}
