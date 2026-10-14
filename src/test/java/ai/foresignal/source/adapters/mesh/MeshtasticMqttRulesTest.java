package ai.foresignal.source.adapters.mesh;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.mqtt.MqttConnectionSpec;
import ai.foresignal.mqtt.MqttEndpoint;
import ai.foresignal.mqtt.MqttInbound;
import ai.foresignal.mqtt.MqttSessionOptions;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.HostPort;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.testsupport.stream.SocketStreamContext;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The Mesh.md rules of {@code meshtastic-mqtt} (prompt 083), enforced in code: the subscription limit, the root pattern,
 * the client id, the session (keepalive 120 s, clean, QoS 0, nothing published), the reconnect bounds, the hourly
 * connect cap and the 100 messages a minute inbound cap.
 */
class MeshtasticMqttRulesTest {

    private final MeshtasticMqttAdapter adapter = new MeshtasticMqttAdapter(null, () -> "abcd1234", new MeshMessageBus(),
            null, "");

    private static ValidatedParams params(Object... pairs) {
        Map<String, Object> values = new java.util.LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            values.put((String) pairs[i], pairs[i + 1]);
        }
        return ValidatedParams.of(values);
    }

    @Test
    @DisplayName("maxSubscriptions above 3 is refused, and root plus extraRoots must fit in it")
    void maxSubscriptions() {
        assertThat(adapter.checkParams(params(MeshtasticMqttAdapter.MAX_SUBSCRIPTIONS, 4)))
                .containsKey(MeshtasticMqttAdapter.MAX_SUBSCRIPTIONS);
        assertThat(adapter.checkParams(params(MeshtasticMqttAdapter.MAX_SUBSCRIPTIONS, 0)))
                .containsKey(MeshtasticMqttAdapter.MAX_SUBSCRIPTIONS);
        assertThat(adapter.checkParams(params(MeshtasticMqttAdapter.EXTRA_ROOTS, List.of("msh/EU_868"))))
                .containsKey(MeshtasticMqttAdapter.MAX_SUBSCRIPTIONS);
        assertThat(adapter.checkParams(params(MeshtasticMqttAdapter.EXTRA_ROOTS, List.of("msh/EU_868", "msh/ANZ"),
                MeshtasticMqttAdapter.MAX_SUBSCRIPTIONS, 3))).isEmpty();
        assertThat(adapter.checkParams(params(MeshtasticMqttAdapter.EXTRA_ROOTS,
                List.of("msh/EU_868", "msh/ANZ", "msh/JP"), MeshtasticMqttAdapter.MAX_SUBSCRIPTIONS, 3)))
                .containsKey(MeshtasticMqttAdapter.MAX_SUBSCRIPTIONS);
        // A repeated root is one subscription.
        assertThat(adapter.checkParams(params(MeshtasticMqttAdapter.EXTRA_ROOTS, List.of("msh/US")))).isEmpty();
        assertThat(adapter.checkParams(ValidatedParams.empty())).isEmpty();
    }

    @Test
    @DisplayName("wildcards and anything above the root are refused")
    void wildcardRoots() {
        for (String root : List.of("msh/#", "+/US", "msh/+", "#", "msh", "msh/US/2/json", "msh/US/CA/x", "other/US",
                "msh/US/#")) {
            assertThat(adapter.checkParams(params(MeshtasticMqttAdapter.ROOT, root))).as(root)
                    .containsKey(MeshtasticMqttAdapter.ROOT);
        }
        assertThat(adapter.checkParams(params(MeshtasticMqttAdapter.ROOT, "msh/US",
                MeshtasticMqttAdapter.EXTRA_ROOTS, List.of("msh/#"), MeshtasticMqttAdapter.MAX_SUBSCRIPTIONS, 2)))
                .containsKey(MeshtasticMqttAdapter.EXTRA_ROOTS);
        assertThat(adapter.checkParams(params(MeshtasticMqttAdapter.ROOT, "msh/US/CA"))).isEmpty();
        assertThat(MeshtasticMqttRules.subscriptions("msh/US", List.of("msh/EU_868", "msh/US")))
                .containsExactly("msh/US/2/json/#", "msh/EU_868/2/json/#");
    }

    @Test
    @DisplayName("client id foresignal-<installId>; keepalive 120 s; clean session; QoS 0; no status topic; 3.1.1")
    void session() {
        assertThat(MeshtasticMqttRules.clientId("abcd1234")).isEqualTo("foresignal-abcd1234");
        MqttConnectionSpec spec = adapter.spec();
        assertThat(spec.clientId()).isEqualTo("foresignal-abcd1234");
        assertThat(spec.options()).isEqualTo(MqttSessionOptions.subscriber(Duration.ofSeconds(120)));
        assertThat(spec.options().keepAlive()).isEqualTo(Duration.ofSeconds(120));
        assertThat(spec.options().cleanStart()).isTrue();
        assertThat(spec.options().subscribeQos()).isZero();
        assertThat(spec.options().autoReconnect()).isFalse();
        assertThat(spec.hasStatusTopic()).isFalse();
        assertThat(spec.v3()).isTrue();
        assertThat(spec.endpoint().host()).isEqualTo("mqtt.meshtastic.org");
        assertThat(spec.endpoint().port()).isEqualTo(1883);
        assertThat(spec.username()).isEqualTo("meshdev");
        assertThat(spec.password()).isEqualTo("large4cats");
        assertThat(spec.toString()).doesNotContain("large4cats");
    }

    @Test
    @DisplayName("a publisher spec still requires a status topic; a subscriber may omit it")
    void statusTopicRules() {
        MqttEndpoint endpoint = MqttEndpoint.parse("mqtt://127.0.0.1:1883");
        assertThatThrownBy(() -> new MqttConnectionSpec("k", null, "c", endpoint, null, null, null, null, null))
                .isInstanceOf(NullPointerException.class);
        assertThat(new MqttConnectionSpec("k", null, "c", endpoint, null, null, null, null, null,
                MqttSessionOptions.subscriber(Duration.ofSeconds(30))).hasStatusTopic()).isFalse();
        assertThat(new MqttConnectionSpec("k", null, "c", endpoint, null, null, null, null, "s").options())
                .isEqualTo(MqttSessionOptions.defaults());
        assertThatThrownBy(() -> new MqttSessionOptions(null, true, 2, false)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("reconnect from 15 s to 300 s with jitter, through the descriptor's ladder")
    void reconnectBounds() {
        SourceDescriptor d = adapter.descriptor();
        assertThat(d.reconnect()).isEqualTo(MeshtasticMqttRules.RECONNECT);
        assertThat(d.reconnect().base(0)).isEqualTo(Duration.ofSeconds(15));
        assertThat(d.reconnect().base(10)).isEqualTo(Duration.ofSeconds(300));
        for (int attempt = 0; attempt < 12; attempt++) {
            for (double u : new double[] {0.0, 0.5, 0.999}) {
                Duration delay = d.reconnect().delay(attempt, () -> u);
                assertThat(delay).isBetween(Duration.ofSeconds(12), Duration.ofSeconds(360));
            }
        }
    }

    @Test
    @DisplayName("the descriptor: STREAMING, the public broker only, budget 4/min and 100/day, sparse data normal")
    void descriptor() {
        SourceDescriptor d = adapter.descriptor();
        assertThat(d.mode()).isEqualTo(Mode.STREAMING);
        assertThat(d.allowedHosts()).containsExactly(HostPort.parse("mqtt.meshtastic.org:1883"));
        assertThat(d.defaultBudget().perMinute()).isEqualTo(4);
        assertThat(d.defaultBudget().perDay()).isEqualTo(100);
        assertThat(d.zeroResultsNormal()).isTrue();
        assertThat(d.privateSource()).isFalse();
        assertThat(d.embedFields()).isEmpty();
        assertThat(d.recordTtl()).isEqualTo(Duration.ofHours(6));
        assertThat(d.significantFields()).containsExactly("longName", "shortName", "role", "hwModel", "nodeType",
                "network", "lowBattery", "advertStatus");
        assertThat(d.tileProperties()).containsExactly("shortName", "role", "nodeType", "batteryLevel");
        assertThat(d.searchFields()).containsExactly("longName", "shortName");
        assertThat(d.coverageFromObservers()).isTrue();
    }

    @Test
    @DisplayName("the 100/min limiter drops the 101st message before parsing, and counts it")
    void inboundCap() {
        SimpleMeterRegistry meters = new SimpleMeterRegistry();
        MeshtasticStream stream = new MeshtasticStream(MeshtasticMqttAdapter.ID, false, null,
                new SlidingWindowLimiter(MeshtasticMqttRules.INBOUND_PER_MINUTE, Duration.ofMinutes(1)),
                new MeshNodeStates(), () -> meters);
        Instant t0 = Instant.parse("2026-10-14T00:00:00Z");
        byte[] body = "{\"from\":1,\"type\":\"telemetry\",\"payload\":{\"battery_level\":50}}"
                .getBytes(StandardCharsets.UTF_8);
        int records = 0;
        for (int i = 0; i < 101; i++) {
            records += stream.onMessage(new MqttInbound("msh/US/2/json/LongFast/!1", body, false),
                    t0.plusMillis(i * 100L)).size();
        }
        assertThat(records).isEqualTo(100);
        assertThat(stream.drops()).containsEntry("rate", 1L);
        assertThat(meters.get(MeshtasticStream.DROPPED_METRIC).tag("reason", "rate").counter().count()).isEqualTo(1.0);
        // A minute later the window has room again.
        assertThat(stream.onMessage(new MqttInbound("msh/US/2/json/LongFast/!1", body, false),
                t0.plusSeconds(70))).hasSize(1);
    }

    @Test
    @DisplayName("the sliding window: limit per window, refusals record nothing")
    void slidingWindow() {
        SlidingWindowLimiter limiter = new SlidingWindowLimiter(2, Duration.ofMinutes(1));
        Instant t0 = Instant.parse("2026-10-14T00:00:00Z");
        assertThat(limiter.tryAcquire(t0)).isTrue();
        assertThat(limiter.tryAcquire(t0.plusSeconds(10))).isTrue();
        assertThat(limiter.tryAcquire(t0.plusSeconds(20))).isFalse();
        assertThat(limiter.tryAcquire(t0.plusSeconds(60))).isTrue();
        assertThatThrownBy(() -> new SlidingWindowLimiter(0, Duration.ofMinutes(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("at most 12 connects an hour: the 13th start is refused before any budget is spent")
    void hourlyConnects() {
        SocketStreamContext ctx = new SocketStreamContext(adapter.descriptor(),
                Clock.fixed(Instant.parse("2026-10-14T00:00:00Z"), ZoneOffset.UTC), ValidatedParams.empty());
        // The test transport refuses every connect (no MqttConnections), so each start fails after the cap check.
        int refusedByCap = 0;
        for (int i = 0; i < 13; i++) {
            try {
                adapter.start(ctx, record -> {});
            } catch (AdapterException e) {
                if (e.getMessage().contains("connects in the last hour")) {
                    refusedByCap++;
                }
            } catch (RuntimeException e) {
                // the null MqttConnections of this test
            }
        }
        assertThat(refusedByCap).isEqualTo(1);
        assertThat(ctx.charged.get()).isEqualTo(12);
    }

    @Test
    @DisplayName("invalid parameters are refused at start too")
    void startValidates() {
        SocketStreamContext ctx = new SocketStreamContext(adapter.descriptor(), Clock.systemUTC(),
                params(MeshtasticMqttAdapter.ROOT, "msh/#"));
        assertThatThrownBy(() -> adapter.start(ctx, record -> {})).isInstanceOf(AdapterException.class)
                .hasMessageContaining("root");
        assertThat(ctx.charged.get()).isZero();
    }
}
