package ai.foresignal.notify.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.mqtt.MqttConnections;
import ai.foresignal.mqtt.MqttProperties;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.notify.ChannelInputParser;
import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.ChannelTypeRegistry;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotifyFixtures;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.notify.mesh.MeshtasticDownlinks;
import ai.foresignal.notify.mesh.cbor.MeshCborProfile;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.testsupport.mqtt.MosquittoSupport;
import ai.foresignal.testsupport.mqtt.MqttProbe;
import ai.foresignal.testsupport.net.TestOutbound;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.InetAddress;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code meshtastic} channel type (prompt 084): the exact JSON downlink envelope and topic, received by a real
 * Mosquitto broker (060's Testcontainer), and every {@code MESH_PUBLIC_CHANNEL} rule.
 */
class MeshtasticChannelTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    final MeshtasticChannel type = new MeshtasticChannel(TestOutbound.offlinePolicy());

    static Map<String, Object> config() {
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("brokerUrl", "mqtt://192.168.1.20:1883");
        c.put("root", "msh/local");
        c.put("gatewayNodeNum", "2864434397");
        c.put("channelIndex", 2);
        c.put("channelName", "Family");
        c.put("privateChannelConfirmed", true);
        c.put("region", "EU_868");
        return c;
    }

    static ChannelSettings settings(Map<String, Object> config) {
        return new ChannelSettings(NotifyFixtures.CHANNEL, "mesh", config, Map.of(), ZoneId.of("UTC"), 99,
                NotifyFixtures.T, NotifyFixtures.ORIGIN, "Foresignal/test");
    }

    static NotificationMessage fired() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("alertId", 7);
        data.put("ruleName", "Quakes");
        data.put("kind", "earthquake");
        data.put("sourceId", "usgs-earthquakes");
        data.put("firedAt", "2026-05-01T12:00:00Z");
        data.put("region", Map.of("near", Map.of("name", "Austin")));
        return NotificationMessage.builder("alert", "alert.fired", "[warning] Quakes", "warning").id(42L)
                .shortTitle("Quakes").data(data).createdAt(NotifyFixtures.T).alertId(7L).build();
    }

    @Test
    @DisplayName("a private config validates; the catalog lists the fields")
    void valid() {
        assertThat(type.validate(config(), Map.of())).isEmpty();
        assertThat(type.validationCode(config(), Map.of())).isNull();
        assertThat(type.spec().configFields()).extracting(f -> f.name()).contains("brokerUrl", "gatewayNodeNum",
                "channelIndex", "channelName", "privateChannelConfirmed", "region", "airtimePreset",
                "dutyCyclePercent", "profile", "minSeverity", "reportsEnabled", "allowedSenders");
        assertThat(type.capabilities().editInPlace()).isFalse();
        assertThat(type.acceptsUpdate("alert.resolved")).isTrue();
        assertThat(type.acceptsUpdate("alert.acknowledged")).isFalse();
    }

    @Test
    @DisplayName("every public-channel rule answers MESH_PUBLIC_CHANNEL")
    void publicRules() {
        assertPublic(Map.of("brokerUrl", "mqtt://mqtt.meshtastic.org:1883"), "config.brokerUrl");
        assertPublic(Map.of("brokerUrl", "mqtts://eu.meshtastic.org:8883"), "config.brokerUrl");
        assertPublic(Map.of("brokerUrl", "mqtt://8.8.8.8:1883"), "config.brokerUrl");
        assertPublic(Map.of("channelIndex", 0), "config.channelIndex");
        assertPublic(Map.of("channelName", ""), "config.channelName");
        for (String preset : List.of("LongFast", "LongSlow", "LongModerate", "MediumFast", "MediumSlow", "ShortFast",
                "ShortSlow", "ShortTurbo", "VLongSlow", "longfast")) {
            assertPublic(Map.of("channelName", preset), "config.channelName");
        }
        assertPublic(Map.of("licensedMode", true), "config.licensedMode");
        assertPublic(Map.of("privateChannelConfirmed", false), "config.privateChannelConfirmed");
    }

    private void assertPublic(Map<String, Object> change, String field) {
        Map<String, Object> c = config();
        c.putAll(change);
        List<FieldError> errors = type.validate(c, Map.of());
        assertThat(errors).extracting(FieldError::field).as(change.toString()).contains(field);
        assertThat(type.validationCode(c, Map.of())).isEqualTo(ErrorCodes.MESH_PUBLIC_CHANNEL);
        ChannelInputParser parser = new ChannelInputParser(new ChannelTypeRegistry(List.of(type)), JSON,
                TestOutbound.offlinePolicy());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "mesh");
        body.put("type", "meshtastic");
        body.put("config", c);
        assertThatThrownBy(() -> parser.parse(JSON.valueToTree(body), null))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ErrorCodes.MESH_PUBLIC_CHANNEL));
    }

    @Test
    @DisplayName("other problems stay VALIDATION_FAILED: node numbers, root, region, share above the ceiling")
    void otherProblems() {
        Map<String, Object> c = config();
        c.put("gatewayNodeNum", "4294967296");
        c.put("root", "msh/#");
        c.put("dutyCyclePercent", 12.5);
        assertThat(type.validate(c, Map.of())).extracting(FieldError::field)
                .contains("config.gatewayNodeNum", "config.root", "config.dutyCyclePercent");
        assertThat(type.validationCode(c, Map.of())).isNull();
        Map<String, Object> senders = config();
        senders.put("reportsEnabled", true);
        assertThat(type.validate(senders, Map.of())).extracting(FieldError::field).contains("config.allowedSenders");
        senders.put("allowedSenders", Map.of("!a1b2c3d4", "ack", "nobody", "report"));
        assertThat(type.validate(senders, Map.of())).extracting(FieldError::message)
                .anyMatch(m -> m.contains("'nobody' is not a sender"));
    }

    @Test
    @DisplayName("render: the JSON downlink on {root}/2/json/mqtt/ with from, sendtext, payload and channel")
    void envelope() {
        PreparedRequest request = type.render(fired(), settings(config()));
        PreparedRequest.Mesh mesh = (PreparedRequest.Mesh) request.body();
        assertThat(mesh.topic()).isEqualTo("msh/local/2/json/mqtt/");
        assertThat(request.bodyText()).isEqualTo("{\"from\":2864434397,\"type\":\"sendtext\","
                + "\"payload\":\"!! Quakes Austin 1200Z #0007\",\"channel\":2}");
        assertThat(mesh.shortCode()).isEqualTo("0007");
        assertThat(mesh.deliveryId()).isEqualTo(99);
        assertThat(mesh.airtimeBytes()).isEqualTo("!! Quakes Austin 1200Z #0007".length());

        Map<String, Object> dm = config();
        dm.put("toNodeNum", "16");
        dm.put("topicSuffixNodeId", true);
        PreparedRequest direct = type.render(fired(), settings(dm));
        assertThat(((PreparedRequest.Mesh) direct.body()).topic()).isEqualTo("msh/local/2/json/mqtt/!aabbccdd");
        assertThat(direct.bodyText()).endsWith(",\"channel\":2,\"to\":16}");

        Map<String, Object> cbor = config();
        cbor.put("profile", "cbor");
        JsonNode json = JSON.readTree(type.render(fired(), settings(cbor)).bodyText());
        String payload = json.get("payload").asString();
        assertThat(payload).startsWith("~E1 ");
        assertThat(payload.length()).isLessThanOrEqualTo(200);
        assertThat(MeshCborProfile.decode(Base64.getUrlDecoder().decode(payload.substring(4))).shortCode())
                .isEqualTo("0007");
    }

    private final MosquittoSupport broker = MosquittoSupport.get();
    private final MqttConnections connections = new MqttConnections(MqttProperties.defaults(),
            OutboundUrlPolicy.withoutOverrides(InetAddress::getAllByName),
            new StaticListableBeanFactory().getBeanProvider(MeterRegistry.class), Clock.systemUTC());

    @AfterEach
    void closeConnections() {
        connections.closeAll();
    }

    @Test
    @DisplayName("an embedded broker receives the exact envelope on the exact topic, once, from foresignal-<id>-mesh")
    void broker() throws Exception {
        InstallIdService installId = Mockito.mock(InstallIdService.class);
        Mockito.when(installId.installId()).thenReturn("abcd1234");
        OutboundUrlPolicy policy = OutboundUrlPolicy.withoutOverrides(InetAddress::getAllByName);
        MeshtasticDownlinks downlinks = new MeshtasticDownlinks(connections, MqttProperties.defaults(), installId,
                policy);
        Map<String, Object> c = config();
        c.put("brokerUrl", broker.plainUrl());
        PreparedRequest.Mesh mesh = (PreparedRequest.Mesh) new MeshtasticChannel(policy).render(fired(), settings(c))
                .body();
        try (MqttProbe probe = MqttProbe.subscribe(broker, "msh/local/2/json/mqtt/#")) {
            ChannelResponse response = downlinks.publish(c, Map.of(), mesh);
            assertThat(response.status()).isEqualTo(200);
            assertThat(response.body()).isEqualTo("PUBACK");
            MqttProbe.Received received = probe.await("msh/local/2/json/mqtt/", r -> true, Duration.ofSeconds(10));
            assertThat(received.payload()).isEqualTo("{\"from\":2864434397,\"type\":\"sendtext\","
                    + "\"payload\":\"!! Quakes Austin 1200Z #0007\",\"channel\":2}");
            assertThat(received.retained()).isFalse();
            downlinks.publish(c, Map.of(), mesh);
            assertThat(connections.keys()).as("one client per broker").hasSize(1);
            assertThat(MeshtasticDownlinks.clientId("abcd1234")).isEqualTo("foresignal-abcd1234-mesh");
        }
    }
}
