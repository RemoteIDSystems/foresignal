package ai.foresignal.mqtt.channel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.mqtt.MqttConnection;
import ai.foresignal.mqtt.MqttConnections;
import ai.foresignal.notify.ChannelRateLimiter;
import ai.foresignal.notify.ChannelService;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.store.NotificationDeliveryRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.mqtt.MosquittoSupport;
import ai.foresignal.testsupport.mqtt.MqttProbe;
import ai.foresignal.testsupport.notify.NotifyFixture;
import ai.foresignal.testsupport.notify.NotifyTestConfig;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Home Assistant MQTT discovery against a real Mosquitto broker (prompt 060): the device config on connect and
 * again after Home Assistant's birth message, the debounce of rule changes, component removal and device
 * removal when discovery is turned off or the channel is deleted. Every test uses its own discovery prefix.
 */
@IntegrationTest
@Import(NotifyTestConfig.class)
@Timeout(120)
class HaDiscoveryIT {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired ChannelService channels;
    @Autowired ChannelRateLimiter limiter;
    @Autowired NotificationDeliveryRepository deliveries;
    @Autowired MqttChannelManager manager;
    @Autowired MqttConnections connections;
    @Autowired TriggerRuleService rules;
    @Autowired InstallIdService installId;
    @Autowired AppSettingService settings;
    @Autowired MutableClock clock;

    MosquittoSupport broker;
    NotifyFixture fixture;
    String prefix;
    String haPrefix;
    String deviceTopic;
    final List<MqttProbe> probes = new ArrayList<>();

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        clock.setInstant(T);
        broker = MosquittoSupport.get();
        manager.resync();
        fixture = new NotifyFixture(channels, deliveries, limiter);
        String id = UUID.randomUUID().toString().substring(0, 8);
        prefix = "fs" + id;
        haPrefix = "ha" + id;
        deviceTopic = haPrefix + "/device/foresignal_" + installId.installId() + "/config";
    }

    @AfterEach
    void tearDown() {
        manager.closeAll();
        probes.forEach(MqttProbe::close);
        probes.clear();
    }

    MqttProbe probe(String filter) {
        MqttProbe probe = MqttProbe.subscribe(broker, filter);
        probes.add(probe);
        return probe;
    }

    String body(boolean discovery) {
        return "{'name':'ha','type':'mqtt','config':{'brokerUrl':'" + broker.plainUrl() + "','topicPrefix':'" + prefix
                + "','haDiscovery':" + discovery + ",'haPrefix':'" + haPrefix
                + "','publishHealth':false,'publishSummary':false}}";
    }

    UUID channel() {
        UUID id = fixture.channel(body(true));
        await().atMost(Duration.ofSeconds(20)).until(() -> connections.find("channel:" + id)
                .map(MqttConnection::connected).orElse(false));
        return id;
    }

    UUID rule(String name, String severity) {
        return rules.create(JSON.readTree("{\"name\":\"" + name + "\",\"severity\":\"" + severity
                + "\",\"kinds\":[\"earthquake\"],\"conditions\":[{\"path\":\"mag\",\"op\":\"exists\",\"value\":true}]}"))
                .rule().getId();
    }

    List<MqttProbe.Received> configs(MqttProbe probe) {
        return probe.on(deviceTopic);
    }

    @Test
    @DisplayName("the device config is published on connect (not retained) and again after Home Assistant's birth")
    void onConnectAndBirth() {
        MqttProbe probe = probe(haPrefix + "/#");
        channel();
        JsonNode config = JSON.readTree(probe.await(deviceTopic).payload());
        assertThat(config.get("dev").get("ids").get(0).asString()).isEqualTo("foresignal_" + installId.installId());
        assertThat(config.get("o").get("name").asString()).isEqualTo("Foresignal");
        assertThat(config.get("availability").get(0).get("topic").asString()).isEqualTo(prefix + "/status");
        assertThat(config.get("cmps").has("alerts_open_critical")).isTrue();
        assertThat(config.get("cmps").get("alerts_critical").get("p").asString()).isEqualTo("event");
        try (MqttProbe late = MqttProbe.subscribe(broker, deviceTopic)) {
            await().during(Duration.ofSeconds(1)).atMost(Duration.ofSeconds(3)).until(() -> late.on(deviceTopic).isEmpty());
        }

        probe.clear();
        probe.publish(haPrefix + "/status", "online");
        probe.await(deviceTopic, r -> !r.payload().isEmpty(), Duration.ofSeconds(10));
        probe.clear();
        probe.publish(haPrefix + "/status", "offline");
        await().during(Duration.ofSeconds(6)).atMost(Duration.ofSeconds(8)).until(() -> configs(probe).isEmpty());
    }

    @Test
    @DisplayName("rule changes are debounced into one publish 5 s after the last; a deleted rule goes {p} first")
    void debounceAndComponentRemoval() {
        MqttProbe probe = probe(deviceTopic);
        channel();
        probe.await(deviceTopic);
        probe.clear();

        UUID a = rule("Alpha", "critical");
        UUID b = rule("Beta", "warning");
        await().during(Duration.ofSeconds(3)).atMost(Duration.ofSeconds(4)).until(() -> configs(probe).isEmpty());
        JsonNode config = JSON.readTree(probe.await(deviceTopic, r -> true, Duration.ofSeconds(10)).payload());
        assertThat(config.get("cmps").has("rule_" + a)).isTrue();
        assertThat(config.get("cmps").has("rule_" + b)).isTrue();
        assertThat(config.get("cmps").get("rule_" + a).get("device_class").asString()).isEqualTo("problem");
        await().during(Duration.ofSeconds(2)).atMost(Duration.ofSeconds(3)).until(() -> configs(probe).size() == 1);

        probe.clear();
        rules.delete(a);
        await().atMost(Duration.ofSeconds(12)).until(() -> configs(probe).size() >= 2);
        JsonNode first = JSON.readTree(configs(probe).get(0).payload());
        JsonNode second = JSON.readTree(configs(probe).get(1).payload());
        assertThat(first.get("cmps").get("rule_" + a)).isEqualTo(JSON.readTree("{\"p\":\"binary_sensor\"}"));
        assertThat(second.get("cmps").has("rule_" + a)).isFalse();
        assertThat(second.get("cmps").has("rule_" + b)).isTrue();
    }

    @Test
    @DisplayName("turning haDiscovery off publishes an empty retained payload on the device topic")
    void removalOnDisable() {
        MqttProbe probe = probe(deviceTopic);
        UUID id = channel();
        probe.await(deviceTopic);
        probe.clear();
        channels.replace(id, JSON.readTree(body(false).replace('\'', '"')));
        probe.await(deviceTopic, r -> r.payload().isEmpty(), Duration.ofSeconds(15));
    }

    @Test
    @DisplayName("deleting the channel removes the device; a retained config is cleared")
    void removalOnDelete() {
        MqttProbe probe = probe(deviceTopic);
        UUID id = fixture.channel(body(true).replace("'publishHealth'", "'haRetainDiscovery':true,'publishHealth'"));
        await().atMost(Duration.ofSeconds(20)).until(() -> connections.find("channel:" + id)
                .map(MqttConnection::connected).orElse(false));
        probe.await(deviceTopic);
        try (MqttProbe late = MqttProbe.subscribe(broker, deviceTopic)) {
            assertThat(late.await(deviceTopic, MqttProbe.Received::retained, Duration.ofSeconds(5)).payload()).isNotEmpty();
        }
        probe.clear();
        channels.delete(id);
        probe.await(deviceTopic, r -> r.payload().isEmpty(), Duration.ofSeconds(15));
        try (MqttProbe after = MqttProbe.subscribe(broker, deviceTopic)) {
            await().during(Duration.ofSeconds(1)).atMost(Duration.ofSeconds(3)).until(() -> after.on(deviceTopic).isEmpty());
        }
    }
}
