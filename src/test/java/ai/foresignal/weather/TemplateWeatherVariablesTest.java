package ai.foresignal.weather;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.AlertTemplates;
import ai.foresignal.trigger.MessageTemplate;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The weather template variables (prompt 056): {@code {{context.weather.summary}}} and the section fields render
 * in rule and channel templates; a missing context renders empty; the default body of the six chat and push types
 * appends the summary line, other types and custom templates do not.
 */
class TemplateWeatherVariablesTest {

    static final Instant T = Instant.parse("2026-10-12T21:00:00Z");
    static final String SUMMARY = "Radar max 52 dBZ within 10 km; lightning data stale since 20:50 UTC. Automated — not"
            + " an official warning.";

    static Map<String, Object> weather() {
        Map<String, Object> radar = new LinkedHashMap<>();
        radar.put("status", "ok");
        radar.put("maxDbz", 52.0);
        radar.put("nearestCell", Map.of("id", "c-1", "distanceKm", 8.5, "motion", Map.of("speedKmh", 45,
                "bearingDeg", 70)));
        Map<String, Object> weather = new LinkedHashMap<>();
        weather.put("status", "ok");
        weather.put("radar", radar);
        weather.put("lightning", Map.of("status", "stale", "asOf", "2026-10-12T20:50:00Z"));
        weather.put("warnings", Map.of("status", "ok", "items", List.of(Map.of("id", "w1", "event", "Flood Warning"))));
        weather.put("wind", new LinkedHashMap<>(Map.of("status", "ok", "gustMs", 12.3, "speedMs", 7.0)));
        weather.put("summary", SUMMARY);
        return weather;
    }

    static MessageTemplate.Context context() {
        return new MessageTemplate.Context("Storm near home", "nws-alerts", "w1", "nws-alert", "Flood Warning", "warning",
                T, null, null, Map.of());
    }

    static String render(String template, Map<String, String> vars) {
        return MessageTemplate.render(template, context(), new MessageTemplate.Extras(ZoneOffset.UTC, T, vars, null));
    }

    @Test
    @DisplayName("the summary and section fields render, numbers canonically, nested paths and list items included")
    void fields() {
        Map<String, String> vars = WeatherTemplateVars.of(weather());

        assertThat(render("{{context.weather.summary}}", vars)).isEqualTo(SUMMARY);
        assertThat(render("{{context.weather.radar.maxDbz}} dBZ", vars)).isEqualTo("52 dBZ");
        assertThat(render("{{context.weather.radar.nearestCell.distanceKm}} km", vars)).isEqualTo("8.5 km");
        assertThat(render("{{context.weather.radar.nearestCell.motion.speedKmh}}", vars)).isEqualTo("45");
        assertThat(render("{{context.weather.lightning.status}} since {{context.weather.lightning.asOf}}", vars))
                .isEqualTo("stale since 2026-10-12T20:50:00Z");
        assertThat(render("{{context.weather.warnings.items.0.event}}", vars)).isEqualTo("Flood Warning");
        assertThat(render("gusts {{context.weather.wind.gustMs}} m/s", vars)).isEqualTo("gusts 12.3 m/s");
        assertThat(vars).doesNotContainKey("context.weather.radar").doesNotContainKey("context.weather.radar.nearestCell");
    }

    @Test
    @DisplayName("a missing context, section or field renders empty")
    void missing() {
        assertThat(render("[{{context.weather.summary}}]", WeatherTemplateVars.of(null))).isEqualTo("[]");
        assertThat(render("[{{context.weather.smoke.pm25UgM3}}]", WeatherTemplateVars.of(weather()))).isEqualTo("[]");
        assertThat(WeatherTemplateVars.fromAlertContext(null)).isEmpty();
        assertThat(WeatherTemplateVars.fromAlertContext(Map.of("near", Map.of("placeIds", List.of("home"))))).isEmpty();
        assertThat(WeatherTemplateVars.fromAlertContext(Map.of("weather", weather())))
                .containsEntry(WeatherTemplateVars.SUMMARY, SUMMARY);
    }

    static Map<String, Object> data(boolean withWeather) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ruleName", "Storm near home");
        data.put("sourceId", "nws-alerts");
        data.put("externalId", "w1");
        data.put("kind", "nws-alert");
        data.put("severity", "warning");
        data.put("observedAt", T.toString());
        data.put("message", "Flood Warning near home");
        if (withWeather) {
            data.put("context", Map.of("weather", weather()));
        }
        return data;
    }

    @Test
    @DisplayName("channel templates: weather variables from the notification data; custom templates use them")
    void channelTemplates() {
        AlertTemplates.Text text = AlertTemplates.render(null, "{{message}} | {{context.weather.radar.maxDbz}} dBZ",
                data(true), "https://x/v1/alerts/1", ZoneOffset.UTC, T);
        assertThat(text.body()).isEqualTo("Flood Warning near home | 52 dBZ");
    }

    @Test
    @DisplayName("default bodies: the six types append the summary line when present; nothing without a context")
    void defaultBodies() {
        AlertTemplates.Text slack = AlertTemplates.render(null, null, data(true), "u", ZoneOffset.UTC, T, false, true);
        assertThat(slack.body()).contains("Radar max 52 dBZ within 10 km").contains("not an official warning");

        AlertTemplates.Text webhook = AlertTemplates.render(null, null, data(true), "u", ZoneOffset.UTC, T, false, false);
        assertThat(webhook.body()).doesNotContain("Radar max");

        AlertTemplates.Text noContext = AlertTemplates.render(null, null, data(false), "u", ZoneOffset.UTC, T, false,
                true);
        assertThat(noContext.body()).doesNotContain("Radar").doesNotContain("\n\n\n");

        assertThat(AlertTemplates.WEATHER_LINE_TYPES).containsExactlyInAnyOrder("discord", "telegram", "email", "ntfy",
                "pushover", "slack");
    }
}
