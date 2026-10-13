package ai.foresignal.weather;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static ai.foresignal.testsupport.trigger.TriggerTestSources.SAMPLED;
import static ai.foresignal.testsupport.trigger.TriggerTestSources.SAMPLED_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.trigger.TriggerFixture;
import ai.foresignal.testsupport.trigger.TriggerTestSources;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertEvent;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.TriggerEngine;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Alert weather context end to end (prompt 056): a geolocated alert stores {@code context.weather} before it is
 * dispatched (the {@code alert.fired} event already carries it, and the rule's template can use it); a rule with
 * {@code weatherContext=off}, the global {@code weather.alertContext=off} and an alert without geometry skip it; a
 * failing computation stores {@code {status: unavailable}} and the alert is still dispatched. The weather service is
 * a Mockito stand-in, so no frames are needed.
 */
@IntegrationTest
@Import(TriggerTestSources.class)
class AlertWeatherContextIT {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();
    static final String SUMMARY = "Radar max 40 dBZ within 10 km. Automated — not an official warning.";

    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired TriggerRuleService rules;
    @Autowired TriggerEngine engine;
    @Autowired AlertStore alertStore;
    @Autowired AlertBus bus;
    @Autowired ChangeBus changeBus;
    @Autowired WeatherSettingsService weatherSettings;

    @MockitoBean WeatherContextService weather;

    TriggerFixture fixture;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        reset(weather);
        when(weather.anyInputEnabled()).thenReturn(true);
        when(weather.now()).thenReturn(T);
        when(weather.compute(any(Geometry.class), any(Instant.class))).thenAnswer(i -> context(i.getArgument(1)));
        weatherSettings.put(WeatherSettings.defaults());
        fixture = new TriggerFixture(rules, engine, changeBus, alertStore, bus);
        if (!engine.running()) {
            engine.start();
        }
    }

    @AfterEach
    void tearDown() {
        fixture.close();
        weatherSettings.put(WeatherSettings.defaults());
    }

    static WeatherContext context(Instant at) {
        Map<String, Map<String, Object>> sections = new LinkedHashMap<>();
        sections.put("radar", WeatherSections.ok(Map.of("maxDbz", 40.0)));
        return new WeatherContext(WeatherSections.OK, at, T, sections, SUMMARY);
    }

    static NormalizedRecord vessel(String id, Instant at) {
        return NormalizedRecord.builder(id, SAMPLED_KIND).observedAt(at).point(-97.75, 30.27).put("name", "Vessel " + id)
                .build();
    }

    UUID vesselRule(String extra) {
        return fixture.rule("{'name':'Vessels','sources':['" + SAMPLED + "'],"
                + "'messageTemplate':'{{name}}: {{context.weather.summary}}'" + extra + "}");
    }

    static JsonNode weatherOf(AlertView alert) {
        if (alert.context() == null) {
            return null;
        }
        return JSON.readTree(alert.context()).get("weather");
    }

    @Test
    @DisplayName("a geolocated alert carries context.weather on its fired event and in its rendered message")
    void storedBeforeDispatch() {
        UUID rule = vesselRule("");
        records.ingest(SAMPLED, vessel("v1", T));

        List<AlertView> alerts = fixture.alerts(rule);
        assertThat(alerts).hasSize(1);
        AlertEvent fired = fixture.awaitEvent(AlertEvent.FIRED, alerts.getFirst().id());
        JsonNode stored = weatherOf(fired.alert());
        assertThat(stored).as("the event published after commit already carries the context").isNotNull();
        assertThat(stored.get("status").asString()).isEqualTo("ok");
        assertThat(stored.get("summary").asString()).isEqualTo(SUMMARY);
        assertThat(stored.get("radar").get("maxDbz").asDouble()).isEqualTo(40.0);
        assertThat(stored.get("at").asString()).isEqualTo(T.toString());
        assertThat(fired.alert().message()).isEqualTo("Vessels: " + SUMMARY);
    }

    @Test
    @DisplayName("rule weatherContext=off skips it")
    void ruleOff() {
        UUID rule = vesselRule(",'weatherContext':'off'");
        records.ingest(SAMPLED, vessel("v2", T));

        AlertView alert = fixture.alerts(rule).getFirst();
        assertThat(weatherOf(alert)).isNull();
        assertThat(alert.message()).isEqualTo("Vessels: ");
    }

    @Test
    @DisplayName("global weather.alertContext=off skips it")
    void globalOff() {
        weatherSettings.put(new WeatherSettings(WeatherSettings.OFF, null, null));
        UUID rule = vesselRule("");
        records.ingest(SAMPLED, vessel("v3", T));

        assertThat(weatherOf(fixture.alerts(rule).getFirst())).isNull();
        verify(weather, never()).compute(any(Geometry.class), any(Instant.class));
    }

    @Test
    @DisplayName("an alert without geometry skips it")
    void noGeometry() {
        UUID rule = fixture.rule("{'name':'Quakes','sources':['" + STANDARD + "']}");
        records.ingest(STANDARD, NormalizedRecord.builder("q1", STANDARD_KIND).observedAt(T).put("mag", 5.0).build());

        assertThat(weatherOf(fixture.alerts(rule).getFirst())).isNull();
        verify(weather, never()).compute(any(Geometry.class), any(Instant.class));
    }

    @Test
    @DisplayName("a failing computation stores status unavailable and the alert is still dispatched")
    void failingStillDispatches() {
        when(weather.compute(any(Geometry.class), any(Instant.class))).thenThrow(new IllegalStateException("frames gone"));
        UUID rule = vesselRule("");
        records.ingest(SAMPLED, vessel("v4", T));

        AlertView alert = fixture.alerts(rule).getFirst();
        AlertEvent fired = fixture.awaitEvent(AlertEvent.FIRED, alert.id());
        JsonNode stored = weatherOf(fired.alert());
        assertThat(stored.get("status").asString()).isEqualTo("unavailable");
        assertThat(stored.get("summary").asString()).isEqualTo(WeatherSummary.UNAVAILABLE);
    }
}
