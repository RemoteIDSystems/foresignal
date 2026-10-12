package ai.foresignal.notify.push;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The 3993-byte plaintext budget and its shrink order (prompt 031). */
class PushPayloadBudgetTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final PushPayloadBudget BUDGET = new PushPayloadBudget(JSON);

    static PushMessage message(String title, String body, List<String> tags) {
        return new PushMessage("alert", 42L, 7L, UUID.fromString("00000000-0000-0000-0000-0000000000c1"), "warning", title,
                "Short", body, "https://fs.example/v1/alerts/7", "foresignal://alerts/7", "https://fs.example/a/7", tags,
                "foresignal-alert-7", "topic", false, 1_700_000_000_000L, null, null);
    }

    static JsonNode parse(byte[] bytes) {
        return JSON.readTree(new String(bytes, StandardCharsets.UTF_8));
    }

    static List<String> tags(int n) {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            out.add("tag-" + i);
        }
        return out;
    }

    @Test
    @DisplayName("a small payload is untouched: every member present, links and url alias")
    void untouched() {
        JsonNode p = parse(BUDGET.fit(message("Quake M5.1", "Near Austin", List.of("quake")), "foresignal", "en"));
        assertThat(p.get("v").asInt()).isEqualTo(1);
        assertThat(p.get("event").asString()).isEqualTo("alert");
        assertThat(p.get("id").asLong()).isEqualTo(42);
        assertThat(p.get("alertId").asLong()).isEqualTo(7);
        assertThat(p.get("body").asString()).isEqualTo("Near Austin");
        assertThat(p.get("url").asString()).isEqualTo(p.get("links").get("api").asString());
        assertThat(p.get("links").get("app").asString()).isEqualTo("foresignal://alerts/7");
        assertThat(p.get("lang").asString()).isEqualTo("en");
        assertThat(p.get("tag").asString()).isEqualTo("foresignal-alert-7");
        assertThat(p.get("silent").asBoolean()).isFalse();
    }

    @Test
    @DisplayName("an oversize multibyte body is cut first, on code points, with an ellipsis, within 3993 bytes")
    void bodyFirst() {
        String body = "😀é漢".repeat(600); // 4 + 2 + 3 bytes, 4 UTF-16 units each
        byte[] out = BUDGET.fit(message("Title ✓", body, tags(8)), "foresignal", "en");
        assertThat(out.length).isLessThanOrEqualTo(3993);
        JsonNode p = parse(out);
        String cut = p.get("body").asString();
        assertThat(cut).endsWith("…");
        assertThat(cut.length()).isLessThan(body.length());
        // Never a lone surrogate.
        for (int i = 0; i < cut.length(); i++) {
            char c = cut.charAt(i);
            if (Character.isHighSurrogate(c)) {
                assertThat(i + 1).isLessThan(cut.length());
                assertThat(Character.isLowSurrogate(cut.charAt(i + 1))).isTrue();
            } else if (Character.isLowSurrogate(c)) {
                assertThat(i).isPositive();
                assertThat(Character.isHighSurrogate(cut.charAt(i - 1))).isTrue();
            }
        }
        // Later steps were not needed.
        assertThat(p.get("links").has("api")).isTrue();
        assertThat(p.get("tags")).hasSize(8);
        assertThat(p.get("title").asString()).isEqualTo("Title ✓");
    }

    @Test
    @DisplayName("when the body is gone, links.api (and url) go, then tags are capped at 5, then the title is cut to 120")
    void shrinkOrder() {
        String longTitle = "T".repeat(3800);
        List<String> manyTags = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            manyTags.add("x".repeat(20) + i);
        }
        JsonNode p = parse(BUDGET.fit(message(longTitle, "body", manyTags), "foresignal", "en"));
        assertThat(p.get("body").asString()).isEmpty();
        assertThat(p.has("url")).isFalse();
        assertThat(p.get("links").has("api")).isFalse();
        assertThat(p.get("tags")).hasSize(5);
        assertThat(p.get("title").asString()).hasSizeLessThanOrEqualTo(120).endsWith("…");

        // Tags alone overflow: capped, title kept.
        List<String> hugeTags = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            hugeTags.add("tag-" + "y".repeat(10) + i);
        }
        JsonNode q = parse(BUDGET.fit(message("Kept title", "b", hugeTags), "foresignal", "en"));
        assertThat(q.get("tags")).hasSize(5);
        assertThat(q.get("title").asString()).isEqualTo("Kept title");
    }

    @Test
    @DisplayName("a payload that cannot fit falls back to {v, event, id, severity, shortTitle}")
    void minimalFallback() {
        // 130 three-byte characters: still 360 bytes after the 120-character cut, more than a 120-byte cap.
        String hugeTitle = "€".repeat(130);
        PushPayloadBudget tiny = new PushPayloadBudget(JSON, 120);
        byte[] out = tiny.fit(message(hugeTitle, "body", tags(3)), "foresignal", "en");
        assertThat(out.length).isLessThanOrEqualTo(120);
        JsonNode p = parse(out);
        assertThat(p.properties().stream().map(java.util.Map.Entry::getKey).toList())
                .containsExactly("v", "event", "id", "severity", "shortTitle");
        assertThat(new PushPayloadBudget(JSON, 10).fit(message(hugeTitle, "b", List.of()), "foresignal", "en").length)
                .isLessThanOrEqualTo(30);
    }

    @Test
    @DisplayName("the declarative format wraps the payload, navigates to links.web, and also stays within the cap")
    void declarative() {
        JsonNode p = parse(BUDGET.fit(message("Quake", "Near Austin", List.of("q")), "declarative", "de"));
        assertThat(p.get("web_push").asInt()).isEqualTo(8030);
        JsonNode n = p.get("notification");
        assertThat(n.get("title").asString()).isEqualTo("Quake");
        assertThat(n.get("body").asString()).isEqualTo("Near Austin");
        assertThat(n.get("navigate").asString()).isEqualTo("https://fs.example/a/7");
        assertThat(n.get("lang").asString()).isEqualTo("de");
        assertThat(n.get("tag").asString()).isEqualTo("foresignal-alert-7");
        assertThat(n.has("app_badge")).isFalse();
        assertThat(p.has("app_badge")).isFalse();
        assertThat(n.get("data").has("title")).isFalse();
        assertThat(n.get("data").has("body")).isFalse();
        assertThat(n.get("data").get("alertId").asLong()).isEqualTo(7);

        byte[] big = BUDGET.fit(message("Quake", "ü".repeat(5000), tags(3)), "declarative", "de");
        assertThat(big.length).isLessThanOrEqualTo(3993);
    }

    @Test
    @DisplayName("every shape of oversize input stays within 3993 bytes")
    void neverExceeds() {
        String[] samples = {"a", "é", "😀", "漢", "\"", "\\", "\u0001", "👩‍👩‍👧"};
        for (String s : samples) {
            for (int n : new int[] {1000, 2000, 5000}) {
                byte[] out = BUDGET.fit(message(s.repeat(n), s.repeat(n), List.of(s.repeat(50))), "foresignal", "en");
                assertThat(out.length).as(s + " × " + n).isLessThanOrEqualTo(3993);
                parse(out);
            }
        }
    }

    @Test
    @DisplayName("the verify payload is {v, event, subscriptionId, code}")
    void verify() {
        UUID id = UUID.randomUUID();
        JsonNode p = parse(BUDGET.fit(PushMessage.verify(id, "abc", 1L, "https://fs.example"), "foresignal", "en"));
        assertThat(p.properties().stream().map(java.util.Map.Entry::getKey).toList())
                .containsExactly("v", "event", "subscriptionId", "code");
        assertThat(p.get("subscriptionId").asString()).isEqualTo(id.toString());
        JsonNode d = parse(BUDGET.fit(PushMessage.verify(id, "abc", 1L, "https://fs.example"), "declarative", "en"));
        assertThat(d.get("notification").get("navigate").asString()).isEqualTo("https://fs.example");
        assertThat(d.get("notification").get("body").asString()).contains("abc");
        assertThat(d.get("notification").get("data").get("code").asString()).isEqualTo("abc");
    }
}
