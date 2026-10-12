package ai.foresignal.trigger;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link MessageTemplate} (prompt 017, task 4). */
class MessageTemplateTest {

    static final Instant OBSERVED = Instant.parse("2026-05-01T12:00:00Z");
    static final Instant EVENT = Instant.parse("2026-05-01T11:59:00Z");

    static MessageTemplate.Context context(Instant eventTime, Map<String, ?> payload) {
        return new MessageTemplate.Context("Big quake", "quakes", "us123", "earthquake", "Near Here", "critical",
                OBSERVED, eventTime, Instant.parse("2026-05-02T00:00:00Z"), payload);
    }

    @Test
    @DisplayName("the default template")
    void defaultTemplate() {
        assertThat(MessageTemplate.render(null, context(null, Map.of()))).isEqualTo("Big quake: quakes/us123");
        assertThat(MessageTemplate.render("  ", context(null, Map.of()))).isEqualTo("Big quake: quakes/us123");
    }

    @Test
    @DisplayName("every placeholder, with whitespace inside the braces")
    void placeholders() {
        String out = MessageTemplate.render(
                "{{name}}|{{source}}|{{externalId}}|{{kind}}|{{title}}|{{severity}}|{{observedAt}}|{{eventTime}}|{{time}}|{{ validFrom }}",
                context(EVENT, Map.of()));
        assertThat(out).isEqualTo("Big quake|quakes|us123|earthquake|Near Here|critical|2026-05-01T12:00:00Z"
                + "|2026-05-01T11:59:00Z|2026-05-01T11:59:00Z|2026-05-02T00:00:00Z");
    }

    @Test
    @DisplayName("time falls back to observedAt without an event time")
    void effectiveTime() {
        assertThat(MessageTemplate.render("{{time}}|{{eventTime}}", context(null, Map.of()))).isEqualTo("2026-05-01T12:00:00Z|");
    }

    @Test
    @DisplayName("payload paths: numbers canonical, nested, arrays, objects and missing render as nothing")
    void payload() {
        Map<String, Object> payload = Map.of("mag", 6.10, "place", "Here", "n", Map.of("deep", 3L), "list", List.of(1, 2),
                "flag", true);
        assertThat(MessageTemplate.render("M{{payload.mag}} {{payload.place}} {{payload.n.deep}} {{payload.list.1}}"
                + " [{{payload.n}}] [{{payload.missing}}] {{payload.flag}} [{{payload.}}]", context(null, payload)))
                .isEqualTo("M6.1 Here 3 2 [] [] true []");
    }

    @Test
    @DisplayName("unknown placeholders render as nothing; substituted values are never re-scanned")
    void noRecursion() {
        assertThat(MessageTemplate.render("a{{nope}}b", context(null, Map.of()))).isEqualTo("ab");
        assertThat(MessageTemplate.render("{{payload.x}}", context(null, Map.of("x", "{{name}}")))).isEqualTo("{{name}}");
        assertThat(MessageTemplate.render("open {{name", context(null, Map.of()))).isEqualTo("open {{name");
    }

    @Test
    @DisplayName("output is cut to 2,000 characters")
    void cap() {
        String big = "x".repeat(1500);
        String out = MessageTemplate.render("{{payload.a}}{{payload.a}}", context(null, Map.of("a", big)));
        assertThat(out).hasSize(MessageTemplate.MAX_OUTPUT);
    }
}
