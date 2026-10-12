package ai.foresignal.notify.push;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The text and ids native pushes carry (prompt 032). */
class NativeContentTest {

    static PushMessage message(String event, String severity, String shortTitle, String topic, String app, String web) {
        return new PushMessage(event, 1L, null, null, severity, "Full title", shortTitle, "Full body", null, app, web,
                List.of(), "foresignal-" + event + "-1", topic, false, 0, null, null);
    }

    @Test
    @DisplayName("minimal and short texts never include the full title or body")
    void texts() {
        PushMessage alert = message("alert", "info", "Short", null, null, null);
        assertThat(NativeContent.title(alert, "minimal")).isEqualTo("Foresignal");
        assertThat(NativeContent.body(alert, "minimal")).isEqualTo("Info alert");
        assertThat(NativeContent.title(alert, "short")).isEqualTo("Foresignal · info");
        assertThat(NativeContent.body(alert, "short")).isEqualTo("Short");
        assertThat(NativeContent.body(message("alert", null, "  ", null, null, null), "short")).isEqualTo("Info alert");
        assertThat(NativeContent.body(message("verify", "info", null, null, null, null), "minimal")).isEqualTo("System notice");
    }

    @Test
    @DisplayName("collapse id: a- plus the topic, else derived from the tag; at most 64 bytes")
    void collapseId() {
        assertThat(NativeContent.collapseId(message("alert", "info", null, "abc", null, null))).isEqualTo("a-abc");
        String derived = NativeContent.collapseId(message("system", "info", null, null, null, null));
        assertThat(derived).isEqualTo("a-" + PushTopic.derive("foresignal-system-1"));
        assertThat(NativeContent.collapseId(message("alert", "info", null, "x".repeat(100), null, null)))
                .hasSize(NativeContent.MAX_COLLAPSE_ID);
    }

    @Test
    @DisplayName("link prefers the app link, then the web link; thread id by rule, else event; category by event")
    void linksAndThreads() {
        assertThat(NativeContent.link(message("alert", "info", null, null, "foresignal://alerts/1", "https://w")))
                .isEqualTo("foresignal://alerts/1");
        assertThat(NativeContent.link(message("alert", "info", null, null, null, "https://w"))).isEqualTo("https://w");
        assertThat(NativeContent.link(message("alert", "info", null, null, null, null))).isNull();
        UUID rule = UUID.randomUUID();
        PushMessage ruled = new PushMessage("alert", 1L, 1L, rule, "info", "t", null, null, null, null, null, List.of(),
                "t", null, false, 0, null, null);
        assertThat(NativeContent.threadId(ruled)).isEqualTo("foresignal-" + rule);
        assertThat(NativeContent.category(ruled)).isEqualTo("FORESIGNAL_ALERT");
    }

    @Test
    @DisplayName("PushMessage keeps badge and category through its stored form")
    void storedForm() {
        PushMessage original = new PushMessage("briefing", 3L, null, null, "info", "t", null, null, null, null, null,
                List.of(), "t", null, false, 5, null, null, 4, "FORESIGNAL_BRIEFING");
        PushMessage read = PushMessage.fromMap(original.toMap());
        assertThat(read.badge()).isEqualTo(4);
        assertThat(read.category()).isEqualTo("FORESIGNAL_BRIEFING");
    }
}
