package ai.foresignal.mqtt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The shared MQTT client pieces of prompt 060 that need no broker. */
class MqttConnectionsTest {

    @Test
    @DisplayName("backoff doubles from 1 s and caps at 120 s")
    void backoffSchedule() {
        long[] expected = {1, 2, 4, 8, 16, 32, 64, 120, 120, 120};
        for (int attempt = 1; attempt <= expected.length; attempt++) {
            assertThat(MqttBackoff.base(attempt)).as("attempt " + attempt).isEqualTo(Duration.ofSeconds(expected[attempt - 1]));
        }
        assertThat(MqttBackoff.base(0)).isEqualTo(Duration.ofSeconds(1));
        assertThat(MqttBackoff.base(1000)).isEqualTo(Duration.ofSeconds(120));
    }

    @Test
    @DisplayName("jitter stays within ±20 % and never exceeds 120 s")
    void backoffJitter() {
        assertThat(MqttBackoff.delay(1, 0.0)).isEqualTo(Duration.ofMillis(800));
        assertThat(MqttBackoff.delay(1, 0.5)).isEqualTo(Duration.ofMillis(1000));
        assertThat(MqttBackoff.delay(1, 0.999999)).isLessThanOrEqualTo(Duration.ofMillis(1200));
        assertThat(MqttBackoff.delay(4, 0.0)).isEqualTo(Duration.ofMillis(6400));
        assertThat(MqttBackoff.delay(20, 1.0)).isEqualTo(Duration.ofSeconds(120));
        assertThat(MqttBackoff.delay(20, 0.0)).isEqualTo(Duration.ofSeconds(96));
    }

    @Test
    @DisplayName("client id: foresignal-{installId}-{first 8 hex digits of the channel id}")
    void clientIdFormat() {
        UUID channel = UUID.fromString("1a2b3c4d-5e6f-7081-92a3-b4c5d6e7f809");
        assertThat(MqttConnections.clientId("abcd1234", channel)).isEqualTo("foresignal-abcd1234-1a2b3c4d");
        assertThat(MqttConnections.clientId("abcd1234", channel)).matches("^foresignal-[a-z0-9]{8}-[0-9a-f]{8}$");
    }

    @Test
    @DisplayName("protocol 3.1.1 is honored and anything else falls back to 5")
    void protocolFallback() {
        MqttEndpoint endpoint = MqttEndpoint.parse("mqtt://broker.lan");
        assertThat(spec("3.1.1", endpoint).v3()).isTrue();
        assertThat(spec("3.1.1", endpoint).protocolVersion()).isEqualTo(MqttConnectionSpec.V311);
        assertThat(spec("5", endpoint).v3()).isFalse();
        assertThat(spec(null, endpoint).protocolVersion()).isEqualTo(MqttConnectionSpec.V5);
        assertThat(spec("4", endpoint).protocolVersion()).isEqualTo(MqttConnectionSpec.V5);
        assertThat(spec("5", endpoint).toString()).doesNotContain("secret");
    }

    @Test
    @DisplayName("an MQTT 3.1.1 client is built without connecting, and publishing before start fails fast")
    void v311ClientBuilds() throws Exception {
        MqttConnection connection = new MqttConnection(spec("3.1.1", MqttEndpoint.parse("mqtt://127.0.0.1:1")),
                MqttProperties.defaults(), () -> null, Executors.newVirtualThreadPerTaskExecutor(), () -> 0.5,
                Clock.systemUTC());
        assertThat(connection.connected()).isFalse();
        assertThat(connection.publish(MqttMessage.retained("t", "x", null))).isCompletedExceptionally();
        connection.close();
        assertThat(connection.closed()).isTrue();
    }

    @Test
    @DisplayName("a connection the policy refuses is not started and reports the reason")
    void policyRefusal() {
        MqttConnection connection = new MqttConnection(spec("5", MqttEndpoint.parse("mqtt://169.254.169.254")),
                MqttProperties.defaults(), () -> "metadata address", Executors.newVirtualThreadPerTaskExecutor(),
                () -> 0.5, Clock.systemUTC());
        connection.start();
        assertThat(connection.connected()).isFalse();
        assertThat(connection.lastError()).isEqualTo("metadata address");
        connection.close();
    }

    @Test
    @DisplayName("endpoints: default ports per scheme, TLS and WebSocket flags, the WebSocket path")
    void endpoints() {
        assertThat(MqttEndpoint.parse("mqtt://b")).isEqualTo(new MqttEndpoint("mqtt", "b", 1883, false, false, ""));
        assertThat(MqttEndpoint.parse("MQTTS://b")).isEqualTo(new MqttEndpoint("mqtts", "b", 8883, true, false, ""));
        assertThat(MqttEndpoint.parse("ws://b/mqtt")).isEqualTo(new MqttEndpoint("ws", "b", 80, false, true, "mqtt"));
        assertThat(MqttEndpoint.parse("wss://b:9001/")).isEqualTo(new MqttEndpoint("wss", "b", 9001, true, true, ""));
        assertThat(MqttEndpoint.parse("mqtt://[fd00::1]:1884").host()).isEqualTo("fd00::1");
        assertThatThrownBy(() -> MqttEndpoint.parse("https://b")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MqttEndpoint.parse(" ")).hasMessage("is required");
    }

    @Test
    @DisplayName("topic filters: + matches one level, # the rest")
    void topicMatching() {
        assertThat(MqttConnection.matches("homeassistant/status", "homeassistant/status")).isTrue();
        assertThat(MqttConnection.matches("a/+/c", "a/b/c")).isTrue();
        assertThat(MqttConnection.matches("a/+/c", "a/b/d")).isFalse();
        assertThat(MqttConnection.matches("a/#", "a/b/c")).isTrue();
        assertThat(MqttConnection.matches("a/#", "a")).isTrue();
        assertThat(MqttConnection.matches("a/b", "a/b/c")).isFalse();
        assertThat(MqttConnection.matches("a/b/c", "a/b")).isFalse();
    }

    @Test
    @DisplayName("TLS: a blank CA keeps the system trust; garbage is refused; messages are checked")
    void tlsAndMessages() {
        assertThat(MqttTls.trustManagers(null)).isNull();
        assertThat(MqttTls.trustManagers(" ")).isNull();
        assertThat(MqttTls.problem("not a pem")).isNotNull();
        assertThat(MqttTls.problem(null)).isNull();
        assertThatThrownBy(() -> new MqttMessage("t", null, 2, false, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(new MqttMessage("t", null, 1, true, null, null).payload()).isEmpty();
    }

    static MqttConnectionSpec spec(String version, MqttEndpoint endpoint) {
        return new MqttConnectionSpec("channel:x", "x", "foresignal-abcd1234-00000000", endpoint, version, "user",
                "secret", null, "foresignal/status");
    }
}
