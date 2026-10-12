package ai.foresignal.notify;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link DigestBuilder} (prompt 018). */
class DigestBuilderTest {

    static NotificationMessage alert(long id, String severity, String rule) {
        return NotificationMessage.builder("alert", "alert.fired", "Alert " + id + " a_b", severity).id(id)
                .url("http://h/v1/alerts/" + id).data(Map.of("ruleName", rule)).build();
    }

    @Test
    @DisplayName("newest first, capped, '+N more alerts', highest severity, the newest link")
    void digest() {
        List<NotificationMessage> items = new ArrayList<>();
        for (long id = 1; id <= 5; id++) {
            items.add(alert(id, id == 2 ? "critical" : "info", id % 2 == 0 ? "A" : "B"));
        }
        NotificationMessage digest = DigestBuilder.build(items, 3);
        assertThat(digest.subtype()).isEqualTo("alert.digest");
        assertThat(digest.severity()).isEqualTo("critical");
        assertThat(digest.title()).isEqualTo("[critical] 5 alerts");
        assertThat(digest.url()).isEqualTo("http://h/v1/alerts/5");
        assertThat(digest.bodyMarkdown()).startsWith("- **[info]** Alert 5 a\\_b\n- **[info]** Alert 4 a\\_b\n- **[info]** Alert 3")
                .endsWith("+2 more alerts");
        assertThat(digest.data()).containsEntry("count", 5);
    }

    @Test
    @DisplayName("one rule: its name is in the title and there is no '+N more' within the cap")
    void oneRule() {
        NotificationMessage digest = DigestBuilder.build(List.of(alert(1, "warning", "Quakes"), alert(2, "info", "Quakes")), 20);
        assertThat(digest.title()).isEqualTo("[warning] Quakes: 2 alerts");
        assertThat(digest.bodyMarkdown()).doesNotContain("more alerts");
    }
}
