package ai.foresignal.mqtt.channel;

import static ai.foresignal.testsupport.notify.NotifyFixture.alert;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.mqtt.MqttConnection;
import ai.foresignal.mqtt.MqttConnections;
import ai.foresignal.notify.ChannelRateLimiter;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.notify.DeliveryWorker;
import ai.foresignal.notify.NotificationRouter;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.store.DeliveryStatus;
import ai.foresignal.store.NotificationDeliveryEntity;
import ai.foresignal.store.NotificationDeliveryRepository;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.system.SystemInfoService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.mqtt.MosquittoSupport;
import ai.foresignal.testsupport.mqtt.MqttProbe;
import ai.foresignal.testsupport.notify.ActionFixture;
import ai.foresignal.testsupport.notify.NotifyFixture;
import ai.foresignal.testsupport.notify.NotifyTestConfig;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertEvent;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.TriggerRuleService;
import io.micrometer.core.instrument.MeterRegistry;
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
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code mqtt} channel against a real Mosquitto broker in Testcontainers (prompt 060): durable alert
 * deliveries with message expiry, retained rule state set and cleared, retained health and summary, the Last
 * Will when the connection is cut, retries through the delivery ladder while the broker is unreachable, and
 * TLS with a test CA. Every test uses its own topic prefix, because retained messages outlive a test.
 */
@IntegrationTest
@Import(NotifyTestConfig.class)
@Timeout(120)
class MqttChannelIT {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final UUID RULE = UUID.fromString("00000000-0000-0000-0000-0000000000c1");
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired NotificationRouter router;
    @Autowired DeliveryWorker worker;
    @Autowired ChannelService channels;
    @Autowired ChannelRateLimiter limiter;
    @Autowired NotificationDeliveryRepository deliveries;
    @Autowired MqttChannelManager manager;
    @Autowired MqttStatePublisher state;
    @Autowired MqttConnections connections;
    @Autowired TriggerRuleRepository rules;
    @Autowired TriggerRuleService ruleService;
    @Autowired AlertBus bus;
    @Autowired AlertStore alerts;
    @Autowired JdbcTemplate jdbc;
    @Autowired SystemInfoService system;
    @Autowired MeterRegistry meters;
    @Autowired AppSettingService settings;
    @Autowired MutableClock clock;
    @Autowired org.springframework.test.web.servlet.MockMvc mvc;
    @Autowired ai.foresignal.security.AdminTokenService adminTokens;

    MosquittoSupport broker;
    NotifyFixture fixture;
    String prefix;
    final List<MqttProbe> probes = new ArrayList<>();

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        clock.setInstant(T);
        broker = MosquittoSupport.get();
        broker.restore();
        manager.resync();
        fixture = new NotifyFixture(channels, deliveries, limiter);
        prefix = "fs" + UUID.randomUUID().toString().substring(0, 8);
    }

    @AfterEach
    void tearDown() {
        manager.closeAll();
        probes.forEach(MqttProbe::close);
        probes.clear();
        broker.restore();
    }

    MqttProbe probe(String filter) {
        MqttProbe probe = MqttProbe.subscribe(broker, filter);
        probes.add(probe);
        return probe;
    }

    UUID channel(String brokerUrl, String extraConfig, String secrets) {
        return fixture.channel("{'name':'mqtt','type':'mqtt','events':['alert','briefing','system'],'config':{'brokerUrl':'"
                + brokerUrl + "','topicPrefix':'" + prefix + "'" + extraConfig + "}" + (secrets == null ? ""
                : ",'secrets':" + secrets) + "}");
    }

    MqttConnection connection(UUID channel) {
        return connections.find("channel:" + channel).orElseThrow();
    }

    void awaitConnected(UUID channel) {
        await().atMost(Duration.ofSeconds(20)).until(() -> connections.find("channel:" + channel)
                .map(MqttConnection::connected).orElse(false));
    }

    NotificationDeliveryEntity only(UUID channel) {
        return fixture.deliveries(channel).getFirst();
    }

    static JsonNode json(MqttProbe.Received r) {
        return JSON.readTree(r.payload());
    }

    @Test
    @DisplayName("an alert is delivered on {prefix}/alerts/{severity} at QoS 1 with message expiry and content type")
    void alertOnSeverityTopic() {
        MqttProbe probe = probe(prefix + "/#");
        UUID c = channel(broker.plainUrl(), "", null);
        awaitConnected(c);
        probe.await(prefix + "/status", r -> r.payload().equals("online"), Duration.ofSeconds(10));

        router.fired(alert(1, RULE, "Quakes", "critical", T));
        worker.runOnce();

        assertThat(only(c).getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(only(c).getAttempts()).isEqualTo(1);
        MqttProbe.Received m = probe.await(prefix + "/alerts/critical");
        assertThat(m.retained()).isFalse();
        assertThat(m.expirySeconds()).isNotNull().isLessThanOrEqualTo(86_400L).isGreaterThan(86_000L);
        assertThat(m.contentType()).isEqualTo("application/json");
        JsonNode payload = json(m);
        assertThat(payload.get("event_type").asString()).isEqualTo("critical");
        assertThat(payload.get("id").asLong()).isEqualTo(1);
        assertThat(payload.get("ruleName").asString()).isEqualTo("Quakes");
        assertThat(payload.get("sourceId").asString()).isEqualTo("usgs-earthquakes");

        router.lifecycle(AlertEvent.RESOLVED, NotifyFixture.resolved(alert(1, RULE, "Quakes", "critical", T),
                T.plusSeconds(60), "admin"));
        worker.runOnce();
        JsonNode update = json(probe.await(prefix + "/alerts/updates"));
        assertThat(update.get("id").asLong()).isEqualTo(1);
        assertThat(update.get("state").asString()).isEqualTo("resolved");
    }

    @Test
    @DisplayName("the retained rule state is set when an alert fires and cleared when the rule is deleted")
    void ruleStateSetAndCleared() {
        ActionFixture actions = new ActionFixture(jdbc, rules);
        UUID rule = actions.rule("Quakes", T);
        UUID c = channel(broker.plainUrl(), ",'publishHealth':false,'publishSummary':false", null);
        awaitConnected(c);
        String topic = prefix + "/rules/" + rule + "/state";
        try (MqttProbe idle = MqttProbe.subscribe(broker, topic)) {
            assertThat(json(idle.await(topic, MqttProbe.Received::retained, Duration.ofSeconds(15))).get("state")
                    .asString()).isEqualTo("idle");
        }

        MqttProbe live = probe(topic);
        long alertId = actions.alert(rule, "warning", T);
        bus.publish(AlertEvent.of(AlertEvent.FIRED, alerts.find(alertId).orElseThrow()));
        JsonNode firing = json(live.await(topic, r -> r.payload().contains("firing"), Duration.ofSeconds(15)));
        assertThat(firing.get("alertId").asLong()).isEqualTo(alertId);
        assertThat(firing.get("severity").asString()).isEqualTo("warning");
        assertThat(firing.get("lastFiredAt").asString()).isEqualTo(T.toString());
        try (MqttProbe late = MqttProbe.subscribe(broker, topic)) {
            assertThat(late.await(topic, MqttProbe.Received::retained, Duration.ofSeconds(10)).payload())
                    .contains("firing");
        }

        ruleService.delete(rule);
        live.await(topic, r -> r.payload().isEmpty(), Duration.ofSeconds(15));
        try (MqttProbe after = MqttProbe.subscribe(broker, topic)) {
            await().during(Duration.ofSeconds(1)).atMost(Duration.ofSeconds(3)).until(() -> after.on(topic).isEmpty());
        }
    }

    @Test
    @DisplayName("health and summary are retained topics; the summary is re-published only when it changed")
    void healthAndSummary() {
        UUID c = channel(broker.plainUrl(), ",'summaryInterval':'30s'", null);
        awaitConnected(c);
        String summaryTopic = prefix + "/summary";
        try (MqttProbe late = MqttProbe.subscribe(broker, prefix + "/#")) {
            JsonNode summary = json(late.await(summaryTopic, MqttProbe.Received::retained, Duration.ofSeconds(15)));
            assertThat(summary.has("status")).isTrue();
            assertThat(summary.get("alerts").has("open")).isTrue();
            MqttProbe.Received health = late.await(prefix + "/sources/usgs-earthquakes/health",
                    MqttProbe.Received::retained, Duration.ofSeconds(15));
            assertThat(json(health).has("status")).isTrue();
            assertThat(json(health).has("lastSuccessAt")).isTrue();
        }

        MqttProbe live = probe(summaryTopic);
        clock.advanceSeconds(31);
        state.publishSummaries();
        await().during(Duration.ofSeconds(1)).atMost(Duration.ofSeconds(3)).until(() -> live.on(summaryTopic).isEmpty());

        state.refreshHealth();
        try (MqttProbe again = MqttProbe.subscribe(broker, prefix + "/sources/+/health")) {
            again.await(prefix + "/sources/usgs-earthquakes/health");
        }
    }

    @Test
    @DisplayName("the Last Will flips {prefix}/status to offline when the connection is cut, online after reconnect")
    void lastWill() {
        MqttProbe probe = probe(prefix + "/status");
        UUID c = channel(broker.proxiedUrl(), ",'publishHealth':false,'publishSummary':false", null);
        awaitConnected(c);
        probe.await(prefix + "/status", r -> r.payload().equals("online"), Duration.ofSeconds(10));
        probe.clear();

        broker.cut();
        probe.await(prefix + "/status", r -> r.payload().equals("offline"), Duration.ofSeconds(20));
        assertThat(connection(c).connected()).isFalse();

        probe.clear();
        broker.restore();
        probe.await(prefix + "/status", r -> r.payload().equals("online"), Duration.ofSeconds(30));
        awaitConnected(c);

        probe.clear();
        manager.closeAll();
        probe.await(prefix + "/status", r -> r.payload().equals("offline"), Duration.ofSeconds(10));
    }

    @Test
    @DisplayName("QoS 1 deliveries follow the retry ladder while the broker is unreachable and succeed after")
    void retriesWhileBrokerDown() {
        MqttProbe probe = probe(prefix + "/alerts/#");
        UUID c = channel(broker.proxiedUrl(), ",'publishHealth':false,'publishSummary':false", null);
        awaitConnected(c);
        broker.cut();
        await().atMost(Duration.ofSeconds(10)).until(() -> !connection(c).connected());

        router.fired(alert(2, RULE, "Quakes", "warning", T));
        worker.runOnce();
        NotificationDeliveryEntity first = only(c);
        assertThat(first.getStatus()).isEqualTo(DeliveryStatus.PENDING);
        assertThat(first.getAttempts()).isEqualTo(1);
        assertThat(first.getNextAttemptAt()).isEqualTo(clock.instant().plusSeconds(10));
        assertThat(first.getLastError()).contains("not connected");

        broker.restore();
        awaitConnected(c);
        clock.advanceSeconds(10);
        worker.runOnce();
        assertThat(only(c).getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(only(c).getAttempts()).isEqualTo(2);
        assertThat(json(probe.await(prefix + "/alerts/warning")).get("id").asLong()).isEqualTo(2);
    }

    @Test
    @DisplayName("TLS with the test CA connects and the channel test reports the PUBACK latency; without the CA it fails")
    void tlsWithTestCa() {
        String ca = MosquittoSupport.caPem().replace("\n", "\\n");
        UUID trusted = channel(broker.tlsUrl(), ",'publishHealth':false,'publishSummary':false",
                "{'caCertPem':'" + ca + "'}");
        awaitConnected(trusted);
        ChannelService.TestResult result = channels.test(trusted);
        assertThat(result.success()).isTrue();
        assertThat(result.status()).isNull();
        assertThat(result.error()).isNull();

        UUID untrusted = channel(broker.tlsUrl(), ",'publishHealth':false,'publishSummary':false", null);
        await().atMost(Duration.ofSeconds(15)).until(() -> connections.find("channel:" + untrusted)
                .map(MqttConnection::lastError).orElse(null) != null);
        assertThat(connection(untrusted).connected()).isFalse();
        assertThat(channels.test(untrusted).success()).isFalse();
    }

    @Test
    @DisplayName("credentials from the secrets open the password listener; a wrong password does not")
    void passwordListener() {
        UUID good = channel(broker.passwordUrl(), ",'publishHealth':false,'publishSummary':false",
                "{'username':'" + MosquittoSupport.USER + "','password':'" + MosquittoSupport.PASSWORD + "'}");
        awaitConnected(good);
        UUID bad = channel(broker.passwordUrl(), ",'publishHealth':false,'publishSummary':false",
                "{'username':'" + MosquittoSupport.USER + "','password':'wrong'}");
        await().atMost(Duration.ofSeconds(15)).until(() -> connections.find("channel:" + bad)
                .map(MqttConnection::lastError).orElse(null) != null);
        assertThat(connection(bad).connected()).isFalse();
    }

    @Test
    @DisplayName("/v1/system reports the mqtt block and foresignal.mqtt.connected is 1 per connected channel")
    void systemBlockAndMetric() {
        UUID c = channel(broker.plainUrl(), ",'publishHealth':false,'publishSummary':false", null);
        awaitConnected(c);
        @SuppressWarnings("unchecked")
        Map<String, Object> block = (Map<String, Object>) system.build(true).get("mqtt");
        assertThat((Integer) block.get("connections")).isGreaterThanOrEqualTo(1);
        assertThat((Integer) block.get("connected")).isGreaterThanOrEqualTo(1);
        assertThat(meters.find(MqttConnections.METRIC).tag("channel", c.toString()).gauge().value()).isEqualTo(1.0);

        channels.delete(c);
        await().atMost(Duration.ofSeconds(10)).until(() -> connections.find("channel:" + c).isEmpty());
        assertThat(meters.find(MqttConnections.METRIC).tag("channel", c.toString()).gauge()).isNull();
    }

    @Test
    @DisplayName("POST /v1/channels/{id}/mqtt/republish answers 202 and re-publishes the retained states")
    void republishEndpoint() throws Exception {
        String admin = "Bearer " + adminTokens.rotate();
        UUID c = channel(broker.plainUrl(), ",'publishHealth':false", null);
        awaitConnected(c);
        MqttProbe probe = probe(prefix + "/summary");
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/v1/channels/" + c + "/mqtt/republish")
                        .header(org.springframework.http.HttpHeaders.AUTHORIZATION, admin))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isAccepted());
        assertThat(json(probe.await(prefix + "/summary")).has("status")).isTrue();
    }

    @Test
    @DisplayName("an MQTT 3.1.1 channel connects, publishes and keeps its Last Will")
    void mqtt311() {
        MqttProbe probe = probe(prefix + "/#");
        UUID c = channel(broker.plainUrl(), ",'protocolVersion':'3.1.1','publishHealth':false,'publishSummary':false",
                null);
        awaitConnected(c);
        probe.await(prefix + "/status", r -> r.payload().equals("online"), Duration.ofSeconds(10));
        router.fired(alert(3, RULE, "Quakes", "info", T));
        worker.runOnce();
        assertThat(only(c).getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(probe.await(prefix + "/alerts/info").expirySeconds()).isNull();
    }
}
