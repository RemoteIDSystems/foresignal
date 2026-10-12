package ai.foresignal.api;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.sse.SseClient;
import ai.foresignal.stream.sse.SseHub;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.stream.SseTestClient;
import ai.foresignal.testsupport.stream.SseTestClient.Event;
import ai.foresignal.testsupport.trigger.TriggerFixture;
import ai.foresignal.testsupport.trigger.TriggerTestSources;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertService;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.TriggerEngine;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code GET /v1/alerts/stream} and the {@code alerts} topic of {@code /v1/events/stream} (prompt 017). */
@WebIntegrationTest
@Import(TriggerTestSources.class)
class AlertStreamIT {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final Duration WAIT = Duration.ofSeconds(5);
    static final JsonMapper JSON = JsonMapper.builder().build();

    @LocalServerPort int port;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired SseHub hub;
    @Autowired AppSettingService settings;
    @Autowired AdminTokenService adminTokens;
    @Autowired TriggerRuleService rules;
    @Autowired TriggerEngine engine;
    @Autowired ChangeBus changeBus;
    @Autowired AlertStore alerts;
    @Autowired AlertService alertService;
    @Autowired AlertBus bus;

    final List<SseTestClient> streams = new ArrayList<>();
    TriggerFixture fixture;
    UUID warning;
    UUID critical;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        settings.invalidateCache();
        fixture = new TriggerFixture(rules, engine, changeBus, alerts, bus);
        warning = fixture.rule("{'name':'Warn','sources':['" + STANDARD + "'],'conditions':[{'path':'mag','op':'>=','value':5}]}");
        critical = fixture.rule("{'name':'Crit','sources':['" + STANDARD + "'],'severity':'critical',"
                + "'conditions':[{'path':'mag','op':'>=','value':7}]}");
    }

    @AfterEach
    void tearDown() {
        streams.forEach(SseTestClient::close);
        hub.clients().forEach(SseClient::close);
        fixture.close();
    }

    SseTestClient open(String path, Map<String, String> headers) {
        SseTestClient stream = SseTestClient.connect("http://localhost:" + port, path, headers);
        streams.add(stream);
        return stream;
    }

    void quake(String id, double mag) {
        records.ingest(STANDARD, NormalizedRecord.builder(id, STANDARD_KIND).observedAt(T).put("mag", mag).build());
    }

    static JsonNode data(Event event) {
        return JSON.readTree(event.data());
    }

    @Test
    @DisplayName("fired, updated and resolved events arrive within 5 s, with id = rev")
    void lifecycle() {
        SseTestClient stream = open("/v1/alerts/stream?ruleId=" + warning, Map.of());
        assertThat(stream.status()).isEqualTo(200);
        assertThat(stream.header("Cache-Control")).isEqualTo("no-cache");
        quake("a", 5.5);
        Event fired = stream.next("alert", WAIT);
        assertThat(fired).isNotNull();
        JsonNode body = data(fired);
        assertThat(body.get("type").asString()).isEqualTo("fired");
        assertThat(body.get("suppressed").asBoolean()).isFalse();
        long id = body.get("alert").get("id").asLong();
        assertThat(fired.id()).isEqualTo(body.get("alert").get("rev").asString());

        alertService.acknowledge(id, AlertService.Transition.NONE, "admin");
        Event updated = stream.next("alert.updated", WAIT);
        assertThat(updated).isNotNull();
        assertThat(data(updated).get("type").asString()).isEqualTo("acknowledged");
        assertThat(Long.parseLong(updated.id())).isGreaterThan(Long.parseLong(fired.id()));

        alertService.resolve(id, AlertService.Transition.NONE, "admin");
        Event resolved = stream.next("alert.resolved", WAIT);
        assertThat(resolved).isNotNull();
        assertThat(data(resolved).get("alert").get("state").asString()).isEqualTo("resolved");
    }

    @Test
    @DisplayName("a resume replays the alerts with a newer rev, once each, in their current state")
    void resume() {
        quake("a", 5.5);
        List<AlertView> first = fixture.alerts(warning);
        long rev = first.getFirst().rev();
        quake("b", 5.6);
        AlertView second = fixture.alerts(warning).get(1);
        alertService.acknowledge(second.id(), AlertService.Transition.NONE, "admin");
        alertService.resolve(first.getFirst().id(), AlertService.Transition.NONE, "admin");

        SseTestClient stream = open("/v1/alerts/stream?ruleId=" + warning, Map.of("Last-Event-ID", Long.toString(rev)));
        Event one = stream.next(WAIT);
        Event two = stream.next(WAIT);
        assertThat(List.of(one.event(), two.event())).containsExactly("alert.updated", "alert.resolved");
        assertThat(data(one).get("alert").get("id").asLong()).isEqualTo(second.id());
        assertThat(Long.parseLong(two.id())).isGreaterThan(Long.parseLong(one.id()));
        assertThat(stream.next("alert", Duration.ofMillis(500))).as("nothing replayed twice").isNull();

        assertThat(open("/v1/alerts/stream", Map.of("Last-Event-ID", "x")).status()).isEqualTo(400);
    }

    @Test
    @DisplayName("minSeverity filters, and suppressed alerts appear only with includeSuppressed")
    void filters() {
        SseTestClient criticalOnly = open("/v1/alerts/stream?minSeverity=critical", Map.of());
        SseTestClient withSuppressed = open("/v1/alerts/stream?includeSuppressed=true&ruleId=" + warning, Map.of());
        SseTestClient plain = open("/v1/alerts/stream?ruleId=" + warning, Map.of());
        quake("c", 5.5);
        assertThat(plain.next("alert", WAIT)).isNotNull();
        assertThat(criticalOnly.next("alert", Duration.ofMillis(700))).isNull();
        quake("d", 7.5);
        Event crit = criticalOnly.next("alert", WAIT);
        assertThat(data(crit).get("alert").get("severity").asString()).isEqualTo("critical");

        assertThat(withSuppressed.next("alert", WAIT)).isNotNull();
        withSuppressed.next("alert", WAIT);
        rules.snooze(warning, T.plusSeconds(3600));
        quake("e", 5.5);
        Event suppressed = withSuppressed.next("alert", WAIT);
        assertThat(suppressed).isNotNull();
        assertThat(data(suppressed).get("suppressed").asBoolean()).isTrue();
        assertThat(plain.collect("alert", Duration.ofMillis(700))).as("hidden without includeSuppressed")
                .allSatisfy(e -> assertThat(data(e).get("suppressed").asBoolean()).isFalse());
        assertThat(open("/v1/alerts/stream?minSeverity=high", Map.of()).status()).isEqualTo(400);
        assertThat(open("/v1/alerts/stream?source=nope", Map.of()).status()).isEqualTo(404);
    }

    @Test
    @DisplayName("the alerts topic on /v1/events/stream uses the composite id a=<rev>")
    void eventsTopic() {
        SseTestClient stream = open("/v1/events/stream?topics=changes,alerts&alerts.minSeverity=warning", Map.of());
        JsonNode hello = data(stream.next("hello", WAIT));
        assertThat(hello.get("topics").toString()).contains("alerts");
        quake("f", 5.5);
        Event alert = stream.next("alert", WAIT);
        assertThat(alert).isNotNull();
        assertThat(alert.id()).matches("^c=\\d+;a=" + data(alert).get("alert").get("rev").asString() + "$");
    }
}
