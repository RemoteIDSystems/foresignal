package ai.foresignal.trigger;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The {@code |local} and {@code |ago} filters and extra variables of {@link MessageTemplate} (prompt 018). */
class TemplateFiltersTest {

    static final ZoneId CHICAGO = ZoneId.of("America/Chicago");

    static MessageTemplate.Context at(Instant observed) {
        return new MessageTemplate.Context("Rule", "src", "e1", "quake", "Title", "warning", observed, null, null, Map.of());
    }

    static MessageTemplate.Extras extras(Instant now) {
        return new MessageTemplate.Extras(CHICAGO, now, Map.of(), null);
    }

    @Test
    @DisplayName("|local formats in the zone across both DST transitions in America/Chicago")
    void localAcrossDst() {
        Instant now = Instant.parse("2026-12-01T00:00:00Z");
        // Before the spring-forward on 2026-03-08: CST
        assertThat(MessageTemplate.render("{{observedAt|local}}", at(Instant.parse("2026-03-08T07:30:00Z")), extras(now)))
                .isEqualTo("2026-03-08 01:30 CST");
        // After it: CDT
        assertThat(MessageTemplate.render("{{observedAt|local}}", at(Instant.parse("2026-03-08T08:30:00Z")), extras(now)))
                .isEqualTo("2026-03-08 03:30 CDT");
        // The repeated hour on 2026-11-01: first CDT, then CST
        assertThat(MessageTemplate.render("{{observedAt|local}}", at(Instant.parse("2026-11-01T06:30:00Z")), extras(now)))
                .isEqualTo("2026-11-01 01:30 CDT");
        assertThat(MessageTemplate.render("{{observedAt|local}}", at(Instant.parse("2026-11-01T07:30:00Z")), extras(now)))
                .isEqualTo("2026-11-01 01:30 CST");
    }

    @Test
    @DisplayName("|ago renders whole units relative to now, and the future as 'in …'")
    void ago() {
        Instant now = Instant.parse("2026-05-01T12:00:00Z");
        assertThat(MessageTemplate.render("{{observedAt|ago}}", at(now.minusSeconds(180)), extras(now))).isEqualTo("3 min ago");
        assertThat(MessageTemplate.render("{{observedAt | ago}}", at(now.minusSeconds(2)), extras(now))).isEqualTo("just now");
        assertThat(MessageTemplate.render("{{observedAt|ago}}", at(now.minusSeconds(45)), extras(now))).isEqualTo("45 s ago");
        assertThat(MessageTemplate.render("{{observedAt|ago}}", at(now.minusSeconds(7_300)), extras(now))).isEqualTo("2 h ago");
        assertThat(MessageTemplate.render("{{observedAt|ago}}", at(now.minusSeconds(3 * 86_400)), extras(now))).isEqualTo("3 d ago");
        assertThat(MessageTemplate.render("{{observedAt|ago}}", at(now.plusSeconds(600)), extras(now))).isEqualTo("in 10 min");
    }

    @Test
    @DisplayName("extra variables win over built-ins; escaping applies to substituted values; unknown filters are ignored")
    void varsAndEscape() {
        Instant t = Instant.parse("2026-05-01T12:00:00Z");
        MessageTemplate.Extras x = new MessageTemplate.Extras(CHICAGO, t, Map.of("summary", "S*", "severity", "critical"),
                v -> v.replace("*", "\\*"));
        assertThat(MessageTemplate.render("{{severity}} {{summary}} {{name}} {{observedAt|weird}}", at(t), x))
                .isEqualTo("critical S\\* Rule 2026-05-01T12:00:00Z");
        assertThat(MessageTemplate.render("{{observedAt}}", at(t))).isEqualTo("2026-05-01T12:00:00Z");
    }
}
